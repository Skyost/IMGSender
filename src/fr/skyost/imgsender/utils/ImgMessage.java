package fr.skyost.imgsender.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class ImgMessage {
	
	@SuppressWarnings("serial")
	public static HashMap<Color, ChatColor> colorMap = new HashMap<Color, ChatColor>() {{
		put(new Color(0, 0, 0), ChatColor.BLACK);
		put(new Color(0, 0, 170), ChatColor.DARK_BLUE);
		put(new Color(0, 170, 0), ChatColor.DARK_GREEN);
		put(new Color(0, 170, 170), ChatColor.DARK_AQUA);
		put(new Color(170, 0, 0), ChatColor.DARK_RED);
		put(new Color(170, 0, 170), ChatColor.DARK_PURPLE);
		put(new Color(255, 170, 0), ChatColor.GOLD);
		put(new Color(170, 170, 170), ChatColor.GRAY);
		put(new Color(85, 85, 85), ChatColor.DARK_GRAY);
		put(new Color(85, 85, 255), ChatColor.BLUE);
		put(new Color(85, 255, 85), ChatColor.GREEN);
		put(new Color(85, 255, 255), ChatColor.AQUA);
		put(new Color(255, 85, 85), ChatColor.RED);
		put(new Color(255, 85, 255), ChatColor.LIGHT_PURPLE);
		put(new Color(255, 255, 85), ChatColor.YELLOW);
		put(new Color(255, 255, 255), ChatColor.WHITE);
	}};

	public static ChatColor[][] toChatColorArray(BufferedImage image, int height) {
		double ratio = (double) image.getHeight() / image.getWidth();
		int width = (int) (height / ratio);
		if (width > 25) width = 25;
		BufferedImage resized = resizeImage(image, (int) (height / ratio), height);

		ChatColor[][] chatImg = new ChatColor[resized.getWidth()][resized.getHeight()];
		for (int x = 0; x < resized.getWidth(); x++) {
			for (int y = 0; y < resized.getHeight(); y++) {
				int rgb = resized.getRGB(x, y);
				ChatColor closest = ChatColor.WHITE;
				double closestDistance = Double.MAX_VALUE;
				for (Color c : colorMap.keySet()) {
					double dis = colorDiff(c, new Color(rgb));
					if (dis < closestDistance) {
						closestDistance = dis;
						closest = colorMap.get(c);
					}
				}
				chatImg[x][y] = closest;
			}
		}
		return chatImg;
	}

	public static String[] toImgMessage(ChatColor[][] colors, char imgchar) {
		String[] lines = new String[colors[0].length];
		for (int y = 0; y < colors[0].length; y++) {
			String line = "";
			for (int x = 0; x < colors.length; x++) {
				line += colors[x][y].toString()+imgchar;
			}
			lines[y] = line + ChatColor.RESET;
		}
		return lines;
	}

	public static String[] appendTextToImg(String[] chatImg, String... text) {
		for (int y = 0, x = 0; y < chatImg.length; y++) {
			while(text.length>x && chatImg[y].length() < ChatPaginator.AVERAGE_CHAT_PAGE_WIDTH) {
				chatImg[y] = chatImg[y] + " " + Utils.colourize(text[x]);
				x++;
			}
		}
		return chatImg;
	}

	public static void imgMessage(Player player, BufferedImage image, int height, char imgchar, String... text) {
		ChatColor[][] colors = toChatColorArray(image, height);
		String[] lines = toImgMessage(colors, imgchar);
		lines = appendTextToImg(lines, text);
		for (String line : lines) {
			player.sendMessage(line);
		}
	}
	
	public static String[] getImgMessage(BufferedImage image, int height, char imgchar, String... text) {
		ChatColor[][] colors = toChatColorArray(image, height);
		String[] lines = toImgMessage(colors, imgchar);
		lines = appendTextToImg(lines, text);
		return lines;
	}

	private static double colorDiff(Color c1, Color c2) {
		double r1 = c1.getRed();
		double g1 = c1.getGreen();
		double b1 = c1.getBlue();
		double r2 = c2.getRed();
		double g2 = c2.getGreen();
		double b2 = c2.getBlue();
		double distance = (r2 - r1) * (r2 - r1) + (g2 - g1) * (g2 - g1) + (b2 - b1) * (b2 - b1);
		return distance;
	}

	private static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
		int imageType = originalImage.getType();
		if(imageType == 0) {
			imageType = 5;
		}
		BufferedImage resizedImage = new BufferedImage(width, height, imageType);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();
		g.setComposite(AlphaComposite.Src);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		return resizedImage;
	}

	public enum ImgChar {
		BLOCK('\u2588'),
		DARK_SHADE('\u2593'),
		MEDIUM_SHADE('\u2592'),
		LIGHT_SHADE('\u2591');
		private char c;

		ImgChar(char c) {
			this.c = c;
		}

		public char getChar() {
			return c;
		}
	}
}