package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.Unit.UnitView;

public class Townhall {
	public final int id;
	public final Position pos;
	public final int amountWood;
	public final int amountGold;
	
	public Townhall(UnitView unit){
		id = unit.getID();
		pos = new Position(unit.getXPosition(), unit.getYPosition());
		amountWood = 0;
		amountGold = 0;
	}
	
	private Townhall(int id, Position pos, int amountWood, int amountGold){
		this.id = id;
		this.pos = pos;
		this.amountWood = amountWood;
		this.amountGold = amountGold;
	}
	
	public Townhall depositWood(){
		return new Townhall(id, pos, amountWood + 100, amountGold);
	}
	
	public Townhall depositGold(){
		return new Townhall(id, pos, amountWood, amountGold + 100);
	}

}
