package com.heiying.heiyingmail.search.controller;

import com.heiying.heiyingmail.search.service.MailSearchService;
import com.heiying.heiyingmail.search.vo.SearchParamVO;
import com.heiying.heiyingmail.search.vo.SearchResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {
    @Autowired
    MailSearchService mailSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParamVO paramVO, Model model, HttpServletRequest request) {
        String queryString = request.getQueryString();
        paramVO.set_queryString(queryString);
        SearchResponseVO result = mailSearchService.search(paramVO);
        model.addAttribute("result", result);
        return "list";
    }
}
