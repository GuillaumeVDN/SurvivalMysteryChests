package be.pyrrh4.smc.misc;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import be.pyrrh4.smc.SMC;

public class InventoryData
{
	private String id;
	private Inventory inventory;
	private Location blockLocation;
	private ArrayList<String> choices;
	private boolean isRolling;
	private boolean isFinished;

	public InventoryData(String id, Inventory inventory, Location location)
	{
		this.id = id;
		this.inventory = inventory;
		this.blockLocation = location;
		this.choices = new ArrayList<String>();
	}

	// Roll

	public void setRolling(boolean rolling)
	{
		this.isRolling = rolling;
	}

	public boolean isRolling()
	{
		return isRolling;
	}

	// Finished

	public void setFinished(boolean finished)
	{
		this.isFinished = finished;
	}

	public boolean isFinished()
	{
		return isFinished;
	}

	// Get

	public String getId()
	{
		return id;
	}

	public Inventory getInventory()
	{
		return inventory;
	}

	public Location getBlockLocation()
	{
		return blockLocation;
	}

	public Player getPlayer()
	{
		return (Player) inventory.getHolder();
	}

	public ArrayList<Integer> getChoices()
	{
		ArrayList<Integer> choices = new ArrayList<Integer>();

		for (String choice : this.choices)
			choices.add(Integer.parseInt(choice));

		return choices;
	}

	// Add

	public void addChoice(int choice)
	{
		this.choices.add(String.valueOf(choice));
	}

	// Set

	public void setInventory(Inventory inventory)
	{
		this.inventory = inventory;
	}

	// Remove

	public void removeChoice(int choice)
	{
		this.choices.remove(String.valueOf(choice));
	}

	public void remove()
	{
		SMC.i.inventoryManager.inventories.remove(this);

		this.id = null;
		this.inventory = null;
		this.blockLocation = null;
		this.choices = null;
		this.isRolling = false;
		this.isFinished = false;
	}
}
