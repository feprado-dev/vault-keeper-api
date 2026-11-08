package br.com.vaultkeeper.web;

import br.com.vaultkeeper.magazine.CoverStorage;
import java.time.Duration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Serve a pasta das capas em /capas/**. O nome do arquivo é um UUID, então nunca muda: pode ficar em cache.
@Configuration
public class StorageWebConfig implements WebMvcConfigurer {

    private final CoverStorage storage;

    public StorageWebConfig(CoverStorage storage) {
        this.storage = storage;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(CoverStorage.URL_PREFIX + "**")
                .addResourceLocations(storage.directory().toUri().toString())
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic());
    }
}
