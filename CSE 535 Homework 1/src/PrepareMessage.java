import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
public class PrepareMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public Paxos paxos;
    public ArrayList<Integer> n;
    public ArrayList<Integer> lastCommittedBallot;

    public PrepareMessage(ArrayList<Integer> n, ArrayList<Integer> lastCommittedBallot, Paxos paxos) {
        this.n = n;
        this.lastCommittedBallot = lastCommittedBallot;
        this.paxos = paxos;
    }
}
