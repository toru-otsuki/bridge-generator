package com.toru_otsuki.bridge_generator.gen;

import com.toru_otsuki.bridge_generator.model.IdlEnum;
import com.toru_otsuki.bridge_generator.model.Struct;
import com.toru_otsuki.bridge_generator.model.TypeDefinition;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/// 生成するどのプロジェクトにも共通の設定
///
/// @param basePackage 生成プロジェクトのベースパッケージ
/// @param stringCharset 生成アプリの`corba.string-charset`の既定値
/// @param stubRoot idljの出力先。ここから生成プロジェクトへコピーする
/// @param structs 構造体
/// @param enums enum
/// @param naming クラスの名前の付け方
public record GenSettings(
    String basePackage,
    String stringCharset,
    Path stubRoot,
    List<Struct> structs,
    List<IdlEnum> enums,
    Naming naming) {

  /// {@return 構造体・exception・enumを、この順で}
  List<TypeDefinition> definitions() {
    return Stream.<TypeDefinition>concat(this.structs.stream(), this.enums.stream()).toList();
  }

  /// {@return このベースパッケージと名前の付け方での型の対応付け}
  TypeMapping types() {
    return new TypeMapping(this.basePackage, this.naming);
  }
}
