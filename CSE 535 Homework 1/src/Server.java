import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.LinkedList;
import java.net.*;
import java.io.*;

public class Server implements Runnable, Serializable {
    private String serverName;
    private int balance;
    private boolean leader;
    private ArrayList<Transaction> localLog;
    private LinkedList<MajorBlock> datastore;
    private ArrayList<Integer> acceptNum;
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
    public ArrayList<Integer> getAcceptNum() {
        return this.acceptNum;
    }
    public void setAcceptNum(ArrayList<Integer> acceptNum) {
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
//                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                InputStream inputStream = clientSocket.getInputStream();
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                Object object = objectInputStream.readObject();
                if(object instanceof String) {
                    String[] details = ((String) object).split(",");
                    String sender = details[0];
                    String receiver = details[1];
                    int amount = Integer.parseInt(details[2]);

                    // Process the transaction
                    Transaction t = new Transaction(sender, receiver, amount);
                    this.performTransaction(t);
                }

                if(object instanceof PrepareMessage) {
                    System.out.println("Paxos was initiated");
                    PrepareMessage message = (PrepareMessage) object;
                    System.out.println("RECEIVED PREPARE OBJECT FROM OTHER SERVER" + message.n);
                    PromiseMessage promiseMessage = new PromiseMessage(message.n, this.acceptNum, this.acceptVal, this.localLog);
                    message.paxos.acceptPhase(promiseMessage);
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
            // don't reply to client
//            client.setBalance(this.getBalance());

            System.out.println("Client and server: " + this.getServerName() + " balance is now:" + this.getBalance());
        } else {
            ballotNum += 1;
            System.out.println("Paxos needs to be initiated");
            this.paxos.setLeader(this);
            this.paxos.preparePhase();
            // Send to paxos problem (class), say solve this problem for me
            // message passing between threads

        }
    }
    public void sendMessage(int port, String message) {
        try {
            Socket socket = new Socket("localhost", port);
//            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(message);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendPrepareMessage(int port, PrepareMessage message) {
        try {
            Socket socket = new Socket("localhost", port);
//            OutputStream outputStream = socket.getOutputStream();
//            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(message);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendPromiseMessage(int port, PromiseMessage message) {
        try {
            Socket socket = new Socket("localhost", port);
//            OutputStream outputStream = socket.getOutputStream();
//            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(message);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
