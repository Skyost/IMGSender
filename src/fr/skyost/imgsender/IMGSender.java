package fr.skyost.imgsender;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import fr.skyost.imgsender.listeners.CommandsExecutor;
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
	
	public static final HashMap<URL, Integer> cache = new HashMap<URL, Integer>();
	public static File cacheDir;
	
	public static ConfigFile config;
	
	@Override
	public final void onEnable() {
		try {
			final ConsoleCommandSender console = Bukkit.getConsoleSender();
			config = new ConfigFile(this);
			config.init();
			cacheDir = new File(config.Config_CacheDirectory);
			if(!cacheDir.exists()) {
				cacheDir.mkdir();
			}
			if(config.ImageSize_Min < 5) {
				config.ImageSize_Min = 5;
				console.sendMessage(ChatColor.RED + "The MinImageSize must be superior than 5 ! It has been reverted back to 5.");
			}
			if(config.Config_EnableUpdater) {
				new Skyupdater(this, 70595, this.getFile(), true, true);
			}
			if(!(Utils.isFloat(config.RescaleOp_Offset) || Utils.isFloat(config.RescaleOp_ScaleFactor))) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "You must enter a number like this : '0.0' in your RescaleOp settings !");
				config.RescaleOp_Offset = "0.0";
				config.RescaleOp_ScaleFactor = "0.0";
			}
			try {
				new URL(IMGSender.config.Default_URL);
			}
			catch(MalformedURLException ex) {
				IMGSender.config.Default_URL = "http://www.skyost.eu/wp-content/uploads/2013/12/IMGSender.png";
				console.sendMessage(ChatColor.RED + "The default URL is not valid ! It has been reverted to default.");
			}
			config.save();
			new MetricsLite(this).start();
			for(final PluginCommand pluginCommand : new PluginCommand[]{this.getCommand("img"), this.getCommand("img-item")}) {
				pluginCommand.setUsage(ChatColor.RED + pluginCommand.getDescription());
				pluginCommand.setExecutor(new CommandsExecutor());
			}
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
	
}