package com.heiying.heiyingmail.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
/**
 *  二级分类VO
 */
public class Catalog2VO {
    private String catalog1Id;
    private List<Catalog3VO> catalog3List;
    private String id;
    private String name;


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    /**
     * 三级分类VO
     */
    public static class Catalog3VO{
        private String catalog2Id;
        private String id;
        private String name;
    }
}
