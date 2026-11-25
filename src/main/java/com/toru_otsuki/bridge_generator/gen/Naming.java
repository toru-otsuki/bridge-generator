package com.toru_otsuki.bridge_generator.gen;

import com.toru_otsuki.bridge_generator.model.Operation;
import java.util.List;
import javax.lang.model.SourceVersion;

/// OpenAPIのスキーマ名になるクラス名の、前後に付ける文字列
///
/// @param dto 構造体・enumのDTO
/// @param rpcParams リクエストの`params`のクラス
/// @param rpcRequest リクエスト全体のクラス
/// @param rpcResult レスポンスの`result`のクラス
/// @param rpcResponse レスポンス全体のクラス
public record Naming(
    Affix dto, Affix rpcParams, Affix rpcRequest, Affix rpcResult, Affix rpcResponse) {
  /// 指定が無いときの付け方
  public static final Naming DEFAULT =
      new Naming(
          new Affix("", "Dto"),
          new Affix("", "RpcParams"),
          new Affix("", "RpcRequest"),
          new Affix("", "RpcResult"),
          new Affix("", "RpcResponse"));

  /// クラス名の前後に付ける文字列
  ///
  /// @param prefix 前に付ける文字列
  /// @param suffix 後ろに付ける文字列
  public record Affix(String prefix, String suffix) {
    /// {@return `name`の前後に文字列を付けた名前}
    ///
    /// @param name 元の名前
    String apply(String name) {
      return this.prefix + name + this.suffix;
    }
  }

  /// クラス名の前後に付けられる文字列か
  ///
  /// @param affix 前後に付ける文字列。空でもよい
  /// @param prefix 前に付けるならtrue
  /// @return 付けた結果がJavaの識別子になるならtrue
  public static boolean isValidAffix(String affix, boolean prefix) {
    // HACK: 空文字を許容するため仮の文字"A"を付与する
    return SourceVersion.isIdentifier(prefix ? affix + "A" : "A" + affix);
  }

  /// {@return 構造体・enumのDTOのクラス名}
  ///
  /// @param idlName idljが付けたクラス名
  String dtoName(String idlName) {
    return this.dto.apply(idlName);
  }

  /// {@return リクエストの`params`のクラス名}
  ///
  /// @param op 操作
  String rpcParamsName(Operation op) {
    return this.rpcParams.apply(op.capitalizedName());
  }

  /// {@return リクエスト全体のクラス名}
  ///
  /// @param op 操作
  String rpcRequestName(Operation op) {
    return this.rpcRequest.apply(op.capitalizedName());
  }

  /// {@return レスポンスの`result`のクラス名}
  ///
  /// @param op 操作
  String rpcResultName(Operation op) {
    return this.rpcResult.apply(op.capitalizedName());
  }

  /// {@return レスポンス全体のクラス名}
  ///
  /// @param op 操作
  String rpcResponseName(Operation op) {
    return this.rpcResponse.apply(op.capitalizedName());
  }

  /// {@return 操作ごとに生成する4つのクラスの名前}
  ///
  /// @param op 操作
  List<String> rpcClassNames(Operation op) {
    return List.of(
        this.rpcParamsName(op),
        this.rpcRequestName(op),
        this.rpcResultName(op),
        this.rpcResponseName(op));
  }
}
