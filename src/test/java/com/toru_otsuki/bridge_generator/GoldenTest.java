package com.toru_otsuki.bridge_generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/// 生成ロジックを意図して変えたときは`-Dgolden.update=true`で期待値を更新し、差分を確かめてからコミットする。
class GoldenTest {
  private static final Path GOLDEN_ROOT = Path.of("src/test/resources/golden");

  static Stream<Arguments> scenarios() {
    String coverage = TestGeneration.COVERAGE_IDL.toString();
    return Stream.of(
        Arguments.of(
            "coverage-dto",
            List.of("--single-project", "--idl-package-prefix=com.corba", coverage)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("scenarios")
  void generatedProjectMatchesGolden(String name, List<String> options, @TempDir Path tmp)
      throws Exception {
    TestGeneration.Result result = TestGeneration.run(tmp, options.toArray(String[]::new));
    Map<String, String> actual =
        TestGeneration.generatedFiles(result.singleProject(), result.stubRoot());

    Path goldenDir = GOLDEN_ROOT.resolve(name);
    if (Boolean.getBoolean("golden.update")) {
      writeGolden(goldenDir, actual);
      return;
    }

    Map<String, String> expected = new TreeMap<>();
    try (Stream<Path> walk = Files.walk(goldenDir)) {
      for (Path p : walk.filter(Files::isRegularFile).toList()) {
        expected.put(goldenDir.relativize(p).toString().replace('\\', '/'), TestGeneration.read(p));
      }
    }
    assertEquals(expected.keySet(), actual.keySet(), "生成されたファイルの一覧が期待値と異なります");
    for (String path : expected.keySet()) {
      assertEquals(expected.get(path), actual.get(path), path);
    }
  }

  private static void writeGolden(Path goldenDir, Map<String, String> files) throws Exception {
    if (Files.exists(goldenDir)) {
      try (Stream<Path> walk = Files.walk(goldenDir)) {
        for (Path p : walk.sorted(Comparator.reverseOrder()).toList()) Files.delete(p);
      }
    }
    for (Map.Entry<String, String> e : files.entrySet()) {
      Path target = goldenDir.resolve(e.getKey());
      Files.createDirectories(target.getParent());
      Files.writeString(target, e.getValue());
    }
  }
}
