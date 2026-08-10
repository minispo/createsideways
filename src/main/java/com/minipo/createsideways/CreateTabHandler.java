package com.minipo.createsideways;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/**
 * Create の「Palettes」タブに、自MODの縦ハーフ・横向き階段を挿入する。
 *
 * 挿入位置:
 *   縦ハーフ   … 対応する <name>_slab   の直後
 *   横向き階段 … 対応する <name>_stairs の直後
 *
 * 注意: Create 側のタブID・スラブ/階段のブロックIDは、実機で確認して
 *       スラブ/階段のブロックIDは <name>_slab / <name>_stairs で統一されている。
 */
@EventBusSubscriber(modid = CreateSideways.MODID)
public class CreateTabHandler {

    private static final String CREATE = "create";

    /**
     * Create の建築ブロック(パレット)タブ。
     *
     * AllCreativeModeTabs を解析して確認済み:
     *   REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "create")
     *   BASE_CREATIVE_TAB     = REGISTER.register("base", ...)
     *   PALETTES_CREATIVE_TAB = REGISTER.register("palettes", ...)
     * Create のタブは base と palettes の2つのみ。
     * cut_* などの石系建築ブロックは AllPaletteBlocks に属するため palettes 側。
     */
    private static final ResourceLocation PALETTES_TAB =
            ResourceLocation.fromNamespaceAndPath(CREATE, "palettes");

    @SubscribeEvent
    public static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().location().equals(PALETTES_TAB)) {
            return;
        }

        // --- 銅系(屋根板・タイル) ---
        for (CopperBlockTypes.CopperType t : CopperBlockTypes.TYPES) {
            String name = t.name();
            var slabH = ModBlocks.VERTICAL_SLAB_ITEMS.get(name);
            var stairH = ModBlocks.SIDEWAYS_STAIRS_ITEMS.get(name);
            if (slabH == null || stairH == null) {
                continue;
            }
            // copper_shingles -> copper_shingle_slab / copper_shingle_stairs (末尾の s が落ちる)
            String cbase = name.substring(0, name.length() - 1);
            insertAfter(event, cbase + "_slab", slabH.get());
            insertAfter(event, cbase + "_stairs", stairH.get());
        }

        List<String> types = CreateBlockTypes.TYPES;
        for (String name : types) {
            String base = baseNameFor(name);

            var slabHolder = ModBlocks.VERTICAL_SLAB_ITEMS.get(name);
            var stairHolder = ModBlocks.SIDEWAYS_STAIRS_ITEMS.get(name);
            if (slabHolder == null || stairHolder == null) {
                continue; // Create 未導入などで未登録の場合
            }

            insertAfter(event, base + "_slab", slabHolder.get());
            insertAfter(event, base + "_stairs", stairHolder.get());
        }
    }

    /**
     * Create のスラブ/階段のブロックIDを組み立てるための基準名を返す。
     *
     * Create は "..._bricks" のフルブロックに対して、スラブ/階段では複数形の s を落とす。
     *   cut_granite_bricks   -> cut_granite_brick_slab / cut_granite_brick_stairs
     *   small_granite_bricks -> small_granite_brick_slab / small_granite_brick_stairs
     * それ以外(cut_/polished_ 系)はフルブロック名がそのまま基準になる。
     *   cut_granite          -> cut_granite_slab / cut_granite_stairs
     */
    private static String baseNameFor(String fullBlockName) {
        if (fullBlockName.endsWith("_bricks")) {
            return fullBlockName.substring(0, fullBlockName.length() - 1);
        }
        return fullBlockName;
    }

    /**
     * create 名前空間の指定アイテムの直後に、自MODのアイテムを挿入する。
     * 基準が存在しない/そのタブに無い場合は何もしない。
     */
    private static void insertAfter(BuildCreativeModeTabContentsEvent event, String createPath, Item newItem) {
        Item baseItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(CREATE, createPath));
        if (baseItem == Items.AIR) {
            return;
        }

        ItemStack baseStack = new ItemStack(baseItem);
        if (!event.getParentEntries().contains(baseStack) || !event.getSearchEntries().contains(baseStack)) {
            return;
        }

        event.insertAfter(baseStack, new ItemStack(newItem),
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }
}
