package turingsmaze.generators;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Display256Generator {
    
    private static final String WORKSPACE_DIR = "workspace/";
    private static final String COMPONENTS_DIR = WORKSPACE_DIR + "components/"; 
    
    private static final String DESTINATION_FILE = COMPONENTS_DIR + "display256.png";

    private static final int MARGIN = 8;
    private static final int DEMUX_INTERVAL = 4;
    private static final int DISPLAY_INTERVAL = 18;
    
    private void launch() throws Exception {

        final BufferedImage image = new BufferedImage(10000, 10000, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = image.createGraphics();
        g.setColor(new Color(turingsmaze.Color.GRAY));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.setColor(new Color(turingsmaze.Color.BLACK));
        
        final int y = image.getHeight() - 1 - ((512 * DISPLAY_INTERVAL) / 2) - DISPLAY_INTERVAL / 2;
        
        for (int i = 0; i < 256; ++i) {
            g.drawLine(MARGIN + 2 * i, 0, MARGIN + 2 * i, image.getHeight() - 1);
            g.drawLine(MARGIN + 2 * i, image.getHeight() - 1 - i * DISPLAY_INTERVAL, 
                    2 * MARGIN + 2 * 256, image.getHeight() - 1 - i * DISPLAY_INTERVAL);
            g.drawLine(MARGIN + 2 * (255 - i), image.getHeight() - 1 - i * DISPLAY_INTERVAL - 256 * DISPLAY_INTERVAL, 
                    2 * MARGIN + 2 * 256, image.getHeight() - 1 - i * DISPLAY_INTERVAL - 256 * DISPLAY_INTERVAL);                                   
            g.drawLine(0, y + DEMUX_INTERVAL * i, MARGIN + 2 * (255 - i), y + DEMUX_INTERVAL * i);
        }        
        
        g.dispose();
        ImageIO.write(image, "png", new File(DESTINATION_FILE));
    }    

    public static void main(final String... args) throws Exception {
        new Display256Generator().launch();
    }
}
