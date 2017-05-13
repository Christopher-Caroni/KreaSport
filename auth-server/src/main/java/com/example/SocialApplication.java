/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example;

import com.example.domain.Account;
import com.example.domain.Checkpoint;
import com.example.domain.Race;
import com.example.repository.AccountRepository;
import com.example.repository.RaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.filter.CompositeFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan("com.example")
@EnableMongoRepositories
@EnableOAuth2Client
@EnableAuthorizationServer
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SocialApplication extends WebSecurityConfigurerAdapter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    OAuth2ClientContext oauth2ClientContext;

    /**
     * /user is secured by cookies from social login
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.antMatcher("/**")
                .authorizeRequests()
                    .antMatchers("/", "/login**", "/webjars/**").permitAll()
                    .anyRequest().authenticated()
                .and().httpBasic()
                .and().exceptionHandling()
                    .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/"))
                .and().logout()
                .logoutSuccessUrl("/").permitAll().and().csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).and()
                .addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
        // @formatter:on
    }

    /**
     * /me is secured by the resource server i.e. from oauth token
     *
     */
    @Configuration
    @EnableResourceServer
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
        @Override
        public void configure(HttpSecurity http) throws Exception {
            // @formatter:off
            http.antMatcher("/me").authorizeRequests().anyRequest().authenticated();
            // @formatter:on
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(SocialApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }

    @Bean
    @ConfigurationProperties("github")
    public ClientResources github() {
        return new ClientResources();
    }

    @Bean
    @ConfigurationProperties("facebook")
    public ClientResources facebook() {
        return new ClientResources();
    }

    private Filter ssoFilter() {
        CompositeFilter filter = new CompositeFilter();
        List<Filter> filters = new ArrayList<>();
        filters.add(ssoFilter(facebook(), "/login/facebook"));
        filters.add(ssoFilter(github(), "/login/github"));
        filter.setFilters(filters);
        return filter;
    }

    private Filter ssoFilter(ClientResources client, String path) {
        OAuth2ClientAuthenticationProcessingFilter filter = new OAuth2ClientAuthenticationProcessingFilter(
                path);
        OAuth2RestTemplate template = new OAuth2RestTemplate(client.getClient(), oauth2ClientContext);
        filter.setRestTemplate(template);
        UserInfoTokenServices tokenServices = new UserInfoTokenServices(
                client.getResource().getUserInfoUri(), client.getClient().getClientId());
        tokenServices.setRestTemplate(template);
        filter.setTokenServices(tokenServices);
        return filter;
    }


    // CORS
    @Bean
    FilterRegistrationBean corsFilter(
            @Value("${tagit.origin:http://localhost:9000}") String origin) {
        return new FilterRegistrationBean(new Filter() {
            public void doFilter(ServletRequest req, ServletResponse res,
                                 FilterChain chain) throws IOException, ServletException {
                HttpServletRequest request = (HttpServletRequest) req;
                HttpServletResponse response = (HttpServletResponse) res;
                String method = request.getMethod();
                // this origin value could just as easily have come from a database
                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Access-Control-Allow-Methods",
                        "POST,GET,OPTIONS,DELETE");
                response.setHeader("Access-Control-Max-Age", Long.toString(60 * 60));
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader(
                        "Access-Control-Allow-Headers",
                        "Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization");
                if ("OPTIONS".equals(method)) {
                    response.setStatus(HttpStatus.OK.value());
                }
                else {
                    chain.doFilter(req, res);
                }
            }

            public void init(FilterConfig filterConfig) {
            }

            public void destroy() {
            }
        });
    }


    @Bean
    CommandLineRunner init(AccountRepository accountRepository,
                           RaceRepository raceRepository) {

        return new CommandLineRunner() {
            @Override
            public void run(String... strings) throws Exception {
                accountRepository.save(new Account("chris", "password"));


                List<Race> deleted = new ArrayList<>();
                deleted.addAll(raceRepository.deleteByTitle("Dummy Race Title 0"));
                deleted.addAll(raceRepository.deleteByTitle("Dummy Race Title 1"));
                deleted.addAll(raceRepository.deleteByTitle("GCU Race"));
                deleted.addAll(raceRepository.deleteByTitle("IUT Race"));

                for (Race race : deleted) {
                    logger.debug("Deleted " + race.getTitle() + ", " + race.getId());
                }

                ArrayList<Race> dummyRaces = new ArrayList<>();
                List<Checkpoint> dummyCheckpointList = new ArrayList<>();

                // FIRST RACE IUT
                dummyCheckpointList.add(new Checkpoint("Dummy title 1", "Dummy Description 1", "Dummy question 1", 50.613664, 3.136939,
                        Arrays.asList("First Question", "Second Question")));
                dummyCheckpointList.add(new Checkpoint("Dummy title 2", "Dummy Description 2", "Dummy question 2", 50.613278, 3.137973,
                        Arrays.asList("First Question", "Second Question")));
                raceRepository.save(new Race("IUT Race", "Dummy Race Description 0", 50.613664, 3.136939, dummyCheckpointList));

                // SECOND RACE GLASGOW
                dummyCheckpointList = new ArrayList<>();
                dummyCheckpointList.add(new Checkpoint("Dummy title 1", "Dummy Description 1", "Dummy question 1", 55.866576, -4.251175,
                        Arrays.asList("First Question", "Second Question")));
                dummyCheckpointList.add(new Checkpoint("Dummy title 2", "Dummy Description 2", "Dummy question 2", 55.866035, -4.251379,
                        Arrays.asList("First Question", "Second Question")));
                raceRepository.save(new Race("GCU Race", "Dummy Race Description 1", 55.866576, -4.251175, dummyCheckpointList));
            }
        };
    }

}

class ClientResources {

    @NestedConfigurationProperty
    private AuthorizationCodeResourceDetails client = new AuthorizationCodeResourceDetails();

    @NestedConfigurationProperty
    private ResourceServerProperties resource = new ResourceServerProperties();

    public AuthorizationCodeResourceDetails getClient() {
        return client;
    }

    public ResourceServerProperties getResource() {
        return resource;
    }
}
