package turingsmaze;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Maze {

    private final byte[][] tiles;
    private final int width;
    private final int height;
    
    public Maze(final int width, final int height) {
        this.width = width;
        this.height = height;
        tiles = new byte[height][width];
    }
    
    public Maze(final String filename) throws IOException {
        final BufferedImage image = ImageIO.read(new File(filename));
        width = image.getWidth();
        height = image.getHeight();
        tiles = new byte[height][width];
        for (int y = height - 1; y >= 0; --y) {
            for (int x = width - 1; x >= 0; --x) {
                final int color = image.getRGB(x, y);
                final boolean red = ((color >> 16) & 0xFF) >= 0x80;
                final boolean green = ((color >> 8) & 0xFF) >= 0x80;
                final boolean blue = (color & 0xFF) >= 0x80;                
                if (!(red || green || blue)) {
                    tiles[y][x] = Tile.BLACK;
                } else if (red && !green && !blue) {
                    tiles[y][x] = Tile.RED;
                } else if (green && !red && !blue) {
                    tiles[y][x] = Tile.GREEN;
                } else {
                    tiles[y][x] = Tile.GRAY;
                }
            }
        }        
    }

    public void write(final String filename) throws IOException {
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = height - 1; y >= 0; --y) {
            for (int x = width - 1; x >= 0; --x) {
                switch (tiles[y][x]) {
                    case Tile.BLACK:
                        image.setRGB(x, y, Color.BLACK);
                        break;
                    case Tile.GRAY:
                        image.setRGB(x, y, Color.GRAY);
                        break;
                    case Tile.RED:
                        image.setRGB(x, y, Color.RED);
                        break;
                    case Tile.GREEN:
                        image.setRGB(x, y, Color.GREEN);
                        break;
                }
            }
        }        
        ImageIO.write(image, filename.substring(filename.length() - 3), new File(filename));
    }    
    
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    public void setTile(final int x, final int y, final byte value) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return;
        }
        tiles[y][x] = value;
    }
    
    public byte getTile(final int x, final int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return Tile.GRAY;
        }
        return tiles[y][x];
    }
}
