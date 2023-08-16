package dev.kikugie.techutils.feature.containerscan.scanners;

import com.google.common.collect.Iterables;
import dev.kikugie.techutils.feature.containerscan.LinkedStorageEntry;
import dev.kikugie.techutils.feature.containerscan.PlacementContainerAccess;
import dev.kikugie.techutils.feature.containerscan.handlers.InteractionHandler;
import dev.kikugie.techutils.feature.containerscan.screens.BlockingScreenHandler;
import dev.kikugie.techutils.render.outline.OutlineRenderer;
import dev.kikugie.techutils.util.ContainerUtils;
import dev.kikugie.techutils.util.LocalPlacementPos;
import dev.kikugie.techutils.util.ValidBox;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.schematic.container.LitematicaBlockStateContainer;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.malilib.util.Color4f;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Records contents of nearby containers by opening them manually via the player entity. This method is the most inefficient due to relying on ordered packets and player limitations, but doesn't have any additional requirements.
 */
public class InteractionScanner implements Scanner {
    private final Map<BlockPos, LinkedStorageEntry> cache = new Hashtable<>();
    /* -Util- */
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final ClientPlayerEntity player = Objects.requireNonNull(this.client.player);
    private final ClientWorld world = Objects.requireNonNull(this.client.world);
    private final ClientPlayerInteractionManager interactionManager = Objects.requireNonNull(this.client.interactionManager);
    /* ------ */

    /**
     * Used Litematica placement. If not null, will limit scanned containers only to ones matching in the placement and will record it's items in {@link LinkedStorageEntry}.
     */
    @Nullable
    private final SchematicPlacement placement;
    /**
     * Only used if {@link InteractionScanner#placement} isn't null. Represents all containers to be scanned.
     */
    private final Set<BlockPos> waiting = new HashSet<>();
    private boolean running = true;

    public InteractionScanner() {
        this.placement = null;
    }

    public InteractionScanner(@Nullable SchematicPlacement placement) {
        this.placement = placement;
        initPlacementBlocks();
    }

    /**
     * Called upon modifying the active placement. Resets {@link InteractionScanner#cache}, {@link InteractionScanner#waiting} lists and writes new positions for placement containers.
     * <br>
     * Generally, don't move your placement while its being scanned.
     */
    private void initPlacementBlocks() {
        if (this.placement == null)
            return;
        this.waiting.clear();
        this.cache.clear();
        LitematicaSchematic schematic = this.placement.getSchematic();
        for (String region : schematic.getAreas().keySet()) {
            LitematicaBlockStateContainer container = schematic.getSubRegionContainer(region);
            Map<BlockPos, NbtCompound> blockEntities = schematic.getBlockEntityMapForRegion(region);
            if (blockEntities == null)
                continue;

            assert container != null;
            for (BlockPos pos : blockEntities.keySet()) {
                BlockPos worldPos = LocalPlacementPos.getWorldPos(pos, region, this.placement);

                BlockState worldState = this.world.getBlockState(worldPos);
                BlockState schemState = container.get(pos.getX(), pos.getY(), pos.getZ());
                if (worldState.equals(schemState))
                    this.waiting.add(worldPos);
            }
        }
    }

    /**
     * @return an unordered set of available containers within player's reach.
     */
    private Set<BlockPos> getNearbyContainers() {
        Vec3d camera = getEyesPos(this.player);
        float reach = this.interactionManager.getReachDistance();
        List<BlockPos> positions = this.placement != null ? getPlacementContainers(camera, reach) : getWorldContainers(camera, reach);
        Set<BlockPos> result = new HashSet<>();
        for (BlockPos pos : positions)
            validatePos(pos, camera).ifPresent(result::add);
        return result;
    }

    /**
     * @return All containers in player's reach
     */
    private List<BlockPos> getWorldContainers(Vec3d camera, float reach) {
        BlockPos corner1 = BlockPos.ofFloored(camera.add(reach, reach, reach));
        BlockPos corner2 = BlockPos.ofFloored(camera.subtract(reach, reach, reach));
        return getAvailable(camera, reach, BlockPos.iterate(corner1, corner2));
    }

    /**
     * @return Containers matching entries in {@link InteractionScanner#waiting} list within player's reach
     */
    private List<BlockPos> getPlacementContainers(Vec3d camera, float reach) {
        BlockPos corner1 = BlockPos.ofFloored(camera.add(reach, reach, reach));
        BlockPos corner2 = BlockPos.ofFloored(camera.subtract(reach, reach, reach));
        ValidBox box = new ValidBox(corner1, corner2);
        return getAvailable(camera, reach, Iterables.filter(this.waiting, box::contains));
    }

