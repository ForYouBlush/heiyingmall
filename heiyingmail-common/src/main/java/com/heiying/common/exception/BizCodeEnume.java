package com.heiying.common.exception;

/** 错误码五位数：前两位表示哪个服务        后三位表示错误信息
 *  错误码列表：
 *    10：通用
 *    11：商品
 *    12：订单
 *    13：购物车
 *    14：物流
 */
public enum BizCodeEnume {
    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    VAILD_EXCEPTION(10001,"参数格式校验失败");
    private int code;
    private String msg;
    BizCodeEnume(int code,String msg){
        this.code=code;
        this.msg=msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
