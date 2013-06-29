package me.jascotty2.itemnames;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import me.jascotty2.lib.io.CheckInput;
import me.jascotty2.lib.io.FileIO;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

public class NamesChanger {

	final ItemNames plugin;
	private File configFile;
	private FileConfiguration config;
	protected HashMap<Integer, ItemSubNames> names = new HashMap<Integer, ItemSubNames>();

	public NamesChanger(ItemNames plugin) {
		this.plugin = plugin;
	}

	public void reloadConfig() {
		names.clear();
		if (configFile == null) {
			configFile = new File(plugin.getDataFolder(), "names.yml");
		}
		if (!configFile.exists()) {
			configFile.getParentFile().mkdirs();
			try {
				FileIO.extractResource("names.yml", configFile, ItemNames.class);
			} catch (Exception ex) {
				plugin.getLogger().log(Level.SEVERE, "Failed to extract default config", ex);
			}
		}
		try {
			config = YamlConfiguration.loadConfiguration(configFile);
			for (String key : config.getKeys(false)) {
				int id = CheckInput.GetInt(key, 0);
				if(id > 0) {
					Object o = config.get(key);
					if (o instanceof String) {
						names.put(id, new ItemSubNames((String) o));
					} // else if (o instanceof List) { // todo? random names?
					else if (o instanceof MemorySection) {
						MemorySection m = (MemorySection) o;
						ItemSubNames nms = new ItemSubNames();
						o = m.get("default");
						if(o instanceof String) {
							nms.name = (String) o;
						}
						for (String subkey : m.getKeys(false)) {
							short sub = CheckInput.GetShort(subkey, (short)0);
							if(sub >= 0 && (o = m.get(subkey)) instanceof String) {
								nms.addName(sub, (String) o);
							}
						}
						names.put(id, nms);
					}
				}
			}
		} catch (Exception ex) {
			plugin.getLogger().log(Level.SEVERE, "Failed to load Names config", ex);
		}
	}

	public FileConfiguration getConfig() {
		if (config == null) {
			reloadConfig();
		}
		return config;
	}

	public void saveConfig() {
		if (config != null && configFile != null) {
			try {
				getConfig().save(configFile);
			} catch (IOException ex) {
				plugin.getLogger().log(Level.SEVERE,
						"Could not save config to " + configFile, ex);
			}
		}
	}

	public void setName(Collection<ItemStack> items) {
		for (ItemStack its : items) {
			plugin.names.setName(its);
		}
	}

	public void setName(ItemStack[] items) {
		for (ItemStack its : items) {
			plugin.names.setName(its);
		}
	}
	
	public void setName(ItemStack items) {
		if(items != null && !hasNameSet(items) && names.containsKey(items.getTypeId())) {
			String custom = names.get(items.getTypeId()).getName(items.getDurability());
			if(custom != null) {
				ItemMeta m = items.getItemMeta().clone();
				m.setDisplayName(custom);
				items.setItemMeta(m);
			}
		}
	}

	public boolean hasNameAvailable(ItemStack item) {
		return item != null && names.containsKey(item.getTypeId())
				&& names.get(item.getTypeId()).getName(item.getDurability()) != null;
	}

	public boolean hasNameSet(ItemStack item) {
		return item != null && item.getItemMeta() != null && item.getItemMeta().hasDisplayName();
	}

	public void setCraftingRecipies() {
		Iterator serverRecipes = plugin.getServer().recipeIterator();
		ArrayList<Recipe> toRemove = new ArrayList<Recipe>();
		ArrayList<Recipe> keep = new ArrayList<Recipe>();
		while (serverRecipes.hasNext()) {
			Object rec = serverRecipes.next();
			if (rec instanceof ShapedRecipe || rec instanceof ShapelessRecipe) {
				toRemove.add((Recipe) rec);
			} else if (rec instanceof Recipe) {
				keep.add((Recipe) rec);
			}
		}
		plugin.getServer().clearRecipes();
		for (Recipe r : keep) {
			plugin.getServer().addRecipe(r);
		}
		for (Recipe rec : toRemove) {
			if (rec instanceof ShapedRecipe) {
				ShapedRecipe r = (ShapedRecipe) rec;
				ItemStack res = r.getResult();
				setName(res);
				ShapedRecipe newR = new ShapedRecipe(res);
				newR.shape(r.getShape());
				Map<Character, ItemStack> craft = r.getIngredientMap();
				for (Map.Entry<Character, ItemStack> e : craft.entrySet()) {
					if (e.getValue() != null) {
						newR.setIngredient(e.getKey(), e.getValue().getType(), e.getValue().getDurability());
					}
				}
				plugin.getServer().addRecipe(newR);
			} else if (rec instanceof ShapelessRecipe) {
				ShapelessRecipe r = (ShapelessRecipe) rec;
				ItemStack res = r.getResult();
				setName(res);
				ShapelessRecipe newR = new ShapelessRecipe(res);
				List<ItemStack> craft = r.getIngredientList();
				for (ItemStack it : craft) {
					if (it != null) {
						newR.addIngredient(it.getAmount(), it.getType(), it.getDurability());
					}
				}
				plugin.getServer().addRecipe(newR);
			}
		}
	}

	static class ItemSubNames {

		public String name = null;
		HashMap<Short, String> names = null;

		ItemSubNames() {
		}

		public ItemSubNames(String name) {
			this.name = name;
		}

		public void addName(short data, String name) {
			if (names == null) {
				names = new HashMap<Short, String>();
			}
			names.put(data, name);
		}

		public String getName(short data) {
			if (names != null && names.containsKey(data)) {
				return names.get(data);
			}
			return name;
		}
	}
}
