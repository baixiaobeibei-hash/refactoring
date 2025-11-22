package theater;
/**
 * Represents a theatrical play with a name and type (e.g., tragedy, comedy).
 */
public class Play {

    public final String name;
    public final String type;

    public Play(String name, String type) {
        this.name = name;
        this.type = type;
    }


    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
