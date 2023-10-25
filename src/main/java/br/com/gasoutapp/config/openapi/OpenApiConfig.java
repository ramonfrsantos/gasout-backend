package br.com.gasoutapp.config.openapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {
	@Bean
	public OpenAPI publicApi() {
		return new OpenAPI().info(new Info().title("Serviços GasOut  - API")
			.description("Interface para mapear endpoints dos serviços da aplicação GasOut.")
			.version("0.0.1-SNAPSHOT"));
//			.license(new License().name("").url("")));
	}
}
