package fr.skyost.imgsender.tasks;

import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;

/**
 *
 * @author Goblom
 */
public class TestFile implements Runnable {

	private final File file;
	private boolean isImage = false;
	
	public TestFile(final File file) {
		this.file = file; 
		run();
	}
	
	public void run() {
		try {
			Image image = ImageIO.read(file);
			if(image != null) {
				isImage = true;
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			isImage = false;
		}
	}
	
	public boolean isImage() {
		return isImage;
	}
	
	public File getFile() {
		return file;
	}
}
