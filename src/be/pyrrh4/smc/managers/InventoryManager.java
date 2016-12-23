package be.pyrrh4.smc.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import be.pyrrh4.core.util.UString;
import be.pyrrh4.core.util.collection.ItemBuilder;
import be.pyrrh4.smc.SMC;
import be.pyrrh4.smc.misc.InventoryData;

public class InventoryManager
{
	public Random random = new Random();
	public HashMap<String, HashMap<String, ItemStack>> items;
	public ArrayList<InventoryData> inventories;
	public ItemStack selectedItem;
	public ItemStack notSelectedItem;

	public InventoryManager()
	{
		random = new Random();
		items = new HashMap<String, HashMap<String, ItemStack>>();
		inventories = new ArrayList<InventoryData>();
		selectedItem = ItemBuilder.fromConfig(SMC.i.config.getLast(), "items.selected").build();
		notSelectedItem = ItemBuilder.fromConfig(SMC.i.config.getLast(), "items.not-selected").build();

		loadItems();
	}

	@SuppressWarnings("deprecation")
	public void loadItems()
	{
		Bukkit.getLogger().info("Loading items...");
		ConfigurationSection cs = SMC.i.config.getLast().getConfigurationSection("");

		if (cs == null) {
			return;
		}

		for (Entry<String, Object> e : cs.getValues(false).entrySet())
		{
			String chestId = e.getKey();
			ConfigurationSection configurationSection = SMC.i.config.getLast().getConfigurationSection("chests." + chestId + ".wins");
			HashMap<String, ItemStack> items = new HashMap<String, ItemStack>();

			if (configurationSection == null)
				return;

			for (int i = 0; i < 50; i++)
			{
				// On parcourt la liste des récompenses

				for (Entry<String, Object> entry : configurationSection.getValues(false).entrySet())
				{
					String id = entry.getKey();
					MemorySection memorySection = (MemorySection) entry.getValue();
					int chance = memorySection.getInt("chance");

					if (random.nextInt(100) > chance)
					{
						String brut = memorySection.getString("item");
						Material type = Material.getMaterial(brut.split(" ")[0]);
						int data = Integer.parseInt(brut.split(" ")[1]);
						String name = SMC.i.config.getLast().getString(memorySection.getCurrentPath() + ".name");
						List<String> lore = SMC.i.config.getLast().getStringList(memorySection.getCurrentPath() + ".lore");
						ItemStack item;

						if (name != null)
							name = UString.format(name);

						if (lore != null)
							lore = UString.format(lore);

						if (name == null)
						{
							item = new ItemStack(type, 1, (short) 0, (byte) data);

							if (lore != null)
							{
								ItemMeta meta = item.getItemMeta();
								meta.setLore(lore);
								item.setItemMeta(meta);
							}
						}
						else
							item = new ItemBuilder(type, (byte) data, 1).setName(name).addLore(lore.toArray(new String[lore.size()])).build();

						if (memorySection.contains("enchantments"))
						{
							for (String enchantmentBrut : memorySection.getStringList("enchantments"))
							{
								Enchantment enchantment = Enchantment.getByName(enchantmentBrut.split(" ")[0]);
								int level = Integer.parseInt(enchantmentBrut.split(" ")[1]);

								item.addUnsafeEnchantment(enchantment, level);
							}
						}

						items.put(id, item);
					}
				}
			}

			this.items.put(chestId, items);
		}

		Bukkit.getLogger().info("Items loaded.");
	}

	// Mise à jour de l'inventaire avec un nouveau choix

	public void updateInventory(Inventory inventory, int newChoice)
	{
		InventoryData inventoryData = getInventoryData(inventory);

		// On ajoute le choix à la liste

		inventoryData.addChoice(newChoice);

		// On change l'item dans l'inventaire

		inventory.setItem(newChoice, selectedItem);
	}

	// R§cup§ration d'un nouvel inventaire de choix vide

	public Inventory getEmptyChoiceInventory(String id, Player player)
	{
		String name = UString.format(SMC.i.config.getLast().getString("chests." + id + ".settings.name"));
		int size = SMC.i.config.getLast().getInt("chests." + id + ".settings.size");
		Inventory inventory = Bukkit.createInventory(player, size, name);

		// On ajoute les items

		for (int i = 0; i < size; i++)
		{
			inventory.setItem(i, notSelectedItem);
		}

		return inventory;
	}

	// Mise à jour de l'inventaire avec un item aléatoire dans chaque case

	public void roll(Inventory inventory, String chestId)
	{
		for (int i = 0; i < inventory.getSize(); i++)
			inventory.setItem(i, getRandomReward(chestId));
	}

	public ItemStack getRandomReward(String chestId)
	{
		HashMap<String, ItemStack> items = this.items.get(chestId);

		if (items == null)
			return new ItemStack(Material.BEDROCK, 0);

		int size = (items.size() > 0 ? items.size() : 1);
		int rnd = random.nextInt(size);
		ItemStack itemStack = null;
		String rewardId = null;
		int i = 0;

		for (Entry<String, ItemStack> entry : items.entrySet())
		{
			String id = entry.getKey();
			ItemStack item = entry.getValue();

			if (i >= rnd)
			{
				rewardId = id;
				itemStack = item;
				break;
			}

			i++;
		}

		String brutAmount = SMC.i.config.getLast().getString("chests." + chestId + ".wins." + rewardId + ".item").split(" ")[2];
		int amount = 0;

		if (brutAmount.contains("random"))
		{
			brutAmount = brutAmount.replace("random{", "").replace("}", "");
			int min = Integer.parseInt(brutAmount.split(",")[0]);
			int max = Integer.parseInt(brutAmount.split(",")[1]);

			for (i = 0; i < 200; i++)
			{
				amount = random.nextInt(max) + 1;

				if (amount >= min)
					break;
			}
		}
		else
		{
			amount = Integer.parseInt(brutAmount);
		}

		itemStack.setAmount(amount);
		return itemStack;
	}

	// On r§cup§re les param§tres d'une chest

	public InventoryData getInventoryData(Inventory inventory)
	{
		for (InventoryData inventoryData : inventories)
		{
			if (inventoryData.getInventory().equals(inventory))
			{
				return inventoryData;
			}
		}

		return null;
	}

	public InventoryData getInventoryData(Player player)
	{
		for (InventoryData inventoryData : inventories)
		{
			if (inventoryData.getPlayer().getUniqueId().equals(player.getUniqueId()))
			{
				return inventoryData;
			}
		}

		return null;
	}

	public InventoryData getInventoryData(Player player, Location location)
	{
		for (InventoryData inventoryData : inventories)
		{
			if (inventoryData.getBlockLocation().equals(location) && inventoryData.getPlayer().getUniqueId().equals(player.getUniqueId()))
				return inventoryData;
		}

		return null;
	}

	// On récupère l'inventaire de chest actuel du joueur

	public Inventory getInventory(Player player, Location location)
	{
		InventoryData inventoryData = getInventoryData(player, location);
		Inventory inventory = inventoryData.getInventory();

		return inventory;
	}

	// On vérifie si le joueur a un inventaire de chest

	public boolean hasInventory(Player player, Location location)
	{
		for (InventoryData inventoryData : inventories)
		{
			if (inventoryData.getBlockLocation().equals(location) && inventoryData.getPlayer().getUniqueId().equals(player.getUniqueId()))
			{
				return true;
			}
		}

		return false;
	}
}