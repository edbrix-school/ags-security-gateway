package com.asg.security.gateway.config.utility;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "docid.api")
@Getter
@Setter
public class DocIdApiMappingProperties {
    private Map<String, String> mappings = new HashMap<>();
    private String special;
    private String validDocIdFormatRegexp;
}
