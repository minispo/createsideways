package com.minipo.createsideways.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * 横倒しの階段ブロック。上下の区別はなく、FACINGの4方向のみを持つ。
 *
 * 形状は「全高のまま、真上から見るとL字(1つの角だけ欠ける)」。
 * 上下方向の段差を持たないので、普通の階段には見えず「横倒し」に見える。
 * 縦ハーフ(全高の壁)に、全高の1/4柱が水平にくっついた形と同じ。
 *
 * FACING = EAST の場合、真上から見て南東(SE)の角だけが欠ける:
 *     北
 *    ●●
 *    ●○   (○ = 南東、欠け)
 *     南
 *
 * このMODが追加する独立した実ブロック。
 * 縦ハーフブロックが接続時にこの形へ変化する機能でも、ここで定義した形状を共有する予定。
 */
public class SidewaysStairsBlock extends Block implements SimpleWaterloggedBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** 水を含んでいるか(バニラのスラブ・階段と同じ挙動) */
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // 各 FACING に対応する形状を事前計算して保持する
    private static final VoxelShape SHAPE_NORTH;
    private static final VoxelShape SHAPE_SOUTH;
    private static final VoxelShape SHAPE_EAST;
    private static final VoxelShape SHAPE_WEST;

    static {
        // 全て「全高(y:0-16)」で、真上から見たときにL字(1つの角だけ欠ける)になる形。
        // 上下方向の段差を持たないので、普通の階段には見えず「横倒し」になる。
        //
        // 4つの角(全高)を定義しておく (x, z の平面で1/4ずつ):
        //   NW = x[0,8]  z[0,8]      NE = x[8,16] z[0,8]
        //   SW = x[0,8]  z[8,16]     SE = x[8,16] z[8,16]
        VoxelShape nw = Block.box(0, 0, 0, 8, 16, 8);
        VoxelShape ne = Block.box(8, 0, 0, 16, 16, 8);
        VoxelShape sw = Block.box(0, 0, 8, 8, 16, 16);
        VoxelShape se = Block.box(8, 0, 8, 16, 16, 16);

        // FACING = EAST  : 南東(SE)だけ欠ける = NW + NE + SW
        SHAPE_EAST = Shapes.or(nw, ne, sw);
        // FACING = SOUTH : 南西(SW)だけ欠ける = NW + NE + SE
        SHAPE_SOUTH = Shapes.or(nw, ne, se);
        // FACING = WEST  : 北西(NW)だけ欠ける = NE + SW + SE
        SHAPE_WEST = Shapes.or(ne, sw, se);
        // FACING = NORTH : 北東(NE)だけ欠ける = NW + SW + SE
        SHAPE_NORTH = Shapes.or(nw, sw, se);
    }

    public SidewaysStairsBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    /**
     * 設置時の向き決定。
     * クリックした位置(ブロックを真上から見た4分割のどこを指すか)を見て、
     * 「指した角の対角の角が欠ける」ように向きを決める(標準的な階段の置き方に近い)。
     *
     *   カーソル北西(左上) → 南東が欠ける → FACING=EAST
     *   カーソル南西(左下) → 北東が欠ける → FACING=NORTH
     *   カーソル北東(右上) → 南西が欠ける → FACING=SOUTH
     *   カーソル南東(右下) → 北西が欠ける → FACING=WEST
     *
     * クリック面の水平位置(x,z)で判定するので、どの方角から設置しても
     * 「指した角の対角が欠ける」という関係が保たれる。
     */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        double relX = context.getClickLocation().x - pos.getX();
        double relZ = context.getClickLocation().z - pos.getZ();

        boolean west = relX < 0.5;   // 西寄り(左)
        boolean north = relZ < 0.5;  // 北寄り(上)

        Direction facing;
        if (north && west) {          // 北西を指す → 南東が欠ける
            facing = Direction.EAST;
        } else if (!north && west) {  // 南西を指す → 北東が欠ける
            facing = Direction.NORTH;
        } else if (north) {           // 北東を指す → 南西が欠ける
            facing = Direction.SOUTH;
        } else {                      // 南東を指す → 北西が欠ける
            facing = Direction.WEST;
        }

        boolean water = context.getLevel().getFluidState(pos).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(FACING, facing).setValue(WATERLOGGED, water);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // --- 水没(waterlogging) ---
    // バニラのスラブ・階段と同じく、水を含んだ状態を保持できる。

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }




    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }


    /**
     * Mobの経路探索。バニラの階段と同じく、通行可能な床とは見なさない。
     */
    @Override
    protected boolean isPathfindable(BlockState state, net.minecraft.world.level.pathfinder.PathComputationType type) {
        return false;
    }

}
