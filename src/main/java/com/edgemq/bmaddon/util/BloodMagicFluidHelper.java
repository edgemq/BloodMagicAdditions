package com.edgemq.bmaddon.util;

import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import wayoftime.bloodmagic.common.fluid.BloodMagicFluids;

public final class BloodMagicFluidHelper {
    public static Fluid lifeEssenceFluid() {
        Object value = BloodMagicFluids.LIFE_ESSENCE_FLUID.get();

        if (value instanceof Fluid fluid) {
            return fluid;
        }

        throw new IllegalStateException("Blood Magic Life Essence fluid is not available.");
    }

    public static FluidStack lifeEssenceStack(int amount) {
        return new FluidStack(lifeEssenceFluid(), amount);
    }

    public static boolean isLifeEssence(FluidStack stack) {
        return !stack.isEmpty() && stack.getFluid() == lifeEssenceFluid();
    }

    private BloodMagicFluidHelper() {
    }
}