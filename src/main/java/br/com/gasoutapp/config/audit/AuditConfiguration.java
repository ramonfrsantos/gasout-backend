package br.com.gasoutapp.config.audit;

import javax.persistence.EntityManagerFactory;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditConfiguration {

	private final EntityManagerFactory entityManagerFactory;

	AuditConfiguration(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	@Bean
	AuditReader auditReader() {
		return AuditReaderFactory.get(entityManagerFactory.createEntityManager());
	}
}
