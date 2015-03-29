package edu.cwru.sepia.agent.planner;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

public class Peasant implements Comparable<Peasant>{
	public final int id;
	public final Item carrying;
	public enum Item {WOOD, GOLD, NOTHING};
	public final int isAtResource;
	public final int isAtTownhall;
	
	public Peasant(UnitView unit, Item toCarry){
		carrying = toCarry;
		id = unit.getID();
		isAtResource = -1;
		isAtTownhall = -1;
	}
	
	public Peasant(int myID, Item toCarry, int resourceId, int townhall){
		id = myID;
		carrying = toCarry;
		isAtResource = resourceId;
		isAtTownhall = townhall;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Peasant){
			Peasant p = (Peasant) o;
			return id == p.id && isAtResource == p.isAtResource && isAtTownhall == p.isAtTownhall && carrying == p.carrying;
		}
		return false;
	}

	public boolean hasNothing() {
		return carrying == Item.NOTHING;
	}
	
	@Override
	public String toString(){
		return id + ", "+carrying+", "+isAtResource+", "+isAtTownhall;
	}

	@Override
	public int compareTo(Peasant p) {
		return id - p.id;
	}
}