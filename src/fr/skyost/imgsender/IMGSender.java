package fr.skyost.imgsender;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.skyost.imgsender.tasks.Downloader;
import fr.skyost.imgsender.utils.ImgMessage;
import fr.skyost.imgsender.utils.MetricsLite;
import fr.skyost.imgsender.utils.ImgMessage.ImgChar;
import fr.skyost.imgsender.utils.Skyupdater;

/**
 * IMGSender main class.
 * <br>Thanks to <b>Goblom</b> for the help !
 * 
 * @author Skyost.
 *
 */

public class IMGSender extends JavaPlugin {
	
	private final HashMap<String, Integer> cache = new HashMap<String, Integer>();
	private ConfigFile config;
	private File cacheDir;
	
	@Override
	public final void onEnable() {
		try {
			config = new ConfigFile(this);
			config.init();
			cacheDir = new File(this.getDataFolder() + "\\cache");
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
			new MetricsLite(this).start();
			config.save();
			this.getCommand("img").setUsage(ChatColor.RED + "/img [url] [size] [char] [text] [player].");
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public final void onDisable() {
		for(File file : cacheDir.listFiles()) {
			file.delete();
		}
	}
	
	public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
		try {
			if(cmd.getName().equalsIgnoreCase("img")) {
				String url = config.Default_URL;
				int size = config.Default_Size;
				ImgChar imgChar = config.Default_Char;
				String[] text = null;
				Player player = null;
				if(args.length >= 1) {
					url = args[0];
					for(String word : config.Config_Unauthorized_Words) {
						if(url.contains(word)) {
							sender.sendMessage(ChatColor.RED + "Your URL contains an unauthorized word : '" + word + "'.");
							return true;
						}
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
				}
				if(args.length >= 3) {
					try {
						imgChar = ImgChar.valueOf(args[2]);
					}
					catch(IllegalArgumentException ex) {
						sender.sendMessage(ChatColor.RED + "'" + args[2] + "' is not a valid char !");
						sender.sendMessage(ChatColor.RED + "Available chars :");
						for(ImgChar value : ImgChar.values()) {
							sender.sendMessage(ChatColor.RED + value.name());
						}
						return true;
					}
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
				BufferedImage image = null;
				if(cache.get(url) == null) {
					if(cache.size() == config.Cache_Size_Max) {
						for(File file : cacheDir.listFiles()) {
							file.delete();
						}
						cache.clear();
					}
					final File imageFile = new File(cacheDir, String.valueOf(cache.size() + 1));
					new Downloader(url, imageFile, sender).run();
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
				else {
					image = ImageIO.read(new File(cacheDir, String.valueOf(cache.get(url))));
				}
				final ChatColor[][] colors = ImgMessage.toChatColorArray(image, size);
				String[] lines = ImgMessage.toImgMessage(colors, imgChar.getChar());
				if(text != null) {
					lines = ImgMessage.appendTextToImg(lines, text);
				}
				if(player != null) {
					player.sendMessage("<" + sender.getName() + "> :");
					for(String line : lines) {
						player.sendMessage(line);
					}
				}
				else {
					for(Player online : Bukkit.getOnlinePlayers()) {
						online.sendMessage("<" + sender.getName() + "> :");
						for(String line : lines) {
							online.sendMessage(line);
						}
					}
					final CommandSender console = Bukkit.getConsoleSender();
					console.sendMessage("<" + sender.getName() + "> :");
					for(String line : lines) {
						console.sendMessage(line);
					}
				}
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return true;
	}
	
	public File getCacheDir() {
		return cacheDir;
	}
	
	public boolean isCached(final String imageURL) {
		if(cache.get(imageURL) != null) {
			return true;
		}
		return false;
	}

}
