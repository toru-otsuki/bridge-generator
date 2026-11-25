package com.toru_otsuki.bridge_generator.gen;

import com.palantir.javapoet.ArrayTypeName;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.TypeName;
import com.toru_otsuki.bridge_generator.model.CorbaType;

/// [CorbaType]ごとに、DTOの型・CORBAの型・相互の変換式を決める
///
/// 式の中のクラスは[ClassName]で埋め込むので、`JavaFile`ではimportした単純名に、`CodeBlock#toString()`では完全修飾名になる。
///
/// @param basePackage 生成プロジェクトのベースパッケージ
/// @param naming DTOの名前の付け方
record TypeMapping(String basePackage, Naming naming) {
  /// `JavaFile`でも`java.lang.`を付けずに出す
  static final ClassName STRING = ClassName.get("", "String");

  private static final ClassName CORBA_ANY = ClassName.get("org.omg.CORBA", "Any");

  /// {@return DTOのパッケージ}
  String dtoPackage() {
    return this.basePackage + ".dto";
  }

  /// {@return Mapperのパッケージ}
  String mapperPackage() {
    return this.basePackage + ".mapper";
  }

  /// {@return 構造体・enumのDTOのクラス}
  ///
  /// @param type 構造体かenum
  ClassName dtoClass(CorbaType.Named type) {
    return this.dtoClass(this.naming.dtoName(type.simpleName()));
  }

  /// {@return DTOのパッケージにあるクラス}
  ///
  /// @param simpleName クラス名
  ClassName dtoClass(String simpleName) {
    return ClassName.get(this.dtoPackage(), simpleName);
  }

  /// {@return Mapperのパッケージにあるクラス}
  ///
  /// @param simpleName クラス名
  ClassName mapperClass(String simpleName) {
    return ClassName.get(this.mapperPackage(), simpleName);
  }

  /// {@return DTOでの型}
  ///
  /// @param type 値の型
  TypeName dtoType(CorbaType type) {
    return switch (type) {
      case CorbaType.Primitive p -> primitive(p.name());
      case CorbaType.Str s -> STRING;
      case CorbaType.AnyType a -> this.dtoClass("AnyValue");
      case CorbaType.Named n -> this.dtoClass(n);
      case CorbaType.ArrayOf a -> ArrayTypeName.of(this.dtoType(a.element()));
    };
  }

  /// {@return スタブでの型}
  ///
  /// @param type 値の型
  TypeName corbaType(CorbaType type) {
    return switch (type) {
      case CorbaType.Primitive p -> primitive(p.name());
      case CorbaType.Str s -> STRING;
      case CorbaType.AnyType a -> CORBA_ANY;
      case CorbaType.Named n -> ClassName.get(n.pkg(), n.simpleName());
      case CorbaType.ArrayOf a -> ArrayTypeName.of(this.corbaType(a.element()));
    };
  }

  /// CORBAの値をDTOの値にする式を返す
  ///
  /// @param type 値の型
  /// @param rhs 変換元の式。複数回評価されることがあるので、副作用のない変数参照を渡す
  /// @return 変換の式
  CodeBlock toDto(CorbaType type, CodeBlock rhs) {
    return this.convert(type, rhs, true, 0);
  }

  /// nullのままCORBAに渡せない型か
  ///
  /// プリミティブは既定値に、Anyは空のAnyにできるので含めない。
  ///
  /// @param type 値の型
  /// @return 渡せないならtrue
  static boolean needsValue(CorbaType type) {
    return type instanceof CorbaType.Str
        || type instanceof CorbaType.Named
        || type instanceof CorbaType.ArrayOf;
  }

  /// DTOの値をCORBAの値にする式を返す
  ///
  /// @param type 値の型
  /// @param rhs 変換元の式。複数回評価されることがあるので、副作用のない変数参照を渡す
  /// @return 変換の式
  CodeBlock fromDto(CorbaType type, CodeBlock rhs) {
    return this.convert(type, rhs, false, 0);
  }

