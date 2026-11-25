package com.toru_otsuki.bridge_generator.idl;

import com.sun.tools.corba.ee.idl.AttributeEntry;
import com.sun.tools.corba.ee.idl.EnumEntry;
import com.sun.tools.corba.ee.idl.ExceptionEntry;
import com.sun.tools.corba.ee.idl.InterfaceEntry;
import com.sun.tools.corba.ee.idl.MethodEntry;
import com.sun.tools.corba.ee.idl.ModuleEntry;
import com.sun.tools.corba.ee.idl.ParameterEntry;
import com.sun.tools.corba.ee.idl.PrimitiveEntry;
import com.sun.tools.corba.ee.idl.SequenceEntry;
import com.sun.tools.corba.ee.idl.StringEntry;
import com.sun.tools.corba.ee.idl.StructEntry;
import com.sun.tools.corba.ee.idl.SymtabEntry;
import com.sun.tools.corba.ee.idl.TypedefEntry;
import com.sun.tools.corba.ee.idl.toJavaPortable.Util;
import com.toru_otsuki.bridge_generator.model.CorbaType;
import com.toru_otsuki.bridge_generator.model.Field;
import com.toru_otsuki.bridge_generator.model.IdlEnum;
import com.toru_otsuki.bridge_generator.model.IdlModel;
import com.toru_otsuki.bridge_generator.model.Operation;
import com.toru_otsuki.bridge_generator.model.Param;
import com.toru_otsuki.bridge_generator.model.Service;
import com.toru_otsuki.bridge_generator.model.Struct;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/// idljの記号表からモデルを作る
///
/// Javaの名前はidljの規則（[Util#javaQualifiedName]・[Util#holderName]）で求め、スタブの名前と一致させる。
final class ModelBuilder {
  /// Javaのプリミティブ型になる、idljの基本型のJavaでの名前
  private static final Set<String> PRIMITIVES =
      Set.of("int", "long", "short", "byte", "boolean", "float", "double", "char");

  private ModelBuilder() {}

  /// パッケージを割り当てた後の記号表から、module・interfaceの中も含めて集める
  ///
  /// @param topLevel トップレベルの定義
  /// @param idlNames [#idlNames]の結果
  /// @return モデル
  static IdlModel build(List<SymtabEntry> topLevel, Map<SymtabEntry, String> idlNames) {
    List<Service> services = new ArrayList<>();
    List<Struct> structs = new ArrayList<>();
    List<IdlEnum> enums = new ArrayList<>();
    for (SymtabEntry entry : allEntries(topLevel)) {
      if (entry instanceof InterfaceEntry iface) {
        services.add(service(iface));
      } else if (entry instanceof StructEntry struct) {
        structs.add(struct(struct, idlNames.get(struct)));
      } else if (entry instanceof EnumEntry enumEntry) {
        String javaName = Util.javaQualifiedName(enumEntry);
        enums.add(
            new IdlEnum(
                packageName(javaName),
                simpleName(javaName),
                enumEntry.repositoryID().ID(),
                idlNames.get(enumEntry),
                List.copyOf(enumEntry.elements())));
      }
    }
    return IdlModel.merge(List.of(new IdlModel(services, structs, enums)));
  }

  /// 各定義のIDLのスコープ付きの名前（例: `Coverage.Inner.Point`）を求める
  ///
  /// パッケージを割り当てると記号表の親の連なりが変わるので、その前に呼ぶ。
  ///
  /// @param topLevel トップレベルの定義
  /// @return 定義ごとの名前
  static Map<SymtabEntry, String> idlNames(List<SymtabEntry> topLevel) {
    Map<SymtabEntry, String> names = new IdentityHashMap<>();
    for (SymtabEntry entry : allEntries(topLevel)) {
      Deque<String> parts = new ArrayDeque<>();
      for (SymtabEntry e = entry; e != null && !e.name().isEmpty(); e = e.container()) {
        parts.push(e.name());
      }
      names.put(entry, String.join(".", parts));
    }
    return names;
  }

