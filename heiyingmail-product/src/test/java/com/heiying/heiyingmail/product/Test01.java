package com.heiying.heiyingmail.product;

import com.heiying.heiyingmail.product.dao.AttrGroupDao;
import com.heiying.heiyingmail.product.dao.SkuSaleAttrValueDao;
import com.heiying.heiyingmail.product.service.AttrGroupService;
import com.heiying.heiyingmail.product.service.CategoryService;
import com.heiying.heiyingmail.product.vo.SkuItemVO;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@SpringBootTest
@Slf4j
public class Test01 {
    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;


    @Test
    void testSkuSaleAttrValueDao(){
        List<SkuItemVO.SkuItemSaleAttrVO> saleAttrVOS = skuSaleAttrValueDao.getSaleAttrsBySpuId(11L);
        System.out.println(saleAttrVOS);
    }


    @Test
    void testAttrGroupDao(){
        List<SkuItemVO.SpuItemAttrGroupVO> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(13L, 225L);
        for (SkuItemVO.SpuItemAttrGroupVO spuItemAttrGroupVO : attrGroupWithAttrsBySpuId) {
            System.out.println(spuItemAttrGroupVO);
        }
    }

    @Test
    void testRedisson() {
        System.out.println(redissonClient);
    }


    @Test
    void testRedis() {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.set("hello","world"+ UUID.randomUUID().toString());
        String hello = valueOperations.get("hello");
        System.out.println(hello);
    }
    @Test
    void testFindPath() {
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完整路径，{}", Arrays.asList(catelogPath));
    }

    @Test
    void random() {
        HashSet set = new HashSet();
        for (int i = 0; i <1000 ; i++) {
            System.out.println(getNumber(set));
        }
    }

    public static String getNumber(Set set) {
        String s = 139 + "";
        int cardNumber;
        int max = 99999999;
        int min = 10000000;
            cardNumber = (int) (Math.random() * (max - min + 1)) + min;
            while (set.contains(cardNumber)){
                set.add(getNumber(set));
            }
        return s + cardNumber;
    }
}
