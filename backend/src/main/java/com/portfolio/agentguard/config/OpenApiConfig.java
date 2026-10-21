package com.portfolio.agentguard.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI agentGuardOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("AgentGuard Platform API")
                .description("Production-grade AI-Agent Commerce, Payment Trust, Risk, Double-Entry Ledger, and Auditability Platform.")
                .version("v1.0.0")
                .contact(new Contact().name("Tejasgiri Gosavi").email("tejasgirigosavi29@gmail.com"))
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
            .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
            .components(new Components()
                .addSecuritySchemes("BearerAuth", new SecurityScheme()
                    .name("BearerAuth")
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }

    @Bean
    public OperationCustomizer globalHeadersCustomizer() {
        return (operation, handlerMethod) -> {
            operation.addParametersItem(new Parameter()
                .in("header")
                .name("X-Tenant-ID")
                .description("Multi-tenant organization partition identifier")
                .required(false)
                .example("tenant-corp-alpha"));
            operation.addParametersItem(new Parameter()
                .in("header")
                .name("Idempotency-Key")
                .description("UUID to guarantee exactly-once mutation semantics")
                .required(false)
                .example("a4c878b2-5452-474c-8f43-1e9a03b5f920"));
            return operation;
        };
    }
}
