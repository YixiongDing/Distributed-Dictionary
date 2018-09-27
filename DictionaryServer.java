/* Yixiong Ding, 671499
 * <yixiongd@student.unimelb.edu.au>
 * COMP90015 Distributed System
 * September, 2018
 * The University of Melbourne */

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import javax.net.ServerSocketFactory;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class DictionaryServer {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("java DictionaryServer <PORT> <DICT FILE>");
            System.exit(1);
        }

        try {
            int port = Integer.parseInt(args[0]);
            String dict = args[1];

            File f = new File(dict);
            if (!f.exists() || f.isDirectory()) {
                System.err.println(dict + " is not a valid file");
                System.exit(1);
            }

            ServerSocketFactory factory = ServerSocketFactory.getDefault();
            ServerSocket server = factory.createServerSocket(port);
            dictionary = new Dictionary(dict);
            System.out.println("load " + dict + " complete");

            while (true) {
                Socket client = server.accept();
                Thread t = new Thread(new ServeThread(client, dict));
                t.start();
            }
        } catch (NumberFormatException e) {
            System.err.println("\"" + args[0] + "\"" + " is not a valid integer");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ServeThread implements Runnable {
        ServeThread(Socket socket, String dict) {
            this.socket = socket;
            this.dict = dict;
        }

        public void run() {
            try {
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());

                while (true) {
                    String input_string = input.readUTF();
                    if (null == input_string) break;
                    HashMap<String, String> map = Message.toHashMap(input_string);
                    System.out.println(map.toString());
                    String operation = map.get("operation");
                    String word = map.get("word");
                    Message msg = new Message();

                    if (operation.equals("SEARCH")) {
                        boolean succeed = dictionary.hasWord(word);
                        if (succeed) {
                            String meaning = dictionary.search(word);
                            msg.put("status", "SUCCESS");
                            msg.put("meaning", meaning);
                            msg.put("message", "found " + "'"+ word + "'");
                        } else {
                            msg.put("status", "FAIL");
                            msg.put("message", "'"+ word + "'" + " not found");
                        }
                    } else if (operation.equals("ADD")) {
                        boolean succeed = dictionary.add(word, map.get("meaning"));
                        if (succeed) {
                            msg.put("status", "SUCCESS");
                            msg.put("message", "add " + "'"+ word + "'"+ " successfully");
                            dictionary.writeDisk();
                        } else {
                            msg.put("status", "FAIL");
                            msg.put("message","'"+ word + "'" + " already exist");
                        }
                    } else if (operation.equals("REMOVE")) {
                        boolean succeed = dictionary.remove(word);
                        if (succeed) {
                            msg.put("status", "SUCCESS");
                            msg.put("message", "'"+ word + "'"+" removed successfully");
                            dictionary.writeDisk();
                        } else {
                            msg.put("status", "FAIL");
                            msg.put("message", "invaild removing " + "'"+ word + "'");
                        }
                    }

                    output.writeUTF(msg.toString());
                }
            } catch (IOException e) {
                // e.printStackTrace();
                System.out.println("connection lost");
            }
        }

        private Socket socket;
        private String dict;
    }

    private static class Dictionary {
        Dictionary(String dict) {
            this.dict = dict;
            this.map = toHashMap(dict);
        }

        public boolean hasWord(String word) {
            return this.map.containsKey(word);
        }

        public String search(String word) {
            System.out.println("[SEARCH] " +  word);
            return this.map.get(word);
        }

        public boolean add(String word, String meaning) {
            System.out.println("[ADD] " + word);
            if (hasWord(word)) return false;
            return this.map.put(word, meaning) == null;
        }

        public boolean remove(String word) {
            System.out.println("[REMOVE] " +  word);
            return this.map.remove(word) != null;
        }

        public void writeDisk() {
            try {
                String s = Message.mapToString(this.map);
                System.out.println("[WRITE DISK] " + s);
                BufferedWriter writer = new BufferedWriter(new FileWriter(this.dict));
                writer.write(s);
                writer.close();
            } catch (IOException e) {
                System.out.println("Error writing to file");
                System.exit(0);
            }
        }

        private static String readDictFile(String dictFile) {
            StringBuilder sb = new StringBuilder();
            String b = null;

            try { 
                boolean first = true;
                BufferedReader br = new BufferedReader(new FileReader(dictFile));
                String line = null;
                while (true) {
                    line = br.readLine();
                    if (line == null) break;
                    if (!first) sb.append("`");
                    sb.append(line);
                    first = false;
                }
                br.close();
                b = sb.toString();
            } catch (Exception e) {
                
            }

            return b;
        }

        private static ConcurrentHashMap<String, String> toHashMap(String dict) {
            String s = readDictFile(dict);
            System.out.println(s);
            if (s == null) s = "";
            Map<String, String> mp = Message.toHashMap(s);
            return new ConcurrentHashMap<String, String>(mp);
        }

        private ConcurrentHashMap<String, String> map;
        private String dict;
    }

    static Dictionary dictionary;
}