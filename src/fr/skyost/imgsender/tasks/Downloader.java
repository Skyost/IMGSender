package fr.skyost.imgsender.tasks;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Downloader extends Thread {
	
	private String response;
	private String site;
	private File pathTo;
	private CommandSender sender;
	
	public Downloader(final String site, final File pathTo, final CommandSender sender) {
		this.site = site;
		this.pathTo = pathTo;
		this.sender = sender;
	}

	@Override
	public void run() {
		try {
			final String senderName = sender.getName();
			if(!senderName.equals("CONSOLE")) {
				Bukkit.getConsoleSender().sendMessage(senderName + " is downloading '" + site + "'...");
			}
			sender.sendMessage(ChatColor.GOLD + "Downloading " + site + "...");
			final HttpURLConnection connection = (HttpURLConnection)new URL(site).openConnection();
			connection.addRequestProperty("User-Agent", "IMGSender by Skyost");
			response = connection.getResponseCode() + " " + connection.getResponseMessage();
			if(!response.startsWith("2")) {
				return;
			}
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
			sender.sendMessage(ChatColor.GOLD + "Done !");
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public final String getResponse() {
		waitForThread();
		return response;
	}
	
	private void waitForThread() {
		if(this.isAlive()) {
			try {
				this.join();
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

}
