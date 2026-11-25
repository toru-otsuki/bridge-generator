package com.toru_otsuki.bridge_generator.model;

/// DTOとMapperを生成する、IDLで名前を付けて定義した型
public sealed interface TypeDefinition permits Struct, IdlEnum {
  /// {@return Javaパッケージ名}
  String pkg();

  /// {@return Javaのクラス名}
  String name();

  /// {@return idljが付けたリポジトリID}
  String repoId();

  /// {@return IDLのスコープ付きの名前}
  String idlName();

  /// {@return この型を参照するときの型モデル}
  default CorbaType.Named type() {
    return new CorbaType.Named(this.pkg(), this.name());
  }
}
