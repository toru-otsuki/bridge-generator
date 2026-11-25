package com.toru_otsuki.bridge_generator;

import com.toru_otsuki.bridge_generator.gen.GenSettings;
import com.toru_otsuki.bridge_generator.gen.NameConflicts;
import com.toru_otsuki.bridge_generator.gen.SpringBootProjectGenerator;
import com.toru_otsuki.bridge_generator.idl.IdljCompiler;
import com.toru_otsuki.bridge_generator.model.IdlModel;
import com.toru_otsuki.bridge_generator.model.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/// IDLファイルからSpringBootプロジェクトを生成する
///
/// @param options 解析済みのコマンドライン引数
record BridgeGenerator(CliOptions options) {
  /// スタブとモデルを作り、プロジェクトを生成する
  ///
  /// @return 生成したプロジェクトのディレクトリ
  List<Path> run() throws IOException, GenerationException {
    Path stubRoot = this.options.stubRoot();
    Files.createDirectories(stubRoot);
    List<IdlModel> models = new ArrayList<>();

    for (Path idlFile : this.options.idlFiles()) {
      if (!Files.isRegularFile(idlFile)) {
        throw new GenerationException("IDLファイルが見つかりません: " + idlFile);
      }
      models.add(IdljCompiler.compile(idlFile, stubRoot, this.options.idlPackagePrefix()));
    }

    IdlModel model = IdlModel.merge(models);
    if (model.services().isEmpty()) {
      throw new GenerationException("IDLにinterfaceが1つもありません");
    }

    GenSettings settings =
        new GenSettings(
            this.options.basePackage(),
            this.options.stringCharset(),
            stubRoot,
            model.structs(),
            model.enums(),
            this.options.naming());
    List<String> conflicts = NameConflicts.find(model.services(), settings);
    if (!conflicts.isEmpty()) {
      throw new GenerationException(String.join("\n", conflicts));
    }
    List<Path> generated = new ArrayList<>();
    if (this.options.singleProject()) {
      Path output = this.options.outputRoot().resolve(this.options.projectName());
      SpringBootProjectGenerator.generateAll(model.services(), output, settings);
      generated.add(output);
    } else {
      for (Service service : model.services()) {
        Path output = this.options.outputRoot().resolve(service.name() + "Api");
        SpringBootProjectGenerator.generate(service, output, settings);
        generated.add(output);
      }
    }
    return generated;
  }
}
