package com.minipo.createsideways.block;

import java.util.EnumSet;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * 縦ハーフブロック(統一理論版)。
 *
 * === 統一理論 ===
 * このブロックの形は「真上から見た4つの角(NW/NE/SW/SE)が、それぞれ埋まっているか」だけで決まる。
 * 通常の縦ハーフ・横向き階段(L字)・1/4柱は、すべてこの4角の組み合わせで表現される。
 *
 *   通常の縦ハーフ = 隣り合う2角   例: 北向き = NW+NE
 *   L字(横向き階段) = 3角
 *   1/4柱           = 1角
 *
 * 各角が埋まる条件は、以下の3ステップだけ:
 *
 *  ステップ1【柱化スイッチ】
 *    FACING 方向の隣に、自分と垂直な向きの縦ハーフがあるか?
 *      ある → ON / ない → OFF
 *
 *  ステップ2【FACING が占有する2角を決める】
 *    スイッチ OFF → 2角とも無条件に埋まる(素の縦ハーフ)
 *    スイッチ ON  → 角ごとに「支え」があるかを見る。支えがあれば埋まり、なければ欠ける。
 *      支えの条件(どちらかを満たせば支えあり):
 *        (a) その角の横隣が、自分と同じ向きの縦ハーフである
 *        (b) FACING 方向の隣(front)が、その角の側へ張り出している
 *            = front の向きが、その角の横方向と一致する
 *
 *  ステップ3【L字化】
 *    FACING の反対側の隣に垂直な向きの縦ハーフがあれば、
 *    (自分の反対方向 × 相手の向き) が交わる角を追加で埋める。
 *
 * この3ステップだけで、これまで確認した全挙動(単体・L字・1/4柱・柱の復帰)が再現される。
 *
 * 形の変化は見た目・当たり判定のみで、アイテムとしての実体は常に縦ハーフのまま。
 */
