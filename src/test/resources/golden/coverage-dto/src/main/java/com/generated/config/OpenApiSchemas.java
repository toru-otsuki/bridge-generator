package com.generated.config;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiSchemas {
  private static final List<Class<?>> TYPES =
      List.of(
          com.generated.dto.PointDto.class,
          com.generated.dto.ItemDto.class,
          com.generated.dto.TagDto.class,
          com.generated.dto.StatusDto.class);

  @Bean
  public OpenApiCustomizer allDtoSchemas() {
    return api -> {
      if (api.getComponents() == null) {
        api.setComponents(new Components());
      }
      ModelConverters converters =
          ModelConverters.getInstance(api.getSpecVersion() == SpecVersion.V31);
      for (Class<?> type : TYPES) {
        converters.readAll(new AnnotatedType(type)).forEach(api.getComponents()::addSchemas);
      }
      Schema<?> any =
          api.getComponents().getSchemas() == null
              ? null
              : api.getComponents().getSchemas().get("CORBA.Any");
      if (any != null && any.getProperties() != null) {
        any.getProperties().put("value", new Schema<>());
      }
    };
  }
}
