package com.minipo.createsideways.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 酸化する、接続機能なしの縦ハーフブロック(銅系)。
 * 酸化・ワックスの対応関係は NeoForge のデータマップで宣言する。
 */
public class WeatheringSimpleVerticalSlabBlock extends SimpleVerticalSlabBlock implements WeatheringCopper {

    private final WeatherState weatherState;

    public WeatheringSimpleVerticalSlabBlock(WeatherState weatherState, Properties properties) {
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
        return WeatheringCopper.getNext(state.getBlock()).isPresent();
    }
}
