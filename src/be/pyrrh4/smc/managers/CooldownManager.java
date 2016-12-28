package be.pyrrh4.smc.managers;

import java.util.HashMap;

import org.bukkit.entity.Player;

import be.pyrrh4.core.messenger.Replacer;
import be.pyrrh4.smc.SMC;

public class CooldownManager
{
	public boolean checkCooldown(Player player, String chest)
	{
		if (SMC.i.cooldowns.containsKey(player))
		{
			HashMap<String, Long> times = SMC.i.cooldowns.get(player);

			if (times.containsKey(chest))
			{
				long now = System.currentTimeMillis();
				long time = times.get(chest);
				int delay = SMC.i.config.getLast().getInt("chests." + chest + ".settings.delay");

				if ((now - time) >= 1000L * delay)
				{
					times.put(chest, System.currentTimeMillis());
					SMC.i.cooldowns.put(player, times);
					return true;
				}
				else
				{
					SMC.i.getMessage("chest-delay").send(new Replacer("{time}", delay - ((now - time) / 1000L)), player);
					return false;
				}
			}
			else
			{
				times.put(chest, System.currentTimeMillis());
				SMC.i.cooldowns.put(player, times);
				return true;
			}
		}
		else
		{
			SMC.i.cooldowns.put(player, new HashMap<String, Long>());
			return true;
		}
	}
}
