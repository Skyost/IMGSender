package fr.skyost.imgsender;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.plugin.Plugin;

import fr.skyost.imgsender.utils.Config;
import fr.skyost.imgsender.utils.ImgMessage.ImgChar;

public class ConfigFile extends Config {
	
	public boolean Config_EnableUpdater = true;
	public ArrayList<String> Config_Unauthorized_Words = new ArrayList<String>();
	
	public int Cache_Size_Max = 10;
	
	public int ImageSize_Min = 5;
	public int ImageSize_Max = 25;
	
	public String Default_URL = "http://www.skyost.eu/wp-content/uploads/2013/12/IMGSender.png";
	public int Default_Size = 15;
	public ImgChar Default_Char = ImgChar.MEDIUM_SHADE;
	
	public ConfigFile(Plugin plugin) {
		CONFIG_FILE = new File(plugin.getDataFolder(), "config.yml");
		CONFIG_HEADER = "##################################################### #";
		CONFIG_HEADER += "\n             IMGSender Configuration                  #";
		CONFIG_HEADER += "\n See http://dev.bukkit.org/bukkit-plugins/img-sender  #";
		CONFIG_HEADER += "\n              for more informations.                  #";
		CONFIG_HEADER += "\n##################################################### #";
		
		Config_Unauthorized_Words.add("porn");
		Config_Unauthorized_Words.add("dick");
		Config_Unauthorized_Words.add("4tube");
	}
	
}
