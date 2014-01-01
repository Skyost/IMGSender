package fr.skyost.imgsender.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.plugin.Plugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * A simple auto-updater.
 * <br>Please follow this link to read more about checking for updates in your plugin : http://url.skyost.eu/3.
 * <br><br>Thanks to Gravity for his updater (this file use some parts of his code) !
 * 
 * @author Skyost
 */

public class Skyupdater {
	
	private Plugin plugin;
	private File pluginFile;
	private Logger logger;
	private int id;
	private boolean download;
	private boolean announce;
	private boolean isEnabled = true;
	
	private String apiKey;
	private URL url;
	private final Properties config = new Properties();
	private File skyupdaterFolder;
	private File updateFolder;
	private Result result = Result.SUCCESS;
	private String[] updateData;
	private String response;
	private Thread updaterThread;
	
	private static final String SKYUPDATER_VERSION = "0.2.3";
	
	public enum Result {
		
		/**
		 * A new version has been found, downloaded an will be loaded at the next server reload / restart.
		 */
		
		SUCCESS,
		
		/**
		 * A new version has been found but nothing was downloaded.
		 */
		
		UPDATE_AVAILABLE,
		
		/**
		 * No update found.
		 */
		
		NO_UPDATE,
		
		/**
		 * The updater is disabled.
		 */
		
		DISABLED,
		
		/**
		 * An error occured.
		 */
		
		ERROR;
	}
	
	public enum InfoType {
		
		/**
		 * Get the download URL.
		 */
		
		DOWNLOAD_URL,
		
		/**
		 * Get the file name.
		 */
		
		FILE_NAME,
		
		/**
		 * Get the game version.
		 */
		
		GAME_VERSION,
		
		/**
		 * Get the file title.
		 */
		
		FILE_TITLE,
		
		/**
		 * Get the release type.
		 */
		
		RELEASE_TYPE;
	}
	
	/**
	 * Initialize Skyupdater.
	 * 
	 * @param plugin Your plugin.
	 * @param id Your plugin ID on BukkitDev (you can get it here : https://api.curseforge.com/servermods/projects?search=your+plugin).
	 * @param pluginFile The plugin file.
	 * @param download If you want to download the file.
	 * @param announce If you want to announce the progress of the Updater.
	 * @throws IOException InputOutputException.
	 */
	
	public Skyupdater(final Plugin plugin, final int id, final File pluginFile, final boolean download, final boolean announce) throws IOException {
		this.plugin = plugin;
		this.id = id;
		this.pluginFile = pluginFile;
		this.download = download;
		this.announce = announce;
		logger = plugin.getServer().getLogger();
		updateFolder = plugin.getServer().getUpdateFolderFile();
		if(!updateFolder.exists()) {
			updateFolder.mkdir();
		}
		final Properties systemProperties = System.getProperties();
		skyupdaterFolder = new File(plugin.getDataFolder().getParentFile() + systemProperties.getProperty("file.separator", "/") + "Skyupdater");
		if(!skyupdaterFolder.exists()) {
			skyupdaterFolder.mkdir();
		}
		final File propertiesFile = new File(skyupdaterFolder, "skyupdater.properties");
		if(propertiesFile.exists()) {
			config.load(new FileInputStream(propertiesFile));
			String key = config.getProperty("api-key", "NONE");
			if(!(key.equalsIgnoreCase("NONE") || key.equals(""))) {
				apiKey = key;
			}
			if(!config.getProperty("enable", "true").equalsIgnoreCase("true")) {
				result = Result.DISABLED;
				isEnabled = false;
				if(announce) {
					logger.log(Level.INFO, "[Skyupdater] Skyupdater is disabled.");
				}
			}
		}
		else {
			final String lineSeparator = systemProperties.getProperty("line.separator", "\n");
			config.put("enable", "true");
			config.put("api-key", "NONE");
			final StringBuilder header = new StringBuilder();
			header.append("Skyupdater configuration");
			header.append(lineSeparator);
			header.append(lineSeparator);
			header.append("What is Skyupdater ?");
			header.append(lineSeparator);
			header.append("Skyupdater is a simple auto-updater created by Skyost (http://www.skyost.eu) for Bukkit plugins.");
			header.append(lineSeparator);
			header.append(lineSeparator);
			header.append("So what is this file ?");
			header.append(lineSeparator);
			header.append("This file is just a config file for the auto-updater.");
			header.append(lineSeparator);
			header.append(lineSeparator);
			header.append("Configuration :");
			header.append(lineSeparator);
			header.append("'enable': Choose if you want to enable the auto-updater.");
			header.append(lineSeparator);
			header.append("'api-key': OPTIONAL. Your BukkitDev API Key.");
			header.append(lineSeparator);
			header.append(lineSeparator);
			header.append("Good game, I hope you will enjoy your plugins always up-to-date ;)");
			header.append(lineSeparator);
			config.store(new FileOutputStream(propertiesFile), header.toString());
		}
		url = new URL("https://api.curseforge.com/servermods/files?projectIds=" + id);
		updaterThread = new Thread(new UpdaterThread());
		updaterThread.start();
	}
	
