package com.edgemq.bmaddon.ae2;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import com.edgemq.bmaddon.item.BloodAltarPatternItem;
import com.edgemq.bmaddon.util.BloodAltarRecipeHelper;
import com.edgemq.bmaddon.util.BloodMagicFluidHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.recipe.BMRecipes;
import wayoftime.bloodmagic.common.recipe.bloodaltar.BloodAltarRecipe;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public final class BloodAltarPatternMatcher {
    public static Optional<IPatternDetails> createDetailsFromStack(
            Level level,
            ItemStack patternStack,
            int bloodMagicRecipeTierLimit
    ) {
        if (level == null || patternStack.isEmpty()) {
            return Optional.empty();
        }

        if (BloodAltarPatternItem.isEncoded(patternStack)) {
            ResourceLocation recipeId = BloodAltarPatternItem.getRecipeId(patternStack);

            if (recipeId == null) {
                return Optional.empty();
            }

            Optional<BloodAltarRecipe> recipeOptional = BloodAltarRecipeHelper.getAltarRecipe(level, recipeId);

            if (recipeOptional.isEmpty()) {
                return Optional.empty();
            }

            BloodAltarRecipe recipe = recipeOptional.get();

            if (recipe.getMinTier() > bloodMagicRecipeTierLimit) {
                return Optional.empty();
            }

            return BloodAltarPatternDetails.create(level, patternStack, recipe)
                    .map(details -> details);
        }

        if (!PatternDetailsHelper.isEncodedPattern(patternStack)) {
            return Optional.empty();
        }

        IPatternDetails decoded = PatternDetailsHelper.decodePattern(patternStack, level);

        if (decoded == null) {
            return Optional.empty();
        }

        return resolvePatternDetails(level, decoded, bloodMagicRecipeTierLimit)
                .map(resolved -> decoded);
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

        return resolveAe2ProcessingPattern(level, patternDetails, bloodMagicRecipeTierLimit);
    }

    private static Optional<ResolvedBloodAltarPattern> resolveCustomBloodAltarPattern(
            Level level,
            BloodAltarPatternDetails patternDetails,
            int bloodMagicRecipeTierLimit
    ) {
        ResourceLocation recipeId = patternDetails.getRecipeId();

        Optional<BloodAltarRecipe> recipeOptional = BloodAltarRecipeHelper.getAltarRecipe(level, recipeId);

        if (recipeOptional.isEmpty()) {
            return Optional.empty();
        }

        BloodAltarRecipe recipe = recipeOptional.get();

        if (recipe.getMinTier() > bloodMagicRecipeTierLimit) {
            return Optional.empty();
        }

        GenericStack primaryOutput = patternDetails.getPrimaryOutput();

        if (primaryOutput == null || primaryOutput.amount() <= 0) {
            return Optional.empty();
        }

        if (!matchesRecipeOutput(patternDetails, recipe)) {
            return Optional.empty();
        }

        return Optional.of(new ResolvedBloodAltarPattern(recipeId, primaryOutput));
    }

    private static Optional<ResolvedBloodAltarPattern> resolveAe2ProcessingPattern(
            Level level,
            IPatternDetails patternDetails,
            int bloodMagicRecipeTierLimit
    ) {
        /*
         * Обычный AE2 Processing Pattern предназначен для внешних машин.
         * Crafting patterns, smithing patterns и прочие внутренние шаблоны сюда не пускаем.
         */
        if (!patternDetails.supportsPushInputsToExternalInventory()) {
            return Optional.empty();
        }

        GenericStack primaryOutput = patternDetails.getPrimaryOutput();

        if (primaryOutput == null || primaryOutput.amount() <= 0) {
            return Optional.empty();
        }

        List<RecipeHolder<BloodAltarRecipe>> recipes = level.getRecipeManager().getAllRecipesFor(BMRecipes.BLOOD_ALTAR_TYPE.get());

        for (RecipeHolder<BloodAltarRecipe> recipeHolder : recipes) {
            BloodAltarRecipe recipe = recipeHolder.value();

            if (recipe.getMinTier() > bloodMagicRecipeTierLimit) {
                continue;
            }

            if (!matchesRecipeOutput(patternDetails, recipe)) {
                continue;
            }

            if (!matchesRecipeItemInput(patternDetails, recipe)) {
                continue;
            }

            if (!matchesRecipeLifeEssenceInput(patternDetails, recipe)) {
                continue;
            }

            return Optional.of(new ResolvedBloodAltarPattern(recipeHolder.id(), primaryOutput));
        }

        return Optional.empty();
    }

    private static boolean matchesRecipeOutput(IPatternDetails patternDetails, BloodAltarRecipe recipe) {
        GenericStack primaryOutput = patternDetails.getPrimaryOutput();

        if (primaryOutput == null || primaryOutput.amount() <= 0) {
            return false;
        }

        ItemStack recipeOutput = recipe.getResult().copy();

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

    private static boolean matchesRecipeItemInput(IPatternDetails patternDetails, BloodAltarRecipe recipe) {
        for (IPatternDetails.IInput input : patternDetails.getInputs()) {
            long multiplier = Math.max(1L, input.getMultiplier());

            for (GenericStack possibleInput : input.getPossibleInputs()) {
                if (!(possibleInput.what() instanceof AEItemKey itemKey)) {
                    continue;
                }

                long amount = possibleInput.amount() * multiplier;

                if (amount < 1) {
                    continue;
                }

                ItemStack stack = itemKey.toStack(1);

                if (recipe.getInput().test(stack)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean matchesRecipeLifeEssenceInput(IPatternDetails patternDetails, BloodAltarRecipe recipe) {
        int requiredLifeEssence = BloodAltarPatternDetails.getRequiredLifeEssence(recipe);

        if (requiredLifeEssence <= 0) {
            return true;
        }

        AEFluidKey lifeEssenceKey = AEFluidKey.of(BloodMagicFluidHelper.lifeEssenceFluid());

        for (IPatternDetails.IInput input : patternDetails.getInputs()) {
            long multiplier = Math.max(1L, input.getMultiplier());

            for (GenericStack possibleInput : input.getPossibleInputs()) {
                if (!possibleInput.what().equals(lifeEssenceKey)) {
                    continue;
                }

                long amount = possibleInput.amount() * multiplier;

                if (amount >= requiredLifeEssence) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isSupportedPatternStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (BloodAltarPatternItem.isEncoded(stack)) {
            return true;
        }

        return PatternDetailsHelper.isEncodedPattern(stack);
    }

    public static ItemStack getOutputPreviewForStack(@Nullable Level level, ItemStack patternStack) {
        if (patternStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (BloodAltarPatternItem.isEncoded(patternStack)) {
            return BloodAltarPatternItem.getOutputPreview(patternStack);
        }

        if (level == null) {
            return ItemStack.EMPTY;
        }

        if (!PatternDetailsHelper.isEncodedPattern(patternStack)) {
            return ItemStack.EMPTY;
        }

        IPatternDetails decoded = PatternDetailsHelper.decodePattern(patternStack, level);

        if (decoded == null) {
            return ItemStack.EMPTY;
        }

        GenericStack primaryOutput = decoded.getPrimaryOutput();

        if (primaryOutput == null || primaryOutput.amount() <= 0) {
            return ItemStack.EMPTY;
        }

        if (primaryOutput.what() instanceof AEItemKey itemKey) {
            int amount = (int) Math.min(Integer.MAX_VALUE, primaryOutput.amount());
            return itemKey.toStack(amount);
        }

        return GenericStack.wrapInItemStack(primaryOutput);
    }

    private BloodAltarPatternMatcher() {
    }

    public record ResolvedBloodAltarPattern(
            ResourceLocation recipeId,
            GenericStack output
    ) {
    }
}
