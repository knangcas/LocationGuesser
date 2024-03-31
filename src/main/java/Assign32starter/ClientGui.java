package Assign32starter;

import java.awt.*;

import com.google.gson.Gson;
import org.json.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * The ClientGui class is a GUI frontend that displays an image grid, an input text box,
 * a button, and a text area for status. 
 *
 * Methods of Interest
 * ----------------------
 * show(boolean modal) - Shows the GUI frame with current state
 *     -> modal means that it opens GUI and suspends background processes. 
 * 		  Processing still happens in the GUI. If it is desired to continue processing in the 
 *        background, set modal to false.
 * newGame(int dimension) - Start a new game with a grid of dimension x dimension size
 * insertImage(String filename, int row, int col) - Inserts an image into the grid
 * appendOutput(String message) - Appends text to the output panel
 * submitClicked() - Button handler for the submit button in the output panel
 *
 * Notes
 * -----------
 * > Does not show when created. show() must be called to show he GUI.
 *
 */
public class ClientGui implements Assign32starter.OutputPanel.EventHandlers {
	JFrame frame;
	PicturePanel picPanel;
	OutputPanel outputPanel;
	String currentMess;

	TimerPanel timerPanel;

	Socket sock;
	OutputStream out;
	DataOutputStream os;
	BufferedReader bufferedReader;

	static ImageIcon logo;

	DataInputStream in;

	// TODO: SHOULD NOT BE HARDCODED change to spec
	String host = "localhost";
	int port = 9000;

	int start = 0;

	int score = 0;

	String name;

	int timer;

	boolean gameStarted;



	/**
	 * Construct dialog
	 * @throws IOException
	 */
	public ClientGui(String host, int port) throws IOException {
		this.host = host;
		this.port = port;
		gameStarted = false;

		frame = new JFrame("Name The Place 2024!");
		frame.getContentPane().setBackground(Color.BLACK);
		frame.setLayout(new GridBagLayout());
		frame.setMinimumSize(new Dimension(563, 700));
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);


		// setup the top picture frame
		picPanel = new PicturePanel();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 0.25;
		frame.add(picPanel, c);

		// setup the input, button, and output area
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 0.75;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		outputPanel = new OutputPanel();
		outputPanel.addEventHandlers(this);
		frame.add(outputPanel, c);




		picPanel.newGame(1);
		//insertImage("img/ASU1.png", 0, 0);

		open(); // opening server connection here
		//currentMess = "{'type': 'start'}"; // very initial start message for the connection
		JSONObject request = new JSONObject();
		request.put("type", "start");
		request.put("status", 0);
		//request.put("type", "start");

		byte[] sendThis = convert2Bytes(request);
		os.writeInt(sendThis.length);
		os.write(sendThis);
		os.flush();
		//os.writeObject(request.toString());
		//os.flush();

		//String s = "";
		/*try {
			int inLen = in.readInt();
			byte[] message = new byte[inLen];
			in.readFully(message,0,message.length);

			//os.writeObject(request.toString());
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		 */


		int inLen = in.readInt();
		byte[] message = new byte[inLen];
		in.readFully(message,0,message.length);
		String i;

		i = convertFromBytes(message);


		//byte[] responseBytes = NetworkUtils.Receive(in);
		JSONObject res = new JSONObject(i);
		System.out.println("Connection Successful");

		ImageIcon ii = Response.readImg(res);
		//outputPanel.appendOutput(res.getString("message"));




