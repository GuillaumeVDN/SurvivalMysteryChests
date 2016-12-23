package be.pyrrh4.smc.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import be.pyrrh4.core.util.UString;
import be.pyrrh4.smc.SMC;

public class CommandsManager
{
	public void executeCommands(ItemStack item, String chestId, Player player)
	{
		List<String> commands = getCommands(item, chestId);

		for (String command : commands)
		{
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.getName()));
		}
	}

	public List<String> getCommands(ItemStack item, String chestId)
	{
		ConfigurationSection ConfigurationSection = SMC.i.config.getLast().getConfigurationSection("chests." + chestId + ".wins");
		List<String> commands = new ArrayList<String>();

		// On parcourt la liste des actions

		for (Entry<String, Object> entry : ConfigurationSection.getValues(false).entrySet())
		{
			if (!(entry.getValue() instanceof MemorySection))
				continue;

			MemorySection memorySection = (MemorySection) entry.getValue();
			String brut = memorySection.getString("item");

			Material type = Material.getMaterial(brut.split(" ")[0]);
			String name = memorySection.getString("name");
			List<String> lore = memorySection.getStringList("name");

			if (name != null)
				name = UString.format(name);

			if (lore != null)
				lore = UString.format(lore);

			if (item.getType().equals(type)
					&& (item.getItemMeta().hasDisplayName() && name != null  ? item.getItemMeta().getDisplayName().equals(name) : true)
					&& (item.getItemMeta().hasLore() && lore != null ? item.getItemMeta().getLore().equals(lore) : true))
			{
				if (memorySection.contains("commands"));
				commands = memorySection.getStringList("commands");
			}
		}

		return commands;
	}
}
