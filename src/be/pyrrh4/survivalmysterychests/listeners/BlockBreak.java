package be.pyrrh4.survivalmysterychests.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import be.pyrrh4.core.messenger.Messenger;
import be.pyrrh4.core.messenger.Messenger.Level;
import be.pyrrh4.survivalmysterychests.Chest;
import be.pyrrh4.survivalmysterychests.SMC;

public class BlockBreak implements Listener
{
	@EventHandler
	public void event(BlockBreakEvent event)
	{
		Chest chest = SMC.instance().getChest(event.getBlock().getLocation());

		if (chest != null) {
			SMC.instance().unregisterChest(chest);
			Messenger.send(event.getPlayer(), Level.NORMAL_SUCCESS, "MysteryChests", "The chest has been removed !");
		}
	}
}
