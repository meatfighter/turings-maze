package turingsmaze;

public class Maze {

    private final byte[][] tiles;
    private final int width;
    private final int height;
    
    public Maze(final int width, final int height) {
        this.width = width;
        this.height = height;
        tiles = new byte[height][width];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    public void setTile(final int x, final int y, final byte value) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return;
        }
        tiles[y][x] = value;
    }
    
    public byte getTile(final int x, final int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return Tile.GRAY;
        }
        return tiles[y][x];
    }    
}
