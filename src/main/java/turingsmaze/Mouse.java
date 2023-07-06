package turingsmaze;

public class Mouse {

    private int x;
    private int y;
    private int direction;
    
    public Mouse() {        
    }
    
    public Mouse(final int x, final int y, final int direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
    }
    
    public Mouse(final Mouse mouse) {
        x = mouse.getX();
        y = mouse.getY();
        direction = mouse.getDirection();
    }
    
    public Mouse(final Maze maze) {

        x = -1;
        y = maze.getHeight() - 1;  
        direction = Direction.NORTH;
                
        int X = -1;
        for (int i = maze.getWidth() - 1; i >= 0; --i) {
            if (maze.getTile(i, y) == Tile.BLACK) {
                if (x < 0) {
                    x = i;
                } else {
                    throw new IllegalArgumentException("Maze has multiple entrances.");
                }
            }
            if (maze.getTile(i, 0) == Tile.BLACK) {
                if (X < 0) {
                    X = i;
                } else {
                    throw new IllegalArgumentException("Maze has multiple exits.");
                }
            }
        }

        if (x < 0) {
            throw new IllegalArgumentException("Maze does not contain an entrance.");
        }
        if (X < 0) {
            throw new IllegalArgumentException("Maze does not contain an exit.");
        }
    }

    public int getX() {
        return x;
    }

    public void setX(final int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(final int y) {
        this.y = y;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(final int direction) {
        this.direction = direction;
    }
    
    public void reverseDirection() {
        direction = (direction + 2) & 3;
    }
    
    public void turnClockwise() {
        direction = (direction + 1) & 3;
    }    
    
    public void turnCounterclockwise() {
        direction = (direction - 1) & 3;
    }
    
    public void updateGate(final Maze maze) {
        switch (maze.getTile(x, y)) {
            case Tile.RED:
                if (direction == Direction.NORTH) {
                    maze.setTile(x, y, Tile.GREEN);
                }
                reverseDirection();
                break;
            case Tile.GREEN:
                switch (direction) {
                    case Direction.NORTH:
                        direction = Direction.SOUTH;
                        break;
                    case Direction.SOUTH:
                        direction = Direction.NORTH;
                        maze.setTile(x, y, Tile.RED);
                        break;
                } 
                break;
        }
    }
    
    public void updateDirection(final Maze maze) {
        switch (getGrayTiles(maze)) {
            case 0b000:                    
                break;
            case 0b001:                    
                break;
            case 0b010:
                turnClockwise();
                break;
            case 0b011:
                turnCounterclockwise();
                break;
            case 0b100:
                turnClockwise();
                break;
            case 0b101:                    
                break;
            case 0b110:
                turnClockwise();
                break;
            case 0b111:
                reverseDirection();
                break;
        }
    }
    
    private int getGrayTiles(final Maze maze) {
        switch (direction) {
            case Direction.NORTH:
                return (isGray(maze, x - 1, y) << 2) | (isGray(maze, x, y - 1) << 1) | isGray(maze, x + 1, y);
            case Direction.EAST:
                return (isGray(maze, x, y - 1) << 2) | (isGray(maze, x + 1, y) << 1) | isGray(maze, x, y + 1);
            case Direction.SOUTH:
                return (isGray(maze, x + 1, y) << 2) | (isGray(maze, x, y + 1) << 1) | isGray(maze, x - 1, y);
            default:
                return (isGray(maze, x, y + 1) << 2) | (isGray(maze, x - 1, y) << 1) | isGray(maze, x, y - 1);
        }
    }
    
    private int isGray(final Maze maze, final int x, final int y) {
        return (maze.getTile(x, y) == Tile.GRAY) ? 1 : 0;
    }
    
    public void step() {
        switch (direction) {
            case Direction.NORTH:
                --y;
                break;
            case Direction.EAST:
                ++x;
                break;
            case Direction.SOUTH:
                ++y;
                break;
            case Direction.WEST:
                --x;
                break;
        }
    }

    public boolean isInMaze() {
        return y > 0;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + this.x;
        hash = 19 * hash + this.y;
        hash = 19 * hash + this.direction;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Mouse other = (Mouse) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        return this.direction == other.direction;
    }
}