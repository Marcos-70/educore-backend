package com.api.educore.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer nanSafeCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            JsonSerializer<Double> nanSafeSerializer = new JsonSerializer<>() {
                @Override
                public void serialize(Double value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                    if (value == null || Double.isNaN(value) || Double.isInfinite(value)) {
                        gen.writeNumber(0);
                    } else {
                        gen.writeNumber(value);
                    }
                }
            };
            module.addSerializer(Double.class, nanSafeSerializer);
            module.addSerializer(double.class, nanSafeSerializer);
            builder.modulesToInstall(module);
        };
    }
}
