package com.minipo.createsideways;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

/**
 * クライアント専用の初期化。専用サーバーでは読み込まれない。
 *
 * 現時点でクライアント固有の処理は無いが、
 * 将来クライアント側の登録が必要になった場合の受け皿として残しておく。
 */
@Mod(value = CreateSideways.MODID, dist = Dist.CLIENT)
public class CreateSidewaysClient {

    public CreateSidewaysClient() {
    }
}
