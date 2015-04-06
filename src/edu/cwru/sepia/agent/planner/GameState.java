package edu.cwru.sepia.agent.planner;


import edu.cwru.sepia.environment.model.state.ResourceNode.Type;
import edu.cwru.sepia.agent.planner.Peasant.Item;
import edu.cwru.sepia.agent.planner.actions.Build_Peasant;
import edu.cwru.sepia.agent.planner.actions.Deposit;
import edu.cwru.sepia.agent.planner.actions.Harvest_Gold;
import edu.cwru.sepia.agent.planner.actions.Harvest_Wood;
import edu.cwru.sepia.agent.planner.actions.Move_To_Resource;
import edu.cwru.sepia.agent.planner.actions.Move_To_Townhall;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class is used to represent the state of the game after applying one of the avaiable actions. It will also
 * track the A* specific information such as the parent pointer and the cost and heuristic function. Remember that
 * unlike the path planning A* from the first assignment the cost of an action may be more than 1. Specifically the cost
 * of executing a compound action such as move can be more than 1. You will need to account for this in your heuristic
 * and your cost function.
 *
 * The first instance is constructed from the StateView object (like in PA2). Implement the methods provided and
 * add any other methods and member variables you need.
 *
 * Some useful API calls for the state view are
 *
 * state.getXExtent() and state.getYExtent() to get the map size
 *
 * I recommend storing the actions that generated the instance of the GameState in this class using whatever
 * class/structure you use to represent actions.
 */
public class GameState implements Comparable<GameState> {
	
	public final int xExtent;
	public final int yExtent;
	public int amountGold;
	public int amountWood;
	private int turn;
	public final int playernum;
	public final int requiredGold;
	public final int requiredWood;
	private boolean buildPeasants;
	public TreeSet<Peasant> peasants;
	public TreeSet<Resource> mines;
	public TreeSet<Resource> forests;
	public Townhall townhall;
	public StripsAction action;
	public GameState parent;
	public Map<Integer, Integer> distToTownhall;
	private int maxUnitId;
	
	@Override
	public String toString(){
		return
				"GameState:\n"
				+ "amountGold = "+amountGold+", amountWood = "+amountWood+ ", turn = "+turn+
				"\npeasants = "+peasants+" , size = "+peasants.size()+"\n"
						+ "cost = "+getCost()+", heuristic = "+heuristic();
	}
	
    /**
     * Construct a GameState from a stateview object. This is used to construct the initial search node. All other
     * nodes should be constructed from the another constructor you create or by factory functions that you create.
     *
     * @param state The current stateview at the time the plan is being created
     * @param playernum The player number of agent that is planning
     * @param requiredGold The goal amount of gold (e.g. 200 for the small scenario)
     * @param requiredWood The goal amount of wood (e.g. 200 for the small scenario)
     * @param buildPeasants True if the BuildPeasant action should be considered
     */
    public GameState(State.StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants) {
        // TODO: Implement me!
    	//basic info
		xExtent = state.getXExtent();
		yExtent = state.getYExtent();
		turn = state.getTurnNumber();
		
		//goalstate
		this.playernum = playernum;
		this.requiredGold = requiredGold;
		this.requiredWood = requiredWood;
		this.buildPeasants = buildPeasants;
		
		//what we are trying to achieve
		amountGold = state.getResourceAmount(playernum, ResourceType.GOLD);
		amountWood = state.getResourceAmount(playernum, ResourceType.WOOD);
		
		action = null;
		parent = null;
		
		distToTownhall = new HashMap<Integer, Integer>();
		setPeasants(state.getUnits(playernum));
		setResources(state.getAllResourceNodes());
		//System.out.println(mines.size());
    }
    
