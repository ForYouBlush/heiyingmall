package com.heiying.heiyingmail.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.heiying.heiyingmail.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2021000118655745";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC7+0Tb7S8AfluoXU4njGPm/B3HBV4Nh7EFGIjSR7WxTZ5g4bGhYi768MmqwDUcK3XZyT9RcLDrMYwEtP6/DLEAphotCCNZaWhOI52P55vkLnam3jkGvexWOxOXR7P88jP3at6FCRKpQOBWguS3JzVnCLIaklLy46wqyuz3ANcvf7x3CI0SvkpOx6ppgglPLCPK1KBJM/8aEP8JK50DbXhvOCBzV+/jzXYjl4fq5DRdlL485y2tK5xULaq56HErnfIqfdD63MgD+SfxT8hYqig0N8YWv1J5pAR+RgyEZhFXNzyYDb31MpiN0IcGVUhJCtUI9nLogL7ZKi1Q1Ymo/BE5AgMBAAECggEAS9L/pIJYeid9yzzPoI41Yd+CF1wn4uc/1SFfVwqL1FfNsaB36PPUpW3oupJLGfDB2bYebzg4dLYIl4F2XBO/ddaKKm3k7FTjONmO3ZP7wp+nRonOkNSc9u9faJ8Ij9gWdA9McHcHavP7SYoEvYf4N3Y/eQ5anRSOxrgRu1RAeCyp1/r/GLWkBo65G6kmFgSI+kUZkO9enuNBrfqgredOsUxcSN6z6sZg1CbeG1GAOZcC8jMEuVfFKrXr9aHGLGc70wzTX++LEFZy6+F/kvKu38uEZQWBsjC5Uwtf98v8s8K/WGwXvvv6fwlX+ptqlbc03P5auLEf99bnyiP4Unmb3QKBgQDnxNyVBc8fJHWHzmOBu2JzDeJohojmcyw45VDTzg5ZlLuoPMClRu98HhIx7XfZ8gn2J9SBOBb0GgoAITtYL568IOl3p1DNDh7DDYUWbgC4/lYvFLfqVvNEVVnFk/DNxgFsZAzo4jcitIuCTpVh5/XlXe7IzsGXGF2Q6/nY6zLQzwKBgQDPonc0R2tU2AmG3K2nYNzYe89Yyut7D0BDn+82jnmym6osTA+SB1JnMgDEb4aGLoobsWxvxejK8yJpIFWEJbFeKmv4v7Mw/UGpxRRPMTpFocI2IeGI7c0jtwfi7fq3UvdIJi4EUAYloA6rwoO4iv8pIbxkeKIgJEkaoE07e0kvdwKBgQCcNX/iZO7rc3xPkxhYGP7KwwHlJz/f6qJUmqI2jzv2vpsYUhIZk0zYH8BtrDrKRyrVoJTGnxJBZcd1vPAtiLL440nX3opEGo87McKQkoWkKa/WvaUTqZT+anQ+CW8Uka7l6i0VTVqR5pPcuLBQM7H07LGC4G90MfTmN1XHEQbH2wKBgQDHhiaojSzdxhA5kVwqKJFwUmFowO5SPdCSa2ES+wyFDsMTeC/KMtEDq3BDU6xgyt4aSH+cVQEj7UoW3WRv7Uyo4XkJ+yT1oZh0zJZ8lyCZAx0+qYYx79oeROcGMYuomMhZg57atA+EHm+aq/up3Z/e5bVEkwuweU3qkTpGO22TdwKBgDiwKq5PFnjkHQ6KR1n9bECvGq/F6PldCFrx8UfUCCbk9yqCC5MH91rPdmGvM4fErQHzufMSTRoBR6s5eTcM0E1xEFKBEZMVM9ml2OSOmMACtGcSyu4bjD+IIC/irW5RvIcew+AJGzDlEyzo/g3x/gdNdrul6xOuYFxdmBAXTJJq";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjoPtu5obiTbThyiZVNW6hV85EZGfjmcE8NexbPn9OYqAVWN3oZZowNPb27nRt8x07h0PBR0USfO/SC6QjTwAw25BLx0hzBNYpWyAP4xDMvm9UqApUqe81O+iVog+3J1cwbTK+oudmClVJ2C0/KXa+2Dn3nzRQ5tzzbGRyrZUCvi/lrHoDoERzmfLXfkPn4IchN9R0oWfMM2kY6w0/mbmvqQudE9UF+CinynpeX//csFnv0/ehF6xgYoKMj0ulPf3WF0/ECyH2LPRlPUsSnW8a6taHrgJ5tlbbK/HYhUOl8mdVM1PXKUNeUVkizmmepxS5Apxz0FZaSkWJucT8vTa0QIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url="http://tqmw5v.natappfree.cc/payed/notify";

    // 页面跳转同步通知页面
    // 路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url="http://member.heiyingmail.com/orderList.html";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    //超时时间
    private String timeout="1m";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+timeout+"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
