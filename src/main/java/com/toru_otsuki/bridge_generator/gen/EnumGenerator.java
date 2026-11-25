package com.toru_otsuki.bridge_generator.gen;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeSpec;
import com.toru_otsuki.bridge_generator.model.IdlEnum;
import java.io.IOException;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;

/// IDLの`enum`ごとに、DTO（Javaのenum）とMapperを生成する
///
/// JSONでは選択肢の名前で表し、CORBAとは番号（DTOの`ordinal()`とidljの`value()`/`from_int`）で変換する。
final class EnumGenerator {
  private EnumGenerator() {}

  /// enum1つ分のDTOとMapperを生成する
  ///
  /// DTOには`enumAsRef`を付ける。付けないとインラインに展開され、[OpenApiSchemasWriter]で載せられない。
  ///
  /// @param idlEnum enum
  /// @param ctx 生成先
  static void generate(IdlEnum idlEnum, GenContext ctx) throws IOException {
    TypeMapping types = ctx.types();
    ClassName dtoClass = types.dtoClass(idlEnum.type());
    ClassName corbaClass = ClassName.get(idlEnum.pkg(), idlEnum.name());

    TypeSpec.Builder dto =
        TypeSpec.enumBuilder(dtoClass)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(
                AnnotationSpec.builder(
                        ClassName.get("io.swagger.v3.oas.annotations.media", "Schema"))
                    .addMember("enumAsRef", "true")
                    .build());
    for (String member : idlEnum.members()) {
      dto.addEnumConstant(constantName(member));
    }
    JavaFile.builder(types.dtoPackage(), dto.build()).build().writeTo(ctx.javaDir());

    TypeSpec mapper =
        TypeSpec.classBuilder(idlEnum.name() + "Mapper")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addMethod(
                MethodSpec.methodBuilder("toDto")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(dtoClass)
                    .addParameter(corbaClass, "src")
                    .addStatement("return src == null ? null : $T.values()[src.value()]", dtoClass)
                    .build())
            .addMethod(
                MethodSpec.methodBuilder("fromDto")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(corbaClass)
                    .addParameter(dtoClass, "src")
                    .addStatement(
                        "return src == null ? null : $T.from_int(src.ordinal())", corbaClass)
                    .build())
            .build();
    JavaFile.builder(types.mapperPackage(), mapper).build().writeTo(ctx.javaDir());
  }

  /// 選択肢の名前をJavaの定数名にする
  ///
  /// Javaの予約語になる名前は、idljと同じく先頭に`_`を付ける。
  ///
  /// @param member IDLの選択肢の名前
  /// @return 定数名
  private static String constantName(String member) {
    return SourceVersion.isKeyword(member) ? "_" + member : member;
  }
}
