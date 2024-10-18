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

}
