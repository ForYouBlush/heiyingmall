package com.heiying.heiyingmail.product.service.impl;

import com.heiying.heiyingmail.product.service.CategoryBrandRelationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.Query;

import com.heiying.heiyingmail.product.dao.CategoryDao;
import com.heiying.heiyingmail.product.entity.CategoryEntity;
import com.heiying.heiyingmail.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Resource
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //找到一级分类
        List<CategoryEntity> level1Menus = entities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map(item->{
                    item.setChildren(getChildrens(item,entities));
                    return item;
                })
                .sorted((o1, o2) -> {
                    return (o1 == null ? 0 : o1.getSort()) - (o2 == null ? 0 : o2.getSort());
                })
                .collect(Collectors.toList());
        return level1Menus;
    }

    @Override
    public void removeMenusByIds(List<Long> asList) {
        //TODO 1、检查当前删除的菜单，是否有别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths=new ArrayList<>();
        paths=findParentPath(catelogId,paths);
        Collections.reverse(paths);
        return  paths.toArray(new Long[paths.size()]);
    }

    /**
     *      级联更新所有关联的数据
     * @param category
     */
    @Override
    @Transactional
    public void updateCascade(CategoryEntity category) {
            this.updateById(category);
            categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity entity = this.getById(catelogId);
        if (entity.getParentCid()!=0){
            findParentPath(entity.getParentCid(),paths);
        }
        return paths;
    }

    private  List<CategoryEntity> getChildrens(CategoryEntity parent, List<CategoryEntity> all){
        List<CategoryEntity> childrens = all.stream().
                filter(categoryEntity -> categoryEntity.getParentCid() == parent.getCatId())
                .map(item -> {      //map方法把找到的子菜单拆分
                    item.setChildren(getChildrens(item, all));//递归，把子菜单的子菜单也找到
                    return item;
                }).sorted((o1, o2) -> {
                    return (o1.getSort() == null ? 0 : o1.getSort()) - (o2.getSort() == null ? 0 : o2.getSort());
                }).collect(Collectors.toList());
        return childrens;
    }

}