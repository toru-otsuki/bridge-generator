package com.toru_otsuki.bridge_generator.gen;

import com.toru_otsuki.bridge_generator.model.CorbaType;
import com.toru_otsuki.bridge_generator.model.Operation;
import com.toru_otsuki.bridge_generator.model.Service;
import com.toru_otsuki.bridge_generator.model.TypeDefinition;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/// 生成するクラスの名前やパッケージが重ならないかを、生成の前に調べる
///
/// 重なったまま生成すると、ファイルが上書きされたり、生成コードが別のクラスを指してコンパイルできなくなったりする。
public final class NameConflicts {
  /// 生成するクラスを置く、ベースパッケージからの相対パッケージ
  private static final List<String> GENERATED_SUBPACKAGES =
      List.of("", "dto", "mapper", "controller", "corba", "config");

  private NameConflicts() {}

  /// 名前とパッケージの重なりを探す
  ///
  /// @param services サービス
  /// @param settings 共通の設定
  /// @return 利用者に表示する、重なりの説明。無ければ空
  public static List<String> find(List<Service> services, GenSettings settings) {
    List<String> problems = new ArrayList<>();
    checkPackages(services, settings, problems);
    checkDtoNames(services, settings, problems);
    return problems;
  }

  /// IDLの型のパッケージが、生成するクラスのパッケージと同じでないか調べる
  ///
  /// @param services サービス
  /// @param settings 共通の設定
  /// @param problems 見つけた重なりの説明を足す先
  private static void checkPackages(
      List<Service> services, GenSettings settings, List<String> problems) {
    Set<String> generated = new LinkedHashSet<>();
    for (String sub : GENERATED_SUBPACKAGES) {
      generated.add(sub.isEmpty() ? settings.basePackage() : settings.basePackage() + "." + sub);
    }
    Stream.concat(
            services.stream().map(Service::pkg),
            settings.definitions().stream().map(TypeDefinition::pkg))
        .filter(generated::contains)
        .distinct()
        .forEach(
            pkg ->
                problems.add(
                    "IDLから生成したクラスのパッケージ %s が、生成するプロジェクトのパッケージと同じです。--idl-package-prefix か --base-package を変えてください"
                        .formatted(pkg)));
  }

  /// DTOと操作ごとのクラスの名前が、互いに、また固定の名前のクラスやjava.langと重ならないか調べる
  ///
  /// @param services サービス
  /// @param settings 共通の設定
  /// @param problems 見つけた重なりの説明を足す先
  private static void checkDtoNames(
      List<Service> services, GenSettings settings, List<String> problems) {
    Map<String, String> owners = new LinkedHashMap<>();
    owners.put("AnyValue", "anyのDTO");
    owners.put(SystemExceptionWriter.CORBA_TYPES, "CORBAの標準の型");
    owners.put(ServiceDtoWriter.JSON_RPC_TYPES, "JSON-RPCの項目の型");
    checkRpcNames(services, settings.naming(), owners, problems);
    for (CorbaType.Named type :
        settings.definitions().stream().map(TypeDefinition::type).toList()) {
      String name = settings.naming().dtoName(type.simpleName());
      String owner = "型 " + qualified(type);
      String other = owners.putIfAbsent(name, owner);
      if (other != null) {
        problems.add(
            "%sのDTOの名前 %s が、%sのクラスと重なります。--dto-prefix か --dto-suffix を変えてください"
                .formatted(owner, name, other));
      } else if (isJavaLang(name)) {
        problems.add(
            "%sのDTOの名前 %s が、java.langのクラスと重なります。--dto-prefix か --dto-suffix を変えてください"
                .formatted(owner, name));
      }
    }
  }

  /// 操作ごとのクラスの名前の重なりを調べる
  ///
  /// 同じ名前の操作は同じクラスになるので、2つ目以降は調べない。
  ///
  /// @param services サービス
  /// @param naming 名前の付け方
  /// @param owners クラス名ごとの、そのクラスを生成する元の説明。調べた名前を足していく
  /// @param problems 見つけた重なりの説明を足す先
  private static void checkRpcNames(
      List<Service> services, Naming naming, Map<String, String> owners, List<String> problems) {
    Set<String> seen = new HashSet<>();
    for (Service service : services) {
      for (Operation op : service.operations()) {
        if (!seen.add(op.name())) continue;
        String owner = "操作 %s.%s".formatted(service.name(), op.name());
        for (String name : naming.rpcClassNames(op)) {
          String other = owners.putIfAbsent(name, owner);
          if (other != null) {
            problems.add(
                "%sのクラスの名前 %s が、%sのクラスと重なります。--rpc-params-*・--rpc-request-*・--rpc-result-*・--rpc-response-* を変えてください"
                    .formatted(owner, name, other));
          } else if (isJavaLang(name)) {
            problems.add(
                "%sのクラスの名前 %s が、java.langのクラスと重なります。--rpc-params-*・--rpc-request-*・--rpc-result-*・--rpc-response-* を変えてください"
                    .formatted(owner, name));
          }
        }
      }
    }
  }

  /// java.langのクラスと同じ名前か
  ///
  /// 生成コードは`String`などを単純名で書くので、同じ名前のクラスがあると、そちらを指してしまう。
  ///
  /// @param simpleName クラス名
  /// @return 同じならtrue
  private static boolean isJavaLang(String simpleName) {
    try {
      Class.forName("java.lang." + simpleName, false, null);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  private static String qualified(CorbaType.Named type) {
    return type.pkg().isEmpty() ? type.simpleName() : type.pkg() + "." + type.simpleName();
  }
}
