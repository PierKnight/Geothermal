package net.pier.geoe.gui;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

public record MenuContext<T extends BlockEntity>(int windowId, Inventory inv, T be){}