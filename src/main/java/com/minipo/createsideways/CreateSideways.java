package com.minipo.createsideways;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Create: Sideways
 *
 * Create および Create Deco の建材に、縦ハーフ(vertical slab)と
 * 横向き階段(sideways stairs)を追加するアドオン。
 *
 * Create は必須依存、Create Deco は任意依存。
 */
@Mod(CreateSideways.MODID)
public class CreateSideways {

    public static final String MODID = "createsideways";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public CreateSideways(IEventBus modEventBus, ModContainer modContainer) {
        // ブロック・アイテムの登録内容を組み立てる。
        // DeferredRegister を modEventBus に登録する前に呼ぶ必要がある。
        ModBlocks.init();

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
