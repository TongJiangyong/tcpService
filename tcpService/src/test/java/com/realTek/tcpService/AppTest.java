package com.realTek.tcpService;

import org.hamcrest.Factory;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
	//这里一定要先通过工厂获取spring的对象
	private BeanFactory factory = new ClassPathXmlApplicationContext("springBeans.xml");
	//注意这里的app2并不能直接用
	//应为在spring中，使用componet后，所有的均只是注册到bean中，并没有直接实例化，所以不能这么做
	@Autowired
	App app2;
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
	@Test
    public void AppTest(){
		testApp app = (testApp)factory.getBean("testApp");
		app.sayHello();
		//new AppTest().app2.sayHello();		
    }

}
