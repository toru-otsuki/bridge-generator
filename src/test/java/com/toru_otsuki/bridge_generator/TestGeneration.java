package com.toru_otsuki.bridge_generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

final class TestGeneration {
  /// 生成ロジックの各分岐を通るように作ったIDL
  static final Path COVERAGE_IDL = Path.of("src/test/resources/idl/coverage.idl");

  static final List<String> WRAPPER_FILES =
      List.of(
          "gradlew",
          "gradlew.bat",
          "gradle/wrapper/gradle-wrapper.jar",
          "gradle/wrapper/gradle-wrapper.properties");

  private TestGeneration() {}

  record Result(Path stubRoot, List<Path> projects) {
    Path singleProject() {
      if (this.projects.size() != 1) throw new IllegalStateException("projects: " + this.projects);
      return this.projects.get(0);
    }
  }

  /// @param workDir 作業ディレクトリ
  /// @param args `--stub-root`と`--output-root`以外の引数
  /// @return 生成結果
  static Result run(Path workDir, String... args) throws Exception {
    Path stubRoot = workDir.resolve("stubs");
    List<String> all = new ArrayList<>();
    all.add("--stub-root=" + stubRoot);
    all.add("--output-root=" + workDir.resolve("spring"));
    all.addAll(List.of(args));
    List<Path> projects = new BridgeGenerator(CliOptions.parse(all.toArray(String[]::new))).run();
    return new Result(stubRoot, projects);
  }

  /// @param project プロジェクトのディレクトリ
  /// @param stubRoot idljの出力先。コピーしたスタブを除くのに使う
  /// @return 相対パス（`/`区切り）ごとの内容。スタブとGradle Wrapperは除く
  static Map<String, String> generatedFiles(Path project, Path stubRoot) throws IOException {
    Path javaDir = project.resolve("src/main/java");
    Map<String, String> files = new TreeMap<>();
    try (Stream<Path> walk = Files.walk(project)) {
      for (Path p : walk.filter(Files::isRegularFile).toList()) {
        if (p.startsWith(javaDir) && Files.exists(stubRoot.resolve(javaDir.relativize(p)))) {
          continue;
        }
        String relative = project.relativize(p).toString().replace('\\', '/');
        if (WRAPPER_FILES.contains(relative)) continue;
        files.put(relative, read(p));
      }
    }
    return files;
  }

  /// チェックアウト時の改行変換に左右されないよう、改行をLFにそろえて読む
  ///
  /// @param p ファイル
  /// @return 内容
  static String read(Path p) throws IOException {
    return Files.readString(p).replace("\r\n", "\n");
  }
}
