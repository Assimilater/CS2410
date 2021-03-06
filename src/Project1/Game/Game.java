package Project1.Game;

import Project1.GUI.CardButton;
import Project1.GUI.GamePanel;
import Project1.GUI.MainFrame;
import Project1.Game.Modifiers.GameSize;
import Project1.Game.Modifiers.Options;
import Project1.Game.Modifiers.Player;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Game {
	public int BoardSize;
	public int ConsecutiveRun;
	public boolean TimerRunning;
	public CardButton[][] Board;
	public CardButton FlippedCard;
	public Player Player1, Player2, thisTurn;
	
	// Statically track the current game
	private static Game instance;
	public static Game get() { return instance; }
	
	public Game() {
		// Close any previous game
		if (instance != null) {
			instance.close();
		}
		
		// Set the instance variable
		instance = this;
		
		BoardSize = Options.getGameSize();
		Player1 = Options.Player1;
		Player2 = Options.Player2;
		thisTurn = Player1;
		TimerRunning = false;
		ConsecutiveRun = 0;
		
		// Reset the scores and turns of each player
		Player1.newGame();
		Player2.newGame();
		
		// Update the labels on the GamePanel
		GamePanel.get().updateLabels();
		
		Board = new CardButton[BoardSize][BoardSize];
		LinkedList<CardButton> Pool = new LinkedList<CardButton>();
		LinkedList<Integer> Considered = new LinkedList<Integer>();
		
		// Every board should have two creepers :)
		Pool.add(new CardButton(Card.getByID(Card.CREEPER)));
		Pool.add(new CardButton(Card.getByID(Card.CREEPER)));
		Considered.add(Card.CREEPER);
		
		// If the board is big, add Herobrine ;)
		if (BoardSize > Options.toInt(GameSize.C6)) {
			Considered.add(Card.HEROBRINE);
			Pool.add(new CardButton(Card.getByID(Card.HEROBRINE)));
			
			// If the board is bigger, add another Herobrine ;)
			if (BoardSize == Options.toInt(GameSize.C8)) {
				Pool.add(new CardButton(Card.getByID(Card.HEROBRINE)));
			}
		}
		
		// Fill the rest of the pool randomly
		Random rand = new Random(); int randInt;
		while (Pool.size() < Math.pow(BoardSize, 2)) {
			// Randomized range of size-1 because this doesn't include Herobrine
			// Helps limit excess loops, even if just a little :)
			randInt = rand.nextInt(Card.Collection.size() - 1);
			if (!Considered.contains(randInt)) {
				Pool.add(new CardButton(Card.getByID(randInt)));
				Pool.add(new CardButton(Card.getByID(randInt)));
				Considered.add(randInt);
			}
		}
		
		// Shuffle the pool in
		shuffle(Pool);
		
		// Tell the GamePanel to update
		GamePanel.get().newGame();
		
		// Play morning-time music :)
		Card.Clips.get("Start").play();
	}
	
	// A publicly accessible method that scrambles the board
	public void shuffle() {
		// Return a pool with remaining cards on the field
		LinkedList<CardButton> Pool = new LinkedList<CardButton>();
		for (int i = 0; i < BoardSize; ++i) {
			for (int j = 0; j < BoardSize; ++j) {
				Pool.add(Board[i][j]);
				Board[i][j] = null;
			}
		}
		shuffle(Pool);
	}
	
	// Given a pool of cards randomly assign them to the board
	private void shuffle(LinkedList<CardButton> Pool) {
		// Randomly assign out the pool to the board
		Random rand = new Random(); int randInt;
		for (int i = 0; i < BoardSize; ++i) {
			for (int j = 0; j < BoardSize; ++j) {
				randInt = rand.nextInt(Pool.size());
				Board[i][j] = Pool.get(randInt);
				Pool.remove(randInt);
			}
		}
		
		GamePanel.get().drawBoard();
		
		// For debugging purposes I'll cheat ;)
		System.out.println("\n\n\n\n~~~~~Shuffle~~~~~\n\n");
		for (int i = 0; i < BoardSize; ++i) {
			for (int j = 0; j < BoardSize; ++j) {
				System.out.println(
					"Column: " + (j + 1) + "\t\t" +
					"Row: " + (i + 1) + "\t\t" +
					"Image: " + (Board[i][j].getCard() == null ? "null" : Board[i][j].getCard().getImageName())
				);
			}
		}
	}
	
	// A way to check if the game is over
	public boolean gameOver() {
		int cHerobrine = 0, cTotal = 0;
		for (int i = 0; i < BoardSize; ++i) {
			for (int j = 0; j < BoardSize; ++j) {
				if (Board[i][j].getCard() != null) {
					++cTotal;
					if (Board[i][j].getCard().getID() == Card.HEROBRINE) {
						++cHerobrine;
					}
				}
			}
		}
		if (cTotal - cHerobrine == 0 ) {
			// Award the winner the game and exit
			Player victor =
				Player1.getScore() > Player2.getScore()
				? Player1
				: Player2;
			
			victor.giveWin();
			GamePanel.get().updateLabels();
			
			// Play victory sound (eat for a second then burp) :)
			try {
				Card.Clips.get("Eat").loop();
				TimeUnit.SECONDS.sleep(1);
				Card.Clips.get("Eat").stop();
			} catch (InterruptedException ie) {
				Card.Clips.get("Eat").stop();
			}
			Card.Clips.get("Burp").play();
			
			// Show Congratulatory Message
			JOptionPane.showMessageDialog(
				MainFrame.get(),
				"Congratulations " + victor.getName() + "!",
				"You Won",
				JOptionPane.INFORMATION_MESSAGE
			);
			
			// Set ActiveGame to null
			GamePanel.get().clearBoard();
			this.close();
			
			// Signal that the game has ended
			return true;
		}
		return false;
	}
	
	// Quit the game, either when finished, closing, or creating a new game
	public void close() {
		instance = null;
	}
}
