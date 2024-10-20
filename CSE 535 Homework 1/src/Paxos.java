import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
public class Paxos implements Serializable {
//    @Serial
//    private static final long serialVersionUID = 1L;
    private ArrayList<Server> servers;
//    private ArrayList<String> clients;
    private Server leader;

    public Paxos(Server s1, Server s2, Server s3, Server s4, Server s5) {
        this.servers = new ArrayList<Server>();
        this.servers.add(s1);
        this.servers.add(s2);
        this.servers.add(s3);
        this.servers.add(s4);
        this.servers.add(s5);
//        this.clients = new ArrayList<String>();
//        this.clients.add("C1");
//        this.clients.add("C2");
//        this.clients.add("C3");
//        this.clients.add("C4");
//        this.clients.add("C5");
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
    public void preparePhase() {
        System.out.println("In the prepare phase of paxos");
        for (Server server : this.servers) {
            if (!this.leader.getServerName().equals(server.getServerName())) {
                // Prepare message will look like: Paxos,PREPARE,1 1,lastCommittedBlock
//                leader.sendMessage(server.getPort(), "Paxos,PREPARE," + Server.ballotNum + " " + leader.getServerName().charAt(1) + " from " + leader.getServerName());
//                leader.sendMessage(server.getPort(), "Paxos");
                ArrayList<Integer> ballotNum = new ArrayList<Integer>();
                ballotNum.add(Server.ballotNum);
                ballotNum.add(Character.getNumericValue(this.leader.getServerName().charAt(1)));
                PrepareMessage message = new PrepareMessage(ballotNum, server.getLastBallotNumber(), this);
                this.leader.sendPrepareMessage(server.getPort(), message);
            }
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
            if (!this.leader.getServerName().equals(server.getServerName())) {
                AcceptMessage acceptMessage = new AcceptMessage(ballotNum, block, this);
                this.leader.sendAcceptMessage(server.getPort(), acceptMessage);
            }
        }
    }
    public void acceptedPhase(AcceptedMessage acceptedMessage, String serverName) {
        // Send accepted message to leader, which then commits
        for (Server server : this.servers) {
            if (server.getServerName().equals(serverName)) {
                server.sendAcceptedMessage(this.leader.getPort(), acceptedMessage);
            }
        }
    }

}
