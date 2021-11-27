package com.heiying.heiyingmail.thirdparty;

import com.aliyun.oss.OSSClient;
import com.heiying.heiyingmail.thirdparty.component.SmsComponent;
//import com.heiying.heiyingmail.thirdparty.util.HttpUtils;
import com.heiying.heiyingmail.thirdparty.uitl.HttpUtils;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class HeiyingmailThirdPartyApplicationTests {

    @Resource
    OSSClient ossClient;
    @Resource
    SmsComponent smsComponent;

    @Test
    void testTime(){
        String format = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        System.out.println(format);
    }

//
//        @Test
//    void testText(){
//        smsComponent.sendCode("17679235221","4545");
//    }


//    @Test
//    void sendSms(){
//        String host = "https://dfsns.market.alicloudapi.com";
//        String path = "/data/send_sms";
//        String method = "POST";
//        String appcode = "41f2ecebef8a476994e86b5fbcc4b490";
//        Map<String, String> headers = new HashMap<String, String>();
//        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
//        headers.put("Authorization", "APPCODE " + appcode);
//        //根据API的要求，定义相对应的Content-Type
//        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//        Map<String, String> querys = new HashMap<String, String>();
//        Map<String, String> bodys = new HashMap<String, String>();
//        bodys.put("content", "code:13134,expire_at:60");
//        bodys.put("phone_number", "17679235221");
//        bodys.put("template_id", "TPL_0001");
//
//
//        try {
//            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
//            System.out.println(response.toString());
//            //获取response的body
//            //System.out.println(EntityUtils.toString(response.getEntity()));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    @Test
    void contextLoads() throws FileNotFoundException {
        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
//        String endpoint = "oss-cn-beijing.aliyuncs.com";
//        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
//        String accessKeyId = "LTAI5tHBWuBNrcurKyAmcteX";
//        String accessKeySecret = "pg64qNoNcYkojvQGdXgUr4g67ymzuu";

// 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//
//
//// 填写本地文件的完整路径。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
//        InputStream inputStream = new FileInputStream("C:\\Users\\27494\\Pictures\\Saved Pictures\\maomao.jpg");
//// 依次填写Bucket名称（例如examplebucket）和Object完整路径（例如exampledir/exampleobject.txt）。Object完整路径中不能包含Bucket名称。
//        ossClient.putObject("heiyingmail", "maomao3.jpg", inputStream);
//
//// 关闭OSSClient。
//        ossClient.shutdown();
//        System.out.println("上传完成...");
    }
}
