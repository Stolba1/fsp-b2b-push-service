package com.uefa.platform.service.b2bpush.core.configuration;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfiguration {

    public static final String FSP_B2B_PUSH_SERVICE_TAG_STRING = "FSP B2B Push Service Overview";
    private static final Tag FSP_B2B_PUSH_SERVICE_TAG = new Tag().name(FSP_B2B_PUSH_SERVICE_TAG_STRING)
            .description("All FSP B2B Push Service related resources");

    @Bean
    public GroupedOpenApi v1GroupedOpenApi() {
        return getGroupedOpenApi("v1", "Handles the process of pushing fsp data updates to clients");
    }

    private GroupedOpenApi getGroupedOpenApi(String version, String description) {
        return GroupedOpenApi.builder()
                .group("fsp-b2b-push-service-" + version)
                .pathsToMatch("/**/" + version + "/**")
                .packagesToScan("com.uefa")
                .addOpenApiCustomiser(apiInfo(version, description))
                .build();
    }

    private OpenApiCustomiser apiInfo(String version, String description) {
        return openApi -> openApi
                .info(new Info()
                        .title("FSP B2B Push Service")
                        .description(description)
                        .version(version))
                .setTags(List.of(FSP_B2B_PUSH_SERVICE_TAG));
    }
}
