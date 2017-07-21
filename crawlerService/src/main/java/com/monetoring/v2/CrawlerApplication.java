package com.monetoring.v2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CrawlerApplication {

	@Value("${crawler.max.page}")
	private String mP;
	@Value("${crawler.max.retries}")
	private String mR;

	public int MAX_PAGE;
	public int MAX_RETRIES;

	public static void main(String[] args) {
		SpringApplication.run(CrawlerApplication.class, args);
	}

	@PostConstruct
	public void init(){
		MAX_PAGE = Integer.parseInt(mP);
		MAX_RETRIES = Integer.parseInt(mR);
	}
}
