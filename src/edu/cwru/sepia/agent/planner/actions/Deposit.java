package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Peasant.Item;
import edu.cwru.sepia.agent.planner.Townhall;

public class Deposit implements StripsAction{
	Peasant peasant;
	Townhall townhall;
	
	public Deposit(Peasant p, Townhall t){
		peasant = p;
		townhall = t;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		return !peasant.hasNothing() && peasant.isAtTownhall == townhall.id;
	}

	@Override
	public GameState apply(GameState state) {
		Peasant p = new Peasant(peasant.id, Item.NOTHING, -1, townhall.id);
		GameState toReturn = new GameState(state, peasant, p, this);
		
		switch(peasant.carrying){
			case WOOD:
				toReturn.townhall = townhall.depositWood();
				toReturn.amountWood += 100;
				break;
			case GOLD:
				toReturn.townhall = townhall.depositGold();
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
