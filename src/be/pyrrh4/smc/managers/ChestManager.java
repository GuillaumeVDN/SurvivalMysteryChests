package be.pyrrh4.smc.managers;

import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;

import be.pyrrh4.core.util.ULocation;
import be.pyrrh4.smc.SMC;

public class ChestManager
{
	public void registerChest(String id, Location location)
	{
		// On enregistre le coffre dans la database

		String uuid = UUID.randomUUID().toString();
		SMC.i.database.set(uuid + ".location", ULocation.serializeLocation(location));
		SMC.i.database.set(uuid + ".id", id);
	}

	public String getChestId(Location location)
	{
		ConfigurationSection configurationSection = SMC.i.database.getLast().getConfigurationSection("");

		if (configurationSection == null)
			return null;

		for (Entry<String, Object> entry : configurationSection.getValues(false).entrySet())
		{
			MemorySection memorySection = (MemorySection) entry.getValue();
			Location loc = ULocation.unserializeLocation(memorySection.getString("location"));
			String id = memorySection.getString("id");

			if (location.equals(loc))
				return id;
		}

		return null;
	}

	public String getChestPath(Location location)
	{
		ConfigurationSection configurationSection = SMC.i.database.getLast().getConfigurationSection("");

		if (configurationSection == null)
			return null;

		for (Entry<String, Object> entry : configurationSection.getValues(false).entrySet())
		{
			MemorySection memorySection = (MemorySection) entry.getValue();
			Location loc = ULocation.unserializeLocation(memorySection.getString("location"));

			if (location.equals(loc))
				return entry.getKey();
		}

		return null;
	}
}
