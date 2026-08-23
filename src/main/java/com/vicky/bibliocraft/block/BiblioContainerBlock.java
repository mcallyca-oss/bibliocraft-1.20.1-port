package com.vicky.bibliocraft.block;

import com.vicky.bibliocraft.blockentity.BiblioContainerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class BiblioContainerBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public BiblioContainerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BiblioContainerBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
            Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof BiblioContainerBlockEntity container) {
                container.serverTick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 net.minecraft.world.entity.player.Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BiblioContainerBlockEntity container)) return InteractionResult.PASS;
        String id = container.legacyId();

        if ("seat".equals(id)) {
            if (!level.isClientSide) sitOnSeat(level, pos, player);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if ("clock".equals(id)) {
            if (!level.isClientSide && player.isShiftKeyDown()) {
                player.displayClientMessage(Component.translatable(
                        "message.bibliocraft.clock_activity", container.clockActivity()), true);
            } else if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, container, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, container, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void sitOnSeat(Level level, BlockPos pos, net.minecraft.world.entity.player.Player player) {
        AABB box = new AABB(pos).inflate(0.75D);
        for (ArmorStand existing : level.getEntitiesOfClass(ArmorStand.class, box,
                a -> a.getPersistentData().getLong("BiblioSeatPos") == pos.asLong())) {
            if (!existing.getPassengers().isEmpty()) return;
            existing.discard();
        }

        ArmorStand carrier = new ArmorStand(level, pos.getX() + 0.5D, pos.getY() + 0.22D, pos.getZ() + 0.5D);
        BlockState seatState = level.getBlockState(pos);
        if (seatState.hasProperty(FACING)) carrier.setYRot(seatState.getValue(FACING).toYRot());
        carrier.setInvisible(true);
        carrier.setInvulnerable(true);
        carrier.setNoGravity(true);
        carrier.setSmall(true);
        carrier.getPersistentData().putLong("BiblioSeatPos", pos.asLong());
        level.addFreshEntity(carrier);
        player.startRiding(carrier, true);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, Direction side) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof BiblioContainerBlockEntity container && container.clockRedstoneActive() ? 15 : 0;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                          BlockState replacement, boolean moved) {
        if (state.getBlock() != replacement.getBlock()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BiblioContainerBlockEntity container) {
                net.minecraft.world.Containers.dropContents(level, pos, container);
                if ("seat".equals(container.legacyId())) {
                    AABB box = new AABB(pos).inflate(0.75D);
                    for (ArmorStand stand : level.getEntitiesOfClass(ArmorStand.class, box,
                            a -> a.getPersistentData().getLong("BiblioSeatPos") == pos.asLong())) {
                        stand.discard();
                    }
                }
                level.removeBlockEntity(pos);
            }
        }
        super.onRemove(state, level, pos, replacement, moved);
    }
}
