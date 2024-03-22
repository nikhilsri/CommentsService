package com.IntuitCraft.demo.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(value = "default")
@Configuration
public class CommonConfigurations {

    int fetchSize;
}
