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

		// On lance la première tâche de 4 secondes

		// TODO : 3 tasks, what a genius ;-; change this
		new BukkitRunnable()
		{
			int remaining = 20 * 4;

			@Override
			public void run()
			{
				remaining -= 2;

				if (remaining <= 0)
				{
					// On lance la deuxième tâche de 3 secondes

					new BukkitRunnable()
					{
						int remaining = 20 * 3;

						@Override
						public void run()
						{
							remaining -= 5;

							if (remaining <= 0)
							{
								// On lance la troisième tâche de 2 secondes

								new BukkitRunnable()
								{
									int remaining = 20 * 2;

									@Override
									public void run()
									{
										remaining -= 8;

										if (remaining <= 0)
										{
											// On roll une dernière fois

											roll(player, inventory, id);
											finish(inventoryData);

											// On cancel la troisième tâche

											cancel();
										}

										// On roll

										roll(player, inventory, id);
									}
								}.runTaskTimer(SMC.instance(), 0L, 8L);

								// On cancel la deuxi§me tâche

								cancel();
							}

							// On roll

							roll(player, inventory, id);
						}
					}.runTaskTimer(SMC.instance(), 0L, 5L);

					// On cancel la première tâche

					cancel();
				}

				// On roll

				roll(player, inventory, id);
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
