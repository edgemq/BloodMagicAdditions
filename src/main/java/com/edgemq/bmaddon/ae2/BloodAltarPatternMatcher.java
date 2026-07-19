package com.edgemq.bmaddon.ae2;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import com.edgemq.bmaddon.item.BloodAltarPatternItem;
import com.edgemq.bmaddon.util.BloodAltarRecipeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.recipe.RecipeBloodAltar;

import javax.annotation.Nullable;
import java.util.Optional;

public final class BloodAltarPatternMatcher {
    public static Optional<IPatternDetails> createDetailsFromStack(
            Level level,
            ItemStack patternStack,
            int bloodMagicRecipeTierLimit
    ) {
        if (level == null || patternStack.isEmpty() || !BloodAltarPatternItem.isEncoded(patternStack)) {
            return Optional.empty();
        }

        ResourceLocation recipeId = BloodAltarPatternItem.getRecipeId(patternStack);

        if (recipeId == null) {
            return Optional.empty();
        }

        Optional<RecipeBloodAltar> recipeOptional = BloodAltarRecipeHelper.getAltarRecipe(level, recipeId);

        if (recipeOptional.isEmpty()) {
            return Optional.empty();
        }

        RecipeBloodAltar recipe = recipeOptional.get();

        if (recipe.getMinimumTier() > bloodMagicRecipeTierLimit) {
            return Optional.empty();
        }

        return BloodAltarPatternDetails.create(level, patternStack, recipe)
                .map(details -> details);
    }

    public static Optional<ResolvedBloodAltarPattern> resolvePatternDetails(
            Level level,
            IPatternDetails patternDetails,
            int bloodMagicRecipeTierLimit
    ) {
        if (level == null || patternDetails == null) {
            return Optional.empty();
        }

        if (patternDetails instanceof BloodAltarPatternDetails bloodAltarPatternDetails) {
            return resolveCustomBloodAltarPattern(level, bloodAltarPatternDetails, bloodMagicRecipeTierLimit);
        }

        return Optional.empty();
    }

    private static Optional<ResolvedBloodAltarPattern> resolveCustomBloodAltarPattern(
            Level level,
            BloodAltarPatternDetails patternDetails,
            int bloodMagicRecipeTierLimit
    ) {
        ResourceLocation recipeId = patternDetails.getRecipeId();

        Optional<RecipeBloodAltar> recipeOptional = BloodAltarRecipeHelper.getAltarRecipe(level, recipeId);

        if (recipeOptional.isEmpty()) {
            return Optional.empty();
        }

        RecipeBloodAltar recipe = recipeOptional.get();

        if (recipe.getMinimumTier() > bloodMagicRecipeTierLimit) {
            return Optional.empty();
        }

        GenericStack primaryOutput = patternDetails.getPrimaryOutput();

        if (primaryOutput == null || primaryOutput.amount() <= 0) {
            return Optional.empty();
        }

        if (!matchesRecipeOutput(patternDetails, recipe)) {
            return Optional.empty();
        }

        return Optional.of(new ResolvedBloodAltarPattern(recipe.getId(), primaryOutput));
    }

    private static boolean matchesRecipeOutput(IPatternDetails patternDetails, RecipeBloodAltar recipe) {
        GenericStack primaryOutput = patternDetails.getPrimaryOutput();

        if (primaryOutput == null || primaryOutput.amount() <= 0) {
            return false;
        }

        ItemStack recipeOutput = recipe.getOutput().copy();

        if (recipeOutput.isEmpty()) {
            return false;
        }

        AEItemKey recipeOutputKey = AEItemKey.of(recipeOutput);

        if (recipeOutputKey == null) {
            return false;
        }

        return primaryOutput.what().equals(recipeOutputKey)
                && primaryOutput.amount() == recipeOutput.getCount();
    }

    public static boolean isSupportedPatternStack(ItemStack stack) {
        return BloodAltarPatternItem.isEncoded(stack);
    }

    public static ItemStack getOutputPreviewForStack(@Nullable Level level, ItemStack patternStack) {
        if (patternStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (BloodAltarPatternItem.isEncoded(patternStack)) {
            return BloodAltarPatternItem.getOutputPreview(patternStack);
        }

        return ItemStack.EMPTY;
    }

    private BloodAltarPatternMatcher() {
    }

    public record ResolvedBloodAltarPattern(
            ResourceLocation recipeId,
            GenericStack output
    ) {
    }
}
