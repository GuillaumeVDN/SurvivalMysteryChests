package be.pyrrh4.survivalmysterychests.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import be.pyrrh4.core.Perm;
import be.pyrrh4.core.command.CommandArgument;
import be.pyrrh4.core.command.CommandCall;
import be.pyrrh4.core.command.Param;
import be.pyrrh4.core.messenger.Locale;
import be.pyrrh4.core.messenger.Messenger;
import be.pyrrh4.core.messenger.Messenger.Level;
import be.pyrrh4.core.util.Utils;
import be.pyrrh4.survivalmysterychests.SMC;

public class ArgGivekey extends CommandArgument {

	private static final Param paramPlayer = new Param(Utils.asList("player", "p"), "name", null, false);
	private static final Param paramKey = new Param(Utils.asList("key"), "id", null, false);

	public ArgGivekey() {
		super(SMC.instance(), Utils.asList("givekey"), "give a key", Perm.SURVIVALMYSTERYCHESTS_ADMIN, false, paramPlayer, paramKey);
	}

	@Override
	public void perform(CommandCall call) {
		CommandSender player = call.getSender();
		Player target = paramPlayer.getPlayer(call, true);
		String keyId = paramKey.getString(call);
		if (target != null && keyId != null) {
			if (!SMC.instance().getConfiguration().contains("keys." + keyId + ".name")) {
				Messenger.send(player, Level.SEVERE_ERROR, "MysteryChests", "Invalid key id.");
				return;
			}
			ItemStack keyItem = SMC.instance().getConfiguration().getItem("keys." + keyId).getItemStack();
			target.getInventory().addItem(keyItem);
			target.updateInventory();

			String keyName = SMC.instance().getConfiguration().getStringFormatted("keys." + keyId + ".name");
			Messenger.send(player, Level.NORMAL_SUCCESS, "MysteryChests", "You gave a " + keyName + " key to " + target.getName() + " !");
			Locale.MSG_SURVIVALMYSTERYCHESTS_KEYRECEIVE.getActive().send(target, "{name}", keyName);
		}
	}

}
