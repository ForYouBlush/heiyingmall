package com.heiying.heiyingmail.product.service.impl;

import com.heiying.heiyingmail.product.entity.AttrEntity;
import com.heiying.heiyingmail.product.service.AttrService;
import com.heiying.heiyingmail.product.vo.AttrGroupWithAttrsVO;
import com.heiying.heiyingmail.product.vo.AttrVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.Query;

import com.heiying.heiyingmail.product.dao.AttrGroupDao;
import com.heiying.heiyingmail.product.entity.AttrGroupEntity;
import com.heiying.heiyingmail.product.service.AttrGroupService;

import javax.annotation.Resource;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Resource
    AttrService attrService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catlogId) {
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });}
        if (catlogId == 0) {
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
            return new PageUtils(page);
        } else {
            wrapper.eq("catelog_id", catlogId);
                IPage<AttrGroupEntity> page = this.page(
                        new Query<AttrGroupEntity>().getPage(params),
                        wrapper );

            return new PageUtils(page);
        }

    }

    /**
     * 根据分类id查出所有的分组以及这些组里面的属性
     * @param catlogId
     * @return
     */
    @Override
    public List<AttrGroupWithAttrsVO> getAttrGroupWithAttrsByCatlogId(Long catlogId) {
        //1、查询分组信息
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>()
                .eq("catelog_id", catlogId));
        //2、查询所有属性
        List<AttrGroupWithAttrsVO> collect = attrGroupEntities.stream().map(item -> {
            AttrGroupWithAttrsVO attrsVO = new AttrGroupWithAttrsVO();
            BeanUtils.copyProperties(item,attrsVO);
            List<AttrEntity> attr = attrService.getRelationAttr(attrsVO.getAttrGroupId());
            if (attr!=null&&attr.size()>0){
                attrsVO.setAttrs(attr);
            }
            return attrsVO;
        }).collect(Collectors.toList());
        return collect;
    }


}