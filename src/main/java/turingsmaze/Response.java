package turingsmaze;

public final class Response {

    public final Gate[] reds;
    public final Gate[] greens;    
    public final Gate destination;
    public final int direction;

    public Response(final Gate[] reds, final Gate[] greens, final Gate destination, final int direction) {
        this.reds = reds;
        this.greens = greens;
        this.destination = destination;
        this.direction = direction;
    }
}