import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

public class AcceptedMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public ArrayList<Integer> n;
    public MajorBlock block;
    public String serverName;

    public AcceptedMessage(ArrayList<Integer> n, MajorBlock block, String serverName) {
        this.n = n;
        this.block = block;
        this.serverName = serverName;
    }
}
