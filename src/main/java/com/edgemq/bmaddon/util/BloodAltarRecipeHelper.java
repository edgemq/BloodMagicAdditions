package com.edgemq.bmaddon.util;

import com.edgemq.bmaddon.ae2.BloodMagicPatternKind;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.recipe.BMRecipes;
import wayoftime.bloodmagic.common.recipe.alchemy_table.AlchemyTableRecipe;
import wayoftime.bloodmagic.common.recipe.bloodaltar.BloodAltarRecipe;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class BloodAltarRecipeHelper {
    public record FoundAltarRecipe(ResourceLocation id, BloodAltarRecipe recipe) {
    }

    public record FoundAlchemyTableRecipe(ResourceLocation id, AlchemyTableRecipe recipe) {
    }

    public static Optional<BloodAltarRecipe> findAltarRecipe(Level level, ItemStack input) {
        return findAltarRecipeWithId(level, input).map(FoundAltarRecipe::recipe);
    }

    public static Optional<FoundAltarRecipe> findAltarRecipeWithId(Level level, ItemStack input) {
        if (level == null || input.isEmpty()) {
            return Optional.empty();
        }

        List<RecipeHolder<BloodAltarRecipe>> recipes = level.getRecipeManager().getAllRecipesFor(BMRecipes.BLOOD_ALTAR_TYPE.get());

        return recipes.stream()
                .filter(recipeHolder -> recipeHolder.value().getInput().test(input))
                .sorted(Comparator.comparingInt(recipeHolder -> recipeHolder.value().getMinTier()))
                .map(recipeHolder -> new FoundAltarRecipe(recipeHolder.id(), recipeHolder.value()))
                .findFirst();
    }

    public static Optional<BloodAltarRecipe> getAltarRecipe(Level level, ResourceLocation recipeId) {
        if (level == null || recipeId == null) {
            return Optional.empty();
        }

        return level.getRecipeManager()
                .byKey(recipeId)
                .map(RecipeHolder::value)
                .filter(recipe -> recipe instanceof BloodAltarRecipe)
                .map(recipe -> (BloodAltarRecipe) recipe);
    }

    public static Optional<AlchemyTableRecipe> getAlchemyTableRecipe(Level level, ResourceLocation recipeId) {
        if (level == null || recipeId == null) {
            return Optional.empty();
        }

        return level.getRecipeManager()
                .byKey(recipeId)
                .map(RecipeHolder::value)
                .filter(recipe -> recipe instanceof AlchemyTableRecipe)
                .map(recipe -> (AlchemyTableRecipe) recipe);
    }

    public static Optional<AlchemyTableRecipe> findAlchemyTableRecipe(
            Level level,
            List<ItemStack> inputs,
            ItemStack expectedOutput
    ) {
        return findAlchemyTableRecipeWithId(level, inputs, expectedOutput).map(FoundAlchemyTableRecipe::recipe);
    }

    public static Optional<FoundAlchemyTableRecipe> findAlchemyTableRecipeWithId(
            Level level,
            List<ItemStack> inputs,
            ItemStack expectedOutput
    ) {
        if (level == null || inputs.isEmpty() || expectedOutput.isEmpty()) {
            return Optional.empty();
        }

        List<RecipeHolder<AlchemyTableRecipe>> recipes = level.getRecipeManager().getAllRecipesFor(BMRecipes.ALCHEMY_TABLE_TYPE.get());

        return recipes.stream()
                .filter(recipeHolder -> outputMatches(recipeHolder.value().output(), expectedOutput))
                .filter(recipeHolder -> inputsMatch(getAlchemyTableIngredients(recipeHolder.value()), inputs))
                .sorted(Comparator.comparingInt(recipeHolder -> recipeHolder.value().tier()))
                .map(recipeHolder -> new FoundAlchemyTableRecipe(recipeHolder.id(), recipeHolder.value()))
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
                    .map(recipe -> recipe.getResult().copy())
                    .orElse(ItemStack.EMPTY);
            case ALCHEMY_TABLE -> getAlchemyTableRecipe(level, recipeId)
                    .map(recipe -> recipe.output().copy())
                    .orElse(ItemStack.EMPTY);
        };
    }

    @SuppressWarnings("unchecked")
    public static List<Ingredient> getAlchemyTableIngredients(AlchemyTableRecipe recipe) {
        return recipe.inputs();
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

        return ItemStack.isSameItemSameComponents(recipeOutput, expectedOutput)
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
