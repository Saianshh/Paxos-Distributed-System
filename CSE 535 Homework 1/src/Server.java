import java.util.ArrayList;
import java.util.LinkedList;
import java.net.*;
import java.io.*;

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
    public static int ballotNum = 0;
    private ArrayList<Integer> lastBallotNumber;
    private int port;
    private Paxos paxos;

    public Server(String serverName, int port) {
        this.serverName = serverName;
        this.balance = 100;
        this.leader = false;
        this.localLog = new ArrayList<>();
        this.datastore = new LinkedList<>();
        this.acceptNum = null;
        this.acceptVal = null;
        this.client = null;
        this.majorBlockNumbers = 0;
        this.lastBallotNumber = null;
        this.port = port;
//        this.paxos = new Paxos();
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
    public ArrayList<Integer> getLastBallotNumber() {
        return this.lastBallotNumber;
    }
    public void setLastBallotNumber(ArrayList<Integer> lastBallotNumber) {
        this.lastBallotNumber = lastBallotNumber;
    }
    public int getPort() {
        return this.port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public Paxos getPaxos() {
        return this.paxos;
    }
    public void setPaxos(Paxos paxos) {
        this.paxos = paxos;
    }
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Thread " + this.getServerName() + " is running");
            System.out.println("Server " + this.getServerName() + " listening on port " + port);
            while (true) {
                // Wait for a client to send a transaction
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                // Read transaction details from the client
                String transactionDetails = in.readLine();
                // Have in the first index have the name of the person sending message, if client it's a transaction,
                // if other server then paxos
                String[] details = transactionDetails.split(",");
                String sender = details[0];
                String receiver = details[1];
                int amount = Integer.parseInt(details[2]);

                // Process the transaction
                Transaction t = new Transaction(sender, receiver, amount);
                this.performTransaction(t);

                // Send confirmation back to the client
                if (balance >= amount) {
                    out.println("Transaction successful.");
                } else {
                    out.println("Paxos initiated");
                }

                clientSocket.close();
            }
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
            ballotNum += 1;
            System.out.println("Paxos needs to be initiated");
            // Send to paxos problem (class), say solve this problem for me
            // message passing between threads

        }
    }


}
