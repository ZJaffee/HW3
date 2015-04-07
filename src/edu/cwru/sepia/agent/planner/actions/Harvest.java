package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Peasant.Item;
import edu.cwru.sepia.agent.planner.Resource;

/**
 * A harvest action
 * This is abstract so that I will differentiate between harvesting wood and gold very simply with the abstract getType function
 *
 */
public abstract class Harvest implements StripsAction{

	//The peasant and resource this harvest move relates to
	public final Peasant peasant;
	public final Resource resource;
	
	//Have peasant p harvest resource r
	public Harvest(Peasant p, Resource r){
		peasant = p;
		resource = r;
	}
	
	//Returns true if peasant p can harvest resource r in the GameState state
	@Override
	public boolean preconditionsMet(GameState state) {
		//The peasant must have nothing, and be at the resource, and the resource must have some resource left
		return peasant.hasNothing() && peasant.isAtResource == resource.id && resource.amount > 0;
	}
	
	//The type of resource we are harvesting
	protected abstract Item getResourceType();

	@Override
	public GameState apply(GameState state) {
		//The new peasant will be holding this resource type
		Peasant p = new Peasant(peasant.id, getResourceType(), peasant.isAtResource, -1);
		//The new resource will have 100 less resource
		Resource r = new Resource(resource.id, resource.amount - 100, resource.type, resource.dist);
		//Return the state after these changes
		return new GameState(state, peasant, p, resource, r, this, 1);
	}
	
	@Override
	public int getPeasantId(){
		return peasant.id;
	}

}
