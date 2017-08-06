package be.pyrrh4.survivalmysterychests;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import be.pyrrh4.core.Core;
import be.pyrrh4.core.PyrPlugin;
import be.pyrrh4.core.User;
import be.pyrrh4.core.command.Argument;
import be.pyrrh4.core.command.Command;
import be.pyrrh4.core.util.Utils;
import be.pyrrh4.survivalmysterychests.commands.CommandCreate;
import be.pyrrh4.survivalmysterychests.commands.CommandGivekey;
import be.pyrrh4.survivalmysterychests.listeners.BlockBreak;
import be.pyrrh4.survivalmysterychests.listeners.InventoryClick;
import be.pyrrh4.survivalmysterychests.listeners.PlayerInteract;
import be.pyrrh4.survivalmysterychests.managers.CommandsManager;
import be.pyrrh4.survivalmysterychests.managers.InventoryManager;
import be.pyrrh4.survivalmysterychests.managers.RollManager;

public class SMC extends PyrPlugin
{
	// ------------------------------------------------------------
	// Instance
	// ------------------------------------------------------------

	private static SMC instance;

	public SMC() {
		instance = this;
	}

	public static SMC instance() {
		return instance;
	}

	// ------------------------------------------------------------
	// Fields
	// ------------------------------------------------------------

	private InventoryManager inventoryManager;
	private RollManager rollManager;
	private CommandsManager commandsManager;
	private HashMap<Player, String> definers;
	private MainData mainData;

	public InventoryManager getInventoryManager() {
		return inventoryManager;
	}

	public RollManager getRollManager() {
		return rollManager;
	}

	public CommandsManager getCommandsManager() {
		return commandsManager;
	}

	public HashMap<Player, String> getDefiners() {
		return definers;
	}

	public MainData getMainData() {
		return mainData;
	}

	// ------------------------------------------------------------
	// Preload
	// ------------------------------------------------------------

	@Override
	protected void init()
	{
		getSettings().autoUpdateUrl("https://www.spigotmc.org/resources/15755/");
		getSettings().localeConfigName("locale");
		getSettings().localeDefault("survivalmysterychests_en_US.pyrml");
	}

	@Override
	public void initUserPluginData(User user) {}

	@Override
	protected void initPluginData() {
		// Main data
		mainData = Utils.getPluginData(Core.getDataStorage().getFile("survivalmysterychests.data"), new MainData());
	}

	@Override
	protected void savePluginData() {
		mainData.save();
	}

	// ------------------------------------------------------------
	// On enable
	// ------------------------------------------------------------

	@Override
	protected void enable()
	{
		// settings
		inventoryManager = new InventoryManager();
		rollManager = new RollManager();
		commandsManager = new CommandsManager();
		definers = new HashMap<Player, String>();

		// commands
		Command command = new Command(this, Utils.asList("smc", "survivalmysterychests"), Utils.emptyList(), "survivalmysterychests", false, false, null, "SurvivalMysteryChests main command", Utils.emptyList());
		new CommandCreate(command, Utils.asList("create"), Utils.asList(Argument.STRING), true, false, "smc.chest.create", "create a chest", Utils.asList("[chest id]"));
		new CommandGivekey(command, Utils.asList("givekey"), Utils.asList(Argument.ONLINE_PLAYER, Argument.STRING), false, false, "smc.key.give", "give a key to a player", Utils.asList("[player] [key id]"));

		// events
		Bukkit.getPluginManager().registerEvents(new BlockBreak(), this);
		Bukkit.getPluginManager().registerEvents(new InventoryClick(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerInteract(), this);
	}

	// ------------------------------------------------------------
	// On disable
	// ------------------------------------------------------------

	@Override
	protected void disable() {}

	// ------------------------------------------------------------
	// Utils
	// ------------------------------------------------------------

	public void registerChest(String id, Location location)
	{
		Chest chest = new Chest(id, location);
		mainData.getChests().add(chest);
		mainData.save();
	}

	public void unregisterChest(Chest chest)
	{
		mainData.getChests().remove(chest);
		mainData.save();
	}

	public Chest getChest(Location location)
	{
		for (Chest chest : mainData.getChests()) {
			if (Utils.coordsEquals(chest.getLocation(), location)) {
				return chest;
			}
		}
		return null;
	}
}