    public GameState(GameState original, Peasant pToRemove, Peasant pToAdd, StripsAction action, int addTurns){
    	// TODO: Implement me!
    	//basic info
		xExtent = original.xExtent;
		yExtent = original.yExtent;
		turn = original.turn + addTurns;
		
		//goalstate
		this.playernum = original.playernum;
		this.requiredGold = original.requiredGold;
		this.requiredWood = original.requiredWood;
		this.buildPeasants = original.buildPeasants;
		
		//what we are trying to achieve
		amountGold = original.amountGold;
		amountWood = original.amountWood;
				
		peasants = new TreeSet<Peasant>();
		peasants.addAll(original.peasants);
		peasants.remove(pToRemove);
		peasants.add(pToAdd);
		
		mines = original.mines;
		forests = original.forests;
		
		distToTownhall = original.distToTownhall;
		
		townhall = original.townhall;

		maxUnitId = original.maxUnitId;
		this.action = action;
		parent = original;

    	//System.out.println(toString());
    }
    
    public GameState(GameState original, Peasant pToRemove, Peasant pToAdd, Resource rToRemove, Resource rToAdd, StripsAction action, int addTurns){
    	this(original, pToRemove, pToAdd, action, addTurns);
    	switch(rToRemove.type){
    		case TREE:
    			forests = new TreeSet<Resource>();
    			forests.addAll(original.forests);
    			forests.remove(rToRemove);
    			forests.add(rToAdd);
    			break;
    		case GOLD_MINE:
    			mines = new TreeSet<Resource>();
    			mines.addAll(original.mines);
    			mines.remove(rToRemove);
    			mines.add(rToAdd);
    			break;
    		default:
    			System.out.println("Error: unkown resource type;");
    	}

    	//System.out.println(toString());
    }
    
    private GameState(GameState original, StripsAction action){
    	// TODO: Implement me!
    	//basic info
		xExtent = original.xExtent;
		yExtent = original.yExtent;
		turn = original.turn + 1;
		
		//goalstate
		this.playernum = original.playernum;
		this.requiredGold = original.requiredGold;
		this.requiredWood = original.requiredWood;
		this.buildPeasants = original.buildPeasants;
		
		//what we are trying to achieve
		amountGold = original.amountGold;
		amountWood = original.amountWood;
				
		peasants = new TreeSet<Peasant>();
		peasants.addAll(original.peasants);
		
		mines = original.mines;
		forests = original.forests;
		
		distToTownhall = original.distToTownhall;
		maxUnitId = original.maxUnitId;
		townhall = original.townhall;
		
		this.action = action;
		parent = original;
    }
    
    public GameState addPeasant(StripsAction action){
    	GameState toReturn = new GameState(this, action);
    	toReturn.peasants.add(new Peasant((++toReturn.maxUnitId), Item.NOTHING, -1, townhall.id));
    	toReturn.amountGold -= 400;

    	//System.out.println(toReturn.toString());
    	return toReturn;
    }

    private void setResources(List<ResourceView> allResourceNodes) {
		mines = new TreeSet<Resource>();
		forests = new TreeSet<Resource>();
		Position pos;
		for(ResourceView rv : allResourceNodes){
			pos = new Position(rv.getXPosition(), rv.getYPosition());
			distToTownhall.put(rv.getID(), townhall.pos.chebyshevDistance(pos));
			switch(rv.getType()){
				case GOLD_MINE:
					mines.add(new Resource(rv, distToTownhall.get(rv.getID())));
					break;
				case TREE:
					forests.add(new Resource(rv, distToTownhall.get(rv.getID())));
					System.out.println(Arrays.toString(forests.toArray()));
					break;
				default:
					System.out.println("Error: Unknown resource type.");
			}
		}
	}
	
	private void setPeasants(List<UnitView> units){
		peasants = new TreeSet<Peasant>();
		maxUnitId = Integer.MIN_VALUE;
		for(UnitView unit : units){
			if(unit.getID() > maxUnitId){
				maxUnitId = unit.getID();
			}
			if(unit.getTemplateView().canMove()){
				peasants.add(new Peasant(unit, Item.NOTHING));
			}else{
				townhall = new Townhall(unit);
			}
		}
	}

