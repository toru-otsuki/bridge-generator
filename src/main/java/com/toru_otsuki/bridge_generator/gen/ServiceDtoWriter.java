package com.toru_otsuki.bridge_generator.gen;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import com.toru_otsuki.bridge_generator.model.Operation;
import com.toru_otsuki.bridge_generator.model.Param;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;

/// 操作ごとのリクエスト・レスポンスのクラスと、application.ymlを出力する
final class ServiceDtoWriter {
  /// Jacksonの`@JsonProperty`
  private static final ClassName JSON_PROPERTY =
      ClassName.get("com.fasterxml.jackson.annotation", "JsonProperty");

  /// JSON-RPCの項目の型を入れ子で持つクラスの名前
  static final String JSON_RPC_TYPES = "JSONRPC";

  private ServiceDtoWriter() {}

  /// `JSONRPC.Version`と、リクエストの項目を寛容に読むデシリアライザを出力する
  ///
  /// @param ctx 生成先
  static void writeJsonRpcTypes(GenContext ctx) throws IOException {
    Files.writeString(
        ctx.dtoDir().resolve(JSON_RPC_TYPES + ".java"),
        """
        package %s.dto;

        import com.fasterxml.jackson.annotation.JsonProperty;
        import io.swagger.v3.oas.annotations.media.Schema;
        import tools.jackson.core.JsonParser;
        import tools.jackson.databind.DeserializationContext;
        import tools.jackson.databind.JsonNode;
        import tools.jackson.databind.ValueDeserializer;

        public final class JSONRPC {
          private JSONRPC() {}

          @Schema(name = "JSONRPC.Version", enumAsRef = true)
          public enum Version {
            @JsonProperty("2.0")
            V2_0
          }

          public static final class LenientVersion extends ValueDeserializer<Version> {
            @Override
            public Version deserialize(JsonParser p, DeserializationContext ctxt) {
              JsonNode node = ctxt.readTree(p);
              return node.isString() && node.stringValue().equals("2.0") ? Version.V2_0 : null;
            }
          }

          public static final class LenientString extends ValueDeserializer<String> {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) {
              JsonNode node = ctxt.readTree(p);
              return node.isString() ? node.stringValue() : null;
            }
          }
        }
        """
            .formatted(ctx.basePackage()));
  }

  /// 想定しない値をnullとして読むフィールドを作る
  ///
  /// 呼び出しに使わない`jsonrpc`・`method`は、おかしな値でも400にせずnullとして読む。
  /// Jacksonの`READ_UNKNOWN_ENUM_VALUES_AS_NULL`では、整数が選択肢の番号として読まれ、配列などは400になる。
  ///
  /// @param type フィールドの型
  /// @param name フィールド名
  /// @param deserializer 使うデシリアライザ
  /// @return フィールド
  private static FieldSpec.Builder lenientField(
      TypeName type, String name, ClassName deserializer) {
    return FieldSpec.builder(type, name, Modifier.PUBLIC)
        .addAnnotation(
            AnnotationSpec.builder(
                    ClassName.get("tools.jackson.databind.annotation", "JsonDeserialize"))
                .addMember("using", "$T.class", deserializer)
                .build());
  }

  /// {@return `JSONRPC`の入れ子のクラス}
  ///
  /// @param ctx 生成先
  /// @param nested 入れ子のクラス名
  private static ClassName jsonRpcClass(GenContext ctx, String nested) {
    return ctx.types().dtoClass(JSON_RPC_TYPES).nestedClass(nested);
  }

  /// `params`（in/inout引数）とリクエスト全体のクラスを出力する
  ///
  /// @param op 操作
  /// @param ctx 生成先
  static void generateRequestDto(Operation op, GenContext ctx) throws IOException {
    TypeMapping types = ctx.types();
    Naming naming = ctx.settings().naming();
    String paramsName = naming.rpcParamsName(op);

    TypeSpec.Builder requestDto = TypeSpec.classBuilder(paramsName).addModifiers(Modifier.PUBLIC);
    for (Param param : op.params()) {
      if (!param.isInput()) continue;
      requestDto.addField(publicField(types.dtoType(param.type()), param.name()));
    }
    write(requestDto.build(), ctx);

    TypeSpec rpcRequest =
        TypeSpec.classBuilder(naming.rpcRequestName(op))
            .addModifiers(Modifier.PUBLIC)
            .addField(
                lenientField(
                        jsonRpcClass(ctx, "Version"),
                        "jsonrpc",
                        jsonRpcClass(ctx, "LenientVersion"))
                    .build())
            .addField(
                lenientField(
                        ClassName.get(String.class), "method", jsonRpcClass(ctx, "LenientString"))
                    .addAnnotation(
                        AnnotationSpec.builder(
                                ClassName.get("io.swagger.v3.oas.annotations.media", "Schema"))
                            .addMember("allowableValues", "$S", op.name())
                            .build())
                    .build())
            .addField(publicField(TypeName.INT, "id"))
            .addField(publicField(types.dtoClass(paramsName), "params"))
            .build();
    write(rpcRequest, ctx);
  }