    private List<BlockPos> getAvailable(Vec3d camera, float reach, Iterable<BlockPos> positions) {
        List<BlockPos> result = new ArrayList<>();
        for (BlockPos pos : positions) {
            if (isInReach(pos, camera, reach))
                result.add(new BlockPos(pos));
        }
        return result;
    }

    /**
     * Checks if player is able to access a container. Conditions can be summarised as:<br>
     * <pre>
     *     - position has a container;
     *     - {@link InteractionScanner#cache} doesn't contain the position;
     *     - player must be able to open the container.</pre>
     *
     * @return {@link BlockPos} if input is valid, {@link Optional#empty()} otherwise
     */
    private Optional<BlockPos> validatePos(BlockPos pos, Vec3d camera) {
        BlockState state = this.world.getBlockState(pos);
        if (state.isAir() || this.cache.containsKey(pos) || ContainerUtils.validateContainer(pos, state).isEmpty())
            return Optional.empty();

        if (state.getBlock() instanceof ChestBlock) {
            if (!this.player.isSpectator() && !ContainerUtils.isChestAccessible(this.world, pos, state))
                return Optional.empty();

            ChestType type = state.get(ChestBlock.CHEST_TYPE);
            if (type == ChestType.SINGLE)
                return Optional.of(pos);

            BlockPos adjacent = pos.add(ChestBlock.getFacing(state).getVector());
            return Optional.of(pos.getSquaredDistance(camera) < adjacent.getSquaredDistance(camera)
                    ? pos : adjacent);
        }
        if (state.getBlock() instanceof ShulkerBoxBlock) {
            return this.player.isSpectator() || ContainerUtils.isShulkerBoxAccessible(this.world, pos, state)
                    ? Optional.of(pos) : Optional.empty();
        }
        return Optional.of(pos);
    }

    private boolean isInReach(BlockPos pos, Vec3d camera, double reach) {
        double dx = camera.getX() - ((double) pos.getX() + 0.5D);
        double dy = camera.getY() - ((double) pos.getY() + 0.5D);
        double dz = camera.getZ() - ((double) pos.getZ() + 0.5D);
        return dx * dx + dy * dy + dz * dz <= reach * reach;
    }

    private Vec3d getEyesPos(PlayerEntity player) {
        return new Vec3d(player.getX(), player.getY() + player.getEyeHeight(player.getPose()), player.getZ());
    }

    /**
     * Registers a position in {@link InteractionHandler} if it's not already in the queue. Registered handler is configured to close the screen as soon as contents packet arrives. After registering the handler it interacts with the block via the player entity.
     *
     * @param pos  position to register
     * @param tick time of register
     * @see BlockingScreenHandler
     */
    private void register(BlockPos pos, long tick) {
        if (InteractionHandler.contains(pos))
            return;

        BlockState state = this.world.getBlockState(pos);
        Optional<Inventory> inventory = ContainerUtils.validateContainer(pos, state);
        if (inventory.isEmpty())
            return;

        SimpleInventory worldInv = new SimpleInventory(inventory.get().size());
        LinkedStorageEntry entry = this.placement == null ? new LinkedStorageEntry(pos, worldInv, null) : PlacementContainerAccess.getEntry(pos, state, worldInv);
        this.cache.put(pos, entry);
        Optional<Color4f> color = entry.validate();
        Color4f red = new Color4f(1, 0, 0, 1);
        color.ifPresent(it -> OutlineRenderer.add(this.world, red.intValue, camera -> pos.getSquaredDistance(camera) <= 32 * 32, pos));
        this.waiting.remove(pos);
        InteractionHandler.add(new InteractionHandler(pos, tick) {
            @Override
            public boolean accept(Screen screen) {
                ClientPlayerEntity player = Objects.requireNonNull(MinecraftClient.getInstance().player);
                player.currentScreenHandler = new BlockingScreenHandler(((ScreenHandlerProvider<?>) screen).getScreenHandler(), worldInv);
                return false;
            }
        });
        interact(pos, getEyesPos(this.player));
    }

    private void interact(BlockPos pos, Vec3d player) {
        Vec3d click = Vec3d.of(pos).add(0.5D, 0.5D, 0.5D);
        BlockHitResult hit = new BlockHitResult(click, Direction.getFacing(click.x - player.x, click.y - player.y, click.z - player.z), pos, false);
        this.interactionManager.interactBlock(this.client.player, Hand.MAIN_HAND, hit);
    }

    @Override
    public void tick() {
        if (!this.running || (this.placement != null && this.waiting.isEmpty()))
            return;

        long tick = this.world.getTime();
        Set<BlockPos> nearby = getNearbyContainers();
        for (BlockPos pos : nearby)
            register(pos, tick);
    }

    @Override
    public void start() {
        this.running = true;
    }

    @Override
    public void stop() {
        this.running = false;
    }

    @Override
    public void update() {
        initPlacementBlocks();
    }
}
