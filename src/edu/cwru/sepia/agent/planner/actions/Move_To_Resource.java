package edu.cwru.sepia.agent.planner.actions;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Resource;

//Move to resource StripsAction
public class Move_To_Resource implements StripsAction{
	public final Peasant peasant;
	public final int toResourceId;
	public final Resource resource;
	public final int dist;
	
	//We will try moving Peasant p to the resource with id resourceId
	public Move_To_Resource(Peasant p, int resourceId, int dist, Resource resource){
		peasant = p;
		toResourceId = resourceId;
		this.dist = dist;
		this.resource = resource;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		//The peasant must not be at the resource already, and the resource must still exist
		return (peasant.isAtResource != toResourceId) && resource.amount > 0;
			
	}

	@Override
	public GameState apply(GameState state) {
		//The new state simple has a new peasant at that resource
		return new GameState(state, peasant, new Peasant(peasant.id, peasant.carrying, toResourceId, -1), this, dist);
	}
	
	@Override
	public int getPeasantId(){
		return peasant.id;
	}

}
