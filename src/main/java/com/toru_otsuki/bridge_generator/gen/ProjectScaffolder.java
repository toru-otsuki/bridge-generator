package com.toru_otsuki.bridge_generator.gen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/// build.gradleと、Spring Bootの起動クラスを出力する
final class ProjectScaffolder {
  private ProjectScaffolder() {}

  /// build.gradleを出力する
  ///
  /// 組み込みTomcatは、外部のTomcatにデプロイしたときに本体のクラスと重ならないよう`providedRuntime`にする。
  ///
  /// @param outputDir プロジェクトのルートディレクトリ
  static void writeBuildGradle(Path outputDir) throws IOException {
    String content =
        """
        plugins {
          id 'java'
          id 'war'
          id 'org.springframework.boot' version '4.1.2'
          id 'io.spring.dependency-management' version '1.1.7'
        }

        group = 'generated'
        version = '0.0.1-SNAPSHOT'

        java {
          toolchain {
            languageVersion = JavaLanguageVersion.of(25)
          }
        }

        repositories {
          mavenCentral()
        }

        dependencies {
          implementation 'org.springframework.boot:spring-boot-starter-webmvc'
          implementation 'org.glassfish.corba:glassfish-corba-orb:5.0.3'
          runtimeOnly 'org.glassfish.gmbal:gmbal:4.1.2'
          implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1'
          providedRuntime 'org.springframework.boot:spring-boot-starter-tomcat-runtime'
        }
        """;
    Files.writeString(outputDir.resolve("build.gradle"), content);
  }

  /// `Application`を出力する
  ///
  /// @param ctx 生成先
  static void writeApplicationClass(GenContext ctx) throws IOException {
    String content =
        """
        package %s;

        import org.springframework.boot.SpringApplication;
        import org.springframework.boot.autoconfigure.SpringBootApplication;

        @SpringBootApplication
        public class Application {
          public static void main(String[] args) {
            System.setProperty(
              "org.glassfish.gmbal.no.multipleUpperBoundsException",
              "true");
            SpringApplication.run(Application.class, args);
          }
        }
        """
            .formatted(ctx.basePackage());
    Files.writeString(ctx.packageDir().resolve("Application.java"), content);
  }

  /// 外部のTomcatにデプロイしたときの起動クラス`ServletInitializer`を出力する
  ///
  /// @param ctx 生成先
  static void writeServletClass(GenContext ctx) throws IOException {
    String content =
        """
        package %s;

        import org.springframework.boot.builder.SpringApplicationBuilder;
        import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

        public class ServletInitializer extends SpringBootServletInitializer {
          @Override
          protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
            System.setProperty(
              "org.glassfish.gmbal.no.multipleUpperBoundsException",
              "true");
            return application.sources(Application.class);
          }
        }
        """
            .formatted(ctx.basePackage());
    Files.writeString(ctx.packageDir().resolve("ServletInitializer.java"), content);
  }
}
