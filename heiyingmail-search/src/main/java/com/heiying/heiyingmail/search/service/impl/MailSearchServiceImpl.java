package com.heiying.heiyingmail.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.heiying.common.to.es.SkuEsModel;
import com.heiying.common.utils.R;
import com.heiying.heiyingmail.search.config.ElasticSearchConfig;
import com.heiying.heiyingmail.search.constant.EsConstant;
import com.heiying.heiyingmail.search.feign.ProductFeignService;
import com.heiying.heiyingmail.search.service.MailSearchService;
import com.heiying.heiyingmail.search.vo.AttrResponseVO;
import com.heiying.heiyingmail.search.vo.BrandVO;
import com.heiying.heiyingmail.search.vo.SearchParamVO;
import com.heiying.heiyingmail.search.vo.SearchResponseVO;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MailSearchServiceImpl implements MailSearchService {
    @Resource
    RestHighLevelClient client;
    @Resource
    ProductFeignService productFeignService;

    @Override
    public SearchResponseVO search(SearchParamVO paramVO) {
        SearchResponseVO searchResponseVO = null;
        //准备检索请求
        SearchRequest searchRequest = buildSearchRequest(paramVO);

        try {
            //执行检索请求
            SearchResponse response = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);

            //分析响应数据封装成我们需要的格式
            searchResponseVO = buildSearchResult(response,paramVO);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return searchResponseVO;
    }

    /**
     * 构建结果数据
     *
     * @return
     */
    private SearchResponseVO buildSearchResult(SearchResponse response,SearchParamVO paramVO) {
        SearchResponseVO responseVO = new SearchResponseVO();
        //1、返回查到的所有商品
        SearchHits hits = response.getHits();
        List<SkuEsModel> esModels=new ArrayList<>();
        if (hits.getHits()!=null&&hits.getHits().length>0){
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel sku= JSON.parseObject(sourceAsString,SkuEsModel.class);
                if (!StringUtils.isEmpty(paramVO.getKeyword())){
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String s = skuTitle.getFragments()[0].string();
                    sku.setSkuTitle(s);
                }

                esModels.add(sku);
            }
        }
        responseVO.setProducts(esModels);


        //2、当前商品涉及到的所有属性信息
        List<SearchResponseVO.AttrVO> attrVOS=new ArrayList<>();
        ParsedNested attrAgg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResponseVO.AttrVO attrVO = new SearchResponseVO.AttrVO();
            //得到属性的id
            long attrId = bucket.getKeyAsNumber().longValue();
            //得到属性的名字
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            //得到属性的所有值
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                String keyAsString = item.getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());

            attrVO.setAttrId(attrId);
            attrVO.setAttrName(attrName);
            attrVO.setAttrValue(attrValues);
            attrVOS.add(attrVO);
        }
        responseVO.setAttrs(attrVOS);



        //3、当前商品涉及到的所有品牌信息
        List<SearchResponseVO.BrandVO> brandVOS=new ArrayList<>();
        ParsedLongTerms brandAgg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            SearchResponseVO.BrandVO brandVO = new SearchResponseVO.BrandVO();

            //得到品牌的id
            long brandId = bucket.getKeyAsNumber().longValue();
            //得到品牌的name
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            //得到品牌的图片img
            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();

            brandVO.setBrandId(brandId);
            brandVO.setBrandImg(brandImg);
            brandVO.setBrandName(brandName);
            brandVOS.add(brandVO);
        }
        responseVO.setBrands(brandVOS);


        //4、当前商品涉及到的所有分类信息
        ParsedLongTerms catalogAgg = response.getAggregations().get("catalog_agg");
        List<SearchResponseVO.CatalogVO> catalogVOS=new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalogAgg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResponseVO.CatalogVO catalogVO = new SearchResponseVO.CatalogVO();
            String keyAsString = bucket.getKeyAsString();
            //得到分类id
            catalogVO.setCatalogId(Long.parseLong(keyAsString));

            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            //得到分类名字
            catalogVO.setCatalogName(catalogName);
            catalogVOS.add(catalogVO);
        }
        responseVO.setCatalogs(catalogVOS);
        //======以上2-4从聚合信息获取=========

        //5、分页信息，页码
        responseVO.setPageNum(paramVO.getPageNum());
        //5、分页信息，总记录数
        long total = hits.getTotalHits().value;
        responseVO.setTotal(total);
        //5、分页信息，总页码    计算
        int totalPages = (int) (total % EsConstant.PRODUCT_PAGESIZE == 0 ? total / EsConstant.PRODUCT_PAGESIZE : (total / EsConstant.PRODUCT_PAGESIZE) + 1);
        responseVO.setTotalPages(totalPages);


        List<Integer> pagNavs=new ArrayList<>();
        for (int i = 1; i < totalPages; i++) {
            pagNavs.add(i);
        }
        responseVO.setPageNavs(pagNavs);

        //构建面包屑导航功能
        if (paramVO.getAttrs()!=null&&paramVO.getAttrs().size()>0){
            List<SearchResponseVO.NavVO> collect = paramVO.getAttrs().stream().map(attr -> {
                SearchResponseVO.NavVO navVO = new SearchResponseVO.NavVO();
                //attrs=2_5寸：6寸
                String[] s = attr.split("_");
                navVO.setNavValue(s[1]);
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                if (r.getCode()==0){
                    AttrResponseVO data = r.getData("attr", new TypeReference<AttrResponseVO>() {
                    });
                    navVO.setNavName(data.getAttrName());
                }else {
                    navVO.setNavName(s[0]);
                }
                String replace = replaceQueryString(paramVO, attr,"attrs");
                navVO.setLink("http://search.heiyingmail.com/list.html?"+replace);

                return navVO;
            }).collect(Collectors.toList());



            responseVO.setNavs(collect);
        }
        //品牌，分类
        if (paramVO.getBrandId()!=null&&paramVO.getBrandId().size()>0){
            List<SearchResponseVO.NavVO> navs = responseVO.getNavs();
            SearchResponseVO.NavVO navVO = new SearchResponseVO.NavVO();
            navVO.setNavName("品牌");
            //远程查询所有品牌
            R r = productFeignService.brandInfo(paramVO.getBrandId());
            if (r.getCode()==0){
                List<BrandVO> brand = r.getData("brand", new TypeReference<List<BrandVO>>() {
                });
                StringBuffer stringBuffer=new StringBuffer();
                String replace="";
                for (BrandVO brandVO : brand) {
                   stringBuffer.append(brandVO.getBrandName()+";");
                    replace=replaceQueryString(paramVO,brandVO.getBrandId()+"","brandId");
                }
                navVO.setNavValue(stringBuffer.toString());
                navVO.setLink("http://search.heiyingmail.com/list.html?"+replace);
            }
            navs.add(navVO);
        }


        return responseVO;
    }

    private String replaceQueryString(SearchParamVO paramVO, String value,String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            encode.replace("+","%20");//浏览器对空格的编码不一样
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String replace = paramVO.get_queryString().replace("&"+key+"=" + encode, "");
        return replace;
    }


    /**
     * 准备检索请求
     *
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParamVO paramVO) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();//构建DSL语句
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //查询：过滤（按照属性，分类，品牌，价格区间，库存）
        if (!StringUtils.isEmpty(paramVO.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", paramVO.getKeyword()));
        }

        if (paramVO.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", paramVO.getCatalog3Id()));
        }

        if (paramVO.getBrandId() != null && paramVO.getBrandId().size() > 0) {
            List<Long> brandId = paramVO.getBrandId();
            boolQuery.filter(QueryBuilders.termsQuery("brandId",brandId ));
        }

        if (paramVO.getAttrs() != null && paramVO.getAttrs().size() > 0) {

            for (String attrStr : paramVO.getAttrs()) {
                //传过来的数据可能为     attrs=1_5寸:8寸&3_8G:16G
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String attrId = s[0];//检索的属性id
                String[] attrValues = s[1].split(":");//检索用的值
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                //每一个都必须生成一个nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }

        }

        boolQuery.filter(QueryBuilders.termQuery("hasStock", paramVO.getHasStock() == 1));

        if (!StringUtils.isEmpty(paramVO.getSkuPrice())) {
            //  传过来的数据可能为   500_800/500_/_500
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] strings = paramVO.getSkuPrice().split("_");
            if (strings.length == 2) {
                 rangeQuery.gte(strings[0]).lte(strings[1]);
            } else if (strings.length == 1) {
                if (paramVO.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(strings[1]);
                }
                if (paramVO.getSkuPrice().endsWith("_")) {
                    rangeQuery.gte(strings[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }

        //把以前的条件都封装上
        searchSourceBuilder.query(boolQuery);

        //排序
        if (!StringUtils.isEmpty(paramVO.getSort())) {
            String sort = paramVO.getSort();
            //  传过来的数据可能为   hotScore_asc/desc
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(s[0], order);
        }

        //分页
        searchSourceBuilder.from((paramVO.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        //高亮
        if (!StringUtils.isEmpty(paramVO.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color: red'>");
            builder.postTags("</b>");
            searchSourceBuilder.highlighter(builder);
        }


        //品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg");
        brandAgg.field("brandId").size(50);
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        searchSourceBuilder.aggregation(brandAgg);

        //分类聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg");
        catalogAgg.field("catalogId").size(1);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        searchSourceBuilder.aggregation(catalogAgg);

        //属性聚合
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", "attrs");
        //聚合出所有的attrId
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        //聚合出所有attrId对应的名字
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        //聚合出所有attrId对应所有可能的属性值
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attrAgg.subAggregation(attrIdAgg);

        searchSourceBuilder.aggregation(attrAgg);

        System.out.println("构建的DSL" + searchSourceBuilder.toString());


        System.out.println("构建的DSL" + searchSourceBuilder.toString());
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
        return searchRequest;



    }
}
