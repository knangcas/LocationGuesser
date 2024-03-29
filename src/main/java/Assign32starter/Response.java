package Assign32starter;

import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

public class Response {

public static int score = 0;

static int getScore() {
    return score;
}

static void addScore(int n) {
    score+=n;
}

    static JSONObject evaluateResponse(JSONObject json, PicturePanel picPanel, OutputPanel outputPanel) throws PicturePanel.InvalidCoordinateException, IOException {
            String type = json.getString("type");
            if (type.equals("message")) {
                //append msg
            } else if (type.equals("image")) {
                ImageIcon img= readImg(json);
                picPanel.insertImageI(0,0,img);
                System.out.println("recieved image");
            } else if (type.equals("leaderboards")) {
                Leaderboard.leaderboard(json);
            } else if (type.equals("new game")) {
                picPanel.newGame(1);
                outputPanel.appendOutput("Starting new game. You have 30 seconds!");
                //TODO timer
                ImageIcon img = readImg(json);
                picPanel.insertImageI(0,0,img);
                OutputPanel.timerStart();
            } else if (type.equals("+1")) {
                addScore(1);
                ImageIcon img = readImg(json);
                picPanel.insertImageI(0,0,img);
                System.out.println("Success. Score + 1. New image loaded. ");
                outputPanel.appendOutput("Correct!");
                outputPanel.setPoints(score);
            } else if (type.equals("wrong guess")) {
                outputPanel.appendOutput("Incorrect!");
            } else if (type.equals("start") && !json.has("data")) {
                outputPanel.appendOutput(json.getString("message"));
            } else if (type.equals("start")) {
                outputPanel.appendOutput(json.getString("message"));
                ImageIcon img= readImg(json);
                picPanel.insertImageI(0,0,img);
                System.out.println("recieved image");

            }



            return null;

    }

    static ImageIcon readImg(JSONObject jo) {
        System.out.println("Your image");
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] bytes = decoder.decode(jo.getString("data"));
        ImageIcon icon = null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            BufferedImage image = ImageIO.read(bais);
            icon = new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return icon;
    }




}
