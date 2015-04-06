package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
public class Build_Peasant implements StripsAction{
	
	@Override
	public boolean preconditionsMet(GameState state) {
		return state.peasants.size() < 3 && state.amountGold >= 400;
	}

	@Override
	public GameState apply(GameState state) {
		//System.out.println("Building peasant");
		return state.addPeasant(this);
	}

	@Override
	public int getPeasantId() {
		// TODO Auto-generated method stub
		return -1;
	}

}
