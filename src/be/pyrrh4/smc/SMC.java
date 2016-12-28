package be.pyrrh4.smc;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import be.pyrrh4.core.AbstractPlugin;
import be.pyrrh4.core.Core;
import be.pyrrh4.core.Setting;
import be.pyrrh4.core.command.CommandArgumentsPattern;
import be.pyrrh4.core.command.CommandCallInfo;
import be.pyrrh4.core.command.CommandHandler;
import be.pyrrh4.core.command.CommandSubHandler;
import be.pyrrh4.core.messenger.Replacer;
import be.pyrrh4.core.storage.Config;
import be.pyrrh4.core.storage.ConfigFile;
import be.pyrrh4.core.util.UString;
import be.pyrrh4.core.util.collection.ItemBuilder;
import be.pyrrh4.smc.listeners.BlockBreak;
import be.pyrrh4.smc.listeners.InventoryClick;
import be.pyrrh4.smc.listeners.PlayerInteract;
import be.pyrrh4.smc.managers.ChestManager;
import be.pyrrh4.smc.managers.CommandsManager;
import be.pyrrh4.smc.managers.CooldownManager;
import be.pyrrh4.smc.managers.InventoryManager;
import be.pyrrh4.smc.managers.RollManager;
import net.milkbowl.vault.economy.Economy;

public class SMC extends AbstractPlugin
{
	public static SMC i;
	public static Economy vault;

	public ChestManager chestManager;
	public InventoryManager inventoryManager;
	public RollManager rollManager;
	public CommandsManager commandsManager;
	public CooldownManager cooldownManager;

	public HashMap<Player, String> definers;
	public HashMap<Player, HashMap<String, Long>> cooldowns;

	private CommandHandler handler;
	public ConfigFile database;

	// Initialize

	@Override
	public void initialize()
	{
		setSetting(Setting.PLUGIN_SHORT_NAME, "SMC");
		setSetting(Setting.AUTO_UPDATE_URL, "https://www.spigotmc.org/resources/15755/");
		setSetting(Setting.ALLOW_PUBLIC_MYSQL, true);
		setSetting(Setting.HAS_STORAGE, true);
		setSetting(Setting.CONFIG_FILE_NAME, "config.yml");
		setSetting(Setting.CONFIG_PATH_MESSAGES, "msg");
	}

	// On enable

