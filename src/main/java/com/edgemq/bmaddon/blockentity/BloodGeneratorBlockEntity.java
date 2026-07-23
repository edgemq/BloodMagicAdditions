package com.edgemq.bmaddon.blockentity;

import com.edgemq.bmaddon.config.BMAddonCommonConfig;
import com.edgemq.bmaddon.menu.BloodGeneratorMenu;
import com.edgemq.bmaddon.registry.BMAddonBlockEntities;
import com.edgemq.bmaddon.util.BloodMagicFluidHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;

public class BloodGeneratorBlockEntity extends BlockEntity implements MenuProvider {
    private static final String TAG_ENERGY = "Energy";
    private static final String TAG_BLOOD_TANK = "BloodTank";

    public static final int DATA_ENERGY_STORED = 0;
    public static final int DATA_MAX_ENERGY_STORED = 1;
    public static final int DATA_BLOOD_AMOUNT = 2;
    public static final int DATA_BLOOD_CAPACITY = 3;
    public static final int DATA_COUNT = 4;

    private final GeneratorEnergyStorage energyStorage = new GeneratorEnergyStorage();
    private final FluidTransactionJournal fluidTransactionJournal = new FluidTransactionJournal();
    private final ResourceHandler<FluidResource> transferFluidHandler = new OutputOnlyTransferFluidHandler();

