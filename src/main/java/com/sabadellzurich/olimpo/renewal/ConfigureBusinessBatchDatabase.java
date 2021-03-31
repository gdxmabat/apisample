package com.sabadellzurich.olimpo.renewal;

import java.sql.SQLException;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.sabadellzurich.olimpo.renewal.common.lib.constants.RecomhConstants;

import lombok.extern.slf4j.Slf4j;


@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "businessEntityManagerFactory",
        transactionManagerRef = "transactionManagerSecondary",
        basePackages = {RecomhConstants.BASE_PACKAGES_RECOMH}
        
)

@Slf4j
public class ConfigureBusinessBatchDatabase { 
	 
	@Autowired 
	private Environment env;
	@Value("${spring.business.datasource.dialect}")
	private String dialect;
    
    public final static String PERSISTENCE_UNIT_NAME = "secondary";
    
    public final static String MODEL_PACKAGE = RecomhConstants.BASE_MODEL_RECOMH;

    
	@Bean(name="businessDataSource")
	@ConfigurationProperties("spring.business.datasource")
    public DataSource  businessDataSource() throws SQLException {
		
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("spring.business.datasource.driver-class-name"));
        dataSource.setUrl(env.getProperty("spring.business.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.business.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.business.datasource.password"));
 
        return dataSource;
    }
	
	@Bean(name="businessEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean businessEntityManagerFactory( final @Qualifier("businessDataSource") DataSource dataSourceWrite) {
	
		log.debug("init bean businessEntityManagerFactory");
		
		Properties JPA_PROPERTIES = new Properties() {{
			put("hibernate.dialect", env.getProperty("spring.business.datasource.dialect"));
			put("show-sql", env.getProperty("spring.business.datasource.show-sql"));
		}};
	
	        return new LocalContainerEntityManagerFactoryBean() {{
	            setDataSource(dataSourceWrite);
	            setPersistenceProviderClass(HibernatePersistenceProvider.class);
	            setPersistenceUnitName(PERSISTENCE_UNIT_NAME);
	            setPackagesToScan(MODEL_PACKAGE);
	            setJpaProperties(JPA_PROPERTIES);
	        }};
	    }
		
		@Bean(name="transactionManagerSecondary")
	    public PlatformTransactionManager transactionManagerSecondary(@Qualifier("businessEntityManagerFactory")EntityManagerFactory businessEntityManagerFactory) {
	        return new JpaTransactionManager(businessEntityManagerFactory);
	    }
	
		


}