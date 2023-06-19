package turingsmaze;

public final class Gate {
    
    public final int index;
    public final Coordinates coordinates;
    
    public boolean red;
    
    public Gate(final int index, final Coordinates coordinates, final boolean red) {
        this.index = index;
        this.coordinates = coordinates;
        this.red = red;
    }
}
