package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceNode.Type;

public class Resource implements Comparable<Resource>{
	public final Position pos;
	public final int amount;
	public final int id;
	public final Type type;
	public final int dist;
	
	public Resource(ResourceView rv, int dist){
		id = rv.getID();
		pos = new Position(rv.getXPosition(), rv.getYPosition());
		amount = rv.getAmountRemaining();
		type = rv.getType();
		this.dist = dist;
	}
	
	public Resource(int myID, Position p, int amnt, Type t, int dist){
		pos = p;
		amount = amnt;
		id = myID;
		type = t;
		this.dist = dist;
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

	@Override
	public int compareTo(Resource o) {
		return (dist - o.dist);
	}
	
	@Override
	public String toString(){
		return "id "+id+", dist "+dist+", amount "+amount;
	}
	
}
