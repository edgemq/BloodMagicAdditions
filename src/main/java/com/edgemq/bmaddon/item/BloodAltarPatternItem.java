package com.edgemq.bmaddon.item;

import com.edgemq.bmaddon.ae2.BloodMagicPatternKind;
import com.edgemq.bmaddon.util.BloodAltarRecipeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.recipe.alchemy_table.AlchemyTableRecipe;
import wayoftime.bloodmagic.common.recipe.bloodaltar.BloodAltarRecipe;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BloodAltarPatternItem extends Item {
    private static final String TAG_RECIPE_KIND = "RecipeKind";
    private static final String TAG_RECIPE_ID = "RecipeId";
    private static final String TAG_INPUT_PREVIEW = "InputPreview";
    private static final String TAG_INPUT_PREVIEWS = "InputPreviews";
    private static final String TAG_OUTPUT_PREVIEW = "OutputPreview";
    private static final String TAG_MINIMUM_TIER = "MinimumTier";
    private static final String TAG_SYPHON = "Syphon";
    private static final String TAG_CONSUME_RATE = "ConsumeRate";
    private static final String TAG_DRAIN_RATE = "DrainRate";

    /*
     * Старый тег оставляем только для очистки старых шаблонов.
     * Новые шаблоны его больше не пишут и код его больше не использует.
     */
    private static final String TAG_CRAFT_TIME_TICKS = "CraftTimeTicks";

    public BloodAltarPatternItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        if (isEncoded(stack)) {
            return 1;
        }

        return super.getMaxStackSize(stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        if (!isEncoded(stack)) {
            player.displayClientMessage(
                    Component.translatable("message.bmaddon.blood_altar_pattern.already_empty"),
                    true
            );

            return InteractionResultHolder.fail(stack);
        }

        clear(stack);

        player.displayClientMessage(
                Component.translatable("message.bmaddon.blood_altar_pattern.cleared"),
                true
        );

        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public Component getName(ItemStack stack) {
        if (isEncoded(stack)) {
            ItemStack outputPreview = getOutputPreview(stack);

            if (!outputPreview.isEmpty()) {
                return Component.translatable(
                        "item.bmaddon.blood_altar_pattern.encoded",
                        outputPreview.getHoverName()
                );
            }

            return Component.translatable("item.bmaddon.blood_altar_pattern.encoded_unknown");
        }

        return super.getName(stack);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        if (!isEncoded(stack)) {
            tooltip.add(Component.translatable("tooltip.bmaddon.blood_altar_pattern.empty").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.bmaddon.blood_altar_pattern.how_to_encode").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        ResourceLocation recipeId = getRecipeId(stack);

        if (recipeId == null) {
            tooltip.add(Component.translatable("tooltip.bmaddon.blood_altar_pattern.invalid").withStyle(ChatFormatting.RED));
            return;
        }

        BloodMagicPatternKind kind = getRecipeKind(stack);
        List<ItemStack> inputPreviews = getInputPreviews(stack);
        Level level = null;
        ItemStack outputPreview = getOutputPreview(stack);

        tooltip.add(Component.translatable(
                "tooltip.bmaddon.blood_altar_pattern.type",
                Component.translatable(getRecipeKindTranslationKey(kind))
        ).withStyle(ChatFormatting.GRAY));

        if (inputPreviews.size() == 1) {
            tooltip.add(Component.translatable(
                    "tooltip.bmaddon.blood_altar_pattern.input",
                    inputPreviews.get(0).getHoverName()
            ).withStyle(ChatFormatting.GRAY));
        } else {
            for (int index = 0; index < inputPreviews.size(); index++) {
                ItemStack input = inputPreviews.get(index);

                if (!input.isEmpty()) {
                    tooltip.add(Component.translatable(
                            "tooltip.bmaddon.blood_altar_pattern.input_indexed",
                            index + 1,
                            input.getHoverName()
                    ).withStyle(ChatFormatting.GRAY));
                }
            }
        }

        if (!outputPreview.isEmpty()) {
            tooltip.add(Component.translatable(
                    "tooltip.bmaddon.blood_altar_pattern.output",
                    outputPreview.getHoverName()
            ).withStyle(ChatFormatting.GRAY));
        }

        tooltip.add(Component.translatable(
                "tooltip.bmaddon.blood_altar_pattern.tier",
                getStoredAltarTierForDisplay(stack)
        ).withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable(
                "tooltip.bmaddon.blood_altar_pattern.life_essence",
                getStoredSyphon(stack)
        ).withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable("tooltip.bmaddon.blood_altar_pattern.shift_clear").withStyle(ChatFormatting.DARK_GRAY));

    }

    private static String getRecipeKindTranslationKey(BloodMagicPatternKind kind) {
        return switch (kind) {
            case BLOOD_ALTAR -> "tooltip.bmaddon.blood_altar_pattern.type.blood_altar";
            case ALCHEMY_TABLE -> "tooltip.bmaddon.blood_altar_pattern.type.alchemy_table";
        };
    }

    private static ItemStack getResolvedOutputPreview(
            ItemStack stack,
            @Nullable Level level,
            BloodMagicPatternKind kind,
            ResourceLocation recipeId
    ) {
        if (level != null) {
            ItemStack recipeOutput = BloodAltarRecipeHelper.getOutputPreview(level, kind, recipeId);

            if (!recipeOutput.isEmpty()) {
                return recipeOutput;
            }
        }

        return getOutputPreview(stack);
    }

    public static boolean isEncoded(ItemStack stack) {
        CompoundTag tag = getCustomTag(stack);

        return tag != null
                && tag.contains(TAG_RECIPE_ID)
                && BloodAltarRecipeHelper.parseRecipeId(tag.getString(TAG_RECIPE_ID)) != null;
    }

    public static BloodMagicPatternKind getRecipeKind(ItemStack stack) {
        CompoundTag tag = getCustomTag(stack);

        if (tag == null || !tag.contains(TAG_RECIPE_KIND)) {
            return BloodMagicPatternKind.BLOOD_ALTAR;
        }

        return BloodMagicPatternKind.byName(tag.getString(TAG_RECIPE_KIND));
    }

    @Nullable
    public static ResourceLocation getRecipeId(ItemStack stack) {
        CompoundTag tag = getCustomTag(stack);

        if (tag == null || !tag.contains(TAG_RECIPE_ID)) {
            return null;
        }

        return BloodAltarRecipeHelper.parseRecipeId(tag.getString(TAG_RECIPE_ID));
    }

    public static ItemStack getInputPreview(ItemStack stack) {
        List<ItemStack> previews = getInputPreviews(stack);

        if (previews.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return previews.get(0);
    }

    public static List<ItemStack> getInputPreviews(ItemStack stack) {
        CompoundTag tag = getCustomTag(stack);

        if (tag == null) {
            return List.of();
        }

        if (tag.contains(TAG_INPUT_PREVIEWS, Tag.TAG_LIST)) {
            ListTag listTag = tag.getList(TAG_INPUT_PREVIEWS, Tag.TAG_COMPOUND);
            List<ItemStack> inputs = new ArrayList<>();

            for (int index = 0; index < listTag.size(); index++) {
                ItemStack input = ItemStack.EMPTY;

                if (!input.isEmpty()) {
                    inputs.add(input);
                }
            }

            return inputs;
        }

        if (tag.contains(TAG_INPUT_PREVIEW, Tag.TAG_COMPOUND)) {
            ItemStack oldInput = ItemStack.EMPTY;

            if (!oldInput.isEmpty()) {
                return List.of(oldInput);
            }
        }

        return List.of();
    }

    public static ItemStack getOutputPreview(ItemStack stack) {
        CompoundTag tag = getCustomTag(stack);

        if (tag == null || !tag.contains(TAG_OUTPUT_PREVIEW)) {
            return ItemStack.EMPTY;
        }

        return ItemStack.EMPTY;
    }

    public static int getStoredMinimumTier(ItemStack stack) {
        CompoundTag tag = getCustomTag(stack);

        if (tag == null) {
            return 0;
        }

        return tag.getInt(TAG_MINIMUM_TIER);
    }

    public static int getStoredAltarTierForDisplay(ItemStack stack) {
        return getStoredMinimumTier(stack) + 1;
    }

    public static int getStoredSyphon(ItemStack stack) {
        CompoundTag tag = getCustomTag(stack);

        if (tag == null) {
            return 0;
        }

        return tag.getInt(TAG_SYPHON);
    }

    public static int getStoredConsumeRate(ItemStack stack) {
        CompoundTag tag = getCustomTag(stack);

        if (tag == null) {
            return 0;
        }

        return tag.getInt(TAG_CONSUME_RATE);
    }

    public static int getStoredDrainRate(ItemStack stack) {
        CompoundTag tag = getCustomTag(stack);

        if (tag == null) {
            return 0;
        }

        return tag.getInt(TAG_DRAIN_RATE);
    }

    public static void encode(ItemStack patternStack, ResourceLocation recipeId, BloodAltarRecipe recipe, ItemStack inputPreview) {
        ItemStack storedInputPreview = inputPreview.copy();
        storedInputPreview.setCount(1);

        encodeInternal(
                patternStack,
                BloodMagicPatternKind.BLOOD_ALTAR,
                recipeId,
                List.of(storedInputPreview),
                recipe.getResult().copy(),
                recipe.getMinTier(),
                recipe.getTotalBlood(),
                recipe.getCraftSpeed(),
                recipe.getDrainSpeed()
        );
    }

    public static void encode(ItemStack patternStack, ResourceLocation recipeId, AlchemyTableRecipe recipe, List<ItemStack> inputPreviews) {
        List<ItemStack> storedInputs = new ArrayList<>();

        for (ItemStack input : inputPreviews) {
            if (input.isEmpty()) {
                continue;
            }

            ItemStack copy = input.copy();
            copy.setCount(1);
            storedInputs.add(copy);
        }

        encodeInternal(
                patternStack,
                BloodMagicPatternKind.ALCHEMY_TABLE,
                recipeId,
                storedInputs,
                recipe.output().copy(),
                recipe.tier(),
                recipe.essence(),
                0,
                0
        );
    }

    private static void encodeInternal(
            ItemStack patternStack,
            BloodMagicPatternKind kind,
            ResourceLocation recipeId,
            List<ItemStack> inputPreviews,
            ItemStack outputPreview,
            int minimumTier,
            int syphon,
            int consumeRate,
            int drainRate
    ) {
        CompoundTag tag = getOrCreateCustomTag(patternStack);

        ListTag inputList = new ListTag();

        for (ItemStack inputPreview : inputPreviews) {
            if (!inputPreview.isEmpty()) {
            }
        }

        tag.putString(TAG_RECIPE_KIND, kind.getSerializedName());
        tag.putString(TAG_RECIPE_ID, recipeId.toString());
        tag.put(TAG_INPUT_PREVIEWS, inputList);

        if (!inputPreviews.isEmpty()) {
        }

        tag.putInt(TAG_MINIMUM_TIER, minimumTier);
        tag.putInt(TAG_SYPHON, syphon);
        tag.putInt(TAG_CONSUME_RATE, consumeRate);
        tag.putInt(TAG_DRAIN_RATE, drainRate);
        tag.remove(TAG_CRAFT_TIME_TICKS);
        setCustomTag(patternStack, tag);
    }

    public static void clear(ItemStack stack) {
        stack.remove(DataComponents.CUSTOM_DATA);
    }

    @Nullable
    private static CompoundTag getCustomTag(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData == null ? null : customData.copyTag();
    }

    private static CompoundTag getOrCreateCustomTag(ItemStack stack) {
        CompoundTag tag = getCustomTag(stack);
        return tag == null ? new CompoundTag() : tag;
    }

    private static void setCustomTag(ItemStack stack, CompoundTag tag) {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
            return;
        }

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
