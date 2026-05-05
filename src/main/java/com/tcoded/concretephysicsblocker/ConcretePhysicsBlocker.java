package com.tcoded.concretephysicsblocker;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class ConcretePhysicsBlocker extends JavaPlugin implements Listener {

    private final List<Material> concreteBlocks = new ArrayList<>();

    private boolean preventWaterTransform = true;
    private boolean preventGravity = true;

    @Override
    public void onEnable() {
        // Config
        saveDefaultConfig();
        preventWaterTransform = getConfig().getBoolean("prevent-water-transform");
        preventGravity = getConfig().getBoolean("prevent-gravity");

        for (Material material : Material.values()) {
            if (material.name().endsWith("CONCRETE_POWDER")) {
                concreteBlocks.add(material);
            }
        }

        getServer().getPluginManager().registerEvents(this, this);

    }

    @EventHandler
    public void onConcretePhysics(EntityChangeBlockEvent event) {
        if (!preventGravity) return;
        if (!concreteBlocks.contains(event.getBlock().getType())) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onConcreteHarden(BlockFormEvent event) {
        if (!preventWaterTransform) return;
        if (!concreteBlocks.contains(event.getBlock().getType())) return;

        event.setCancelled(true);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(((Plugin) this));
    }
}
