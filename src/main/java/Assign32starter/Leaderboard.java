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

public class Leaderboard {

    static String[] lboard;
    static int[] score;
    static void leaderboard(JSONObject jo) {
        //keys shuold be
        //playername, rank, score

        int players = 0;

        lboard = new String[5];
        score = new int[5];
        JSONArray array = jo.getJSONArray("data");


        for(int i = 0; i < 5; i++) {
            JSONObject player= (JSONObject) array.get(i);
            lboard[i] = player.getString("name");
            score[i] = player.getInt("score");
        }


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
            File trophy = new File("resources/trophy.png");
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



        JLabel result1 = new JLabel("1) "+ score[0] + "--" + lboard[0]);
        result1.setHorizontalAlignment(JLabel.RIGHT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.gridx = 0;
        lb.add(result1, gbc);

        JLabel result2 = new JLabel("2) "+ score[1] + "--" + lboard[1]);
        result2.setHorizontalAlignment(JLabel.RIGHT);
        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.gridx = 0;
        lb.add(result2, gbc);

        JLabel result3 = new JLabel("3) "+ score[2] + "--" + lboard[2]);
        result3.setHorizontalAlignment(JLabel.RIGHT);
        gbc = new GridBagConstraints();
        gbc.gridy = 3;
        gbc.gridx = 0;
        lb.add(result3, gbc);

        JLabel result4 = new JLabel("4) "+ score[3] + "--" + lboard[3]);
        result4.setHorizontalAlignment(JLabel.RIGHT);
        gbc = new GridBagConstraints();
        gbc.gridy = 4;
        gbc.gridx = 0;
        lb.add(result4, gbc);

        JLabel result5 = new JLabel("5) "+ score[4] + "--" + lboard[4]);
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

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lb.dispose();
            }
        });


        lb.setVisible(true);





    }



}
