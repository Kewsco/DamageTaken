package com.kewsco.damagetaken;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class DamageTakenDevClient
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(DamageTakenPlugin.class);
		RuneLite.main(args);
	}
}
