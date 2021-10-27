package com.heiying.heiyingmail.product.vo;

import lombok.Data;

@Data
public class AttrRespVO extends AttrVO{
    private String catelogName;
    private String groupName;
    private Long[]  catelogPath;
}
