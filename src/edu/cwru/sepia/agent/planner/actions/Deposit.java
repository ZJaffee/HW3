package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Peasant.Item;
import edu.cwru.sepia.agent.planner.Townhall;

//Deposit stripsAction
public class Deposit implements StripsAction{
	Peasant peasant;
	Townhall townhall;
	
	//We will try to deposit from Peasant p to Townhall t
	public Deposit(Peasant p, Townhall t){
		peasant = p;
		townhall = t;
	}

	//Check if we can deposit
	@Override
	public boolean preconditionsMet(GameState state) {
		//The peasant must be holding something, and be at the townhall
		return !peasant.hasNothing() && peasant.isAtTownhall == townhall.id;
	}

	//Returns the resulting game state
	@Override
	public GameState apply(GameState state) {
		//The new peasant is holding nothing
		Peasant p = new Peasant(peasant.id, Item.NOTHING, -1, townhall.id);
		//The new game state will be the same but will 100 more of the resource the peasant deposits
		GameState toReturn = new GameState(state, peasant, p, this, 1);
		switch(peasant.carrying){
			case WOOD:
				toReturn.amountWood += 100;
				break;
			case GOLD:
				toReturn.amountGold += 100;
				break;
			default:
				System.out.println("Error: unkown type carried by peasant.");
		}
		
		return toReturn;
	}
	
	@Override
	public int getPeasantId(){
		return peasant.id;
	}
}
