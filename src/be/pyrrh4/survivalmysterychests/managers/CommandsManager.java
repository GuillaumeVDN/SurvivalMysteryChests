package be.pyrrh4.survivalmysterychests.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

import be.pyrrh4.core.storage.YMLConfiguration;
import be.pyrrh4.core.util.Utils;
import be.pyrrh4.survivalmysterychests.SMC;

public class CommandsManager
{
	public void executeCommands(ItemStack item, String chestId, Player player)
	{
		ArrayList<String> commands = getCommands(item, chestId);

		for (String command : commands) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("$PLAYER", player.getName()));
		}
	}

	public ArrayList<String> getCommands(ItemStack item, String chestId)
	{
		YMLConfiguration config = SMC.instance().getConfiguration();
		ArrayList<String> commands = new ArrayList<String>();

		// On parcourt la liste des actions

		for (String key : config.getKeysForSection("chests." + chestId + ".wins", false))
		{
			String brut = config.getString("chests." + chestId + ".wins." + key + ".item");

			Material type = Material.getMaterial(brut.split(" ")[0]);
			String name = config.getString("chests." + chestId + ".wins." + key + ".name");
			ArrayList<String> lore = config.getList("chests." + chestId + ".wins." + key + ".lore");

			if (name != null) {
				name = Utils.format(name);
			}

			if (lore != null) {
				lore = Utils.format(lore);
			}

			if (item.getType().equals(type)
					&& (item.getItemMeta().hasDisplayName() && name != null  ? item.getItemMeta().getDisplayName().equals(name) : true)
					&& (item.getItemMeta().hasLore() && lore != null ? item.getItemMeta().getLore().equals(lore) : true))
			{
				if (config.contains(config.getString("chests." + chestId + ".wins." + key + ".commands"))) {
					commands = config.getList("chests." + chestId + ".wins." + key + ".commands");
				}
			}
		}

		return commands;
	}
}
