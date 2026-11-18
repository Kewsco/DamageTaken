package com.kewsco.damagetaken;

import com.google.inject.Provides;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Hitsplat;
import net.runelite.api.HitsplatID;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Projectile;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(
	name = "Damage Taken"
)
public class DamageTakenPlugin extends Plugin
{
	private static final Logger log = LoggerFactory.getLogger(DamageTakenPlugin.class);
	private static final int ANIMATION_WINDOW_TICKS = 3;
	private static final int PROJECTILE_IMPACT_WINDOW_CYCLES = 2;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private DamageTakenOverlay overlay;

	@Inject
	private DamageTakenTracker tracker;

	private final Map<NPC, Integer> lastAnimationTicks = new IdentityHashMap<>();
	private final Map<NPC, Long> lastAnimationSequences = new IdentityHashMap<>();
	private final Map<Projectile, PendingProjectile> pendingProjectiles = new IdentityHashMap<>();
	private final Set<Projectile> matchedProjectiles =
		Collections.newSetFromMap(new IdentityHashMap<>());
	private int gameTick;
	private long animationSequence;

	@Override
	protected void startUp() throws Exception
	{
		tracker.reset();
		lastAnimationTicks.clear();
		lastAnimationSequences.clear();
		pendingProjectiles.clear();
		matchedProjectiles.clear();
		gameTick = 0;
		animationSequence = 0;
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		tracker.reset();
		lastAnimationTicks.clear();
		lastAnimationSequences.clear();
		pendingProjectiles.clear();
		matchedProjectiles.clear();
	}

