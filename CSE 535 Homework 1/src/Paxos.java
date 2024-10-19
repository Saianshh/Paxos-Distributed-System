import java.util.ArrayList;
public class Paxos {
    private ArrayList<Server> servers;
//    private ArrayList<String> clients;

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
    public void preparePhase(Server leader) {
        System.out.println("In the prepare phase of paxos");
        for (Server server : this.servers) {
            if (!leader.getServerName().equals(server.getServerName())) {
                Server.ballotNum += 1;
                // Prepare message will look like: Paxos,PREPARE,1 1,lastCommittedBlock
                leader.sendMessage(server.getPort(), "Paxos,PREPARE," + Server.ballotNum + leader.getServerName().charAt(1) + " from " + leader.getServerName());
            }
        }
    }

}
