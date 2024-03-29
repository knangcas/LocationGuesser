package Assign32starter;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import java.util.Stack;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import java.awt.image.BufferedImage;
import java.io.*;

import com.google.gson.Gson;
import org.json.*;


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

	static int caIndex;

	public static void main (String args[]) {
		currentAnswer = new String[]{"ASU", "Berlin", "Paris"};
		direction = 1;
		caIndex = 1;

		try {

			//opening the socket here, just hard coded since this is just a bas example
			ServerSocket serv = new ServerSocket(9000); // TODO, should not be hardcoded
			System.out.println("Server ready for connetion");

			// placeholder for the person who wants to play a game
			String name = "";
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
						writeOut(response);
						continue;
					}
					System.out.println("Got a request");
					System.out.println(s);
					JSONObject request = new JSONObject(s);


					response = testField(request, "type", null);
					if (!response.getBoolean("ok")) {    // no "type" header provided
						response = noType(request);
						writeOut(response);
						continue;
					}

					if (request.getString("type").equals("input")) {
						//TODO
						JSONObject test = testField(request, "input", "input");
						if (!test.getBoolean("ok")) {
							response = noType(request);
							writeOut(response);
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
							direction = 1;
							response = fetchImage(currentAnswer[caIndex], direction, response);
							System.out.println("Fetched new set of images. Answer: " + currentAnswer[caIndex]);
						} else if (input.equals("leaderboards")) {
							response.put("type", "leaderboards");
							//send leaderboard stuff
						} else if (input.equals("start")) {
							response.put("type", "new game");
						} else {
							//probably a guess.
							if (input.equals(currentAnswer[caIndex])) {
								response.put("type", "+1");
							} else {
								response.put("type", "wrong guess");
							}

						}


					} else if (request.getString("type").equals("start")) {
						if (!request.has("message")) {
							System.out.println("Got a start!");
							response.put("type", "start");
							response.put("message", "Hello, what is your name?");
							response.put("data", "");
							sendImg("img/hi.png", response);
						} else {
							name = request.getString("message");
							System.out.println(name);
							response.put("type", "message");
							response.put("message", "Hello " + name +" What would you like to do?");
						}
					} else if (request.getString("type").equals("Name")) {
						//todo
					} else {
						response = wrongType(request);
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
				res.put("message", "req not JSON");
				return res;
			}
		}
		return new JSONObject();
	}

	static void writeOut(JSONObject res) {
		try {
			os.writeUTF(res.toString());  //has to be JSON format.
			// make sure it wrote and doesn't get cached in a buffer
			os.flush();

		} catch(Exception e) {e.printStackTrace();}

	}

	static JSONObject testField(JSONObject req, String key, String type){
		JSONObject res = new JSONObject();
		if (type!=null) {
			res.put("type", type);
		}
		// field does not exist
		if (!req.has(key)){
			res.put("ok", false);
			res.put("message", "Field " + key + " does not exist in request");
			return res;
		}
		return res.put("ok", true);
	}

	static JSONObject noType(JSONObject req){
		System.out.println("No type request: " + req.toString());
		JSONObject res = new JSONObject();
		res.put("ok", false);
		res.put("message", "No request type was given.");
		return res;
	}

	static JSONObject wrongType(JSONObject req){
		System.out.println("Wrong type request: " + req.toString());
		JSONObject res = new JSONObject();
		res.put("ok", false);
		res.put("message", "Type " + req.getString("type") + " is not supported.");
		return res;
	}
}
