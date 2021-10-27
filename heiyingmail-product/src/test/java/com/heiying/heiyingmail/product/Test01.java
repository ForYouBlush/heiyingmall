package com.heiying.heiyingmail.product;

import com.heiying.heiyingmail.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class Test01 {
    @Autowired
    CategoryService categoryService;
    @Test
    void testFindPath(){
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完整路径，{}", Arrays.asList(catelogPath));
    }
}
