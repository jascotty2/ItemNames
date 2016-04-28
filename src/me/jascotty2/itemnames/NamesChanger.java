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
import org.bukkit.ChatColor;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

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
				if (id > 0) {
					Object o = config.get(key);
					if (o instanceof String) {
						names.put(id, new ItemSubNames(ChatColor.translateAlternateColorCodes('&', (String) o)));
					} // else if (o instanceof List) { // todo? random names?
					else if (o instanceof MemorySection) {
						ItemSubNames nms = loadSection(o);
						if (nms == null) {
							MemorySection m = (MemorySection) o;
							o = m.get("default");

							nms = loadSection(o);
							if (nms == null) {
								nms = new ItemSubNames();
							}

							for (String subkey : m.getKeys(false)) {
								short sub = CheckInput.GetShort(subkey, (short) 0);
								if (sub >= 0) {
									o = m.get(subkey);
									ItemSubNames t = loadSection(o);
									if (t != null) {
										if (t.name != null) {
											nms.addName(sub, t.name);
										}
										if (t.lore != null) {
											nms.addLore(sub, t.lore);
										}
									}
								}
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

	ItemSubNames loadSection(Object o) {
		if (o instanceof String) {
			return new ItemSubNames(ChatColor.translateAlternateColorCodes('&', (String) o));
		} else if (o instanceof MemorySection) {
			final MemorySection md = (MemorySection) o;
			if (md.isString("name") || md.isList("lore")) {
				final String name = md.getString("name");
				final ItemSubNames nm = new ItemSubNames(name == null ? null : ChatColor.translateAlternateColorCodes('&', name));
				if (md.isList("lore")) {
					final ArrayList<String> lore = new ArrayList<String>();
					for (Object l : md.getList("lore")) {
						lore.add(ChatColor.translateAlternateColorCodes('&', l.toString()));
					}
					nm.lore = lore;
				}
				return nm;
			}
		}
		return null;
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
		if (items != null && !hasNameSet(items) && names.containsKey(items.getTypeId())) {
			String custom = names.get(items.getTypeId()).getName(items.getDurability());
			List<String> lore = names.get(items.getTypeId()).getLore(items.getDurability());
			if (custom != null || lore != null) {
				ItemMeta m = items.getItemMeta().clone();
				if (custom != null) {
					m.setDisplayName(custom);
				}
				if (lore != null) {
					m.setLore(lore);
				}
				items.setItemMeta(m);
			}
		}
	}

	public boolean hasNameAvailable(ItemStack item) {
		return item != null && names.containsKey(item.getTypeId())
				&& names.get(item.getTypeId()).getName(item.getDurability()) != null;
	}

	public boolean hasNameSet(ItemStack item) {
		final ItemMeta meta;
		return item != null && (meta = item.getItemMeta()) != null
				&& meta.hasDisplayName() && (!plugin.renameCustom || ((meta instanceof Repairable) && ((Repairable) meta).hasRepairCost()));
	}

	public void setCraftingRecipes() {
		Iterator<Recipe> serverRecipes = plugin.getServer().recipeIterator();
		ArrayList<Recipe> temp = new ArrayList<Recipe>();
		while (serverRecipes.hasNext()) {
			Recipe rec = serverRecipes.next();
			if(hasNameAvailable(rec.getResult())) {
				temp.add(getNamedRecipeCopy(rec));
			} else {
			//	temp.add(rec);
			}
		}
		// stained leather is a special recipe that can't be copied, and clear deletes it
		// ...
		//plugin.getServer().clearRecipes();
		for (Recipe r : temp) {
			plugin.getServer().addRecipe(r);
		}
	}

	Recipe getNamedRecipeCopy(Recipe rec) {
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
			return newR;
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
			return newR;
		}
		return rec;
	}
/*
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
*/
	static class ItemSubNames {

		public String name = null;
		public List<String> lore = null;
		HashMap<Short, String> names = null;
		HashMap<Short, List<String>> lores = null;

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

		public void addLore(short data, List<String> lore) {
			if (lores == null) {
				lores = new HashMap<Short, List<String>>();
			}
			lores.put(data, lore);
		}

		public String getName(short data) {
			if (names != null && names.containsKey(data)) {
				return names.get(data);
			}
			return name;
		}

		public List<String> getLore(short data) {
			if (lores != null && lores.containsKey(data)) {
				return lores.get(data);
			}
			return lore;
		}
	}
}
