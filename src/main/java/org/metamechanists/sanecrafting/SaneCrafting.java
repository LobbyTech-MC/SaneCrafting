package org.metamechanists.sanecrafting;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import lombok.Getter;
import lombok.NonNull;
import net.guizhanss.guizhanlibplugin.updater.GuizhanUpdater;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.metamechanists.sanecrafting.patches.CraftingTablePatch;
import org.metamechanists.sanecrafting.patches.RecipeBookResearchPatch;
import org.metamechanists.sanecrafting.patches.RecipeLorePatch;
import org.metamechanists.sanecrafting.patches.UsableInWorkbenchPatch;

import java.util.logging.Level;


public final class SaneCrafting extends JavaPlugin implements SlimefunAddon {
    private static final int BSTATS_ID = 22737;
    @Getter
    private static SaneCrafting instance;

    @Override
    public void onEnable() {
        instance = this;

        if (!getServer().getPluginManager().isPluginEnabled("GuizhanLibPlugin")) {
            getLogger().log(Level.SEVERE, "本插件需要 鬼斩前置库插件(GuizhanLibPlugin) 才能运行!");
            getLogger().log(Level.SEVERE, "从此处下载: https://50L.cc/gzlib");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (getConfig().getBoolean("auto-update") && getPluginVersion().startsWith("Dev")) {
            GuizhanUpdater.start(this, getFile(), "SlimefunGuguProject", "SaneCrafting", "master");
        }

        new Metrics(this, BSTATS_ID);

        // Patches applied on first tick to ensure everything has loaded
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            UsableInWorkbenchPatch.apply();
            CraftingTablePatch.apply();
            RecipeBookResearchPatch.apply();
            RecipeLorePatch.apply();
        }, 1);

    }

    @Override
    public void onDisable() {

    }

    @NonNull
    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Nullable
    @Override
    public String getBugTrackerURL() {
        return null;
    }
}
