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
        Client A = new Client("A");
        Client B = new Client("B");
        Client C = new Client("C");
        Client D = new Client("D");
        Client E = new Client("E");

        Server[] servers = new Server[5];
        for(int i = 0; i < 5; i++) {
            if(i == 0) {
                servers[i] = new Server("A");
                servers[i].setClient(A);
                A.setServer(servers[i]);
                new Thread(servers[i]).start();
            } else if(i == 1) {
                servers[i] = new Server("B");
                servers[i].setClient(B);
                B.setServer(servers[i]);
                new Thread(servers[i]).start();
            } else if(i == 2) {
                servers[i] = new Server("C");
                servers[i].setClient(C);
                C.setServer(servers[i]);
                new Thread(servers[i]).start();
            } else if(i == 3) {
                servers[i] = new Server("D");
                servers[i].setClient(D);
                D.setServer(servers[i]);
                new Thread(servers[i]).start();
            } else if(i == 4) {
                servers[i] = new Server("E");
                servers[i].setClient(E);
                E.setServer(servers[i]);
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
        }
        // Each entry of hashmap is a new set, do a loop through all of them, after completion prompt user with menu

    }
}