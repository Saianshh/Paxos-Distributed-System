import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        // Replace with input file for testing
        Scanner in = new Scanner(new File("C:\\Users\\Sai\\IdeaProjects\\CSE 535 Homework 1\\src\\lab1_Test.csv"));

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
        // Generate server stuff here
        // 5 servers, 5 clients in every case
//        Client c1 = new Client("C1");
//        Client c2 = new Client("C2");
//        Client c3 = new Client("C3");
//        Client c4 = new Client("C4");
//        Client c5 = new Client("C5");

        Client[] clients = new Client[5];
        for(int i = 0; i < 5; i++) {
            String clientName = "C" + (i+1);
            clients[i] = new Client(clientName);
        }

        Server[] servers = new Server[5];
        for(int i = 0; i < 5; i++) {
            if(i == 0) {
                servers[i] = new Server("S1");
                servers[i].setClient(clients[i]);
                clients[i].setServer(servers[i]);
                new Thread(servers[i]).start();
            } else if(i == 1) {
                servers[i] = new Server("S2");
                servers[i].setClient(clients[i]);
                clients[i].setServer(servers[i]);
                new Thread(servers[i]).start();
            } else if(i == 2) {
                servers[i] = new Server("S3");
                servers[i].setClient(clients[i]);
                clients[i].setServer(servers[i]);
                new Thread(servers[i]).start();
            } else if(i == 3) {
                servers[i] = new Server("S4");
                servers[i].setClient(clients[i]);
                clients[i].setServer(servers[i]);
                new Thread(servers[i]).start();
            } else if(i == 4) {
                servers[i] = new Server("S5");
                servers[i].setClient(clients[i]);
                clients[i].setServer(servers[i]);
                new Thread(servers[i]).start();
            }
        }
        // Get first set of transactions and attempt to run with it
        System.out.println(sets.get(1));
//        ArrayList<String> aliveServers = new ArrayList<>();
        String[] aliveServers = sets.get(1).getFirst().split(", ");
        for(int i = 0; i < aliveServers.length; i++) {
            aliveServers[i] = aliveServers[i].replaceAll("\\[", "");
            aliveServers[i] = aliveServers[i].replaceAll("]", "");
//            System.out.println(s[i]);
        }
        for(int i = 1; i < sets.get(1).size(); i++) {
            String s = sets.get(1).get(i);
            String firstServer = s.substring(1, s.indexOf(","));
            String secondServer = s.substring(s.indexOf(",") + 2, s.lastIndexOf(","));
            String transferAmount = s.substring(s.lastIndexOf(",") + 2, s.indexOf(")"));
            System.out.println(s);
            System.out.println(firstServer);
            System.out.println(secondServer);
            System.out.println(transferAmount);
            // put these in a transaction object
            // send to client to do transaction
            int amount = Integer.parseInt(transferAmount);
            Transaction t = new Transaction(firstServer, secondServer, amount);
            // Have this transaction be sent from the right client
            int clientIndex = Character.getNumericValue(firstServer.charAt(1)) - 1;
            clients[clientIndex].sendTransaction(t);
            
        }
        // Each entry of hashmap is a new set, do a loop through all of them, after completion prompt user with menu

    }
}