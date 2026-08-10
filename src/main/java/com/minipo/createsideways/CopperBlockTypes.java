package com.minipo.createsideways;

import java.util.List;

import net.minecraft.world.level.block.WeatheringCopper;

/**
 * Create の銅系建築ブロック(屋根板・タイル)16種。
 *
 * ブロックID規則(Createのデータマップで確認済み):
 *   フルブロック : copper_shingles / copper_tiles
 *   スラブ・階段 : copper_shingle_slab / copper_shingle_stairs (複数形の s が落ちる)
 *
 * waxed = true のものは酸化しない(ワックス済み)。
 */
public final class CopperBlockTypes {

    /**
     * @param name    Create のフルブロック名
     * @param state   酸化段階
     * @param waxed   ワックス済みか(trueなら酸化しない)
     */
    public record CopperType(String name, WeatheringCopper.WeatherState state, boolean waxed) {}

    public static final List<CopperType> TYPES = List.of(
            new CopperType("copper_shingles", WeatheringCopper.WeatherState.UNAFFECTED, false),
            new CopperType("copper_tiles", WeatheringCopper.WeatherState.UNAFFECTED, false),
            new CopperType("exposed_copper_shingles", WeatheringCopper.WeatherState.EXPOSED, false),
            new CopperType("exposed_copper_tiles", WeatheringCopper.WeatherState.EXPOSED, false),
            new CopperType("oxidized_copper_shingles", WeatheringCopper.WeatherState.OXIDIZED, false),
            new CopperType("oxidized_copper_tiles", WeatheringCopper.WeatherState.OXIDIZED, false),
            new CopperType("waxed_copper_shingles", WeatheringCopper.WeatherState.UNAFFECTED, true),
            new CopperType("waxed_copper_tiles", WeatheringCopper.WeatherState.UNAFFECTED, true),
            new CopperType("waxed_exposed_copper_shingles", WeatheringCopper.WeatherState.EXPOSED, true),
            new CopperType("waxed_exposed_copper_tiles", WeatheringCopper.WeatherState.EXPOSED, true),
            new CopperType("waxed_oxidized_copper_shingles", WeatheringCopper.WeatherState.OXIDIZED, true),
            new CopperType("waxed_oxidized_copper_tiles", WeatheringCopper.WeatherState.OXIDIZED, true),
            new CopperType("waxed_weathered_copper_shingles", WeatheringCopper.WeatherState.WEATHERED, true),
            new CopperType("waxed_weathered_copper_tiles", WeatheringCopper.WeatherState.WEATHERED, true),
            new CopperType("weathered_copper_shingles", WeatheringCopper.WeatherState.WEATHERED, false),
            new CopperType("weathered_copper_tiles", WeatheringCopper.WeatherState.WEATHERED, false)
    );

    private CopperBlockTypes() {}
}
