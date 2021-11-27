package com.heiying.heiyingmail.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.heiying.common.utils.R;
import com.heiying.heiyingmail.ware.feign.MemberFeignService;
import com.heiying.heiyingmail.ware.vo.FareVO;
import com.heiying.heiyingmail.ware.vo.MemberAddressVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.Query;

import com.heiying.heiyingmail.ware.dao.WareInfoDao;
import com.heiying.heiyingmail.ware.entity.WareInfoEntity;
import com.heiying.heiyingmail.ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key= (String) params.get("key");
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(key)){
            wrapper.eq("id",key).or().like("name",key)
                    .or().like("address",key)
                    .or().like("areacode",key);
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
               wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public FareVO getFare(Long addrId) {
        R r = memberFeignService.addrInfo(addrId);
        FareVO fareVO=new FareVO();
        if (r.getCode()==0){
            MemberAddressVO data = r.getData("memberReceiveAddress",new TypeReference<MemberAddressVO>() {
            });
            fareVO.setAddress(data);
            if (data!=null){
                String phone = data.getPhone();
                String substring = phone.substring(phone.length() - 1, phone.length());
//                Random random=new Random();
//                int i = random.nextInt(20) ;
                fareVO.setFare(new BigDecimal(substring));
                return fareVO;
            }
        }
        return null;
    }

}