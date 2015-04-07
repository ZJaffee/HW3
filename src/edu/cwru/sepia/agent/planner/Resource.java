package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceNode.Type;

/**
 * This class contians all the relevant information about a resource
 *
 */
public class Resource implements Comparable<Resource>{
	//The amount of resource this resource has
	public final int amount;
	//The id of this resource -- this id IS congruent with sepia ids
	public final int id;
	//The type of resource this is
	public final Type type;
	//The distance to the townhall
	public final int dist;
	
	//Constructor given resource view and distance to townhall
	public Resource(ResourceView rv, int dist){
		id = rv.getID();
		amount = rv.getAmountRemaining();
		type = rv.getType();
		this.dist = dist;
	}
	
	//Constructor given everything
	public Resource(int myID, int amnt, Type t, int dist){
		amount = amnt;
		id = myID;
		type = t;
		this.dist = dist;
	}
	
	//The hashCode is just the id hashCode
	@Override
	public int hashCode(){
		return ((Integer)id).hashCode();
	}
	
	//Two resources are equal if they have the same id
	@Override
	public boolean equals(Object o){
		if(o instanceof Resource){
			return id == ((Resource)o).id;
		}
		return false;
	}

	//compareTo is just comparing ids
	@Override
	public int compareTo(Resource o) {
		int ret = (dist - o.dist);
		if( ret == 0){
			ret = id - o .id;
		}
		return ret;
	}
	
	//toString for debugging
	@Override
	public String toString(){
		return "id "+id+", dist "+dist+", amount "+amount;
	}
	
}
