import java.io.Serializable;

public class Transaction implements Serializable {
    private String s1;
    private String s2;
    private int amt;

    public Transaction(String s1, String s2, int amt) {
        this.s1 = s1;
        this.s2 = s2;
        this.amt = amt;
    }
    public String getS1() {
        return this.s1;
    }
    public String getS2() {
        return this.s2;
    }
    public int getAmt() {
        return this.amt;
    }

    @Override
    public String toString() {
        return "(" + this.s1 + ", " + this.s2 + ", " + this.amt + ")";
    }
}
