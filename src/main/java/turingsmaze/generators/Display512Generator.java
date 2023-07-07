package turingsmaze.generators;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Display512Generator {
    
    private static final String WORKSPACE_DIR = "workspace/";
    private static final String COMPONENTS_DIR = WORKSPACE_DIR + "components/"; 
    
    private static final String DESTINATION_FILE = COMPONENTS_DIR + "display512.png";

    private static final int DEMUX_INTERVAL = 4;
    private static final int DISPLAY_INTERVAL = 18;
    
    private void launch() throws Exception {

        final BufferedImage image = new BufferedImage(10000, 10000, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = image.createGraphics();
        g.setColor(new Color(turingsmaze.Color.GRAY));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        
        
        
        g.dispose();
        ImageIO.write(image, "png", new File(DESTINATION_FILE));
    }    

    public static void main(final String... args) throws Exception {
        new Display512Generator().launch();
    }
}
