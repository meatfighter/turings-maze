package turingsmaze;

public final class Response {

    public final Switch[] reds;
    public final Switch[] greens;    
    public final Switch destination;
    public final int direction;

    public Response(final Switch[] reds, final Switch[] greens, final Switch destination, final int direction) {
        this.reds = reds;
        this.greens = greens;
        this.destination = destination;
        this.direction = direction;
    }
}