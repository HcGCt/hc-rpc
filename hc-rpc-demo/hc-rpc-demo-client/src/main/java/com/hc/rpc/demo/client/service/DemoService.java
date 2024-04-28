package com.hc.rpc.demo.client.service;

import com.hc.rpc.demo.client.dto.Student;

/**
 * @Author hc
 */
public interface DemoService {

    String sayHello(String name);

    Student getStudent(String name);
}
