package edu.cwru.sepia.agent.planner.actions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Peasant.Item;
import edu.cwru.sepia.agent.planner.Resource;

public class Harvest_Gold extends Harvest{

	public Harvest_Gold(Peasant p, Resource r) {
		super(p, r);
	}
	
	@Override
	protected Item getResourceType(){
		return Item.GOLD;
	}
	
	public static List<Resource> adjacent_mines(Peasant p, GameState state){
		List<Resource> adj_mines = new LinkedList<Resource>();
		for(Resource gold : state.mines){
			if(gold.pos.chebyshevDistance(p.pos) == 1 && gold.amount > 0){
				adj_mines.add(gold);
			}
		}
		
		return adj_mines;
	}


}
