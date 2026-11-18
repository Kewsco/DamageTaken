package com.kewsco.damagetaken;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.MenuAction;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

public class DamageTakenOverlay extends Overlay
{
	private static final Color TITLE = new Color(255, 193, 82);
	private static final Color TEXT = new Color(238, 220, 180);

	private final DamageTakenPlugin plugin;
	private final DamageTakenConfig config;
	private final PanelComponent panel = new PanelComponent();

	@Inject
	public DamageTakenOverlay(DamageTakenPlugin plugin, DamageTakenConfig config)
	{
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_RIGHT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		addMenuEntry(MenuAction.RUNELITE_OVERLAY, "Reset data", "Damage Taken",
			menuEntry -> plugin.reset());
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		List<DamageTakenEntry> entries = plugin.getTracker().getEntries();
		panel.getChildren().clear();
		int opacity = (int) Math.round(config.overlayOpacity() * 255 / 100.0);
		panel.setBackgroundColor(new Color(46, 32, 21, opacity));
		panel.setBorder(new Rectangle(6, 6, 6, 6));
		panel.setPreferredSize(new Dimension(190, 0));
		if (!config.hideHeader())
		{
			panel.getChildren().add(LineComponent.builder()
				.left("Damage Taken")
				.leftColor(TITLE)
				.right(" ")
				.build());
			panel.getChildren().add(new SeparatorComponent(new Color(177, 127, 65, opacity), 6));
		}

		if (entries.isEmpty())
		{
			panel.getChildren().add(LineComponent.builder()
				.left("No damage recorded")
				.leftColor(TEXT)
				.build());
		}
		else
		{
			int limit = Math.max(1, config.visibleEntries());
			for (int i = 0; i < Math.min(limit, entries.size()); i++)
			{
				DamageTakenEntry entry = entries.get(i);
				if (i > 0)
				{
					panel.getChildren().add(new SeparatorComponent(new Color(111, 77, 42,
						(int) Math.round(120 * opacity / 255.0)), 5));
				}

				panel.getChildren().add(LineComponent.builder()
					.left(entry.getSource())
					.leftColor(TEXT)
					.right(Integer.toString(entry.getDamage()))
					.rightColor(TITLE)
					.build());
			}
		}

		Dimension size = panel.render(graphics);
		graphics.setColor(new Color(157, 111, 56, opacity));
		graphics.drawRect(0, 0, size.width - 1, size.height - 1);
		return size;
	}

	private static final class SeparatorComponent implements LayoutableRenderableEntity
	{
		private final Color color;
		private final int height;
		private final Rectangle bounds = new Rectangle();
		private Point preferredLocation = new Point();
		private Dimension preferredSize = new Dimension();

		private SeparatorComponent(Color color, int height)
		{
			this.color = color;
			this.height = height;
		}

		@Override
		public Rectangle getBounds()
		{
			return bounds;
		}

		@Override
		public void setPreferredLocation(Point position)
		{
			preferredLocation = position;
		}

		@Override
		public void setPreferredSize(Dimension dimension)
		{
			preferredSize = dimension;
		}

		@Override
		public Dimension render(Graphics2D graphics)
		{
			int width = Math.max(1, preferredSize.width);
			graphics.setColor(color);
			int y = preferredLocation.y + height / 2;
			graphics.drawLine(preferredLocation.x, y, preferredLocation.x + width - 1, y);
			Dimension size = new Dimension(width, height);
			bounds.setBounds(preferredLocation.x, preferredLocation.y, size.width, size.height);
			return size;
		}
	}
}
