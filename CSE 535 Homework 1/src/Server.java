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
    private MajorBlock acceptVal;
    private Client client;
    private int majorBlockNumbers;
    public static int ballotNum = 0;
    private ArrayList<Integer> lastBallotNumber;
    private int port;
    private Paxos paxos;
    private int numPromiseMessages;
    private ArrayList<Transaction> loggedPromises;
    private boolean enteredAccept = false;

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
        this.loggedPromises = new ArrayList<>();
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
    public MajorBlock getAcceptVal() {
        return this.acceptVal;
    }
    public void setAcceptVal(MajorBlock acceptVal) {
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
    public void addToLoggedPromises(Transaction loggedPromise) {
        loggedPromises.add(loggedPromise);
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
                } else if (object instanceof PrepareMessage) {
                    System.out.println("Paxos was initiated");
                    PrepareMessage message = (PrepareMessage) object;
                    System.out.println("RECEIVED PREPARE OBJECT FROM OTHER SERVER" + message.n);
                    PromiseMessage promiseMessage = new PromiseMessage(message.n, this.acceptNum, this.acceptVal, this.localLog);
                    message.paxos.promisePhase(promiseMessage, this.getServerName());
                } else if (object instanceof PromiseMessage) {
                    System.out.println("Leader receiving promise message " + this.serverName + " " + this);
                    this.numPromiseMessages += 1;
                    PromiseMessage message = (PromiseMessage) object;
//                    System.out.println("LOCAL LOGS:" + message.localLog);
//                    if (this.numPromiseMessages == 2) {
//                        // send own for majority
//                        for (int i = 0; i < this.localLog.size(); i++) {
//                            this.addToLoggedPromises(this.localLog.get(i));
//                        }
//                        this.numPromiseMessages += 1;
//                    }
                    System.out.println("LOCAL LOGS2: " + message.localLog);
                    if (message.acceptNum == null && message.acceptVal == null) {
                        // T'(S)
                        System.out.println("LOCAL LOGS3: " + message.localLog);
                        for (int i = 0; i < message.localLog.size(); i++) {
                            this.addToLoggedPromises(message.localLog.get(i));
                        }
                    }
                    if (this.numPromiseMessages >= 3 && !this.enteredAccept) {
//                        System.out.println(this.numPromiseMessages);
                        this.enteredAccept = true;
                        new Thread(() -> {
                            try {
                                Thread.sleep(50);
                                MajorBlock block = new MajorBlock(loggedPromises, message.n);
                                this.paxos.acceptPhase(block, message.n);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();
                    }
                } else if (object instanceof AcceptMessage) {
                    System.out.println("RECEIVED ACCEPT MESSAGE FROM LEADER " + this.serverName + " " + this);
                    // Update acceptNum and acceptVal
                    AcceptMessage message = (AcceptMessage) object;
                    this.acceptNum = message.n;
                    this.acceptVal = message.block;
                    // Send accepted message
                    AcceptedMessage acceptedMessage = new AcceptedMessage(message.n, message.block, this.serverName);
                    message.paxos.acceptedPhase(acceptedMessage);
                } else if (object instanceof AcceptedMessage) {
                    System.out.println("LEADER RECEIVED ACCEPTED MESSAGE");
                    AcceptedMessage message = (AcceptedMessage) object;
                    this.paxos.commitPhase(message.block, message.n, message.serverName);
                } else if (object instanceof CommitMessage) {
                    System.out.println("IN COMMIT PHASE " + this.serverName + " " + this);
                    CommitMessage message = (CommitMessage) object;
                    this.addToDatastore(message.block);
                    this.lastBallotNumber = message.n;
                    this.acceptNum = null;
                    this.acceptVal = null;
                    for(int i = 0; i < message.block.getLocalTransactions().size(); i++) {
                        for(int j = 0; j < this.localLog.size(); j++) {
                            if((message.block.getLocalTransactions().get(i).getTimestamp() == this.localLog.get(j).getTimestamp()) && (message.block.getLocalTransactions().get(i).getS1().equals(this.localLog.get(j).getS1())) && (message.block.getLocalTransactions().get(i).getS2().equals(this.localLog.get(j).getS2())) && (message.block.getLocalTransactions().get(i).getAmt() == this.localLog.get(j).getAmt())) {
                                // remove from local log
                                this.localLog.remove(this.localLog.get(j));
                                break;
                            }
                        }
                    }
                    this.updateBalanceWithMajorBlock(message.block);
                    if(this.serverName.equals(this.paxos.getLeader().getServerName())) {
                        System.out.println("IN LEADER IF STATEMENT BALANCE IS: " + this.getBalance());
                        this.paxos.postPaxos(this);
                    }
                    System.out.println(this.getServerName() + " " + this.getBalance());

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
            this.numPromiseMessages = 0;
            this.loggedPromises = new ArrayList<>();
            this.enteredAccept = false;
            System.out.println("INITIATING PAXOS ON THIS SERVER: " + this);
            this.paxos.preparePhase(t);
            // Send to paxos problem (class), say solve this problem for me
            // message passing between threads
            // try now, if not enough money then failed to process transaction, else successful

        }
    }
    public void updateBalanceWithMajorBlock(MajorBlock block) {
        for(int i = 0; i < block.getLocalTransactions().size(); i++) {
            if(block.getLocalTransactions().get(i).getS2().equals(this.serverName)) {
                this.balance += block.getLocalTransactions().get(i).getAmt();
            }
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
    public void sendAcceptMessage(int port, AcceptMessage message) {
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
    public void sendAcceptedMessage(int port, AcceptedMessage message) {
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
    public void sendCommitMessage(int port, CommitMessage message) {
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
