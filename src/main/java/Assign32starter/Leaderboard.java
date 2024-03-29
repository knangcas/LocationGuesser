package Assign32starter;

import org.json.JSONObject;

import javax.swing.*;

public class Leaderboard {

    static void leaderboard(JSONObject jo) {
        //keys shuold be
        //playername, rank, score

        JDialog lb = new JDialog();
        lb.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JLabel results = new JLabel("Super\nCool\nGuy");

        lb.add(results);
        lb.setLocationRelativeTo(null);
        lb.setVisible(true);

    }



}
