package be.pyrrh4.smc.managers;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import be.pyrrh4.smc.SMC;
import be.pyrrh4.smc.misc.InventoryData;

public class RollManager
{
	public void launchRoll(final InventoryData inventoryData)
	{
		final Inventory inv = inventoryData.getInventory();
		final Player player = inventoryData.getPlayer();
		final String id = inventoryData.getId();
		final Inventory inventory = Bukkit.createInventory(player, inv.getSize(), SMC.i.config.getMessage("inventory-rolling").getLines(null).get(0));

		// On actualise l'inventaire

		inventory.setContents(inv.getContents());
		inventoryData.setInventory(inventory);
		player.openInventory(inventory);

		// On actualise les paramètres

		inventoryData.setRolling(true);

		// On lance la première tâche de 4 secondes

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
								}.runTaskTimer(SMC.i, 0L, 8L);

								// On cancel la deuxi§me tâche

								cancel();
							}

							// On roll

							roll(player, inventory, id);
						}
					}.runTaskTimer(SMC.i, 0L, 5L);

					// On cancel la première tâche

					cancel();
				}

				// On roll

				roll(player, inventory, id);
			}
		}.runTaskTimer(SMC.i, 0L, 2L);
	}

	private void roll(Player player, Inventory inventory, String id)
	{
		// On joue un son

		player.playSound(player.getLocation(),
				Sound.valueOf(SMC.i.config.getString("sounds.roll.sound")),
				Float.valueOf(SMC.i.config.getString("sounds.roll.volume")),
				Float.valueOf(SMC.i.config.getString("sounds.roll.pitch")));

		// On affiche les items

		SMC.i.inventoryManager.roll(inventory, id);
	}

	private void finish(InventoryData inventoryData)
	{
		Player player = inventoryData.getPlayer();
		Inventory inv = inventoryData.getInventory();
		Inventory inventory = Bukkit.createInventory(player, inv.getSize(), SMC.i.config.getMessage("inventory-finished").getLines(null).get(0));

		// On actualise l'inventaire

		for (int slot : inventoryData.getChoices())
			inventory.setItem(slot, inv.getContents()[slot]);

		inventoryData.setInventory(inventory);
		player.openInventory(inventory);

		// On actualise les param§tres

		inventoryData.setRolling(false);
		inventoryData.setFinished(true);

		// On joue un son

		player.playSound(player.getLocation(),
				Sound.valueOf(SMC.i.config.getString("sounds.finished.sound")),
				Float.valueOf(SMC.i.config.getString("sounds.finished.volume")),
				Float.valueOf(SMC.i.config.getString("sounds.finished.pitch")));
	}
}
