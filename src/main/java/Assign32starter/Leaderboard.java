package Assign32starter;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

public class Leaderboard {

    static String[] lboard5;

    static String[] lboard;
    static double[] score5;

    static double[] score;



    static void leaderboardFULL(JSONObject jo, OutputPanel op) {
        //lboard = new String[5];
        //score = new double[5];
        JSONArray array = jo.getJSONArray("data");


        for(int i = array.length() - 1; i >= 0; i--) {
            JSONObject player= (JSONObject) array.get(i);
            //lboard[i] =
            //score[i]

            String name = player.getString("name");;
            double score = player.getDouble("score");
            int rank = player.getInt("rank");

            //String playerStr = "" + rank + ") " + score + " --" + name;


            op.appendOutput(String.format("%d) %.2f -- %s", rank, score, name));
        }






        op.appendOutput("Leaderboard (All Players)");
        op.appendOutput("Options: Start (Starts the game), Leaderboard (Shows top 5 scores), Leaderboard Full (Shows full leaderboard), Quit (shuts down game)");

        
    }
    static void leaderboard(JSONObject jo, boolean splash) {
        //keys shuold be
        //playername, rank, score

        int players = 0;

        lboard5 = new String[5];
        score5 = new double[5];
        JSONArray array = jo.getJSONArray("data");


        for(int i = 0; i < 5; i++) {
            JSONObject player= (JSONObject) array.get(i);
            lboard5[i] = player.getString("name");
            score5[i] = player.getDouble("score");
        }

        //sort(score5, lboard5);


        JLabel trophyLabel = new JLabel();



        JDialog lb = new JDialog();
        lb.setTitle("Top 5 scores");
        lb.setLayout(new GridBagLayout());
        lb.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        lb.setLocationRelativeTo(null);
        lb.setSize(300, 220);
        lb.getContentPane().setBackground(Color.white);
        lb.setModal(true);



        try {
            File trophy = new File("resourcesClient/trophy.png");
            BufferedImage img = ImageIO.read(trophy);
            ImageIcon trophyDialog = new ImageIcon(img);
            trophyLabel.setIcon(trophyDialog);
            GridBagConstraints c = new GridBagConstraints();
            c.gridx=0;
            c.gridy=0;
            lb.add(trophyLabel, c);

        } catch (Exception e) {
            System.out.println("unable to load image");
        }



        JLabel result1 = new JLabel(String.format("1) %.2f -- %s", score5[0], lboard5[0]));
        result1.setHorizontalAlignment(JLabel.RIGHT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.gridx = 0;
        lb.add(result1, gbc);

        JLabel result2 = new JLabel(String.format("2) %.2f -- %s", score5[1], lboard5[1]));
        result2.setHorizontalAlignment(JLabel.RIGHT);
        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.gridx = 0;
        lb.add(result2, gbc);

        JLabel result3 = new JLabel(String.format("3) %.2f -- %s", score5[2], lboard5[2]));
        result3.setHorizontalAlignment(JLabel.RIGHT);
        gbc = new GridBagConstraints();
        gbc.gridy = 3;
        gbc.gridx = 0;
        lb.add(result3, gbc);

        JLabel result4 = new JLabel(String.format("4) %.2f -- %s", score5[3], lboard5[3]));
        result4.setHorizontalAlignment(JLabel.RIGHT);
        gbc = new GridBagConstraints();
        gbc.gridy = 4;
        gbc.gridx = 0;
        lb.add(result4, gbc);

        JLabel result5 = new JLabel(String.format("5) %.2f -- %s", score5[4], lboard5[4]));
        result5.setHorizontalAlignment(JLabel.RIGHT);
        gbc = new GridBagConstraints();
        gbc.gridy = 5;
        gbc.gridx = 0;
        lb.add(result5, gbc);

        JButton button = new JButton("OK");

        gbc = new GridBagConstraints();
        gbc.gridy=6;
        gbc.gridx=0;
        lb.add(button, gbc);

        if (splash) {
            JLabel note = new JLabel();
            note.setText("(Full leaderboard available in game)");
            gbc = new GridBagConstraints();
            gbc.gridy=7;
            gbc.gridx=0;
            lb.add(note, gbc);
        }

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lb.dispose();
            }
        });


        lb.setVisible(true);





    }




}
