package loweh.endergolf.database;

import loweh.endergolf.EnderGolf;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

public class ScoreAdmin {
    public int id;
    public String uuid;
    public Date dtGranted;

    public ScoreAdmin(int id, String uuid, Date dtGranted) {
        this.id = id;
        this.uuid = uuid;
        this.dtGranted = dtGranted;
    }

    public static ScoreAdmin fromDatabase(int id) {
        Database db = new Database(EnderGolf.DB_HOSTNAME, EnderGolf.DB_PORT, EnderGolf.DB_USERNAME, EnderGolf.DB_PASS_FILE_PATH);

        ArrayList<Object> params = new ArrayList<Object>();
        params.add(id);

        ResultSet rs = null;
        ScoreAdmin sAdmin = null;

        try {
            if (db.connect()) {
                rs = db.query("SELECT * FROM score_admin WHERE id = ?", params);
            }

            if (rs == null || !rs.next()) {
                throw new RuntimeException("Empty result set.");
            }

            int adminId = rs.getInt(1);
            String uuid = rs.getString(2);
            Timestamp timestamp = rs.getTimestamp(3);
            java.util.Date dtGranted = new java.util.Date(timestamp.getTime());

            sAdmin = new ScoreAdmin(adminId, uuid, dtGranted);
            db.close();
        } catch (SQLException sqlEx) {
            System.out.println("Failed to get ScoreAdmin with id " + id +  " from database due to SQL exception: " + sqlEx.getMessage());
        } catch (RuntimeException runEx) {
            System.out.println("Failed to get ScoreAdmin with id " + id + " from database due to runtime exception: " + runEx.getMessage());
        }

        return sAdmin;
    }

    public static ArrayList<ScoreAdmin> allFromDatabase() {
        Database db = new Database(EnderGolf.DB_HOSTNAME, EnderGolf.DB_PORT, EnderGolf.DB_USERNAME, EnderGolf.DB_PASS_FILE_PATH);

        ArrayList<ScoreAdmin> admins = null;
        ResultSet rs = null;

        try {
            if (db.connect()) {
                rs = db.query("SELECT * FROM score_admin WHERE 1=1", new ArrayList<Object>());
            }

            if (rs == null) {
                throw new RuntimeException("Empty result set.");
            }

            admins = new ArrayList<>();

            while (rs.next()) {
                int adminId = rs.getInt(1);
                String uuid = rs.getString(2);
                Timestamp timestamp = rs.getTimestamp(3);
                java.util.Date dtGranted = new java.util.Date(timestamp.getTime());

                ScoreAdmin sAdmin = new ScoreAdmin(adminId, uuid, dtGranted);
                admins.add(sAdmin);
            }

            db.close();
        } catch (SQLException sqlEx) {
            System.out.println("Failed to get all ScoreAdmins from database due to SQL Exception: " + sqlEx.getMessage());
        } catch (RuntimeException runEx) {
            System.out.println("Failed to get all ScoreAdmins from database due to runtime exception: " + runEx.getMessage());
        }

        return admins;
    }
}
