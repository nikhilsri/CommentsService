package com.IntuitCraft.demo.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
public class SwaggerConfiguration {

    @Bean
    @Primary
    public OpenAPI OpenAPIConfiguration(@Value("${springdoc.version}") String docVersion,
                                        @Value("${server.port}") String serverPort) {
        return new OpenAPI()
                .info(new Info().title("Comments Service").version(docVersion)
                        .description("Collection of all Comments Service Requests"))


                .addServersItem(
                        new Server().url("{protocol}://{host}:{port}/{path}")
                                .description("Comment Service Server's URL")
                                .variables(new ServerVariables()
                                        .addServerVariable("protocol",
                                                new ServerVariable()._default("http").addEnumItem("http")
                                                        .addEnumItem("https"))
                                        .addServerVariable("host", new ServerVariable()._default("localhost"))
                                        .addServerVariable("port", new ServerVariable()._default(serverPort))
                                        .addServerVariable("path", new ServerVariable()._default(""))));
    }
    @Bean
    public GroupedOpenApi commentsServiceApi() {
        return GroupedOpenApi.builder().group("comments-service").pathsToMatch("/**").build();
    }

}
