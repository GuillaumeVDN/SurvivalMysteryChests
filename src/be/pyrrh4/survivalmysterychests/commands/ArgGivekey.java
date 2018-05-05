package be.pyrrh4.survivalmysterychests.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import be.pyrrh4.core.command.CommandCall;
import be.pyrrh4.core.command.CommandPattern;
import be.pyrrh4.core.messenger.Messenger;
import be.pyrrh4.core.messenger.Messenger.Level;
import be.pyrrh4.survivalmysterychests.SMC;

public class ArgGivekey extends CommandPattern {

	public ArgGivekey() {
		super("givekey [player]%player [string]%chest_id", "give a key to a player", "smc.key.give", false);
	}

	@Override
	public void perform(CommandCall call) {
		CommandSender player = call.getSender();
		Player target = call.getArgAsPlayer(this, 1);
		String keyId = call.getArgAsString(this, 2);
		if (!SMC.instance().getConfiguration().contains("keys." + keyId + ".name")) {
			Messenger.send(player, Level.SEVERE_ERROR, "MysteryChests", "Invalid key id.");
			return;
		}
		ItemStack keyItem = SMC.instance().getConfiguration().getItem("keys." + keyId, "", "").getItem();
		target.getInventory().addItem(keyItem);
		target.updateInventory();

		String keyName = SMC.instance().getConfiguration().getStringFormatted("keys." + keyId + ".name");
		Messenger.send(player, Level.NORMAL_SUCCESS, "MysteryChests", "You gave a " + keyName + " key to " + target.getName() + " !");
		SMC.instance().getLocale().getMessage("key-receive").send(target, "$NAME", keyName);
	}

}
