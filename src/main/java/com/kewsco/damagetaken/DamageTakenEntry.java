package com.kewsco.damagetaken;

import java.util.Objects;

public final class DamageTakenEntry
{
	private final String source;
	private int damage;

	public DamageTakenEntry(String source, int damage)
	{
		this.source = Objects.requireNonNull(source);
		this.damage = damage;
	}

	public String getSource()
	{
		return source;
	}

	public int getDamage()
	{
		return damage;
	}

	void addDamage(int amount)
	{
		damage += amount;
	}
}
