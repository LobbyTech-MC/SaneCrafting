package org.metamechanists.sanecrafting.patches;

import io.github.thebusybiscuit.slimefun4.implementation.items.multiblocks.EnhancedCraftingTable;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.Nullable;
import org.metamechanists.sanecrafting.SaneCrafting;
import org.metamechanists.sanecrafting.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import static org.metamechanists.sanecrafting.Util.generateRecipeId;


@UtilityClass
public class CraftingTablePatch {
    @Nullable
    private List<ItemStack[]> getRecipes() {
        EnhancedCraftingTable enhancedCraftingTable = Util.findMultiblock(EnhancedCraftingTable.class);
        if (enhancedCraftingTable == null) {
            return null;
        }
        return enhancedCraftingTable.getRecipes();
    }

    private void convertRecipe(List<ItemStack> input, ItemStack output) {
        // Remove recipe if already registered
        NamespacedKey key = new NamespacedKey(SaneCrafting.getInstance(), generateRecipeId(output));
        if (Bukkit.getServer().getRecipe(key) != null) {
            Bukkit.getServer().removeRecipe(key);
        }

        // Convert to shape
        String itemCharacters = "abcdefghi";
        List<String> shape = new ArrayList<>(List.of("abc", "def", "ghi"));
        Map<Character, ItemStack> ingredients = new HashMap<>();
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int i = y*3 + x;
                char character = itemCharacters.charAt(i);
                ItemStack itemStack = null;
                if (i < input.size()) {
                	itemStack = input.get(i);
                }
                if (itemStack == null) {
                    shape.set(y, shape.get(y).replace(character, ' '));
                } else {
                    ingredients.put(character, itemStack);
                }
            }
        }

        // Trim vertical
        for (int y = shape.size() - 1; y >= 0; y--) {
            if (Objects.equals(shape.get(y), "   ")) {
                shape.remove(y);
            }
        }

        // Skip if no recipe (just in case)
        if (shape.isEmpty()) {
            return;
        }

        // Trim horizontal
        for (int x = shape.get(0).length() - 1; x >= 0; x--) {
            boolean allRowsEmptyAtX = true;
            for (int y = shape.size() - 1; y >= 0; y--) {
                if (shape.get(y).charAt(x) != ' ') {
                    allRowsEmptyAtX = false;
                    break;
                }
            }
            if (allRowsEmptyAtX) {
                for (int y = shape.size() - 1; y >= 0; y--) {
                    String newRow = new StringBuilder(shape.get(y))
                            .deleteCharAt(x)
                            .toString();
                    shape.set(y, newRow);
                }
            }
        }

        ShapedRecipe recipe = new ShapedRecipe(key, output);
        recipe.shape(shape.toArray(new String[]{}));
        for (Entry<Character, ItemStack> entry : ingredients.entrySet()) {
            recipe.setIngredient(entry.getKey(), entry.getValue());
        }
        Bukkit.getServer().addRecipe(recipe);
    }

    public void apply() {
        List<ItemStack[]> recipes = getRecipes();
        if (recipes == null) {
            return;
        }

        int changedRecipes = 0;
        for (int j = 0; j < recipes.size(); j += 2) {
            ItemStack[] input = recipes.get(j);
            ItemStack output = recipes.get(j + 1)[0];

            try {
                convertRecipe(Arrays.asList(input), output);
            } catch (RuntimeException e) {
                String name = PlainTextComponentSerializer.plainText().serialize(output.displayName());
                SaneCrafting.getInstance().getLogger().severe("无法为该物品转换增强型工作台配方：" + name);
                e.printStackTrace();
                continue;
            }

            changedRecipes++;
        }

        SaneCrafting.getInstance().getLogger().info("已应用工作台补丁，转换了 " + changedRecipes + " 个增强型工作台配方到原版工作台配方。");
    }
}
