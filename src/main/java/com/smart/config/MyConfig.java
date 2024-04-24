package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MyConfig 
{
	@Bean
    public UserDetailsService getUserDetailsService()
    {
    	return new UserDetailServiceImpl();
    }
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder();
	}
	
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider()
	{
		DaoAuthenticationProvider daoAuthenticationProvider=new DaoAuthenticationProvider() ;
		daoAuthenticationProvider.setUserDetailsService(this.getUserDetailsService());
		daoAuthenticationProvider.setPasswordEncoder(this.passwordEncoder());
		
		return daoAuthenticationProvider;
	}
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception
	{
		
		http.csrf().disable().authorizeHttpRequests()
		.requestMatchers("/admin/**").hasRole("ADMIN")
		.requestMatchers("/user/**").hasRole("USER")
		.requestMatchers("/**").permitAll()
		.and().formLogin().loginPage("/signin").loginProcessingUrl("/userLogin")
		.defaultSuccessUrl("/user/index")
				/* .failureUrl("/login_fail") */
		.and().logout()
		.permitAll();
		
		/*
		 * Server methods that we can use to configure the behaviour of the login form
		 * ===========================================================================
		 * 1.loginPage() - the custom login page
		 * 2.loginProcessingUrl() - the url to submit the username and password to
		 * 3.defaultSuccessUrl() - the landing page after a successful login
		 * 4.failureUrl() - the landing page after an unsuccessful login
		 * 
		 */
		return http.build();
	}
	
	
	
}
