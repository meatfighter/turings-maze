package turingsmaze;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class Simulator {
    
    private void launch(final String inputFilename, final String outputFilename) throws Exception {
        
        final Maze maze = new Maze(inputFilename);
        final Mouse mouse = new Mouse(maze);

        while (mouse.isInMaze()) {
            mouse.updateGate(maze);
            mouse.updateDirection(maze);
            mouse.step();
        }
        
        maze.write(outputFilename);
    }

    public static void main(final String... args) throws Exception {

        if (args.length == 0) {
            System.out.println("args: -i [ input image ] -o [ output image ]");
            return;
        }
        
        String inputFilename = null;
        String outputFilename = null;
        
        int i = 0;
        while (i < args.length) {
            final String flag = args[i++];
            if (i == args.length) {
                System.err.format("Flag missing argument: %s%n", flag);
                return;
            }
            switch (flag) {
                case "-i":
                    inputFilename = args[i++];
                    break;
                case "-o":
                    outputFilename = args[i++];
                    break;
                default:
                    System.err.format("Unknown flag: %s%n", flag);
                    return;
            }
        }
        
        if (isBlank(inputFilename)) {
            System.err.println("Input image not specified.");
            return;
        }
        if (isBlank(outputFilename)) {
            System.err.println("Output image not specified.");
            return;
        }
        
        new Simulator().launch(inputFilename, outputFilename);
    }
}