		try {
			picPanel.insertImageI(0, 0, ii);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		//String string = this.bufferedReader.readLine();
		System.out.println("Got a connection to server");
		//JSONObject json = new JSONObject(string);
		; // putting the message in the outputpanel

		// reading out the image (abstracted here as just a string)
		System.out.println("Pretend I got an image: ");
		/// would put image in picture panel
		close(); //closing the connection to server

		JDialog welcome = new JDialog();
		welcome.setResizable(false);
		welcome.setTitle("Name The Place 2024!");
		welcome.setModal(true);
		welcome.getContentPane().setBackground(Color.WHITE);
		welcome.setSize(700,700);
		welcome.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		welcome.setLayout(new GridBagLayout());

		JLabel welcomeText = new JLabel();
		welcomeText.setText("Hello! What is your name?");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		welcome.add(welcomeText, c);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 0.75;
		JTextField nameField = new JTextField();
		nameField.setPreferredSize(new Dimension(180, 25));
		//nameField.setSize(180, 50);
		welcome.add(nameField, c);

		JButton nameButton = new JButton("Submit");
		nameButton.setPreferredSize(new Dimension(120, 25));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		welcome.add(nameButton, c);

		JButton leaderButton = new JButton("Leaderboard");
		leaderButton.setPreferredSize(new Dimension(120, 25));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 4;
		welcome.add(leaderButton, c);

		JButton quitButton = new JButton("Quit");
		quitButton.setPreferredSize(new Dimension(120, 25));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 5;
		welcome.add(quitButton, c);



		nameButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				name = nameField.getText();
				outputPanel.setInputText(name);
				System.out.println("Player's name is " + name);
				submitClicked();
				welcome.dispose();
			}
		});

		leaderButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Showing leaderboard");
				outputPanel.setInputText("leaderboardSplash");
				start = 2;
				submitClicked();
				start = 0;
			}
		});


		quitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Quitting...");
				outputPanel.setInputText("quit");
				start = 2;
				submitClicked();
				welcome.dispose();
			}
		});

		JLabel logoLabel = new JLabel();
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		logoLabel.setIcon(logo);
		welcome.add(logoLabel, c);

		welcome.setLocationRelativeTo(null);
		welcome.setVisible(true);





		// Now Client interaction only happens when the submit button is used, see "submitClicked()" method
	}

	private static byte[] convert2Bytes(JSONObject jo) {
		Gson gson = new Gson();
		byte[] byteArray = gson.toJson(jo.toString()).getBytes();

		return byteArray;
	}

	private static String convertFromBytes(byte[] bytes) {
		Gson gson = new Gson();
		String conv = new String(bytes, StandardCharsets.UTF_8);
		String convert = gson.fromJson(conv, String.class);

		return convert;
	}

	/**
	 * Shows the current state in the GUI
	 * @param makeModal - true to make a modal window, false disables modal behavior
	 */
	public void show(boolean makeModal) {
		frame.pack();
		//frame.setModal(makeModal);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}


	/**
	 * Creates a new game and set the size of the grid 
	 * @param dimension - the size of the grid will be dimension x dimension
	 * No changes should be needed here
	 */


	/**
	 * Insert an image into the grid at position (col, row)
	 *
	 * @param filename - filename relative to the root directory
	 * @param row - the row to insert into
	 * @param col - the column to insert into
	 * @return true if successful, false if an invalid coordinate was provided
	 * @throws IOException An error occured with your image file
	 */
	public boolean insertImage(String filename, int row, int col) throws IOException {
		System.out.println("Image insert");
		String error = "";
		try {
			// insert the image
			if (picPanel.insertImage(filename, row, col)) {
				// put status in output
				outputPanel.appendOutput("Inserting " + filename + " in position (" + row + ", " + col + ")"); // you can of course remove this
				return true;
			}
			error = "File(\"" + filename + "\") not found.";
		} catch(PicturePanel.InvalidCoordinateException e) {
			// put error in output
			error = e.toString();
		}
		outputPanel.appendOutput(error);
		return false;
	}

	/**
	 * Submit button handling
	 *
	 * TODO: This is where your logic will go or where you will call appropriate methods you write. 
	 * Right now this method opens and closes the connection after every interaction, if you want to keep that or not is up to you. 
	 */
	@Override
	public void submitClicked() {
		try {
			JSONObject send = new JSONObject();
			System.out.println("Submit clicked. Sending message");


			// Pulls the input box text
			String input = outputPanel.getInputText();
			if (start > 0) {
				input = input.toLowerCase();
			}
			if (input.equals("start")) {

				boolean check = false;
				while (!check) {
					String time = null;
					time = JOptionPane.showInputDialog("How many seconds would you like to play for?");

					if (time == null && input.equals("start")) {
						outputPanel.appendOutput("No time entered. Choose an option: Start (Starts new game), Leaderboard (Shows top 5 scores), Quit (shuts down game)");
						outputPanel.clearInputText();
						return;
					}
					try {
						timer = Integer.parseInt(time);
						if (timer < 5) {
							throw new NumberFormatException("Number less than 5");
						}
						check = true;
						//OutputPanel.setTime(timer);
						send.put("time", time);

					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
						JOptionPane.showMessageDialog(null, "Please enter a number. Minimum = 5 seconds", "Error", JOptionPane.ERROR_MESSAGE);
					}

				}

			}

			open(); // opening a server connection again

			// send request to server
			if (start == 0) {
				send.put("type", "start");
				send.put("status", 0);
				send.put("name", input);
				start+=2;
			} else {
				send.put("type", "input");
			}



			if (start == 1) {
				gameStarted = true;
				send.put("status", 1);
			}

			if (start == 2) {
				gameStarted = false;
				if (!send.has("name")) {
					send.put("status", 2);
				}
			}
			send.put("input", input);
			System.out.println(send);
			byte[] sendThis = convert2Bytes(send);
			try {
				os.writeInt(sendThis.length);
				os.write(sendThis);
				os.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			outputPanel.clearInputText();
			String r;
			JSONObject res;
			JSONObject servReply;
			try {
				System.out.println("Waiting on response");
				int inLen = in.readInt();
				byte[] msg = new byte[inLen];
				in.readFully(msg, 0, msg.length);
				r = convertFromBytes(msg);
				System.out.println("got a response");

				outputPanel.clearInputText();
				res = new JSONObject(r);
				if (!res.has("data")) {
					System.out.println(res);
				}
				if (res.has("type") && res.getString("type").equals("new game")) {
					start = 1;
					gameStarted = true;
				}


				servReply = Response.evaluateResponse(res, picPanel, outputPanel);
				score = Response.getScore();

				if (servReply!=null && servReply.has("error")) {
					System.out.println("Error occurred. Improper protocol");
					JOptionPane.showMessageDialog(null, "An error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (servReply !=null) {
					System.out.println("auto reply to server");
					reply2Server(servReply);
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PicturePanel.InvalidCoordinateException e) {
				throw new RuntimeException(e);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

			// wait for an answer and handle accordingly
		/*try {
			System.out.println("Waiting on response");
			String i = (String) in.readUTF();
			JSONObject response = new JSONObject(i);
			System.out.println("Got a response:" + response);
			evaluateResponse(response);
			//System.out.println(string);
		} catch (Exception e) {
			e.printStackTrace();
		}

		 */
			close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//just in case
	public void reply2Server(JSONObject jo) {
		if (jo.has("input") && jo.getString("input").equals("quit2")) {
			close();
			System.exit(0);
		}

		if (jo.has("score")) {
			gameStarted = false;
			start=2;
		}
		byte[] sendThis = convert2Bytes(jo);
		try {
			os.writeInt(sendThis.length);
			os.write(sendThis);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		outputPanel.clearInputText();


		String r;
		JSONObject res;
		try {
			System.out.println("Waiting on response");
			int inLen = in.readInt();
			byte[] msg = new byte[inLen];
			in.readFully(msg, 0, msg.length);
			r = convertFromBytes(msg);
			System.out.println("got a response");
			outputPanel.clearText();
			res = new JSONObject(r);
			if (!res.has("data")) {
				System.out.println(res);
			}
			Response.evaluateResponse(res, picPanel, outputPanel);
			score = Response.getScore();

		} catch (IOException | PicturePanel.InvalidCoordinateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}


	}


	/**
	 * Key listener for the input text box
	 *
	 * Change the behavior to whatever you need
	 */
	@Override
	public void inputUpdated(String input) {
		if (input.equals("surprise")) {
			outputPanel.appendOutput("You found me!");
		}
	}

	public void open() throws UnknownHostException, IOException {
		this.sock = new Socket(host, port); // connect to host and socket

		// get output channel
		this.out = sock.getOutputStream();
		// create an object output writer (Java only)

		this.in = new DataInputStream(sock.getInputStream());

		this.os = new DataOutputStream(out);
		//this.bufferedReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));

	}

	public void close() {
		try {
			if (out != null)  out.close();
			if (bufferedReader != null)   bufferedReader.close();
			if (sock != null) sock.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		// create the frame



		try {
			File file = new File("resources/logo.png");
			BufferedImage img = ImageIO.read(file);
			logo = new ImageIcon(img);

			String host = args[0];
			int port = Integer.parseInt(args[1]);


			ClientGui main = new ClientGui(host, port);
			main.show(true);


		} catch (Exception e) {e.printStackTrace();}



	}
}
