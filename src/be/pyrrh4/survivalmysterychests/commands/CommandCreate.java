package be.pyrrh4.survivalmysterychests.commands;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import be.pyrrh4.core.command.Argument;
import be.pyrrh4.core.command.CallInfo;
import be.pyrrh4.core.messenger.Messenger;
import be.pyrrh4.core.messenger.Messenger.Level;
import be.pyrrh4.survivalmysterychests.SMC;

public class CommandCreate extends Argument
{
	// ------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------

	public CommandCreate(Argument parent, ArrayList<String> aliases, ArrayList<String> params, boolean playerOnly, boolean async, String permission, String description, ArrayList<String> paramsDescription) {
		super(parent, aliases, params, playerOnly, async, permission, description, paramsDescription);
	}

	// ------------------------------------------------------------
	// Override
	// ------------------------------------------------------------

	@Override
	public void perform(CallInfo call)
	{
		Player player = call.getSenderAsPlayer();
		String id = call.getArgAsString(1);

		if (!SMC.instance().getConfiguration().contains(id + ".settings.name"))
		{
			Messenger.send(player, Level.SEVERE_ERROR, "MysteryChests", "Invalid chest id.");
			return;
		}

		SMC.instance().getDefiners().put(player, id);
		Messenger.send(player, Level.NORMAL_INFO, "MysteryChests", "Right click on the chest to define it.");
	}
}
