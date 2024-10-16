import java.util.ArrayList;

public class MajorBlock {
    private ArrayList<Transaction> localTransactions;

    public MajorBlock(ArrayList<Transaction> localTransactions) {
        this.localTransactions = localTransactions;
    }
}
