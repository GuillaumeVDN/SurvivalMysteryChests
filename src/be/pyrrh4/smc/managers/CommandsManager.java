package be.pyrrh4.smc.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import be.pyrrh4.core.storage.PMLReader;
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
		PMLReader reader = SMC.i.config;
		List<String> commands = new ArrayList<String>();

		// On parcourt la liste des actions

		for (String key : reader.getKeysForSection("chests." + chestId + ".wins", false))
		{
			String brut = reader.getString("chests." + chestId + ".wins." + key + ".item");

			Material type = Material.getMaterial(brut.split(" ")[0]);
			String name = reader.getString("chests." + chestId + ".wins." + key + ".name");
			List<String> lore = reader.getListOfString("chests." + chestId + ".wins." + key + ".lore");

			if (name != null)
				name = UString.format(name);

			if (lore != null)
				lore = UString.format(lore);

			if (item.getType().equals(type)
					&& (item.getItemMeta().hasDisplayName() && name != null  ? item.getItemMeta().getDisplayName().equals(name) : true)
					&& (item.getItemMeta().hasLore() && lore != null ? item.getItemMeta().getLore().equals(lore) : true))
			{
				if (reader.contains(reader.getString("chests." + chestId + ".wins." + key + ".commands"))) {
					commands = reader.getListOfString("chests." + chestId + ".wins." + key + ".commands");
				}
			}
		}

		return commands;
	}
}
