package com.toru_otsuki.bridge_generator;

/// 入力やツールの問題で生成を続けられないことを表す例外
public class GenerationException extends Exception {
  /// 利用者に表示する説明を持つ例外を作る
  ///
  /// @param message 説明
  public GenerationException(String message) {
    super(message);
  }
}
