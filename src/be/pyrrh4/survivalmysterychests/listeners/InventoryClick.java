package be.pyrrh4.survivalmysterychests.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import be.pyrrh4.core.compat.sound.Sound;
import be.pyrrh4.survivalmysterychests.SMC;
import be.pyrrh4.survivalmysterychests.misc.InventoryData;

public class InventoryClick implements Listener
{
	@EventHandler
	public void onExecute(InventoryClickEvent event)
	{
		Inventory inventory = event.getInventory();
		Player player = (Player) event.getWhoClicked();
		InventoryData inventoryData = SMC.instance().getInventoryManager().getInventoryData(player);
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

						SMC.instance().getCommandsManager().executeCommands(item, inventoryData.getId(), player);

						// On joue un son

						Sound.valueOf(SMC.instance().getConfiguration().getString("sounds.reward")).play(player);

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

					SMC.instance().getInventoryManager().updateInventory(inventory, slot);

					// On joue un son

					Sound.valueOf(SMC.instance().getConfiguration().getString("sounds.select")).play(player);

					// On vérifie s'il ne faut pas lancer la roulette

					if (inventoryData.getChoices().size() >= SMC.instance().getConfiguration().getInt(inventoryData.getId() + ".settings.choice")){
						SMC.instance().getRollManager().launchRoll(inventoryData);
					}
				}
			}
		}
		catch(Exception ignored) {}
	}
}
