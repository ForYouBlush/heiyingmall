package com.heiying.heiyingmail.member.service.impl;

import com.heiying.heiyingmail.member.entity.MemberLevelEntity;
import com.heiying.heiyingmail.member.exception.PhoneExistException;
import com.heiying.heiyingmail.member.exception.UserNameExistException;
import com.heiying.heiyingmail.member.service.MemberLevelService;
import com.heiying.heiyingmail.member.vo.MemberLoginVO;
import com.heiying.heiyingmail.member.vo.MemberRegistVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.Query;

import com.heiying.heiyingmail.member.dao.MemberDao;
import com.heiying.heiyingmail.member.entity.MemberEntity;
import com.heiying.heiyingmail.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelService memberLevelService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVO vo) {
        MemberEntity memberEntity = new MemberEntity();
        //设置默认等级
        MemberLevelEntity levelEntity = memberLevelService.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());


        //检查用户名和手机号是否唯一     为了让controller能感知异常：异常机制
        checkPhoneUnique(vo.getPhone());
        memberEntity.setMobile(vo.getPhone());

        checkUserUnique(vo.getUserName());
        memberEntity.setUsername(vo.getUserName());

        //设置密码，进行加密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);

        memberEntity.setNickname(vo.getUserName());
        memberEntity.setCreateTime(new Date());
        baseMapper.insert(memberEntity);
    }

    @Override
    public void checkUserUnique(String userName) throws UserNameExistException{
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (count>0){
            throw new UserNameExistException();
        }
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException{
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count>0){
            throw new PhoneExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVO vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>()
                .eq("username", loginacct).or().eq("mobile",loginacct));
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (memberEntity!=null){
            if (passwordEncoder.matches(password,memberEntity.getPassword())){
                return memberEntity;
            }else {
                //密码不正确
                return null;
            }
        }else{
            //没有该账户
            return null;
        }

    }

}