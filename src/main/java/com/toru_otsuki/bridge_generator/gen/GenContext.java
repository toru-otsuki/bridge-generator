package com.toru_otsuki.bridge_generator.gen;

import java.nio.file.Path;

/// 生成するプロジェクト1つ分の設定と出力先
///
/// @param settings 共通の設定
/// @param outputDir プロジェクトのルートディレクトリ
record GenContext(GenSettings settings, Path outputDir) {

  /// {@return 生成プロジェクトのベースパッケージ}
  String basePackage() {
    return this.settings.basePackage();
  }

  /// {@return 型の対応付け}
  TypeMapping types() {
    return this.settings.types();
  }

  /// {@return DTOのパッケージ}
  String dtoPackage() {
    return this.types().dtoPackage();
  }

  /// {@return `src/main/java`}
  Path javaDir() {
    return this.outputDir.resolve("src/main/java");
  }

  /// {@return ベースパッケージのディレクトリ}
  Path packageDir() {
    return this.javaDir().resolve(this.basePackage().replace('.', '/'));
  }

  /// {@return Controllerのディレクトリ}
  Path controllerDir() {
    return this.packageDir().resolve("controller");
  }

  /// {@return CorbaClientとCorbaOrbのディレクトリ}
  Path corbaDir() {
    return this.packageDir().resolve("corba");
  }

  /// {@return DTOのディレクトリ}
  Path dtoDir() {
    return this.packageDir().resolve("dto");
  }

  /// {@return Mapperのディレクトリ}
  Path mapperDir() {
    return this.packageDir().resolve("mapper");
  }

  /// {@return Springの設定クラスのディレクトリ}
  Path configDir() {
    return this.packageDir().resolve("config");
  }

  /// {@return `src/main/resources`}
  Path resourcesDir() {
    return this.outputDir.resolve("src/main/resources");
  }
}
