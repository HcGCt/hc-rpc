package com.hc.rpc.demo.test.serializer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试序列化类
 *
 * @Author hc
 */
public class MessageInfo implements Serializable {
    private String username;
    private String password;
    private int age;
    private Map<String, Object> params;

    public static MessageInfo buildMessage() {
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setUsername("zhangsan");
        messageInfo.setPassword("123456789");
        messageInfo.setAge(27);
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            map.put(String.valueOf(i), "a");
        }
        messageInfo.setParams(map);
        return messageInfo;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
