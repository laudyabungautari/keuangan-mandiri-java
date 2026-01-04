package util;

import java.io.*;
import java.util.*;

public class CsvUtil {

    public static String esc(String v) {
        if (v == null) return "";
        boolean needQuote = v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r");
        String out = v.replace("\"", "\"\"");
        return needQuote ? "\"" + out + "\"" : out;
    }

    public static List<String> parseLine(String line) {
        List<String> res = new ArrayList<>();
        if (line == null) return res;

        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else inQuotes = false;
                } else cur.append(c);
            } else {
                if (c == ',') {
                    res.add(cur.toString());
                    cur.setLength(0);
                } else if (c == '"') inQuotes = true;
                else cur.append(c);
            }
        }
        res.add(cur.toString());
        return res;
    }

    public static List<List<String>> readAll(String path) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                rows.add(parseLine(line));
            }
        }
        return rows;
    }

    public static void appendRow(String path, List<String> cols) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true))) {
            for (int i = 0; i < cols.size(); i++) {
                if (i > 0) bw.write(",");
                bw.write(esc(cols.get(i)));
            }
            bw.newLine();
        }
    }

    public static void writeAll(String path, List<List<String>> rows) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (List<String> cols : rows) {
                for (int i = 0; i < cols.size(); i++) {
                    if (i > 0) bw.write(",");
                    bw.write(esc(cols.get(i)));
                }
                bw.newLine();
            }
        }
    }
}
