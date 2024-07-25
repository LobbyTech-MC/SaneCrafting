package org.metamechanists.sanecrafting;

import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum;
import com.github.houbb.pinyin.util.PinyinHelper;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class Util {
    private Util() {}

    // Technically could lead to clashes if two shaped recipes for same item but... hopefully not...
    public static @NotNull String generateRecipeId(@NotNull ItemStack output) {
        var sfItem = SlimefunItem.getByItem(output);
        String normalisedName;
        if (sfItem != null) {
            normalisedName = sfItem.getId();
        } else {
            normalisedName = PlainTextComponentSerializer.plainText().serialize(output.displayName());
            normalisedName = PinyinHelper.toPinyin(normalisedName, PinyinStyleEnum.NUM_LAST) + "_" + normalisedName.hashCode();
        }
        normalisedName = normalisedName.toLowerCase()
            .replace(' ', '_')
            .replaceAll("[^a-z0-9/._\\-]", ""); // remove characters not allowed in id
        return "sanecrafting_" + normalisedName;
    }

    public static @Nullable <T extends SlimefunItem> T findMultiblock(Class<T> clazz) {
        for (SlimefunItem item : Slimefun.getRegistry().getEnabledSlimefunItems()) {
            if (clazz.isInstance(item)) {
                return clazz.cast(item);
            }
        }

        SaneCrafting.getInstance().getLogger().severe("无法初始化 SaneCrafting：EnhancedCraftingTable 不存在");
        return null;
    }
}
