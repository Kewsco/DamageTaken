package com.kewsco.damagetaken;

import java.util.List;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DamageTakenTrackerTest
{
	@Test
	public void aggregatesByEnemyNameIgnoringCase()
	{
		DamageTakenTracker tracker = new DamageTakenTracker();
		tracker.addDamage("Goblin", 4);
		tracker.addDamage("goblin", 7);


		List<DamageTakenEntry> entries = tracker.getEntries();
		assertEquals(1, entries.size());
		assertEquals("Goblin", entries.get(0).getSource());
		assertEquals(11, entries.get(0).getDamage());
	}

	@Test
	public void sortsDescendingAndBreaksTiesByName()
	{
		DamageTakenTracker tracker = new DamageTakenTracker();
		tracker.addDamage("Zombie", 10);
		tracker.addDamage("Abyssal demon", 10);
		tracker.addDamage("Goblin", 12);

		List<DamageTakenEntry> entries = tracker.getEntries();
		assertEquals("Goblin", entries.get(0).getSource());
		assertEquals("Abyssal demon", entries.get(1).getSource());
		assertEquals("Zombie", entries.get(2).getSource());
	}

	@Test
	public void ignoresInvalidDamage()
	{
		DamageTakenTracker tracker = new DamageTakenTracker();
		tracker.addDamage(null, 4);
		tracker.addDamage("Goblin", 0);
		tracker.addDamage(" ", 2);

		assertTrue(tracker.isEmpty());
	}

	@Test
	public void resetClearsAllTotals()
	{
		DamageTakenTracker tracker = new DamageTakenTracker();
		tracker.addDamage("Goblin", 4);
		tracker.reset();

		assertTrue(tracker.isEmpty());
	}
}
