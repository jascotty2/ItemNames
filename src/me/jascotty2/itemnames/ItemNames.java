
package me.jascotty2.itemnames;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemNames extends JavaPlugin {

	Listeners listener = new Listeners(this);
	NamesChanger names = new NamesChanger(this);
	
	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(listener, this);
		names.reloadConfig();
		names.setCraftingRecipies();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if(sender instanceof Player) {
			if(!((Player) sender).hasPermission("itemnames.admin")) {
				sender.sendMessage(ChatColor.RED + "You do not have access to this!");
			}
		}
		if(args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			names.reloadConfig();
			sender.sendMessage(ChatColor.AQUA + "Names Reloaded from file");
		} else {
			sender.sendMessage(ChatColor.YELLOW + "Incomplete or invalid command");
			sender.sendMessage(ChatColor.YELLOW + "Usage: /" + commandLabel + " reload  #Reloads Names from file");
		}
		//todo? add methods to add names ingame
		//todo? method to name all current ingame items (in chests, inventories, etc)
		
		return true;
	}
	
}
