package be.pyrrh4.survivalmysterychests;

import java.util.ArrayList;

import be.pyrrh4.core.storage.PluginData;

public class SurvivalMysteryChestsData extends PluginData {

	// ------------------------------------------------------------
	// Fields and methods
	// ------------------------------------------------------------

	private ArrayList<Chest> chests = new ArrayList<Chest>();

	public ArrayList<Chest> getChests() {
		return chests;
	}

}