	/**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
        // TODO: Implement me!
        if(amountGold>=requiredGold && amountWood >= requiredWood)
        	return true;
        return false;
    }

    /**
     * The branching factor of this search graph are much higher than the planning. Generate all of the possible
     * successor states and their associated actions in this method.
     *
     * @return A list of the possible successor states and their associated actions
     */
    public List<GameState> generateChildren() {
        // TODO: Implement me!
        Set<GameState> nextQ = new HashSet<GameState>();
        List<GameState> currQ = new LinkedList<GameState>();
        currQ.add(this);
        for(Peasant p : peasants){
        	while(!currQ.isEmpty()){
        		GameState cur = currQ.remove(0);
        		nextQ.addAll(getAllMoves(p,cur));
        	}
        	currQ.addAll(nextQ);
        	nextQ.clear();
        }
        
        
		return currQ;
        
    }
    
    private List<GameState> getAllMoves(Peasant p, GameState cur) {
		List<GameState> validMoves = new LinkedList<GameState>();
		StripsAction curMove;
		GameState initial = cur;
		if(buildPeasants){
			curMove = new Build_Peasant();
			if(curMove.preconditionsMet(cur)){
				initial = curMove.apply(cur);
			}
		}
		if(p.isAtResource == -1 && p.hasNothing()){
			//try moving
    		//System.out.println("Start");
			int found = 0;
			if(initial.requiredGold - initial.amountGold > 0){
				MOVE_MINES:
				for(Resource r : initial.mines){
		    		curMove = new Move_To_Resource(p, r.id, distToTownhall.get(r.id), r);

					//System.out.println("Trying to move to "+r);

	    			//System.out.println("Moving to mine");
		    		if(curMove.preconditionsMet(initial)){
		    			validMoves.add(curMove.apply(initial));
		    			found++;
		    			//if(found > 2)
		    			//	break MOVE_MINES;
		    		}
		    	}
			}
			if(initial.requiredWood - initial.amountWood > 0){
				MOVE_FORESTS:
		    	for(Resource r : initial.forests){
					//System.out.println("Trying to move to "+r);
		    		curMove = new Move_To_Resource(p, r.id, distToTownhall.get(r.id), r);
	    			//System.out.println(r.id+" -- "+r.amount);
		    		if(curMove.preconditionsMet(initial)){
		    			validMoves.add(curMove.apply(initial));
		    			found++;
		    			//System.out.println("Did it");
		    			//if(found > 5)
		    			//	break MOVE_FORESTS;
		    		}
		    	}
			}
		}
		
		/*if(buildPeasants){
			curMove = new Build_Peasant();
    		if(curMove.preconditionsMet(cur)){
    			validMoves.add(curMove.apply(cur));
    		}
		}*/
		
		if(p.isAtResource != -1 && p.hasNothing()){
			//try harvesting
			for(Resource r : initial.mines){
				curMove = new Harvest_Gold(p, r);
	    		if(curMove.preconditionsMet(initial)){
	    			validMoves.add(curMove.apply(initial));
	    			break;
	    		}
			}
			for(Resource r : initial.forests){	    		
	    		curMove = new Harvest_Wood(p, r);
	    		if(curMove.preconditionsMet(initial)){
	    			validMoves.add(curMove.apply(initial));
	    			break;
	    		}
	    	}
		}
		
		if(p.isAtResource != -1 && !p.hasNothing()){
			//try moving to townhall
			Integer dist = distToTownhall.get(p.isAtResource);
	    	if(dist == null){
	    		dist = 1;
	    	}
	    	curMove = new Move_To_Townhall(p, initial.townhall.id, dist);
	    	if(curMove.preconditionsMet(initial)){
				validMoves.add(curMove.apply(initial));
			}	
		}
		
		if(p.isAtTownhall != -1 && !p.hasNothing()){
			//Add deposit if possible
	    	curMove = new Deposit(p, initial.townhall);
	    	if(curMove.preconditionsMet(initial)){
				//System.out.println("Gonna deposit");
				validMoves.add(curMove.apply(initial));
			}
		}
		//System.out.println(validMoves.size());
		return validMoves;
	}

/*
 *     private Set<MapLocation> getSucessors(MapLocation current, MapLocation enemyFootmanLoc, Set<MapLocation> resourceLocations, int xExtent, int yExtent)
    {
    	//Initialize the returned set
    	Set<MapLocation> toReturn = new HashSet<MapLocation>();
    	//We must check all the grid locations around current
    	//-- up, down, left, right, and diagonoals
    	for(int i = -1; i<=1; i++)
    	{
    		for(int j = -1; j<=1; j++)
    		{
    			//Make the map location of this potential succesor
    			MapLocation loc = new MapLocation(current.x+i,current.y+j);
    			//Check if there's anything already in this potential successor
    			if(isValidLocation(current, loc, enemyFootmanLoc, resourceLocations, xExtent,  yExtent))
    			{
    				//If there's nothing at loc, it is a successor, so add it to the return set
    				toReturn.add(loc);
    			}
    		}
    	}
    	return toReturn;
    	
    }

 */
    /**
     * Write your heuristic function here. Remember this must be admissible for the properties of A* to hold. If you
     * can come up with an easy way of computing a consistent heuristic that is even better, but not strictly necessary.
     *
     * Add a description here in your submission explaining your heuristic.
     *
     * @return The value estimated remaining cost to reach a goal state from this state.
     */
    public double heuristic() {
        int neededGold = requiredGold - amountGold;
        if(neededGold < 0){
        	neededGold = 0;
        	//return Double.POSITIVE_INFINITY;
        }
        int neededWood = requiredWood - amountWood;
        if(neededWood < 0){
        	neededWood = 0;
        	//return Double.POSITIVE_INFINITY;
        }
        
        int peasantsHave = 0;
        for(Peasant p : peasants){
        	if(p.carrying == Item.WOOD && neededWood > 0){
        		peasantsHave += 100;
        	}else if(p.carrying == Item.GOLD && neededGold > 0){
        		peasantsHave += 100;
        	}
        }
        double turnsToGetNeeded = ((neededGold + neededWood - peasantsHave/2)/100.0)*8;
        //System.out.println("Size = "+peasants.size() + " -- "+turnsToGetNeeded/peasants.size());
        return turnsToGetNeeded/peasants.size();
    }

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {
        return turn ;
    }

