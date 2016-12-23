package be.pyrrh4.smc.listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import be.pyrrh4.smc.SMC;
import be.pyrrh4.smc.misc.InventoryData;

public class InventoryClick implements Listener
{
	@EventHandler
	public void onExecute(InventoryClickEvent event)
	{
		Inventory inventory = event.getInventory();
		Player player = (Player) event.getWhoClicked();
		InventoryData inventoryData = SMC.i.inventoryManager.getInventoryData(player);
		int slot = event.getSlot();

		if (slot < 0 || slot > 54) {
			return;
		}

		try
		{
			ItemStack item = inventory.getContents()[slot];

			if (item == null) {
				return;
			}

			if (inventoryData == null) {
				return;
			}

			if (inventoryData.getInventory().equals(inventory))
			{
				event.setCancelled(true);

				if (inventoryData.isFinished())
				{
					if (inventoryData.getChoices().contains(slot))
					{
						// On donne l'item au joueur

						player.getInventory().addItem(item);
						player.updateInventory();

						// On actualise l'inventaire

						inventory.setItem(slot, null);

						// On exécute les éventuelles commandes

						SMC.i.commandsManager.executeCommands(item, inventoryData.getId(), player);

						// On joue un son

						player.playSound(player.getLocation(),
								Sound.valueOf(SMC.i.config.getLast().getString("sounds.reward.sound")),
								Float.valueOf(SMC.i.config.getLast().getString("sounds.reward.volume")),
								Float.valueOf(SMC.i.config.getLast().getString("sounds.reward.pitch")));

						// On supprime le slot

						inventoryData.removeChoice(slot);

						// On finalise tout si besoin

						if (inventoryData.getChoices().isEmpty())
						{
							inventoryData.remove();
							player.closeInventory();
						}
					}
				}
				else if (!inventoryData.isRolling())
				{
					if (inventoryData.getChoices().contains(slot)) {
						return;
					}

					SMC.i.inventoryManager.updateInventory(inventory, slot);

					// On joue un son

					player.playSound(player.getLocation(),
							Sound.valueOf(SMC.i.config.getLast().getString("sounds.select.sound")),
							Float.valueOf(SMC.i.config.getLast().getString("sounds.select.volume")),
							Float.valueOf(SMC.i.config.getLast().getString("sounds.select.pitch")));

					// On vérifie s'il ne faut pas lancer la roulette

					if (inventoryData.getChoices().size() >= SMC.i.config.getLast().getInt(inventoryData.getId() + ".settings.choice")){
						SMC.i.rollManager.launchRoll(inventoryData);
					}
				}
			}
		}
		catch(Exception ignored) {}
	}
}
