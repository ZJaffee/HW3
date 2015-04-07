package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Peasant.Item;
import edu.cwru.sepia.agent.planner.Resource;

//Harvest wood stripsAction
public class Harvest_Wood extends Harvest{

	public Harvest_Wood(Peasant p, Resource r) {
		super(p, r);
	}
	
	//We just have to return Wood type
	@Override
	protected Item getResourceType(){
		return Item.WOOD;
	}
	


}
