package be.pyrrh4.survivalmysterychests;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import be.pyrrh4.core.messenger.Locale;
import be.pyrrh4.core.util.Utils;

public class Chest
{
	private String path;
	private Location loc;
	private HashMap<UUID, Long> cooldowns = new HashMap<UUID, Long>();
	private long cooldownDelay;

	public Chest(String path, Location loc) {
		this.path = path;
		this.loc = loc;
		this.cooldownDelay = (long) (SMC.instance().getConfiguration().getLong("chests." + path + ".settings.delay") * 1000);
	}

	public String getPath() {
		return path;
	}

	public Location getLocation() {
		return loc;
	}

	public boolean hasCooldown(Player player)
	{
		UUID uuid = player.getUniqueId();

		if (cooldowns.containsKey(uuid))
		{
			if (System.currentTimeMillis() - cooldowns.get(uuid) >= cooldownDelay)
			{
				cooldowns.put(uuid, System.currentTimeMillis());
				return false;
			}
			else
			{
				Locale.MSG_GENERIC_COOLDOWN.getActive().send(player, "{plugin}", SMC.instance().getName(), "{time}", Utils.formatDurationMillis(cooldownDelay - (System.currentTimeMillis() - cooldowns.get(uuid))));
				return true;
			}
		}
		else
		{
			cooldowns.put(uuid, System.currentTimeMillis());
			return false;
		}
	}
}