  /// module・interfaceの中も含めて、すべての定義を宣言順に並べる
  ///
  /// @param topLevel トップレベルの定義
  /// @return すべての定義
  private static List<SymtabEntry> allEntries(List<SymtabEntry> topLevel) {
    List<SymtabEntry> all = new ArrayList<>();
    Deque<SymtabEntry> pending = new ArrayDeque<>(topLevel);
    while (!pending.isEmpty()) {
      SymtabEntry entry = pending.pop();
      all.add(entry);
      if (entry instanceof ModuleEntry module) {
        module.contained().reversed().forEach(pending::push);
      } else if (entry instanceof InterfaceEntry iface) {
        iface.contained().reversed().forEach(pending::push);
      }
    }
    return all;
  }

  /// interfaceからサービスを作る
  ///
  /// @param iface interface
  /// @return サービス
  private static Service service(InterfaceEntry iface) {
    String javaName = Util.javaQualifiedName(iface);
    return new Service(simpleName(javaName), packageName(javaName), operations(iface.methods()));
  }

  /// 操作のモデルを作る
  ///
  /// 属性（`attribute`）は対象にしない。
  ///
  /// @param methods 操作と属性
  /// @return 操作
  private static List<Operation> operations(List<MethodEntry> methods) {
    List<Operation> operations = new ArrayList<>();
    for (MethodEntry method : methods) {
      if (method instanceof AttributeEntry) continue;
      Optional<CorbaType> returnType =
          method.type() == null ? Optional.empty() : Optional.of(resolve(method.type()));
      List<CorbaType.Named> raises =
          method.exceptions().stream().map(e -> (CorbaType.Named) resolve(e)).toList();
      operations.add(new Operation(method.name(), returnType, params(method), raises));
    }
    return operations;
  }

  /// 操作の引数を作る
  ///
  /// @param method 操作
  /// @return 引数。out/inout引数にはHolderの型を持たせる
  private static List<Param> params(MethodEntry method) {
    List<Param> params = new ArrayList<>();
    for (ParameterEntry p : method.parameters()) {
      CorbaType type = resolve(p.type());
      Param.Direction direction =
          switch (p.passType()) {
            case ParameterEntry.In -> Param.Direction.IN;
            case ParameterEntry.Out -> Param.Direction.OUT;
            default -> Param.Direction.INOUT;
          };
      String holder = direction == Param.Direction.IN ? null : Util.holderName(p.type());
      params.add(new Param(p.name(), type, holder, direction));
    }
    return params;
  }

  /// 構造体・exceptionのモデルを作る
  ///
  /// @param struct 構造体かexception
  /// @param idlName IDLのスコープ付きの名前
  /// @return 構造体
  private static Struct struct(StructEntry struct, String idlName) {
    String javaName = Util.javaQualifiedName(struct);
    List<Field> fields =
        struct.members().stream().map(m -> new Field(m.name(), resolve(m))).toList();
    return new Struct(
        packageName(javaName),
        simpleName(javaName),
        struct.repositoryID().ID(),
        idlName,
        fields,
        struct instanceof ExceptionEntry);
  }

  /// 記号表の型を型モデルにする
  ///
  /// typedefは元の型までたどり、配列の宣言（`long a[3]`）は配列にする。
  ///
  /// @param type 記号表の型
  /// @return 型モデル
  static CorbaType resolve(SymtabEntry type) {
    if (type instanceof TypedefEntry typedef) {
      CorbaType resolved = resolve(typedef.type());
      for (int i = 0; i < typedef.arrayInfo().size(); i++) {
        resolved = new CorbaType.ArrayOf(resolved);
      }
      return resolved;
    }
    if (type instanceof SequenceEntry sequence) {
      return new CorbaType.ArrayOf(resolve(sequence.type()));
    }
    if (type instanceof StringEntry) {
      return CorbaType.STRING;
    }
    String javaName = Util.javaQualifiedName(type);
    if (type instanceof PrimitiveEntry) {
      if (javaName.equals("org.omg.CORBA.Any")) return CorbaType.ANY;
      if (PRIMITIVES.contains(javaName)) return new CorbaType.Primitive(javaName);
    }
    return new CorbaType.Named(packageName(javaName), simpleName(javaName));
  }

  private static String packageName(String javaName) {
    int dot = javaName.lastIndexOf('.');
    return dot < 0 ? "" : javaName.substring(0, dot);
  }

  private static String simpleName(String javaName) {
    return javaName.substring(javaName.lastIndexOf('.') + 1);
  }
}
