package com.heiying.heiyingmail.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heiying.common.utils.PageUtils;
import com.heiying.heiyingmail.member.entity.MemberEntity;
import com.heiying.heiyingmail.member.exception.PhoneExistException;
import com.heiying.heiyingmail.member.exception.UserNameExistException;
import com.heiying.heiyingmail.member.vo.MemberLoginVO;
import com.heiying.heiyingmail.member.vo.MemberRegistVO;

import java.util.Map;

/**
 * 会员
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-13 14:43:33
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVO userRegistVO);


    void checkUserUnique(String userName)throws UserNameExistException;

    void checkPhoneUnique(String phone) throws PhoneExistException;

    MemberEntity login(MemberLoginVO vo);
}

