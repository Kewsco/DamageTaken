package com.kewsco.damagetaken;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.MenuAction;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

public class DamageTakenOverlay extends Overlay
{
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
		Color text = withOpacity(ColorScheme.LIGHT_GRAY_COLOR, opacity);
		Color white = withOpacity(Color.WHITE, opacity);
		panel.setBackgroundColor(withOpacity(new Color(62, 53, 41), opacity));
		panel.setBorder(new Rectangle(4, 4, 4, 4));
		panel.setPreferredSize(new Dimension(config.overlayWidth(), 0));
		if (!config.hideHeader())
		{
			panel.getChildren().add(LineComponent.builder()
				.left("Damage Taken")
				.leftColor(white)
				.right(" ")
				.build());
			panel.getChildren().add(new SeparatorComponent(
				white, 4));
		}

		if (entries.isEmpty())
		{
			panel.getChildren().add(LineComponent.builder()
				.left("No damage recorded")
				.leftColor(text)
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
					panel.getChildren().add(new SeparatorComponent(
						new Color(255, 255, 255, (int) Math.round(120 * opacity / 255.0)), 4));
				}

				panel.getChildren().add(LineComponent.builder()
					.left(entry.getSource())
					.leftColor(white)
					.right(Integer.toString(entry.getDamage()))
					.rightColor(text)
					.build());
			}
		}

		Dimension size = panel.render(graphics);
		int borderOpacity = (int) Math.round(180 * opacity / 255.0);
		graphics.setColor(new Color(48, 41, 32, borderOpacity));
		graphics.drawRect(0, 0, size.width - 1, size.height - 1);
		return size;
	}

	private static Color withOpacity(Color color, int opacity)
	{
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
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
