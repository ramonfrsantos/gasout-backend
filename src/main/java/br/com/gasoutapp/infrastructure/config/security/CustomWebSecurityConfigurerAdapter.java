package br.com.gasoutapp.infrastructure.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class CustomWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
	private static final String AUTHORITY = "ADMIN";

	@Autowired
	private UserDetailsService userDetailService;

	@Autowired
	private SecurityFilter securityFilter;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		auth.userDetailsService(userDetailService).passwordEncoder(encoder);
	}

	@Bean
	@Override
	public UserDetailsService userDetailsService() {
		return super.userDetailsService();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests()
				.antMatchers("/swagger-ui/**").permitAll()
				.antMatchers("/auth").permitAll().and().authorizeRequests()
				.antMatchers("/auth/**").permitAll().and().authorizeRequests()
				.antMatchers("/users/**").hasAuthority(AUTHORITY)
				.antMatchers("/users").authenticated()
				.antMatchers("/notifications/**").hasAuthority(AUTHORITY)
				.antMatchers("/notifications").authenticated()
				.antMatchers("/rooms/**").hasAuthority(AUTHORITY)
				.antMatchers("/rooms").authenticated()
				.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		http.addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}