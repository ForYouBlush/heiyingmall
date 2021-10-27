package com.heiying.heiyingmail.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heiying.common.utils.PageUtils;
import com.heiying.heiyingmail.product.entity.AttrGroupEntity;
import com.heiying.heiyingmail.product.vo.AttrGroupWithAttrsVO;
import com.heiying.heiyingmail.product.vo.AttrVO;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-12 15:12:54
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catlogId);


    List<AttrGroupWithAttrsVO> getAttrGroupWithAttrsByCatlogId(Long catlogId);
}

