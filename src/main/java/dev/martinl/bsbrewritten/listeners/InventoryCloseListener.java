package dev.martinl.bsbrewritten.listeners;

import dev.martinl.bsbrewritten.BSBRewritten;
import dev.martinl.bsbrewritten.util.MaterialUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class InventoryCloseListener implements Listener {
    private final BSBRewritten instance;

    public InventoryCloseListener(BSBRewritten instance) {
        this.instance = instance;
        instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        Inventory inventory = e.getInventory();
        if(inventory.getType()!= InventoryType.SHULKER_BOX) return; //validate inventory type
        if(inventory.getHolder()!=null||e.getInventory().getLocation()!=null) return; //check that the shulker inventory is not a block inventory
        if(!instance.getShulkerManager().isShulkerInventory(inventory)) return; //check that the inventory belongs to BSB
        Bukkit.broadcastMessage("Is shulker inventory: " + instance.getShulkerManager().isShulkerInventory(inventory));
        instance.getShulkerManager().closeShulkerBox(player, inventory, Optional.empty());
    }

    //todo view-mode only
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent e) {
        ItemStack droppedItem = e.getItemDrop().getItemStack();
        if(!MaterialUtil.isShulkerBox(droppedItem.getType())) return; //check if the dropped item is a shulker box
        if(e.getPlayer().getOpenInventory().getType()!=InventoryType.SHULKER_BOX) return; //check if the open inventory is one from a shulker box
        if(e.getPlayer().getOpenInventory().getTopInventory().getLocation()!=null) return; //check if the shulker is a block
        if(!instance.getShulkerManager().doesPlayerHaveShulkerOpen(e.getPlayer().getUniqueId())) return; //check if the inventory belongs to BSB
        ItemStack corresponding = instance.getShulkerManager().getCorrespondingStack(e.getPlayer().getOpenInventory().getTopInventory());
        if(corresponding==null) {
            Bukkit.broadcastMessage("Corresponding is null!");
            return;
        } else if(!corresponding.equals(droppedItem)) {
            Bukkit.broadcastMessage("Corresponding is not equal to the dropped item");
            Bukkit.broadcastMessage(corresponding.getType().toString());
            return;
        }
        //Bukkit.broadcastMessage("Should work!");
        e.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if(e.getPlayer().getOpenInventory().getType()!=InventoryType.SHULKER_BOX) return; //check if the open inventory is one from a shulker box
        if(e.getPlayer().getOpenInventory().getTopInventory().getLocation()!=null) return; //check if the shulker is a block
        if(!instance.getShulkerManager().doesPlayerHaveShulkerOpen(e.getPlayer().getUniqueId())) return; //check if the inventory belongs to BSB
        if(e.getFrom().distance(e.getTo())>1) {
            instance.getShulkerManager().closeShulkerBox(e.getPlayer(), e.getPlayer().getOpenInventory().getTopInventory(), Optional.empty());
        }
    }

    @EventHandler
    public void onDamage() {

    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if(e.getPlayer().getOpenInventory().getType()!=InventoryType.SHULKER_BOX) return; //check if the open inventory is one from a shulker box
        if(e.getPlayer().getOpenInventory().getTopInventory().getLocation()!=null) return; //check if the shulker is a block
        if(!instance.getShulkerManager().doesPlayerHaveShulkerOpen(e.getPlayer().getUniqueId())) return; //check if the inventory belongs to BSB
        instance.getShulkerManager().closeShulkerBox(e.getPlayer(), e.getPlayer().getOpenInventory().getTopInventory(), Optional.empty());
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        Player player = (Player) e.getWhoClicked();

        if(player.getOpenInventory().getType()!=InventoryType.SHULKER_BOX) return; //check if the open inventory is one from a shulker box
        if(player.getOpenInventory().getTopInventory().getLocation()!=null) return; //check if the shulker is a block
        if(!instance.getShulkerManager().doesPlayerHaveShulkerOpen(player.getUniqueId())) return; //check if the inventory belongs to BSB

        /*
        *   This should prevent some kind of exploit in which a player closes an inventory
        *   locally, the packet is not sent, and then tries to dye the shulker to try and
        *   duplicate items
        *
        */
        if(MaterialUtil.isShulkerBox(e.getRecipe().getResult().getType())) {
            e.setCancelled(true);
        }
        instance.getShulkerManager().closeShulkerBox(player, player.getOpenInventory().getTopInventory(), Optional.empty());
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        Bukkit.broadcastMessage(e.getInventory().getType().toString() + " - " + e.getPlayer().getOpenInventory().getTopInventory().getType().toString());
    }
}
