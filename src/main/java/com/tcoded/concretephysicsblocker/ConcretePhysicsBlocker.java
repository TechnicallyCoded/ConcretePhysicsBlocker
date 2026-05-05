package com.tcoded.concretephysicsblocker;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public final class ConcretePhysicsBlocker extends JavaPlugin implements Listener {

    private final Set<Material> concreteBlocks = EnumSet.noneOf(Material.class);
    private final Set<String> enabledWorlds = new HashSet<>();

    private boolean preventWaterTransform = true;
    private boolean blockSand = true;
    private boolean blockGravel = true;
    private boolean blockConcrete = true;
    private boolean blockOtherGravity = true;

    @Override
    public void onEnable() {
        // Config
        saveDefaultConfig();
        preventWaterTransform = getConfig().getBoolean("prevent-water-transform", true);
        blockSand = getConfig().getBoolean("block-sand", true);
        blockGravel = getConfig().getBoolean("block-gravel", true);
        blockConcrete = getConfig().getBoolean("block-concrete", true);
        blockOtherGravity = getConfig().getBoolean("block-other-gravity",
                getConfig().getBoolean("block-other-falling", true));

        for (Material material : Material.values()) {
            if (material.name().endsWith("CONCRETE_POWDER")) {
                concreteBlocks.add(material);
            }
        }

        computeEnabledWorlds();

        getServer().getPluginManager().registerEvents(this, this);

    }

    @EventHandler
    public void onConcretePhysics(EntityChangeBlockEvent event) {
        if (!isEnabledWorld(event.getBlock().getWorld())) return;
        if (!shouldBlockGravity(event.getBlock().getType())) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (!isEnabledWorld(event.getBlock().getWorld())) return;
        if (!shouldBlockGravity(event.getBlock().getType())) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onConcreteHarden(BlockFormEvent event) {
        if (!preventWaterTransform) return;
        if (!blockConcrete) return;
        if (!isEnabledWorld(event.getBlock().getWorld())) return;
        if (!concreteBlocks.contains(event.getBlock().getType())) return;

        event.setCancelled(true);
    }

    private void computeEnabledWorlds() {
        enabledWorlds.clear();

        Set<String> whitelist = toLowercaseSet(getConfig().getStringList("worlds.whitelist"));
        Set<String> blacklist = toLowercaseSet(getConfig().getStringList("worlds.blacklist"));

        for (World world : getServer().getWorlds()) {
            String worldName = world.getName().toLowerCase();
            if (blacklist.contains(worldName)) continue;
            if (!whitelist.isEmpty() && !whitelist.contains(worldName)) continue;

            enabledWorlds.add(worldName);
        }
    }

    private Set<String> toLowercaseSet(Iterable<String> values) {
        Set<String> set = new HashSet<>();
        for (String value : values) {
            if (value == null) continue;

            String normalized = value.trim().toLowerCase();
            if (normalized.isEmpty()) continue;

            set.add(normalized);
        }
        return set;
    }

    private boolean isEnabledWorld(World world) {
        return enabledWorlds.contains(world.getName().toLowerCase());
    }

    private boolean shouldBlockGravity(Material material) {
        if (blockConcrete && concreteBlocks.contains(material)) return true;
        if (blockSand && isSand(material)) return true;
        if (blockGravel && material == Material.GRAVEL) return true;
        return blockOtherGravity && material.hasGravity();
    }

    private boolean isSand(Material material) {
        return material == Material.SAND || material == Material.RED_SAND;
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(((Plugin) this));
    }
}
