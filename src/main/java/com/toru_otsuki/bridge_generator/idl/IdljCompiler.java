package com.toru_otsuki.bridge_generator.idl;

import com.sun.tools.corba.ee.idl.EnumEntry;
import com.sun.tools.corba.ee.idl.ForwardEntry;
import com.sun.tools.corba.ee.idl.InterfaceEntry;
import com.sun.tools.corba.ee.idl.ModuleEntry;
import com.sun.tools.corba.ee.idl.StructEntry;
import com.sun.tools.corba.ee.idl.SymtabEntry;
import com.sun.tools.corba.ee.idl.TypedefEntry;
import com.sun.tools.corba.ee.idl.UnionEntry;
import com.sun.tools.corba.ee.idl.toJavaPortable.Arguments;
import com.sun.tools.corba.ee.idl.toJavaPortable.Compile;
import com.toru_otsuki.bridge_generator.GenerationException;
import com.toru_otsuki.bridge_generator.model.IdlModel;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/// idljを同じプロセスで動かし、スタブを生成した記号表からモデルも作る
///
/// スタブとモデルで名前が食い違わないよう、idljの構文解析と名前の対応規則をそのまま使う。idljの内部APIに触るのは、このクラスと[ModelBuilder]だけにする。
///
/// [#preEmit]の上書きは、idljが想定している拡張方法ではない。idljの版を上げるときは、`start`が`preParse`・`parse`・
/// `preEmit`・`generate`の順に呼ぶことと、`preEmit`でパッケージを割り当てていることを確かめる。
public final class IdljCompiler extends Compile {
  /// `--idl-package-prefix`が無いときに、グローバルスコープの型を入れるパッケージ
  static final String DEFAULT_GLOBAL_PACKAGE = "generated";

  /// `--idl-package-prefix`の値
  private final String packagePrefix;

  /// 構文解析に失敗したらnull
  private List<SymtabEntry> topLevel;

  /// パッケージを割り当てる前に控えた、定義ごとのIDLの名前
  private Map<SymtabEntry, String> idlNames;

  private IdljCompiler(String packagePrefix) {
    this.packagePrefix = packagePrefix;
    // idljは処理中の自分自身をこの静的フィールドから参照する
    Compile.compiler = this;
  }

  /// IDLファイルからスタブを生成し、モデルを作る
  ///
  /// @param idlFile IDLファイル。includeしたIDLの型も含めて処理する
  /// @param stubRoot スタブの出力先
  /// @param packagePrefix moduleとグローバルスコープの型に付けるパッケージ。空なら付けない
  /// @return モデル
  public static IdlModel compile(Path idlFile, Path stubRoot, String packagePrefix)
      throws GenerationException {
    Path idl = idlFile.toAbsolutePath();
    String[] args = {
      "-emitAll",
      "-fclient",
      "-i",
      idl.getParent().toString(),
      "-td",
      stubRoot.toAbsolutePath().toString(),
      idl.toString()
    };

    // 日本語ロケールだと、idljがスタブのヘッダーに日時を書くたびに非推奨のタイムゾーンID「JST」の警告が出る
    IdljCompiler compiler = new IdljCompiler(packagePrefix);
    Locale saved = Locale.getDefault();
    Locale.setDefault(Locale.ENGLISH);
    try {
      compiler.start(args);
    } finally {
      Locale.setDefault(saved);
    }

    if (compiler.topLevel == null) {
      throw new GenerationException("idljがIDLを解析できませんでした: " + idlFile);
    }
    return ModelBuilder.build(compiler.topLevel, compiler.idlNames);
  }

  /// idljがパッケージを割り当てる前に、moduleとグローバルスコープの型を`-pkgPrefix`の対象に足す
  ///
  /// グローバルスコープの型は、無名パッケージのままだとimportできないので、必ずパッケージに入れる。
  @Override
  protected void preEmit(Enumeration<SymtabEntry> entries) {
    this.topLevel = Collections.list(entries);
    // 割り当て後は記号表の親の連なりが変わるので、IDLの名前を先に控える
    this.idlNames = ModelBuilder.idlNames(this.topLevel);
    Arguments args = (Arguments) arguments;
    for (SymtabEntry entry : this.topLevel) {
      if (entry instanceof ModuleEntry) {
        if (!this.packagePrefix.isEmpty()) args.packages.put(entry.name(), this.packagePrefix);
      } else if (isNamedType(entry)) {
        args.packages.put(
            entry.name(),
            this.packagePrefix.isEmpty() ? DEFAULT_GLOBAL_PACKAGE : this.packagePrefix);
      }
    }
    super.preEmit(Collections.enumeration(this.topLevel));
  }

  /// `-pkgPrefix`でパッケージを割り当てられる、トップレベルの型定義か
  ///
  /// @param entry 定義
  /// @return 型定義ならtrue
  private static boolean isNamedType(SymtabEntry entry) {
    return entry instanceof TypedefEntry
        || entry instanceof StructEntry
        || entry instanceof EnumEntry
        || entry instanceof UnionEntry
        || entry instanceof InterfaceEntry
        || entry instanceof ForwardEntry;
  }
}
