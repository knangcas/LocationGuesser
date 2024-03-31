package Assign32starter;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class PicturePanel extends JPanel {

  private static final long serialVersionUID = 1L;



  // States that the widget can be in
  private enum States {
    // no game started
    NotStarted, 
    // game started, but has no image and therefore doesn't know size
    InGameNoImage, 
    // game fully initialized
    InGameWithImage
  }

  // picture grid state
  private JLabel[][] labels;
  private States state;

  /**
   * Constructor
   */
  public PicturePanel() {
    setLayout(new FlowLayout());
    setSize(500, 500);
    labels = new JLabel[0][0];

    state = States.NotStarted;
  }

  /**
   * Creates a new game 
   * NOTE: Will reset all state and clear board
   * @param dimension - size of rows and columns
   */
  public void newGame(int dimension) {
    this.removeAll();
    setLayout(new GridLayout(dimension, dimension));
    labels = new JLabel[dimension][dimension];
    for (int row = 0; row < dimension; ++row) {
      for (int col = 0; col < dimension; ++col) {
        labels[row][col] = new JLabel();
        add(labels[row][col]);
      }
    }
    state = States.InGameNoImage;
  }

  /**
   * Utility method to set the dimensions of all containers
   * @param width of first image
   * @param height of first image
   */
  private void handleFirstImage(int width, int height) {
    if (state == States.InGameNoImage) {
      // calculate and set bounding box
      int totalDimensionWidth = labels.length * width;
      int totalDimensionHeight = labels.length * height;
      setSize(totalDimensionWidth, totalDimensionHeight);
      
      // set each images dimensions
      for (int row = 0; row < labels.length; ++row) {
        for (int col = 0; col < labels[0].length; ++col) {
          labels[row][col].setSize(width, height);
        }
      }
      state = States.InGameWithImage;
    }
  }

  public void insertImageI(int row, int col, ImageIcon image) throws IOException {

      handleFirstImage(image.getIconWidth(), image.getIconHeight());
      labels[row][col].setIcon(image);

  }
  
  
}
