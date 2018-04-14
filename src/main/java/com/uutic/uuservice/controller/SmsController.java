package com.uutic.uuservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class SmsController {
    private String accessKeyId = "LTAI5uuEa7FYtAX7";
    private String accessSecret = "i0bvyuvTnxe0ugbxdrUmbtL9w0pKCz";
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private String host = "http://dysmsapi.aliyuncs.com/";

    private String getRequestUrl(String signName, String templateCode, String templateParam, String phoneNumbers) throws Exception {
        df.setTimeZone(new SimpleTimeZone(0, "GMT"));// 这里一定要设置GMT时区

        Map<String, String> paras = new HashMap<>();
        // 1. 系统参数
        paras.put("AccessKeyId", accessKeyId);
        paras.put("Timestamp", df.format(Calendar.getInstance().getTime()));
        paras.put("Format", "XML");
        paras.put("SignatureMethod", "HMAC-SHA1");
        paras.put("SignatureVersion", "1.0");
        paras.put("SignatureNonce", UUID.randomUUID().toString());
        // 2. 业务API参数
        paras.put("Action", "SendSms");
        paras.put("Version", "2017-05-25");
        paras.put("RegionId", "cn-hangzhou");
        paras.put("PhoneNumbers", phoneNumbers);
        paras.put("SignName", signName);
        paras.put("TemplateCode", templateCode);
        paras.put("TemplateParam", templateParam);
        // 3. 去除签名关键字Key
        if (paras.containsKey("Signature"))
            paras.remove("Signature");
        // 4. 排序
        TreeMap<String, String> sortParas = new TreeMap<>();
        sortParas.putAll(paras);
        // 5. 构造待签名的请求串
        Iterator<String> it = sortParas.keySet().iterator();
        StringBuilder sortQueryStringTmp = new StringBuilder();
        while (it.hasNext()) {
            String key = it.next();
            sortQueryStringTmp.append("&").append(specialUrlEncode(key)).append("=").append(specialUrlEncode(paras.get(key)));
        }
        String sortedQueryString = sortQueryStringTmp.substring(1);// 去除第一个多余的&符号
        // 6. 按POP的签名规则拼接成最终的待签名串
        StringBuilder stringToSign = new StringBuilder();
        stringToSign.append("GET").append("&");
        stringToSign.append(specialUrlEncode("/")).append("&");
        stringToSign.append(specialUrlEncode(sortedQueryString));
        // 7. 签名
        String sign = sign(accessSecret + "&", stringToSign.toString());
        // 8. 增加签名结果到请求参数中，发送请求
        String signature = specialUrlEncode(sign);

        return host + "?Signature=" + signature + sortQueryStringTmp;
    }

    public String specialUrlEncode(String value) throws Exception {
        return URLEncoder.encode(value, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
    }

    public static String sign(String accessSecret, String stringToSign) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA1");
        mac.init(new javax.crypto.spec.SecretKeySpec(accessSecret.getBytes("UTF-8"), "HmacSHA1"));
        byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
        return new sun.misc.BASE64Encoder().encode(signData);
    }

    @RequestMapping("/api/sms/send")
    public String SendSms() throws Exception {
        String requestUrl = getRequestUrl("柚柚网络", "SMS_126290028", "{\"code\":\"888666\"}", "15510826347");
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(new URI(requestUrl), String.class);

        return result;
    }
}
