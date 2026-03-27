package leave_manager.leave_manager.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI leaveSyncOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LeaveSync API")
                        .description("Leave Management System — Hackathon Project by Team: Karthik · Maha · Kavi · Jeyanth")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("LeaveSync Team")
                                .email("team@leavesync.com")))
                // Add JWT bearer token input to Swagger UI
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your JWT access token here (without 'Bearer ' prefix)")));
    }
}
