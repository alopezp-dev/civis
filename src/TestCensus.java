import java.sql.*;
import java.io.File;
import java.util.*;

public class TestCensus {
    public static void main(String[] args) {
        String dbPath = "census.db";
        File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            System.out.println("File not found: " + dbPath);
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:civis.db");
                Statement stmt = conn.createStatement()) {

            String attachSql = "ATTACH DATABASE '" + dbFile.getAbsolutePath().replace("\\", "/") + "' AS census";
            System.out.println("Executing: " + attachSql);
            stmt.execute(attachSql);

            System.out.println("Diagnosing mismatches (sampling first 100,000 rows)...");

            // 1. Load known localities
            Set<String> knownLocs = new HashSet<>();
            try (Statement stmtLoc = conn.createStatement();
                    ResultSet rsLoc = stmtLoc.executeQuery("SELECT nombre, provincia_nombre FROM localidades")) {
                while (rsLoc.next()) {
                    String n = rsLoc.getString(1);
                    String p = rsLoc.getString(2);
                    if (n != null) {
                        String key = n.trim().toUpperCase() + "|" + (p != null ? p.trim().toUpperCase() : "NULL");
                        knownLocs.add(key);
                        // Also add without province for looser matching check? No, strict for now.
                    }
                }
            }
            System.out.println("Loaded " + knownLocs.size() + " main localities.");

            // 2. Scan census sample
            int totalSampled = 0;
            int matches = 0;
            Map<String, Integer> missingCounts = new HashMap<>();

            try (ResultSet rs = stmt.executeQuery(
                    "SELECT municipio, provincia FROM census.direcciones WHERE id % 100 = 0 LIMIT 100000")) {
                while (rs.next()) {
                    totalSampled++;
                    String m = rs.getString(1);
                    String p = rs.getString(2);
                    if (m == null)
                        continue;

                    String key = m.trim().toUpperCase() + "|" + (p != null ? p.trim().toUpperCase() : "NULL");

                    if (knownLocs.contains(key)) {
                        matches++;
                    } else {
                        missingCounts.put(key, missingCounts.getOrDefault(key, 0) + 1);
                    }
                }
            }

            System.out.println("Sampled: " + totalSampled);
            System.out.println("Matched: " + matches);
            System.out.println("Match Rate: " + (matches * 100.0 / totalSampled) + "%");

            System.out.println("\nTop 20 Missing Municipalities in Sample:");
            missingCounts.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(20)
                    .forEach(e -> System.out.println(" - " + e.getKey() + ": " + e.getValue()));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
