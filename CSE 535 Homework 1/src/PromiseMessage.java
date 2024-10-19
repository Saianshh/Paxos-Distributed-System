import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

public class PromiseMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public ArrayList<Integer> n;
    public ArrayList<Integer> acceptNum;
    public ArrayList<String> acceptVal;
    public ArrayList<Transaction> localLog;

    public PromiseMessage(ArrayList<Integer> n, ArrayList<Integer> acceptNum, ArrayList<String> acceptVal, ArrayList<Transaction> localLog) {
        this.n = n;
        this.acceptNum = acceptNum;
        this.acceptVal = acceptVal;
        this.localLog = localLog;
    }
}
