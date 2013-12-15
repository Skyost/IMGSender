package fr.skyost.imgsender;

import java.io.File;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.skyost.imgsender.utils.ImgMessage;
import fr.skyost.imgsender.utils.ImgMessage.ImgChar;
import fr.skyost.imgsender.utils.MetricsLite;
import fr.skyost.imgsender.utils.Updater;
import fr.skyost.imgsender.utils.Updater.UpdateType;
import fr.skyost.imgsender.tasks.Downloader;
import fr.skyost.imgsender.tasks.TestFile;

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
			if(config.ImageSize_Max > 25) {
				config.ImageSize_Max = 25;
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The MinImageSize must be inferior than 25 ! It has been reverted back to 25.");
			}
			if(config.Config_EnableUpdater) {
				new Updater(this, 70595, this.getFile(), UpdateType.DEFAULT, true);
			}
			new MetricsLite(this).start();
			config.save();
			this.getCommand("img").setUsage(ChatColor.RED + "/img [url] [size] [char] [player].");
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
				Player player = null;
				int size = config.Default_Size;
				ImgChar imgChar = config.Default_Char;
				if(args.length >= 1) {
					url = args[0];
					String lowerUrl = url.toLowerCase();
//					if(!(lowerUrl.endsWith("jpeg") || lowerUrl.endsWith("jpg") || lowerUrl.endsWith("png") || lowerUrl.endsWith("bmp") || lowerUrl.endsWith("wbmp") || lowerUrl.endsWith("gif"))) {
//						sender.sendMessage(ChatColor.RED + "Please enter an url which ends with only :");
//						sender.sendMessage(ChatColor.RED + ".jpeg");
//						sender.sendMessage(ChatColor.RED + ".jpg");
//						sender.sendMessage(ChatColor.RED + ".png");
//						sender.sendMessage(ChatColor.RED + ".bmp");
//						sender.sendMessage(ChatColor.RED + ".wbmp");
//						sender.sendMessage(ChatColor.RED + ".gif");
//						return true;
//					}
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
					player = Bukkit.getPlayer(args[3]);
					if(player == null) {
						sender.sendMessage(ChatColor.RED + "This player was not found !");
						return true;
					}
				}
				if(cache.get(url) == null) {
					if(cache.size() == config.Cache_Size_Max) {
						for(File file : cacheDir.listFiles()) {
							file.delete();
						}
						cache.clear();
					}
                                        
					new Downloader(url, new File(cacheDir, String.valueOf(cache.size() + 1)), sender).run();
                                        TestFile checkImage = new TestFile(new File(cacheDir, String.valueOf(cache.size() + 1)));
                                        if (checkImage.isImage()) {
                                                cache.put(url, cache.size() + 1);
                                        } else {
                                                sender.sendMessage(ChatColor.RED + "URL is not an Image");
                                        }
				}
				ChatColor[][] colors = ImgMessage.toChatColorArray(ImageIO.read(new File(cacheDir, String.valueOf(cache.get(url)))), size);
				String[] lines = ImgMessage.toImgMessage(colors, imgChar.getChar());
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
