package edu.cwru.sepia.agent.planner;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

public class Peasant{
	public final int id;
	public final Position pos;
	public final Item carrying;
	public enum Item {WOOD, GOLD, NOTHING};
	
	public Peasant(UnitView unit, Item toCarry){
		pos = new Position(unit.getXPosition(), unit.getYPosition());
		carrying = toCarry;
		id = unit.getID();
	}
	
	public Peasant(int myID, Position p, Item toCarry){
		id = myID;
		pos = p;
		carrying = toCarry;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Peasant){
			return id == ((Peasant)o).id;
		}
		return false;
	}
}