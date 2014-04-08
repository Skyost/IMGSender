package fr.skyost.imgsender.tasks;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.base.Joiner;

import fr.skyost.imgsender.IMGSender;
import fr.skyost.imgsender.imgmessage.ImageChar;
import fr.skyost.imgsender.imgmessage.ImageMessage;

/**
 * IMGManager is used to send an image.
 * 
 * @author Skyost.
 */

public class IMGTask extends Thread {
	
	private final CommandSender sender;
	private final String url;
	private final String size;
	private final String imageChar;
	private final String[] text;
	private final Player player;
	private final ItemStack itemStack;
	
	/**
	 * Every arguments are Strings because it is more convenient to use in a CommandExecutor.
	 * <br>There are all nullable except <b>sender</b>.
	 * 
	 * @param sender The CommandSender.
	 * @param url The url of the image.
	 * @param size The image's size.
	 * @param imageChar The look of the "texted" image.
	 * @param text The text to append on the image.
	 * @param player The player who will receive the image.
	 * @param itemStack If is /img or /img-item.
	 */
	
	public IMGTask(final CommandSender sender, final String url, final String size, final String imageChar, final String[] text, final Player player, final ItemStack itemStack) {
		this.sender = sender;
		this.url = url == null ? IMGSender.config.Default_URL : url;
		this.imageChar = imageChar == null ? IMGSender.config.Default_Char.name() : imageChar;
		this.text = text;
		this.player = player;
		this.itemStack = itemStack;
		this.size = size == null ? (itemStack == null ? String.valueOf(IMGSender.config.Default_Size) : (IMGSender.config.Default_Size > 30 ? "30" : String.valueOf(IMGSender.config.Default_Size))) : size;
	}
	
	@Override
	public void run() {
		try {
			final URL url = new URL(this.url);
			for(final String word : IMGSender.config.Config_Unauthorized_Words) {
				if(url.toString().contains(word)) {
					sender.sendMessage(ChatColor.RED + "Your URL contains an unauthorized word : '" + word + "'.");
					return;
				}
			}
			final int size = Integer.parseInt(this.size);
			final int maxSize = itemStack == null ? IMGSender.config.ImageSize_Max : (IMGSender.config.ImageSize_Max > 30 ? 30 : IMGSender.config.ImageSize_Max);
			if(size < IMGSender.config.ImageSize_Min || size > maxSize) {
				sender.sendMessage(ChatColor.RED + "The image size must be between '" + IMGSender.config.ImageSize_Min + "' and '" + maxSize + (itemStack == null ? "'" : "' (limited by items)") + " !");
				return;
			}
			final ImageChar imageChar = ImageChar.valueOf(this.imageChar);
			BufferedImage bufferedImage = null;
			if(IMGSender.cache.get(url) == null) {
				if(IMGSender.cache.size() == IMGSender.config.Cache_Size_Max) {
					for(final File file : IMGSender.cacheDir.listFiles()) {
						file.delete();
					}
					IMGSender.cache.clear();
				}
				final File imageFile = new File(IMGSender.cacheDir, String.valueOf(IMGSender.cache.size() + 1));
				final Downloader downloader = new Downloader(url, imageFile, sender);
				downloader.run();
				if(!downloader.getResponse().startsWith("2")) {
					final String message = ChatColor.RED + "Bad HTTP response '" + downloader.getResponse() + "'. Maybe anti-hotlinking has been activated ?";
					sender.sendMessage(message);
					if(!sender.getName().equals("CONSOLE")) {
						Bukkit.getConsoleSender().sendMessage(message);
					}
					if(imageFile.exists()) {
						imageFile.delete();
					}
					return;
				}
				bufferedImage = ImageIO.read(imageFile);
				if(bufferedImage != null) {
				   	IMGSender.cache.put(url, IMGSender.cache.size() + 1);
				}
				else {
					sender.sendMessage(ChatColor.RED + "This file is not an image.");
					imageFile.delete();
					return;
				}
			}
			else {
				bufferedImage = ImageIO.read(new File(IMGSender.cacheDir, String.valueOf(IMGSender.cache.get(url))));
			}
			if(!(IMGSender.config.RescaleOp_Offset.equals("0.0") || IMGSender.config.RescaleOp_Offset.equals("0")) && (IMGSender.config.RescaleOp_ScaleFactor.equals("0.0") || IMGSender.config.RescaleOp_ScaleFactor.equals("0"))) {
				new RescaleOp(Float.parseFloat(IMGSender.config.RescaleOp_Offset), Float.parseFloat(IMGSender.config.RescaleOp_ScaleFactor), null).filter(bufferedImage, bufferedImage);
			}
			final String senderLine = IMGSender.config.Config_ChatFormat.replaceAll("/sender/", sender.getName());
			final ChatColor[][] colors = ImageMessage.toChatColorArray(bufferedImage, size, IMGSender.config.Config_UseNewAlgorithm);
			String[] lines = ImageMessage.toImgMessage(colors, imageChar.getChar());
			if(text != null) {
				if(itemStack == null) {
					if(IMGSender.config.Config_TextCenter) {
						lines = ImageMessage.appendCenteredTextToImg(lines, text);
					}
					else {
						lines = ImageMessage.appendTextToImg(lines, text);
					}
				}
				else {
					final List<String> listedLines = new ArrayList<String>(Arrays.asList(lines));
					listedLines.add("");
					listedLines.add(Joiner.on(' ').join(text));
					lines = listedLines.toArray(new String[listedLines.size()]);
				}
			}
			if(itemStack == null) {
				if(player != null) {
					if(!sender.hasPermission("img.private.send")) {
						sender.sendMessage(ChatColor.RED + "You do not have permission to perform this action.");
						return;
					}
					if(!player.hasPermission("img.private.receive")) {
						sender.sendMessage(ChatColor.RED + player.getName() + " does not have the right to receive private images.");
						return;
					}
					player.sendMessage(senderLine);
					for(final String line : lines) {
						player.sendMessage(line);
					}
				}
				else {
					if(!sender.hasPermission("img.broadcast.send")) {
						sender.sendMessage(ChatColor.RED + "You do not have permission to perform this action.");
						return;
					}
					Bukkit.broadcast(senderLine, "img.broadcast.receive");
					for(final String line : lines) {
						Bukkit.broadcast(line, "img.broadcast.receive");
					}
				}
			}
			else {
				if(!sender.hasPermission("img.item")) {
					sender.sendMessage(ChatColor.RED + "You do not have permission to perform this action.");
					return;
				}
				final ItemMeta itemMeta = itemStack.getItemMeta();
				itemMeta.setLore(Arrays.asList(lines));
				itemStack.setItemMeta(itemMeta);
			}
		}
		catch(MalformedURLException ex) {
			sender.sendMessage(ChatColor.RED + "This is not a valid URL !");
			return;
		}
		catch(NumberFormatException ex) {
			sender.sendMessage(ChatColor.RED + "'" + size + "' is not a valid number !");
			return;
		}
		catch(IllegalArgumentException ex) {
			sender.sendMessage(ChatColor.RED + "'" + imageChar + "' is not a valid char !");
			sender.sendMessage(ChatColor.RED + "Available chars :");
			for(final ImageChar value : ImageChar.values()) {
				sender.sendMessage(ChatColor.RED + value.name());
			}
			return;
		}
		catch(IOException ex) {
			sender.sendMessage(ChatColor.RED + "Error when IMGSender try to read the image !\n'" + ex + "'\nPlease send an e-mail to me@skyost.eu if you want that I try to correct this bug !");
			return;
		}
	}
	
}
