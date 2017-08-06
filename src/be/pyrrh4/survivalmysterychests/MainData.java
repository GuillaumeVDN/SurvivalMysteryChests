package be.pyrrh4.survivalmysterychests;

import java.util.ArrayList;

import be.pyrrh4.core.PluginData;

public class MainData extends PluginData
{
	// ------------------------------------------------------------
	// Fields and methods
	// ------------------------------------------------------------

	private ArrayList<Chest> chests = new ArrayList<Chest>();

	public ArrayList<Chest> getChests() {
		return chests;
	}
}
