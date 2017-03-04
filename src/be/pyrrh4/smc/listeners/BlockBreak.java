package be.pyrrh4.smc.listeners;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import be.pyrrh4.core.Core;
import be.pyrrh4.smc.SMC;

public class BlockBreak implements Listener
{
	@EventHandler
	public void onExecute(BlockBreakEvent event)
	{
		Block block = event.getBlock();
		Location location = block.getLocation();
		String path = SMC.i.chestManager.getChestPath(location);
		Player player = event.getPlayer();

		if (path != null)
		{
			SMC.i.database.set(path + ".location", null);
			SMC.i.database.set(path + ".id", null);
			SMC.i.database.set(path, null).save();
			Core.getMessenger().normal(player, "MysteryChests >>", "The chest has been removed !");
		}
	}
}
