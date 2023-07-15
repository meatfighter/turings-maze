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

public class TestMultiplier {
    
    private static final String WORKSPACE_DIR = "workspace/";
    private static final String TESTS_DIR = WORKSPACE_DIR + "tests/";
    
    private static final String SOURCE_FILE = TESTS_DIR + "test-multiplier-16.png";
    
    public static final int DECIMAL_PLACES = 10;
    
    private class Reg {
        
        final String name;
        
        public Reg(final String name) {
            this.name = name;
        }
        
        Switch[][] gates;
        boolean lastAllWritesDisabled = true;
        
        boolean isUpdated() {
            boolean allWritesDisabled = true;
            for (final Switch gate : gates[1]) {
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
        final Switch[] gates = findGates(maze);
        final Response[][] responses = createResponseTable(maze, gates);
        
        final Switch[] xGates = extract(gates, 3, 1158);
        final Switch[] yGates = extract(gates, 3, 1050); 
        
        final Reg i = extractRegister("  I", gates, 172, 928, 5);
        final Reg a = extractRegister("  A", gates, 172, 794);
        final Reg pl = extractRegister(" PL", gates, 172, 660);
        final Reg ph = extractRegister(" PH", gates, 172, 526);
        final Reg thl = extractRegister("THL", gates, 172, 392);
        final Reg rs = extractRegister(" RS", gates, 172, 138, 32);
        final Reg ls = extractRegister(" LS", gates, 172, 4);
        final Reg[] regs = { i, a, pl, ph, thl, rs, ls };  
        
//        System.out.format("Expected value: %d%n", multiplyAndPrint(61825, 47090));
        
//        storeValue(new Gate[][] { xGates, null }, 61825);
//        storeValue(new Gate[][] { yGates, null }, 47090);
//        emulate(gates, responses, regs);        
          
        final Random random = ThreadLocalRandom.current();
        
        while (true) {
            final int x = random.nextInt(0x10000);
            final int y = random.nextInt(0x10000);            
            final int expectedValue = multiply(x, y);

            storeValue(new Switch[][] { xGates, null }, x);
            storeValue(new Switch[][] { yGates, null }, y);
            emulate(gates, responses, new Reg[0]);
            final int actualValue = loadValue(ph.gates);

            if (expectedValue != actualValue) {
                System.out.format("%d * %d = %d, %d%n", x, y, expectedValue, actualValue);
                //return;
            } else {
                System.out.println("match");
            }
        }

//        System.out.println(multiply(1, 32768));
    }
    
    private void emulate(final Switch[] gates, final Response[][] responses, final Reg[] regs) {
        int direction = Direction.NORTH;
        Switch gate = gates[0];
        
        while (true) {
            for (final Reg reg : regs) {
                if (reg.isUpdated()) {
                    reg.print();
                }
            }            
            
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
    
    private int loadValue(final Switch[][] register) {
        int value = 0;
        for (int i = 0; i < register[0].length; ++i) {
            value <<= 1;
            value |= register[0][i].red ? 0 : 1;
        }
        return value;
    }
    
    private void storeValue(final Switch[][] register, final int value) {
        for (int i = register[0].length - 1, v = value; i >= 0; --i, v >>= 1) {
            register[0][i].red = (v & 1) == 0;            
        }
    }
    
    private Reg extractRegister(final String name, final Switch[] gates, final int x, final int minY) {
        return extractRegister(name, gates, x, minY, 16);
    }
    
    private Reg extractRegister(final String name, final Switch[] gates, final int x, final int minY, final int length) {
        final Reg reg = new Reg(name);
        reg.gates = new Switch[][] {
            extract(gates, x, minY, length),
            extract(gates, x + 2, minY + 4, length),
        };
        return reg;
    }
    
    private Switch[] extract(final Switch[] gates, final int x, final int minY) {
        return extract(gates, x, minY, 16);
    }
    
    private Switch[] extract(final Switch[] gates, final int x, final int minY, final int length) {
        final List<Switch> gs = new ArrayList<>();
        for (final Switch gate : gates) {
            if (gate.coordinates.x == x && gate.coordinates.y >= minY) {
                gs.add(gate);
            }
        }
        gs.sort((a, b) -> Integer.compare(a.coordinates.y, b.coordinates.y));
        return gs.subList(0, length).toArray(new Switch[0]);
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
    
    public int negate(final int a) {
        return subtract(0, (0xFFFF & a));
    }
    
    public int leftShift(final int a) {
        return 0xFFFF & ((0xFFFF & a) << 1);
    }
    
    public int multiply(final int a, final int b) {
        
        int A = a;
        int PH = b;
        final boolean b15 = ((b >> 15) & 1) == 1;
        
        if (b15) {
            PH = negate(PH);
        }
        int PL = PH;
        int I = 0;
        PH = 0;
        
        while (true) {
            if ((PL & 1) == 1) {
                PH = add(PH, A);
            }
            
            long P = (PH << 16) | PL;
            P &= 0xFFFFFFFF;
            P >>>= 1;
            PH = (int) (0xFFFF & (P >>> 16));
            PL = (int) (0xFFFF & P);
            
            ++I;
            if (((I >> 4) & 1) == 1) {
                break;
            }
        }
        
        long P = (PH << 16) | PL;
        P <<= 6;
        PH = (int) (0xFFFF & (P >>> 16));
        
        if (b15) {
            PH = negate(PH);
        }
        
        return PH;
    }
    
    public int multiplyAndPrint(final int a, final int b) {
        
        int A = a;
        printValue("  A", A);
        int PH = b;
        printValue(" PH", PH);
        final boolean b15 = ((b >> 15) & 1) == 1;
        
        if (b15) {
            PH = negate(PH);
            printValue(" PH", PH);
        }
        int PL = PH;
        printValue(" PL", PL);
        int I = 0;
        printValue("  I", I);
        PH = 0;
        printValue(" PH", PH);
        
        while (true) {
            if ((PL & 1) == 1) {
                PH = add(PH, A);
                printValue(" PH", PH);
            }
            
            long P = (PH << 16) | PL;
            printValue(" RS", (int) P, 32);
            P >>= 1;            
            PH = (int) (0xFFFF & (P >>> 16));
            printValue(" PH", PH);
            PL = (int) (0xFFFF & P);
            printValue(" PL", PL);
            
            ++I;
            printValue("  I", I);
            if (((I >> 4) & 1) == 1) {
                break;
            }
        }
        
        long P = (PH << 16) | PL;
        P <<= 6;
        PH = (int) (0xFFFF & (P >>> 16));
        printValue(" PH", PH);
        
        if (b15) {
            PH = negate(PH);
            printValue(" PH", PH);
        }
        
        return PH;
    }    
    
    public int multiplyOld(final int a, final int b) {
        
        long p = 0; // p represents 32-bit register (but we only need the top 22-bits)
        int i = 0;
        
        if (((b >> 15) & 1) == 1) { // test bit 15
            p = negate(b);
        } else {
            p = b;
        }
              
        while (true) {
            
            if ((p & 1) == 1) {                
                p = (add((int) (p >>> 16), a) << 16) | (p & 0xFFFF); // add upper 16 bits of p with a
            }
            
            p >>= 1; // signed right shift by 1
            
            ++i;
            
            if (((i >> 4) & 1) == 1) { // test bit 4
                break;
            }            
        }  
        
        p = 0xFFFFFFFF & (p << (16 - DECIMAL_PLACES));
        final int r = (int) (0xFFFF & (p >> 16));        
        
        if (((b >> 15) & 1) == 1) { // test bit 15
            return negate(r);
        } 
        
        return r;
    }    
    
    public static void main(final String... args) throws Exception {
        new TestMultiplier().launch();
    }
}
