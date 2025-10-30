package com.metrolinx.bookexchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class BookexchangeApplication {


	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(BookexchangeApplication.class, args);
		
	}
}
