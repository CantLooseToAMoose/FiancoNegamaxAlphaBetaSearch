# FiancoNegamaxAlphaBetaSearch

## How to use the Engine

To start the Game Engine simply run the Fianco.GameEngine.Fianco.GameEngine Class. If you want to connect with an AIClient run the Fianco.ServerStructure.GameClient
Class. When you start the Fianco.GameEngine.Fianco.GameEngine Class the game is not already running so
you will have to press the Start/Restart Button. When you do this the game waits for Moves to be made by either a human
Player through the GUI or through a connected Client.
You can also undo moves using the Undo Button, but the Game pauses when you do it and you will have to hit the Continue
button to continue.

## Developers

If you want to utilize this Engine for your own Fianco.AI and develop using Java you can simply let your Fianco.AI implement the
Fianco.AI.Agent.IAgent Interface and inject it into a Fianco.ServerStructure.GameClient Object.
When you do so the generateMove method, has to take the current BoardState represented as a 2D integer Array as an
Argument and return the BoardState after the Fianco.AI's move has been made.

### Note:

It is also possible to use different Programming Languages. You only need to connect to the Server using a TCP
connection, send a Message containing your PlayerID (either "player1" or "player2").
After Connection, the Server will ask you to generate a Move by sending you a Message with the following pattern: 

`"Your Move:[[1, 1, 1, 1, 1, 1, 1, 1, 1][0, 1, 0, 0, 0, 0, 0, 1, 0][0, 0, 1, 0, 0, 0, 1, 0, 0][0, 0, 0, 1, 0, 1, 0, 0, 0][0, 0, 0, 0, 0, 0, 0, 0, 0][0, 0, 0, 2, 0, 2, 0, 0, 0][0, 0, 2, 0, 0, 0, 2, 0, 0][0, 2, 0, 0, 0, 0, 0, 2, 0][2, 2, 2, 2, 2, 2, 2, 2, 2]]"`

To which the Client will answer with the new Boardstate in the same Format but without the "Your Move:" Prefix
