package loweh.endergolf.database;

import loweh.endergolf.EnderGolf;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

public class Score {
    private String uuid;
    private ArrayList<Integer> scores;
    private Date dateTime;
    private boolean isFront;

    public Score(String uuid, ArrayList<Integer> scores, Date dateTime, boolean isFront) {
        if (scores.size() != 9 && scores.size() != 18) {
            throw new IllegalArgumentException("Scores must have either 9 or 18 values.");
        }

        for (int score : scores) {
            if (score < 1 || score > 9) {
                throw new IllegalArgumentException("Invalid score value. Expected: 1 - 9 (inclusive). Received: " + score);
            }
        }

        this.uuid = uuid;
        this.scores = scores;
        this.dateTime = dateTime;
        this.isFront = isFront;
    }

    public static ArrayList<Score> allScores(String subquery, ArrayList<Object> subparams) {
        Database db = new Database(EnderGolf.DB_HOSTNAME, EnderGolf.DB_PORT, EnderGolf.DB_USERNAME, EnderGolf.DB_PASS_FILE_PATH);

        ResultSet rs = null;
        ArrayList<Score> result = null;

        try {
            if (db.connect()) {
                rs = db.query(
                        "SELECT score.id AS id, score.uuid AS UUID, score.date_time AS date_time, " +
                        "hole_9_score.is_front AS is_front, hole_9_score.hole_1 AS hole_1, hole_9_score.hole_2 AS hole_2, " +
                        "hole_9_score.hole_3 AS hole_3, hole_9_score.hole_4 AS hole_4, hole_9_score.hole_5 AS hole_5, " +
                        "hole_9_score.hole_6 AS hole_6, hole_9_score.hole_7 AS hole_7, hole_9_score.hole_8 AS hole_8, hole_9_score.hole_9 AS hole_9 " +
                        "FROM score INNER JOIN hole_9_score ON score.id = hole_9_score.score_id " + (subquery != null ? subquery : ""),
                        subparams != null ? subparams : new ArrayList<Object>()
                );
            }

            if (rs == null) {
                throw new RuntimeException("Empty result set.");
            }

            result = new ArrayList<>();
            int prevScoreId = 0;

            while (rs.next()) {
                int scoreId = rs.getInt(1);
                String uuid = rs.getString(2);
                Timestamp timestamp = rs.getTimestamp(3);
                java.util.Date dateTime = new java.util.Date(timestamp.getTime());
                boolean isFront = rs.getBoolean(4);

                ArrayList<Integer> tmpScores = new ArrayList<>();

                for (int i = 5; i < 5 + 9; i++) {
                    tmpScores.add(rs.getInt(i));
                }

                if (scoreId == prevScoreId) {
                    result.getLast().addScores(tmpScores, isFront);
                } else {
                    result.add(new Score(uuid, tmpScores, dateTime, isFront));
                }

                db.close();
                prevScoreId = scoreId;
            }
        } catch (SQLException sqlEx) {
            System.out.println("Failed to get all scores from database due to SQL exception: " + sqlEx.getMessage());
        } catch (RuntimeException runEx) {
            System.out.println("Failed to get all scores from database due to runtime exception: " + runEx.getMessage());
        }

        return result;
    }

    public String getUUID() {
        return this.uuid;
    }

    public ArrayList<Integer> getScores() {
        return this.scores;
    }

    public boolean addScores(ArrayList<Integer> newScores, boolean front) {
        if (front) {
            ArrayList<Integer> tmp = this.scores;
            this.scores = newScores;
            return this.scores.addAll(tmp);
        } else {
            return this.scores.addAll(newScores);
        }
    }

    public void clearScores() {
        this.scores.clear();
    }

    public Date getDateTime() {
        return this.dateTime;
    }

    public boolean getIsFront() {
        return this.isFront;
    }
}
