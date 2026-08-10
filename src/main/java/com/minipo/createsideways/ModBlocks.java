package com.minipo.createsideways;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.minipo.createsideways.block.SidewaysStairsBlock;
import com.minipo.createsideways.block.VerticalSlabBlock;
import com.minipo.createsideways.block.SimpleVerticalSlabBlock;
import com.minipo.createsideways.block.WeatheringSidewaysStairsBlock;
import com.minipo.createsideways.block.WeatheringSimpleVerticalSlabBlock;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * 縦ハーフ(vertical_slab)と横向き階段(sideways_stairs)の一括登録。
 *
 * Create は必須依存のため、Create 由来のブロックは無条件に登録する。
 * Create Deco は任意依存なので、導入されている場合のみ登録する。
 *
 * 登録しない = レジストリにIDが存在しない、ということなので:
 *   - クリエイティブタブやJEIに出ない
 *   - /give や /setblock でも "unknown item" となり取得できない
 *   - テクスチャ欠けの紫黒ブロックが出てくることがない
 */
public final class ModBlocks {

    public static final String CREATEDECO = "createdeco";

    /** 実際に登録された種類だけが入る。 */
    public static final Map<String, DeferredBlock<Block>> VERTICAL_SLABS = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<BlockItem>> VERTICAL_SLAB_ITEMS = new LinkedHashMap<>();
    public static final Map<String, DeferredBlock<Block>> SIDEWAYS_STAIRS = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<BlockItem>> SIDEWAYS_STAIRS_ITEMS = new LinkedHashMap<>();

    private ModBlocks() {}

    public static boolean isLoaded(String modid) {
        return ModList.get().isLoaded(modid);
    }

    /**
     * DeferredRegister への登録。BLOCKS/ITEMS を modEventBus に登録する前に呼ぶこと。
     */
    public static void init() {
        List<String> types = new ArrayList<>();

        // Create Deco は任意依存
        if (isLoaded(CREATEDECO)) {
            types.addAll(BrickTypes.TYPES);
        } else {
            CreateSideways.LOGGER.info("Create Deco が見つからないため、対応ブロックの登録をスキップします");
        }

        // Create は必須依存なので無条件に登録する
        types.addAll(CreateBlockTypes.TYPES);

        for (String name : types) {
            String slabId = name + "_vertical_slab";
            DeferredBlock<Block> slab = CreateSideways.BLOCKS.register(slabId,
                    () -> new VerticalSlabBlock(props()));
            VERTICAL_SLABS.put(name, slab);
            VERTICAL_SLAB_ITEMS.put(name, CreateSideways.ITEMS.registerSimpleBlockItem(slabId, slab));

            String stairId = name + "_sideways_stairs";
            DeferredBlock<Block> stair = CreateSideways.BLOCKS.register(stairId,
                    () -> new SidewaysStairsBlock(props()));
            SIDEWAYS_STAIRS.put(name, stair);
            SIDEWAYS_STAIRS_ITEMS.put(name, CreateSideways.ITEMS.registerSimpleBlockItem(stairId, stair));
        }

        // --- 銅系(酸化・ワックスあり)。Create必須なので無条件 ---
        for (CopperBlockTypes.CopperType t : CopperBlockTypes.TYPES) {
            registerCopper(t);
        }

        int copper = CopperBlockTypes.TYPES.size();
        CreateSideways.LOGGER.info("登録した種類: {} + 銅 {} ({} ブロック)",
                types.size(), copper, (types.size() + copper) * 2);
    }

    /**
     * 銅系ブロックの登録。
     * ワックス済みは酸化しないので通常のクラス、未ワックスは酸化するクラスを使う。
     */
    private static void registerCopper(CopperBlockTypes.CopperType t) {
        String name = t.name();

        // 銅は接続機能(L字化・1/4柱化)を持たない。
        // 屋根材は方向性の強いテクスチャのため、形を固定して向きごとに焼き付ける。
        String slabId = name + "_vertical_slab";
        DeferredBlock<Block> slab = CreateSideways.BLOCKS.register(slabId,
                () -> t.waxed()
                        ? new SimpleVerticalSlabBlock(copperProps())
                        : new WeatheringSimpleVerticalSlabBlock(t.state(), copperProps()));
        VERTICAL_SLABS.put(name, slab);
        VERTICAL_SLAB_ITEMS.put(name, CreateSideways.ITEMS.registerSimpleBlockItem(slabId, slab));

        String stairId = name + "_sideways_stairs";
        DeferredBlock<Block> stair = CreateSideways.BLOCKS.register(stairId,
                () -> t.waxed()
                        ? new SidewaysStairsBlock(copperProps())
                        : new WeatheringSidewaysStairsBlock(t.state(), copperProps()));
        SIDEWAYS_STAIRS.put(name, stair);
        SIDEWAYS_STAIRS_ITEMS.put(name, CreateSideways.ITEMS.registerSimpleBlockItem(stairId, stair));
    }

    /** 銅ブロック相当の性質(音・硬さ)。 */
    private static BlockBehaviour.Properties copperProps() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).noOcclusion().randomTicks();
    }

    private static BlockBehaviour.Properties props() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).noOcclusion();
    }
}
