package com.heiying.heiyingmail.search.service;

import com.heiying.heiyingmail.search.vo.SearchParamVO;
import com.heiying.heiyingmail.search.vo.SearchResponseVO;

public interface MailSearchService {
    /**
     *
     *@param paramVO   检索的所有参数
     * @return          返回检索的结果，里面包含所有页面需要的所有信息
     */
    SearchResponseVO search(SearchParamVO paramVO);
}
