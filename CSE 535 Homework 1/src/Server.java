import java.security.cert.TrustAnchor;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.LinkedList;
import java.net.*;
import java.io.*;
import java.util.Queue;

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
    private boolean paxosInitiated;
    private ArrayList<Integer> receivedAcceptNum;
    private MajorBlock receivedAcceptVal;
    private ArrayList<Integer> currentBallotNumber;
    private Queue<Transaction> queue;
    private Transaction paxosTransaction;
    private boolean receivedPromises;
    private Queue<Transaction> queueNonMajority;

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
        this.paxosInitiated = false;
        this.receivedAcceptNum = null;
        this.receivedAcceptVal = null;
        this.currentBallotNumber = null;
        this.queue = new LinkedList<>();
        this.paxosTransaction = null;
        this.receivedPromises = false;
        this.queueNonMajority = new LinkedList<>();
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
    public boolean getPaxosInitiated() {
        return this.paxosInitiated;
    }
    public void setPaxosInitiated(boolean paxosInitiated) {
        this.paxosInitiated = paxosInitiated;
    }
    public void setReceivedAcceptNum(ArrayList<Integer> receivedAcceptNum) {
        this.receivedAcceptNum = receivedAcceptNum;
    }
    public void setReceivedAcceptVal(MajorBlock receivedAcceptVal) {
        this.receivedAcceptVal = receivedAcceptVal;
    }
    public Queue<Transaction> getQueue() {
        return this.queue;
    }
    public void setQueue(Queue<Transaction> queue) {
        this.queue = queue;
    }
    public void addToQueue(Transaction t) {
        this.queue.add(t);
    }

    public Queue<Transaction> getQueueNonMajority() {
        return this.queueNonMajority;
    }
    public void setQueueNonMajority(Queue<Transaction> queueNonMajority) {
        this.queueNonMajority = queueNonMajority;
    }
    public void addToQueueNonMajority(Transaction t) {
        this.queueNonMajority.add(t);
    }

    public Transaction getPaxosTransaction() {
        return this.paxosTransaction;
    }
    public int getNumPromiseMessages() {
        return this.numPromiseMessages;
    }
    public void setNumPromiseMessages(int numPromiseMessages) {
        this.numPromiseMessages = numPromiseMessages;
    }
    public boolean getEnteredAccept() {
        return this.enteredAccept;
    }
    public void setEnteredAccept(boolean enteredAccept) {
        this.enteredAccept = enteredAccept;
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
                    if (this.lastBallotNumber != null) {
                        if(message.lastCommittedBallot.get(0) >= this.lastBallotNumber.get(0)) {
                            PromiseMessage promiseMessage;
                            if (this.acceptNum == null && this.acceptVal == null) {
                                promiseMessage = new PromiseMessage(message.n, this.acceptNum, this.acceptVal, this.localLog);
                            } else {
                                promiseMessage = new PromiseMessage(message.n, this.acceptNum, this.acceptVal, null);
                            }
                            this.currentBallotNumber = message.n;
                            message.paxos.promisePhase(promiseMessage, this.getServerName());
                        } else {
                            System.out.println(this.lastBallotNumber);
                            System.out.println(message.lastCommittedBallot);
                            System.out.println("server lastballot greater than leader");
                        }
                    } else {
                        PromiseMessage promiseMessage;
                        if (this.acceptNum == null && this.acceptVal == null) {
                            promiseMessage = new PromiseMessage(message.n, this.acceptNum, this.acceptVal, this.localLog);
                        } else {
                            promiseMessage = new PromiseMessage(message.n, this.acceptNum, this.acceptVal, null);
                        }
                        this.currentBallotNumber = message.n;
                        message.paxos.promisePhase(promiseMessage, this.getServerName());
                    }
                } else if (object instanceof PromiseMessage) {
                    System.out.println("Leader receiving promise message " + this.serverName + " " + this);
                    this.numPromiseMessages += 1;
                    PromiseMessage message = (PromiseMessage) object;
                    if (message.acceptNum == null && message.acceptVal == null) {
                        // T'(S)
                        for (int i = 0; i < message.localLog.size(); i++) {
                            this.addToLoggedPromises(message.localLog.get(i));
                        }
                    } else {
                        this.setReceivedAcceptNum(message.acceptNum);
                        this.setReceivedAcceptVal(message.acceptVal);
                    }
                    if (this.numPromiseMessages >= 3 && !this.enteredAccept) {
                        this.receivedPromises = true;
//                        System.out.println(this.numPromiseMessages);
                        this.enteredAccept = true;
                        new Thread(() -> {
                            try {
                                Thread.sleep(50);
                                if(this.receivedAcceptNum != null && this.receivedAcceptVal != null) {
                                    this.paxos.acceptPhase(this.receivedAcceptVal, this.receivedAcceptNum);
                                } else {
                                    MajorBlock block = new MajorBlock(loggedPromises, message.n);
                                    this.paxos.acceptPhase(block, message.n);
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();
                    }
//                    Thread.sleep(50);
//                    if(!this.enteredAccept) {
//                        System.out.println("Unable to get majority. Adding " + this.paxosTransaction + " to queue");
//                        this.paxos.addFailedToQueue();
//                    }
                } else if (object instanceof AcceptMessage) {
                    System.out.println("RECEIVED ACCEPT MESSAGE FROM LEADER " + this.serverName + " " + this);
                    // Update acceptNum and acceptVal
                    AcceptMessage message = (AcceptMessage) object;
                    System.out.println("PRINTING N TO SEE WHICH IS BIGGER " + message.n + message + " , " + this.currentBallotNumber);
                    if(message.n.get(0) >= this.currentBallotNumber.get(0)) {
                        System.out.println("NOT GETTING RID OF OLD PAXOS");
                        this.acceptNum = message.n;
                        this.acceptVal = message.block;
                        // Send accepted message
                        AcceptedMessage acceptedMessage = new AcceptedMessage(message.n, message.block, this.serverName);
                        message.paxos.acceptedPhase(acceptedMessage);

                    } else {
                        this.queue.add(this.paxosTransaction);
                        System.out.println("GETTING RID OF OLD PAXOS ADDING TO QUEUE: " + this.queue);
                    }
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

                    System.out.println("PRINTING LOCAL TRANSACTIONS");
                    System.out.println(message.block.getLocalTransactions());
                    System.out.println("PRINTING LOCAL LOG");
                    System.out.println(this.localLog);
                    for(int i = 0; i < message.block.getLocalTransactions().size(); i++) {
                        for(int j = 0; j < this.localLog.size(); j++) {
                            if((message.block.getLocalTransactions().get(i).getTimestamp().equals(this.localLog.get(j).getTimestamp())) && (message.block.getLocalTransactions().get(i).getS1().equals(this.localLog.get(j).getS1())) && (message.block.getLocalTransactions().get(i).getS2().equals(this.localLog.get(j).getS2())) && (message.block.getLocalTransactions().get(i).getAmt() == this.localLog.get(j).getAmt())) {
                                // remove from local log
                                System.out.println("PRIOR TO REMOVING FROM LOCAL LOG");
                                System.out.println(this.localLog);
                                this.localLog.remove(this.localLog.get(j));
                                System.out.println("AFTER REMOVING FROM LOCAL LOG");
                                System.out.println(this.localLog);
                                break;
                            }
                        }
                    }
                    this.updateBalanceWithMajorBlock(message.block);
//                    if(this.serverName.equals(this.paxos.getLeader().getServerName())) {
//                        System.out.println("IN LEADER IF STATEMENT BALANCE IS: " + this.getBalance());
//                        this.paxos.postPaxos(this);
//                    }
                    if(this.queue.size() != 0) {
                        this.paxos.postPaxosQueue(this);
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
            this.paxosInitiated = false;
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
            System.out.println("INITIATING PAXOS ON THIS SERVER: " + this.serverName);
            System.out.println("PAXOS TRANSACTION IS: " + t);
            this.paxosInitiated = true;
            this.paxosTransaction = t;
            this.queue.add(t);
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
