import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        // Replace with input file for testing
        // Changed input path and firstFirst to get(0)
        Scanner in = new Scanner(new File("/Users/saiansh/CSE-535-Homework-1/CSE 535 Homework 1/src/lab1_Test.csv"));
        Scanner input = new Scanner(System.in);

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
//            System.out.println(key + ": " + value);
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
//        Paxos paxos = new Paxos(servers[0], servers[1], servers[2], servers[3], servers[4]);
//        for(int i = 0; i < servers.length; i++) {
//            servers[i].setPaxos(paxos);
//        }
        // Get first set of transactions and attempt to run with it
        for(int i = 1; i < sets.size()+1; i++) {
//            System.out.println(sets.get(i));
            String[] aliveServers = sets.get(i).get(0).split(", ");
            for(int j = 0; j < aliveServers.length; j++) {
                aliveServers[j] = aliveServers[j].replaceAll("\\[", "");
                aliveServers[j] = aliveServers[j].replaceAll("]", "");
            }
            ArrayList<Server> newServerSet = new ArrayList<>();
            for(int j = 0; j < servers.length; j++) {
                String serverName = servers[j].getServerName();
                for(int k = 0; k < aliveServers.length; k++) {
                    if(serverName.equals(aliveServers[k])) {
                        newServerSet.add(servers[j]);
                        break;
                    }
                }
            }
//            System.out.println("NEW SERVER SET");
//            System.out.println(newServerSet);
            Paxos newPaxos = new Paxos(newServerSet);
            for(int j = 0; j < servers.length; j++) {
                servers[j].setPaxos(newPaxos);
            }
            for(int j = 0; j < servers.length; j++) {
                if(!newServerSet.contains(servers[j])) {
                    servers[j].setWasFailed(true);
                    // Crashed and may have had local transactions
//                    System.out.println("Set " + servers[j].getServerName() + " to fail");
//                    System.out.println(servers[j].getLocalLog());
                    for(int k = 0; k < servers[j].getLocalLog().size(); k++) {
                        servers[j].setBalance(servers[j].getBalance() + servers[j].getLocalLog().get(k).getAmt());
                    }
                    servers[j].getLocalLog().clear();
                } else {
                    servers[j].setWasFailed(false);
                }
            }
            for(int j = 0; j < servers.length; j++) {
                if(servers[j].getQueueNonMajority().size() > 0) {
                    if(servers[j].getQueueNonMajority().peek() == null) {
                        servers[j].getQueueNonMajority().clear();
                    }
                    for (Transaction transaction : servers[j].getQueueNonMajority()) {
                        servers[j].performTransaction(transaction);
                        servers[j].getQueueNonMajority().remove();
                    }
                }
            }
            long start = System.currentTimeMillis();
            for(int j = 1; j < sets.get(i).size(); j++) {
                String s = sets.get(i).get(j);
                String firstServer = s.substring(1, s.indexOf(","));
                String secondServer = s.substring(s.indexOf(",") + 2, s.lastIndexOf(","));
                String transferAmount = s.substring(s.lastIndexOf(",") + 2, s.indexOf(")"));
                int amount = Integer.parseInt(transferAmount);
                Transaction t = new Transaction(firstServer, secondServer, amount);
                // Have this transaction be sent from the right client
                int clientIndex = Character.getNumericValue(firstServer.charAt(1)) - 1;
                clients[clientIndex].sendTransaction(t);
            }
            for(int j = 0; j < servers.length; j++) {
                if (servers[j].getPaxosInitiated()) {
//                    while (servers[j].getPaxosInitiated()) {
                    Thread.sleep(500);
//                    System.out.println("waiting for paxos to finish");
                    if (servers[j].getNumPromiseMessages() < 3) {
                        // If not enough promises were received, queue the transaction
//                        System.out.println("Not enough promises, queuing the transaction for later.");
//                        System.out.println("Adding " + servers[j].getPaxosTransaction() + " to the queue");
                        servers[j].addToQueueNonMajority(servers[j].getPaxosTransaction());  // Add the transaction to the queue for later execution
                        servers[j].setNumPromiseMessages(0);
                        servers[j].setEnteredAccept(false);
                        servers[j].setPaxosInitiated(false);
                    } else if(servers[j].getQueue().size() > 0) {
                        servers[j].getPaxos().postPaxosQueue(servers[j]);
                    }

//
                }
            }
            long end = System.currentTimeMillis();
            int size = sets.get(i).size()-1;
            boolean nextSet = false;
            System.out.println("Set " + i + ":");
            while(!nextSet) {
                System.out.println("MENU:");
                System.out.println("Press the b key for balances after this set");
                System.out.println("Press the l key to see local logs of a server");
                System.out.println("Press the d key to see the current datastore");
                System.out.println("Press the t key to see throughput and latency for this set");
                System.out.println("Press the n key to go onto the next set");
                System.out.println("Enter your selection (case sensitive): ");
                String given = input.nextLine();
                if(given.equals("b")) {
                    PrintBalance(servers);
                } else if(given.equals("l")) {
                    System.out.println("Which server would you like to see? (S1-S5, case sensitive): ");
                    String givenServer = input.nextLine();
                    for(int s = 0; s < servers.length; s++) {
                        if(servers[s].getServerName().equals(givenServer)) {
                            PrintLog(servers[s]);
                        }
                    }
                } else if(given.equals("d")) {
                    PrintDB(servers);
                } else if(given.equals("t")) {
                    Performance(start, end, size);
                } else if(given.equals("n")) {
                    nextSet = true;
                }
            }
        }
        // Each entry of hashmap is a new set, do a loop through all of them, after completion of each set prompt user with menu

    }
    public static void PrintBalance(Server[] servers) {
        for(int j = 0; j < servers.length; j++) {
            System.out.println("Server " + servers[j].getServerName() + ": " + servers[j].getBalance());
        }
    }
    public static void PrintLog(Server server) {
        System.out.println(server.getLocalLog());
    }
    public static void PrintDB(Server[] servers) {
        for(MajorBlock block : servers[0].getDatastore()) {
            System.out.print(block + " -> ");
        }
        System.out.println();
    }
    public static void Performance(long start, long end, int total) {
        long latency = end - start;
        double seconds = latency / 1000.0;
        double throughput = total / seconds;
        System.out.println("Throughput: " + throughput + " transactions per second, Latency: " + latency + " ms");
    }
}