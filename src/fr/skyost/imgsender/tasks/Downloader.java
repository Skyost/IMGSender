package fr.skyost.imgsender.tasks;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Downloader extends Thread {
	
	private final URL url;
	private final File file;
	private final CommandSender sender;
	
	private String response;
	
	public Downloader(final URL url, final File file, final CommandSender sender) {
		this.url = url;
		this.file = file;
		this.sender = sender;
	}

	@Override
	public void run() {
		try {
			final String senderName = sender.getName();
			sender.sendMessage(senderName.equals("CONSOLE") ? senderName + " is downloading '" + url + "'..." : "Downloading '" + url + "'...");
			final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.addRequestProperty("User-Agent", "IMGSender by Skyost");
			response = connection.getResponseCode() + " " + connection.getResponseMessage();
			if(!response.startsWith("2")) {
				return;
			}
			final InputStream inputStream = connection.getInputStream();
			final FileOutputStream fileOutputStream = new FileOutputStream(file);
			final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 1024);
			final byte[] data = new byte[1024];
			int i = 0;
			while((i = inputStream.read(data, 0, 1024)) >= 0) {
				bufferedOutputStream.write(data, 0, i);
			}
			bufferedOutputStream.close();
			fileOutputStream.close();
			inputStream.close();
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
