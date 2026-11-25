# README <!-- omit in toc -->

## 目次 <!-- omit in toc -->

- [1. 概要](#1-概要)
- [2. 使い方](#2-使い方)
- [3. 生成したアプリの設定](#3-生成したアプリの設定)
- [4. 生成したAPI](#4-生成したapi)
- [5. 開発](#5-開発)

## 1. 概要

CORBAのIDLから、そのCORBAサービスをJSON-RPC形式のREST APIとして公開するSpring Bootプロジェクトを生成します。生成したアプリは、接続先のCORBAサービスを設定すれば動きます。

## 2. 使い方

JDK 25が必要です。

```powershell
.\gradlew.bat run --console=plain --args="--base-package=com.bridge --idl-package-prefix=com.corba --single-project --project-name=BridgeServer idl/service.idl"
```

| 引数 | 説明 | 既定値 |
|---|---|---|
| `--stub-root` | idljが生成するJavaスタブの出力先 | `generated-stubs` |
| `--output-root` | 生成するプロジェクトの出力先 | `generated-spring` |
| `--base-package` | 生成するコードのベースパッケージ | `com.generated` |
| `--idl-package-prefix` | スタブのパッケージに付ける接頭辞 | なし |
| `--string-charset` | `corba.string-charset`の既定値。`Windows-31J`などJavaの文字コード名か`none` | `SJIS` |
| `--single-project` | 全サービスを1つのプロジェクトにまとめる。無ければサービスごとに`<サービス名>Api`を生成する | |
| `--project-name` | `--single-project`のときのプロジェクト名 | `CombinedApi` |
| `--dto-prefix` / `--dto-suffix` | 構造体・enumのDTOのクラス名の前後に付ける文字列 | なし / `Dto` |
| `--rpc-params-prefix` / `--rpc-params-suffix` | リクエストの`params`のクラス名の前後 | なし / `RpcParams` |
| `--rpc-request-prefix` / `--rpc-request-suffix` | リクエスト全体のクラス名の前後 | なし / `RpcRequest` |
| `--rpc-result-prefix` / `--rpc-result-suffix` | レスポンスの`result`のクラス名の前後 | なし / `RpcResult` |
| `--rpc-response-prefix` / `--rpc-response-suffix` | レスポンス全体のクラス名の前後 | なし / `RpcResponse` |
| `--help` / `-h` | 使い方を表示する | |
| `idl/service.idl` | IDLファイル（複数可） | |

クラス名の前後に付ける文字列は空にもできます。これらのクラス名がOpenAPIのスキーマ名になり、OpenAPI Generatorで作るクライアントの型名になります。

`--idl-package-prefix`が無いときは、moduleはIDLの名前のままのパッケージに、グローバルスコープの型は`generated`パッケージに入ります。

引数に誤りがあると、エラーを表示して終了コード2で終わります。生成するクラスの名前がほかのクラス（`java.lang`のクラスや、別のmoduleにある同じ名前の型など）と重なるときや、IDLのパッケージが生成するパッケージと同じときも、生成せずにエラーにします。

## 3. 生成したアプリの設定

生成したプロジェクトはSpring Boot 4.1・JDK 25向けで、Gradle Wrapperが入っています。`java -jar`で起動することも、WARとして外部のTomcatに置くこともでき、同じTomcatに複数置けます（パラレルデプロイを含む）。

設定は`application.yml`か起動引数（`--corba.ItemService.object-ref=...`など）で渡します。

| 設定 | 内容 | 既定値 |
|---|---|---|
| `corba.<サービス名>.object-ref` | 接続先。IOR（`IOR:...`）、NameServiceの名前（`corbaname::nshost:2809#ItemService`）、`corbaloc:`のURLのどれか | なし（必須） |
| `corba.string-charset` | CORBAサーバーが文字列に使う文字コード。`none`なら読み替えない | 生成時の`--string-charset` |
| `corba.response-timeout` | 応答を待つ最大時間（`40m`、`10s`など） | `30m` |
| `corba.connect-timeout` | 接続できないときに、やり直しを続ける時間の上限 | `60s` |
| `corba.orb.*` | ORBにそのまま渡す設定 | |

CORBAサーバーやNameServiceが止まっていてもアプリは起動し、サーバーが再起動したあとは自動でつなぎ直します。

時間のかかる操作があるときは、`response-timeout`をそれより長くしてください。超えると`COMM_FAILURE`（`COMPLETED_MAYBE`）になり、CORBAサーバーでは処理が続いていることがあります。`connect-timeout`には1回ごとの接続の待ちが含まれないので、実際にはこれより長くかかることがあります。

## 4. 生成したAPI

操作ごとに`POST /操作名`を公開します。

CORBAのシステム例外（通信の失敗やタイムアウトなど）が起きたときは、HTTPステータス200で、`result`の`return`とout引数をnullにし、`result.system_exception`に内容を入れて返します。

```json
{"jsonrpc": "2.0", "id": 1, "result": {"return": null, "system_exception": {"id": "IDL:omg.org/CORBA/COMM_FAILURE:1.0", "minor": 1330446337, "completion_status": "COMPLETED_NO"}}}
```

OpenAPIの定義は3.0で出します。どのAPIからも使われていない型も含めて、IDLの全構造体・全enumを載せるので、Anyの中にしか入らない型もOpenAPI Generatorでモデルにできます。Anyの`value`は型を指定しないスキーマ（任意の値）です。

IDLの`raises`（ユーザー例外）には対応していません。`raises`のあるIDLから生成したプロジェクトはコンパイルできません。

## 5. 開発

JDK 25の入った開発環境は、VS CodeのDev Containers拡張で「Reopen in Container」を選ぶと作れます。

```powershell
.\gradlew.bat test
```

網羅用のIDL（`src/test/resources/idl/coverage.idl`）から生成したプロジェクトを期待値（`src/test/resources/golden/`）と比べ、生成したソースがコンパイルできることも確かめます。生成ロジックを意図して変えたときは`-Dgolden.update=true`で期待値を更新し、差分を確かめてください。
