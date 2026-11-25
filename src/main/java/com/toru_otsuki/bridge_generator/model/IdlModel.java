package com.toru_otsuki.bridge_generator.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/// IDLから読み取った、生成の対象になる定義
///
/// @param services サービス
/// @param structs 構造体とexception
/// @param enums enum
public record IdlModel(List<Service> services, List<Struct> structs, List<IdlEnum> enums) {

  /// IDLファイルごとのモデルを1つにまとめる
  ///
  /// IDL同士がincludeし合うと同じ型が何度も現れるので、完全修飾名で1つにする。
  /// IDLファイルを渡す順によらず同じものを生成するよう、ファイルパスの順（大文字小文字を区別しない）に並べる。
  ///
  /// @param models IDLファイルごとのモデル
  /// @return まとめたモデル
  public static IdlModel merge(Collection<IdlModel> models) {
    Map<String, Service> services = new LinkedHashMap<>();
    Map<String, Struct> structs = new LinkedHashMap<>();
    Map<String, IdlEnum> enums = new LinkedHashMap<>();
    for (IdlModel m : models) {
      m.services().forEach(s -> services.putIfAbsent(path(s.pkg(), s.name() + "Operations"), s));
      m.structs().forEach(s -> structs.putIfAbsent(path(s.pkg(), s.name()), s));
      m.enums().forEach(e -> enums.putIfAbsent(path(e.pkg(), e.name()), e));
    }
    return new IdlModel(sorted(services), sorted(structs), sorted(enums));
  }

  /// 重複の判定と並べ替えに使う、スタブのファイルパスを大文字にしたキーを返す
  ///
  /// @param pkg Javaパッケージ名
  /// @param name Javaのクラス名
  /// @return キー
  private static String path(String pkg, String name) {
    return (pkg.isEmpty() ? name : pkg.replace('.', '/') + "/" + name).toUpperCase(Locale.ROOT);
  }

  /// {@return キーの順に並べた値}
  ///
  /// @param byPath [#path]をキーにした定義
  /// @param <T> 定義の型
  private static <T> List<T> sorted(Map<String, T> byPath) {
    return byPath.entrySet().stream()
        .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
        .map(Map.Entry::getValue)
        .toList();
  }
}
