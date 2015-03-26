package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceNode.Type;

public class Resource {
	public final Position pos;
	public final int amount;
	public final int id;
	public final Type type;
	
	public Resource(ResourceView rv){
		id = rv.getID();
		pos = new Position(rv.getXPosition(), rv.getYPosition());
		amount = rv.getAmountRemaining();
		type = rv.getType();
	}
	
	public Resource(int myID, Position p, int amnt, Type t){
		pos = p;
		amount = amnt;
		id = myID;
		type = t;
	}
	
	@Override
	public int hashCode(){
		return pos.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Resource){
			return pos.equals(((Resource)o).pos);
		}
		return false;
	}
	
}
