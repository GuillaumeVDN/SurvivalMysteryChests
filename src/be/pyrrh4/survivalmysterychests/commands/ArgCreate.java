package be.pyrrh4.survivalmysterychests.commands;

import org.bukkit.entity.Player;

import be.pyrrh4.core.Perm;
import be.pyrrh4.core.command.CommandArgument;
import be.pyrrh4.core.command.CommandCall;
import be.pyrrh4.core.command.Param;
import be.pyrrh4.core.messenger.Messenger;
import be.pyrrh4.core.messenger.Messenger.Level;
import be.pyrrh4.core.util.Utils;
import be.pyrrh4.survivalmysterychests.SMC;

public class ArgCreate extends CommandArgument {

	private static final Param paramChest = new Param(Utils.asList("chest"), "id", null, true);

	public ArgCreate() {
		super(SMC.instance(), Utils.asList("create", "new"), "create a chest", Perm.SURVIVALMYSTERYCHESTS_ADMIN, true, paramChest);
	}

	@Override
	public void perform(CommandCall call) {
		Player player = call.getSenderAsPlayer();
		String id = paramChest.getString(call);
		if (id != null) {
			if (!SMC.instance().getConfiguration().contains(id)) {
				Messenger.send(player, Level.SEVERE_ERROR, "MysteryChests", "Invalid chest id.");// TODO : locale for this
				return;
			}
			SMC.instance().getDefiners().put(player, id);
			Messenger.send(player, Level.NORMAL_INFO, "MysteryChests", "Right click on the chest to define it.");
		}
	}

}
