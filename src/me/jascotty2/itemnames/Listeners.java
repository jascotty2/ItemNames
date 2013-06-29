package me.jascotty2.itemnames;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class Listeners implements Listener {

	final ItemNames plugin;

	public Listeners(ItemNames plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST) 
	public void onItemDrop(EntityDeathEvent event) {
		plugin.names.setName(event.getDrops());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST) 
	public void onSmelt(FurnaceSmeltEvent event) {
		plugin.names.setName(event.getResult());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST) 
	public void onCreate(InventoryCreativeEvent event) {
		plugin.names.setName(event.getCursor());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST) 
	public void onOpen(InventoryOpenEvent event) {
		plugin.names.setName(event.getInventory().getContents());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST) 
	public void onInventory(InventoryClickEvent event) {
		plugin.names.setName(event.getCurrentItem());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST) 
	public void onInventory(InventoryPickupItemEvent event) {
		plugin.names.setName(event.getItem().getItemStack());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST) 
	public void onInventory(PlayerPickupItemEvent event) {
		plugin.names.setName(event.getItem().getItemStack());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST) 
	public void onBucket(PlayerBucketFillEvent event) {
		plugin.names.setName(event.getItemStack());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST) 
	public void onBucket(PlayerBucketEmptyEvent event) {
		plugin.names.setName(event.getItemStack());
	}
	
	// fires /before/ brew potion conversion
//	@EventHandler(priority = EventPriority.HIGHEST) 
//	public void onBrew(BrewEvent event) {
//		BrewerInventory inv = event.getContents();
//		plugin.names.setName(inv.getContents());
//		
//	}
}
