package be.pyrrh4.survivalmysterychests.commands;

import org.bukkit.entity.Player;

import be.pyrrh4.core.command.CommandCall;
import be.pyrrh4.core.command.CommandPattern;
import be.pyrrh4.core.messenger.Messenger;
import be.pyrrh4.core.messenger.Messenger.Level;
import be.pyrrh4.survivalmysterychests.SMC;

public class ArgCreate extends CommandPattern {

	public ArgCreate() {
		super("create [string]%chest_id", "create a chest", "smc.chest.create", true);
	}

	@Override
	public void perform(CommandCall call) {
		Player player = call.getSenderAsPlayer();
		String id = call.getArgAsString(this, 1);
		if (!SMC.instance().getConfiguration().contains(id + ".settings.name")) {
			Messenger.send(player, Level.SEVERE_ERROR, "MysteryChests", "Invalid chest id.");
			return;
		}
		SMC.instance().getDefiners().put(player, id);
		Messenger.send(player, Level.NORMAL_INFO, "MysteryChests", "Right click on the chest to define it.");
	}

}
