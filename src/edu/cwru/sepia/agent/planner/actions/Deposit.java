package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Peasant.Item;

public class Deposit implements StripsAction{
	Peasant peasant;
	
	public Deposit(Peasant p){
		peasant = p;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		return !peasant.hasNothing() && peasant.pos.isAdjacent(state.townhall.pos);
	}

	@Override
	public GameState apply(GameState state) {
		Peasant p = new Peasant(peasant.id, peasant.pos, Item.NOTHING);
		GameState toReturn = new GameState(state, peasant, p);
		
		switch(peasant.carrying){
			case WOOD:
				toReturn.townhall = toReturn.townhall.depositWood();
				break;
			case GOLD:
				toReturn.townhall = toReturn.townhall.depositGold();
				break;
			default:
				System.out.println("Error: unkown type carried by peasant.");
		}
		
		return toReturn;
	}
}
