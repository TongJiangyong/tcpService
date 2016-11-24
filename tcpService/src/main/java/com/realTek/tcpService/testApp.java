package com.realTek.tcpService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("testApp")
public class testApp {

	@Autowired
	private App app;
	public void sayHello(){
		System.out.println("just a test");
		app.sayHello();
	}
	
}