	/**
	 * Get the version of Skyupdater.
	 * 
	 * @return The version of Skyupdater.
	 */
	
	public static String getVersion() {
		return SKYUPDATER_VERSION;
	}
	
	/**
	 * Get the result of Skyupdater.
	 * 
	 * @return The result of the update process.
	 */
	
	public Result getResult() {
		waitForThread();
		return result;
	}
	
	/**
	 * Get informations about the latest file.
	 * 
	 * @param type The type of information you want.
	 * 
	 * @return The information you want.
	 */
	
	public String getLatestFileInfo(final InfoType type) {
		waitForThread();
		switch(type) {
		case DOWNLOAD_URL:
			return updateData[0];
		case FILE_NAME:
			return updateData[1];
		case GAME_VERSION:
			return updateData[2];
		case FILE_TITLE:
			return updateData[3];
		case RELEASE_TYPE:
			return updateData[4];
		}
		return null;
	}
	
	/**
	 * Get raw data about the latest file.
	 * 
	 * @return An array string which contains all data you want !
	 */
	
	public String[] getLatestFileData() {
		waitForThread();
		return updateData;
	}
	
	/**
	 * Downloads a file.
	 * 
	 * @param site The URL of the file you want to download.
	 * @param pathTo The path where you want the file to be downloaded.
	 * 
	 * @throws IOException InputOutputException.
	 */
	
	private void download(final String site, final File pathTo) throws IOException {
		final HttpURLConnection con = (HttpURLConnection)new URL(site).openConnection();
		con.addRequestProperty("User-Agent", "Skyupdater v" + SKYUPDATER_VERSION);
		response = con.getResponseCode() + " " + con.getResponseMessage();
		if(!response.startsWith("2")) {
			if(announce) {
				logger.log(Level.INFO, "[Skyupdater] Bad response : '" + response + "' when trying to download the update.");
			}
			result = Result.ERROR;
			return;
		}
		final int size = con.getContentLength();
		int lastPercent = 0;
		int percent = 0;
		float totalDataRead = 0;
		final InputStream is = con.getInputStream();
		final FileOutputStream fos = new java.io.FileOutputStream(pathTo);
		final BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
		byte[] data = new byte[1024];
		int i=0;
		while((i = is.read(data, 0, 1024)) >= 0) {
			totalDataRead += i;
			bos.write(data, 0, i);
			if(announce) {
				percent = ((int)((totalDataRead * 100) / size));
				if(lastPercent != percent) {
					lastPercent = percent;
					logger.log(Level.INFO, "[Skyupdater] " + percent + "%");
				}
			}
		}
		bos.close();
		fos.close();
		is.close();
	}
	
