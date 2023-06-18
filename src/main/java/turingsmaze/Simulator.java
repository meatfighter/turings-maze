package turingsmaze;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Simulator {

    private static final String WORKSPACE_DIR = "workspace/";
    private static final String TESTS_DIR = WORKSPACE_DIR + "tests/";
    
    private static final String SOURCE_FILE = TESTS_DIR + "test-nand.png";
    private static final String DESTINATION_FILE = TESTS_DIR + "out.png";
       
    private void launch() throws Exception {
        final Maze maze = readMaze();
        final Mouse mouse = createMouse(maze);
        run(mouse, maze);        
        writeMaze(maze);
    }
    
    private void run(final Mouse mouse, final Maze maze) {        
        while (mouse.getY() > 0) {
            switch (maze.getTile(mouse.getX(), mouse.getY())) {
                case Tile.RED:
                    if (mouse.getDirection() == Direction.NORTH) {
                        maze.setTile(mouse.getX(), mouse.getY(), Tile.GREEN);
                    }
                    mouse.reverseDirection();
                    break;
                case Tile.GREEN:
                    switch (mouse.getDirection()) {
                        case Direction.NORTH:
                            mouse.reverseDirection();
                            break;
                        case Direction.SOUTH:
                            maze.setTile(mouse.getX(), mouse.getY(), Tile.RED);
                            mouse.reverseDirection();
                            break;
                    } 
                    break;
            }
            switch (mouse.getGrayTiles(maze)) {
                case 0b000:                    
                    break;
                case 0b001:                    
                    break;
                case 0b010:
                    mouse.turnClockwise();
                    break;
                case 0b011:
                    mouse.turnCounterclockwise();
                    break;
                case 0b100:
                    mouse.turnClockwise();
                    break;
                case 0b101:                    
                    break;
                case 0b110:
                    mouse.turnClockwise();
                    break;
                case 0b111:
                    mouse.reverseDirection();
                    break;
            }
            switch (mouse.getDirection()) {
                case Direction.NORTH:
                    mouse.setY(mouse.getY() - 1);
                    break;
                case Direction.EAST:
                    mouse.setX(mouse.getX() + 1);
                    break;
                case Direction.SOUTH:
                    mouse.setY(mouse.getY() + 1);
                    break;
                case Direction.WEST:
                    mouse.setX(mouse.getX() - 1);
                    break;
            }
        }        
    }
    
    private Mouse createMouse(final Maze maze) {
        for (int x = maze.getWidth() - 1, y = maze.getHeight() - 1; x >= 0; --x) {
            if (maze.getTile(x, y) == Tile.BLACK) {
                return new Mouse(x, y, Direction.NORTH);
            }
        }        
        throw new IllegalArgumentException("Maze does not contain an entrance.");
    }
    
    private void writeMaze(final Maze maze) throws Exception {
        final BufferedImage image = new BufferedImage(maze.getWidth(), maze.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = maze.getHeight() - 1; y >= 0; --y) {
            for (int x = maze.getWidth() - 1; x >= 0; --x) {
                switch (maze.getTile(x, y)) {
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
        ImageIO.write(image, DESTINATION_FILE.substring(DESTINATION_FILE.length() - 3), new File(DESTINATION_FILE));
    }
    
    private final Maze readMaze() throws Exception {
        final BufferedImage image = ImageIO.read(new File(SOURCE_FILE));
        final int width = image.getWidth();
        final int height = image.getHeight();
        final Maze maze = new Maze(width, height);
        for (int y = height - 1; y >= 0; --y) {
            for (int x = width - 1; x >= 0; --x) {
                final int color = image.getRGB(x, y) & 0xFFFFFF;
                switch (color) {
                    case Color.BLACK:
                        maze.setTile(x, y, Tile.BLACK);
                        break;
                    case Color.GRAY:
                        maze.setTile(x, y, Tile.GRAY);
                        break;
                    case Color.RED:
                        maze.setTile(x, y, Tile.RED);
                        break;
                    case Color.GREEN:
                        maze.setTile(x, y, Tile.GREEN);
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
        new Simulator().launch();
    }
}
