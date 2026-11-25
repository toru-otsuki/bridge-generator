package com.toru_otsuki.bridge_generator.gen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.palantir.javapoet.CodeBlock;
import com.toru_otsuki.bridge_generator.model.CorbaType;
import org.junit.jupiter.api.Test;

class TypeMappingTest {
  private static final TypeMapping TYPES = new TypeMapping("b", Naming.DEFAULT);

  private static final CodeBlock X = CodeBlock.of("x");

  private static final CorbaType TAG = new CorbaType.Named("A", "Tag");

  private static final CorbaType INT = new CorbaType.Primitive("int");

  private static CorbaType arrayOf(CorbaType element) {
    return new CorbaType.ArrayOf(element);
  }

  @Test
  void typeNames() {
    assertEquals("b.dto.TagDto[]", TYPES.dtoType(arrayOf(TAG)).toString());
    assertEquals("A.Tag[]", TYPES.corbaType(arrayOf(TAG)).toString());
    assertEquals("b.dto.AnyValue", TYPES.dtoType(CorbaType.ANY).toString());
    assertEquals("String[]", TYPES.corbaType(arrayOf(CorbaType.STRING)).toString());
    assertEquals("int", TYPES.dtoType(INT).toString());
  }

  @Test
  void stringIsAlwaysConverted() {
    assertEquals("b.mapper.StringMapper.encode(x)", TYPES.toDto(CorbaType.STRING, X).toString());
    assertEquals("b.mapper.StringMapper.decode(x)", TYPES.fromDto(CorbaType.STRING, X).toString());
    assertEquals(
        "x == null ? null : java.util.Arrays.stream(x).map(b.mapper.StringMapper::encode).toArray(String[]::new)",
        TYPES.toDto(arrayOf(CorbaType.STRING), X).toString());
  }

  @Test
  void primitivesAreNotConverted() {
    assertEquals("x", TYPES.toDto(INT, X).toString());
    assertEquals("x", TYPES.toDto(arrayOf(INT), X).toString());
    assertEquals("x", TYPES.fromDto(arrayOf(INT), X).toString());
  }

  @Test
  void structsAndAnyAreConverted() {
    assertEquals("b.mapper.TagMapper.toDto(x)", TYPES.toDto(TAG, X).toString());
    assertEquals("b.mapper.AnyMapper.toAnyValue(x)", TYPES.toDto(CorbaType.ANY, X).toString());
    assertEquals(
        "x == null ? null : java.util.Arrays.stream(x).map(b.mapper.TagMapper::fromDto).toArray(A.Tag[]::new)",
        TYPES.fromDto(arrayOf(TAG), X).toString());
  }

  @Test
  void needsValueOnlyForTypesThatCannotBeNull() {
    assertTrue(TypeMapping.needsValue(CorbaType.STRING));
    assertTrue(TypeMapping.needsValue(TAG));
    assertTrue(TypeMapping.needsValue(arrayOf(INT)));
    assertFalse(TypeMapping.needsValue(INT));
    assertFalse(TypeMapping.needsValue(CorbaType.ANY));
  }

  @Test
  void nestedArraysUseLambdas() {
    assertEquals(
        "x == null ? null : java.util.Arrays.stream(x).map(e0 -> e0 == null ? null :"
            + " java.util.Arrays.stream(e0).map(b.mapper.TagMapper::toDto).toArray(b.dto.TagDto[]::new))"
            + ".toArray(b.dto.TagDto[][]::new)",
        TYPES.toDto(arrayOf(arrayOf(TAG)), X).toString());
  }
}
