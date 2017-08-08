package com.melot.talkee.context;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.xml.DOMConfigurator;

import com.melot.common.melot_jedis.RedisDataSourceFactory;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.talkee.utils.ConfigHelper;

public class ConfigContextListener implements ServletContextListener {
	
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		RedisDataSourceFactory.getGlobalInstance().destroy();
		// 销毁Spring容器
		MelotBeanFactory.close();
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
	    // 初始化Spring容器
		MelotBeanFactory.init("classpath*:/conf/spring-bean-container*.xml");
	
		String configLocation = event.getServletContext().getInitParameter("configLocation");
        if (configLocation != null) {
            ConfigHelper.initConfig(event.getServletContext().getRealPath("/") + configLocation);
        }
		
		String log4jConfigLocation = event.getServletContext().getInitParameter("log4jConfigLocation");
		if (log4jConfigLocation != null) {
			DOMConfigurator.configureAndWatch(event.getServletContext().getRealPath("/") + log4jConfigLocation);
		}
		
	}
}
