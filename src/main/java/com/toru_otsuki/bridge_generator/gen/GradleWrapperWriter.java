package com.toru_otsuki.bridge_generator.gen;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

/// 生成するプロジェクトにGradle Wrapperと`gradle.properties`を入れる
///
/// Wrapperはジェネレータ自身のものを、ビルド時にリソースへ含めておいて書き出す（build.gradleの`processResources`）。
/// 大きなIDLでは既定の512MBのヒープではコンパイルが止まるので、`gradle.properties`で4GBにする。
final class GradleWrapperWriter {
  /// ジェネレータのリソースのうち、Wrapperを置いた場所
  private static final String RESOURCE_ROOT = "/project-wrapper/";

  /// 書き出すWrapperのファイル
  private static final List<String> FILES =
      List.of(
          "gradlew",
          "gradlew.bat",
          "gradle/wrapper/gradle-wrapper.jar",
          "gradle/wrapper/gradle-wrapper.properties");

  private GradleWrapperWriter() {}

  /// Gradle Wrapperと`gradle.properties`を書き出す
  ///
  /// @param outputDir プロジェクトのルートディレクトリ
  static void write(Path outputDir) throws IOException {
    for (String file : FILES) {
      Path target = outputDir.resolve(file);
      Files.createDirectories(target.getParent());
      try (InputStream in = GradleWrapperWriter.class.getResourceAsStream(RESOURCE_ROOT + file)) {
        Files.copy(Objects.requireNonNull(in, file), target, StandardCopyOption.REPLACE_EXISTING);
      }
    }
    // リソースからのコピーでは実行権限が付かない。Windowsでは何も起きない
    outputDir.resolve("gradlew").toFile().setExecutable(true, false);
    Files.writeString(
        outputDir.resolve("gradle.properties"),
        """
        org.gradle.jvmargs=-Xmx4g
        """);
  }
}
