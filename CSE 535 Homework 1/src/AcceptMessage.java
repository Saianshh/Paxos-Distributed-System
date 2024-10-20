import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

public class AcceptMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public ArrayList<Integer> n;
    public MajorBlock block;
    public Paxos paxos;

    public AcceptMessage(ArrayList<Integer> n, MajorBlock block, Paxos paxos) {
        this.n = n;
        this.block = block;
        this.paxos = paxos;
    }

}
