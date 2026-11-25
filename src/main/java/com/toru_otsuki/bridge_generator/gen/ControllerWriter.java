package com.toru_otsuki.bridge_generator.gen;

import com.toru_otsuki.bridge_generator.model.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

/// サービスごとのControllerを出力する
final class ControllerWriter {
  private ControllerWriter() {}

  /// 操作ごとに`POST /操作名`を受けるControllerを出力する
  ///
  /// @param service サービス
  /// @param clientClassName 呼び出すCorbaClientのクラス名
  /// @param ctx 生成先
  static void write(Service service, String clientClassName, GenContext ctx) throws IOException {
    String controllerName = service.name() + "Controller";
    String clientFqn = ctx.basePackage() + ".corba." + clientClassName;

    String methods =
        service.operations().stream()
            .map(op -> ControllerMethodBuilder.build(op, ctx))
            .collect(Collectors.joining());

    String controller =
        """
        package %1$s.controller;

        import org.springframework.web.bind.annotation.RestController;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.PostMapping;
        import org.springframework.web.bind.annotation.RequestBody;

        @RestController
        @RequestMapping("/")
        public class %2$s {
          private final %3$s client;

          public %2$s(%3$s client) {
            this.client = client;
          }

        %4$s
        }
        """
            .formatted(ctx.basePackage(), controllerName, clientFqn, methods);

    Files.writeString(ctx.controllerDir().resolve(controllerName + ".java"), controller);
  }
}
