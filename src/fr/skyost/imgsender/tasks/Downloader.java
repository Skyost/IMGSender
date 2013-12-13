package fr.skyost.imgsender.tasks;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Downloader implements Runnable {
	
	private String site;
	private File pathTo;
	private CommandSender logger;
	
	public Downloader(final String site, final File pathTo, final CommandSender logger) {
		this.site = site;
		this.pathTo = pathTo;
		this.logger = logger;
	}

	@Override
	public void run() {
		try {
			logger.sendMessage(ChatColor.GOLD + "Downloading " + site + "...");
			URL url = new URL(site);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			float totalDataRead = 0;
			BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
			FileOutputStream fos = new FileOutputStream(pathTo);
			BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
			byte[] data = new byte[1024];
			int i = 0;
			while((i = in.read(data, 0, 1024)) >= 0) {
				totalDataRead = totalDataRead + i;
				bout.write(data, 0, i);
			}  
			bout.close();
			in.close();
			logger.sendMessage(ChatColor.GOLD + "Done !");
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

}
