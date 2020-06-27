package com.aero51.springbootepdapi;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DatabaseConfig {

	@Value("postgres://nwcaszdefnhttg:c849cddbbfc55b1e81efc0454e8a4464ef1675a98ed07301022c05d66983b610@ec2-52-200-48-116.compute-1.amazonaws.com:5432/d13rkm6j40tt4h")
	private String dbUrl;

	@Bean
	public DataSource dataSource() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(dbUrl);
		return new HikariDataSource(config);
	}
}