	/**
	 * Compare two versions.
	 * 
	 * @param version1 The version you want to compare to.
	 * @param version2 The version you want to compare with.
	 * 
	 * @return <b>true</b> If <b>version1</b> is inferior than <b>version2</b>.
	 * <br><b>false</b> If <b>version1</b> is superior or equals to <b>version2</b>.
	 */
	
	private static boolean compare(final String version1, final String version2) {
		final int cmp = normalisedVersion(version1).compareTo(normalisedVersion(version2));
		if(cmp < 0) {
			return false;
		}
		else if(cmp > 0) {
			return true;
		}
		return false;
	}
	
	private static String normalisedVersion(String version) {
		return normalisedVersion(version, ".", 4);
	}

	private static String normalisedVersion(String version, String sep, int maxWidth) {
		String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
		StringBuilder sb = new StringBuilder();
		for(String s : split) {
			sb.append(String.format("%" + maxWidth + 's', s));
		}
		return sb.toString();
	}
	
	/**
	 * As the result of Updater output depends on the thread's completion, it is necessary to wait for the thread to finish
	 * before allowing anyone to check the result.
	 * 
	 * @author Gravity from his Updater.
	 */
	
	private void waitForThread() {
		if(updaterThread != null && updaterThread.isAlive()) {
			try {
				updaterThread.join();
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	public class UpdaterThread implements Runnable {
	
		@Override
		public void run() {
			if(isEnabled) {
				try {
					final HttpURLConnection con = (HttpURLConnection)url.openConnection();
					con.addRequestProperty("User-Agent", "Skyupdater v" + SKYUPDATER_VERSION);
					if(apiKey != null) {
						con.addRequestProperty("X-API-Key", apiKey);
					}
					response = con.getResponseCode() + " " + con.getResponseMessage();
					if(!response.startsWith("2")) {
						if(announce) {
							logger.log(Level.INFO, "[Skyupdater] Bad response : '" + response + "'. Maybe your API Key is invalid ?");
						}
						result = Result.ERROR;
						return;
					}
					final BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
					final String response = reader.readLine();
					if(!response.equals("[]")) {
						final JSONArray array = (JSONArray)JSONValue.parseWithException(response);
						final JSONObject latest = (JSONObject)array.get(array.size() - 1);
						updateData = new String[] {String.valueOf(latest.get("downloadUrl")), String.valueOf(latest.get("fileName")), String.valueOf(latest.get("gameVersion")), String.valueOf(latest.get("name")), String.valueOf(latest.get("releaseType"))};
						if(compare(updateData[3].split(" v")[1], plugin.getDescription().getVersion()) && updateData[0].toLowerCase().endsWith(".jar")) {
							result = Result.UPDATE_AVAILABLE;
							if(download) {
								if(announce) {
									logger.log(Level.INFO, "[Skyupdater] Downloading a new update : " + updateData[3] + "...");
								}
								download(updateData[0], new File(updateFolder, pluginFile.getName()));
								result = Result.SUCCESS;
								if(announce) {
									logger.log(Level.INFO, "[Skyupdater] The update of '" + plugin.getName() + "' has been downloaded and installed. It will be loaded at the next server load / reload.");
								}
							}
							else if(announce) {
								logger.log(Level.INFO, "[Skyupdater] An update has been found for '" + plugin.getName() + "' but nothing was downloaded.");
							}
							return;
						}
						else {
							result = Result.NO_UPDATE;
							if(announce) {
								logger.log(Level.INFO, "[Skyupdater] No update found for '" + plugin.getName() + "'.");
							}
						}
					}
					else {
						logger.log(Level.SEVERE, "[Skyupdater] The ID '" + id + "' was not found (or no files found for this project) !");
						result = Result.ERROR;
					}
				}
				catch (Exception ex) {
					ex.printStackTrace();
					result = Result.ERROR;
				}
			}
		}
		
	}

}
