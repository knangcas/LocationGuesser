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

    static boolean bonus=false;

    static int getScore() {
        return score;
    }

    static void addScore(int n) {
        score+=n;
    }

    static JSONObject evaluateResponse(JSONObject json, PicturePanel picPanel, OutputPanel outputPanel) throws PicturePanel.InvalidCoordinateException, IOException, InterruptedException {
        if (json.has("type")) {
            String type = json.getString("type");
            JSONObject response = new JSONObject();
            if (type.equals("message")) {
                //append msg
            } else if (type.equals("image")) {
                ImageIcon img = readImg(json);
                picPanel.insertImageI(0, 0, img);
                System.out.println("recieved image");
            } else if (type.equals("leaderboards")) {
                Leaderboard.leaderboard(json);
            } else if (type.equals("new game")) {
                picPanel.newGame(1);
                outputPanel.appendOutput("Starting new game. You have 30 seconds!");
                ImageIcon img = readImg(json);
                picPanel.insertImageI(0, 0, img);
                OutputPanel.timerReset();
                OutputPanel.timerStart();
                score = 0;
                bonus = false;
                outputPanel.setPoints(score);
            } else if (type.equals("+1")) {
                addScore(1);
                int streak = 0;
                int add = 0;
                if (json.has("streak")) {
                    streak = json.getInt("streak");
                }
                ImageIcon img = readImg(json);
                picPanel.insertImageI(0, 0, img);
                System.out.println("Success. Score + 1. New image loaded. ");
                if (streak > 0) {
                    outputPanel.appendOutput("Amazing! " + streak + " in a row! (" + add + " bonus points!");
                } else {
                    outputPanel.appendOutput("Correct!");
                }
                outputPanel.setPoints(score);
            } else if (type.equals("wrong guess")) {
                outputPanel.appendOutput("Incorrect! Try Again");
            } else if (type.equals("start") && !json.has("data")) {
                outputPanel.appendOutput(json.getString("message"));
                outputPanel.appendOutput("Options: Start (Starts the game), Leaderboard (Shows top 5 scores), Quit (shuts down game)");
            } else if (type.equals("start")) {
                outputPanel.appendOutput(json.getString("message"));
                ImageIcon img = readImg(json);
                picPanel.insertImageI(0, 0, img);
                System.out.println("recieved image");
            } else if (type.equals("start2")) {
                outputPanel.appendOutput(json.getString("message"));
                outputPanel.appendOutput("Options: Start (Starts the game), Leaderboard (shows leaderboard), Quit (shuts down game)");
            } else if (type.equals("notplayingCommands")) {
                JOptionPane.showMessageDialog(null, "Command not recognized. Options: Start, Leaderboard, Quit", "Invalid Command", JOptionPane.INFORMATION_MESSAGE);
                outputPanel.appendOutput("Invalid command");
            } else if (type.equals("notplayingCommandsIntro")) {
                JOptionPane.showMessageDialog(null, "Command not recognized. Options: Start, Leaderboard, Quit", "Invalid Command", JOptionPane.INFORMATION_MESSAGE);
                outputPanel.appendOutput("Invalid command");
            } else if (type.equals("quit")) {
                System.out.println("Quitting game.");
                response.put("type", "input");
                response.put("status", 2);
                response.put("input", "quit2");
                return response;
            } else if (type.equals("ok")) {
                System.out.println("sending score...");
                ImageIcon img = readImg(json);
                picPanel.insertImageI(0,0,img);
                response.put("type", "input");
                response.put("input", "gover!revog");
                response.put("status", 2);
                response.put("score", calculateScore());
                return response;
            }

        } else {

            JSONObject response = new JSONObject();
            if (json.has("ok") && !json.getBoolean("ok")) {
                response.put("error", json.getString("message"));
            }

            return response;
        }

        return null;

    }

    static String calculateScore() {
        int score = getScore();
        int seconds = OutputPanel.getTime();
        double finalScore = ((double)score/(double)seconds) * 100;

        return String.valueOf(finalScore);
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
