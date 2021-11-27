package com.heiying.common.exception;

/** 错误码五位数：前两位表示哪个服务        后三位表示错误信息
 *  错误码列表：
 *    10：通用
 *      001:参数格式校验
 *      002：短信验证码频率太高
 *    11：商品
 *    12：订单
 *    13：购物车
 *    14：物流
 *    15：用户
 *    21：库存
 */
public enum BizCodeEnume {
    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    VAILD_EXCEPTION(10001,"参数格式校验失败"),
    VAILD_SMS_EXCEPTION(10002,"短信验证码获取频率太高，稍后再试"),
    TOO_MANY_REQUEST(10003,"请求流量过大"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常"),
    USER_EXIST_EXCEPTION(15001,"用户存在"),
    PHONE_EXIST_EXCEPTION(15002,"手机号存在"),
    NO_STOCK_EXCEPTION(21000,"商品库存不足"),
    LOGINACCT_PASSWORD_INVALID_EXCEPTION(15003,"账户或密码错误");
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
