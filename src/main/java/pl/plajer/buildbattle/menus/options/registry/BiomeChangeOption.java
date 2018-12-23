/*
 * BuildBattle 4 - Ultimate building competition minigame
 * Copyright (C) 2018  Plajer's Lair - maintained by Plajer and Tigerpanzer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.plajer.buildbattle.menus.options.registry;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import pl.plajer.buildbattle.arena.Arena;
import pl.plajer.buildbattle.arena.ArenaRegistry;
import pl.plajer.buildbattle.arena.plots.Plot;
import pl.plajer.buildbattle.handlers.ChatManager;
import pl.plajer.buildbattle.menus.options.MenuOption;
import pl.plajer.buildbattle.menus.options.OptionsRegistry;
import pl.plajer.buildbattle.utils.Utils;
import pl.plajerlair.core.services.exception.ReportedException;
import pl.plajerlair.core.utils.ItemBuilder;
import pl.plajerlair.core.utils.MinigameUtils;
import pl.plajerlair.core.utils.XMaterial;

/**
 * @author Plajer
 * <p>
 * Created at 23.12.2018
 */
public class BiomeChangeOption {

  public BiomeChangeOption(OptionsRegistry registry) {
    registry.registerOption(new MenuOption(32, "BIOME", new ItemBuilder(XMaterial.MYCELIUM.parseItem())
        .name(ChatManager.colorMessage("Menus.Option-Menu.Items.Biome.Item-Name"))
        .lore(ChatManager.colorMessage("Menus.Option-Menu.Items.Biome.Item-Lore"))
        .build(), ChatManager.colorMessage("Menus.Option-Menu.Items.Biome.Inventory-Name")) {

      @Override
      public void onClick(InventoryClickEvent e) {
        e.getWhoClicked().closeInventory();
        Inventory biomeInv = Bukkit.createInventory(null, MinigameUtils.serializeInt(Biome.values().length), ChatManager.colorMessage("Menus.Option-Menu.Items.Biome.Inventory-Name"));
        for (Biome biome : Biome.values()) {
          biomeInv.addItem(new ItemBuilder(new ItemStack(Material.GRASS)).name(ChatManager.colorRawMessage("&6" + biome.name())).build());
        }
        e.getWhoClicked().openInventory(biomeInv);
      }

      @Override
      public void onTargetClick(InventoryClickEvent e) {
        Arena arena = ArenaRegistry.getArena((Player) e.getWhoClicked());
        if (arena == null) {
          return;
        }
        Plot plot = arena.getPlotManager().getPlot((Player) e.getWhoClicked());
        try {
          for (Block block : plot.getCuboid().blockList()) {
            block.setBiome(Biome.valueOf(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName())));
          }
          for (Chunk chunk : plot.getCuboid().chunkList()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
              Utils.sendPacket(p, Utils.getNMSClass("PacketPlayOutMapChunk").getConstructor(Utils.getNMSClass("Chunk"), int.class)
                  .newInstance(chunk.getClass().getMethod("getHandle").invoke(chunk), 65535));
            }
          }
          for (UUID owner : plot.getOwners()) {
            Player p = Bukkit.getPlayer(owner);
            if (p == null) {
              continue;
            }
            p.sendMessage(ChatManager.getPrefix() + ChatManager.colorMessage("Menus.Option-Menu.Items.Biome.Biome-Set"));
          }
        } catch (Exception ex) {
          new ReportedException(registry.getPlugin(), ex);
        }
      }
    });
  }

}