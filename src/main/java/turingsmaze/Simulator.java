package turingsmaze;

public class Simulator {

    private static final String WORKSPACE_DIR = "workspace/";
    private static final String TESTS_DIR = WORKSPACE_DIR + "tests/";
    
    private static final String SOURCE_FILE = TESTS_DIR + "test-nand.png";
    private static final String DESTINATION_FILE = TESTS_DIR + "out.png";
          
    public static void main(final String... args) throws Exception {
        final Maze maze = new Maze(SOURCE_FILE);
        final Mouse mouse = new Mouse(maze);
        while (mouse.isInMaze()) {
            mouse.updateGate(maze);
            mouse.updateDirection(maze);
            mouse.step();
        }        
        maze.write(DESTINATION_FILE);
    }
}
