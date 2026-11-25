package com.toru_otsuki.bridge_generator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GradleWrapperTest {
  @Test
  void wrapperIsCopied(@TempDir Path tmp) throws Exception {
    TestGeneration.Result result =
        TestGeneration.run(tmp, "--single-project", TestGeneration.COVERAGE_IDL.toString());
    for (String file : TestGeneration.WRAPPER_FILES) {
      assertArrayEquals(
          Files.readAllBytes(Path.of(file)),
          Files.readAllBytes(result.singleProject().resolve(file)),
          file);
    }
  }
}
