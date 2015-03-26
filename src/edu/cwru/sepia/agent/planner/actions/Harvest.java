package edu.cwru.sepia.agent.planner.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
		return peasant.hasNothing() && peasant.pos.isAdjacent(resource.pos) && resource.amount > 0;
	}
	
	protected abstract Item getResourceType();

	@Override
	public GameState apply(GameState state) {
		Peasant p = new Peasant(peasant.id, peasant.pos, getResourceType());
		Resource r = new Resource(resource.id, resource.pos, resource.amount - 100, resource.type);
		return new GameState(state, peasant, p, resource, r);
	}

}
