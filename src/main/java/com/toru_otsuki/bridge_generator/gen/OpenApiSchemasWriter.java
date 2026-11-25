package com.toru_otsuki.bridge_generator.gen;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

/// OpenAPIのスキーマを整える`config/OpenApiSchemas`を生成する
///
/// springdocはControllerからたどれる型しか載せないので、Anyの中にしか入らない型のために全DTOを載せる。Anyの`value`は、OpenAPI
/// 3.0では`Object`が`type: object`になり数値や文字列が入らないので、型を指定しないスキーマにする。
final class OpenApiSchemasWriter {
  private OpenApiSchemasWriter() {}

  /// `OpenApiSchemas`を出力する
  ///
  /// `ModelConverters`はOpenAPIの版ごとに型の情報の持ち方が違うので、出力する版のものを使う。
  /// DTOを載せると`CORBA.Any`も載せ直されるので、`value`はその後に置き換える。
  ///
  /// @param ctx 生成先
  static void write(GenContext ctx) throws IOException {
    String classes =
        ctx.settings().definitions().stream()
            .map(d -> "          " + ctx.types().dtoClass(d.type()).canonicalName() + ".class")
            .collect(Collectors.joining(",\n"));

    Files.writeString(
        ctx.configDir().resolve("OpenApiSchemas.java"),
        """
        package %s.config;

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
        %s);

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
                      : api.getComponents().getSchemas().get("%s");
              if (any != null && any.getProperties() != null) {
                any.getProperties().put("value", new Schema<>());
              }
            };
          }
        }
        """
            .formatted(ctx.basePackage(), classes, AnyTypeWriter.ANY_SCHEMA));
  }
}
