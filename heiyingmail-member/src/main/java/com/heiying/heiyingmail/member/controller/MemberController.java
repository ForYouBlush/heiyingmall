package com.heiying.heiyingmail.member.controller;

import com.heiying.common.exception.BizCodeEnume;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.R;
import com.heiying.heiyingmail.member.entity.MemberEntity;
import com.heiying.heiyingmail.member.exception.PhoneExistException;
import com.heiying.heiyingmail.member.exception.UserNameExistException;
import com.heiying.heiyingmail.member.fegin.MemberCouponsService;
import com.heiying.heiyingmail.member.service.MemberService;
import com.heiying.heiyingmail.member.vo.MemberLoginVO;
import com.heiying.heiyingmail.member.vo.MemberRegistVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 会员
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-13 14:43:33
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;
    @Autowired
    MemberCouponsService memberCouponsService;

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVO vo) {
        MemberEntity memberEntity = memberService.login(vo);
        if (memberEntity != null) {
            return R.ok().setData(memberEntity);
        } else {
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getCode(), BizCodeEnume.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }
    }


    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVO userRegistVO) {

        try {
            memberService.regist(userRegistVO);
        } catch (UserNameExistException e) {
            return R.error(BizCodeEnume.USER_EXIST_EXCEPTION.getCode(), BizCodeEnume.USER_EXIST_EXCEPTION.getMsg());
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    @RequestMapping("/coupons")
    public R coupons() {
        R coupons = memberCouponsService.coupons();
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        return R.ok().put("member", memberEntity).put("coupons", coupons.get("coupons"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
