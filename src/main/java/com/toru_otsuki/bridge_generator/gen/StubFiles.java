package com.toru_otsuki.bridge_generator.gen;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

/// idljが出力したスタブのファイルを扱う
final class StubFiles {
  private StubFiles() {}

  /// 1ファイル分の処理
  @FunctionalInterface
  interface FileAction {
    /// 1ファイルを処理する
    ///
    /// @param path ファイル
    void apply(Path path) throws IOException;
  }

  /// ディレクトリの下にあるJavaのソースファイルを、サブディレクトリも含めて集める
  ///
  /// @param root ディレクトリ
  /// @return ソースファイル
  static List<Path> javaFiles(Path root) throws IOException {
    try (Stream<Path> walk = Files.walk(root)) {
      return walk.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".java")).toList();
    }
  }

  /// 大きなIDLではスタブが1万ファイルに及ぶので、ファイルごとの処理を並列に行う
  ///
  /// @param files ファイル
  /// @param action 1ファイル分の処理
  static void forEachParallel(List<Path> files, FileAction action) throws IOException {
    // 並列ストリームはチェック例外を投げられないので、包んで外で元に戻す
    try {
      files.parallelStream()
          .forEach(
              p -> {
                try {
                  action.apply(p);
                } catch (IOException e) {
                  throw new UncheckedIOException(e);
                }
              });
    } catch (UncheckedIOException e) {
      throw e.getCause();
    }
  }

  /// スタブのソースを、パッケージの構成を保って生成プロジェクトへコピーする
  ///
  /// @param stubSourceRoot idljの出力先
  /// @param javaDir 生成プロジェクトの`src/main/java`
  static void copyIdlStubSources(Path stubSourceRoot, Path javaDir) throws IOException {
    forEachParallel(
        javaFiles(stubSourceRoot),
        p -> {
          Path target = javaDir.resolve(stubSourceRoot.relativize(p));
          Files.createDirectories(target.getParent());
          Files.copy(p, target, StandardCopyOption.REPLACE_EXISTING);
        });
  }
}
