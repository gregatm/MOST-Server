package de.muenchen.mostserver.security

import de.muenchen.mostserver.security.UmaPermissionAuthorizationManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class NoSecurityConfig {
    @Bean
    fun defaultSecurityFilterChain(http: HttpSecurity, authz: UmaPermissionAuthorizationManager) : SecurityFilterChain {
        return http
            //.authorizeHttpRequests { auth -> auth.anyRequest().access(authz) }
            //.authorizeHttpRequests({ auth -> auth.requestMatchers("/**").permitAll() })
            .authorizeHttpRequests({ auth -> auth.anyRequest().authenticated() })
            .oauth2Login({ c ->
                try {
                    c.init(http);
                } catch (e: Exception) {
                throw RuntimeException(e);
                }
            })
            .csrf { it.disable() }
            .build()
    }
}