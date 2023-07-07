package turingsmaze.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import turingsmaze.Coordinates;
import turingsmaze.Direction;
import turingsmaze.Gate;
import turingsmaze.Maze;
import turingsmaze.Mouse;
import turingsmaze.Response;
import turingsmaze.Tile;

public class TestDemux {
    
    private static final String WORKSPACE_DIR = "workspace/";
    private static final String TESTS_DIR = WORKSPACE_DIR + "tests/";
    
    private static final String SOURCE_FILE = TESTS_DIR + "test-demux-512.png";
    
    private class Reg {
        
        final String name;
        
        public Reg(final String name) {
            this.name = name;
        }
        
        Gate[][] gates;
        boolean lastAllWritesDisabled = true;
        
        boolean isUpdated() {
            boolean allWritesDisabled = true;
            for (final Gate gate : gates[1]) {
                if (!gate.red) {
                    allWritesDisabled = false;
                    break;
                }
            }
            final boolean updated = !lastAllWritesDisabled && allWritesDisabled;
            lastAllWritesDisabled = allWritesDisabled;
            return updated;
        }
        
        void print() {
            printValue(name, loadValue(gates), gates[0].length);
        }
    }
    
    private void printValue(final String name, final int value) {
        printValue(name, value, 16);
    }
    
    private void printValue(final String name, final int value, final int bits) {
        final StringBuilder sb = new StringBuilder();
        sb.append(name).append(": ");        
        for (int i = bits - 1; i >= 0; --i) {
            sb.append((value >> i) & 1);
            if ((i & 3) == 0) {
                sb.append(' ');
            }
        }
        sb.append(' ').append(0xFFFF & value);
        System.out.println(sb);
    }

    private void launch() throws Exception {
        final Maze maze = new Maze(SOURCE_FILE);
        final Gate[] gates = findGates(maze);
        final Response[][] responses = createResponseTable(maze, gates);
        
        final Gate[] addressGates = extract(gates, 3, 2417, 9);
        final Gate[] outputGates = extract(gates, 92, 3, 512);

        for (int i = 0; i < 512; ++i) {            
            storeValue(new Gate[][] { addressGates, null }, i);
            emulate(gates, responses, new Reg[0], maze);
            
            int count = 0;
            for (int j = 0; j < 512; ++j) {
                final Gate gate = outputGates[j];                
                if (!gate.red) {
                    System.out.println(j);
                    if (i != (511 - j)) {
                        System.out.println("Mismatch: " + i + " " + j);
                    }
                    gate.red = true;
                    ++count;
                }
            }
            if (count != 1) {
                System.out.println("Fail: " + i + " " + count);
                break;
            }
        }
    }
    
    private void emulate(final Gate[] gates, final Response[][] responses, final Reg[] regs, final Maze maze) 
            throws Exception {
        
        int direction = Direction.NORTH;
        Gate gate = gates[0];
        
        while (true) {
            for (final Reg reg : regs) {
                if (reg.isUpdated()) {
                    reg.print();
                }
            }            
            
            final Response response = responses[direction][gate.index];            
            final Gate[] reds = response.reds;
            for (int i = reds.length - 1; i >= 0; --i) {
                reds[i].red = true;
            }
            final Gate[] greens = response.greens;
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
    
    private int loadValue(final Gate[][] register) {
        int value = 0;
        for (int i = 0; i < register[0].length; ++i) {
            value <<= 1;
            value |= register[0][i].red ? 0 : 1;
        }
        return value;
    }
    
    private void storeValue(final Gate[][] register, final int value) {
        for (int i = register[0].length - 1, v = value; i >= 0; --i, v >>= 1) {
            register[0][i].red = (v & 1) == 0;            
        }
    }
    
    private Reg extractRegister(final String name, final Gate[] gates, final int x, final int minY) {
        return extractRegister(name, gates, x, minY, 16);
    }
    
    private Reg extractRegister(final String name, final Gate[] gates, final int x, final int minY, final int length) {
        final Reg reg = new Reg(name);
        reg.gates = new Gate[][] {
            extract(gates, x, minY, length),
            extract(gates, x + 2, minY + 4, length),
        };
        return reg;
    }
    
    private Gate[] extract(final Gate[] gates, final int x, final int minY) {
        return extract(gates, x, minY, 16);
    }
    
    private Gate[] extract(final Gate[] gates, final int x, final int minY, final int length) {
        final List<Gate> gs = new ArrayList<>();
        for (final Gate gate : gates) {
            if (gate.coordinates.x == x && gate.coordinates.y >= minY) {
                gs.add(gate);
            }
        }
        gs.sort((a, b) -> Integer.compare(a.coordinates.y, b.coordinates.y));
        return gs.subList(0, length).toArray(new Gate[0]);
    }
    
    private Response[][] createResponseTable(final Maze maze, final Gate[] gates) {
        
        final Map<Coordinates, Gate> gatesMap = createGatesMap(gates);
        final Response[][] responses = new Response[4][gates.length];
        for (int direction = 3; direction >= 0; --direction) {
            for (int index = gates.length - 1; index >= 0; --index) {
                responses[direction][index] = createResponse(maze, gatesMap, direction, gates[index]);
            }
        }
        return responses;
    }
    
    private Response createResponse(final Maze maze, final Map<Coordinates, Gate> gatesMap, final int direction, 
            final Gate gate) {
        
        final Mouse mouse = new Mouse(gate.coordinates.x, gate.coordinates.y, direction);
        final List<Gate> reds = new ArrayList<>();
        final List<Gate> greens = new ArrayList<>();
        
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
        
        return new Response(reds.toArray(new Gate[0]), greens.toArray(new Gate[0]), 
                gatesMap.get(new Coordinates(mouse.getX(), mouse.getY())), mouse.getDirection());
    }
    
    private Map<Coordinates, Gate> createGatesMap(final Gate[] gates) {
        final Map<Coordinates, Gate> gatesMap = new HashMap<>();
        for (final Gate gate : gates) {
            gatesMap.put(gate.coordinates, gate);
        }
        return gatesMap;
    }
    
    private Gate[] findGates(final Maze maze) {
        final List<Gate> gates = new ArrayList<>();
        
        for (int x = maze.getWidth() - 1, y = maze.getHeight() - 1; x >= 0; --x) {
            if (maze.getTile(x, y) == Tile.BLACK) {
                gates.add(new Gate(0, new Coordinates(x, y), false));
                break;
            }
        }
        for (int x = maze.getWidth() - 1; x >= 0; --x) {
            if (maze.getTile(x, 0) == Tile.BLACK) {
                gates.add(new Gate(1, new Coordinates(x, 0), false));
                break;
            }
        }
        for(int y = maze.getHeight() - 2, index = 2; y > 0; --y) {
            for (int x = maze.getWidth() - 1; x >= 0; --x) {
                switch (maze.getTile(x, y)) {
                    case Tile.RED:
                        gates.add(new Gate(index++, new Coordinates(x, y), true));
                        break;
                    case Tile.GREEN:
                        gates.add(new Gate(index++, new Coordinates(x, y), false));
                        break;
                }                
            }
        }        
        
        return gates.toArray(new Gate[0]);
    }
    
    public static void main(final String... args) throws Exception {
        new TestDemux().launch();
    }
}
