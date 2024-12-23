package com.yoger.productserviceorganization.product.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

@Configuration
@Profile("gcp")
public class GCSProductConfig {
    private final GcpProductProperties gcpProductProperties;

    public GCSProductConfig(GcpProductProperties gcpProductProperties) {
        this.gcpProductProperties = gcpProductProperties;
    }

    @Bean
    public Storage storage() throws IOException {
        GoogleCredentials credentials;
        if (gcpProductProperties.credentialsPath() != null && !gcpProductProperties.credentialsPath().isEmpty()) {
            ClassPathResource resource = new ClassPathResource(gcpProductProperties.credentialsPath());
            InputStream credentialsStream = resource.getInputStream();
            credentials = GoogleCredentials.fromStream(credentialsStream);
        } else {
            credentials = GoogleCredentials.getApplicationDefault();
        }

        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .setProjectId(gcpProductProperties.projectId())
                .build()
                .getService();
    }
}
