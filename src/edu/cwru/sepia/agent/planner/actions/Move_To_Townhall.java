package edu.cwru.sepia.agent.planner.actions;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.agent.planner.Resource;
import edu.cwru.sepia.util.Direction;


public class Move_To_Townhall implements StripsAction{
	public final Peasant peasant;
	public final int townhall;
	
	public Move_To_Townhall(Peasant p, int townhallId){
		peasant = p;
		townhall = townhallId;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		return (peasant.isAtTownhall != townhall);
			
	}

	@Override
	public GameState apply(GameState state) {
		return new GameState(state, peasant, new Peasant(peasant.id, peasant.carrying, -1, townhall), this);
	}
	
	@Override
	public int getPeasantId(){
		return peasant.id;
	}

}
