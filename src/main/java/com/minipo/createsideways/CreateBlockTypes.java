package com.minipo.createsideways;

import java.util.List;

/**
 * Create 本体の建築ブロック(石系)のうち、縦ハーフ・横向き階段を追加する対象。
 *
 * これらはコネクテッドテクスチャ(CTM)を持たないため、
 * Create Deco のレンガと同じ静的モデル方式でそのまま扱える。
 * (CTMを持つ銅系ブロックは対象外。別途対応が必要)
 */
public final class CreateBlockTypes {

    public static final List<String> TYPES = List.of(
            "cut_granite",
            "polished_cut_granite",
            "cut_granite_bricks",
            "small_granite_bricks",
            "cut_diorite",
            "polished_cut_diorite",
            "cut_diorite_bricks",
            "small_diorite_bricks",
            "cut_andesite",
            "polished_cut_andesite",
            "cut_andesite_bricks",
            "small_andesite_bricks",
            "cut_calcite",
            "polished_cut_calcite",
            "cut_calcite_bricks",
            "small_calcite_bricks",
            "cut_dripstone",
            "polished_cut_dripstone",
            "cut_dripstone_bricks",
            "small_dripstone_bricks",
            "cut_deepslate",
            "polished_cut_deepslate",
            "cut_deepslate_bricks",
            "small_deepslate_bricks",
            "cut_tuff",
            "polished_cut_tuff",
            "cut_tuff_bricks",
            "small_tuff_bricks",
            "cut_asurine",
            "polished_cut_asurine",
            "cut_asurine_bricks",
            "small_asurine_bricks",
            "cut_crimsite",
            "polished_cut_crimsite",
            "cut_crimsite_bricks",
            "small_crimsite_bricks",
            "cut_limestone",
            "polished_cut_limestone",
            "cut_limestone_bricks",
            "small_limestone_bricks",
            "cut_ochrum",
            "polished_cut_ochrum",
            "cut_ochrum_bricks",
            "small_ochrum_bricks",
            "cut_scoria",
            "polished_cut_scoria",
            "cut_scoria_bricks",
            "small_scoria_bricks",
            "cut_scorchia",
            "polished_cut_scorchia",
            "cut_scorchia_bricks",
            "small_scorchia_bricks",
            "cut_veridium",
            "polished_cut_veridium",
            "cut_veridium_bricks",
            "small_veridium_bricks"
    );

    private CreateBlockTypes() {}
}
