package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
public class Build_Peasant implements StripsAction{
	
	@Override
	public boolean preconditionsMet(GameState state) {
		//We can only build a peasant if we have less than 3 already and we have at least 400 gold
		return state.peasants.size() < 3 && state.amountGold >= 400;
	}

	@Override
	public GameState apply(GameState state) {
		//return the game state resulting from adding a peasant
		return state.addPeasant(this);
	}

	@Override
	public int getPeasantId() {
		return -1;
	}

}
