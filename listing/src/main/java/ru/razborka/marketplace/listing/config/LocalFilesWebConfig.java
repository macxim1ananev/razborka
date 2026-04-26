package ru.razborka.marketplace.listing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class LocalFilesWebConfig implements WebMvcConfigurer {

    private final String rootPath;

    public LocalFilesWebConfig(@Value("${app.storage.local.root:./data/uploads}") String rootPath) {
        this.rootPath = Path.of(rootPath).toAbsolutePath().normalize().toUri().toString();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/**")
                .addResourceLocations(rootPath.endsWith("/") ? rootPath : rootPath + "/");
    }
}
