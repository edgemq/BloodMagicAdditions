package com.edgemq.bmaddon.blockentity;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedInvBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.util.inv.AppEngInternalInventory;
import com.edgemq.bmaddon.BMAddon;
import com.edgemq.bmaddon.ae2.BloodAltarPatternDetails;
import com.edgemq.bmaddon.ae2.BloodMagicPatternKind;
import com.edgemq.bmaddon.config.BMAddonCommonConfig;
import com.edgemq.bmaddon.item.BloodAltarPatternItem;
import com.edgemq.bmaddon.item.BloodMagicSpeedCardItem;
import com.edgemq.bmaddon.menu.BloodAltarAssemblerMenu;
import com.edgemq.bmaddon.registry.BMAddonBlockEntities;
import com.edgemq.bmaddon.registry.BMAddonItems;
import com.edgemq.bmaddon.util.BloodAltarRecipeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BloodAltarAssemblerBlockEntity extends AENetworkedInvBlockEntity implements MenuProvider, IUpgradeableObject, IPowerChannelState, ICraftingProvider, IGridTickable {
    public static final Identifier INV_PATTERNS = Identifier.fromNamespaceAndPath(BMAddon.MODID, "blood_altar_assembler_patterns");

    private static final String TAG_PATTERNS = "Patterns";
    private static final String TAG_UPGRADES = "Upgrades";
    private static final String TAG_ACTIVE_CRAFTS = "ActiveCrafts";

    private static final String TAG_CRAFT_RECIPE_KIND = "RecipeKind";
    private static final String TAG_CRAFT_RECIPE_ID = "RecipeId";
    private static final String TAG_CRAFT_OUTPUT = "Output";
    private static final String TAG_CRAFT_PROGRESS_TICKS = "ProgressTicks";
    private static final String TAG_CRAFT_TIME_TICKS = "CraftTimeTicks";
    private static final String TAG_CRAFT_PENDING_OUTPUT = "PendingOutput";

    public static final int PATTERN_SLOT_COUNT = 9;
    public static final int UPGRADE_SLOT_COUNT = 9;
    public static final int BASE_ALTAR_TIER = 1;
    public static final int MAX_ALTAR_TIER = 4;

    private final AppEngInternalInventory patternInventory = new AppEngInternalInventory(
            this,
            PATTERN_SLOT_COUNT,
            1
    ) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return !stack.isEmpty()
                    && stack.is(BMAddonItems.BLOOD_ALTAR_PATTERN.get())
                    && BloodAltarPatternItem.isEncoded(stack);
        }
    };

    private final IUpgradeInventory upgrades = UpgradeInventories.forMachine(
            BMAddonItems.BLOOD_ALTAR_ASSEMBLER.get(),
            UPGRADE_SLOT_COUNT,
            this::onUpgradesChanged
    );

    private final List<ActiveCraft> activeCrafts = new ArrayList<>();

    private boolean powered;

    public BloodAltarAssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(BMAddonBlockEntities.BLOOD_ALTAR_ASSEMBLER.get(), pos, state);

        this.getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .setIdlePowerUsage(1.0D)
                .setVisualRepresentation(BMAddonItems.BLOOD_ALTAR_ASSEMBLER.get())
                .addService(ICraftingProvider.class, this)
                .addService(IGridTickable.class, this);
    }

    public InternalInventory getPatternInventory() {
        return patternInventory;
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    private void onUpgradesChanged() {
        refreshCraftingProvider();
        alertTickManager();
        saveChanges();
    }

    public int getAltarTier() {
        int tier = BASE_ALTAR_TIER;

        for (ItemStack stack : upgrades) {
            int cardTier = BMAddonItems.getBloodAltarTierFromCard(stack);

            if (cardTier > tier) {
                tier = cardTier;
            }
        }

        return Math.min(MAX_ALTAR_TIER, tier);
    }

    public int getBloodMagicRecipeTierLimit() {
        return getAltarTier();
    }

    public int getAccelerationCardCount() {
        return getVanillaSpeedCardCount()
                + getBloodMagicSpeedCardCount() * BloodMagicSpeedCardItem.SPEED_CARD_EQUIVALENT;
    }

    private int getVanillaSpeedCardCount() {
        int count = 0;

        for (ItemStack stack : upgrades) {
            if (stack.is(AEItems.SPEED_CARD.asItem())) {
                count += stack.getCount();
            }
        }

        return count;
    }

    private int getBloodMagicSpeedCardCount() {
        int count = 0;

        for (ItemStack stack : upgrades) {
            if (BMAddonItems.isBloodMagicSpeedCard(stack)) {
                count += stack.getCount();
            }
        }

        return count;
    }

    public int getParallelCardCount() {
        int count = 0;

        for (ItemStack stack : upgrades) {
            if (BMAddonItems.isBloodAltarParallelCard(stack)) {
                count += stack.getCount();
            }
        }

        return count;
    }

    public int getMaxParallelCrafts() {
        int base = BMAddonCommonConfig.BLOOD_ALTAR_ASSEMBLER_BASE_PARALLEL_CRAFTS.get();
        int perCard = BMAddonCommonConfig.BLOOD_ALTAR_ASSEMBLER_PARALLEL_CRAFTS_PER_CARD.get();
        int cap = BMAddonCommonConfig.BLOOD_ALTAR_ASSEMBLER_MAX_PARALLEL_CRAFTS.get();

        int calculated = base + getParallelCardCount() * perCard;

        return Math.max(1, Math.min(cap, calculated));
    }

    public int getActiveCraftCount() {
        return activeCrafts.size();
    }

    public boolean canCraftRecipeTier(int recipeTier) {
        return recipeTier <= getBloodMagicRecipeTierLimit();
    }

    public int getCraftingProgress() {
        ActiveCraft craft = getFirstWorkingCraft();

        if (craft == null || craft.craftTimeTicks <= 0) {
            return 0;
        }

        return Math.max(0, Math.min(100, (craft.progressTicks * 100) / craft.craftTimeTicks));
    }

    @Nullable
    private ActiveCraft getFirstWorkingCraft() {
        for (ActiveCraft craft : activeCrafts) {
            if (!craft.pendingOutput) {
                return craft;
            }
        }

        return activeCrafts.isEmpty() ? null : activeCrafts.get(0);
    }

    public boolean hasWorkToDo() {
        return !activeCrafts.isEmpty();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.bmaddon.blood_altar_assembler");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BloodAltarAssemblerMenu(containerId, playerInventory, this);
    }

    @Override
    public AECableType getCableConnectionType(Direction direction) {
        return AECableType.COVERED;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return patternInventory;
    }

    @Override
    protected InternalInventory getExposedInventoryForSide(Direction side) {
        return patternInventory;
    }

    @Nullable
    public InternalInventory getSubInventory(Identifier id) {
        if (id.equals(ISegmentedInventory.UPGRADES)) {
            return upgrades;
        }

        if (id.equals(INV_PATTERNS)) {
            return patternInventory;
        }

        return super.getSubInventory(id);
    }

    public void onChangeInventory(InternalInventory inventory, int slot) {
        refreshCraftingProvider();
        alertTickManager();
        saveChanges();
    }

    @Override
    public void saveChanges() {
        setChanged();

        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private void refreshCraftingProvider() {
        if (level == null || level.isClientSide()) {
            return;
        }

        ICraftingProvider.requestUpdate(this.getMainNode());
    }

    private void alertTickManager() {
        if (level == null || level.isClientSide()) {
            return;
        }

        getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        if (level == null || level.isClientSide()) {
            return List.of();
        }

        List<IPatternDetails> availablePatterns = new ArrayList<>();

        for (int slot = 0; slot < patternInventory.size(); slot++) {
            ItemStack patternStack = patternInventory.getStackInSlot(slot);

            if (patternStack.isEmpty()) {
                continue;
            }

            if (!BloodAltarPatternItem.isEncoded(patternStack)) {
                continue;
            }

            BloodAltarPatternDetails.create(level, patternStack)
                    .filter(details -> canCraftRecipeTier(details.getMinimumTier()))
                    .ifPresent(availablePatterns::add);
        }

        return availablePatterns;
    }

    @Override
    public int getPatternPriority() {
        return 0;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (level == null || level.isClientSide()) {
            return false;
        }

        if (isBusy()) {
            return false;
        }

        if (!(patternDetails instanceof BloodAltarPatternDetails bloodMagicPatternDetails)) {
            return false;
        }

        BloodMagicPatternKind kind = bloodMagicPatternDetails.getKind();
        Identifier recipeId = bloodMagicPatternDetails.getRecipeId();

        if (!hasMatchingPatternInInventory(kind, recipeId)) {
            return false;
        }

        if (!BloodAltarRecipeHelper.recipeStillExists(level, kind, recipeId)) {
            return false;
        }

        if (!canCraftRecipeTier(bloodMagicPatternDetails.getMinimumTier())) {
            return false;
        }

        if (!hasProvidedInputs(inputHolder)) {
            return false;
        }

        GenericStack output = bloodMagicPatternDetails.getPrimaryOutput();

        if (output == null || output.amount() <= 0) {
            return false;
        }

        ActiveCraft craft = new ActiveCraft(
                kind,
                recipeId,
                new GenericStack(output.what(), output.amount()),
                0,
                calculateCraftTimeTicks(bloodMagicPatternDetails.getBaseCraftTimeTicks()),
                false
        );

        activeCrafts.add(craft);

        saveChanges();
        alertTickManager();

        return true;
    }

    private boolean hasMatchingPatternInInventory(BloodMagicPatternKind kind, Identifier recipeId) {
        for (int slot = 0; slot < patternInventory.size(); slot++) {
            ItemStack patternStack = patternInventory.getStackInSlot(slot);

            if (patternStack.isEmpty()) {
                continue;
            }

            if (!BloodAltarPatternItem.isEncoded(patternStack)) {
                continue;
            }

            BloodMagicPatternKind storedKind = BloodAltarPatternItem.getRecipeKind(patternStack);
            Identifier storedRecipeId = BloodAltarPatternItem.getRecipeId(patternStack);

            if (storedKind == kind && recipeId.equals(storedRecipeId)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasProvidedInputs(KeyCounter[] inputHolder) {
        if (inputHolder == null || inputHolder.length == 0) {
            return false;
        }

        for (KeyCounter keyCounter : inputHolder) {
            if (keyCounter == null) {
                return false;
            }

            keyCounter.removeZeros();

            if (keyCounter.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isBusy() {
        return activeCrafts.size() >= getMaxParallelCrafts();
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 20, !hasWorkToDo(), 1);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!hasWorkToDo()) {
            return TickRateModulation.SLEEP;
        }

        if (!getMainNode().isActive()) {
            return TickRateModulation.SLOWER;
        }

        int safeTicks = Math.max(1, ticksSinceLastCall);
        boolean changed = false;
        boolean anyCraftProgressed = false;
        boolean anyCraftBlocked = false;

        Iterator<ActiveCraft> iterator = activeCrafts.iterator();

        while (iterator.hasNext()) {
            ActiveCraft craft = iterator.next();

            if (craft.pendingOutput) {
                boolean inserted = tryInsertPendingOutput(craft);

                if (inserted && craft.output == null) {
                    iterator.remove();
                    changed = true;
                    continue;
                }

                if (!inserted) {
                    anyCraftBlocked = true;
                }

                continue;
            }

            if (craft.output == null || craft.output.amount() <= 0) {
                iterator.remove();
                changed = true;
                continue;
            }

            double requiredAe = getAePerTickPerCraft() * safeTicks;

            if (!consumeAeEnergy(requiredAe)) {
                anyCraftBlocked = true;
                continue;
            }

            craft.progressTicks += safeTicks;
            anyCraftProgressed = true;
            changed = true;

            if (craft.progressTicks >= craft.craftTimeTicks) {
                craft.progressTicks = craft.craftTimeTicks;
                craft.pendingOutput = true;
            }
        }

        if (changed) {
            saveChanges();
        }

        if (!hasWorkToDo()) {
            return TickRateModulation.SLEEP;
        }

        if (anyCraftProgressed) {
            return TickRateModulation.FASTER;
        }

        if (anyCraftBlocked) {
            return TickRateModulation.SLOWER;
        }

        return TickRateModulation.IDLE;
    }

    private int calculateCraftTimeTicks(int recipeBaseCraftTimeTicks) {
        int configuredBaseTime = BMAddonCommonConfig.BLOOD_ALTAR_ASSEMBLER_BASE_CRAFT_TIME_TICKS.get();
        int baseTime = recipeBaseCraftTimeTicks > 0 ? recipeBaseCraftTimeTicks : configuredBaseTime;
        int minTime = getBloodMagicSpeedCardCount() > 0
                ? 1
                : BMAddonCommonConfig.BLOOD_ALTAR_ASSEMBLER_MIN_CRAFT_TIME_TICKS.get();
        int speedCards = getAccelerationCardCount();

        int calculated = baseTime / Math.max(1, 1 + speedCards);

        return Math.max(minTime, calculated);
    }

    private double getAePerTickPerCraft() {
        double base = BMAddonCommonConfig.BLOOD_ALTAR_ASSEMBLER_AE_PER_TICK_BASE.get();
        double perAccelerationCard = BMAddonCommonConfig.BLOOD_ALTAR_ASSEMBLER_AE_PER_TICK_PER_ACCELERATION_CARD.get();

        return Math.max(0.0D, base + perAccelerationCard * getAccelerationCardCount());
    }

    private boolean consumeAeEnergy(double amount) {
        if (amount <= 0.0D) {
            return true;
        }

        IGrid grid = getMainNode().getGrid();

        if (grid == null) {
            return false;
        }

        double simulated = grid.getEnergyService().extractAEPower(
                amount,
                Actionable.SIMULATE,
                PowerMultiplier.CONFIG
        );

        if (simulated + 0.0001D < amount) {
            return false;
        }

        double extracted = grid.getEnergyService().extractAEPower(
                amount,
                Actionable.MODULATE,
                PowerMultiplier.CONFIG
        );

        return extracted + 0.0001D >= amount;
    }

    private boolean tryInsertPendingOutput(ActiveCraft craft) {
        if (craft.output == null) {
            return false;
        }

        IGrid grid = getMainNode().getGrid();

        if (grid == null) {
            return false;
        }

        long inserted = grid.getStorageService()
                .getInventory()
                .insert(
                        craft.output.what(),
                        craft.output.amount(),
                        Actionable.MODULATE,
                        IActionSource.empty()
                );

        if (inserted <= 0) {
            return false;
        }

        if (inserted >= craft.output.amount()) {
            craft.output = null;
            return true;
        }

        craft.output = new GenericStack(
                craft.output.what(),
                craft.output.amount() - inserted
        );

        return true;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason == IGridNodeListener.State.GRID_BOOT) {
            return;
        }

        boolean newPowered = false;

        IGrid grid = getMainNode().getGrid();

        if (grid != null) {
            newPowered = getMainNode().isPowered()
                    && grid.getEnergyService().extractAEPower(
                    1.0D,
                    Actionable.SIMULATE,
                    PowerMultiplier.CONFIG
            ) > 0.0001D;
        }

        if (this.powered != newPowered) {
            this.powered = newPowered;
            markForUpdate();
        }

        refreshCraftingProvider();
        alertTickManager();
    }

    @Override
    public boolean isPowered() {
        return powered;
    }

    @Override
    public boolean isActive() {
        return powered;
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        patternInventory.writeToNBT(output, TAG_PATTERNS);
        upgrades.writeToNBT(output, TAG_UPGRADES);

        ValueOutput.ValueOutputList activeCraftList = output.childrenList(TAG_ACTIVE_CRAFTS);

        for (ActiveCraft craft : activeCrafts) {
            ValueOutput craftOutput = activeCraftList.addChild();

            craftOutput.putString(TAG_CRAFT_RECIPE_KIND, craft.kind.getSerializedName());
            craftOutput.putString(TAG_CRAFT_RECIPE_ID, craft.recipeId.toString());

            if (craft.output != null) {
                GenericStack.writeTag(craftOutput.child(TAG_CRAFT_OUTPUT), craft.output);
            }

            craftOutput.putInt(TAG_CRAFT_PROGRESS_TICKS, craft.progressTicks);
            craftOutput.putInt(TAG_CRAFT_TIME_TICKS, craft.craftTimeTicks);
            craftOutput.putBoolean(TAG_CRAFT_PENDING_OUTPUT, craft.pendingOutput);
        }
    }

    @Override
    public void loadTag(ValueInput input) {
        super.loadTag(input);

        patternInventory.readFromNBT(input, TAG_PATTERNS);
        upgrades.readFromNBT(input, TAG_UPGRADES);

        activeCrafts.clear();

        for (ValueInput craftInput : input.childrenListOrEmpty(TAG_ACTIVE_CRAFTS)) {
            BloodMagicPatternKind kind = BloodMagicPatternKind.byName(
                    craftInput.getStringOr(TAG_CRAFT_RECIPE_KIND, "")
            );
            Identifier recipeId = Identifier.tryParse(craftInput.getStringOr(TAG_CRAFT_RECIPE_ID, ""));

            if (recipeId == null) {
                continue;
            }

            GenericStack output = craftInput.child(TAG_CRAFT_OUTPUT)
                    .map(GenericStack::readTag)
                    .orElse(null);

            if (output == null || output.amount() <= 0) {
                continue;
            }

            int progressTicks = Math.max(0, craftInput.getIntOr(TAG_CRAFT_PROGRESS_TICKS, 0));
            int craftTimeTicks = Math.max(1, craftInput.getIntOr(TAG_CRAFT_TIME_TICKS, 1));
            boolean pendingOutput = craftInput.getBooleanOr(TAG_CRAFT_PENDING_OUTPUT, false);

            activeCrafts.add(new ActiveCraft(
                    kind,
                    recipeId,
                    output,
                    progressTicks,
                    craftTimeTicks,
                    pendingOutput
            ));
        }
    }

    @Override
    public void onReady() {
        super.onReady();
        refreshCraftingProvider();
        alertTickManager();
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        /*
         * Не вызываем super.addAdditionalDrops(...), иначе patternInventory может выпасть дважды.
         */

        for (int slot = 0; slot < patternInventory.size(); slot++) {
            ItemStack stack = patternInventory.getStackInSlot(slot);

            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
        }

        for (ItemStack stack : upgrades) {
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
        }

        /*
         * Активные крафты специально не дропаем.
         */
    }

    @Override
    public void clearContent() {
        super.clearContent();

        patternInventory.clear();
        upgrades.clear();
        activeCrafts.clear();
    }

    private static final class ActiveCraft {
        private final BloodMagicPatternKind kind;
        private final Identifier recipeId;

        @Nullable
        private GenericStack output;

        private int progressTicks;
        private final int craftTimeTicks;
        private boolean pendingOutput;

        private ActiveCraft(
                BloodMagicPatternKind kind,
                Identifier recipeId,
                @Nullable GenericStack output,
                int progressTicks,
                int craftTimeTicks,
                boolean pendingOutput
        ) {
            this.kind = kind;
            this.recipeId = recipeId;
            this.output = output;
            this.progressTicks = progressTicks;
            this.craftTimeTicks = Math.max(1, craftTimeTicks);
            this.pendingOutput = pendingOutput;
        }
    }
}
