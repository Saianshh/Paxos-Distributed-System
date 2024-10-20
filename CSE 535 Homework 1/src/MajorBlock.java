import java.io.Serializable;
import java.util.ArrayList;

public class MajorBlock implements Serializable {
    private ArrayList<Transaction> localTransactions;
    private ArrayList<Integer> ballotNum;

    public MajorBlock(ArrayList<Transaction> localTransactions, ArrayList<Integer> ballotNum) {
        this.localTransactions = localTransactions;
        this.ballotNum = ballotNum;
    }
    public ArrayList<Transaction> getLocalTransactions() {
        return this.localTransactions;
    }
    public void setLocalTransactions(ArrayList<Transaction> localTransactions) {
        this.localTransactions = localTransactions;
    }
    public ArrayList<Integer> getBallotNum() {
        return this.ballotNum;
    }
    public void setBallotNum(ArrayList<Integer> ballotNum) {
        this.ballotNum = ballotNum;
    }
    @Override
    public String toString() {
        String s = "[BLOCK " + this.ballotNum + ": ";
        for(int i = 0; i < this.localTransactions.size(); i++) {
            s += this.localTransactions.get(i);
            s += ", ";
        }
        if(s.charAt(s.length()-2) == ',') {
            s = s.substring(0, s.length()-2);
        }
        s += "]";
        return s;
    }
}
