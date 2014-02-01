package fr.skyost.imgsender;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.skyost.imgsender.imgmessage.ImageChar;
import fr.skyost.imgsender.imgmessage.ImageMessage;
import fr.skyost.imgsender.tasks.Downloader;
import fr.skyost.imgsender.utils.MetricsLite;
import fr.skyost.imgsender.utils.Skyupdater;
import fr.skyost.imgsender.utils.Utils;

/**
 * IMGSender main class.
 * <br>Thanks to <b>Goblom</b> for the help !
 * 
 * @author Skyost.
 */

public class IMGSender extends JavaPlugin {
	
	private static final HashMap<URL, Integer> cache = new HashMap<URL, Integer>();
	private static File cacheDir;
	
	private ConfigFile config;
	
	@Override
	public final void onEnable() {
		try {
			config = new ConfigFile(this);
			config.init();
			cacheDir = new File(config.Config_CacheDirectory);
			if(!cacheDir.exists()) {
				cacheDir.mkdir();
			}
			if(config.ImageSize_Min < 5) {
				config.ImageSize_Min = 5;
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The MinImageSize must be superior than 5 ! It has been reverted back to 5.");
			}
			if(config.Config_EnableUpdater) {
				new Skyupdater(this, 70595, this.getFile(), true, true);
			}
			if(!(Utils.isFloat(config.RescaleOp_Offset) || Utils.isFloat(config.RescaleOp_ScaleFactor))) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "You must enter a number like this : '0.0' in your RescaleOp settings !");
				config.RescaleOp_Offset = "0.0";
				config.RescaleOp_ScaleFactor = "0.0";
			}
			new MetricsLite(this).start();
			config.save();
			this.getCommand("img").setUsage(ChatColor.RED + "/img [url] [size] [char] [text] [player].");
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public final void onDisable() {
		for(final File file : cacheDir.listFiles()) {
			file.delete();
		}
	}
	
	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		URL url = null;
		int size = config.Default_Size;
		ImageChar imgChar = config.Default_Char;
		String[] text = null;
		Player player = null;
		if(args.length >= 1) {
			try {
				url = new URL(args[0]);
			}
			catch(MalformedURLException ex) {
				sender.sendMessage(ChatColor.RED + "This is not a valid URL !");
				return true;
			}
			for(final String word : config.Config_Unauthorized_Words) {
				if(url.toString().contains(word)) {
					sender.sendMessage(ChatColor.RED + "Your URL contains an unauthorized word : '" + word + "'.");
					return true;
				}
			}
			if(args.length >= 2) {
				try {
					size = Integer.parseInt(args[1]);
					if(size < config.ImageSize_Min || size > config.ImageSize_Max) {
						sender.sendMessage(ChatColor.RED + "The image size must be between '" + config.ImageSize_Min + "' and '" + config.ImageSize_Max + "' !");
						return true;
					}
				}
				catch(NumberFormatException ex) {
					sender.sendMessage(ChatColor.RED + "'" + args[1] + "' is not a valid number !");
					return true;
				}
				if(args.length >= 3) {
					try {
						imgChar = ImageChar.valueOf(args[2]);
					}
					catch(IllegalArgumentException ex) {
						sender.sendMessage(ChatColor.RED + "'" + args[2] + "' is not a valid char !");
						sender.sendMessage(ChatColor.RED + "Available chars :");
						for(final ImageChar value : ImageChar.values()) {
							sender.sendMessage(ChatColor.RED + value.name());
						}
						return true;
					}
					if(args.length >= 4) {
						player = Bukkit.getPlayer(args[args.length - 1]);
						final StringBuilder result = new StringBuilder();
						if(player == null) {
							for(int i = 3; i < args.length; i++) {
							   result.append(args[i]);
							   result.append(" ");
							}
							text = result.toString().split(" ");
						}
						else {
							for(int i = 3; i < args.length - 1; i++) {
								result.append(args[i]);
								result.append(" ");
							}
							text = result.toString().split(" ");
						}
					}
				}
			}
		}
		else {
			try {
				url = new URL(config.Default_URL);
			}
			catch(MalformedURLException ex) {
				sender.sendMessage(ChatColor.RED + "The default URL is not valid !");
			}
		}
		BufferedImage image = null;
		if(cache.get(url) == null) {
			if(cache.size() == config.Cache_Size_Max) {
				for(final File file : cacheDir.listFiles()) {
					file.delete();
				}
				cache.clear();
			}
			final File imageFile = new File(cacheDir, String.valueOf(cache.size() + 1));
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
				return true;
			}
			try {
				image = ImageIO.read(imageFile);
			   	if(image != null) {
			   		cache.put(url, cache.size() + 1);
			   	}
			   	else {
				   	sender.sendMessage(ChatColor.RED + "This file is not an image.");
				   	imageFile.delete();
				   	return true;
				}
			}
			catch(Exception ex) {
				sender.sendMessage(ChatColor.RED + "Error when IMGSender try to read the image !\n'" + ex.getLocalizedMessage() + "'\nPlease send an e-mail to me@skyost.eu if you want I try to correct this bug !");
				return true;
			}
		}
		else {
			try {
				image = ImageIO.read(new File(cacheDir, String.valueOf(cache.get(url))));
			}
			catch(Exception ex) {
				sender.sendMessage(ChatColor.RED + "Error when IMGSender try to read the image !\n'" + ex.getLocalizedMessage() + "'\nPlease send an e-mail to me@skyost.eu if you want I try to correct this bug !");
				return true;
			}
		}
		if(!(config.RescaleOp_Offset.equals("0.0") || config.RescaleOp_Offset.equals("0")) && (config.RescaleOp_ScaleFactor.equals("0.0") || config.RescaleOp_ScaleFactor.equals("0"))) {
			new RescaleOp(Float.parseFloat(config.RescaleOp_Offset), Float.parseFloat(config.RescaleOp_ScaleFactor), null).filter(image, image);
		}
		final String senderLine = config.Config_ChatFormat.replaceAll("/sender/", sender.getName());
		final ChatColor[][] colors = ImageMessage.toChatColorArray(image, size, config.Config_UseNewAlgorithm);
		String[] lines = ImageMessage.toImgMessage(colors, imgChar.getChar());
		if(text != null) {
			if(config.Config_TextCenter) {
				lines = ImageMessage.appendCenteredTextToImg(lines, text);
			}
			else {
				lines = ImageMessage.appendTextToImg(lines, text);
			}
		}
		if(player != null) {
			if(!sender.hasPermission("img.private.send")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to perform this action.");
				return true;
			}
			if(!player.hasPermission("img.private.receive")) {
				sender.sendMessage(ChatColor.RED + player.getName() + " does not have the right to receive private images.");
				return true;
			}
			player.sendMessage(senderLine);
			for(final String line : lines) {
				player.sendMessage(line);
			}
		}
		else {
			if(!sender.hasPermission("img.broadcast.send")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to perform this action.");
				return true;
			}
			Bukkit.broadcast(senderLine, "img.broadcast.receive");
			for(final String line : lines) {
				Bukkit.broadcast(line, "img.broadcast.receive");
			}
		}
		return true;
	}
	
	public static final File getCacheDir() {
		return cacheDir;
	}
	
	public static boolean isCached(final String imageURL) {
		if(cache.get(imageURL) != null) {
			return true;
		}
		return false;
	}
	
}