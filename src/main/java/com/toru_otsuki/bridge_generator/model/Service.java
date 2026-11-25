package com.toru_otsuki.bridge_generator.model;

import java.util.List;

/// CORBAサービス
///
/// IDLの`interface`。継承した操作は公開しないので持たない。
///
/// @param name Javaのインターフェース名
/// @param pkg Javaパッケージ名
/// @param operations このインターフェースで宣言した操作
public record Service(String name, String pkg, List<Operation> operations) {}
