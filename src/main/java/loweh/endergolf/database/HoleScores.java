package loweh.endergolf.database;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class HoleScores {
    public String uuid;
    public ArrayList<Integer> scores;
    public LocalDateTime dateTime;

    public HoleScores(String uuid, ArrayList<Integer> scores, LocalDateTime dateTime) {
        this.uuid = uuid;
        this.scores = scores;
        this.dateTime = dateTime;
    }
}
