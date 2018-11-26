package be.pyrrh4.survivalmysterychests.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import be.pyrrh4.core.economy.EconomyHandler;
import be.pyrrh4.core.material.Mat;
import be.pyrrh4.core.messenger.Locale;
import be.pyrrh4.core.messenger.Messenger;
import be.pyrrh4.core.messenger.Messenger.Level;
import be.pyrrh4.core.util.Utils;
import be.pyrrh4.survivalmysterychests.Chest;
import be.pyrrh4.survivalmysterychests.SMC;
import be.pyrrh4.survivalmysterychests.misc.InventoryData;

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

			if (SMC.instance().getDefiners().containsKey(player))
			{
				event.setCancelled(true);
				String chestId = SMC.instance().getDefiners().get(player);
				SMC.instance().getDefiners().remove(player);

				if (SMC.instance().getChest(location) != null)
				{
					Messenger.send(player, Level.SEVERE_INFO, "MysteryChests", "This chest is already registered !");
					return;
				}

				SMC.instance().registerChest(chestId, location);
				Messenger.send(player, Level.NORMAL_SUCCESS, "MysteryChests", "The chest has been successfully created !");
			}
			else
			{
				Chest chest = SMC.instance().getChest(location);

				if (chest != null)
				{
					event.setCancelled(true);

					if (SMC.instance().getInventoryManager().hasInventory(player, location)) {
						player.openInventory(SMC.instance().getInventoryManager().getInventory(player, location));
					}
					else
					{
						if (chest.hasCooldown(player)) {
							return;
						}

						Inventory inventory = SMC.instance().getInventoryManager().getEmptyChoiceInventory(chest.getPath(), player);

						// On vérifie la condition

						String price = Utils.format("chests." + SMC.instance().getConfiguration().getString(chest.getPath() + ".settings.price"));

						if (price.contains("money "))
						{
							double amount = Double.parseDouble(price.replace("money ", ""));
							double bank = EconomyHandler.INSTANCE.get(player);

							if (amount > bank)
							{
								Locale.MSG_SURVIVALMYSTERYCHESTS_CHESTPRICE.getActive().send(player, "{object}", EconomyHandler.INSTANCE.format(amount));
								return;
							}
							else
							{
								Locale.MSG_SURVIVALMYSTERYCHESTS_CHESTPAY.getActive().send(player, "{object}", EconomyHandler.INSTANCE.format(amount), "{name}", SMC.instance().getConfiguration().getStringFormatted("chests." + chest.getPath() + ".settings.name"));
								EconomyHandler.INSTANCE.take(player, amount);
							}
						}
						else if (price.contains("key "))
						{
							String keyId = price.replace("key ", "");
							String keyItemName = SMC.instance().getConfiguration().getStringFormatted("keys." + keyId + ".name");
							String brut = SMC.instance().getConfiguration().getString("keys." + keyId + ".item");
							Mat keyItemType = Mat.from(brut, 0);
							boolean payed = false;

							for (int i = 0; i < player.getInventory().getSize(); i++)
							{
								ItemStack item = player.getInventory().getContents()[i];

								if (item == null || !(item.hasItemMeta()) || (item.hasItemMeta() && !(item.getItemMeta().hasDisplayName())))
									continue;

								if (keyItemType.isMat(item) && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals(ChatColor.stripColor(keyItemName)))
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
								Locale.MSG_SURVIVALMYSTERYCHESTS_CHESTPRICE.getActive().send(player, "{object}", keyItemName);
								return;
							}

							Locale.MSG_SURVIVALMYSTERYCHESTS_CHESTPAY.getActive().send(player, "{object}", keyItemName, "{name}", SMC.instance().getConfiguration().getStringFormatted("chests." + chest.getPath() + ".settings.name"));
							player.updateInventory();
						}

						// On ouvre l'inventaire

						player.openInventory(inventory);

						// On ajoute l'inventaire à la liste

						SMC.instance().getInventoryManager().getInventories().add(new InventoryData(chest.getPath(), inventory, location));
					}
				}
			}
		}
	}
}