    private final FluidTank bloodTank = new FluidTank(
            BMAddonCommonConfig.BLOOD_GENERATOR_LIFE_TANK_CAPACITY.get()
    ) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return BloodMagicFluidHelper.isLifeEssence(stack);
        }

        @Override
        protected void onContentsChanged() {
            BloodGeneratorBlockEntity.this.setChangedAndSync();
        }
    };

    private final IFluidHandler externalFluidHandler = new OutputOnlyFluidHandler();

    public BloodGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(BMAddonBlockEntities.BLOOD_GENERATOR.get(), pos, state);
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            BloodGeneratorBlockEntity blockEntity
    ) {
        blockEntity.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide()) {
            return;
        }

        updateTankCapacityFromConfig();

        int interval = Math.max(1, BMAddonCommonConfig.BLOOD_GENERATOR_WORK_INTERVAL_TICKS.get());

        if (level.getGameTime() % interval == 0) {
            tryGenerateLifeEssence();
        }

        tryAutoOutputFluid();
    }

    private void updateTankCapacityFromConfig() {
        int capacity = BMAddonCommonConfig.BLOOD_GENERATOR_LIFE_TANK_CAPACITY.get();

        bloodTank.setCapacity(capacity);

        if (bloodTank.getFluidAmount() > capacity) {
            bloodTank.getFluid().setAmount(capacity);
            setChangedAndSync();
        }
    }

    private void tryGenerateLifeEssence() {
        int configuredEnergyCost = BMAddonCommonConfig.BLOOD_GENERATOR_ENERGY_PER_OPERATION.get();
        int configuredLifeAmount = BMAddonCommonConfig.BLOOD_GENERATOR_LIFE_ESSENCE_PER_OPERATION.get();
        int storedEnergy = energyStorage.getEnergyStored();

        if (configuredEnergyCost <= 0 || configuredLifeAmount <= 0) {
            return;
        }

        if (storedEnergy <= 0) {
            return;
        }

        int energyLimitedLifeAmount = calculateLifeAmountForEnergy(
                configuredEnergyCost,
                configuredLifeAmount,
                storedEnergy
        );

        if (energyLimitedLifeAmount <= 0) {
            return;
        }

        FluidStack requestedGeneration = BloodMagicFluidHelper.lifeEssenceStack(energyLimitedLifeAmount);
        int acceptedLifeAmount = bloodTank.fill(requestedGeneration, IFluidHandler.FluidAction.SIMULATE);

        if (acceptedLifeAmount <= 0) {
            return;
        }

        int actualEnergyCost = calculateEnergyCostForGeneratedLife(
                configuredEnergyCost,
                configuredLifeAmount,
                acceptedLifeAmount
        );

        if (storedEnergy < actualEnergyCost) {
            return;
        }

        FluidStack actualGeneration = BloodMagicFluidHelper.lifeEssenceStack(acceptedLifeAmount);

        energyStorage.consumeInternal(actualEnergyCost);
        bloodTank.fill(actualGeneration, IFluidHandler.FluidAction.EXECUTE);

        setChangedAndSync();
    }

    private int calculateEnergyCostForGeneratedLife(
            int configuredEnergyCost,
            int configuredLifeAmount,
            int actualLifeAmount
    ) {
        if (actualLifeAmount >= configuredLifeAmount) {
            return configuredEnergyCost;
        }

        double ratio = (double) actualLifeAmount / (double) configuredLifeAmount;
        int calculatedCost = (int) Math.ceil(configuredEnergyCost * ratio);

        return Math.max(1, calculatedCost);
    }

    private int calculateLifeAmountForEnergy(
            int configuredEnergyCost,
            int configuredLifeAmount,
            int storedEnergy
    ) {
        if (storedEnergy >= configuredEnergyCost) {
            return configuredLifeAmount;
        }

        long scaledLifeAmount = (long) storedEnergy * (long) configuredLifeAmount;

        return (int) Math.min(configuredLifeAmount, scaledLifeAmount / configuredEnergyCost);
    }

    private void tryAutoOutputFluid() {
        if (level == null || level.isClientSide()) {
            return;
        }

        if (!BMAddonCommonConfig.BLOOD_GENERATOR_AUTO_OUTPUT.get()) {
            return;
        }

        if (bloodTank.isEmpty()) {
            return;
        }

        int maxOutput = BMAddonCommonConfig.BLOOD_GENERATOR_MAX_FLUID_OUTPUT_PER_TICK.get();

        for (Direction direction : Direction.values()) {
            if (bloodTank.isEmpty()) {
                return;
            }

            BlockPos targetPos = worldPosition.relative(direction);
            ResourceHandler<FluidResource> handler = level.getCapability(
                    Capabilities.Fluid.BLOCK,
                    targetPos,
                    direction.getOpposite()
            );

            if (handler == null) {
                continue;
            }

            FluidStack simulatedDrain = bloodTank.drain(maxOutput, IFluidHandler.FluidAction.SIMULATE);

            if (simulatedDrain.isEmpty()) {
                return;
            }

            FluidResource resource = FluidResource.of(simulatedDrain);
            int accepted;

            try (Transaction transaction = Transaction.openRoot()) {
                accepted = handler.insert(resource, simulatedDrain.getAmount(), transaction);

                if (accepted <= 0) {
                    continue;
                }

                FluidStack actualDrain = bloodTank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);

                if (actualDrain.isEmpty()) {
                    continue;
                }

                transaction.commit();
            }

            setChangedAndSync();
        }
    }

    public int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }

    public FluidStack getBloodStack() {
        return bloodTank.getFluid().copy();
    }

    public int getBloodAmount() {
        return bloodTank.getFluidAmount();
    }

    public int getBloodCapacity() {
        return bloodTank.getCapacity();
    }

    public ContainerData createContainerData() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case DATA_ENERGY_STORED -> getEnergyStored();
                    case DATA_MAX_ENERGY_STORED -> getMaxEnergyStored();
                    case DATA_BLOOD_AMOUNT -> getBloodAmount();
                    case DATA_BLOOD_CAPACITY -> getBloodCapacity();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.bmaddon.blood_generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BloodGeneratorMenu(containerId, playerInventory, this);
    }

    private void setChangedAndSync() {
        setChanged();

        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putInt(TAG_ENERGY, getEnergyStored());
        bloodTank.serialize(output.child(TAG_BLOOD_TANK));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        energyStorage.setEnergyStored(input.getIntOr(TAG_ENERGY, 0));
        input.child(TAG_BLOOD_TANK).ifPresent(bloodTank::deserialize);
        updateTankCapacityFromConfig();
    }

    public IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        return energyStorage.oldEnergyStorage;
    }

    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return externalFluidHandler;
    }

    public EnergyHandler getTransferEnergyHandler(@Nullable Direction side) {
        return energyStorage;
    }

    public ResourceHandler<FluidResource> getTransferFluidHandler(@Nullable Direction side) {
        return transferFluidHandler;
    }

    private final class OutputOnlyTransferFluidHandler implements ResourceHandler<FluidResource> {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public FluidResource getResource(int slot) {
            if (slot != 0 || bloodTank.isEmpty()) {
                return FluidResource.EMPTY;
            }

            return FluidResource.of(bloodTank.getFluid());
        }

        @Override
        public long getAmountAsLong(int slot) {
            return slot == 0 ? bloodTank.getFluidAmount() : 0;
        }

        @Override
        public long getCapacityAsLong(int slot, FluidResource resource) {
            return slot == 0 && isLifeEssence(resource) ? bloodTank.getCapacity() : 0;
        }

        @Override
        public boolean isValid(int slot, FluidResource resource) {
            return false;
        }

        @Override
        public int insert(int slot, FluidResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public int extract(int slot, FluidResource resource, int amount, TransactionContext transaction) {
            if (slot != 0 || amount <= 0 || !isLifeEssence(resource)) {
                return 0;
            }

            FluidStack simulatedDrain = bloodTank.drain(amount, IFluidHandler.FluidAction.SIMULATE);

            if (simulatedDrain.isEmpty()) {
                return 0;
            }

            fluidTransactionJournal.updateSnapshots(transaction);

            return bloodTank.drain(simulatedDrain.getAmount(), IFluidHandler.FluidAction.EXECUTE).getAmount();
        }

        private boolean isLifeEssence(FluidResource resource) {
            return resource != null && resource.getFluid() == BloodMagicFluidHelper.lifeEssenceFluid();
        }
    }

    private final class FluidTransactionJournal extends SnapshotJournal<FluidStack> {
        @Override
        protected FluidStack createSnapshot() {
            return bloodTank.getFluid().copy();
        }

        @Override
        protected void revertToSnapshot(FluidStack snapshot) {
            bloodTank.setFluid(snapshot.copy());
            setChangedAndSync();
        }
    }

    private final class GeneratorEnergyStorage extends SimpleEnergyHandler {
        private final IEnergyStorage oldEnergyStorage = new LegacyEnergyStorageView();

        private GeneratorEnergyStorage() {
            super(
                    BMAddonCommonConfig.BLOOD_GENERATOR_ENERGY_CAPACITY.get(),
                    BMAddonCommonConfig.BLOOD_GENERATOR_MAX_ENERGY_INPUT.get(),
                    0
            );
        }

        @Override
        public long getCapacityAsLong() {
            refreshLimitsFromConfig();
            return BMAddonCommonConfig.BLOOD_GENERATOR_ENERGY_CAPACITY.get();
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            refreshLimitsFromConfig();
            int inserted = super.insert(
                    Math.min(amount, BMAddonCommonConfig.BLOOD_GENERATOR_MAX_ENERGY_INPUT.get()),
                    transaction
            );

            if (inserted > 0) {
                setChangedAndSync();
            }

            return inserted;
        }

        private void refreshLimitsFromConfig() {
            capacity = BMAddonCommonConfig.BLOOD_GENERATOR_ENERGY_CAPACITY.get();
            maxInsert = BMAddonCommonConfig.BLOOD_GENERATOR_MAX_ENERGY_INPUT.get();

            if (energy > capacity) {
                energy = capacity;
            }
        }

        private void consumeInternal(int amount) {
            set(Math.max(0, getAmountAsInt() - amount));
            setChangedAndSync();
        }

        private void setEnergyStored(int amount) {
            set(Math.max(0, Math.min(amount, getMaxEnergyStored())));
            setChanged();
        }

        private int getEnergyStored() {
            return getAmountAsInt();
        }

        private int getMaxEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE, getCapacityAsLong());
        }

        private final class LegacyEnergyStorageView implements IEnergyStorage {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                if (simulate) {
                    return Math.min(
                            Math.min(maxReceive, BMAddonCommonConfig.BLOOD_GENERATOR_MAX_ENERGY_INPUT.get()),
                            getMaxEnergyStored() - getEnergyStored()
                    );
                }

                try (Transaction transaction = Transaction.openRoot()) {
                    int inserted = GeneratorEnergyStorage.this.insert(maxReceive, transaction);
                    transaction.commit();
                    return inserted;
                }
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                return 0;
            }

            @Override
            public int getEnergyStored() {
                return GeneratorEnergyStorage.this.getEnergyStored();
            }

            @Override
            public int getMaxEnergyStored() {
                return GeneratorEnergyStorage.this.getMaxEnergyStored();
            }

            @Override
            public boolean canExtract() {
                return false;
            }

            @Override
            public boolean canReceive() {
                return true;
            }
        }
    }

    private final class OutputOnlyFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            if (tank != 0) {
                return FluidStack.EMPTY;
            }

            return bloodTank.getFluid().copy();
        }

        @Override
        public int getTankCapacity(int tank) {
            if (tank != 0) {
                return 0;
            }

            return bloodTank.getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return FluidStack.EMPTY;
            }

            if (!BloodMagicFluidHelper.isLifeEssence(resource)) {
                return FluidStack.EMPTY;
            }

            return bloodTank.drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return bloodTank.drain(maxDrain, action);
        }
    }
}
