import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class Paxos implements Serializable {
//    @Serial
//    private static final long serialVersionUID = 1L;
    private ArrayList<Server> servers;
//    private ArrayList<String> clients;
    private Server leader;
    private Transaction initialTransaction;

    public Paxos(ArrayList<Server> servers) {
        this.servers = servers;
//        this.servers.add(s1);
//        this.servers.add(s2);
//        this.servers.add(s3);
//        this.servers.add(s4);
//        this.servers.add(s5);
    }
    public ArrayList<Server> getServers() {
        return this.servers;
    }
    public void setServers(ArrayList<Server> servers) {
        this.servers = servers;
    }
    public Server getLeader() {
        return this.leader;
    }
    public void setLeader(Server leader) {
        this.leader = leader;
    }
    public void setInitialTransaction(Transaction t) {
        this.initialTransaction = t;
    }
    public Transaction getInitialTransaction() {
        return this.initialTransaction;
    }
    public void preparePhase(Transaction t) {
        System.out.println("In the prepare phase of paxos");
        this.setInitialTransaction(t);
        for (Server server : this.servers) {
//            if (!this.leader.getServerName().equals(server.getServerName())) {
                // Prepare message will look like: Paxos,PREPARE,1 1,lastCommittedBlock
//                leader.sendMessage(server.getPort(), "Paxos,PREPARE," + Server.ballotNum + " " + leader.getServerName().charAt(1) + " from " + leader.getServerName());
//                leader.sendMessage(server.getPort(), "Paxos");
            ArrayList<Integer> ballotNum = new ArrayList<Integer>();
            ballotNum.add(Server.ballotNum);
            ballotNum.add(Character.getNumericValue(this.leader.getServerName().charAt(1)));
            PrepareMessage message = new PrepareMessage(ballotNum, server.getLastBallotNumber(), this);
            this.leader.sendPrepareMessage(server.getPort(), message);
//            }
        }

    }
    public void promisePhase(PromiseMessage promiseMessage, String serverName) {
        // send to leader
        // leader needs 3 nodes to keep going with paxos
        // if no agreement, go to next set, keep local logs
//        System.out.println("IN PROMISE PHASE");
        for (Server server : this.servers) {
            if (server.getServerName().equals(serverName)) {
                server.sendPromiseMessage(this.leader.getPort(), promiseMessage);
            }
        }

    }
    public void acceptPhase(MajorBlock block, ArrayList<Integer> ballotNum) {
        System.out.println("IN ACCEPT PHASE");
        System.out.println(block.getLocalTransactions());
        for (Server server : this.servers) {
//            if (!this.leader.getServerName().equals(server.getServerName())) {
            AcceptMessage acceptMessage = new AcceptMessage(ballotNum, block, this);
            this.leader.sendAcceptMessage(server.getPort(), acceptMessage);
//            }
        }
    }
    public void acceptedPhase(AcceptedMessage acceptedMessage) {
        // Send accepted message to leader, which then commits
        for (Server server : this.servers) {
            if (server.getServerName().equals(acceptedMessage.serverName)) {
                server.sendAcceptedMessage(this.leader.getPort(), acceptedMessage);
            }
        }
    }
    public void commitPhase(MajorBlock block, ArrayList<Integer> ballotNum, String serverName) {
        System.out.println("IN COMMIT IN PAXOS CLASS SIZE OF SERVERS: " +this.servers.size());
        for (Server server : this.servers) {
            if (serverName.equals(server.getServerName())) {
                CommitMessage commitMessage = new CommitMessage(ballotNum, block, this);
                this.leader.sendCommitMessage(server.getPort(), commitMessage);
            }
        }

    }
//    public void postPaxos(Server server) {
//        server.performTransaction(this.initialTransaction);
//    }
    public void postPaxosQueue(Server server) {
        System.out.println("QUEUE IN SERVER " + server.getServerName());
        System.out.println(server.getQueue());
        if(server.getQueue().peek() == null) {
            server.getQueue().clear();
        }
//        for (Transaction transaction : server.getQueue()) {
//            System.out.println(transaction);
////            System.out.println(server.getQueue());
        server.performTransaction(server.getQueue().peek());
        server.getQueue().remove();


    }
    public void addFailedToQueue() {
        this.leader.addToQueue(this.leader.getPaxosTransaction());
    }

    public void synchronizeServer(Server server) {
        for(MajorBlock block : this.leader.getDatastore()) {
            boolean serverHas = false;
            for(MajorBlock blockServer : server.getDatastore()) {
                if((block.getBallotNum().get(0).equals(blockServer.getBallotNum().get(0))) && (block.getBallotNum().get(1).equals(blockServer.getBallotNum().get(1)))) {
                    serverHas = true;
                }
            }
            if(!serverHas) {
                CommitMessage commitMessage = new CommitMessage(block.getBallotNum(), block, this);
                this.leader.sendCommitMessage(server.getPort(), commitMessage);
            }
        }
    }

}
