package ru.eventhub.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openApi(): OpenAPI {
        val bearerSchemeName = "bearerAuth"

        return OpenAPI()
            .info(
                Info()
                    .title("EventHub API")
                    .description("API платформы университетских мероприятий EventHub")
                    .version("v1"),
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        bearerSchemeName,
                        SecurityScheme()
                            .name(bearerSchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT"),
                    ),
            )
            .addSecurityItem(SecurityRequirement().addList(bearerSchemeName))
    }
}
