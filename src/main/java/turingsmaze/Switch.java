package turingsmaze;

public final class Switch {
    
    public final int index;
    public final Coordinates coordinates;
    
    public boolean red;
    
    public Switch(final int index, final Coordinates coordinates, final boolean red) {
        this.index = index;
        this.coordinates = coordinates;
        this.red = red;
    }
}
