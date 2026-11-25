package com.toru_otsuki.bridge_generator.gen;

import com.palantir.javapoet.ClassName;
import java.io.IOException;
import java.nio.file.Files;

/// CORBAのシステム例外をJSONで返すためのDTOとMapperを出力する
final class SystemExceptionWriter {
  /// CORBAの標準の型のDTOを入れ子で持つクラスの名前
  static final String CORBA_TYPES = "CORBA";

  private SystemExceptionWriter() {}

  /// {@return システム例外のDTOのクラス}
  ///
  /// @param ctx 生成先
  static ClassName dtoClass(GenContext ctx) {
    return ctx.types().dtoClass(CORBA_TYPES).nestedClass("SystemException");
  }

  /// `CORBA.SystemException`と`CORBA.CompletionStatus`を出力する
  ///
  /// springdocは入れ子のクラスを単純名で載せるので、`@Schema`でスキーマ名を指定する。`minor`はIDLでは符号なしの32ビット整数なので、`long`で持つ。
  ///
  /// @param ctx 生成先
  static void writeDto(GenContext ctx) throws IOException {
    Files.writeString(
        ctx.dtoDir().resolve(CORBA_TYPES + ".java"),
        """
        package %s.dto;

        import com.fasterxml.jackson.annotation.JsonProperty;
        import io.swagger.v3.oas.annotations.media.Schema;

        public final class CORBA {
          private CORBA() {}

          @Schema(name = "CORBA.SystemException")
          public static class SystemException {
            public String id;

            public long minor;

            @JsonProperty("completion_status")
            public CompletionStatus completionStatus;
          }

          @Schema(name = "CORBA.CompletionStatus", enumAsRef = true)
          public enum CompletionStatus {
            COMPLETED_YES,
            COMPLETED_NO,
            COMPLETED_MAYBE
          }
        }
        """
            .formatted(ctx.basePackage()));
  }

  /// `SystemExceptionMapper`を出力する
  ///
  /// 例外からリポジトリIDを得る標準のAPIが無いので、`org.omg.CORBA`のクラス名から作る。
  /// ORBが独自のサブクラスを投げることがあるので、`org.omg.CORBA`のクラスまで親をたどる。
  ///
  /// @param ctx 生成先
  static void writeMapper(GenContext ctx) throws IOException {
    Files.writeString(
        ctx.mapperDir().resolve("SystemExceptionMapper.java"),
        """
        package %1$s.mapper;

        import %1$s.dto.CORBA;

        public final class SystemExceptionMapper {
          private SystemExceptionMapper() {}

          public static CORBA.SystemException toDto(org.omg.CORBA.SystemException e) {
            Class<?> standard = e.getClass();
            while (!standard.getPackageName().equals("org.omg.CORBA")) {
              standard = standard.getSuperclass();
            }
            CORBA.SystemException dto = new CORBA.SystemException();
            dto.id = "IDL:omg.org/CORBA/" + standard.getSimpleName() + ":1.0";
            dto.minor = Integer.toUnsignedLong(e.minor);
            dto.completionStatus = CORBA.CompletionStatus.values()[e.completed.value()];
            return dto;
          }
        }
        """
            .formatted(ctx.basePackage()));
  }
}
