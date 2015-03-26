package edu.cwru.sepia.agent.planner.actions;

import java.util.Set;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Peasant.Item;
import edu.cwru.sepia.agent.planner.Resource;

public class Harvest_Wood extends Harvest{

	public Harvest_Wood(Peasant p, Resource r) {
		super(p, r);
	}
	
	@Override
	protected Item getResourceType(){
		return Item.WOOD;
	}
	
	@Override
	protected Set<Resource> getResourceList(GameState state){
		return state.forests;
	}


}
