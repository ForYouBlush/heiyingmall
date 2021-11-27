package com.heiying.heiyingmail.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.heiying.heiyingmail.product.service.CategoryBrandRelationService;
import com.heiying.heiyingmail.product.vo.Catalog2VO;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.Query;

import com.heiying.heiyingmail.product.dao.CategoryDao;
import com.heiying.heiyingmail.product.entity.CategoryEntity;
import com.heiying.heiyingmail.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Resource
    CategoryBrandRelationService categoryBrandRelationService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedissonClient redissonClient;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //找到一级分类
        List<CategoryEntity> level1Menus = entities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map(item -> {
                    item.setChildren(getChildrens(item, entities));
                    return item;
                })
                .sorted((o1, o2) -> {
                    return (o1 == null ? 0 : o1.getSort()) - (o2 == null ? 0 : o2.getSort());
                })
                .collect(Collectors.toList());
        return level1Menus;
    }

    @Override
    public void removeMenusByIds(List<Long> asList) {
        //TODO 1、检查当前删除的菜单，是否有别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        paths = findParentPath(catelogId, paths);
        Collections.reverse(paths);
        return paths.toArray(new Long[paths.size()]);
    }

    /**
     * 级联更新所有关联的数据
     *
     * @param category
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "category",key = "'getLevel1Categorys'"),
            @CacheEvict(value = "category",key = "'getCatalogJson'")
    })
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }
    @Cacheable(value = "category",key = "#root.method.name")
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    @Override
    @Cacheable(value = "category",key = "#root.methodName")
    public Map<String, List<Catalog2VO>> getCatalogJson() {
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        List<CategoryEntity> categorys = getParent_cid(selectList, 0L);
        Map<String, List<Catalog2VO>> map = categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> level2Category = getParent_cid(selectList, v.getCatId());
            //找到二级分类
            List<Catalog2VO> catelog2VOS = null;
            if (level2Category != null&&level2Category.size()>0) {
                catelog2VOS = level2Category.stream().map(item -> {
                    List<CategoryEntity> level3Catelogry = getParent_cid(selectList, item.getCatId());
                    List<Catalog2VO.Catalog3VO> catelog3VOS = null;

                    //找到三级分类VO
                    if (level3Catelogry != null&&level3Catelogry.size()>0) {
                        catelog3VOS = level3Catelogry.stream().map(it -> {
                            Catalog2VO.Catalog3VO catelog3VO = new Catalog2VO.Catalog3VO(item.getCatId().toString(), it.getCatId().toString(), it.getName());
                            return catelog3VO;
                        }).collect(Collectors.toList());
                    }
                    //封装二级分类VO
                    Catalog2VO catelog2VO = new Catalog2VO(v.getCatId().toString(), catelog3VOS, item.getCatId().toString(), item.getName());
                    return catelog2VO;
                }).collect(Collectors.toList());
            }
            return catelog2VOS;
        }));
        return map;
    }

    //TODO 产生堆外内存溢出:OutofDirectMemoryError
    //1) 、springboot2.o以后默认使用Lettuce作为操作redis的客户端。它使用netty进行网络通信
    //2) 、lettuce的bug导致netty堆外内存溢出-Xmx300m; netty如果没有指定堆外内存，默认使用-Xmx300m
    //可以通过-Dio.netty.maxDirectMemory进行设置.
    //解决方案:不能使用-Dio.netty.maxDirectMemory只去调大堆外内存。
    // 1)、升级lettuce客户端。2)、切换使用jedis
    public Map<String, List<Catalog2VO>> getCatalogJsonByRedis() {
        /***    1、空结果缓存:解决缓存穿透
         *      2、设置过期时间（加随机值):解决缓存雪崩
         *      3、加锁:解决缓存击穿
         */
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        String catalogJSON = operations.get("catalogJSON");
        //如果redis中没有这个数据，则在数据库中查找并转为JSON字符串放入redis缓存中。
        if (StringUtils.isEmpty(catalogJSON)) {
            System.out.println("缓存不命中");
            Map<String, List<Catalog2VO>> catalogJsonFromDb = getCatalogJsonFromDbWithRedisLock();
            return catalogJsonFromDb;
        }
        System.out.println("缓存命中");
        //把从redis中拿到的数据转换成想要的对象
        Map<String, List<Catalog2VO>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2VO>>>() {
        });
        return result;
    }
    //通过redisson来开启分布式锁
    public Map<String, List<Catalog2VO>> getCatalogJsonFromDbWithRedissonLock() {
        //锁的名字一样就是同一把锁
        RLock lock = redissonClient.getLock("catalogJson-lock");
        lock.lock();
        //占分布式锁
        //加锁成功 执行业务
        //设置过期时间防止死锁（断电或出现异常）   必须保证原子性(同步)
//            stringRedisTemplate.expire("lock",30,TimeUnit.SECONDS);
        try {
            Map<String, List<Catalog2VO>> dataFromDb;
            dataFromDb = getDataFromDb();
            return dataFromDb;
        } finally {
            lock.unlock();
        }
    }

    //通过redis开启分布式锁
    public Map<String, List<Catalog2VO>> getCatalogJsonFromDbWithRedisLock() {
        //占分布式锁
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            //加锁成功 执行业务
            //设置过期时间防止死锁（断电或出现异常）   必须保证原子性(同步)
//            stringRedisTemplate.expire("lock",30,TimeUnit.SECONDS);
            Map<String, List<Catalog2VO>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            } finally {
                //            必须保证原子性，需要配合lua脚本 否则可能发生断电、异常等不可预见的问题导致锁没有删除
//            String lockValue = stringRedisTemplate.opsForValue().get("lock");
//            if (uuid.equals(lockValue))
//                stringRedisTemplate.delete("lock");
                //删除锁   原子性
                String script = "if redis.call('get', KEYS[1])== ARGV[1] then return redis.call('del', KEYS[1])else return 0 end";
                stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class)
                        , Arrays.asList("lock", uuid));
            }
            return dataFromDb;
        } else {
            //加锁失败...重试
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {

            }
            return getCatalogJsonFromDbWithLocalLock(); //自旋的方式
        }
    }


    //使用本地锁从数据库查询并封装数据
    public Map<String, List<Catalog2VO>> getCatalogJsonFromDbWithLocalLock() {
        synchronized (this) {
            //再判断一次，redis中有没有数据，如果没有则查询数据库
            String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
            if (!StringUtils.isEmpty(catalogJSON)) {
                //redis缓存不为null 则说明有数据，直接返回
                Map<String, List<Catalog2VO>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2VO>>>() {
                });
                return result;
            }
        }
        return getDataFromDb();

    }

    private Map<String, List<Catalog2VO>> getDataFromDb() {
        /**
         * 优化：将数据库的多次查询变为一次
         */
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        List<CategoryEntity> categorys = getParent_cid(selectList, 0L);
        Map<String, List<Catalog2VO>> map = categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> level2Category = getParent_cid(selectList, v.getCatId());


            //找到二级分类
            List<Catalog2VO> catelog2VOS = null;
            if (level2Category != null) {
                catelog2VOS = level2Category.stream().map(item -> {
                    List<CategoryEntity> level3Catelogry = getParent_cid(selectList, item.getCatId());
                    List<Catalog2VO.Catalog3VO> catelog3VOS = null;

                    //找到三级分类VO
                    if (level3Catelogry != null) {
                        catelog3VOS = level3Catelogry.stream().map(it -> {
                            Catalog2VO.Catalog3VO catelog3VO = new Catalog2VO.Catalog3VO(item.getCatId().toString(), it.getCatId().toString(), it.getName());
                            return catelog3VO;
                        }).collect(Collectors.toList());
                    }
                    //封装二级分类VO
                    Catalog2VO catelog2VO = new Catalog2VO(v.getCatId().toString(), null, item.getCatId().toString(), item.getName());
                    catelog2VO.setCatalog3List(catelog3VOS);
                    return catelog2VO;
                }).collect(Collectors.toList());
            }
            return catelog2VOS;
        }));
        return map;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
        return collect;
        //return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity entity = this.getById(catelogId);
        if (entity.getParentCid() != 0) {
            findParentPath(entity.getParentCid(), paths);
        }
        return paths;
    }

    private List<CategoryEntity> getChildrens(CategoryEntity parent, List<CategoryEntity> all) {
        List<CategoryEntity> childrens = all.stream().
                filter(categoryEntity -> categoryEntity.getParentCid() == parent.getCatId())
                .map(item -> {      //map方法把找到的子菜单拆分
                    item.setChildren(getChildrens(item, all));//递归，把子菜单的子菜单也找到
                    return item;
                }).sorted((o1, o2) -> {
                    return (o1.getSort() == null ? 0 : o1.getSort()) - (o2.getSort() == null ? 0 : o2.getSort());
                }).collect(Collectors.toList());
        return childrens;
    }

}