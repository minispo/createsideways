package com.minipo.createsideways.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 酸化する縦ハーフブロック(銅系)。
 *
 * 酸化の進行先・ワックスの対応は、すべて NeoForge のデータマップで宣言する:
 *   data/neoforge/data_maps/block/oxidizables.json
 *   data/neoforge/data_maps/block/waxables.json
 *
 * これにより以下がすべて自動で機能する(Java側の追加実装は不要):
 *   - ランダムティックによる酸化の進行
 *   - 斧での削り取り(AXE_SCRAPE / WeatheringCopper.getPrevious)
 *   - ハニカムでのワックスがけ、斧でのワックス落とし(AXE_WAX_OFF)
 *
 * このクラスの役割は「酸化しうるブロックである」ことを示し、
 * ランダムティックを酸化処理へ渡すことだけ。
 */
public class WeatheringVerticalSlabBlock extends VerticalSlabBlock implements WeatheringCopper {

    private final WeatherState weatherState;

    public WeatheringVerticalSlabBlock(WeatherState weatherState, Properties properties) {
        super(properties);
        this.weatherState = weatherState;
    }

    @Override
    public WeatherState getAge() {
        return this.weatherState;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        this.changeOverTime(state, level, pos, random);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        // 次の酸化段階が存在する場合だけランダムティックを受ける
        return WeatheringCopper.getNext(state.getBlock()).isPresent();
    }
}