public class VerticalSlabBlock extends Block implements SimpleWaterloggedBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** 水を含んでいるか(バニラのスラブ・階段と同じ挙動) */
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // 4つの角が埋まっているかを、それぞれ独立したbooleanで保持する(統一理論そのもの)
    public static final BooleanProperty NW = BooleanProperty.create("nw");
    public static final BooleanProperty NE = BooleanProperty.create("ne");
    public static final BooleanProperty SW = BooleanProperty.create("sw");
    public static final BooleanProperty SE = BooleanProperty.create("se");

    /** 真上から見た4隅。 */
    public enum Corner implements StringRepresentable {
        NW("nw", Direction.NORTH, Direction.WEST),
        NE("ne", Direction.NORTH, Direction.EAST),
        SW("sw", Direction.SOUTH, Direction.WEST),
        SE("se", Direction.SOUTH, Direction.EAST);

        private final String name;
        /** この角に接する南北方向 */
        public final Direction ns;
        /** この角に接する東西方向 */
        public final Direction ew;

        Corner(String name, Direction ns, Direction ew) {
            this.name = name;
            this.ns = ns;
            this.ew = ew;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        /** 指定の2方向が交わる角を返す(順不同)。 */
        public static Corner of(Direction a, Direction b) {
            boolean north = (a == Direction.NORTH) || (b == Direction.NORTH);
            boolean east = (a == Direction.EAST) || (b == Direction.EAST);
            if (north && east) return NE;
            if (north) return NW;
            if (east) return SE;
            return SW;
        }

        /**
         * FACING がこの角を占有するか。
         * 例: FACING=NORTH は NW と NE を占有する。
         */
        public boolean isOwnedBy(Direction facing) {
            return this.ns == facing || this.ew == facing;
        }

        /**
         * FACING から見た、この角の「横方向」。
         * FACING が南北なら東西の側、FACING が東西なら南北の側。
         */
        public Direction lateralFor(Direction facing) {
            return facing.getAxis() == Direction.Axis.Z ? this.ew : this.ns;
        }
    }

    // 4隅それぞれの全高ボックス
    private static final VoxelShape BOX_NW = Block.box(0, 0, 0, 8, 16, 8);
    private static final VoxelShape BOX_NE = Block.box(8, 0, 0, 16, 16, 8);
    private static final VoxelShape BOX_SW = Block.box(0, 0, 8, 8, 16, 16);
    private static final VoxelShape BOX_SE = Block.box(8, 0, 8, 16, 16, 16);

    public VerticalSlabBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(NW, true).setValue(NE, true)
                .setValue(SW, false).setValue(SE, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, NW, NE, SW, SE, WATERLOGGED);
    }

    private static BooleanProperty propOf(Corner c) {
        return switch (c) {
            case NW -> VerticalSlabBlock.NW;
            case NE -> VerticalSlabBlock.NE;
            case SW -> VerticalSlabBlock.SW;
            case SE -> VerticalSlabBlock.SE;
        };
    }

    private static VoxelShape boxOf(Corner c) {
        return switch (c) {
            case NW -> BOX_NW;
            case NE -> BOX_NE;
            case SW -> BOX_SW;
            case SE -> BOX_SE;
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Shapes.empty();
        for (Corner c : Corner.values()) {
            if (state.getValue(propOf(c))) {
                shape = Shapes.or(shape, boxOf(c));
            }
        }
        // 念のため: 全角が空になることは無い想定だが、空なら最低限の形を返す
        return shape.isEmpty() ? BOX_NW : shape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    /**
     * 設置時: FACING はプレイヤーが向いている水平方向で決まる。
     *
     * クリックした面には依存しない。これにより:
     *  - 向きを変えずに置き続ければ、縦ハーフの向きが揃う(バラバラにならない)
     *  - フルブロックの壁を向いて置けば、自然と壁に接する側の半分を占有する(壁に張り付く)
     *  - 反対側に回り込んで置けば、占有する半分を反転できる
     *  - 既存の縦ハーフに対して垂直な向きで置けば、統一理論の柱化スイッチが働き1/4柱になる
     */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();

        boolean water = context.getLevel()
                .getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return applyCorners(this.defaultBlockState()
                        .setValue(FACING, facing)
                        .setValue(WATERLOGGED, water),
                context.getLevel(), context.getClickedPos(), facing);
    }

    /**
     * 隣接ブロックが変化したとき: 4角を再計算する。
     */
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        // 水を含んでいる場合は、水流の更新をスケジュールする(バニラのスラブ・階段と同じ)
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return applyCorners(state, level, pos, state.getValue(FACING));
    }

    private static BlockState applyCorners(BlockState state, BlockGetter level, BlockPos pos, Direction facing) {
        EnumSet<Corner> filled = computeCorners(level, pos, facing);
        for (Corner c : Corner.values()) {
            state = state.setValue(propOf(c), filled.contains(c));
        }
        return state;
    }

    /**
     * 統一理論の本体。この位置・この向きの縦ハーフについて、埋まる角の集合を返す。
     */
    private static EnumSet<Corner> computeCorners(BlockGetter level, BlockPos pos, Direction facing) {
        EnumSet<Corner> filled = EnumSet.noneOf(Corner.class);

        // ステップ1: 柱化スイッチ
        Direction front = slabFacingAt(level, pos.relative(facing));
        boolean pillarMode = (front != null && isPerpendicular(front, facing));

        // ステップ2: FACING が占有する2角
        for (Corner c : Corner.values()) {
            if (!c.isOwnedBy(facing)) continue;

            if (!pillarMode) {
                filled.add(c);
                continue;
            }

            Direction lateral = c.lateralFor(facing);
            // 支え(a): 横隣が自分と同じ向き
            if (slabFacingAt(level, pos.relative(lateral)) == facing) {
                filled.add(c);
                continue;
            }
            // 支え(b): FACING方向の隣が、その角の側へ張り出している
            if (front == lateral) {
                filled.add(c);
            }
        }

        // ステップ3: L字化(反対側に垂直な縦ハーフ)
        Direction back = facing.getOpposite();
        Direction backFacing = slabFacingAt(level, pos.relative(back));
        if (backFacing != null && isPerpendicular(backFacing, facing)) {
            filled.add(Corner.of(back, backFacing));
        }

        return filled;
    }

    private static boolean isPerpendicular(Direction a, Direction b) {
        return a.getAxis().isHorizontal() && b.getAxis().isHorizontal() && a.getAxis() != b.getAxis();
    }

    /** 指定位置が縦ハーフなら、その FACING を返す。そうでなければ null。 */
    @Nullable
    private static Direction slabFacingAt(BlockGetter level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        if (s.getBlock() instanceof VerticalSlabBlock) {
            return s.getValue(FACING);
        }
        return null;
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




    /**
     * Mobの経路探索。バニラの階段と同じく、通行可能な床とは見なさない。
     */
    @Override
    protected boolean isPathfindable(BlockState state, net.minecraft.world.level.pathfinder.PathComputationType type) {
        return false;
    }

}
