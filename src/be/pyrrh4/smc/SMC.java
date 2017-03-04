package be.pyrrh4.smc;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
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
import be.pyrrh4.core.storage.PMLConvertor;
import be.pyrrh4.core.storage.PMLWriter;
import be.pyrrh4.core.util.ItemBuilder;
import be.pyrrh4.core.util.UString;
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
	public PMLWriter database;

	// Initialize

	@Override
	public void initialize()
	{
		setSetting(Setting.PLUGIN_SHORT_NAME, "SMC");
		setSetting(Setting.AUTO_UPDATE_URL, "https://www.spigotmc.org/resources/15755/");
		setSetting(Setting.ALLOW_PUBLIC_MYSQL, true);
		setSetting(Setting.HAS_STORAGE, true);
		setSetting(Setting.CONFIG_FILE_NAME, "config.pyrml");
	}

	// On enable

	@Override
	public void enable()
	{
		i = this;
		config.loadTextPaths(this, "msg", null, null);

		// Converting data

		File f = new File(getStorage().getParentDirectory(), "config.yml");

		if (f.exists())
		{
			PMLConvertor convertor = new PMLConvertor(this, f);
			convertor.addPath("items.not-selected.type");
			convertor.addPath("items.not-selected.data");
			convertor.addPath("items.not-selected.amount");
			convertor.addPath("items.not-selected.name");
			convertor.addPath("items.not-selected.lore");
			convertor.addPath("items.selected.type");
			convertor.addPath("items.selected.data");
			convertor.addPath("items.selected.amount");
			convertor.addPath("items.selected.name");
			convertor.addPath("items.selected.lore");
			convertor.addPath("sounds.select.sound");
			convertor.addPath("sounds.select.volume");
			convertor.addPath("sounds.select.pitch");
			convertor.addPath("sounds.roll.sound");
			convertor.addPath("sounds.roll.volume");
			convertor.addPath("sounds.roll.pitch");
			convertor.addPath("sounds.finished.sound");
			convertor.addPath("sounds.finished.volume");
			convertor.addPath("sounds.finished.pitch");
			convertor.addPath("sounds.reward.sound");
			convertor.addPath("sounds.reward.volume");
			convertor.addPath("sounds.reward.pitch");

			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
			ConfigurationSection sec = cfg.getConfigurationSection("keys");

			if (sec != null)
			{
				for (String key : sec.getKeys(false))
				{
					convertor.addPath("keys." + key + ".type");
					convertor.addPath("keys." + key + ".data");
					convertor.addPath("keys." + key + ".name");
					convertor.addPath("keys." + key + ".lore");
				}
			}

			sec = cfg.getConfigurationSection("chests");

			if (sec != null)
			{
				for (String key : sec.getKeys(false))
				{
					convertor.addPath("chests." + key + ".settings.name");
					convertor.addPath("chests." + key + ".settings.price");
					convertor.addPath("chests." + key + ".settings.delay");
					convertor.addPath("chests." + key + ".settings.size");
					convertor.addPath("chests." + key + ".settings.choice");

					ConfigurationSection sec2 = cfg.getConfigurationSection("chests." + key + ".wins");

					if (sec2 != null)
					{
						for (String key2 : sec2.getKeys(false))
						{
							convertor.addPath("chests." + key + ".wins." + key2 + ".chance");
							convertor.addPath("chests." + key + ".wins." + key2 + ".item");
							convertor.addPath("chests." + key + ".wins." + key2 + ".enchantments");
							convertor.addPath("chests." + key + ".wins." + key2 + ".name");
							convertor.addPath("chests." + key + ".wins." + key2 + ".lore");
						}
					}
				}
			}

			convertor.addPath("msg.permission-error");
			convertor.addPath("msg.chest-pay");
			convertor.addPath("msg.chest-price");
			convertor.addPath("msg.chest-delay");
			convertor.addPath("msg.key-receive");
			convertor.addPath("msg.inventory-rolling");
			convertor.addPath("msg.inventory-finished");
			convertor.convert();
		}

		// Vars

		vault = getServer().getServicesManager().getRegistration(Economy.class).getProvider();

		chestManager = new ChestManager();
		inventoryManager = new InventoryManager();
		rollManager = new RollManager();
		commandsManager = new CommandsManager();
		cooldownManager = new CooldownManager();

		definers = new HashMap<Player, String>();
		cooldowns = new HashMap<Player, HashMap<String, Long>>();

		database = getStorage().getPMLWriter("chests.data");

		// Converting old data

		File oldFile = new File(getDataFolder().getParentFile() + File.separator + "SMC", "database.yml");

		if (oldFile.exists() && !database.reader().getOrDefault("converted", false))
		{
			log(Level.INFO, "Starting converting old data from /SurvivalMysteryChests/database.yml to /pyrrh4_plugins/SurvivalMysteryChests/chests.data ...");
			YamlConfiguration old = YamlConfiguration.loadConfiguration(oldFile);
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

			database.set("converted", true).save();
			log(Level.INFO, "Successfully converted all chests from the old database file. " + loaded + " chest" + (loaded > 1 ? "s" : "") + " were loaded and " + skipped + " chest" + (skipped > 1 ? "s" : "") + " were skipped.");
		}

		// Commands

		getCommand("survivalmysterychests").setExecutor(this);

		handler = new CommandHandler(this, "/survivalmysterychests", Core.getMessenger());
		handler.addHelp("/pyr reload SurvivalMysteryChests", "reload the plugin", "pyr.core.admin");
		handler.addHelp("/smc create [chest id]", "create a chest", "smc.chest.create");
		handler.addHelp("/smc givekey [player] [key id]", "give a key to a player", "smc.key.give");

		handler.addSubCommand(new CommandSubHandler(true, true, "smc.chest.create", new CommandArgumentsPattern("create [string]"))
		{
			@Override
			public void execute(CommandCallInfo call)
			{
				Player player = call.getSenderAsPlayer();
				String id = call.getArgAsString(1);

				// On vérifie que la clé est valide

				if (!config.contains(id + ".settings.name"))
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

				if (!config.contains("keys." + keyId + ".name"))
				{
					Core.getMessenger().error(player, "MysteryChests >>", "Invalid id !");
					return;
				}

				// On ajoute l'item au joueur

				ItemStack keyItem = ItemBuilder.fromPMLReader(config, "keys." + keyId).build();
				target.getInventory().addItem(keyItem);
				target.updateInventory();

				// On envoie des messages

				String keyName = UString.format(config.getString("keys." + keyId + ".name"));
				Core.getMessenger().normal(player, "MysteryChests >>", "You gave a '" + keyName + "' key to " + target.getName() + " !");
				config.getMessage("key-receive").send(new Replacer("{name}", keyName), target);
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
		if (args.length == 0) {
			handler.showHelp(sender);
		}
		else {
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
	public String getAdditionalPasteContent()
	{
		return "\n" + "Chests (config) : " + config.getKeysForSection("chests", false).size() + "\n" + "Chests (registered) : " + database.reader().getKeysForSection("", true).size();
	}
}
