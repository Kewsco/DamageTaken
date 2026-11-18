package com.kewsco.damagetaken;

import net.runelite.api.HitsplatID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DamageTakenPluginTest
{
	@Test
	public void identifiesPoisonAndVenomHitsplats()
	{
		assertEquals("Poison", DamageTakenPlugin.statusSource(HitsplatID.POISON));
		assertEquals("Venom", DamageTakenPlugin.statusSource(HitsplatID.VENOM));
		assertEquals("Burn", DamageTakenPlugin.statusSource(HitsplatID.BURN));
		assertNull(DamageTakenPlugin.statusSource(HitsplatID.DAMAGE_ME));
	}

	@Test
	public void acceptsOnlyTheMostRecentMeleeAnimationWhenSourcesDiffer()
	{
		assertEquals(true, DamageTakenPlugin.isUnambiguousMeleeSelection(0, 1, false));
		assertEquals(false, DamageTakenPlugin.isUnambiguousMeleeSelection(1, 1, false));
		assertEquals(true, DamageTakenPlugin.isUnambiguousMeleeSelection(1, 1, true));
	}
}
