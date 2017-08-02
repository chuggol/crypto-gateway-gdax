package org.chuggol.crypto.gateway.gdax;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class GdaxGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GdaxGatewayApplication.class, args);
	}
}
