package com.kewsco.damagetaken;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class DamageTakenTracker
{
	private final Map<String, DamageTakenEntry> entries = new HashMap<>();

	public void addDamage(String source, int amount)
	{
		if (amount <= 0)
		{
			return;
		}

		String normalized = source == null || source.trim().isEmpty() ? "Unknown" : source.trim();
		String key = normalized.toLowerCase(Locale.ROOT);
		DamageTakenEntry entry = entries.get(key);
		if (entry == null)
		{
			entries.put(key, new DamageTakenEntry(normalized, amount));
		}
		else
		{
			entry.addDamage(amount);
		}
	}

	public List<DamageTakenEntry> getEntries()
	{
		return entries.values().stream()
			.sorted(Comparator.comparingInt(DamageTakenEntry::getDamage)
				.reversed()
				.thenComparing(DamageTakenEntry::getSource, String.CASE_INSENSITIVE_ORDER)
			)
			.map(entry -> new DamageTakenEntry(entry.getSource(), entry.getDamage()))
			.collect(Collectors.toCollection(ArrayList::new));
	}

	public void reset()
	{
		entries.clear();
	}

	public boolean isEmpty()
	{
		return entries.isEmpty();
	}
}
