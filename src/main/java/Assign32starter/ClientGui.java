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


import javax.imageio.ImageIO;
import javax.swing.*;


public class ClientGui implements Assign32starter.OutputPanel.EventHandlers {
	JFrame frame;
	PicturePanel picPanel;
	OutputPanel outputPanel;
	String currentMess;



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
		frame.getContentPane().setBackground(Color.WHITE);
		frame.setLayout(new GridBagLayout());
		frame.setMinimumSize(new Dimension(600, 750));
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

		open();
		JSONObject request = new JSONObject();
		request.put("type", "start");
		request.put("status", 0);


		byte[] sendThis = convert2Bytes(request);
		os.writeInt(sendThis.length);
		os.write(sendThis);
		os.flush();


		int inLen = in.readInt();
		byte[] message = new byte[inLen];
		in.readFully(message,0,message.length);
		String i;
		i = convertFromBytes(message);

		JSONObject res = new JSONObject(i);
		System.out.println("Connection Successful");

		ImageIcon ii = Response.readImg(res);

		try {
			picPanel.insertImageI(0, 0, ii);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Got a connection to server");


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

	public void show(boolean makeModal) {
		frame.pack();
		//frame.setModal(makeModal);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}


	@Override
	public void submitClicked() {
		try {
			JSONObject send = new JSONObject();
			System.out.println("Submit clicked. Sending message");

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

			open();


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
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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

		} catch (IOException e) {
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

		try {
			File file = new File("resourcesClient/logo.png");
			BufferedImage img = ImageIO.read(file);
			logo = new ImageIcon(img);

			String host = args[0];
			int port = Integer.parseInt(args[1]);


			ClientGui main = new ClientGui(host, port);
			main.show(true);


		} catch (Exception e) {e.printStackTrace();}


	}
}
