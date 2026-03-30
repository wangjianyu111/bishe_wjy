package com.gdplatform.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class VerificationCodeUtil {
    
    /**
     * 生成指定长度的数字验证码
     * @param length 验证码长度
     * @return 验证码字符串
     */
    public static String generateVerificationCode(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}