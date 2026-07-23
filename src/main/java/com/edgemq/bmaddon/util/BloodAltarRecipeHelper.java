package com.edgemq.bmaddon.util;

import com.breakinblocks.neovitae.api.recipe.AraVitaeRecipe;
import com.breakinblocks.neovitae.common.recipe.NVRecipes;
import com.breakinblocks.neovitae.common.recipe.tabulavitae.TabulaVitaeRecipe;
import com.edgemq.bmaddon.ae2.BloodMagicPatternKind;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class BloodAltarRecipeHelper {
    public static Optional<RecipeHolder<AraVitaeRecipe>> findAltarRecipe(Level level, ItemStack input) {
        if (level == null || input.isEmpty()) {
            return Optional.empty();
        }

        List<RecipeHolder<AraVitaeRecipe>> recipes = getRecipes(level, AraVitaeRecipe.class);

        return recipes.stream()
                .filter(recipe -> recipe.value().getInput().test(input))
                .sorted(Comparator.comparingInt(recipe -> recipe.value().getMinTier()))
                .findFirst();
    }

    public static Optional<AraVitaeRecipe> getAltarRecipe(Level level, Identifier recipeId) {
        if (level == null || recipeId == null) {
            return Optional.empty();
        }

        return level.getServer().getRecipeManager()
                .byKey(ResourceKey.create(Registries.RECIPE, recipeId))
                .map(RecipeHolder::value)
                .filter(recipe -> recipe instanceof AraVitaeRecipe)
                .map(recipe -> (AraVitaeRecipe) recipe);
    }

    public static Optional<TabulaVitaeRecipe> getAlchemyTableRecipe(Level level, Identifier recipeId) {
        if (level == null || recipeId == null) {
            return Optional.empty();
        }

        return level.getServer().getRecipeManager()
                .byKey(ResourceKey.create(Registries.RECIPE, recipeId))
                .map(RecipeHolder::value)
                .filter(recipe -> recipe instanceof TabulaVitaeRecipe)
                .map(recipe -> (TabulaVitaeRecipe) recipe);
    }

    public static Optional<RecipeHolder<TabulaVitaeRecipe>> findAlchemyTableRecipe(
            Level level,
            List<ItemStack> inputs,
            ItemStack expectedOutput
    ) {
        if (level == null || inputs.isEmpty() || expectedOutput.isEmpty()) {
            return Optional.empty();
        }

        List<RecipeHolder<TabulaVitaeRecipe>> recipes = getRecipes(level, TabulaVitaeRecipe.class);

        return recipes.stream()
                .filter(recipe -> outputMatches(recipe.value().getOutput(), expectedOutput))
                .filter(recipe -> inputsMatch(getAlchemyTableIngredients(recipe.value()), inputs))
                .sorted(Comparator.comparingInt(recipe -> recipe.value().getMinimumTier()))
                .findFirst();
    }

    public static boolean recipeStillExists(Level level, BloodMagicPatternKind kind, Identifier recipeId) {
        return switch (kind) {
            case BLOOD_ALTAR -> getAltarRecipe(level, recipeId).isPresent();
            case ALCHEMY_TABLE -> getAlchemyTableRecipe(level, recipeId).isPresent();
        };
    }

    public static ItemStack getOutputPreview(Level level, BloodMagicPatternKind kind, Identifier recipeId) {
        return switch (kind) {
            case BLOOD_ALTAR -> getAltarRecipe(level, recipeId)
                    .map(recipe -> recipe.getResult().copy())
                    .orElse(ItemStack.EMPTY);
            case ALCHEMY_TABLE -> getAlchemyTableRecipe(level, recipeId)
                    .map(recipe -> recipe.getOutput().copy())
                    .orElse(ItemStack.EMPTY);
        };
    }

    @SuppressWarnings("unchecked")
    public static List<Ingredient> getAlchemyTableIngredients(TabulaVitaeRecipe recipe) {
        return recipe.getInput();
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

    @SuppressWarnings("unchecked")
    private static <T extends Recipe<?>> List<RecipeHolder<T>> getRecipes(Level level, Class<T> recipeClass) {
        if (level == null || level.getServer() == null) {
            return List.of();
        }

        return level.getServer().getRecipeManager().getRecipes().stream()
                .filter(holder -> recipeClass.isInstance(holder.value()))
                .map(holder -> (RecipeHolder<T>) holder)
                .toList();
    }

    @Nullable
    public static Identifier parseRecipeId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return Identifier.tryParse(value);
    }

    private BloodAltarRecipeHelper() {
    }
}
