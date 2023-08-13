package turingsmaze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class Emulator {

    private void launch(final String inputFilename, final String outputFilename) throws Exception {
        
        final Maze maze = new Maze(inputFilename);
        final Switch[] switches = findSwitches(maze);        
        final Response[][] responses = createResponseTable(maze, switches);
        
        int direction = Direction.NORTH;
        Switch s = switches[0];        
        while (true) {
            final Response response = responses[direction][s.index];          
            final Switch[] reds = response.reds;
            for (int i = reds.length - 1; i >= 0; --i) {
                reds[i].red = true;
            }
            final Switch[] greens = response.greens;
            for (int i = greens.length - 1; i >= 0; --i) {
                greens[i].red = false;
            }
            direction = response.direction;
            s = response.destination;
            if (s.index == 1) {
                break;
            }
            if (s.red) {
                direction = (direction + 2) & 3;
            }
        }

        for (int i = switches.length - 1; i > 1; --i) {
            s = switches[i];
            maze.setTile(s.coordinates.x, s.coordinates.y, s.red ? Tile.RED : Tile.GREEN);
        }
        
        maze.write(outputFilename);
    }
    
    private Response[][] createResponseTable(final Maze maze, final Switch[] switches) {
        
        final Map<Coordinates, Switch> switchesMap = createSwitchesMap(switches);
        final Response[][] responses = new Response[4][switches.length];
        for (int direction = 3; direction >= 0; --direction) {
            final Thread[] threads = new Thread[Runtime.getRuntime().availableProcessors()];
            final int length = switches.length / threads.length;
            for (int i = threads.length - 1, index = switches.length - 1; i >= 0; --i, index -= length) {
                final int d = direction;
                final int startIndex = (i == 0) ? 0 : index - length;
                final int endIndex = index;
                threads[i] = new Thread(() -> createResponses(d, startIndex, endIndex, responses, maze, switchesMap, 
                        switches));
                threads[i].start();
            }
            for (final Thread thread : threads) {
                try {
                    thread.join();
                } catch (final InterruptedException ignored) {                    
                }
            }
        }
        return responses;
    }
    
    private void createResponses(final int direction, final int startIndex, final int endIndex, 
            final Response[][] responses, final Maze maze, final Map<Coordinates, Switch> switchesMap, 
            final Switch[] switches) {        
        for (int index = startIndex; index < endIndex; ++index) {                
            responses[direction][index] = createResponse(maze, switchesMap, direction, switches[index]);
        }
    }
    
    private Response createResponse(final Maze maze, final Map<Coordinates, Switch> switchesMap, final int direction, 
            final Switch s) {
        
        if (s.index >= 2) {
            if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                return null;
            }
            if (maze.getTile(s.coordinates.x - 1, s.coordinates.y) == Tile.GRAY
                    && maze.getTile(s.coordinates.x + 1, s.coordinates.y) == Tile.GRAY) {
                return null;
            }
        }
        
        final Mouse mouse = new Mouse(s.coordinates.x, s.coordinates.y, direction);
        final List<Switch> reds = new ArrayList<>();
        final List<Switch> greens = new ArrayList<>();
        final Set<Mouse> mice = new HashSet<>();
        
        while (true) {
            mouse.updateDirection(maze);
            mouse.step();
            final Mouse m = new Mouse(mouse);
            if (mice.contains(m)) {
                break;
            } else {
                mice.add(m);
            }
            if (mouse.getY() == 0) {
                break;
            }
            if (maze.getTile(mouse.getX(), mouse.getY()) == Tile.BLACK) {
                continue;
            }
            if (mouse.getDirection() == Direction.EAST || mouse.getDirection() == Direction.WEST) {
                break;
            }
            if (mouse.getDirection() == Direction.NORTH) {
                mouse.setDirection(Direction.SOUTH);
                greens.add(switchesMap.get(new Coordinates(mouse.getX(), mouse.getY())));                
            } else {
                mouse.setDirection(Direction.NORTH);
                reds.add(switchesMap.get(new Coordinates(mouse.getX(), mouse.getY())));
            }            
        }
        
        return new Response(reds.toArray(new Switch[0]), greens.toArray(new Switch[0]), 
                switchesMap.get(new Coordinates(mouse.getX(), mouse.getY())), mouse.getDirection());
    }
    
    private Map<Coordinates, Switch> createSwitchesMap(final Switch[] switches) {
        final Map<Coordinates, Switch> switchesMap = new HashMap<>();
        for (final Switch s : switches) {
            switchesMap.put(s.coordinates, s);
        }
        return switchesMap;
    }
    
    private Switch[] findSwitches(final Maze maze) {
        final List<Switch> switches = new ArrayList<>();
        
        for (int x = maze.getWidth() - 1, y = maze.getHeight() - 1; x >= 0; --x) {
            if (maze.getTile(x, y) == Tile.BLACK) {
                switches.add(new Switch(0, new Coordinates(x, y), false));
                break;
            }
        }
        for (int x = maze.getWidth() - 1; x >= 0; --x) {
            if (maze.getTile(x, 0) == Tile.BLACK) {
                switches.add(new Switch(1, new Coordinates(x, 0), false));
                break;
            }
        }
        for(int y = maze.getHeight() - 2, index = 2; y > 0; --y) {
            for (int x = maze.getWidth() - 1; x >= 0; --x) {
                switch (maze.getTile(x, y)) {
                    case Tile.RED:
                        switches.add(new Switch(index++, new Coordinates(x, y), true));
                        break;
                    case Tile.GREEN:
                        switches.add(new Switch(index++, new Coordinates(x, y), false));
                        break;
                }                
            }
        }        
        
        return switches.toArray(new Switch[0]);
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
        
        new Emulator().launch(inputFilename, outputFilename);
    }
}