	@Override
	public void enable()
	{
		i = this;
		vault = getServer().getServicesManager().getRegistration(Economy.class).getProvider();

		chestManager = new ChestManager();
		inventoryManager = new InventoryManager();
		rollManager = new RollManager();
		commandsManager = new CommandsManager();
		cooldownManager = new CooldownManager();

		definers = new HashMap<Player, String>();
		cooldowns = new HashMap<Player, HashMap<String, Long>>();

		database = getStorage().getConfig("chests.data");

		// Converting old data

		File oldFile = new File(getDataFolder().getParentFile() + File.separator + "SMC", "database.yml");

		if (oldFile.exists() && !database.getOrDefault("converted", false))
		{
			log(Level.INFO, "Starting converting old data from /SurvivalMysteryChests/database.yml to /pyrrh4_plugins/SurvivalMysteryChests/chests.data ...");
			Config old = Config.loadConfiguration(this, oldFile);
			int loaded = 0;
			int skipped = 0;

			if (old.contains("chests"))
			{
				for (String uuid : old.getConfigurationSection("chests").getKeys(false))
				{
					try
					{
						String loc = convertOldLocation(old.getString(uuid));
						String id = old.getString("chests." + uuid + ".id");

						if (loc == null || id == null)
						{
							log(Level.WARNING, "Could not load chest '" + uuid + "' from the old database file.");
							continue;
						}

						database.set(uuid + ".location", loc);
						database.set(uuid + ".id", id);
						loaded++;
						log(Level.INFO, "Successfully loaded chest '" + uuid + "' from the old database file.");
					}
					catch (Exception exception)
					{
						skipped++;
						log(Level.WARNING, "Could not load chest '" + uuid + "' scrollboard from the old database file.");
					}
				}
			}

			database.set("converted", true);
			log(Level.INFO, "Successfully converted all chests from the old database file. " + loaded + " chest" + (loaded > 1 ? "s" : "") + " were loaded and " + skipped + " chest" + (skipped > 1 ? "s" : "") + " were skipped.");
		}

		// Commands

		getCommand("survivalmysterychests").setExecutor(this);
		handler = new CommandHandler("/survivalmysterychests", Core.getMessenger());

		handler.addSubCommand(new CommandSubHandler(true, true, "smc.chest.create", new CommandArgumentsPattern("create [string]"))
		{
			@Override
			public void execute(CommandCallInfo call)
			{
				Player player = call.getSenderAsPlayer();
				String id = call.getArgAsString(1);

				// On vérifie que la clé est valide

				if (!config.getLast().contains(id + ".settings.name"))
				{
					Core.getMessenger().error(player, "MysteryChests >>", "Invalid id !");
					return;
				}

				definers.put(player, id);
				Core.getMessenger().error(player, "MysteryChests >>", "Invalid ID !");
			}
		});

		handler.addSubCommand(new CommandSubHandler(false, false, "smc.key.give", new CommandArgumentsPattern("givekey [player] [string]"))
		{
			@Override
			public void execute(CommandCallInfo call)
			{
				Player player = call.getSenderAsPlayer();
				Player target = call.getArgAsPlayer(1);
				String keyId = call.getArgAsString(2);

				// On vérifie que la clé est valide

				if (!config.getLast().contains("keys." + keyId + ".name"))
				{
					Core.getMessenger().error(player, "MysteryChests >>", "Invalid id !");
					return;
				}

				// On ajoute l'item au joueur

				ItemStack keyItem = ItemBuilder.fromConfig(config.getLast(), "keys." + keyId).build();
				target.getInventory().addItem(keyItem);
				target.updateInventory();

				// On envoie des messages

				String keyName = UString.format(config.getLast().getString("keys." + keyId + ".name"));
				Core.getMessenger().normal(player, "MysteryChests >>", "You gave a '" + keyName + "' key to " + target.getName() + " !");
				getMessage("key-receive").send(new Replacer("{name}", keyName), target);
			}
		});

		// Events

		Bukkit.getPluginManager().registerEvents(new BlockBreak(), this);
		Bukkit.getPluginManager().registerEvents(new InventoryClick(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerInteract(), this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (args.length == 0)
		{
			// TODO : /help : /smc

			Core.getMessenger().listMessage(sender, "MysteryChests >>", "This server is running " + getDescription().getName() + " version " + getDescription().getVersion() + ".");

			if (sender.hasPermission("smc.chest.create")) {
				Core.getMessenger().listSubMessage(sender, "  >>", "§a/smc create [chest id] §7: create a chest");
			}

			if (sender.hasPermission("smc.key.give")) {
				Core.getMessenger().listSubMessage(sender, "  >>", "§a/smc givekey [player] [key id] §7: give a key to a player");
			}

			if (sender.hasPermission("pyr.core.admin")) {
				Core.getMessenger().listSubMessage(sender, "  >>", "§a/pyr rl SurvivalMysteryChests §7: reload the plugin");
			}
		}
		else
		{
			handler.execute(sender, args);
		}

		return true;
	}

	@Override
	public void disable() {}

	private static String convertOldLocation(String oldLocation)
	{
		if (oldLocation == null) {
			return null;
		}

		String world = oldLocation.split("\\|")[0];
		double x = Double.parseDouble(oldLocation.split("\\|")[1]);
		double y = Double.parseDouble(oldLocation.split("\\|")[2]);
		double z = Double.parseDouble(oldLocation.split("\\|")[3]);
		float yaw = Float.parseFloat(oldLocation.split("\\|")[4]);
		float pitch = Float.parseFloat(oldLocation.split("\\|")[5]);

		String ser = new Double(x).intValue() + "s" + new Double(y).intValue() + "s" + new Double(z).intValue() + "s" + yaw + "s" + pitch;
		ser = ser.replace("-", "n");
		ser = ser.replace(".", "d");
		ser = world + "_" + ser;

		return ser;
	}

	@Override
	public String getAdditionnalPasteContent()
	{
		return "\n" + "Chests (config) : " + config.getLast().getConfigurationSection("chests").getKeys(false).size() + "\n" + "Chests (registered) : " + database.getLast().getConfigurationSection("").getKeys(false).size();
	}
}
