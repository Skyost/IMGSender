package fr.skyost.imgsender.imgmessage;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;

import fr.skyost.imgsender.utils.Utils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * ImageMessage modified by Skyost.
 * 
 * @author bobacadodl.
 */
public class ImageMessage {
	
	private final static char TRANSPARENT_CHAR = ' ';
	
	private static final Color[] COLORS = {new Color(0, 0, 0), new Color(0, 0, 170), new Color(0, 170, 0), new Color(0, 170, 170), new Color(170, 0, 0), new Color(170, 0, 170), new Color(255, 170, 0), new Color(170, 170, 170), new Color(85, 85, 85), new Color(85, 85, 255), new Color(85, 255, 85), new Color(85, 255, 255), new Color(255, 85, 85), new Color(255, 85, 255), new Color(255, 255, 85), new Color(255, 255, 255),};
	private static final HashMap<Color, ChatColor> oldColorMap = new HashMap<Color, ChatColor>() {
		private static final long serialVersionUID = 1L; {	
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
	
	private String[] lines;
	
	public ImageMessage(final BufferedImage image, final int height, final char imgChar, final boolean newAlgorithm) {
		final ChatColor[][] chatColors = toChatColorArray(image, height, newAlgorithm);
		lines = toImgMessage(chatColors, imgChar);
	}
	
	public ImageMessage(final ChatColor[][] chatColors, final char imgChar) {
		lines = toImgMessage(chatColors, imgChar);
	}
	
	public ImageMessage(final String... imgLines) {
		lines = imgLines;
	}
	
	public final ImageMessage appendText(final String... text) {
		for(int y = 0, x = 0; y < lines.length; y++) {
			while(text.length > x && lines[y].length() < ChatPaginator.AVERAGE_CHAT_PAGE_WIDTH) {
				lines[y] = lines[y] + " " + Utils.colourize(text[x]);
				x++;
            }
		}
		return this;
	}
	
	public final ImageMessage appendCenteredText(final String... text) {
		for(int y = 0; y < lines.length; y++) {
			if(text.length > y) {
				final int len = ChatPaginator.AVERAGE_CHAT_PAGE_WIDTH - lines[y].length();
				lines[y] = lines[y] + center(text[y], len);
			}
			else {
				return this;
			}
		}
		return this;
	}
	
	public static final String[] appendTextToImg(final String[] chatImg, final String... text) {
		for(int y = 0, x = 0; y < chatImg.length; y++) {
			while(text.length > x && chatImg[y].length() < ChatPaginator.AVERAGE_CHAT_PAGE_WIDTH) {
				chatImg[y] = chatImg[y] + " " + Utils.colourize(text[x]);
				x++;
            }
		}
		return chatImg;
	}
	
	public static final String[] appendCenteredTextToImg(final String[] chatImg, final String... text) {
		for(int y = 0; y < chatImg.length; y++) {
			if(text.length > y) {
				final int len = ChatPaginator.AVERAGE_CHAT_PAGE_WIDTH - chatImg[y].length();
				chatImg[y] = chatImg[y] + center(text[y], len);
			}
			else {
				return chatImg;
			}
		}
		return chatImg;
	}
	
	public static final ChatColor[][] toChatColorArray(final BufferedImage image, final int height, final boolean newAlgorithm) {
		final double ratio = (double)image.getHeight() / image.getWidth();
		final BufferedImage resized = resizeImage(image, (int)(height / ratio), height);
		
		final ChatColor[][] chatImg = new ChatColor[resized.getWidth()][resized.getHeight()];
		if(newAlgorithm) {
			for(int x = 0; x < resized.getWidth(); x++) {
				for(int y = 0; y < resized.getHeight(); y++) {
					int rgb = resized.getRGB(x, y);
					ChatColor closest = getClosestChatColor(new Color(rgb, true));
					chatImg[x][y] = closest;
				}
			}
		}
		else {
			for(int x = 0; x < resized.getWidth(); x++) {
	            for(int y = 0; y < resized.getHeight(); y++) {
	                int rgb = resized.getRGB(x, y);
	                ChatColor closest = ChatColor.WHITE;
	                double closestDistance = Double.MAX_VALUE;
	                for(final Color color : oldColorMap.keySet()) {
	                    double dis = colorDiff(color, new Color(rgb));
	                    if(dis < closestDistance) {
	                        closestDistance = dis;
	                        closest = oldColorMap.get(color);
	                    }
	                }
	                chatImg[x][y] = closest;
	            }
	        }
		}
		return chatImg;
	}
	
	public static final String[] toImgMessage(final ChatColor[][] colors, final char imgchar) {
		final String[] lines = new String[colors[0].length];
		for(int y = 0; y < colors[0].length; y++) {
			String line = "";
			for(int x = 0; x < colors.length; x++) {
				final ChatColor color = colors[x][y];
				line += (color != null) ? colors[x][y].toString() + imgchar : TRANSPARENT_CHAR;
			}
			lines[y] = line + ChatColor.RESET;
		}
		return lines;
	}
	
	private static final BufferedImage resizeImage(final BufferedImage originalImage, final int width, final int height) {
		final AffineTransform af = new AffineTransform();
		af.scale(width / (double) originalImage.getWidth(), height / (double) originalImage.getHeight());
		return new AffineTransformOp(af, AffineTransformOp.TYPE_NEAREST_NEIGHBOR).filter(originalImage, null);
	}
	
	private static final double getDistance(final Color c1, final Color c2) {
		final double rmean = (c1.getRed() + c2.getRed()) / 2.0;
		final double r = c1.getRed() - c2.getRed();
		final double g = c1.getGreen() - c2.getGreen();
		final int b = c1.getBlue() - c2.getBlue();
		final double weightR = 2 + rmean / 256.0;
		final double weightG = 4.0;
		final double weightB = 2 + (255 - rmean) / 256.0;
		return weightR * r * r + weightG * g * g + weightB * b * b;
	}
	
	private static final boolean areIdentical(final Color c1, final Color c2) {
		return Math.abs(c1.getRed() - c2.getRed()) <= 5 && Math.abs(c1.getGreen() - c2.getGreen()) <= 5 && Math.abs(c1.getBlue() - c2.getBlue()) <= 5;
		
	}
	
	private static final ChatColor getClosestChatColor(final Color color) {
		if(color.getAlpha() < 128) {
			return null;
		}
		int index = 0;
		double best = -1;
		
		for(int i = 0; i < COLORS.length; i++) {
			if(areIdentical(COLORS[i], color)) {
				return ChatColor.values()[i];
			}
		}
		
		for(int i = 0; i < COLORS.length; i++) {
			final double distance = getDistance(color, COLORS[i]);
			if(distance < best || best == -1) {
				best = distance;
				index = i;
			}
		}
		return ChatColor.values()[index];
	}
	
	private static final String center(final String s, final int length) {
		if(s.length() > length) {
			return s.substring(0, length);
		}
		else if(s.length() == length) {
			return s;
		}
		else {
			int leftPadding = (length - s.length()) / 2;
			final  StringBuilder leftBuilder = new StringBuilder();
			for(int i = 0; i < leftPadding; i++) {
				leftBuilder.append(" ");
			}
			return leftBuilder.toString() + s;
		}
	}
	
	public final String[] getLines() {
		return lines;
	}
	
	public final void sendToPlayer(final Player player) {
		for(final String line : lines) {
			player.sendMessage(line);
		}
	}
	
    private static final double colorDiff(final Color c1, final Color c2) {
        double r1 = c1.getRed();
        double g1 = c1.getGreen();
        double b1 = c1.getBlue();
        double r2 = c2.getRed();
        double g2 = c2.getGreen();
        double b2 = c2.getBlue();
        double distance = (r2 - r1) * (r2 - r1) + (g2 - g1) * (g2 - g1) + (b2 - b1) * (b2 - b1);
        return distance;
    }
	
}
