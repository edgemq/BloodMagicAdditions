package com.edgemq.bmaddon.util;

import com.breakinblocks.neovitae.common.fluid.NVFluids;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public final class BloodMagicFluidHelper {
    public static Fluid lifeEssenceFluid() {
        return NVFluids.ESSENTIA_VITAE_SOURCE.get();
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
