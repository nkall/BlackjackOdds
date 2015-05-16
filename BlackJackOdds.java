package blackjackoddspackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;


public class BlackJackOdds {
	public static void main(String[] args) throws IOException, InterruptedException
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		int games = 0, wins = 0, busts = 0, beats = 0;
		int decks;
		if (args.length != 1) decks = 1;
		else decks = Integer.parseInt(args[0]);
		CardDeck deck = new CardDeck(decks);

		
	    while (true){
	    	games++;
	    	deck = drawComp(deck);
	    	deck = drawHuman(deck);		
	    	computeOdds(deck);
	    	int cardNum = 2;
	    	char choice;
	    	boolean isBust = false;

	    	while ((choice = humanChoice(in)) == 'h'){
	    		deck = humanHit(deck, cardNum);
	    		//if (deck.humanHasBigAce) System.out.println("Has big ace.");
	    		if (deck.humanValue > 21){
	    			System.out.println("You bust!");
					isBust = true;
					busts++;
					break;
	    		}
	    		cardNum++;
	    		computeOdds(deck);
	    	}
	    	if (choice == 'q'){
	    		in.close();
	    		break;
	    	}
	    	if (!isBust){
	    		deck = compMove(deck);
	    		if (deck.compValue > 21){
	    			System.out.println("Computer busts. You win!");
	    			isBust = true;
	    			wins++;
	    		}
	    	}
	    	
	    	if (!isBust) {
	    		System.out.println("Computer stays.");
		
	    		if (deck.humanValue > deck.compValue){
	    			System.out.println("Your "+deck.humanValue+" beats computer's "
								+deck.compValue+". You win!");
	    			wins++;
	    		} else {
	    			System.out.println("Computer's "+deck.compValue+" beats your " +
	    				deck.humanValue + ". You lose!");
	    			beats++;
	    		}
	    	}
		    Thread.sleep(1000);
	    System.out.println();
	    System.out.println();
	    if (deck.totalCards < 13){
	    	float ratio = (float) wins / games;
	    	System.out.println("Not enough cards for another game, sorry!");
	    	System.out.print("You won "+wins+" games out of "+games+" (Ratio: ");
	    	System.out.format("%.3f).\n", ratio);
	    	System.out.println("You lost "+busts+" games by busting and "+beats+" games by being beaten.");
	    	break;
	    }
	    System.out.println("Dealing... (" + deck.totalCards + " cards left)");
		deck.resetDeck();
	    }
	}

	public static CardDeck compMove (CardDeck deck){
		int cardNum = 2;
		if (deck.compValue < 11) deck.compValue += cardValue(deck.compHiddenCard, 11);
		else deck.compValue += cardValue(deck.compHiddenCard, 1);
		
		while (deck.compValue < 17){
			System.out.print("Comp Hand: ");
			for (int i = 0; i < cardNum; i++){
				printCardType(deck.compHand[i]);
			}
			System.out.println("(Value: " + deck.compValue+")");

			try {
			    Thread.sleep(1000);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			
			System.out.println("Computer hits.");
			
			deck.compHand[cardNum] = deck.pickRandCard();
			deck.remCard(deck.compHand[cardNum]);
			
			if (deck.compValue < 11){
				deck.compValue += cardValue(deck.compHand[cardNum], 11);
				if (deck.compHand[cardNum] == 1) deck.compHasBigAce = true;
			} else deck.compValue += cardValue(deck.compHand[cardNum], 1);
			
			if (deck.compValue > 21 && deck.compHasBigAce){
				deck.compValue -= 10;
				deck.compHasBigAce = false;
			}
			cardNum++;
		}
		System.out.print("Comp Hand: ");
		for (int i = 0; i < cardNum; i++){
			printCardType(deck.compHand[i]);
		}
		System.out.println("(Value: " + deck.compValue+")");

		return deck;
	}
	
	public static CardDeck humanHit(CardDeck deck, int cardNum){
		deck.humanHand[cardNum] = deck.pickRandCard();
		deck.remCard(deck.humanHand[cardNum]);

		System.out.print("Your Hand: ");
		for (int i = 0; i <= cardNum; i++){
			printCardType(deck.humanHand[i]);
		}

		if (deck.humanValue < 11){ 
			deck.humanValue += cardValue(deck.humanHand[cardNum], 11);
			if (deck.humanHand[cardNum] == 1) deck.humanHasBigAce = true;
		} else deck.humanValue += cardValue(deck.humanHand[cardNum], 1);
		
		if (deck.humanValue > 21 && deck.humanHasBigAce){
			deck.humanValue -= 10;
			deck.humanHasBigAce = false;
		}
		System.out.println("(Value: " + deck.humanValue+")");
		
		return deck;
	}
	
	public static char humanChoice(BufferedReader in){
		char c = 'a';
		try {
		    String s;

	    	while(true){
    			System.out.println("Hit or stay?");
	    		if ((s = in.readLine()) != null && s.length() != 0 &&
	    				((c = s.charAt(0)) == 's'|| c == 'h' || c == 'S' || c == 'H'
	    				|| c == 'q' || c == 'Q')){
	    			break;
				}
	    	}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (c == 'Q' || c == 'q') return 'q';
	    if (c == 'H' || c == 'h') return 'h';
	    return 's';
	}
	
	public static void computeOdds(CardDeck deck){
		int trials = 10000000;
		deck.addHiddenCard();
		
		System.out.print("Odds you will bust if you hit:  ");
		computeBust(deck, trials);
		System.out.print("Odds your hand is bigger:       ");
		computeBigger(deck, trials);
		
		deck.remCard(deck.compHiddenCard);
	}
	
	public static void computeBigger(CardDeck deck, int trials){
		int biggers = 0;
		int aceVal = 11;
		int visibleDiff = deck.humanValue - deck.compValue;
		if ((21 - deck.compValue) < 11){
			aceVal = 1; 
		}
		for (int i = 0; i < trials; i++){
			if(cardValue(deck.pickRandCard(),aceVal)<visibleDiff) biggers++;
		}
		float odds = (float) biggers / trials;
		System.out.format("%.3f\n", odds);
	}
	
	public static void computeBust(CardDeck deck, int trials){
		int busts = 0;
		int breathingRoom = 21 - deck.humanValue;
		if (deck.humanHasBigAce) breathingRoom += 10;
		//System.out.println(breathingRoom);
		for (int i = 0; i < trials; i++){
			if(cardValue(deck.pickRandCard(), 1) > breathingRoom) busts++;
		}
		float odds = (float) busts / trials;
		System.out.format("%.3f\n", odds);
	}
	
	public static CardDeck drawComp(CardDeck deck){
		System.out.print("Comp Hand: ");
		//Draw and display first card
		deck.compHand[0] = deck.pickRandCard();
		System.out.print("X ");
		if (deck.compHand[0] == 1) deck.compHasBigAce = true;
		deck.compHiddenCard = deck.compHand[0];
		deck.remCard(deck.compHiddenCard);
		
		deck.compHand[1] = deck.pickRandCard();
		printCardType(deck.compHand[1]);
		deck.remCard(deck.compHand[1]);
		if (deck.compValue < 11){
			deck.compValue += cardValue(deck.compHand[1], 11);
			if (deck.compHand[1] == 1) deck.compHasBigAce = true;
		}
		else deck.compValue += cardValue(deck.compHand[1], 1);
	
		System.out.println("(Value: " + deck.compValue+")");
		
		return deck;
	}
	
	public static CardDeck drawHuman(CardDeck deck){
		System.out.print("Your Hand: ");
		//Draw and display first card
		deck.humanHand[0] = deck.pickRandCard();
		printCardType(deck.humanHand[0]);
		deck.remCard(deck.humanHand[0]);
		if (deck.humanHand[0] == 1) deck.humanHasBigAce = true;
		deck.humanValue += cardValue(deck.humanHand[0], 11);
		
		deck.humanHand[1] = deck.pickRandCard();
		printCardType(deck.humanHand[1]);
		deck.remCard(deck.humanHand[1]);
		if (deck.humanValue < 11){
			deck.humanValue += cardValue(deck.humanHand[1], 11);
			if (deck.humanHand[1] == 1) deck.humanHasBigAce = true;
		}
		else deck.humanValue += cardValue(deck.humanHand[1], 1);
		
		System.out.println("(Value: " + deck.humanValue+")");
		
		return deck;
	}
	
	//Returns point value of a given card (king = 10, 9 = 9, etc)
	public static int cardValue (int type, int aceVal){
		if (type == 1) return aceVal;
		if (type < 10) return type;
		return 10;
	}

	public static void printCardType(int type){
		switch (type){
		case 1:
			System.out.print("A ");
			break;
		case 11:
			System.out.print("J ");
			break;
		case 12:
			System.out.print("Q ");
			break;
		case 13:
			System.out.print("K ");
			break;
		default:
			System.out.print(type + " ");
			break;
		}
	}
	
	static class CardDeck
	{
		public int totalCards;
		private int[] cardType = new int[14]; //Up to 13
		public boolean[] isEmpty = new boolean[14];
		
		public int[]humanHand = new int[12];
		public int[]compHand = new int[12];
		
		public int humanValue;
		public boolean humanHasBigAce;
		public boolean compHasBigAce;

		public int compValue;
		public int compHiddenCard;
		
		//Chooses, but does not remove from deck, a random card
		public int pickRandCard(){
			int i;
			int cardNum;
			int currCardNum;
			while (true){
				currCardNum = 0;
				cardNum = 1 + (int) (Math.random() * totalCards);
				for(i = 1; i<14; i++){
					if(currCardNum >= cardNum){
						break;
					}
					currCardNum += cardType[i];
				}
				i--;
				if (!isEmpty[i]){
					break;
				}
				//else System.out.println(cardNum + " " +i);
			}	
			//System.out.println(cardNum + " is type" + i);
			return i;
		}
		
		//Removes specified card from deck, changing odds calc
		public void remCard(int type){
			if (cardType[type] > 0) {
				totalCards--;
				cardType[type]--;
				if (cardType[type] == 0){
					isEmpty[type] = true;
					//System.out.print("Out of " + type + " cards.");
				}
			} else {
				System.out.println("ERROR: Deck is already empty of "+type+"cards.");
			}
		}
		
		public void addHiddenCard(){
			cardType[compHiddenCard]++;
			totalCards++;
		}
		public void resetDeck(){
			humanValue = 0;
			humanHasBigAce = false;
			compHasBigAce = false;
			compValue = 0;
			compHiddenCard = 0;
		}
		
		public CardDeck(int decks){
			totalCards = 52 * decks;
			Arrays.fill(cardType, 4 * decks);
			Arrays.fill(isEmpty, false);
			humanValue = 0;
			humanHasBigAce = false;
			compHasBigAce = false;
			compValue = 0;
			compHiddenCard = 0;
		}
	}
}