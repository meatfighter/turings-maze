
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class SlowEmulator {

    private static final String WORKSPACE_DIR = "workspace/";
    private static final String TESTS_DIR = WORKSPACE_DIR + "tests/";
    
    private static final String SOURCE_FILE = TESTS_DIR + "test-buffer.png";
    private static final String DESTINATION_FILE = TESTS_DIR + "out.png";
       
    private void launch() throws Exception {
        final byte[][] maze = readMaze();
        
        // TODO UPDATE MAZE
        
        writeMaze(maze);
    }
    
    private void writeMaze(final byte[][] maze) throws Exception {
        final int width = maze[0].length;
        final int height = maze.length;
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                switch (maze[y][x]) {
                    case Tiles.BLACK:
                        image.setRGB(x, y, Colors.BLACK);
                        break;
                    case Tiles.GRAY:
                        image.setRGB(x, y, Colors.GRAY);
                        break;
                    case Tiles.RED:
                        image.setRGB(x, y, Colors.RED);
                        break;
                    case Tiles.GREEN:
                        image.setRGB(x, y, Colors.GREEN);
                        break;
                }
            }
        }        
        ImageIO.write(image, DESTINATION_FILE.substring(DESTINATION_FILE.length() - 3), new File(DESTINATION_FILE));
    }
    
    private final byte[][] readMaze() throws Exception {
        final BufferedImage image = ImageIO.read(new File(SOURCE_FILE));
        final int width = image.getWidth();
        final int height = image.getHeight();
        final byte[][] maze = new byte[height][width];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                final int color = image.getRGB(x, y) & 0xFFFFFF;
                switch (color) {
                    case Colors.BLACK:
                        maze[y][x] = Tiles.BLACK;
                        break;
                    case Colors.GRAY:
                        maze[y][x] = Tiles.GRAY;
                        break;
                    case Colors.RED:
                        maze[y][x] = Tiles.RED;
                        break;
                    case Colors.GREEN:
                        maze[y][x] = Tiles.GREEN;
                        break;
                    default:
                        throw new IllegalArgumentException(
                                String.format("Invalid pixel color %08x at (%d, %d)", color, x, y));
                }
            }
        }
        return maze;
    }
    
    public static void main(final String... args) throws Exception {
        new SlowEmulator().launch();
    }
}
