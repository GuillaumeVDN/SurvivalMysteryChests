package be.pyrrh4.survivalmysterychests;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import be.pyrrh4.core.Perm;
import be.pyrrh4.core.PyrPlugin;
import be.pyrrh4.core.command.CommandRoot;
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
	// Pre enable
	// ------------------------------------------------------------

	@Override
	protected boolean preEnable() {
		this.spigotResourceId = 15755;
		return false;
	}

	@Override
	protected void loadStorage() {
		survivalMysteryChestsData = Utils.getPluginData(SurvivalMysteryChestsData.class);
	}

	@Override
	protected void saveStorage() {
		if (survivalMysteryChestsData != null) {
			survivalMysteryChestsData.saveData();
		}
	}

	// ------------------------------------------------------------
	// Override : reload
	// ------------------------------------------------------------

	@Override
	protected void reloadInner() {
	}

	// ------------------------------------------------------------
	// On enable
	// ------------------------------------------------------------

	@Override
	protected boolean enable() {
		// settings
		inventoryManager = new InventoryManager();
		rollManager = new RollManager();
		commandsManager = new CommandsManager();
		definers = new HashMap<Player, String>();

		// events
		Bukkit.getPluginManager().registerEvents(new BlockBreak(), this);
		Bukkit.getPluginManager().registerEvents(new InventoryClick(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerInteract(), this);

		// commands
		CommandRoot root = new CommandRoot(this, Utils.asList("survivalmysterychests", "smc"), null, Perm.SURVIVALMYSTERYCHESTS_ADMIN, false);
		root.addChild(new ArgCreate());
		root.addChild(new ArgGivekey());

		// return
		return true;
	}

	// ------------------------------------------------------------
	// On disable
	// ------------------------------------------------------------

	@Override
	protected void disable() {
	}

	// ------------------------------------------------------------
	// Utils
	// ------------------------------------------------------------

	public void registerChest(String id, Location location)
	{
		Chest chest = new Chest(id, location);
		survivalMysteryChestsData.getChests().add(chest);
		survivalMysteryChestsData.mustSave(true);
	}

	public void unregisterChest(Chest chest)
	{
		survivalMysteryChestsData.getChests().remove(chest);
		survivalMysteryChestsData.mustSave(true);
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
