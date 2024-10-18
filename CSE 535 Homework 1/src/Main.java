import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
public class Main {
    public static void main(String[] args) throws IOException {
        // Replace with input file for testing
        // Changed input path and firstFirst to get(0)
        Scanner in = new Scanner(new File("/Users/saiansh/CSE-535-Homework-1/CSE 535 Homework 1/src/lab1_Test.csv"));

        // Parsing the input file and saving contents
        HashMap<Integer, ArrayList<String>> sets = new HashMap<Integer, ArrayList<String>>();
        int keyToAdd = -100;
        while(in.hasNext()) {
            String line = in.nextLine();
            if(Character.isDigit(line.charAt(0))) {
                keyToAdd = Character.getNumericValue(line.charAt(0));
                sets.put(keyToAdd, new ArrayList<>());
                // Adding live servers to the beginning of the set list
                sets.get(keyToAdd).add(line.substring(line.indexOf("["), line.indexOf("]")+1));
            }
            sets.get(keyToAdd).add(line.substring(line.indexOf("("), line.indexOf(")")+1));
        }
        in.close();
        for(Map.Entry<Integer, ArrayList<String>> entry : sets.entrySet()) {
            int key = entry.getKey();
            ArrayList<String> value = entry.getValue();
            System.out.println(key + ": " + value);
        }

        int[] ports = {5000, 5001, 5002, 5003, 5004};
        Client[] clients = new Client[5];
        for(int i = 0; i < 5; i++) {
            String clientName = "C" + (i+1);
            clients[i] = new Client(clientName, ports[i]);
        }
        Server[] servers = new Server[5];

        // Starting servers and threads for each server
        for(int i = 0; i < 5; i++) {
            if(i == 0) {
                servers[i] = new Server("S1", ports[i]);
                servers[i].setClient(clients[i]);
                clients[i].setServer(servers[i]);
                new Thread(servers[i]).start();
            } else if(i == 1) {
                servers[i] = new Server("S2", ports[i]);
                servers[i].setClient(clients[i]);
                clients[i].setServer(servers[i]);
                new Thread(servers[i]).start();
            } else if(i == 2) {
                servers[i] = new Server("S3", ports[i]);
                servers[i].setClient(clients[i]);
                clients[i].setServer(servers[i]);
                new Thread(servers[i]).start();
            } else if(i == 3) {
                servers[i] = new Server("S4", ports[i]);
                servers[i].setClient(clients[i]);
                clients[i].setServer(servers[i]);
                new Thread(servers[i]).start();
            } else if(i == 4) {
                servers[i] = new Server("S5", ports[i]);
                servers[i].setClient(clients[i]);
                clients[i].setServer(servers[i]);
                new Thread(servers[i]).start();
            }
        }
        Paxos paxos = new Paxos(servers[0], servers[1], servers[2], servers[3], servers[4]);
        for(int i = 0; i < servers.length; i++) {
            servers[i].setPaxos(paxos);
        }
        // Get first set of transactions and attempt to run with it
        System.out.println(sets.get(1));
        String[] aliveServers = sets.get(1).get(0).split(", ");
        for(int i = 0; i < aliveServers.length; i++) {
            aliveServers[i] = aliveServers[i].replaceAll("\\[", "");
            aliveServers[i] = aliveServers[i].replaceAll("]", "");
        }
        for(int i = 1; i < sets.get(1).size(); i++) {
            String s = sets.get(1).get(i);
            String firstServer = s.substring(1, s.indexOf(","));
            String secondServer = s.substring(s.indexOf(",") + 2, s.lastIndexOf(","));
            String transferAmount = s.substring(s.lastIndexOf(",") + 2, s.indexOf(")"));
            int amount = Integer.parseInt(transferAmount);
            Transaction t = new Transaction(firstServer, secondServer, amount);
            // Have this transaction be sent from the right client
            int clientIndex = Character.getNumericValue(firstServer.charAt(1)) - 1;
            clients[clientIndex].sendTransaction(t);
        }
        // Each entry of hashmap is a new set, do a loop through all of them, after completion of each set prompt user with menu

    }
}