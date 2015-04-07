package edu.cwru.sepia.agent.planner;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

/**
 * This class contains all relevant information we need to know about a Peasant
 *
 */
public class Peasant implements Comparable<Peasant>{
	//The id of the peasant (may not be congruient with SEPIA unit id)
	public final int id;
	//What the peasant is carrying
	public final Item carrying;
	//An enum for what the peasant can carry
	public enum Item {WOOD, GOLD, NOTHING};
	//The id of the resource the peasant is at (-1 if not at a resource)
	public final int isAtResource;
	//The id of the townhall the Peasant is at (-1 if not at townhall)
	//In practice, this could just be a boolean.  For some reason I began
	//this assignment thinking it would be interesting to make it compatible in cases
	//there are multiple townhalls
	public final int isAtTownhall;
	
	//The constructor if the Peasant is neither at the townhall nor a resource
	public Peasant(UnitView unit, Item toCarry){
		carrying = toCarry;
		id = unit.getID();
		isAtResource = -1;
		isAtTownhall = -1;
	}
	
	//The constructor if the peasant is also at a resource and/or townhall
	public Peasant(int myID, Item toCarry, int resourceId, int townhall){
		id = myID;
		carrying = toCarry;
		isAtResource = resourceId;
		isAtTownhall = townhall;
	}
	
	//Two peasants are equal if they have the same id, are at the same pleaces, and are carrying the same things
	@Override
	public boolean equals(Object o){
		if(o instanceof Peasant){
			Peasant p = (Peasant) o;
			return id == p.id && isAtResource == p.isAtResource && isAtTownhall == p.isAtTownhall && carrying == p.carrying;
		}
		return false;
	}

	//Returns true if the peasant is holding nothing
	public boolean hasNothing() {
		return carrying == Item.NOTHING;
	}
	
	//A toString for debugging
	@Override
	public String toString(){
		return id + ", "+carrying+", "+isAtResource+", "+isAtTownhall;
	}

	//CompareTo simply is based on the peasant ids
	// -- Technically this is bad because compareTo can be 0 when equals is false, but because
	// -- the only places where I actually use the compareTo (TreeSet of peasants), I only have one peasant of each id, this is ok
	@Override
	public int compareTo(Peasant p) {
		return id - p.id;
	}
}