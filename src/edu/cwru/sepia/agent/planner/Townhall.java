package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.Unit.UnitView;

/**
 * A class for all relevant information about the townhall
 *    --- not a super necessary class but whatever...
 */
public class Townhall {
	//The id of the townhall
	public final int id;
	//The position of the townhall
	public final Position pos;
	
	//Constructor with unitview
	public Townhall(UnitView unit){
		id = unit.getID();
		pos = new Position(unit.getXPosition(), unit.getYPosition());
	}
	
	//The constructor with all values
	private Townhall(int id, Position pos, int amountWood, int amountGold){
		this.id = id;
		this.pos = pos;
	}
	

}
