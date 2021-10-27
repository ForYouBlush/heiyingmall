package com.heiying.heiyingmail.member.dao;

import com.heiying.heiyingmail.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-13 14:43:33
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
