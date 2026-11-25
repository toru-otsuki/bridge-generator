package com.toru_otsuki.bridge_generator.gen;

import com.toru_otsuki.bridge_generator.model.Param;
import com.toru_otsuki.bridge_generator.model.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

/// ORBを持つ`CorbaOrb`と、サービスごとにスタブを呼び出すCorbaClientを出力する
final class CorbaClientWriter {
  private CorbaClientWriter() {}

  /// アプリで1つのORBを持つ`CorbaOrb`を出力する
  ///
  /// glassfish-corbaのORBはJVMで共有のMBeanサーバーに固定の名前で登録するので、同じTomcatに2つ目のアプリを置くと
  /// 初期化に失敗する。そのため既定ではMBeanを登録しない。
  ///
  /// @param ctx 生成先
  static void writeOrb(GenContext ctx) throws IOException {
    Files.writeString(
        ctx.corbaDir().resolve("CorbaOrb.java"),
        """
        package %1$s.corba;

        import java.time.Duration;
        import java.util.Properties;
        import org.omg.CORBA.ORB;
        import org.springframework.beans.factory.DisposableBean;
        import org.springframework.boot.context.properties.bind.Bindable;
        import org.springframework.boot.context.properties.bind.Binder;
        import org.springframework.core.env.Environment;
        import org.springframework.stereotype.Component;

        @Component
        public class CorbaOrb implements DisposableBean {
          private static final String GMBAL_PROPERTY = "org.glassfish.gmbal.no.multipleUpperBoundsException";
          private static final String REGISTER_MBEANS_PROPERTY = "com.sun.corba.ee.ORBRegisterMBeans";
          private static final String WAIT_FOR_RESPONSE_PROPERTY = "com.sun.corba.ee.transport.ORBWaitForResponseTimeout";
          private static final String CONNECT_TIMEOUTS_PROPERTY = "com.sun.corba.ee.transport.ORBTCPConnectTimeouts";

          private final ORB orb;

          public CorbaOrb(Environment env) {
            if (System.getProperty(GMBAL_PROPERTY) == null) {
              System.setProperty(GMBAL_PROPERTY, "true");
            }
            Properties properties = new Properties();
            properties.setProperty(REGISTER_MBEANS_PROPERTY, "false");
            Binder binder = Binder.get(env);
            binder.bind("corba.response-timeout", Duration.class)
                .ifBound(d -> properties.setProperty(WAIT_FOR_RESPONSE_PROPERTY, millis(d, "corba.response-timeout")));
            binder.bind("corba.connect-timeout", Duration.class)
                .ifBound(d -> properties.setProperty(CONNECT_TIMEOUTS_PROPERTY, "250:" + millis(d, "corba.connect-timeout") + ":100:5000"));
            binder.bind("corba.orb", Bindable.mapOf(String.class, String.class)).ifBound(properties::putAll);
            this.orb = ORB.init(new String[0], properties);
            %1$s.mapper.AnyMapper.useOrb(orb);
          }

          private static String millis(Duration d, String name) {
            long ms = d.toMillis();
            if (ms <= 0 || ms > Integer.MAX_VALUE) {
              throw new IllegalArgumentException(name + " must be between 1 ms and about 24 days: " + d);
            }
            return Long.toString(ms);
          }

          public ORB orb() {
            return orb;
          }

          @Override
          public void destroy() {
            %1$s.mapper.AnyMapper.releaseOrb(orb);
            orb.shutdown(false);
            orb.destroy();
          }
        }
        """
            .formatted(ctx.basePackage()));
  }

  /// サービスのCorbaClientを出力する
  ///
  /// CORBAサーバーやNameServiceが止まっていても起動できるよう、参照は最初の呼び出しで解決する。
  /// 通信に失敗したら参照を捨てて次の呼び出しで解決し直すが、`COMPLETED_MAYBE`で二重に実行しないよう、その呼び出しはやり直さない。
  /// `__stub`は、IDLの識別子から生じるJavaの名前と重ならない。
  ///
  /// @param service サービス
  /// @param clientClassName クラス名（`CorbaClient`か`ItemServiceCorbaClient`）
  /// @param ctx 生成先
  static void write(Service service, String clientClassName, GenContext ctx) throws IOException {
    TypeMapping types = ctx.types();
    String serviceName = service.name();
    String serviceImports =
        service.pkg().isEmpty()
            ? ""
            : """
              import %1$s.%2$s;
              import %1$s.%2$sHelper;
              """
                .formatted(service.pkg(), serviceName);

    // TODO: raises（ユーザー例外）に対応する。スタブが投げるチェック例外を扱っていないので、raisesのあるIDLではコンパイルできない。
    //  エラー時のJSONの形とHTTPステータスを決めてから着手する。
    String methods =
        service.operations().stream()
            .map(
                op -> {
                  String params =
                      op.params().stream()
                          .map(p -> javaType(p, types) + " " + p.name())
                          .collect(Collectors.joining(", "));
                  String args =
                      op.params().stream().map(Param::name).collect(Collectors.joining(", "));
                  String returnType =
                      op.returnType().map(t -> types.corbaType(t).toString()).orElse("void");
                  String call =
                      op.returnType().isPresent()
                          ? "return this.invoke(__stub -> __stub.%s(%s))"
                          : "this.invokeVoid(__stub -> __stub.%s(%s))";
                  return """
                        public %s %s(%s) {
                          %s;
                        }

                      """
                      .formatted(returnType, op.name(), params, call.formatted(op.name(), args));
                })
            .collect(Collectors.joining());

    String source =
        """
        package %1$s.corba;

        import org.springframework.beans.factory.annotation.Value;
        import org.springframework.stereotype.Component;
        %2$s
        @Component
        public class %3$s {
          private final CorbaOrb corbaOrb;
          private final String objectRef;
          private volatile %4$s service;

          public %3$s(CorbaOrb corbaOrb, @Value("${corba.%4$s.object-ref}") String objectRef) {
            if (objectRef == null || objectRef.isBlank()) {
              throw new IllegalStateException("corba.%4$s.object-ref is not set");
            }
            this.corbaOrb = corbaOrb;
            this.objectRef = objectRef.strip();
          }

          private <T> T invoke(java.util.function.Function<%4$s, T> call) {
            %4$s current = this.service;
            if (current == null) {
              current = %4$sHelper.narrow(this.corbaOrb.orb().string_to_object(this.objectRef));
              this.service = current;
            }
            try {
              return call.apply(current);
            } catch (org.omg.CORBA.COMM_FAILURE | org.omg.CORBA.TRANSIENT | org.omg.CORBA.OBJECT_NOT_EXIST e) {
              this.service = null;
              throw e;
            }
          }

          private void invokeVoid(java.util.function.Consumer<%4$s> call) {
            this.invoke(
                __stub -> {
                  call.accept(__stub);
                  return null;
                });
          }

        %5$s}
        """
            .formatted(ctx.basePackage(), serviceImports, clientClassName, serviceName, methods);

    Files.writeString(ctx.corbaDir().resolve(clientClassName + ".java"), source);
  }

  /// {@return スタブに渡す引数の型の完全修飾名。out/inout引数ならHolder}
  ///
  /// @param param 引数
  /// @param types 型の対応付け
  static String javaType(Param param, TypeMapping types) {
    return param.isOut() ? param.holderType() : types.corbaType(param.type()).toString();
  }
}
