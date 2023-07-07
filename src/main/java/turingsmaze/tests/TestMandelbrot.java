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

public class TestMandelbrot {
    
    private static final String WORKSPACE_DIR = "workspace/";
    private static final String TESTS_DIR = WORKSPACE_DIR + "tests/";
    
    private static final String SOURCE_FILE = TESTS_DIR + "test-mandelbrot-16.png";
    private static final String DESTINATION_FILE = TESTS_DIR + "out.png";
    
    public static final int DECIMAL_PLACES = 10;
    
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
        
        final Gate[] xGates = extract(gates, 5, 1443);
        final Gate[] yGates = extract(gates, 5, 1335); 
        
        final Reg i = extractRegister("  I", gates, 246, 1068, 7);
        final Reg x0 = extractRegister(" X0", gates, 246, 934);
        final Reg y0 = extractRegister(" Y0", gates, 246, 800);
        final Reg x = extractRegister("  X", gates, 246, 666);
        final Reg y = extractRegister("  Y", gates, 246, 532);
        final Reg x2 = extractRegister(" X2", gates, 246, 398);
        final Reg y2 = extractRegister(" Y2", gates, 246, 264);
        final Reg s = extractRegister("  S", gates, 246, 130);
        final Reg ls = extractRegister(" LS", gates, 246, 4, 15);        
        final Reg[] regs = { i, x0, y0, x, y, x2, y2, s, ls };
        
//        final int X = 42554;
//        final int Y = 32119;
        
//        System.out.format("Expected value: %d%n", evalAndPrint(X, Y));
        
//        storeValue(new Gate[][] { xGates, null }, X);
//        storeValue(new Gate[][] { yGates, null }, Y);
//        emulate(gates, responses, regs, maze);        
          
        final Random random = ThreadLocalRandom.current();
        
        while (true) {
            final int X = random.nextInt(0x10000);
            final int Y = random.nextInt(0x10000);            
            final int expectedValue = eval(X, Y);

            storeValue(new Gate[][] { xGates, null }, X);
            storeValue(new Gate[][] { yGates, null }, Y);
            emulate(gates, responses, new Reg[0], maze);
            final int actualValue = loadValue(i.gates);

            if (expectedValue != actualValue) {
                System.out.format("eval(%d, %d) = %d, %d%n", X, Y, expectedValue, actualValue);
                //return;
            } else {
                System.out.println("match");
                System.out.format("eval(%d, %d) = %d, %d%n", X, Y, expectedValue, actualValue);
            }
        }

//        System.out.println(multiply(1, 32768));
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
    
    public int evalAndPrint(final int x0, final int y0) {
        
        int x = x0;
        printValue("  X", x);
        int y = y0;
        printValue("  Y", y);        
        int i = 0;
        printValue("  I", i);
        
        while (true) {
            
            final int y2 = multiply(y, y);
            printValue(" Y2", y2);
            final int x2 = multiply(x, x);
            printValue(" X2", x2);
            
            int s = add(0b1111_0000_0000_0000, x2);
            printValue("  S", s);
            s = add(s, y2);
            printValue("  S", s);
            if ((s >> 15) == 0) { // test bit 15
//                System.out.println(Integer.toBinaryString(s));
                break;
            }            
            
            ++i;
            printValue("  I", i);
            
            if ((i >> 6) != 0) { // test bit 6
                break;
            }            
            
            y = multiply(x, y);
            printValue("  Y", y);
            y = leftShift(y);
            printValue("  Y", y);
            y = add(y, y0);
            printValue("  Y", y);

            x = subtract(x2, y2);
            printValue("  X", x);
            x = add(x, x0);
            printValue("  X", x);
        }
        
        return i;
    }    

    public int eval(final int x0, final int y0) {
        
        int x = x0;
        int y = y0;        
        int i = 0;
        
        while (true) {
            
            final int y2 = multiply(y, y);
            final int x2 = multiply(x, x);            
            
            int s = add(0b1111_0000_0000_0000, x2);
            s = add(s, y2);
            if ((s >> 15) == 0) { // test bit 15
//                System.out.println(Integer.toBinaryString(s));
                break;
            }            
            
            ++i;
            
            if ((i >> 6) != 0) { // test bit 6
                break;
            }            
            
            y = multiply(x, y);
            y = leftShift(y);
            y = add(y, y0);

            x = subtract(x2, y2);
            x = add(x, x0);
        }
        
        return i;
    }     
    
    public static void main(final String... args) throws Exception {
        new TestMandelbrot().launch();
    }
}
