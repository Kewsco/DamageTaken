package com.kewsco.damagetaken;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DamageTakenConfigTest
{
	@Test
	public void defaultsOverlayWidthTo150Pixels()
	{
		DamageTakenConfig config = new DamageTakenConfig()
		{
		};

		assertEquals(150, config.overlayWidth());
	}
}
