package com.edgemq.bmaddon.ae2;

import com.breakinblocks.neovitae.api.recipe.AraVitaeRecipe;
import com.breakinblocks.neovitae.common.recipe.tabulavitae.TabulaVitaeRecipe;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import com.edgemq.bmaddon.item.BloodAltarPatternItem;
import com.edgemq.bmaddon.registry.BMAddonItems;
import com.edgemq.bmaddon.util.BloodAltarRecipeHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class BloodAltarPatternEncodingHelper {
    public static Optional<ItemStack> tryConvertAe2ProcessingPattern(ItemStack ae2EncodedPattern, Level level) {
        if (level == null || ae2EncodedPattern.isEmpty()) {
            return Optional.empty();
        }

        IPatternDetails details = PatternDetailsHelper.decodePattern(ae2EncodedPattern, level);

        if (details == null) {
            return Optional.empty();
        }

        GenericStack primaryOutput = details.getPrimaryOutput();

        if (primaryOutput == null || !(primaryOutput.what() instanceof AEItemKey outputKey)) {
            return Optional.empty();
        }

        ItemStack outputPreview = outputKey.toStack((int) Math.min(Integer.MAX_VALUE, primaryOutput.amount()));

        Optional<ItemStack> altarPattern = tryCreateBloodAltarPattern(level, details, outputPreview);

        if (altarPattern.isPresent()) {
            return altarPattern;
        }

        return tryCreateAlchemyTablePattern(level, details, outputPreview);
    }

    private static Optional<ItemStack> tryCreateBloodAltarPattern(
            Level level,
            IPatternDetails details,
            ItemStack outputPreview
    ) {
        List<ItemStack> itemInputs = extractItemInputs(details);

        if (itemInputs.size() != 1) {
            return Optional.empty();
        }

        ItemStack inputPreview = itemInputs.get(0);

        Optional<RecipeHolder<AraVitaeRecipe>> recipeOptional = BloodAltarRecipeHelper.findAltarRecipe(level, inputPreview);

        if (recipeOptional.isEmpty()) {
            return Optional.empty();
        }

        RecipeHolder<AraVitaeRecipe> holder = recipeOptional.get();
        AraVitaeRecipe recipe = holder.value();

        if (!BloodAltarRecipeHelper.outputMatches(recipe.getResult(), outputPreview)) {
            return Optional.empty();
        }

        ItemStack result = new ItemStack(BMAddonItems.BLOOD_ALTAR_PATTERN.get());
        BloodAltarPatternItem.encode(result, holder.id().identifier(), recipe, inputPreview);

        return Optional.of(result);
    }

    private static Optional<ItemStack> tryCreateAlchemyTablePattern(
            Level level,
            IPatternDetails details,
            ItemStack outputPreview
    ) {
        List<ItemStack> itemInputs = extractItemInputs(details);

        if (itemInputs.isEmpty() || itemInputs.size() > TabulaVitaeRecipe.MAX_INPUTS) {
            return Optional.empty();
        }

        Optional<RecipeHolder<TabulaVitaeRecipe>> recipeOptional = BloodAltarRecipeHelper.findAlchemyTableRecipe(
                level,
                itemInputs,
                outputPreview
        );

        if (recipeOptional.isEmpty()) {
            return Optional.empty();
        }

        RecipeHolder<TabulaVitaeRecipe> holder = recipeOptional.get();
        TabulaVitaeRecipe recipe = holder.value();

        ItemStack result = new ItemStack(BMAddonItems.BLOOD_ALTAR_PATTERN.get());
        BloodAltarPatternItem.encode(result, holder.id().identifier(), recipe, itemInputs);

        return Optional.of(result);
    }

    private static List<ItemStack> extractItemInputs(IPatternDetails details) {
        List<ItemStack> inputs = new ArrayList<>();

        for (IPatternDetails.IInput input : details.getInputs()) {
            for (GenericStack possibleInput : input.getPossibleInputs()) {
                if (possibleInput == null || !(possibleInput.what() instanceof AEItemKey inputKey)) {
                    continue;
                }

                long amount = Math.max(1L, possibleInput.amount() * Math.max(1L, input.getMultiplier()));
                int safeAmount = (int) Math.min(64L, amount);

                for (int count = 0; count < safeAmount; count++) {
                    ItemStack stack = inputKey.toStack();
                    stack.setCount(1);
                    inputs.add(stack);
                }

                break;
            }
        }

        return inputs;
    }

    private BloodAltarPatternEncodingHelper() {
    }
}
