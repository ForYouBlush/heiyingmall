package com.heiying.heiyingmail.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.heiying.common.constant.ProductConstant;
import com.heiying.heiyingmail.product.dao.AttrAttrgroupRelationDao;
import com.heiying.heiyingmail.product.dao.AttrGroupDao;
import com.heiying.heiyingmail.product.dao.CategoryDao;
import com.heiying.heiyingmail.product.entity.AttrAttrgroupRelationEntity;
import com.heiying.heiyingmail.product.entity.AttrGroupEntity;
import com.heiying.heiyingmail.product.entity.CategoryEntity;
import com.heiying.heiyingmail.product.service.CategoryService;
import com.heiying.heiyingmail.product.vo.AttrGroupRelationVO;
import com.heiying.heiyingmail.product.vo.AttrRespVO;
import com.heiying.heiyingmail.product.vo.AttrVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.Query;

import com.heiying.heiyingmail.product.dao.AttrDao;
import com.heiying.heiyingmail.product.entity.AttrEntity;
import com.heiying.heiyingmail.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Resource
    AttrAttrgroupRelationDao relationDao;
    @Resource
    CategoryDao categoryDao;
    @Resource
    AttrGroupDao attrGroupDao;
    @Resource
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveAttr(AttrVO attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        //1、保存基本数据
        this.save(attrEntity);
        //2、保存关联关系
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()&& attr.getAttrGroupId()!=null) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attrEntity.getAttrId());
            relationDao.insert(relationEntity);
        }
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catlogId, String type) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>()
                .eq("attr_type", "base"
                        .equalsIgnoreCase(type)
                        ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()
                        : ProductConstant.AttrEnum.ATTR_TYPE_SELL.getCode());
        if (catlogId != 0) {
            wrapper.eq("catelog_id", catlogId);
        }
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            wrapper.and((item) -> {
                item.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrRespVO> respVOList = records.stream().map((item) -> {
            AttrRespVO attrRespVO = new AttrRespVO();
            BeanUtils.copyProperties(item, attrRespVO);
            //设置分类和分组和名字
            if ("base".equals(type)) {
                AttrAttrgroupRelationEntity attr_id = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>()
                        .eq("attr_id", item.getAttrId()));
                if (attr_id != null&&attr_id.getAttrGroupId()!=null) {
                    AttrGroupEntity groupEntity = attrGroupDao.selectById(attr_id.getAttrGroupId());
                    attrRespVO.setGroupName(groupEntity.getAttrGroupName());
                }
            }
            CategoryEntity categoryEntity = categoryDao.selectById(item.getCatelogId());
            if (categoryEntity != null) {
                attrRespVO.setCatelogName(categoryEntity.getName());
            }
            return attrRespVO;
        }).collect(Collectors.toList());
        pageUtils.setList(respVOList);
        return pageUtils;
    }

    @Override
    public AttrRespVO getAttrInfo(Long attrId) {
        AttrRespVO respVO = new AttrRespVO();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, respVO);
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            //设置分组信息
            AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (relationEntity != null) {
                respVO.setAttrGroupId(relationEntity.getAttrGroupId());
                AttrGroupEntity groupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                if (groupEntity != null) {
                    respVO.setGroupName(groupEntity.getAttrGroupName());
                }
            }
        }
        //设置分类信息
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        respVO.setCatelogPath(catelogPath);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        if (categoryEntity != null) {
            respVO.setCatelogName(categoryEntity.getName());
        }
        return respVO;
    }

    @Override
    @Transactional
    public void updateAttr(AttrVO attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            //修改分组关联
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attr.getAttrId());
            Integer count = relationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            if (count > 0) {

                relationDao.update(relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            } else {
                relationDao.insert(relationEntity);
            }
        }


    }

    /**
     *      根据分组id查找关联的所有基本属性
     * @param attrgroupId
     * @return
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> relationEntityList = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>()
                .eq("attr_group_id", attrgroupId));
        List<Long> attrIds = relationEntityList.stream().map((item) -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        if (attrIds==null||attrIds.size()==0){
            return null;
        }
        List<AttrEntity> attrEntities = this.listByIds(attrIds);
        return attrEntities;
    }

    @Override
    public void deleteRelation(AttrGroupRelationVO[] vos) {
//        relationDao.delete(new QueryWrapper<>()
//                .eq("attr_id",).eq("attr_group_id",));
        List<AttrAttrgroupRelationEntity> collect = Arrays.asList(vos).stream().map((item) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        if (collect!=null||collect.size()!=0){
            relationDao.deleteBatchRelation(collect);

        }
    }

    /*
        获取当前分组没有关联的属性
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        //1、当前分组只能关联自己所属分类的所有属性
        AttrGroupEntity attrGroupEntity=attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //2、当前分组只能关联别的分组没有引用的属性
        //2.1）、当前分类下其他的分组
        List<AttrGroupEntity> groupEntities = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>()
                .eq("catelog_id", catelogId));
        List<Long> collect = groupEntities.stream().map(item -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());
        //2.2)、这些分组关联的属性
        List<AttrAttrgroupRelationEntity> relationEntities = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>()
                .in("attr_group_id", collect));
        List<Long> attrIds = relationEntities.stream().map(item -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        //2.3)、从当前分类的所有属性中移除这些属性
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>()
                .eq("catelog_id", catelogId).eq("attr_type",ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (attrIds!=null&&attrIds.size()>0){
            wrapper.notIn("attr_id", attrIds);
        }
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)){
            wrapper.and(item->{
                item.eq("attr_id",key).or().like("attr_name",key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }
}