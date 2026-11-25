package com.toru_otsuki.bridge_generator.gen;

import com.palantir.javapoet.CodeBlock;
import com.toru_otsuki.bridge_generator.model.CorbaType;
import com.toru_otsuki.bridge_generator.model.Operation;
import com.toru_otsuki.bridge_generator.model.Param;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/// Controllerの、操作1つ分のメソッドのソースを組み立てる
///
/// リクエストを`XxxRpcRequest`で受け取り、DTOからCORBAの型に変換して呼び出す。
///
/// NOTE: CORBAのシステム例外が起きた場合、戻り値はnullとし、system_exceptionを200 OKで返す
final class ControllerMethodBuilder {
  private ControllerMethodBuilder() {}

  /// 操作1つ分のメソッドのソースを返す
  ///
  /// @param op 操作
  /// @param ctx 生成先
  /// @return `POST /操作名`を受けるメソッドのソースコード
  static String build(Operation op, GenContext ctx) {
    String dtoPackage = ctx.dtoPackage();
    Naming naming = ctx.settings().naming();
    String rpcRequest = dtoPackage + "." + naming.rpcRequestName(op);
    String rpcResponse = dtoPackage + "." + naming.rpcResponseName(op);
    String rpcResult = dtoPackage + "." + naming.rpcResultName(op);

    String signature =
        """
          @PostMapping("/%s")
          public %s %s(%s) {
        """
            .formatted(op.name(), rpcResponse, op.name(), "@RequestBody " + rpcRequest + " rpc");
    // paramsを省略したリクエストは、引数を既定値で補って呼び出さずに呼び出し側の誤りとして返す。
    // in引数の無い操作でも、他の操作と同じ形にそろえるため`params: {}`を求める
    String paramsExtraction =
        badRequestIf("rpc.params == null", "params is required")
            + "    %s.%s req = rpc.params;\n".formatted(dtoPackage, naming.rpcParamsName(op));

    // 呼び出し引数を引数順にarg0, arg1, ...として用意する。in引数はDTOから変換し、out引数は空のHolderを、
    // inout引数はDTOから変換した値を入れたHolderを渡す
    TypeMapping types = ctx.types();
    List<String> argNames = new ArrayList<>();
    StringBuilder argConversions = new StringBuilder();
    for (Param param : op.params()) {
      String argName = "arg" + argNames.size();
      argNames.add(argName);
      String javaType = CorbaClientWriter.javaType(param, types);
      String fromDto =
          param.isInput()
              ? types.fromDto(param.type(), CodeBlock.of("req.$L", param.name())).toString()
              : "";
      String init =
          switch (param.direction()) {
            case IN -> fromDto;
            case OUT -> "new " + javaType + "()";
            case INOUT -> "new " + javaType + "(" + fromDto + ")";
          };
      // inout引数はサーバーが入力として使うので、空の値で補わない
      if (param.direction() == Param.Direction.INOUT && TypeMapping.needsValue(param.type())) {
        argConversions.append(
            badRequestIf(
                "req.%s == null".formatted(param.name()),
                "params.%s is required (inout parameter)".formatted(param.name())));
      }
      argConversions.append("    %s %s = %s;\n".formatted(javaType, argName, init));
    }

    String call = "client.%s(%s)".formatted(op.name(), String.join(", ", argNames));
    String body = response(op, call, argNames, rpcResult, rpcResponse, ctx);

    return signature
        + paramsExtraction
        + "    try {\n"
        + indent(argConversions + body)
        + """
            } catch (org.omg.CORBA.SystemException __e) {
              %1$s __result = new %1$s();
              __result.systemException = %3$s.mapper.SystemExceptionMapper.toDto(__e);
              %2$s __response = new %2$s();
              __response.result = __result;
              return __response;
            }
          }

        """
            .formatted(rpcResult, rpcResponse, ctx.basePackage());
  }

  /// 呼び出しとレスポンスを作るコードを返す
  ///
  /// 戻り値を`callResult`に受け、戻り値と各`Holder.value`を変換して`XxxRpcResult`に詰める。
  ///
  /// @param op 操作
  /// @param call CorbaClientの呼び出し式
  /// @param argNames 呼び出し引数の変数名（引数順）
  /// @param rpcResult `XxxRpcResult`の完全修飾名
  /// @param rpcResponse `XxxRpcResponse`の完全修飾名
  /// @param ctx 生成先
  /// @return メソッド本体のうち、呼び出し以降のコード
  private static String response(
      Operation op,
      String call,
      List<String> argNames,
      String rpcResult,
      String rpcResponse,
      GenContext ctx) {
    TypeMapping types = ctx.types();
    List<String> assignments = new ArrayList<>();
    op.returnType()
        .ifPresent(
            t ->
                assignments.add(
                    "__result.returnValue = " + toDto(t, CodeBlock.of("callResult"), ctx)));
    for (int i = 0; i < op.params().size(); i++) {
      Param param = op.params().get(i);
      if (!param.isOut()) continue;
      assignments.add(
          "__result.%s = %s"
              .formatted(
                  param.name(),
                  toDto(param.type(), CodeBlock.of("$L.value", argNames.get(i)), ctx)));
    }

    String callLine =
        op.returnType()
            .map(t -> "%s callResult = %s".formatted(types.corbaType(t), call))
            .orElse(call);
    return """
            %1$s;
            %2$s __result = new %2$s();
        %3$s    %4$s __response = new %4$s();
            __response.result = __result;
            return __response;
        """
        .formatted(
            callLine,
            rpcResult,
            assignments.stream().map(a -> "    " + a + ";\n").collect(Collectors.joining()),
            rpcResponse);
  }

  /// 条件を満たしたら400 Bad Requestを返すif文を返す
  ///
  /// @param condition 400にする条件のJava式
  /// @param message メッセージ。文字列リテラルにそのまま埋め込むので、`"`や`\`を含めない
  /// @return if文のコード
  private static String badRequestIf(String condition, String message) {
    return """
            if (%s) {
              throw new org.springframework.web.server.ResponseStatusException(
                  org.springframework.http.HttpStatus.BAD_REQUEST, "%s");
            }
        """
        .formatted(condition, message);
  }

  private static String indent(String code) {
    return code.lines()
        .map(line -> line.isEmpty() ? "\n" : "  " + line + "\n")
        .collect(Collectors.joining());
  }

  private static String toDto(CorbaType type, CodeBlock rhs, GenContext ctx) {
    return ctx.types().toDto(type, rhs).toString();
  }
}
