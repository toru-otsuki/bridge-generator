package com.toru_otsuki.bridge_generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/// 起動クラス以外の生成ソースを、spring-web・spring-context・Jackson・CORBAだけをクラスパスに載せてコンパイルする
class GeneratedCodeCompilesTest {
  private static final Set<String> SPRING_BOOT_ONLY =
      Set.of("Application.java", "ServletInitializer.java");

  @Test
  void singleProjectCompiles(@TempDir Path tmp) throws Exception {
    TestGeneration.Result result =
        TestGeneration.run(
            tmp,
            "--single-project",
            "--idl-package-prefix=com.corba",
            TestGeneration.COVERAGE_IDL.toString());
    assertCompiles(result.singleProject(), tmp.resolve("classes"));
  }

  @Test
  void perServiceProjectsCompile(@TempDir Path tmp) throws Exception {
    TestGeneration.Result result = TestGeneration.run(tmp, TestGeneration.COVERAGE_IDL.toString());

    assertEquals(
        Set.of("ItemServiceApi", "EmptyServiceApi", "ClockServiceApi"),
        result.projects().stream()
            .map(p -> p.getFileName().toString())
            .collect(Collectors.toSet()));
    for (Path project : result.projects()) {
      Path controllers = project.resolve("src/main/java/com/generated/controller");
      boolean empty = project.getFileName().toString().equals("EmptyServiceApi");
      try (Stream<Path> list = Files.list(controllers)) {
        assertEquals(empty, list.findAny().isEmpty(), project.toString());
      }
      assertCompiles(project, tmp.resolve("classes-" + project.getFileName()));
    }
  }

  @Test
  void customNamingCompiles(@TempDir Path tmp) throws Exception {
    TestGeneration.Result result =
        TestGeneration.run(
            tmp,
            "--single-project",
            "--idl-package-prefix=com.corba",
            "--dto-suffix=",
            "--rpc-params-suffix=Params",
            "--rpc-request-prefix=Api",
            "--rpc-request-suffix=Request",
            "--rpc-result-suffix=",
            "--rpc-response-suffix=Response",
            TestGeneration.COVERAGE_IDL.toString());
    Path project = result.singleProject();
    assertTrue(Files.exists(project.resolve("src/main/java/com/generated/dto/Item.java")));
    for (String name :
        List.of("FindItemsParams", "ApiFindItemsRequest", "FindItems", "FindItemsResponse")) {
      assertTrue(
          Files.exists(project.resolve("src/main/java/com/generated/dto/" + name + ".java")), name);
    }
    assertCompiles(project, tmp.resolve("classes"));
  }

  private static void assertCompiles(Path project, Path classesDir) throws Exception {
    List<Path> sources;
    try (Stream<Path> walk = Files.walk(project.resolve("src/main/java"))) {
      sources =
          walk.filter(p -> p.toString().endsWith(".java"))
              .filter(p -> !SPRING_BOOT_ONLY.contains(p.getFileName().toString()))
              .toList();
    }
    assertFalse(sources.isEmpty());
    Files.createDirectories(classesDir);

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    try (StandardJavaFileManager files = compiler.getStandardFileManager(diagnostics, null, null)) {
      boolean ok =
          compiler
              .getTask(
                  new StringWriter(),
                  files,
                  diagnostics,
                  List.of(
                      "-classpath",
                      System.getProperty("java.class.path"),
                      "-d",
                      classesDir.toString(),
                      "-encoding",
                      "UTF-8",
                      "-nowarn",
                      "-proc:none"),
                  null,
                  files.getJavaFileObjectsFromPaths(sources))
              .call();
      String errors =
          diagnostics.getDiagnostics().stream()
              .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
              .map(Object::toString)
              .collect(Collectors.joining("\n"));
      assertTrue(ok, () -> project + " のコンパイルに失敗しました:\n" + errors);
    }
  }
}
