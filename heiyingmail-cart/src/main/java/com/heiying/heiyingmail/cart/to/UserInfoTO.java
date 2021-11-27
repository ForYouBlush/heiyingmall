package com.heiying.heiyingmail.cart.to;

import lombok.Data;

@Data
public class UserInfoTO {
    private Long userId;
    private String userKey;

    private Boolean tempUser=false;
}
