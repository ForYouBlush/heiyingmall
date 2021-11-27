package com.heiying.heiyingmail.search.vo;

import com.heiying.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResponseVO {
    //查询到的所有商品信息
    private List<SkuEsModel> products;
    /**
     * 以下是分页信息
     */
    private Integer pageNum;//当前页码
    private Long total;//总记录数
    private Integer totalPages;//总页码
    private List<Integer> pageNavs;//

    /**
     * 以下是返回给页面的所有信息
     */
    private List<BrandVO> brands;//当前查询到的结果，所有涉及到的品牌
    private List<CatalogVO> catalogs;//当前查询到的结果，所有涉及到的分类
    private List<AttrVO> attrs;//当前查询到的结果，所有涉及到的所有属性


    //面包屑数据
    private List<NavVO> navs=new ArrayList<>();


    @Data
    public static class NavVO{
        private String navName;
        private String navValue;
        private String link;
    }

    @Data
    public static class BrandVO{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatalogVO{
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class AttrVO{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }
}
