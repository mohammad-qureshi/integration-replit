package com.gitintegration.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration for OpenAPI documentation.
 */
@Configuration
public class OpenApiConfig {

    @Value("${springdoc.server.url:http://localhost:8000}")
    private String serverUrl;

    /**
     * Configures the OpenAPI documentation.
     * 
     * @return OpenAPI bean
     */
    @Bean
    public OpenAPI gitIntegrationOpenAPI() {
        Server server = new Server();
        server.setUrl(serverUrl);
        server.setDescription("Git Integration API Server");

        Contact contact = new Contact();
        contact.setName("Git Integration API Support");
        contact.setEmail("support@gitintegration.example.com");
        contact.setUrl("https://www.example.com/support");

        License license = new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0");

        Info info = new Info()
                .title("Git Integration API")
                .version("1.0.0")
                .description("REST API for GitHub and GitLab integration to fetch branches, commits, and merge/pull requests")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}
