package Assign32starter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.*;

/**
 * The output panel that includes an input box, a submit button, and an output
 * text area.
 *
 * Methods of interest
 * ----------------------
 * getInputText() - Get the input text box text
 * setInputText(String newText) - Set the input text box text
 * addEventHandlers(EventHandlers handlerObj) - Add event listeners
 * appendOutput(String message) - Add message to output text
 */
public class OutputPanel extends JPanel {
  // Needed because JPanel is Serializable
  private static final long serialVersionUID = 2L;


  /**
   * Generic event handler for events generated in the panel GUI
   *
   * Uses Observer pattern
   */
  public interface EventHandlers {
    // Executes for every key press in the input textbox
    void inputUpdated(String input);

    // executes when the submit button is clicked
    void submitClicked();
  }
  private JLabel pointsLabel = new JLabel("Current Points this round: 0");
  private JTextField input;
  private JButton submit;
  private JTextArea area;
  private ArrayList<EventHandlers> handlers = new ArrayList<>();

  static Timer timer;

  static int seconds;

  JLabel cd;

  static int score;

  static boolean gOver;

  /**
   * Constructor
   */
  public OutputPanel() {
    setLayout(new GridBagLayout());

    // Setup input text box
    GridBagConstraints c = new GridBagConstraints();



    c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.3;
    add(this.pointsLabel, c);
    c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    c.gridx = 1;
    c.gridy = 0;
    //c.gridwidth = 2;
    //c.weightx = 0.3;
    seconds = 30;
    cd = new JLabel("30");
    cd.setHorizontalAlignment(JLabel.RIGHT);
    cdTimer();
    add(cd, c);


    // c = new GridBagConstraints();
    // c.fill = GridBagConstraints.HORIZONTAL;
    // c.gridx = 1;
    // c.gridy = 0;
    // c.weightx = 0.3;
    // JTextField textField = new JTextField("Hello tx");
    // add(textField, c);

    c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0.75;
    input = new JTextField();
    input.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        JTextField textField = (JTextField) e.getSource();
        for (EventHandlers handler : handlers) {
          handler.inputUpdated(textField.getText());
        }
      }
    });
    input.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent e){

        JTextField textField = (JTextField) e.getSource();
        for (EventHandlers handler : handlers) {
          handler.submitClicked();
        }

      }});
    add(input, c);

    // Setup submit button
    c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 1;
    c.gridy = 1;
    submit = new JButton("Submit");
    submit.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == submit) {
          for (EventHandlers handler : handlers) {
            handler.submitClicked();
          }
        }
      }
    });
    add(submit, c);

    // Setup scrollable output text area
    c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 2;
    c.weighty = 0.75;
    area = new JTextArea();
    JScrollPane pane = new JScrollPane(area);
    add(pane, c);


  }



  public void cdTimer() {
    Font font1 = new Font("Arial", Font.BOLD, 32);


    timer = new Timer(1000, new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        seconds--;
        if (seconds < 4) {
          cd.setFont(font1);
          cd.setForeground(Color.RED);
        }
        cd.setText(""+seconds);



        if (seconds == 0) {
          timer.stop();
          timerEnd();
        }

      }
    });
  }

  public static void timerReset() {
    seconds = 30;
  }

  private void timerEnd() {
    JOptionPane.showMessageDialog(null,  "Game Over. Your final score is: " + score, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    gOver = true;
    input.setText("gover!revog");
    for (EventHandlers handler : handlers) {
      handler.submitClicked();
    }
    clearInputText();
  }

  public boolean getGameStatus() {
    return gOver;
  }


  public static void timerStart() {
    timer.start();
  }

  public static void timerStop() {
    timer.stop();
  }


  /**
   * Get input text box text
   * @return input box value
   */
  public String getInputText() {
    return input.getText();
  }

  public String getWindowText() {
    return area.getText();
  }

  public void clearText() {
    //input.setText("");
    area.setText("");
  }

  public void clearInputText() {
    input.setText("");
  }

  /**
   * Set points in label box
   * @param points current points in round
   */
  public void setPoints(int points) {
    pointsLabel.setText("Current Points this round: " + points);
    score = points;
  }

  /**
   * Set input text box text
   * @param newText the text to put in the text box
   */
  public void setInputText(String newText) {
    input.setText(newText);
  }

  /**
   * Register event observers
   * @param //handler
   */
  public void addEventHandlers(EventHandlers handlerObj) {
    handlers.add(handlerObj);
  }

  /**
   * Append a message to the output panel
   * @param message - the message to print
   */
  public void appendOutput(String message) {
    String current = this.getWindowText();
    clearText();
    area.append(message + "\n");
    area.append(current);
    area.setCaretPosition(0);

  }
}
