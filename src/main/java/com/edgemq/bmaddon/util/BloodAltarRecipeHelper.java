package com.edgemq.bmaddon.util;

import com.edgemq.bmaddon.ae2.BloodMagicPatternKind;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.recipe.BloodMagicRecipeType;
import wayoftime.bloodmagic.recipe.RecipeAlchemyTable;
import wayoftime.bloodmagic.recipe.RecipeBloodAltar;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class BloodAltarRecipeHelper {
    public static Optional<RecipeBloodAltar> findAltarRecipe(Level level, ItemStack input) {
        if (level == null || input.isEmpty()) {
            return Optional.empty();
        }

        List<RecipeBloodAltar> recipes = level.getRecipeManager().getAllRecipesFor(BloodMagicRecipeType.ALTAR.get());

        return recipes.stream()
                .filter(recipe -> recipe.getInput().test(input))
                .sorted(Comparator.comparingInt(RecipeBloodAltar::getMinimumTier))
                .findFirst();
    }

    public static Optional<RecipeBloodAltar> getAltarRecipe(Level level, ResourceLocation recipeId) {
        if (level == null || recipeId == null) {
            return Optional.empty();
        }

        return level.getRecipeManager()
                .byKey(recipeId)
                .filter(recipe -> recipe instanceof RecipeBloodAltar)
                .map(recipe -> (RecipeBloodAltar) recipe);
    }

    public static Optional<RecipeAlchemyTable> getAlchemyTableRecipe(Level level, ResourceLocation recipeId) {
        if (level == null || recipeId == null) {
            return Optional.empty();
        }

        return level.getRecipeManager()
                .byKey(recipeId)
                .filter(recipe -> recipe instanceof RecipeAlchemyTable)
                .map(recipe -> (RecipeAlchemyTable) recipe);
    }

    public static Optional<RecipeAlchemyTable> findAlchemyTableRecipe(
            Level level,
            List<ItemStack> inputs,
            ItemStack expectedOutput
    ) {
        if (level == null || inputs.isEmpty() || expectedOutput.isEmpty()) {
            return Optional.empty();
        }

        List<RecipeAlchemyTable> recipes = level.getRecipeManager().getAllRecipesFor(BloodMagicRecipeType.ALCHEMYTABLE.get());

        return recipes.stream()
                .filter(recipe -> outputMatches(recipe.getOutput(), expectedOutput))
                .filter(recipe -> inputsMatch(getAlchemyTableIngredients(recipe), inputs))
                .sorted(Comparator.comparingInt(RecipeAlchemyTable::getMinimumTier))
                .findFirst();
    }

    public static boolean recipeStillExists(Level level, BloodMagicPatternKind kind, ResourceLocation recipeId) {
        return switch (kind) {
            case BLOOD_ALTAR -> getAltarRecipe(level, recipeId).isPresent();
            case ALCHEMY_TABLE -> getAlchemyTableRecipe(level, recipeId).isPresent();
        };
    }

    public static ItemStack getOutputPreview(Level level, BloodMagicPatternKind kind, ResourceLocation recipeId) {
        return switch (kind) {
            case BLOOD_ALTAR -> getAltarRecipe(level, recipeId)
                    .map(recipe -> recipe.getOutput().copy())
                    .orElse(ItemStack.EMPTY);
            case ALCHEMY_TABLE -> getAlchemyTableRecipe(level, recipeId)
                    .map(recipe -> recipe.getOutput().copy())
                    .orElse(ItemStack.EMPTY);
        };
    }

    @SuppressWarnings("unchecked")
    public static List<Ingredient> getAlchemyTableIngredients(RecipeAlchemyTable recipe) {
        return (List<Ingredient>) recipe.getInput();
    }

    public static boolean inputsMatch(List<Ingredient> ingredients, List<ItemStack> inputs) {
        List<Ingredient> requiredIngredients = ingredients.stream()
                .filter(ingredient -> ingredient != null && !ingredient.isEmpty())
                .toList();

        List<ItemStack> availableInputs = inputs.stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .map(stack -> {
                    ItemStack copy = stack.copy();
                    copy.setCount(1);
                    return copy;
                })
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (requiredIngredients.size() != availableInputs.size()) {
            return false;
        }

        boolean[] used = new boolean[availableInputs.size()];

        for (Ingredient ingredient : requiredIngredients) {
            boolean matched = false;

            for (int index = 0; index < availableInputs.size(); index++) {
                if (used[index]) {
                    continue;
                }

                if (ingredient.test(availableInputs.get(index))) {
                    used[index] = true;
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                return false;
            }
        }

        return true;
    }

    public static boolean outputMatches(ItemStack recipeOutput, ItemStack expectedOutput) {
        if (recipeOutput.isEmpty() || expectedOutput.isEmpty()) {
            return false;
        }

        return ItemStack.isSameItemSameTags(recipeOutput, expectedOutput)
                && recipeOutput.getCount() == expectedOutput.getCount();
    }

    @Nullable
    public static ResourceLocation parseRecipeId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return ResourceLocation.tryParse(value);
    }

    private BloodAltarRecipeHelper() {
    }
}