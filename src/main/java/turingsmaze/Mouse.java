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
    
    public int getGrayTiles(final Maze maze) {
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
    
    public int isGray(final Maze maze, final int x, final int y) {
        return (maze.getTile(x, y) == Tile.GRAY) ? 1 : 0;
    }
}