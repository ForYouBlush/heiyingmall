package com.heiying.heiyingmail.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面可能传过来的查询条件
 */
@Data
public class SearchParamVO {
    private String keyword;//页面传过来的全文匹配关键字
    private Long catalog3Id;//三级分类id
    /**
     * sort=saleCount_asc/desc  销量
     * sort=skuPrice_asc/desc   价格
     * sort=hotScore_asc/desc   热度
     */
    private String sort;//排序条件
    private Integer hasStock=1;//是否只显示有货    0：无货    1：有货    默认有货
    private String skuPrice;//价格区间查询
    private List<Long> brandId;//按照品牌id查询，可以多选
    private List<String> attrs;//按照属性进行筛选
    private Integer pageNum=1;//页码  默认第一页
    private String _queryString;//原生的所有查询条件
}
