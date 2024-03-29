package Assign32starter;

public class Player {

    private String name;

    private int score;

    Player() {

    }

    Player(String name, int score) {
        this.score = score;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return this.score;
    }
    public void setScore(int score) {
        this.score = score;
    }


}
