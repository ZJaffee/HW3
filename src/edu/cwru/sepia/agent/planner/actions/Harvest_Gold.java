package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Peasant.Item;
import edu.cwru.sepia.agent.planner.Resource;

//A harvest gold StripsAction
public class Harvest_Gold extends Harvest{

	public Harvest_Gold(Peasant p, Resource r) {
		super(p, r);
	}
	
	//We just have to override this function
	@Override
	protected Item getResourceType(){
		return Item.GOLD;
	}
	


}
