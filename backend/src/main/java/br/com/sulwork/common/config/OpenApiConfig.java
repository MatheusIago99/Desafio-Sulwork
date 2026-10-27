package br.com.sulwork.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sulworkOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Sulwork Eventos API")
                .version("1.0.0")
                .description("Organização de eventos de alimentação: eventos, participantes, "
                        + "categorias, itens, contribuições, cobertura e alérgenos."));
    }
}
