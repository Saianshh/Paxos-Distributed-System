import java.io.IOException;
import java.net.*;
import java.util.*;
import java.io.*;

public class Client {
    private String clientName;
    private int balance;
    private Server server;
    private int port;

    public Client(String clientName, int port) {
        this.clientName = clientName;
        this.balance = 100;
        this.server = null;
        this.port = port;
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
    public void sendTransaction(Transaction t) throws IOException {
        // Send a transaction to server
        System.out.println("CLIENT SIDE PRE SOCKET: " + t.getS1() + " sending " + t.getAmt() + " to " + t.getS2());
        try {
            Socket socket = new Socket("localhost", this.port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send the transaction details to the server
            out.println(t.getS1() + "," + t.getS2() + "," + t.getAmt());

            // Get confirmation from the server
            String confirmation = in.readLine();

            System.out.println("Server response: " + confirmation);
            // May get rid of this line
            socket.close();
            } catch (IOException e){
                e.printStackTrace();
            }
    }
//        this.server.performTransaction(t);
}
