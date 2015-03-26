package edu.cwru.sepia.agent.planner.actions;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.agent.planner.Resource;
import edu.cwru.sepia.util.Direction;


public class Move implements StripsAction{
	public final Peasant peasant;
	public final Direction dir;
	private Position pos_in_dir;
	
	public Move(Peasant p, Direction d){
		peasant = p;
		dir = d;
		pos_in_dir = peasant.pos.move(dir);
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		return pos_in_dir.inBounds(state.xExtent, state.yExtent) && !state.resourceAt(pos_in_dir);
			
	}

	@Override
	public GameState apply(GameState state) {
		return new GameState(state, peasant, new Peasant(peasant.id, pos_in_dir, peasant.carrying), this);
	}
	
	@Override
	public int getPeasantId(){
		return peasant.id;
	}

}
