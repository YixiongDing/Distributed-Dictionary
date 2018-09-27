/* Yixiong Ding, 671499
 * <yixiongd@student.unimelb.edu.au>
 * COMP90015 Distributed System
 * September, 2018
 * The University of Melbourne */

import java.util.Map;
import java.util.HashMap;

public class Message {
    Message() {
        this.sb = new StringBuilder();
        this.empty = true;
    }
    public void put(String key, String value) {
        if (!empty) sb.append(SPLIT);
        sb.append(key);
        sb.append(SPLIT);
        sb.append(value);
        empty = false;
    }
    public String toString() {
        return sb.toString();
    }
    public static HashMap<String, String> toHashMap(String s) {
        HashMap<String, String> map = new HashMap<>();
        if (s == null || s == "") return map;
        String[] a = s.split(SPLIT);
        for (int i=0; i+1<a.length; i+=2) {
            map.put(a[i], a[i+1]);
            System.out.println(a[i] + ": " + a[i+1]);
        }
        return map;
    }
    public static String mapToString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry: map.entrySet()) {
            if (!first) sb.append(SPLIT);
            sb.append(entry.getKey());
            sb.append(SPLIT);
            sb.append(entry.getValue());
            first = false;
        }
        return sb.toString();
    }
    private StringBuilder sb;
    private boolean empty;
    private static final String SPLIT = "`";
}