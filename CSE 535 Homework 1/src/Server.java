import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;

public class Server implements Runnable {
    private String serverName;
    private int balance;
    private boolean leader;
    private ArrayList<Transaction> localLog;
    private LinkedList<MajorBlock> datastore;
    private String acceptNum;
    private ArrayList<String> acceptVal;
    private Client client;
    private int majorBlockNumbers;

    public Server(String serverName) {
        this.serverName = serverName;
        this.balance = 100;
        this.leader = false;
        this.localLog = new ArrayList<>();
        this.datastore = new LinkedList<>();
        this.acceptNum = null;
        this.acceptVal = null;
        this.client = null;
        this.majorBlockNumbers = 0;
    }

    public String getServerName() {
        return this.serverName;
    }
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    public int getBalance() {
        return this.balance;
    }
    public void setBalance(int balance) {
        this.balance = balance;
    }
    public boolean getLeader() {
        return this.leader;
    }
    public void setLeader(boolean leader) {
        this.leader = leader;
    }
    public ArrayList<Transaction> getLocalLog() {
        return this.localLog;
    }
    public void setLocalLog(ArrayList<Transaction> localLog) {
        this.localLog = localLog;
    }
    public void addToLocalLog(Transaction t) {
        this.localLog.add(t);
    }
    public String getAcceptNum() {
        return this.acceptNum;
    }
    public void setAcceptNum(String acceptNum) {
        this.acceptNum = acceptNum;
    }
    public ArrayList<String> getAcceptVal() {
        return this.acceptVal;
    }
    public void setAcceptVal(ArrayList<String> acceptVal) {
        this.acceptVal = acceptVal;
    }
    public Client getClient() {
        return this.client;
    }
    public void setClient(Client client) {
        this.client = client;
    }
    public int getMajorBlockNumbers() {
        return this.majorBlockNumbers;
    }
    public void setMajorBlockNumbers(int majorBlockNumbers) {
        this.majorBlockNumbers = majorBlockNumbers;
    }
    public void addToDatastore(MajorBlock majorBlock) {
        this.datastore.add(majorBlock);
    }
    public void run() {
        try {
            System.out.println("Thread " + Thread.currentThread().getId() + " is running");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void performTransaction(Transaction t) {
        if(this.getBalance() >= t.getAmt()) {
            this.setBalance(this.getBalance() - t.getAmt());
            this.addToLocalLog(t);
            client.setBalance(this.getBalance());
            System.out.println("Client and server: " + this.getServerName() + " balance is now:" + this.getBalance());
        } else {
            System.out.println("Paxos needs to be initiated");
        }
    }


}
