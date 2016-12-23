package be.pyrrh4.smc.misc;

import org.bukkit.entity.Player;

public enum Permissions
{
	CREATE("smc.chest.create"),
	GIVE_KEY("smc.key.give"),
	UPDATE("smc.update"),
	RELOAD("smc.reload");

	private String name;

	private Permissions(String name)
	{
		this.name = name;
	}

	private String getName()
	{
		return name;
	}

	public static boolean has(Player player, Permissions permission)
	{
		return (player.isOp() ? true : player.hasPermission(permission.getName()));
	}
}
