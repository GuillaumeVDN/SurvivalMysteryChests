package be.pyrrh4.smc.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import be.pyrrh4.core.Core;
import be.pyrrh4.core.lib.messenger.Replacer;
import be.pyrrh4.core.util.UString;
import be.pyrrh4.smc.SMC;
import be.pyrrh4.smc.misc.InventoryData;

public class PlayerInteract implements Listener
{
	@EventHandler
	public void onExecute(PlayerInteractEvent event)
	{
		Action action = event.getAction();
		Player player = event.getPlayer();

		if (action.equals(Action.RIGHT_CLICK_BLOCK))
		{
			Block block = event.getClickedBlock();
			Location location = block.getLocation();

			if (SMC.i.definers.containsKey(player))
			{
				event.setCancelled(true);

				if (SMC.i.chestManager.getChestId(location) != null)
				{
					Core.getMessenger().error(player, "MysteryChests >>", "This chest is already registered !");
					return;
				}

				String chestId = SMC.i.definers.get(player);

				SMC.i.chestManager.registerChest(chestId, location);
				SMC.i.definers.remove(player);

				Core.getMessenger().normal(player, "MysteryChests >>", "The chest has been successfully created !");
			}
			else
			{
				if (SMC.i.chestManager.getChestId(location) != null)
				{
					event.setCancelled(true);

					if (SMC.i.inventoryManager.hasInventory(player, location))
						player.openInventory(SMC.i.inventoryManager.getInventory(player, location));

					else
					{
						String chestId = SMC.i.chestManager.getChestId(location);

						if (!SMC.i.cooldownManager.checkCooldown(player, chestId))
							return;

						Inventory inventory = SMC.i.inventoryManager.getEmptyChoiceInventory(chestId, player);

						// On vérifie la condition

						String price = UString.format("chests." + SMC.i.config.getLast().getString(chestId + ".settings.price"));

						if (price.contains("money "))
						{
							double amount = Double.parseDouble(price.replace("money ", ""));
							double bank = SMC.vault.getBalance(player);

							if (amount > bank)
							{
								SMC.i.getMessage("chest-price").send(new Replacer("{object}", SMC.vault.format(amount)), player);
								return;
							}
							else
							{
								SMC.i.getMessage("chest-pay").send(new Replacer("{object}", SMC.vault.format(amount), "{name}", UString.format(SMC.i.config.getLast().getString("chests." + chestId + ".settings.name"))), player);
								SMC.vault.withdrawPlayer(player, amount);
							}
						}
						else if (price.contains("key "))
						{
							String keyId = price.replace("key ", "");
							String keyItemName = UString.format(SMC.i.config.getLast().getString("keys." + keyId + ".name"));
							String brut = SMC.i.config.getLast().getString("keys." + keyId + ".item");
							Material keyItemType = Material.getMaterial(brut.split(" ")[0]);
							boolean payed = false;

							for (int i = 0; i < player.getInventory().getSize(); i++)
							{
								ItemStack item = player.getInventory().getContents()[i];

								if (item == null || !(item.hasItemMeta()) || (item.hasItemMeta() && !(item.getItemMeta().hasDisplayName())))
									continue;

								if (item.getType().equals(keyItemType) && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals(ChatColor.stripColor(keyItemName)))
								{
									int amount = item.getAmount();

									if (amount > 1)
									{
										item.setAmount(amount - 1);
										player.getInventory().setItem(i, item);
									}
									else
									{
										player.getInventory().setItem(i, null);
									}

									payed = true;
									player.updateInventory();

									break;
								}
							}

							if (payed == false)
							{
								SMC.i.getMessage("chest-price").send(new Replacer("{object}", keyItemName), player);
								return;
							}

							SMC.i.getMessage("chest-pay").send(new Replacer("{object}", keyItemName, "{name}", UString.format(SMC.i.config.getLast().getString("chests." + chestId + ".settings.name"))), player);
							player.updateInventory();
						}

						// On ouvre l'inventaire

						player.openInventory(inventory);

						// On ajoute l'inventaire à la liste

						SMC.i.inventoryManager.inventories.add(new InventoryData(chestId, inventory, location));
					}
				}
			}
		}
	}
}
