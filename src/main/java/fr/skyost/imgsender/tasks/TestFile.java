package fr.skyost.imgsender.tasks;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Goblom
 */
public class TestFile implements Runnable {

    private final File file;
    private boolean isImage = false;
    
    public TestFile(File file) {
        this.file = file; 
        run();
    }
    
    public void run() {
        try {
            Image image = ImageIO.read(file);
            if (image != null) { isImage = true; }
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    public boolean isImage() {
        return isImage;
    }
    
    public File getFile() {
        return file;
    }
}