  /// `result`とレスポンス全体のクラスを出力する
  ///
  /// システム例外のときは戻り値とout引数をnullにするので、プリミティブもラッパー型で持つ。
  ///
  /// @param op 操作
  /// @param ctx 生成先
  static void generateRpcResponse(Operation op, GenContext ctx) throws IOException {
    TypeMapping types = ctx.types();
    Naming naming = ctx.settings().naming();
    String resultName = naming.rpcResultName(op);

    AnnotationSpec jsonReturn =
        AnnotationSpec.builder(JSON_PROPERTY).addMember("value", "$S", "return").build();
    TypeName returnType =
        op.returnType().map(t -> types.dtoType(t).box()).orElse(ClassName.get(Object.class));
    TypeSpec.Builder rpcResult =
        TypeSpec.classBuilder(resultName)
            .addModifiers(Modifier.PUBLIC)
            .addField(
                FieldSpec.builder(returnType, "returnValue", Modifier.PUBLIC)
                    .addAnnotation(jsonReturn)
                    .build());
    for (Param param : op.params()) {
      if (!param.isOut()) continue;
      rpcResult.addField(publicField(types.dtoType(param.type()).box(), param.name()));
    }
    rpcResult.addField(
        FieldSpec.builder(SystemExceptionWriter.dtoClass(ctx), "systemException", Modifier.PUBLIC)
            .addAnnotation(
                AnnotationSpec.builder(JSON_PROPERTY)
                    .addMember("value", "$S", "system_exception")
                    .build())
            .addAnnotation(
                AnnotationSpec.builder(
                        ClassName.get("com.fasterxml.jackson.annotation", "JsonInclude"))
                    .addMember(
                        "value",
                        "$T.NON_NULL",
                        ClassName.get("com.fasterxml.jackson.annotation", "JsonInclude", "Include"))
                    .build())
            .build());
    write(rpcResult.build(), ctx);

    TypeSpec rpcResponse =
        TypeSpec.classBuilder(naming.rpcResponseName(op))
            .addModifiers(Modifier.PUBLIC)
            .addField(
                FieldSpec.builder(jsonRpcClass(ctx, "Version"), "jsonrpc", Modifier.PUBLIC)
                    .initializer("$T.V2_0", jsonRpcClass(ctx, "Version"))
                    .build())
            .addField(
                FieldSpec.builder(TypeName.INT, "id", Modifier.PUBLIC).initializer("1").build())
            .addField(publicField(types.dtoClass(resultName), "result"))
            .build();
    write(rpcResponse, ctx);
  }

  /// application.ymlを出力する
  ///
  /// OpenAPIを3.0にするのは、3.1だとSwagger UIが`$ref`の型名を出さず「object」と表示するため。
  /// IDLの識別子に`-`は使えないので、サービス名が`string-charset`などとぶつかることはない。
  ///
  /// @param ctx 生成先
  /// @param serviceNames サービス名
  static void writeApplicationProperties(GenContext ctx, List<String> serviceNames)
      throws IOException {
    String charset =
        """
          string-charset: %s
          response-timeout: 30m
          connect-timeout: 60s
        """
            .formatted(ctx.settings().stringCharset());
    String services =
        serviceNames.stream()
            .map(
                n ->
                    """
                      %s:
                        object-ref:
                    """
                        .formatted(n))
            .collect(Collectors.joining());
    String springdoc =
        """
        springdoc:
          api-docs:
            version: openapi_3_0
        """;
    Files.writeString(
        ctx.resourcesDir().resolve("application.yml"), "corba:\n" + charset + services + springdoc);
  }

  private static FieldSpec publicField(TypeName type, String name) {
    return FieldSpec.builder(type, name, Modifier.PUBLIC).build();
  }

  /// DTOのパッケージにクラスを書き出す
  ///
  /// @param type クラス
  /// @param ctx 生成先
  private static void write(TypeSpec type, GenContext ctx) throws IOException {
    JavaFile.builder(ctx.dtoPackage(), type).build().writeTo(ctx.javaDir());
  }
}
