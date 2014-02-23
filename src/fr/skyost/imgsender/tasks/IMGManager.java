package fr.skyost.imgsender.tasks;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.skyost.imgsender.IMGSender;
import fr.skyost.imgsender.imgmessage.ImageChar;
import fr.skyost.imgsender.imgmessage.ImageMessage;

/**
 * IMGManager is used to send an image.
 * 
 * @author Skyost.
 */

public class IMGManager extends Thread {
	
	private final CommandSender sender;
	private final String url;
	private final String size;
	private final String imageChar;
	private final String[] text;
	private final Player player;
	
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
	 */
	
	public IMGManager(final CommandSender sender, final String url, final String size, final String imageChar, final String[] text, final Player player) {
		this.sender = sender;
		this.url = url == null ? IMGSender.config.Default_URL : url;
		this.size = size == null ? String.valueOf(IMGSender.config.Default_Size) : size;
		this.imageChar = imageChar == null ? IMGSender.config.Default_Char.name() : imageChar;
		this.text = text;
		this.player = player;
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
			if(size < IMGSender.config.ImageSize_Min || size > IMGSender.config.ImageSize_Max) {
				sender.sendMessage(ChatColor.RED + "The image size must be between '" + IMGSender.config.ImageSize_Min + "' and '" + IMGSender.config.ImageSize_Max + "' !");
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
				if(IMGSender.config.Config_TextCenter) {
					lines = ImageMessage.appendCenteredTextToImg(lines, text);
				}
				else {
					lines = ImageMessage.appendTextToImg(lines, text);
				}
			}
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
