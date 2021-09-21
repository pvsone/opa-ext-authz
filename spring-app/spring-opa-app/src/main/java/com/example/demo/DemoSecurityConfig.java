package com.example.demo;

import org.openpolicyagent.voter.OPAVoter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
class DemoSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${opa.url}")
    private String opaUrl;

    @Value("${opa.path}")
    private String opaPath;

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/health");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/health").permitAll()
                .anyRequest()
                .authenticated()
                .accessDecisionManager(accessDecisionManager())
                .and()
                .httpBasic();
    }

    @Bean
    public AccessDecisionManager accessDecisionManager() {
        List<AccessDecisionVoter<? extends Object>> decisionVoters = Arrays
                .asList(new OPAVoter(opaUrl + opaPath));
        return new UnanimousBased(decisionVoters);
    }

    @Autowired
    public void configure(AuthenticationManagerBuilder auth)
            throws Exception {
        auth.inMemoryAuthentication()
                .withUser("alice").password("{noop}password").roles("USER").and()
                .withUser("betty").password("{noop}password").roles("USER").and()
                .withUser("bob").password("{noop}password").roles("USER").and()
                .withUser("charlie").password("{noop}password").roles("USER").and()
                .withUser("david").password("{noop}password").roles("USER").and()
                .withUser("zoey").password("{noop}password").roles("USER");
    }
}
