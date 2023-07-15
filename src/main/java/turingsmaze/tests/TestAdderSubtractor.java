package turingsmaze.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import turingsmaze.Coordinates;
import turingsmaze.Direction;
import turingsmaze.Switch;
import turingsmaze.Maze;
import turingsmaze.Mouse;
import turingsmaze.Response;
import turingsmaze.Tile;

public class TestAdderSubtractor {
    
    private static final String WORKSPACE_DIR = "workspace/";
    private static final String TESTS_DIR = WORKSPACE_DIR + "tests/";
    
    private static final String SOURCE_FILE = TESTS_DIR + "test-adder-subtractor-16.png";
    
    public static final int DECIMAL_PLACES = 10;

    private void launch() throws Exception {
        final Maze maze = new Maze(SOURCE_FILE);
        final Switch[] gates = findGates(maze);
        final Response[][] responses = createResponseTable(maze, gates);
        
        final Switch[] aGates = extractRegister(gates, 449, 1880);
        final Switch[] bGates = extractRegister(gates, 449, 1774);
        final Switch[] phGates = extractRegister(gates, 445, 553);
  
        final Random random = ThreadLocalRandom.current();
        
        while (true) {
            final int a = random.nextInt(0x10000);
            final int b = random.nextInt(0x10000);
            final int expectedValue = subtract(a, b);

            storeValue(aGates, a);
            storeValue(bGates, b);
            emulate(gates, responses);
            final int actualValue = loadValue(phGates);

            if (expectedValue != actualValue) {
                System.out.format("%d + %d = %d, %d%n", a, b, expectedValue, actualValue);
            } 
        }
    }
    
    private void emulate(final Switch[] gates, final Response[][] responses) {
        int direction = Direction.NORTH;
        Switch gate = gates[0];
        
        while (true) {
            final Response response = responses[direction][gate.index];            
            final Switch[] reds = response.reds;
            for (int i = reds.length - 1; i >= 0; --i) {
                reds[i].red = true;
            }
            final Switch[] greens = response.greens;
            for (int i = greens.length - 1; i >= 0; --i) {
                greens[i].red = false;
            }
            direction = response.direction;
            gate = response.destination;
            if (gate.index == 1) {
                break;
            }
            if (gate.red) {
                direction = (direction + 2) & 3;
            }
        }
    }
    
    private int loadValue(final Switch[] register) {
        int value = 0;
        for (int i = 0; i < 16; ++i) {
            value <<= 1;
            value |= register[i].red ? 0 : 1;
        }
        return value;
    }
    
    private void storeValue(final Switch[] register, final int value) {
        for (int i = 15, v = value; i >= 0; --i, v >>= 1) {
            register[i].red = (v & 1) == 0;            
        }
    }
    
    private Switch[] extractRegister(final Switch[] gates, final int x, final int minY) {
        final List<Switch> gs = new ArrayList<>();
        for (final Switch gate : gates) {
            if (gate.coordinates.x == x && gate.coordinates.y >= minY) {
                gs.add(gate);
            }
        }
        gs.sort((a, b) -> Integer.compare(a.coordinates.y, b.coordinates.y));
        return gs.subList(0, 16).toArray(new Switch[0]);
    }
    
    private Response[][] createResponseTable(final Maze maze, final Switch[] gates) {
        
        final Map<Coordinates, Switch> gatesMap = createGatesMap(gates);
        final Response[][] responses = new Response[4][gates.length];
        for (int direction = 3; direction >= 0; --direction) {
            for (int index = gates.length - 1; index >= 0; --index) {
                responses[direction][index] = createResponse(maze, gatesMap, direction, gates[index]);
            }
        }
        return responses;
    }
    
    private Response createResponse(final Maze maze, final Map<Coordinates, Switch> gatesMap, final int direction, 
            final Switch gate) {
        
        final Mouse mouse = new Mouse(gate.coordinates.x, gate.coordinates.y, direction);
        final List<Switch> reds = new ArrayList<>();
        final List<Switch> greens = new ArrayList<>();
        
        while (true) {                      
            mouse.updateDirection(maze);
            mouse.step();
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
                greens.add(gatesMap.get(new Coordinates(mouse.getX(), mouse.getY())));                
            } else {
                mouse.setDirection(Direction.NORTH);
                reds.add(gatesMap.get(new Coordinates(mouse.getX(), mouse.getY())));
            }            
        }
        
        return new Response(reds.toArray(new Switch[0]), greens.toArray(new Switch[0]), 
                gatesMap.get(new Coordinates(mouse.getX(), mouse.getY())), mouse.getDirection());
    }
    
    private Map<Coordinates, Switch> createGatesMap(final Switch[] gates) {
        final Map<Coordinates, Switch> gatesMap = new HashMap<>();
        for (final Switch gate : gates) {
            gatesMap.put(gate.coordinates, gate);
        }
        return gatesMap;
    }
    
    private Switch[] findGates(final Maze maze) {
        final List<Switch> gates = new ArrayList<>();
        
        for (int x = maze.getWidth() - 1, y = maze.getHeight() - 1; x >= 0; --x) {
            if (maze.getTile(x, y) == Tile.BLACK) {
                gates.add(new Switch(0, new Coordinates(x, y), false));
                break;
            }
        }
        for (int x = maze.getWidth() - 1; x >= 0; --x) {
            if (maze.getTile(x, 0) == Tile.BLACK) {
                gates.add(new Switch(1, new Coordinates(x, 0), false));
                break;
            }
        }
        for(int y = maze.getHeight() - 2, index = 2; y > 0; --y) {
            for (int x = maze.getWidth() - 1; x >= 0; --x) {
                switch (maze.getTile(x, y)) {
                    case Tile.RED:
                        gates.add(new Switch(index++, new Coordinates(x, y), true));
                        break;
                    case Tile.GREEN:
                        gates.add(new Switch(index++, new Coordinates(x, y), false));
                        break;
                }                
            }
        }        
        
        return gates.toArray(new Switch[0]);
    }
    
    public int add(final int a, final int b) {
        return 0xFFFF & ((0xFFFF & a) + (0xFFFF & b));
    }
    
    public int subtract(final int a, final int b) {
        return 0xFFFF & ((0xFFFF & a) - (0xFFFF & b));
    }
    
    public static void main(final String... args) throws Exception {
        new TestAdderSubtractor().launch();
    }
}
