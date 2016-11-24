package com.realTek.tcpService;

import org.springframework.stereotype.Component;

/**
 * Hello world!
 *
 */
//完成了bean的创建工作<bean id="userDao" class=" 包的位置">
@Component("app")
public class App 
{
	public void sayHello(){
		System.out.println("Hello,world");
	}
}
