package com.uefa.platform.service.b2bpush.core.configuration;

import com.uefa.platform.autoconfigure.web.security.SsoAuthenticationProvider;
import com.uefa.platform.autoconfigure.web.security.SsoTokenFilter;
import com.uefa.platform.client.auth.AuthClient;
import com.uefa.platform.service.b2bpush.Application.Profiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.uefa.platform.service.b2bpush.core.domain.feed.controller.DashboardFeedController.DASHBOARD_URL_PREFIX;

/**
 * Spring web security Configurations for Dashboard endpoints
 */
@Configuration
@EnableWebSecurity
public class DashboardSecurityConfiguration {

    private final AuthClient authClient;

    @Value("${dashboardCredentials.username}")
    private String dashboardUsername;

    @Value("${dashboardCredentials.encodedPassword}")
    private String dashboardEncodedPassword;

    @Value("${dashboardCredentials.role}")
    private String dashboardRole;

    @Value("${dashboardCredentials.testPassword}")
    private String dashboardTestPassword;

    @Value("#{${scim.group.mapping}}")
    private Map<String, Set<String>> groupMapping;

    @Value("${dataExplorerCredentials.username}")
    private String dataExplorerUsername;

    @Value("${dataExplorerCredentials.encodedPassword}")
    private String dataExplorerEncodedPassword;

    @Value("${dataExplorerCredentials.testPassword}")
    private String dataExplorerTestPassword;

    @Value("${dataExplorerCredentials.role}")
    private String dataExplorerRole;

    @Autowired
    public DashboardSecurityConfiguration(AuthClient authClient) {
        this.authClient = authClient;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.headers().disable()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .antMatchers(DASHBOARD_URL_PREFIX + "/archives").hasRole(dataExplorerRole)
                .antMatchers(DASHBOARD_URL_PREFIX + "/**").hasRole(dashboardRole)
                .anyRequest().permitAll()
                .and()
                .authenticationProvider(new SsoAuthenticationProvider(authClient, groupMapping))
                .addFilterBefore(new SsoTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .httpBasic();

        return httpSecurity.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsManager() {
        String passwordInstant = Profiles.isLocalProfile() ? passwordEncoder().encode(dashboardTestPassword) : dashboardEncodedPassword;
        String passwordDataExplorer = Profiles.isLocalProfile() ? passwordEncoder().encode(dataExplorerTestPassword) : dataExplorerEncodedPassword;

        return new InMemoryUserDetailsManager(List.of(
                createUser(dashboardUsername, passwordInstant, dashboardRole),
                createUser(dataExplorerUsername, passwordDataExplorer, dataExplorerRole)
        ));
    }

    /**
     * Creates users with the given details.
     *
     * @param username the username
     * @param password the password
     * @param roles    the roles
     * @return the user details
     */
    private UserDetails createUser(String username, String password, String... roles) {
        return User.builder()
                .username(username)
                .password(password)
                .roles(roles)
                .build();
    }
}
