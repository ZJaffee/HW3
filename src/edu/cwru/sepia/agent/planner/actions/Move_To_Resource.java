package edu.cwru.sepia.agent.planner.actions;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;


public class Move_To_Resource implements StripsAction{
	public final Peasant peasant;
	public final int toResource;
	public final int dist;
	
	public Move_To_Resource(Peasant p, int resource, int dist){
		peasant = p;
		toResource = resource;
		this.dist = dist;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		return (peasant.isAtResource != toResource);
			
	}

	@Override
	public GameState apply(GameState state) {
		return new GameState(state, peasant, new Peasant(peasant.id, peasant.carrying, toResource, -1), this, dist);
	}
	
	@Override
	public int getPeasantId(){
		return peasant.id;
	}

}
