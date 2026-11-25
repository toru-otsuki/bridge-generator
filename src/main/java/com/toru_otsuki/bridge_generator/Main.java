package com.toru_otsuki.bridge_generator;

import java.nio.file.Path;

/// コマンドラインの入口
public class Main {
  private Main() {}

  /// 生成を実行し、生成したプロジェクトの場所を表示する
  ///
  /// 終了コード
  ///
  /// - 0: 正常終了
  /// - 1: 生成失敗
  /// - 2: 引数誤り
  ///
  /// @param args コマンドライン引数
  public static void main(String[] args) {
    if (CliOptions.isHelpRequested(args)) {
      System.out.print(CliOptions.usage());
      return;
    }

    CliOptions options;
    try {
      options = CliOptions.parse(args);
    } catch (IllegalArgumentException e) {
      System.err.println(e.getMessage());
      System.exit(2);
      return;
    }

    try {
      for (Path project : new BridgeGenerator(options).run()) {
        System.out.println("Project generated at: " + project);
      }
    } catch (GenerationException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
