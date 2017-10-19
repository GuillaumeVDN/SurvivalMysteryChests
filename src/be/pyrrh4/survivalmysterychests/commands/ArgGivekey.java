package be.pyrrh4.survivalmysterychests.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import be.pyrrh4.core.command.Arguments.Performer;
import be.pyrrh4.core.command.CallInfo;
import be.pyrrh4.core.messenger.Messenger;
import be.pyrrh4.core.messenger.Messenger.Level;
import be.pyrrh4.survivalmysterychests.SMC;

public class ArgGivekey implements Performer
{
	@Override
	public void perform(CallInfo call)
	{
		Player player = call.getSenderAsPlayer();
		Player target = call.getArgAsPlayer(1);
		String keyId = call.getArgAsString(2);

		if (!SMC.instance().getConfiguration().contains("keys." + keyId + ".name"))
		{
			Messenger.send(player, Level.SEVERE_ERROR, "MysteryChests", "Invalid key id.");
			return;
		}

		ItemStack keyItem = SMC.instance().getConfiguration().getItem("keys." + keyId).getItem();
		target.getInventory().addItem(keyItem);
		target.updateInventory();

		String keyName = SMC.instance().getConfiguration().getStringFormatted("keys." + keyId + ".name");
		Messenger.send(player, Level.NORMAL_SUCCESS, "MysteryChests", "You gave a " + keyName + " key to " + target.getName() + " !");
		SMC.instance().getLocale().getMessage("key-receive").send(target, "$NAME", keyName);
	}
}