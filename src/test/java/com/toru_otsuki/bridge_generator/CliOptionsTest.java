package com.toru_otsuki.bridge_generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.toru_otsuki.bridge_generator.gen.Naming;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class CliOptionsTest {

  @Test
  void defaults() {
    CliOptions o = CliOptions.parse("a.idl");
    assertEquals(List.of(Path.of("a.idl")), o.idlFiles());
    assertEquals(Path.of("generated-stubs"), o.stubRoot());
    assertEquals(Path.of("generated-spring"), o.outputRoot());
    assertEquals("com.generated", o.basePackage());
    assertEquals("", o.idlPackagePrefix());
    assertEquals("SJIS", o.stringCharset());
    assertFalse(o.singleProject());
    assertEquals("CombinedApi", o.projectName());
    assertEquals(Naming.DEFAULT, o.naming());
  }

  @Test
  void allOptions() {
    CliOptions o =
        CliOptions.parse(
            "--stub-root=s",
            "--output-root=o",
            "--base-package=com.bridge",
            "--idl-package-prefix=com.corba",
            "--string-charset=Windows-31J",
            "--single-project",
            "--project-name=Bridge",
            "--dto-prefix=Cx",
            "--dto-suffix=",
            "--rpc-params-prefix=P",
            "--rpc-params-suffix=Params",
            "--rpc-request-prefix=Q",
            "--rpc-request-suffix=Request",
            "--rpc-result-prefix=S",
            "--rpc-result-suffix=",
            "--rpc-response-prefix=T",
            "--rpc-response-suffix=Response",
            "a.idl",
            "b.idl");
    assertEquals(List.of(Path.of("a.idl"), Path.of("b.idl")), o.idlFiles());
    assertEquals(Path.of("s"), o.stubRoot());
    assertEquals(Path.of("o"), o.outputRoot());
    assertEquals("com.bridge", o.basePackage());
    assertEquals("com.corba", o.idlPackagePrefix());
    assertEquals("Windows-31J", o.stringCharset());
    assertTrue(o.singleProject());
    assertEquals("Bridge", o.projectName());
    assertEquals(
        new Naming(
            new Naming.Affix("Cx", ""),
            new Naming.Affix("P", "Params"),
            new Naming.Affix("Q", "Request"),
            new Naming.Affix("S", ""),
            new Naming.Affix("T", "Response")),
        o.naming());
  }

  @Test
  void helpIsDetectedBeforeParsing() {
    assertTrue(CliOptions.isHelpRequested("--unknown", "--help"));
    assertTrue(CliOptions.isHelpRequested("-h"));
    assertFalse(CliOptions.isHelpRequested("a.idl"));
    assertFalse(CliOptions.isHelpRequested());
  }

  @Test
  void stringCharsetNoneMeansNoConversion() {
    assertEquals("none", CliOptions.parse("--string-charset=none", "a.idl").stringCharset());
  }

  @Test
  void rejectsInvalidInput() {
    assertThrows(IllegalArgumentException.class, () -> CliOptions.parse("--unknown", "a.idl"));
    assertThrows(
        IllegalArgumentException.class, () -> CliOptions.parse("--return-mode=dto", "a.idl"));
    assertThrows(IllegalArgumentException.class, () -> CliOptions.parse("--target=rest", "a.idl"));
    assertThrows(
        IllegalArgumentException.class, () -> CliOptions.parse("--result-suffix=R", "a.idl"));
    assertThrows(
        IllegalArgumentException.class, () -> CliOptions.parse("--string-charset=NoSuch", "a.idl"));
    assertThrows(
        IllegalArgumentException.class, () -> CliOptions.parse("--string-charset=a b", "a.idl"));
    assertThrows(IllegalArgumentException.class, () -> CliOptions.parse("--single-project"));
    assertThrows(
        IllegalArgumentException.class, () -> CliOptions.parse("--dto-suffix=-x", "a.idl"));
    assertThrows(IllegalArgumentException.class, () -> CliOptions.parse("--dto-prefix=1", "a.idl"));
    assertThrows(
        IllegalArgumentException.class, () -> CliOptions.parse("--rpc-result-suffix=a.b", "a.idl"));
  }

  @Test
  void suffixMayStartWithDigit() {
    assertEquals("2", CliOptions.parse("--dto-suffix=2", "a.idl").naming().dto().suffix());
  }

  @Test
  void usageListsEveryOption() {
    String usage = CliOptions.usage();
    for (String option :
        List.of(
            "--stub-root",
            "--output-root",
            "--base-package",
            "--idl-package-prefix",
            "--string-charset",
            "--single-project",
            "--project-name",
            "--dto-prefix",
            "--dto-suffix",
            "--rpc-params-prefix",
            "--rpc-params-suffix",
            "--rpc-request-prefix",
            "--rpc-request-suffix",
            "--rpc-result-prefix",
            "--rpc-result-suffix",
            "--rpc-response-prefix",
            "--rpc-response-suffix",
            "--help")) {
      assertTrue(usage.contains(option), option);
    }
  }
}
