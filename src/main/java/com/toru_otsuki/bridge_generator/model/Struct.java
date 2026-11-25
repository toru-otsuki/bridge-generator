package com.toru_otsuki.bridge_generator.model;

import java.util.List;

/// DTO・Mapperを生成する対象の型
///
/// フィールドだけを持つ、IDLの`struct`と`exception`由来のクラスを対象とする。
///
/// NOTE: Exceptionはorg.omg.CORBA.UserExceptionのサブクラスとするため分類する。
///
/// @param pkg Javaパッケージ名
/// @param name Javaのクラス名
/// @param repoId idljが付けたリポジトリID
/// @param idlName IDLのスコープ付きの名前
/// @param fields フィールド
/// @param exception `exception`ならtrue
public record Struct(
    String pkg, String name, String repoId, String idlName, List<Field> fields, boolean exception)
    implements TypeDefinition {}