	@Subscribe
	public void onProjectileMoved(ProjectileMoved event)
	{
		Projectile projectile = event.getProjectile();
		Actor source = projectile.getSourceActor();
		Player player = client.getLocalPlayer();
		if (!matchedProjectiles.contains(projectile)
			&& !pendingProjectiles.containsKey(projectile)
			&& source instanceof NPC
			&& targetsPlayer(projectile, player))
		{
			pendingProjectiles.put(projectile,
				new PendingProjectile((NPC) source, projectile.getEndCycle()));
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		gameTick++;
		cleanupTrackingState();
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (event.getActor() instanceof NPC)
		{
			NPC npc = (NPC) event.getActor();
			if (npc.getAnimation() != -1)
			{
				lastAnimationTicks.put(npc, gameTick);
				lastAnimationSequences.put(npc, ++animationSequence);
			}
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		Player player = client.getLocalPlayer();
		Hitsplat hitsplat = event.getHitsplat();
		if (player == null || event.getActor() != player || hitsplat == null || hitsplat.getAmount() <= 0)
		{
			return;
		}

		log.debug("Received player hitsplat: amount={}, type={}, mine={}, others={}",
			hitsplat.getAmount(), hitsplat.getHitsplatType(), hitsplat.isMine(), hitsplat.isOthers());

		String statusSource = statusSource(hitsplat.getHitsplatType());
		if (statusSource != null)
		{
			tracker.addDamage(statusSource, hitsplat.getAmount());
			log.debug("Recorded {} damage: {}", statusSource, hitsplat.getAmount());
			return;
		}

		NPC source = findProjectileSource(player);
		if (source == null)
		{
			source = findAttackingNpc(player);
		}

		if (source != null)
		{
			tracker.addDamage(source.getName(), hitsplat.getAmount());

			log.debug("Attributed {} damage to {}", hitsplat.getAmount(), source.getName());
		}
		else
		{
			tracker.addDamage("Unknown", hitsplat.getAmount());
			log.debug("Could not attribute {} incoming damage to an NPC", hitsplat.getAmount());
		}
	}

	static String statusSource(int hitsplatType)
	{
		if (hitsplatType == HitsplatID.POISON)
		{
			return "Poison";
		}
		if (hitsplatType == HitsplatID.VENOM)
		{
			return "Venom";
		}
		if (hitsplatType == HitsplatID.BURN)
		{
			return "Burn";
		}
		return null;
	}

	public DamageTakenTracker getTracker()
	{
		return tracker;
	}

	public void reset()
	{
		tracker.reset();
		lastAnimationTicks.clear();
		lastAnimationSequences.clear();
		pendingProjectiles.clear();
		matchedProjectiles.clear();
	}

	private NPC findProjectileSource(Player player)
	{
		List<Map.Entry<Projectile, PendingProjectile>> candidates = new ArrayList<>();
		for (Projectile projectile : client.getProjectiles())
		{
			int impactDistance = Math.abs(projectile.getEndCycle() - client.getGameCycle());
			if (!matchedProjectiles.contains(projectile)
				&& projectile.getSourceActor() instanceof NPC
				&& targetsPlayer(projectile, player)
				&& impactDistance <= PROJECTILE_IMPACT_WINDOW_CYCLES
			)
			{
				candidates.add(new java.util.AbstractMap.SimpleImmutableEntry<>(
					projectile, new PendingProjectile((NPC) projectile.getSourceActor(),
						projectile.getEndCycle())));
			}
		}

		for (Map.Entry<Projectile, PendingProjectile> pending : pendingProjectiles.entrySet())
		{
			if (Math.abs(pending.getValue().endCycle - client.getGameCycle())
				<= PROJECTILE_IMPACT_WINDOW_CYCLES
				&& !candidates.stream().anyMatch(candidate -> candidate.getKey() == pending.getKey()))
			{
				candidates.add(new java.util.AbstractMap.SimpleImmutableEntry<>(
					pending.getKey(), pending.getValue()));
			}
		}

		int closestImpact = candidates.stream()
			.mapToInt(candidate -> Math.abs(candidate.getKey().getEndCycle() - client.getGameCycle()))
			.min()
			.orElse(Integer.MAX_VALUE);
		List<Map.Entry<Projectile, PendingProjectile>> closestCandidates = candidates.stream()
			.filter(candidate -> Math.abs(candidate.getKey().getEndCycle() - client.getGameCycle())
				== closestImpact)
			.collect(java.util.stream.Collectors.toList());
		if (closestCandidates.isEmpty() || !sameProjectileSourceName(closestCandidates))
		{
			return null;
		}

		Projectile projectile = closestCandidates.get(0).getKey();
		pendingProjectiles.remove(projectile);
		matchedProjectiles.add(projectile);
		PendingProjectile pending = closestCandidates.get(0).getValue();
		return pending.source;
	}

	private boolean targetsPlayer(Projectile projectile, Player player)
	{
		if (player == null)
		{
			return false;
		}

		if (projectile.getTargetActor() == player)
		{
			return true;
		}

		return projectile.getTargetPoint() != null
			&& player.getWorldLocation().distanceTo(projectile.getTargetPoint()) <= 1;
	}

	private void cleanupTrackingState()
	{
		lastAnimationTicks.entrySet().removeIf(entry ->
			gameTick - entry.getValue() > ANIMATION_WINDOW_TICKS);
		lastAnimationSequences.keySet().removeIf(npc -> !lastAnimationTicks.containsKey(npc));

		int currentCycle = client.getGameCycle();
		pendingProjectiles.entrySet().removeIf(entry ->
			entry.getValue().endCycle + PROJECTILE_IMPACT_WINDOW_CYCLES < currentCycle);

		Set<Projectile> activeProjectiles =
			Collections.newSetFromMap(new IdentityHashMap<>());
		for (Projectile projectile : client.getProjectiles())
		{
			activeProjectiles.add(projectile);
		}
		matchedProjectiles.retainAll(activeProjectiles);
	}

	private NPC findAttackingNpc(Player player)
	{
		List<NPC> interactingAttackers = client.getNpcs().stream()
			.filter(npc -> npc.getInteracting() == player)
			.filter(npc -> npc.getName() != null)
			.collect(java.util.stream.Collectors.toList());

		if (interactingAttackers.size() == 1)
		{
			return interactingAttackers.get(0);
		}

		List<NPC> attackers = interactingAttackers.isEmpty()
			? client.getNpcs().stream()
				.filter(npc -> npc.getName() != null)
				.filter(npc -> animationAge(npc) <= ANIMATION_WINDOW_TICKS)
				.collect(java.util.stream.Collectors.toList())
			: interactingAttackers.stream()
				.filter(npc -> animationAge(npc) <= ANIMATION_WINDOW_TICKS)
				.collect(java.util.stream.Collectors.toList());
		if (attackers.isEmpty())
		{
			return null;
		}

		attackers = attackers.stream()
			.sorted(Comparator.comparingInt(this::animationAge)
				.thenComparing(Comparator.comparingLong(this::animationSequence).reversed()))
			.collect(java.util.stream.Collectors.toList());

		NPC best = attackers.get(0);
		if (attackers.size() > 1 && !isUnambiguousMeleeSelection(
			animationAge(best), animationAge(attackers.get(1)), sameNpcSourceName(attackers)))
		{
			return null;
		}
		return best;
	}

	static boolean isUnambiguousMeleeSelection(int bestAge, int nextBestAge, boolean sameSourceName)
	{
		return sameSourceName || bestAge < nextBestAge;
	}

	private boolean sameProjectileSourceName(List<Map.Entry<Projectile, PendingProjectile>> candidates)
	{
		String sourceName = candidates.get(0).getValue().source.getName();
		if (sourceName == null)
		{
			return false;
		}

		return candidates.stream()
			.allMatch(candidate -> sourceName.equalsIgnoreCase(
				candidate.getValue().source.getName()));
	}

	private boolean sameNpcSourceName(List<NPC> attackers)
	{
		String sourceName = attackers.get(0).getName();
		return sourceName != null && attackers.stream()
			.allMatch(attacker -> sourceName.equalsIgnoreCase(attacker.getName()));
	}

	private int animationAge(NPC npc)
	{
		Integer lastAnimationTick = lastAnimationTicks.get(npc);
		return lastAnimationTick == null ? Integer.MAX_VALUE : gameTick - lastAnimationTick;
	}

	private long animationSequence(NPC npc)
	{
		return lastAnimationSequences.getOrDefault(npc, Long.MIN_VALUE);
	}

	private static final class PendingProjectile
	{
		private final NPC source;
		private final int endCycle;

		private PendingProjectile(NPC source, int endCycle)
		{
			this.source = source;
			this.endCycle = endCycle;
		}
	}

	@Provides
	DamageTakenConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DamageTakenConfig.class);
	}

	@Provides
	DamageTakenTracker provideTracker()
	{
		return new DamageTakenTracker();
	}
}
