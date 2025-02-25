package Assign32starter;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.util.Base64;
import java.util.Set;
import java.util.Stack;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.json.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * A class to demonstrate a simple client-server connection using sockets.
 * Ser321 Foundations of Distributed Software Systems
 */
public class SockServer {
	static Stack<String> imageSource = new Stack<String>();
	static Socket sock;
	static DataOutputStream os;
	static InputStream in;

	static int direction;

	static String[] currentAnswer;

	static RandomDataGenerator rdg;

	static int caIndex;

	static String score;

	static String name;

	static long startTime;

	static long timeChosen;

	static int port = 8080;

	//static boolean gameStarted;

	//static boolean previouslyPlayed;

	static int streak = 0;

	private static Map<String, String> leaderBoards = new Hashtable<>();

	public static void main (String args[]) {
		currentAnswer = new String[]{"ASU", "Berlin", "Paris"};
		direction = 1;
		rdg = new RandomDataGenerator();
		//gameStarted = false;
		//previouslyPlayed = false;

		caIndex = rdg.nextInt(0, currentAnswer.length-1);

		try {
			port = Integer.parseInt(args[0]);
			//opening the socket here, just hard coded since this is just a bas example
			ServerSocket serv = new ServerSocket(port);
			System.out.println("Success. Hosting on port " + port);
			System.out.println("Server ready for connection");

			// placeholder for the person who wants to play a game
			name = "";
			int points = 0;

			// read in one object, the message. we know a string was written only by knowing what the client sent. 
			// must cast the object from Object to desired type to be useful
			while (true) {
				System.out.println("Server waiting for a connection");
				sock = serv.accept(); // blocking wait
				System.out.println("Client connected");

				// could totally use other input outpur streams here
				OutputStream out = sock.getOutputStream();
				DataOutputStream dos = new DataOutputStream(out);
				System.out.println("after stream");

				// create an object output writer (Java only)
				//os = new DataOutputStream(out);

				in = sock.getInputStream();
				DataInputStream ds = new DataInputStream(in);

				//byte[] messageBytes = NetworkUtils.Receive(in);
				//String s = (String) in.readObject();
				//JSONObject json = new JSONObject(s); // the requests that is received
				//JSONObject json = JsonUtils.fromByteArray(messageBytes);


				boolean connected = true;

				while (connected) {


					String s = "";
					getLeaders();
					try {
						int inLen = ds.readInt();
						if(inLen >0) {
							byte[] message = new byte[inLen];
							ds.readFully(message, 0, message.length);
							s = convertFromBytes(message);

						}
						//s = (String) in.readObject();
					} catch (Exception e) {
						System.out.println("Client Disconnect");
						connected = false;
						continue;
					}


					JSONObject response = isValid(s);
					if (response.has("ok")) {      //if isValid gives "ok" key (indicating invalid JSON), skip the rest of the code, and iterate loop again (continue).
						writeOut(response, dos);
						continue;
					}
					System.out.println("Got a request");
					System.out.println(s);
					JSONObject request = new JSONObject(s);
					response = testField(request, "type", null);
					if (!response.getBoolean("ok")) {    // no "type" header provided
						response = noType(request);
						writeOut(response, dos);
						continue;
					}
					response = testField(request, "status", null);
					if (!response.getBoolean("ok")) {    // no "type" header provided
						response = noType(request);
						writeOut(response, dos);
						continue;
					}

					if (request.getString("type").equals("input") && request.getInt("status") == 1) {
						long deadline = startTime + timeChosen;
						if (System.currentTimeMillis() > deadline) {
							request.put("input", "gover!revog");
						}

						JSONObject test = testField(request, "input", "input");
						if (!test.getBoolean("ok")) {
							response = noType(request);
							writeOut(response, dos);
							continue;
						}

						String input = request.getString("input");

						if (input.equals("left")) {
							if (direction != 1) {
								response = fetchImage(currentAnswer[caIndex], direction - 1, response);
								direction--;
							} else {
								response = fetchImage(currentAnswer[caIndex], direction + 3, response);
								direction+=3;
							}
						} else if (input.equals("right")) {
							if (direction != 4) {
								response = fetchImage(currentAnswer[caIndex], direction + 1, response);
								direction++;
							} else {
								response = fetchImage(currentAnswer[caIndex], direction - 3, response);
								direction-=3;
							}
						} else if (input.equals("next")) {
							nextIndex();
							direction = rdg.nextInt(1,4);
							//caIndex = rdg.nextInt(1, currentAnswer.length-1);
							response = fetchImage(currentAnswer[caIndex], direction, response);
							System.out.println("Fetched new set of images. Answer: " + currentAnswer[caIndex]);
						} else if (input.equals("gover!revog")) {
							if (!request.has("score")) {
								System.out.println("Acknowledging Game Over");
								//response.put("message", "")
								//gameStarted = false;
								streak = 0;
								sendImg("img/gover.png", response);
								response.put("type", "ok");
								response.put("time", timeChosen/1000);
							}
						} else if (input.equals(currentAnswer[caIndex].toLowerCase())) {
							nextIndex();
							direction = rdg.nextInt(1,4);
							response = fetchImage(currentAnswer[caIndex], direction, response);
							response.put("type", "+1");
							streak++;
							int threshold = 5;
							if (streak > 5) {
								threshold = 10;
							}
							if (streak > 10) {
								threshold = 20;
							}
							if (streak == threshold) {
								response.put("streak", streak);
							}
							System.out.println("Fetched new set of images. Answer: " + currentAnswer[caIndex]);
						} else {
							streak = 0;
							response.put("type", "wrong guess");
							System.out.println(name + "answered incorrectly. Correct answer is " + currentAnswer[caIndex]);
						}

					} else if (request.getString("type").equals("input") && request.getInt("status") == 2) {
						String input = request.getString("input");

						if (input.equals("leaderboard")) {
							response.put("type", "leaderboards");
							JSONArray lbtop5 = getTop5lb();
							response.put("data", lbtop5);
							System.out.println("sending back top5 leaders");
							System.out.println(lbtop5);
							response.put("message", "false");
						} else if (input.equals("leaderboardsplash")) {
							response.put("type", "leaderboards");
							JSONArray lbtop5 = getTop5lb();
							response.put("data", lbtop5);
							System.out.println("sending back top5 leaders");
							System.out.println(lbtop5);
							response.put("message", "true");
						} else if (input.equals("leaderboard full")) {
							response.put("type", "leaderboardsFULL");
							JSONArray full = getAllScores();
							response.put("data", full);
							System.out.println("Sending full leaderboard");
							System.out.println(full);
						} else if (input.equals("start")) {
							streak = 0;
							//gameStarted = true;
							//previouslyPlayed = true;
							System.out.println("new game started");

							response = fetchImage(currentAnswer[caIndex], 1, response);
							response.put("type", "new game");
							response.put("time" , request.getInt("time"));
							timeChosen = ((long) request.getInt("time")) * 1000;
							startTime = System.currentTimeMillis();
						} else if (input.equals("quit")) {
							response.put("type", "quit");
						} else if (input.equals("quit2")) {
							System.out.println("quitting game and disconnecting client...");
						} else if (input.equals("gover!revog")) {
							//name = request.getString("name");
							System.out.println("Storing score");
							score = request.getString("score");
							double score2 = Double.parseDouble(score);
							if (leaderBoards.get(name) != null) {
								if (score2 > Double.parseDouble(leaderBoards.get(name))) {
									leaderBoards.put(name, String.valueOf(score2));
									writeJSON();
									System.out.println("Updated leaders.json with new high score");
								}
							} else {
								leaderBoards.put(name, String.valueOf(score));
								writeJSON();
								System.out.println("Updating leaders.json");
							}
							//previouslyPlayed = true;
							response.put("type", "start2");
							response.put("message", "Thanks for playing. Select an option");

						} else {
							response.put("type", "notplayingCommandsIntro");
						}


					} else if (request.getString("type").equals("start") && request.getInt("status") == 0) {
						if (!request.has("name")) {
							System.out.println("Got a start!");
							response.put("type", "start");
							response.put("message", "Hello, what is your name?");
							response.put("data", "");
							sendImg("img/hi.png", response);
							response.put("type", "start");
						} else {
							name = request.getString("name");
							System.out.println("Player name: " + name);
							response.put("type", "start");
							response.put("message", "Hello " + name + ", What would you like to do?");
							//if (!leaderBoards.containsKey(name)) {
							//leaderBoards.put(name, String.valueOf(0));
							//}
						}
					}  else {
						response = wrongType(request);
					}

					//if (previouslyPlayed) {
						//gameStarted = false;
					//}

					System.out.println("Sending response");
					if (!response.has("data")) {
						System.out.println(response);
					} else {
						System.out.println("sent response, data too big (image) to print to console");
					}
					byte[] msg2send = convert2Bytes(response);
					dos.writeInt(msg2send.length);
					dos.write(msg2send);
					dos.flush();
					//writeOut(response);

				}

				try {
					dos.close();
					ds.close();
					sock.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			try {
				os.close();
				in.close();
				sock.close();
			} catch (Exception e2) {
				e.printStackTrace();
			}
		}




	}


/*

				if (json.getInt("selected") == 3){
					
					System.out.println("- Got a start");

					response.put("datatype", 1);
					response.put("type", "quote");
					response.put("value","Hello, please tell me your name." );
					//sendImg("img/hi.png", response); // calling a method that will manipulate the image and will make it send ready
					
				}
				else {
					System.out.println("not sure what you meant");
					response.put("type","error" );
					response.put("message","unknown response" );
				}
				//PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true); // using a PrintWriter here, you could also use and ObjectOutputStream or anything you fancy
				//outWrite.println(response.toString());

				byte[] output = JsonUtils.toByteArray(response);
				NetworkUtils.Send(out, output);
			}
			
		} catch(Exception e) {e.printStackTrace();}
	}

 */

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

	private static JSONObject fetchImage(String answer, int num, JSONObject jo) throws Exception {
		jo.put("type", "image");

		String n = String.valueOf(num);
		answer = answer+n;
		String path = "img/" + answer + ".png";
		System.out.println("Image fetched. Path: " + path);
		return sendImg(path, jo);

	}

	//TODO this is for you to implement, I just put a place holder here */
	public static JSONObject sendImg(String filename, JSONObject obj) throws Exception {
		obj.put("type", "image");
		File file = new File(filename);
		byte[] bytes = null;
		//BufferedImage img = ImageIO.read(file);
		if (file.exists()) {
			// import image
			// I did not use the Advanced Custom protocol
			// I read in the image and translated it into basically into a string and send it back to the client where I then decoded again
			obj.put("image", "Pretend I am this image: " + filename);
		} else {
			System.err.println("Cannot find file: " + file.getAbsolutePath());
			System.exit(-1);
		}

		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			BufferedImage img = ImageIO.read(file);
			ImageIO.write(img, "png", out);
			bytes = out.toByteArray();
		}
		if (bytes != null) {
			Base64.Encoder encoder = Base64.getEncoder();
			obj.put("data", encoder.encodeToString(bytes));
			return obj;
		}
		return error("can't send image");
	}

	private static void nextIndex(){
		caIndex++;

		if (caIndex == currentAnswer.length) {
			caIndex = 0;
		}

	}



	public static JSONObject error(String err) {
		JSONObject json = new JSONObject();
		json.put("error", err);
		return json;
	}

	public static JSONObject isValid(String json) {
		try {
			new JSONObject(json);
		} catch (JSONException e) {
			try {
				new JSONArray(json);
			} catch (JSONException ne) {
				JSONObject res = new JSONObject();
				res.put("ok", false);
				res.put("message", "req not JSONObject. Please see documentation");
				return res;
			}
		}
		return new JSONObject();
	}

	private static void writeOut(JSONObject response, DataOutputStream dos) {
		System.out.println("Sending response");
		if (!response.has("data")) {
			System.out.println(response);
		} else {
			System.out.println("sent response, data too big (image) to print to console");
		}
		try {
			byte[] msg2send = convert2Bytes(response);
			dos.writeInt(msg2send.length);
			dos.write(msg2send);
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static JSONObject testField(JSONObject req, String key, String type){
		JSONObject res = new JSONObject();
		if (type!=null) {
			res.put("type", type);
		}
		// field does not exist
		if (!req.has(key)){
			res.put("ok", false);
			res.put("message", "Field " + key + " does not exist in request. Please see documentation for all valid keys");
			return res;
		}
		return res.put("ok", true);
	}

	static JSONObject noType(JSONObject req){
		System.out.println("No type request: " + req.toString());
		JSONObject res = new JSONObject();
		res.put("ok", false);
		res.put("message", "JSON must include \"type\", <String>");
		return res;
	}

	static JSONObject wrongType(JSONObject req){
		System.out.println("Wrong type request: " + req.toString());
		JSONObject res = new JSONObject();
		res.put("ok", false);
		res.put("message", "Type " + req.getString("type") + " is not supported.");
		return res;
	}

	static void getLeaders() {
		ObjectMapper mapper = new ObjectMapper();

		File fileObj = new File("resourcesServer/leaders.json");
		if (fileObj == null) {
			System.exit(1);
		}
		List<Player> players;
		try {
			players = mapper.readValue(fileObj, new TypeReference<List<Player>>() {
			});
			for (Player i : players) {
				leaderBoards.put(i.getName(), i.getScore());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static JSONArray returnArray() {
		JSONArray playerList = new JSONArray();
		int i = 0;
		for (String player : leaderBoards.keySet()){
			JSONObject object = new JSONObject();
			object.put("name", player);
			object.put("score", Double.parseDouble(leaderBoards.get(player)));
			playerList.put(i, object);
			i++;
		}
		return playerList;
	}

	static void writeJSON() {

		try {
			FileWriter file = new FileWriter("resourcesServer/leaders.json");
			file.write(returnArray().toString());
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static JSONArray getAllScores() {

		Map<String, Double> temp = new Hashtable<>();
		for (String player : leaderBoards.keySet()) {
			temp.put(player, Double.parseDouble(leaderBoards.get(player)));
		}
		List<String> sorted = temp.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
		revlist(sorted);
		JSONArray allScores = new JSONArray();

		for (int i = 0; i < sorted.size(); i++) {
			JSONObject p = new JSONObject();
			String name = sorted.get(i);
			double score = Double.parseDouble(leaderBoards.get(name));
			System.out.println("putting " + leaderBoards.get(name) + "into jsonarray");
			p.put("name", name);
			p.put("score", score);
			p.put("rank", i + 1);
			allScores.put(p);
		}
		return allScores;


	}

	static JSONArray getTop5lb() {
		int highest = 0;
		int counter = 5;

		//double[] scores = new double[leaderBoards.size()];
		//String[] names = new String[leaderBoards.size()];



		Map<String, Double> temp = new Hashtable<>();
		for (String player : leaderBoards.keySet()) {
			temp.put(player, Double.parseDouble(leaderBoards.get(player)));
		}

		List<String> sorted = temp.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());

		revlist(sorted);


		JSONArray top5 = new JSONArray();
		int counter2 = 0;
		int rank = 1;
		int sortedLen = sorted.size();
		for (int i = 0; i < 5; i++) {
			JSONObject p = new JSONObject();
			if (counter2 < sortedLen) {
				String name = sorted.get(i);
				double score = Double.parseDouble(leaderBoards.get(name));
				p.put("name", name);
				p.put("score", score);
				p.put("rank", rank);
				rank++;
				top5.put(p);
				counter2++;
			} else {
				p.put("name", "PLAYER");
				p.put("score", 0);
				p.put("rank", rank);
				rank++;
				top5.put(p);
			}

		}

		return top5;

	}


	public static <String> void revlist(List<String> list) {
		if (list.size() <=1 || list == null) {
			return;
		}

		String value = list.remove(0);

		revlist(list);

		list.add(value);
	}

}
