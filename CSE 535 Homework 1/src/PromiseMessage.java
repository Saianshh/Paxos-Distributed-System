import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

public class PromiseMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public ArrayList<Integer> n;
    public ArrayList<Integer> acceptNum;
    public MajorBlock acceptVal;
    public ArrayList<Transaction> localLog;
    public Server server;

    public PromiseMessage(ArrayList<Integer> n, ArrayList<Integer> acceptNum, MajorBlock acceptVal, ArrayList<Transaction> localLog, Server server) {
        this.n = n;
        this.acceptNum = acceptNum;
        this.acceptVal = acceptVal;
        this.localLog = localLog;
        this.server = server;
    }
}
