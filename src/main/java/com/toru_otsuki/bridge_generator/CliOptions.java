package com.toru_otsuki.bridge_generator;

import com.toru_otsuki.bridge_generator.gen.Naming;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/// コマンドライン引数
///
/// @param idlFiles IDLファイル
/// @param stubRoot idljの出力先
/// @param outputRoot 生成プロジェクトの出力先
/// @param basePackage 生成プロジェクトのベースパッケージ
/// @param idlPackagePrefix `idlj -pkgPrefix`に渡すパッケージ
/// @param stringCharset CORBAサーバーが文字列に使う文字コード。`none`なら読み替えない
/// @param singleProject 全サービスを1つのプロジェクトにまとめるか
/// @param projectName まとめるときのプロジェクト名
/// @param naming クラスの名前の付け方
record CliOptions(
    List<Path> idlFiles,
    Path stubRoot,
    Path outputRoot,
    String basePackage,
    String idlPackagePrefix,
    String stringCharset,
    boolean singleProject,
    String projectName,
    Naming naming) {
  /// `--string-charset`で文字コードを読み替えないことを表す値
  static final String NO_CONVERSION = "none";

  private static final Set<String> HELP_OPTIONS = Set.of("--help", "-h");
  private static final Pattern AFFIX_OPTION =
      Pattern.compile("--(dto|rpc-params|rpc-request|rpc-result|rpc-response)-(prefix|suffix)=.*");

  /// IDLファイルの一覧を変更できない複製にする
  CliOptions {
    idlFiles = List.copyOf(idlFiles);
  }

  /// ヘルプの表示を求められているか
  ///
  /// @param args コマンドライン引数
  /// @return `--help`か`-h`があればtrue
  static boolean isHelpRequested(String... args) {
    for (String arg : args) {
      if (HELP_OPTIONS.contains(arg)) return true;
    }
    return false;
  }

  /// コマンドライン引数を解析する
  ///
  /// @param args コマンドライン引数
  /// @return 解析結果
  static CliOptions parse(String... args) {
    List<Path> idlFiles = new ArrayList<>();
    Path stubRoot = Path.of("generated-stubs");
    Path outputRoot = Path.of("generated-spring");
    String basePackage = "com.generated";
    String idlPackagePrefix = "";
    String stringCharset = "SJIS";
    boolean singleProject = false;
    String projectName = "CombinedApi";
    Map<String, String> affixes = new HashMap<>();

    for (String arg : args) {
      if (arg.startsWith("--stub-root=")) {
        stubRoot = Path.of(valueOf(arg));
      } else if (arg.startsWith("--output-root=")) {
        outputRoot = Path.of(valueOf(arg));
      } else if (arg.startsWith("--base-package=")) {
        basePackage = valueOf(arg);
      } else if (arg.startsWith("--idl-package-prefix=")) {
        idlPackagePrefix = valueOf(arg);
      } else if (arg.startsWith("--string-charset=")) {
        stringCharset = valueOf(arg);
      } else if (arg.equals("--single-project")) {
        singleProject = true;
      } else if (arg.startsWith("--project-name=")) {
        projectName = valueOf(arg);
      } else if (AFFIX_OPTION.matcher(arg).matches()) {
        String option = arg.substring(0, arg.indexOf('='));
        affixes.put(option, affixOf(arg, option.endsWith("-prefix")));
      } else if (arg.startsWith("-")) {
        throw new IllegalArgumentException("不明なオプションです: " + arg);
      } else {
        idlFiles.add(Path.of(arg));
      }
    }

    if (!stringCharset.equalsIgnoreCase(NO_CONVERSION) && !isSupportedCharset(stringCharset)) {
      throw new IllegalArgumentException(
          "--string-charset にはJavaの文字コード名か none を指定してください: " + stringCharset);
    }
    if (idlFiles.isEmpty()) {
      throw new IllegalArgumentException("IDLファイルが指定されていません");
    }
    return new CliOptions(
        idlFiles,
        stubRoot,
        outputRoot,
        basePackage,
        idlPackagePrefix,
        stringCharset,
        singleProject,
        projectName,
        new Naming(
            affix(affixes, "dto", Naming.DEFAULT.dto()),
            affix(affixes, "rpc-params", Naming.DEFAULT.rpcParams()),
            affix(affixes, "rpc-request", Naming.DEFAULT.rpcRequest()),
            affix(affixes, "rpc-result", Naming.DEFAULT.rpcResult()),
            affix(affixes, "rpc-response", Naming.DEFAULT.rpcResponse())));
  }

  /// `--name=value`の形の引数から値を取り出す
  ///
  /// @param arg 引数
  /// @return `=`より後ろの文字列
  private static String valueOf(String arg) {
    return arg.substring(arg.indexOf('=') + 1);
  }

  /// クラス名の前後に付ける文字列を取り出す
  ///
  /// @param arg 引数
  /// @param prefix 前に付けるならtrue
  /// @return `=`より後ろの文字列
  private static String affixOf(String arg, boolean prefix) {
    String value = valueOf(arg);
    if (!Naming.isValidAffix(value, prefix)) {
      throw new IllegalArgumentException(
          "%s にはJavaのクラス名に使える文字を指定してください: %s".formatted(arg.substring(0, arg.indexOf('=')), value));
    }
    return value;
  }

  /// 種類ごとに、指定された前後の文字列を既定値と合わせる
  ///
  /// @param affixes オプション名ごとの指定された文字列
  /// @param kind 種類（`dto`・`rpc-params`など）
  /// @param defaults 指定が無いときの文字列
  /// @return 前後の文字列
  private static Naming.Affix affix(
      Map<String, String> affixes, String kind, Naming.Affix defaults) {
    return new Naming.Affix(
        affixes.getOrDefault("--" + kind + "-prefix", defaults.prefix()),
        affixes.getOrDefault("--" + kind + "-suffix", defaults.suffix()));
  }

  /// Javaで使える文字コード名か
  ///
  /// @param name 文字コード名
  /// @return 使えるならtrue
  private static boolean isSupportedCharset(String name) {
    try {
      return Charset.isSupported(name);
    } catch (IllegalCharsetNameException e) {
      return false;
    }
  }

  /// ヘルプの文面を返す
  ///
  /// @return ヘルプの文面
  static String usage() {
    return """
        Usage: [options] <idl-file>...

        Options:
          --stub-root=<path>          IDL生成Javaソースの出力先ディレクトリ(default: generated-stubs)
          --output-root=<path>        Springプロジェクト出力先ディレクトリ(default: generated-spring)
          --base-package=<package>    生成されるSpringプロジェクトのベースパッケージ(default: com.generated)
          --idl-package-prefix=<pkg>  IDLモジュール名に対してidljのパッケージプレフィックスを適用
          --string-charset=<cs>|none  CORBAサーバーが文字列に使う文字コード。noneなら読み替えない
                                      (default: SJIS。生成アプリのcorba.string-charsetで上書きできる)
          --single-project            全サービスを1つのSpringプロジェクトにまとめる
          --project-name=<name>       --single-project 時のプロジェクト名(default: CombinedApi)
          --dto-prefix=<s>            構造体・enumのDTOのクラス名の前に付ける文字列(default: なし)
          --dto-suffix=<s>            構造体・enumのDTOのクラス名の後ろに付ける文字列。空にもできる(default: Dto)
          --rpc-params-prefix=<s>, --rpc-params-suffix=<s>
                                      リクエストのparamsのクラス名の前後(default: なし, RpcParams)
          --rpc-request-prefix=<s>, --rpc-request-suffix=<s>
                                      リクエスト全体のクラス名の前後(default: なし, RpcRequest)
          --rpc-result-prefix=<s>, --rpc-result-suffix=<s>
                                      レスポンスのresultのクラス名の前後(default: なし, RpcResult)
          --rpc-response-prefix=<s>, --rpc-response-suffix=<s>
                                      レスポンス全体のクラス名の前後(default: なし, RpcResponse)
                                      (いずれも空にでき、OpenAPIのスキーマ名になる)
          --help, -h                  このヘルプを表示

        """;
  }
}
