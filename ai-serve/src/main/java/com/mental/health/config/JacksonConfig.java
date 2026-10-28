package com.mental.health.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Jackson 全局时间格式：LocalDateTime ↔ yyyy-MM-dd HH:mm:ss（GMT+8）
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder
                .serializers(new LocalDateTimeSerializer(DATE_TIME))
                .deserializers(new LocalDateTimeDeserializer(DATE_TIME))
                .timeZone(TimeZone.getTimeZone("GMT+8"));
    }
}