    /**
     * This is necessary to use your state in the Java priority queue. See the official priority queue and Comparable
     * interface documentation to learn how this function should work.
     *
     * @param o The other game state to compare
     * @return 1 if this state costs more than the other, 0 if equal, -1 otherwise
     */
    @Override
    public int compareTo(GameState o) {
        // TODO: Implement me!
        return Double.compare(this.getCost() + this.heuristic(),o.getCost() + o.heuristic());
    }

    /**
     * This will be necessary to use the GameState as a key in a Set or Map.
     *
     * @param o The game state to compare
     * @return True if this state equals the other state, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if(o instanceof GameState){
        	GameState g = (GameState) o;
        	//System.out.println("Do they equal?\n"+toString()+g.toString());
        	if(amountWood != g.amountWood || amountGold != g.amountGold){
        		return false;
        	}
        	for(Peasant p : peasants){
        		if(!g.peasants.contains(p)){
        			return false;
        		}
        	}
        	//System.out.println("Yes");
        	return true;
        }
        return false;
    }

    /**
     * This is necessary to use the GameState as a key in a HashSet or HashMap. Remember that if two objects are
     * equal they should hash to the same value.
     *
     * @return An integer hashcode that is equal for equal states.
     */
    @Override
    public int hashCode() {
    	int result = ((Integer)amountWood).hashCode();
        result = 31*result + ((Integer)amountGold).hashCode();
        return result;
    }

	/*public boolean resourceAt(Position pos) {
		Resource dummy = new Resource(-1, pos, -1, null);
		return mines.contains(dummy) || forests.contains(dummy);
	}*/
}
