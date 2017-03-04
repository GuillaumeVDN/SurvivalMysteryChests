package be.pyrrh4.smc.managers;

import java.util.UUID;

import org.bukkit.Location;

import be.pyrrh4.core.storage.PMLReader;
import be.pyrrh4.core.util.ULocation;
import be.pyrrh4.smc.SMC;

public class ChestManager
{
	public void registerChest(String id, Location location)
	{
		// On enregistre le coffre dans la database

		String uuid = UUID.randomUUID().toString();

		SMC.i.database
		.set(uuid + ".location", ULocation.serializeLocation(location))
		.set(uuid + ".id", id)
		.save();
	}

	public String getChestId(Location location)
	{
		PMLReader reader = SMC.i.database.reader();

		for (String key : reader.getKeysForSection("", false))
		{
			if (location.equals(ULocation.unserializeLocation(reader.getString(key + ".location")))) {
				return reader.getString(key + ".id");
			}
		}

		return null;
	}

	public String getChestPath(Location location)
	{
		PMLReader reader = SMC.i.database.reader();

		for (String key : reader.getKeysForSection("", false))
		{
			if (location.equals(ULocation.unserializeLocation(reader.getString(key + ".location")))) {
				return key;
			}
		}

		return null;
	}
}
