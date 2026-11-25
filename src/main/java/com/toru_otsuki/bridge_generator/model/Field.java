package com.toru_otsuki.bridge_generator.model;

/// 構造体・exceptionのフィールド
///
/// @param name フィールド名
/// @param type フィールドの型
public record Field(String name, CorbaType type) {}
