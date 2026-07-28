package com.edgemq.bmaddon.ae2;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.edgemq.bmaddon.config.BMAddonCommonConfig;
import com.edgemq.bmaddon.item.BloodAltarPatternItem;
import com.edgemq.bmaddon.util.BloodAltarRecipeHelper;
import com.edgemq.bmaddon.util.BloodMagicFluidHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import com.breakinblocks.neovitae.api.recipe.AraVitaeRecipe;
import com.breakinblocks.neovitae.common.recipe.tabulavitae.TabulaVitaeRecipe;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class BloodAltarPatternDetails implements IPatternDetails {
    private final BloodMagicPatternKind kind;
    private final ResourceLocation recipeId;
    private final AEItemKey definition;
    private final IInput[] inputs;
    private final GenericStack[] outputs;
    private final int minimumTier;
    private final int baseCraftTimeTicks;

    private BloodAltarPatternDetails(
            BloodMagicPatternKind kind,
            ResourceLocation recipeId,
            AEItemKey definition,
            IInput[] inputs,
            GenericStack[] outputs,
            int minimumTier,
            int baseCraftTimeTicks
    ) {
        this.kind = kind;
        this.recipeId = recipeId;
        this.definition = definition;
        this.inputs = inputs;
        this.outputs = outputs;
        this.minimumTier = minimumTier;
        this.baseCraftTimeTicks = baseCraftTimeTicks;
    }

    public static Optional<BloodAltarPatternDetails> create(Level level, ItemStack patternStack) {
        if (level == null || patternStack.isEmpty() || !BloodAltarPatternItem.isEncoded(patternStack)) {
            return Optional.empty();
        }

        BloodMagicPatternKind kind = BloodAltarPatternItem.getRecipeKind(patternStack);
        ResourceLocation recipeId = BloodAltarPatternItem.getRecipeId(patternStack);

        if (recipeId == null) {
            return Optional.empty();
        }

        return switch (kind) {
            case BLOOD_ALTAR -> BloodAltarRecipeHelper.getAltarRecipe(level, recipeId)
                    .flatMap(recipe -> create(level, patternStack, recipe));
            case ALCHEMY_TABLE -> BloodAltarRecipeHelper.getAlchemyTableRecipe(level, recipeId)
                    .flatMap(recipe -> create(level, patternStack, recipe));
        };
    }

    public static Optional<BloodAltarPatternDetails> create(Level level, ItemStack patternStack, AraVitaeRecipe recipe) {
        if (level == null || patternStack.isEmpty() || recipe == null) {
            return Optional.empty();
        }

        if (!BloodAltarPatternItem.isEncoded(patternStack)) {
            return Optional.empty();
        }

        AEItemKey definition = createDefinition(patternStack);

        if (definition == null) {
            return Optional.empty();
        }

        GenericStack output = createOutput(recipe.getResult());

        if (output == null) {
            return Optional.empty();
        }

        List<IInput> inputs = new ArrayList<>();

        IInput itemInput = createItemInput(recipe.getInput(), BloodAltarPatternItem.getInputPreview(patternStack));

        if (itemInput != null) {
            inputs.add(itemInput);
        }

        IInput bloodInput = createBloodInput(getRequiredLifeEssence(recipe.getTotalBlood()));

        if (bloodInput != null) {
            inputs.add(bloodInput);
        }

        if (inputs.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new BloodAltarPatternDetails(
                BloodMagicPatternKind.BLOOD_ALTAR,
                BloodAltarPatternItem.getRecipeId(patternStack),
                definition,
                inputs.toArray(IInput[]::new),
                new GenericStack[]{output},
                recipe.getMinTier(),
                0
        ));
    }

    public static Optional<BloodAltarPatternDetails> create(Level level, ItemStack patternStack, TabulaVitaeRecipe recipe) {
        if (level == null || patternStack.isEmpty() || recipe == null) {
            return Optional.empty();
        }

        if (!BloodAltarPatternItem.isEncoded(patternStack)) {
            return Optional.empty();
        }

        AEItemKey definition = createDefinition(patternStack);

        if (definition == null) {
            return Optional.empty();
        }

        GenericStack output = createOutput(recipe.getOutput());

        if (output == null) {
            return Optional.empty();
        }

        List<ItemStack> storedPreviews = BloodAltarPatternItem.getInputPreviews(patternStack);
        List<Ingredient> ingredients = BloodAltarRecipeHelper.getAlchemyTableIngredients(recipe);
        List<IInput> inputs = new ArrayList<>();

        for (int index = 0; index < ingredients.size(); index++) {
            Ingredient ingredient = ingredients.get(index);

            if (ingredient == null || ingredient.isEmpty()) {
                continue;
            }

            ItemStack fallbackPreview = index < storedPreviews.size() ? storedPreviews.get(index) : ItemStack.EMPTY;
            IInput itemInput = createItemInput(ingredient, fallbackPreview);

            if (itemInput != null) {
                inputs.add(itemInput);
            }
        }

        IInput bloodInput = createBloodInput(getRequiredLifeEssence(recipe.getSyphon()));

        if (bloodInput != null) {
            inputs.add(bloodInput);
        }

        if (inputs.isEmpty()) {
            return Optional.empty();
        }

        /*
         * Время RecipeAlchemyTable#getTicks() специально не используем.
         * Алхимический стол теперь крафтится по общему времени сборщика из конфига.
         */
        return Optional.of(new BloodAltarPatternDetails(
                BloodMagicPatternKind.ALCHEMY_TABLE,
                BloodAltarPatternItem.getRecipeId(patternStack),
                definition,
                inputs.toArray(IInput[]::new),
                new GenericStack[]{output},
                recipe.getMinimumTier(),
                0
        ));
    }

    @Nullable
    private static AEItemKey createDefinition(ItemStack patternStack) {
        ItemStack definitionStack = patternStack.copy();
        definitionStack.setCount(1);

        return AEItemKey.of(definitionStack);
    }

    public static int getRequiredLifeEssence(AraVitaeRecipe recipe) {
        return getRequiredLifeEssence(recipe.getTotalBlood());
    }

    public static int getRequiredLifeEssence(TabulaVitaeRecipe recipe) {
        return getRequiredLifeEssence(recipe.getSyphon());
    }

    public static int getRequiredLifeEssence(int syphon) {
        int safeSyphon = Math.max(0, syphon);
        double multiplier = BMAddonCommonConfig.BLOOD_ALTAR_ASSEMBLER_LIFE_ESSENCE_MULTIPLIER.get();

        if (safeSyphon <= 0 || multiplier <= 0.0D) {
            return 0;
        }

        return Math.max(1, (int) Math.ceil(safeSyphon * multiplier));
    }

    @Nullable
    private static IInput createItemInput(Ingredient ingredient, ItemStack fallbackPreview) {
        List<GenericStack> possibleInputs = new ArrayList<>();

        for (ItemStack ingredientStack : ingredient.getItems()) {
            if (ingredientStack.isEmpty()) {
                continue;
            }

            ItemStack stack = ingredientStack.copy();
            stack.setCount(1);

            AEItemKey key = AEItemKey.of(stack);

            if (key != null) {
                possibleInputs.add(new GenericStack(key, 1));
            }
        }

        if (possibleInputs.isEmpty() && !fallbackPreview.isEmpty()) {
            ItemStack stack = fallbackPreview.copy();
            stack.setCount(1);

            AEItemKey key = AEItemKey.of(stack);

            if (key != null) {
                possibleInputs.add(new GenericStack(key, 1));
            }
        }

        if (possibleInputs.isEmpty()) {
            return null;
        }

        return new BloodMagicItemInput(ingredient, possibleInputs.toArray(GenericStack[]::new));
    }

    @Nullable
    private static IInput createBloodInput(int requiredLifeEssence) {
        if (requiredLifeEssence <= 0) {
            return null;
        }

        AEFluidKey key = AEFluidKey.of(BloodMagicFluidHelper.lifeEssenceFluid());

        return new BloodMagicFluidInput(new GenericStack(key, requiredLifeEssence));
    }

    @Nullable
    private static GenericStack createOutput(ItemStack outputStack) {
        ItemStack output = outputStack.copy();

        if (output.isEmpty()) {
            return null;
        }

        AEItemKey outputKey = AEItemKey.of(output);

        if (outputKey == null) {
            return null;
        }

        return new GenericStack(outputKey, output.getCount());
    }

    public BloodMagicPatternKind getKind() {
        return kind;
    }

    public ResourceLocation getRecipeId() {
        return recipeId;
    }

    public int getMinimumTier() {
        return minimumTier;
    }

    public int getBaseCraftTimeTicks() {
        return baseCraftTimeTicks;
    }

    @Override
    public AEItemKey getDefinition() {
        return definition;
    }

    @Override
    public IInput[] getInputs() {
        return inputs;
    }

    @Override
    public List<GenericStack> getOutputs() {
        return List.of(outputs);
    }

    @Override
    public boolean supportsPushInputsToExternalInventory() {
        return false;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof BloodAltarPatternDetails other)) {
            return false;
        }

        return kind == other.kind
                && minimumTier == other.minimumTier
                && baseCraftTimeTicks == other.baseCraftTimeTicks
                && Objects.equals(recipeId, other.recipeId)
                && Objects.equals(definition, other.definition)
                && Arrays.equals(inputs, other.inputs)
                && Arrays.equals(outputs, other.outputs);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(kind, recipeId, definition, minimumTier, baseCraftTimeTicks);
        result = 31 * result + Arrays.hashCode(inputs);
        result = 31 * result + Arrays.hashCode(outputs);
        return result;
    }

    private record BloodMagicItemInput(
            Ingredient ingredient,
            GenericStack[] possibleInputs
    ) implements IInput {
        @Override
        public GenericStack[] getPossibleInputs() {
            return possibleInputs;
        }

        @Override
        public long getMultiplier() {
            return 1;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            if (!(input instanceof AEItemKey itemKey)) {
                return false;
            }

            return ingredient.test(itemKey.toStack());
        }

        @Nullable
        @Override
        public AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }

    private record BloodMagicFluidInput(
            GenericStack bloodStack
    ) implements IInput {
        @Override
        public GenericStack[] getPossibleInputs() {
            return new GenericStack[]{bloodStack};
        }

        @Override
        public long getMultiplier() {
            return 1;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            return input.equals(bloodStack.what());
        }

        @Nullable
        @Override
        public AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }
}
