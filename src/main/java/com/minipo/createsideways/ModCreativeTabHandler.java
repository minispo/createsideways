package com.minipo.createsideways;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
 * Create Decoの「Bricks」タブ (createdeco:bricks_tab) に、自MODのブロックを挿入する。
 *
 * 挿入位置:
 *   縦ハーフ      … その色の mossy_<色>_brick_slab   の直後から、種類順に並べる
 *   横向き階段    … その色の mossy_<色>_brick_stairs の直後から、種類順に並べる
 *
 * 種類の並び順(フルブロックと同じ):
 *   brick(無印), short, tiled, long, corner, cracked, mossy
 *
 * 注意: insertAfter は「基準の直後」に挿すため、同じ基準に対して続けて挿すと
 * 後から挿したものほど基準の近く(左)に来る。よって希望の並びにするには
 * リストを逆順に挿入する。
 */
@EventBusSubscriber(modid = CreateSideways.MODID)
public class ModCreativeTabHandler {

    private static final String CD = "createdeco";
    private static final ResourceLocation BRICKS_TAB =
            ResourceLocation.fromNamespaceAndPath(CD, "bricks_tab");

    /** 色 -> その色のレンガ種類(表示したい並び順)。red には無印が存在しない。 */
    private static final Map<String, List<String>> ORDER = new LinkedHashMap<>();
    static {
        ORDER.put("blue", List.of("blue_bricks", "short_blue_bricks", "tiled_blue_bricks", "long_blue_bricks", "corner_blue_bricks", "cracked_blue_bricks", "mossy_blue_bricks"));
        ORDER.put("dean", List.of("dean_bricks", "short_dean_bricks", "tiled_dean_bricks", "long_dean_bricks", "corner_dean_bricks", "cracked_dean_bricks", "mossy_dean_bricks"));
        ORDER.put("dusk", List.of("dusk_bricks", "short_dusk_bricks", "tiled_dusk_bricks", "long_dusk_bricks", "corner_dusk_bricks", "cracked_dusk_bricks", "mossy_dusk_bricks"));
        ORDER.put("pearl", List.of("pearl_bricks", "short_pearl_bricks", "tiled_pearl_bricks", "long_pearl_bricks", "corner_pearl_bricks", "cracked_pearl_bricks", "mossy_pearl_bricks"));
        ORDER.put("red", List.of("short_red_bricks", "tiled_red_bricks", "long_red_bricks", "corner_red_bricks", "cracked_red_bricks", "mossy_red_bricks"));
        ORDER.put("scarlet", List.of("scarlet_bricks", "short_scarlet_bricks", "tiled_scarlet_bricks", "long_scarlet_bricks", "corner_scarlet_bricks", "cracked_scarlet_bricks", "mossy_scarlet_bricks"));
        ORDER.put("umber", List.of("umber_bricks", "short_umber_bricks", "tiled_umber_bricks", "long_umber_bricks", "corner_umber_bricks", "cracked_umber_bricks", "mossy_umber_bricks"));
        ORDER.put("verdant", List.of("verdant_bricks", "short_verdant_bricks", "tiled_verdant_bricks", "long_verdant_bricks", "corner_verdant_bricks", "cracked_verdant_bricks", "mossy_verdant_bricks"));
    }

    @SubscribeEvent
    public static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().location().equals(BRICKS_TAB)) {
            return;
        }

        for (Map.Entry<String, List<String>> e : ORDER.entrySet()) {
            String color = e.getKey();
            List<String> types = e.getValue();

            // 逆順に挿すことで、基準の右側が希望の並び順になる
            for (int i = types.size() - 1; i >= 0; i--) {
                String name = types.get(i);

                var slabHolder = ModBlocks.VERTICAL_SLAB_ITEMS.get(name);
                var stairHolder = ModBlocks.SIDEWAYS_STAIRS_ITEMS.get(name);
                if (slabHolder == null || stairHolder == null) {
                    continue; // Create Deco 未導入などで未登録の場合
                }

                insertAfter(event, "mossy_" + color + "_brick_slab", slabHolder.get());
                insertAfter(event, "mossy_" + color + "_brick_stairs", stairHolder.get());
            }
        }
    }

    /**
     * createdeco の指定アイテムの直後に、自MODのアイテムを挿入する。
     * 基準が存在しない/そのタブに無い場合は何もしない(insertAfterは例外を投げるため)。
     */
    private static void insertAfter(BuildCreativeModeTabContentsEvent event, String createdecoPath, Item newItem) {
        Item baseItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(CD, createdecoPath));
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
