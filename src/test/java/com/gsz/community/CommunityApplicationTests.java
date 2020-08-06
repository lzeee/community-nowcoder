package com.gsz.community;

import com.gsz.community.dao.AlphaDao;
import com.gsz.community.dao.AlphaDaoMyBatisImpl;
import com.gsz.community.service.AlphaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CommunityApplicationTests implements ApplicationContextAware {
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		// 传入的参数就是spring容器
		this.applicationContext = applicationContext;
	}

	@Test
	public void testApplicationContext(){
		System.out.println(applicationContext);
		AlphaDao alphaDao = applicationContext.getBean(AlphaDaoMyBatisImpl.class);
		System.out.println(alphaDao.select());
	}

	@Test
	public void testBeanManagement(){
		AlphaService  alphaService = applicationContext.getBean(AlphaService.class);
		System.out.println(alphaService);
		// bean只被实例化、销毁了一次，默认是单例的
	}

}
