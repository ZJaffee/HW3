package edu.cwru.sepia.agent.planner.actions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
	

	public static List<Resource> adjacent_forests(Peasant p, GameState state){
		List<Resource> adj_forests = new LinkedList<Resource>();
		for(Resource forest : state.forests){
			if(forest.pos.chebyshevDistance(p.pos) == 1 && forest.amount > 0){
				adj_forests.add(forest);
			}
		}
		
		return adj_forests;
	}


}
