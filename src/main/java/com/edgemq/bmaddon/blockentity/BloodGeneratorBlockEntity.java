package com.edgemq.bmaddon.blockentity;

import com.edgemq.bmaddon.config.BMAddonCommonConfig;
import com.edgemq.bmaddon.menu.BloodGeneratorMenu;
import com.edgemq.bmaddon.registry.BMAddonBlockEntities;
import com.edgemq.bmaddon.util.BloodMagicFluidHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

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

        if (configuredEnergyCost <= 0 || configuredLifeAmount <= 0) {
            return;
        }

        if (energyStorage.getEnergyStored() <= 0) {
            return;
        }

        FluidStack requestedGeneration = BloodMagicFluidHelper.lifeEssenceStack(configuredLifeAmount);
        int acceptedLifeAmount = bloodTank.fill(requestedGeneration, IFluidHandler.FluidAction.SIMULATE);

        if (acceptedLifeAmount <= 0) {
            return;
        }

        int actualEnergyCost = calculateEnergyCostForGeneratedLife(
                configuredEnergyCost,
                configuredLifeAmount,
                acceptedLifeAmount
        );

        if (energyStorage.getEnergyStored() < actualEnergyCost) {
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

    private void tryAutoOutputFluid() {
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

    public IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        return energyStorage;
    }

    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return externalFluidHandler;
    }

    private final class GeneratorEnergyStorage implements IEnergyStorage {
        private int energy;

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (maxReceive <= 0) {
                return 0;
            }

            int capacity = getMaxEnergyStored();
            int maxInput = BMAddonCommonConfig.BLOOD_GENERATOR_MAX_ENERGY_INPUT.get();
            int accepted = Math.min(maxReceive, maxInput);
            int inserted = Math.min(accepted, capacity - energy);

            if (inserted <= 0) {
                return 0;
            }

            if (!simulate) {
                energy += inserted;
                setChangedAndSync();
            }

            return inserted;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return energy;
        }

        @Override
        public int getMaxEnergyStored() {
            return BMAddonCommonConfig.BLOOD_GENERATOR_ENERGY_CAPACITY.get();
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }

        private void consumeInternal(int amount) {
            energy = Math.max(0, energy - amount);
            setChangedAndSync();
        }

        private void setEnergyStored(int amount) {
            energy = Math.max(0, Math.min(amount, getMaxEnergyStored()));
            setChanged();
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
