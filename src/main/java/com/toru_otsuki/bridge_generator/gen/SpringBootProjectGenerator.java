package com.toru_otsuki.bridge_generator.gen;

import com.toru_otsuki.bridge_generator.model.IdlEnum;
import com.toru_otsuki.bridge_generator.model.Operation;
import com.toru_otsuki.bridge_generator.model.Service;
import com.toru_otsuki.bridge_generator.model.Struct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/// IDLのサービスからSpring Bootのプロジェクトを生成する
public final class SpringBootProjectGenerator {
  private SpringBootProjectGenerator() {}

  /// 1つのサービスから1つのプロジェクトを生成する
  ///
  /// @param service サービス
  /// @param outputDir プロジェクトのルートディレクトリ
  /// @param settings 共通の設定
  public static void generate(Service service, Path outputDir, GenSettings settings)
      throws IOException {
    GenContext ctx = setupProject(outputDir, settings);
    ServiceDtoWriter.writeJsonRpcTypes(ctx);
    generateService(service, ctx, "CorbaClient");
    ServiceDtoWriter.writeApplicationProperties(ctx, List.of(service.name()));
  }

  /// 全サービスを1つのプロジェクトにまとめて生成する
  ///
  /// @param services サービス
  /// @param outputDir プロジェクトのルートディレクトリ
  /// @param settings 共通の設定
  public static void generateAll(List<Service> services, Path outputDir, GenSettings settings)
      throws IOException {
    GenContext ctx = setupProject(outputDir, settings);
    ServiceDtoWriter.writeJsonRpcTypes(ctx);
    for (Service service : services) {
      generateService(service, ctx, service.name() + "CorbaClient");
    }
    ServiceDtoWriter.writeApplicationProperties(ctx, services.stream().map(Service::name).toList());
  }

  /// サービスによらない、プロジェクトの骨組みと型ごとのDTO・Mapperを生成する
  ///
  /// @param outputDir プロジェクトのルートディレクトリ
  /// @param settings 共通の設定
  /// @return 生成先
  private static GenContext setupProject(Path outputDir, GenSettings settings) throws IOException {
    GenContext ctx = new GenContext(settings, outputDir);
    for (Path dir :
        List.of(
            ctx.controllerDir(),
            ctx.dtoDir(),
            ctx.mapperDir(),
            ctx.corbaDir(),
            ctx.configDir(),
            ctx.resourcesDir())) {
      Files.createDirectories(dir);
    }

    ProjectScaffolder.writeBuildGradle(outputDir);
    GradleWrapperWriter.write(outputDir);
    ProjectScaffolder.writeApplicationClass(ctx);
    ProjectScaffolder.writeServletClass(ctx);
    StubFiles.copyIdlStubSources(settings.stubRoot(), ctx.javaDir());
    AnyTypeWriter.writeAnyValueDto(ctx);
    SystemExceptionWriter.writeDto(ctx);
    SystemExceptionWriter.writeMapper(ctx);
    AnyTypeWriter.writeAnyMapper(ctx);
    StringMapperWriter.write(ctx);
    CorbaClientWriter.writeOrb(ctx);
    for (Struct struct : settings.structs()) {
      DtoGenerator.generate(struct, ctx);
      MapperGenerator.generate(struct, ctx);
    }
    for (IdlEnum idlEnum : settings.enums()) {
      EnumGenerator.generate(idlEnum, ctx);
    }
    OpenApiSchemasWriter.write(ctx);
    return ctx;
  }

  /// 1つのサービスのDTO・CorbaClient・Controllerを生成する
  ///
  /// 宣言した操作が無いサービスは生成しない。
  ///
  /// @param service サービス
  /// @param ctx 生成先
  /// @param clientClassName CorbaClientのクラス名
  private static void generateService(Service service, GenContext ctx, String clientClassName)
      throws IOException {
    if (service.operations().isEmpty()) return;
    for (Operation op : service.operations()) {
      ServiceDtoWriter.generateRequestDto(op, ctx);
      ServiceDtoWriter.generateRpcResponse(op, ctx);
    }
    CorbaClientWriter.write(service, clientClassName, ctx);
    ControllerWriter.write(service, clientClassName, ctx);
  }
}
