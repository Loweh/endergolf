package loweh.endergolf.database;
import java.sql.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Database {
    private String hostname;
    private int port;
    private String username;
    private String password;
    private String passFilePath;

    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet results;

    public Database(String hostname, int port, String username, String passFilePath) {
        try {
            File passFile = new File(passFilePath);
            Scanner sc = new Scanner(passFile);

            if (!sc.hasNextLine()) {
                sc.close();
                throw new IllegalArgumentException("File is empty.");
            }

            this.hostname = hostname;
            this.port = port;
            this.username = username;
            // The password should be the only line in the file. Only read the first.
            this.password = sc.nextLine().trim();

            sc.close();
        } catch (FileNotFoundException fnfEx) {
            throw new IllegalArgumentException("Invalid file path.");
        }
    }

    public boolean connect() throws SQLException {
        conn = DriverManager.getConnection("jdbc:mariadb://" + hostname + ":" + port + "/endergolf?&user=" + username + "&password=" + password + "&allowPublicKeyRetrieval=true");
        return conn.isValid(10);
    }

    public void close() throws SQLException {
        results.close();
        stmt.close();
        conn.close();
    }

    public ResultSet query(String query, ArrayList<Object> params) throws SQLException {
        results = null;

        if (connect()) {
            stmt = conn.prepareStatement(query);

            for (int i = 1; i <= params.size(); i++) {
                stmt.setObject(i, params.get(i - 1));
            }

            results = stmt.executeQuery();
        }

        return results;
    }
}
