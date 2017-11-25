package be.pyrrh4.survivalmysterychests.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import be.pyrrh4.core.compat.sound.Sound;
import be.pyrrh4.survivalmysterychests.SMC;
import be.pyrrh4.survivalmysterychests.misc.InventoryData;

public class RollManager
{
	public void launchRoll(final InventoryData inventoryData)
	{
		final Inventory inv = inventoryData.getInventory();
		final Player player = inventoryData.getPlayer();
		final String id = inventoryData.getId();
		final Inventory inventory = Bukkit.createInventory(player, inv.getSize(), SMC.instance().getLocale().getMessage("inventory-rolling").getLines().get(0));

		// On actualise l'inventaire

		inventory.setContents(inv.getContents());
		inventoryData.setInventory(inventory);
		player.openInventory(inventory);

		// On actualise les paramètres

		inventoryData.setRolling(true);

		// On lance la tâche

		new BukkitRunnable()
		{
			private long left = 220L, interval = 2L, current = 0L;

			public void run()
			{
				// checking time left
				left -= 20L;

				// end
				if (left == 0L) {
					// last roll
					roll(player, inventory, id);
					finish(inventoryData);
					cancel();
					return;
				}

				// changing interval
				if (left == 120L) {
					interval = 3L;
				} else if (left == 80L) {
					interval = 5L;
				} else if (left == 40L) {
					interval = 8L;
				}

				// updating current ticks
				current += 2L;
				if (current == interval) {
					// roll and reset
					current = 0L;
					roll(player, inventory, id);
				}
			}
		}.runTaskTimer(SMC.instance(), 0L, 2L);
	}

	private void roll(Player player, Inventory inventory, String id)
	{
		// on joue un son
		Sound.valueOf(SMC.instance().getConfiguration().getString("sounds.roll")).play(player);
		// on affiche les items
		SMC.instance().getInventoryManager().roll(inventory, id);
	}

	private void finish(InventoryData inventoryData)
	{
		Player player = inventoryData.getPlayer();
		Inventory inv = inventoryData.getInventory();
		Inventory inventory = Bukkit.createInventory(player, inv.getSize(), SMC.instance().getLocale().getMessage("inventory-finished").getLines().get(0));

		// On actualise l'inventaire

		for (int slot : inventoryData.getChoices())
			inventory.setItem(slot, inv.getContents()[slot]);

		inventoryData.setInventory(inventory);
		player.openInventory(inventory);

		// On actualise les param§tres

		inventoryData.setRolling(false);
		inventoryData.setFinished(true);

		// On joue un son

		Sound.valueOf(SMC.instance().getConfiguration().getString("sounds.finished")).play(player);
	}
}
