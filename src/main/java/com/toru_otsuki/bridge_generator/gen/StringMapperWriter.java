package com.toru_otsuki.bridge_generator.gen;

import java.io.IOException;
import java.nio.file.Files;

/// 文字列の文字コードを読み替える`StringMapper`を出力する
final class StringMapperWriter {
  private StringMapperWriter() {}

  /// `StringMapper`を出力する
  ///
  /// CORBAサーバーは自分の文字コード（SJISなど）のバイト列をISO-8859-1の文字列として受け渡すので、その文字コードで読み直す。
  /// Mapperは静的メソッドから呼ばれるので、StringMapper自身をBeanにして、起動時に`corba.string-charset`を静的フィールドへ移す。
  ///
  /// @param ctx 生成先
  static void write(GenContext ctx) throws IOException {
    String content =
        """
        package %1$s.mapper;

        import java.nio.charset.Charset;
        import java.nio.charset.StandardCharsets;
        import org.springframework.beans.factory.annotation.Value;
        import org.springframework.stereotype.Component;

        @Component
        public final class StringMapper {
          private static volatile Charset charset = toCharset("%2$s");

          public StringMapper(@Value("${corba.string-charset:%2$s}") String charsetName) {
            charset = toCharset(charsetName);
          }

          private static Charset toCharset(String name) {
            return name.isBlank() || name.equalsIgnoreCase("none") ? null : Charset.forName(name.strip());
          }

          public static String encode(String text) {
            Charset cs = charset;
            if (text == null || cs == null) return text;
            return new String(text.getBytes(StandardCharsets.ISO_8859_1), cs);
          }

          public static String decode(String text) {
            Charset cs = charset;
            if (text == null || cs == null) return text;
            return new String(text.getBytes(cs), StandardCharsets.ISO_8859_1);
          }
        }
        """
            .formatted(ctx.basePackage(), ctx.settings().stringCharset());
    Files.writeString(ctx.mapperDir().resolve("StringMapper.java"), content);
  }
}
