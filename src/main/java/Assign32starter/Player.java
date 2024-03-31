package Assign32starter;

public class Player {

    private String name;

    private String score;

    Player() {
    }

    Player(String name, String score) {
        this.score = score;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScore() {
        return this.score;
    }
    public void setScore(String score) {
        this.score = score;
    }

}
