package edu.cwru.sepia.agent.planner.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Peasant.Item;
import edu.cwru.sepia.agent.planner.Resource;

public abstract class Harvest implements StripsAction{

	public final Peasant peasant;
	public final Resource resource;
	
	public Harvest(Peasant p, Resource r){
		peasant = p;
		resource = r;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		return peasant.hasNothing() && peasant.isAtResource == resource.id && resource.amount > 0;
	}
	
	protected abstract Item getResourceType();

	@Override
	public GameState apply(GameState state) {
		//System.out.println("Harvesting");
		Peasant p = new Peasant(peasant.id, getResourceType(), peasant.isAtResource, -1);
		Resource r = new Resource(resource.id, resource.pos, resource.amount - 100, resource.type, resource.dist);
		return new GameState(state, peasant, p, resource, r, this, 1);
	}
	
	@Override
	public int getPeasantId(){
		return peasant.id;
	}

}
