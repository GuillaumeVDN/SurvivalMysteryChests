package be.pyrrh4.survivalmysterychests;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import be.pyrrh4.core.PyrPlugin;
import be.pyrrh4.core.command.Arguments;
import be.pyrrh4.core.command.Command;
import be.pyrrh4.core.util.Utils;
import be.pyrrh4.survivalmysterychests.commands.ArgCreate;
import be.pyrrh4.survivalmysterychests.commands.ArgGivekey;
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
	private SurvivalMysteryChestsData survivalMysteryChestsData;

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

	// ------------------------------------------------------------
	// Preload
	// ------------------------------------------------------------

	@Override
	protected void init()
	{
		//getSettings().autoUpdateUrl("https://www.spigotmc.org/resources/15755/");
		getSettings().localeConfigName("locale");
		getSettings().localeDefault("survivalmysterychests_en_US.yml");
	}

	@Override
	protected void initStorage() {
		// Main data
		survivalMysteryChestsData = Utils.getPluginData(SurvivalMysteryChestsData.class);
	}

	@Override
	protected void savePluginData() {
		survivalMysteryChestsData.save();
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
		Command command = new Command(this, "survivalmysterychests", "smc", null);
		command.addArguments(new Arguments("create [string]", "create [chest]", "create a chest", "smc.chest.create", true, new ArgCreate()));
		command.addArguments(new Arguments("givekey [player] [string]", "givekey [player] [chest id]", "give a key to a player", "smc.key.give", true, new ArgGivekey()));

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
		survivalMysteryChestsData.getChests().add(chest);
		survivalMysteryChestsData.save();
	}

	public void unregisterChest(Chest chest)
	{
		survivalMysteryChestsData.getChests().remove(chest);
		survivalMysteryChestsData.save();
	}

	public Chest getChest(Location location)
	{
		for (Chest chest : survivalMysteryChestsData.getChests()) {
			if (Utils.coordsEquals(chest.getLocation(), location)) {
				return chest;
			}
		}
		return null;
	}
}
