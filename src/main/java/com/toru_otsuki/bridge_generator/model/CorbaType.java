package com.toru_otsuki.bridge_generator.model;

/// idljが生成したJava型を、コード生成での扱いの違いで分けたもの
///
/// DTOの型と変換式は`TypeMapping`が決める。
public sealed interface CorbaType {

  /// Javaのプリミティブ型になるIDLの基本型
  ///
  /// @param name Javaの型名（`int`・`double`など）
  record Primitive(String name) implements CorbaType {}

  /// `string`・`wstring`
  ///
  /// CORBAとの受け渡しで文字コードを読み替える。
  record Str() implements CorbaType {}

  /// `any`
  record AnyType() implements CorbaType {}

  /// `struct`・`exception`・`enum`
  ///
  /// DTOとMapperで変換する。
  ///
  /// @param pkg Javaパッケージ名
  /// @param simpleName Javaのクラス名
  record Named(String pkg, String simpleName) implements CorbaType {}

  /// `sequence`と配列
  ///
  /// @param element 要素の型
  record ArrayOf(CorbaType element) implements CorbaType {}

  /// `string`・`wstring`の型
  CorbaType STRING = new Str();

  /// `any`の型
  CorbaType ANY = new AnyType();
}
