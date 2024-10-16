public class Client {
    private String clientName;
    private int balance;
    private Server server;

    public Client(String clientName) {
        this.clientName = clientName;
        this.balance = 100;
        this.server = null;
    }

    public String getClientName() {
        return this.clientName;
    }
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
    public int getBalance() {
        return this.balance;
    }
    public void setBalance(int balance) {
        this.balance = balance;
    }
    public Server getServer() {
        return this.server;
    }
    public void setServer(Server server) {
        this.server = server;
    }
    public void sendTransaction(Transaction t) {
        // Send a transaction to server
        server.performTransaction(t);
    }
}
