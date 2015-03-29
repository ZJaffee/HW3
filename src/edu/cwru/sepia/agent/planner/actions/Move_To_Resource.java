package edu.cwru.sepia.agent.planner.actions;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Resource;


public class Move_To_Resource implements StripsAction{
	public final Peasant peasant;
	public final int toResourceId;
	public final Resource resource;
	public final int dist;
	
	public Move_To_Resource(Peasant p, int resourceId, int dist, Resource resource){
		peasant = p;
		toResourceId = resourceId;
		this.dist = dist;
		this.resource = resource;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		return (peasant.isAtResource != toResourceId) && resource.amount > 0;
			
	}

	@Override
	public GameState apply(GameState state) {
		//System.out.println("Moving to resource");
		return new GameState(state, peasant, new Peasant(peasant.id, peasant.carrying, toResourceId, -1), this, dist);
	}
	
	@Override
	public int getPeasantId(){
		return peasant.id;
	}

}
