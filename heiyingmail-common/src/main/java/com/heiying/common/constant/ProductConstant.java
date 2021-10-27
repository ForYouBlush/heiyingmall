package com.heiying.common.constant;

public class ProductConstant {
    public enum AttrEnum{
        ATTR_TYPE_BASE(1,"基本属性"),ATTR_TYPE_SELL(0,"销售属性");
        private int code;
        private String msg;
         AttrEnum(int code,String msg){
             this.code=code;
             this.msg=msg;
        }

        public String getMsg() {
            return msg;
        }

        public int getCode() {
            return code;
        }
    }
}
