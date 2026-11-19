package com.kewsco.damagetaken;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(DamageTakenConfig.GROUP)
public interface DamageTakenConfig extends Config
{
	String GROUP = "damagetaken";

	@ConfigItem(
		keyName = "visibleEntries",
		name = "Visible entries",
		description = "Maximum number of enemy types shown in the overlay"
	)
	default int visibleEntries()
	{
		return 8;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "overlayOpacity",
		name = "Overlay opacity",
		description = "Opacity of the overlay background"
	)
	default int overlayOpacity()
	{
		return 93;
	}

	@Range(min = 100, max = 400)
	@ConfigItem(
		keyName = "overlayWidth",
		name = "Overlay width",
		description = "Width of the overlay in pixels"
	)
	default int overlayWidth()
	{
		return 150;
	}

	@ConfigItem(
		keyName = "hideHeader",
		name = "Hide overlay header",
		description = "Hide the Damage Taken header row"
	)
	default boolean hideHeader()
	{
		return false;
	}

	@ConfigItem(
		keyName = "resetDamageTotals",
		name = "Reset damage totals",
		description = "Enable to clear recorded damage totals; automatically resets after use"
	)
	default boolean resetDamageTotals()
	{
		return false;
	}

}
