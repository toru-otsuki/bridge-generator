package com.toru_otsuki.bridge_generator.model;

import java.util.List;

/// IDLの`enum`
///
/// @param pkg Javaパッケージ名
/// @param name Javaのクラス名
/// @param repoId idljが付けたリポジトリID
/// @param idlName IDLのスコープ付きの名前（例: `Coverage.Status`）
/// @param members 選択肢の名前。CORBAでは名前ではなくこの並びの番号で送られる
public record IdlEnum(String pkg, String name, String repoId, String idlName, List<String> members)
    implements TypeDefinition {}