  /// 変換せずにそのまま渡せる型か
  ///
  /// @param type 値の型
  /// @return プリミティブか、その配列ならtrue
  private static boolean isIdentity(CorbaType type) {
    return switch (type) {
      case CorbaType.Primitive p -> true;
      case CorbaType.Str s -> false;
      case CorbaType.AnyType a -> false;
      case CorbaType.Named n -> false;
      case CorbaType.ArrayOf a -> isIdentity(a.element());
    };
  }

  /// 変換の式を組み立てる
  ///
  /// @param type 値の型
  /// @param rhs 変換元の式
  /// @param toDto CORBAからDTOへの変換ならtrue
  /// @param depth 配列の入れ子の深さ。ラムダの変数名が重ならないようにするために使う
  /// @return 変換後の式
  private CodeBlock convert(CorbaType type, CodeBlock rhs, boolean toDto, int depth) {
    if (isIdentity(type)) return rhs;
    if (type instanceof CorbaType.ArrayOf array) {
      CorbaType element = array.element();
      TypeName elementType = toDto ? this.dtoType(element) : this.corbaType(element);
      return CodeBlock.of(
          "$L == null ? null : java.util.Arrays.stream($L).map($L).toArray($T[]::new)",
          rhs,
          rhs,
          this.elementFunction(element, toDto, depth),
          elementType);
    }
    MapperMethod method = this.mapperMethod(type, toDto);
    return CodeBlock.of("$T.$L($L)", method.owner(), method.name(), rhs);
  }

  /// 配列の要素を変換する関数の式を返す
  ///
  /// @param element 要素の型
  /// @param toDto CORBAからDTOへの変換ならtrue
  /// @param depth 配列の入れ子の深さ
  /// @return メソッド参照か、入れ子の配列ならラムダ式
  private CodeBlock elementFunction(CorbaType element, boolean toDto, int depth) {
    if (element instanceof CorbaType.ArrayOf) {
      CodeBlock var = CodeBlock.of("e$L", depth);
      return CodeBlock.of("$L -> $L", var, this.convert(element, var, toDto, depth + 1));
    }
    MapperMethod method = this.mapperMethod(element, toDto);
    return CodeBlock.of("$T::$L", method.owner(), method.name());
  }

  /// 値を変換する静的メソッド
  ///
  /// @param owner メソッドを持つクラス
  /// @param name メソッド名
  private record MapperMethod(ClassName owner, String name) {}

  /// 1つの値を変換するメソッドを返す
  ///
  /// @param type 値の型。プリミティブと配列は対象外
  /// @param toDto CORBAからDTOへの変換ならtrue
  /// @return 変換のメソッド
  private MapperMethod mapperMethod(CorbaType type, boolean toDto) {
    return switch (type) {
      case CorbaType.Str s ->
          new MapperMethod(this.mapperClass("StringMapper"), toDto ? "encode" : "decode");
      case CorbaType.AnyType a ->
          new MapperMethod(this.mapperClass("AnyMapper"), toDto ? "toAnyValue" : "toAny");
      case CorbaType.Named n ->
          new MapperMethod(
              this.mapperClass(n.simpleName() + "Mapper"), toDto ? "toDto" : "fromDto");
      default -> throw new IllegalArgumentException("変換メソッドを持たない型です: " + type);
    };
  }

  /// {@return Javaのプリミティブ型}
  ///
  /// @param name 型名
  private static TypeName primitive(String name) {
    return switch (name) {
      case "int" -> TypeName.INT;
      case "long" -> TypeName.LONG;
      case "short" -> TypeName.SHORT;
      case "byte" -> TypeName.BYTE;
      case "boolean" -> TypeName.BOOLEAN;
      case "float" -> TypeName.FLOAT;
      case "double" -> TypeName.DOUBLE;
      case "char" -> TypeName.CHAR;
      default -> throw new IllegalArgumentException("プリミティブ型ではありません: " + name);
    };
  }
}
