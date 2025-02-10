

# Name The Place 2024

### Disclaimer

This is a class project for distributed systems. 

## Description (a)

This project is a location guessing game. The user will be given
a POV image on the screen, and the user can type "left", "right" to turn 90 degrees
left or right as if they are in that exact spot. The user may also guess the location by typing in the location
or simply skip by typing "next".

## Screenshots

![Screenshot1](https://github.com/knangcas/LocationGuesser/blob/main/Screenshots/ss7.png?raw=true)

![Screenshot2](https://github.com/knangcas/LocationGuesser/blob/main/Screenshots/ss6.png?raw=true)

![Screenshot3](https://github.com/knangcas/LocationGuesser/blob/main/Screenshots/ss5.png?raw=true)

![Screenshot4](https://github.com/knangcas/LocationGuesser/blob/main/Screenshots/ss3.png?raw=true)

![Screenshot5](https://github.com/knangcas/LocationGuesser/blob/main/Screenshots/ss2.png?raw=true)

![Screenshot6](https://github.com/knangcas/LocationGuesser/blob/main/Screenshots/ss1.png?raw=true)


### How to run (c)

1. Run server.

```
gradle runServer -Pport=port
```

2. Run client.
```
gradle runClient -Phost=host -Pport=port
```
3. If connection is made, splash will appear asking for user's name with a few options (leaderboard, quit)
- note : leaderboard in splash only shows top 5. Full leaderboard available in game. 
4. User enter's name, and press "Submit"
5. User is greeted with a few options. 
- Start: Start's the game. User will be prompted with how many seconds they would like to play. Minimum = 5;
- Leaderboard: Shows the top 5 user scores. If 5 do not exist, placeholders are put with name PLAYER and score 0.
- Leaderboard Full: Shows the full leaderboard in text area. 
- Quit: disconnects from server and terminates game. 

6. Once a game is started, user is able give the following commands:
- left: move left 90 degrees for a view to your left.
- right: move right 90 degrees for a view to your right.
- next: skip the current location.
- All other inputs will be considered a guess.

7. If a guess is correct, 1 point is given. If a guess is given after timer has ran out on server side, server does not accept request (even if it's correct) and ends game.
8. Game over splash screen is shown, and user is able to give the original commands again (Start, Leaderboard, Leaderboard Full, Quit).
9. Leaderboards are updated.


## Protocol (d)

Protocol is sent with JSONObjects. 
JSONObjects should be converted to bytes, then sent/recieved via DataOutputStream/DataInputStream.
Server will convert the bytes back to a JSONObject. 

### General Request

```
{ 
  "type" : <string>,
  "input" : <string>,
  "status" : <int>
}

```
The status will determine which branch the request will go to. 0 = initial start, 1 = playing game, 2 = menu
### Initial Connection
```
{ 
  "type" : "start",
  "status" : 0 
 }
```

### Name Request
```
{ 
  "type" : "start",
  "name" : <string>,
  "status" : 0
}
```

### Start Game Request
```
{ 
  "type" : "input",
  "input" : "start",
  "status" : 2
}
```
### Leaderboard Request (Top 5)
```
{ 
  "type" : "input",
  "input" : "leaderboard",
  "status" : 2
}
```

### Leaderboard Request (Full)
```
{ 
  "type" : "input",
  "input" : "leaderboard full",
  "status" : 2
}
```

### Quit
```
{ 
  "type" : "input",
  "input" : "quit",
  "status" : 2
}
```

### Left / Right / Next
Replace value for "input" with "left", "right", or "next"
```
{ 
  "type" : "input",
  "input" : "left", 
  "status" : 1
}
```

### User Guess
Replace value for "input" with any input. This will be considered a guess. 
```
{ 
  "type" : "input",
  "input" : <String>, 
  "status" : 1
}
```

### GameOver
```
{
   "type" : "input",
   "input" : "gover!revog"
   "status" : 1
}
```

### General Return Messages

```
{
   "type" : <string>,
   "message" : <string>
}
```

### Receiving an image

```
{
   "type" : "image",
   "data" : <String>
}
```

### Receiving leaderboards

```
{
   "type" : "leaderboards",
   "data" : JSONArray
   "message" : <String>
}
```

### Receiving full leaderboard

```
{
   "type" : "leaderboardsFULL",
   "data" : JSONArray
}
```

### Correct Guess Response

```
{
   "type" : "+1",
   "streak" : <int>
   "data" : <String>
}
```

### Incorrect Guess Response

```
{
   "type" : "wrong guess",
}
```

### Error message (NOT JSON)
```
{
   "ok" : false,
   "message" : "request not JSONObject. Please see documentation"
}
```

### Error message (Unrecognized Key)
```
{
   "ok" : false,
   "message" :  "Field " + key + " does not exist in request. Please see documentation for all valid keys"
}
```

### Error message (Type not in key)
```
{
   "ok" : false,
   "message" : "No request type was given. JSON must include "type", <String>" "
}
```

### Error message (Unrecognized type)
```
{
   "ok" : false,
   "message" :  "Type " + givenType + " is not supported."
}
```

### Generic Error message

```
{
   "error" : <String>
 
}
```

## Robust (e)
The protocol here is very simple, yet robust. Requests will always have a "type". 
if no TYPE or STATUS is included within the request, a response will be sent back giving a proper message saying what it is missing.
For example...
- If the request is not a JSONObject, a return message will return explain the request must be a JSONObject.
- If no type field is given, a message will return explaining that the request must have a type
- If a unrecognized type is given, a message will return saying the value is not recognized. 


## UDP? (f)
Since UDPs do not guarantee delivery, there may have to be a cache of "backup" images if a response is not
sent within a certain amount of time back to the client. The information when it comes to the message will stay the same
for the most part. There can either be a backup cache, error handling (no response) or both. 


## Requirements Met (b)
From the requirements section in the document. 7.x refers to the bullet point. 
- [x] 1
- [x] 2
- [x] 3
- [x] 4
- [x] 5 
- [x] 6
- [x] 7
- [x] 7.1
- [x] 7.2
- [x] 7.3 
- [x] 7.4
- [x] 7.5
- [x] 7.6
- [x] 8
- [x] 9
- [x] 10
- [x] 11
- [x] 12





