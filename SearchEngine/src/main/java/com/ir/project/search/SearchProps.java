package com.ir.project.search;

import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SearchProps {

	public static Properties properties;
	private Logger logger = Logger.getLogger(SearchProps.class);

	@PostConstruct
	public void loadProperties() {
		properties = new Properties();
		properties.setProperty("rocchio.alpha", "1");
		properties.setProperty("rocchio.beta", "0.75");
		properties.setProperty("QE.doc.num", "10");
		properties.setProperty("QE.term.num", "25");
		logger.info("Finished loading the properties");
	}
}
