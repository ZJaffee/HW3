package edu.cwru.sepia.agent.planner;


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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
	
	//The estimated average/min number of turns to obtain a resource
	//Since, in our maps, the closes resource is 3 away from the townhall, this number is 
	// 2*3 + 2 for 2 moves and 1 gather and 1 deposit moves
	private static double AVG_NUM_TURNS_TO_GET_RESOURCE = 8.0;
	
	//Other parameters
	public final int xExtent;
	public final int yExtent;
	public int amountGold;
	public int amountWood;
	private double turn;
	public final int playernum;
	public final int requiredGold;
	public final int requiredWood;
	private boolean buildPeasants;
	public Townhall townhall;
	
	//TreeSets were ideal here because it turned out that iterating
	//in a specific order had certain benefits
	// Peasants, we always want to get moves for peasants in order of peasant ids,
	// So that the scheduler will have an easier time
	//   Ex, the returned stack having move orders of p1,p1,p1,p1,p1,p2,p2,p2,p2,p3,p3,p3,p3 will still acheive the goal
	//   But it is hard for PEAgent because it has to look ahead very far in the stack
	public TreeSet<Peasant> peasants;
	
	//We want to sort resources by their distance to the townhall (or ids if distance is equal)
	//Because we always want to gather from the closest resources first
	public TreeSet<Resource> mines;
	public TreeSet<Resource> forests;
	
	//The action it took to get here
	public StripsAction action;
	//The parent of the state
	public GameState parent;
	//A map of resource id --> distance from townhall
	//This is redundant information because Resource objects also have this info
	//  But it makes methods much simpler
	public Map<Integer, Integer> distToTownhall;
	
	//The maximum peasant id in this state -- important for assigning new peasant ids
	private int maxUnitId;
	
	//A toString for debugging
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
		
		//There is no parent or action to get to this state
		action = null;
		parent = null;
		
		//Set the resource and peasant information
		distToTownhall = new HashMap<Integer, Integer>();
		setPeasants(state.getUnits(playernum));
		setResources(state.getAllResourceNodes());
    }
    
    //This constructor is used for new states that involve changing the state of a peasant
    //It will remove the peasant pToRemove from our set of peasants, and add pToAdd
    //pToRemove and pToAdd are actully different states of the same peasant, stores as two distinct Peasant objects
    public GameState(GameState original, Peasant pToRemove, Peasant pToAdd, StripsAction action, int addTurns){
    	//basic info
		xExtent = original.xExtent;
		yExtent = original.yExtent;
		//note that the turn# increment is divided by the number of peasants
		//The idea is that the turns will be distributed evenly between all peasants
		turn = original.turn + (1.0*addTurns)/original.peasants.size();   //when turn used to be an int//Math.round(((float)addTurns)/original.peasants.size());
		
		//goalstate
		this.playernum = original.playernum;
		this.requiredGold = original.requiredGold;
		this.requiredWood = original.requiredWood;
		this.buildPeasants = original.buildPeasants;
		
		//what we are trying to achieve
		amountGold = original.amountGold;
		amountWood = original.amountWood;
				
		//Add all the old peasants, remove the old one, and add the new
		peasants = new TreeSet<Peasant>();
		peasants.addAll(original.peasants);
		peasants.remove(pToRemove);
		peasants.add(pToAdd);
		
		//Mine and forest info remains the same
		mines = original.mines;
		forests = original.forests;
		
		//the distToTownhall map remains the same
		distToTownhall = original.distToTownhall;
		
		//The townhall remains the same
		townhall = original.townhall;

		//maxUnitId is the same
		maxUnitId = original.maxUnitId;
		
		//Set the action to be the given parameter
		this.action = action;
		//The parent of this is the original GameState parameter
		parent = original;
    }
    
    //This constructor is used when both a peasant changes and a resource changes (a harvest move)
    public GameState(GameState original, Peasant pToRemove, Peasant pToAdd, Resource rToRemove, Resource rToAdd, StripsAction action, int addTurns){
    	//Update the peasants with previous contructor
    	this(original, pToRemove, pToAdd, action, addTurns);
    	//Add and remove from the forests or mine set
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
    }
    
    //A base constructor that copies the original GameState, simply updating the parent and action
    private GameState(GameState original, StripsAction action){
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
    
    //The constructor for adding a peasant
    public GameState addPeasant(StripsAction action){
    	//Copy this GameState
    	GameState toReturn = new GameState(this, action);
    	//Add a new peasant to the copy with the id 1 + maxUnitId
    	toReturn.peasants.add(new Peasant((++toReturn.maxUnitId), Item.NOTHING, -1, townhall.id));
    	//decrement our gold
    	toReturn.amountGold -= 400;
    	
    	return toReturn;
    }

    //Extract all the Resource info we need
    private void setResources(List<ResourceView> allResourceNodes) {
    	//Initialize forest and mine sets
		mines = new TreeSet<Resource>();
		forests = new TreeSet<Resource>();
		Position pos;
		//For each resource
		for(ResourceView rv : allResourceNodes){
			//Make the position object for this resource
			pos = new Position(rv.getXPosition(), rv.getYPosition());
			//Calculate the chebyshev distance from townhall to this resource
			distToTownhall.put(rv.getID(), townhall.pos.chebyshevDistance(pos));
			switch(rv.getType()){
				//If the resource is a gold mine, add it to our mines set
				case GOLD_MINE:
					mines.add(new Resource(rv, distToTownhall.get(rv.getID())));
					break;
				//If its a tree, add it to our forests set
				case TREE:
					forests.add(new Resource(rv, distToTownhall.get(rv.getID())));
					break;
				default:
					System.out.println("Error: Unknown resource type.");
			}
		}
		//I tried setting the AVG_NUM_TURNS_TO_GET_RESOURCE here, but it turned out to be more optimal
		//to just set it to 8
		/* It happened to be that performance was better by setting 
		 * AVG_NUM_TURNS_TO_GET_RESOURCE to 8 rather than calculating the actual average
		 * double avg_dist = 0.0;
		for(Integer d : distToTownhall.values()){
			avg_dist += (1.0*d)/distToTownhall.size();
		}
		AVG_NUM_TURNS_TO_GET_RESOURCE = 2*avg_dist + 2;*/
	}
	
    //Get all the peasant and townhall info we need
	private void setPeasants(List<UnitView> units){
		peasants = new TreeSet<Peasant>();
		maxUnitId = Integer.MIN_VALUE;
		//Go through each unit
		for(UnitView unit : units){
			//Remember the max unit id
			if(unit.getID() > maxUnitId){
				maxUnitId = unit.getID();
			}
			//If the unit can move, its a peasant
			if(unit.getTemplateView().canMove()){
				peasants.add(new Peasant(unit, Item.NOTHING));
			}
			//Otherwise its the townhall
			else{
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
    	//If we have the amoung of gold and wood we need, we are at the goal
        return amountGold>=requiredGold && amountWood >= requiredWood;
    }

    /**
     * The branching factor of this search graph are much higher than the planning. Generate all of the possible
     * successor states and their associated actions in this method.
     * 
     * The implementation of this method has some limitations:
     * It only looks for peasants' move sequences in order of peasant id.
     * If there were certain limitations, like if there could only be 2 peasants at one resource at a time,
     * 	this would limit our search space.  However, we do not have any limitations like this
     * 	(technicall, only 8 peasants can be at one place at once, but we never get to that case)
     * 
     *
     * @return A list of the possible successor states and their associated actions
     */
    public List<GameState> generateChildren() {
    	//We have two queues (actually aren't queues in implementation, but that doesn't matter)
    		//They actually where queues originally but then it was necessary to pop without removing
        Set<GameState> nextQ = new HashSet<GameState>();
        List<GameState> currQ = new ArrayList<GameState>();
        //Add this GameState, the initial parent, to the current queue
        currQ.add(this);
        //For each peasant (note that order is inheritly in increasing id order because of TreeSet and compareTo)
        for(Peasant p : peasants){
        	//Find all the moves this peasant can make from each state in currQ
        	for(int i = 0; i < currQ.size(); i++){
        		GameState cur = currQ.get(i);
        		//Retain all the moves in nextQ
        		nextQ.addAll(getAllMoves(p,cur));
        	}
        	//If there were any moves for this peasant
        	if(!nextQ.isEmpty()){
        		//Reset currQ to have all the states in nextQ
        		currQ.clear();
            	currQ.addAll(nextQ);
            	//Clear nextQ
            	nextQ.clear();
        	}
        }
        //currQ should not ever be empty
        //assert(!currQ.isEmpty());
        //currQ should not contain this
        //assert(!currQ.contains(this));
		return currQ;
        
    }
    
    //This function gets all the possible resulting game states that Peasant p can do on GameState initial
    //We take some liberties -- for example, if a peasant has some wood, it technically can move to other resources,
    //but theres no reason to.  Thus, if a peasant has a resource, we say it MUST move to the townhall next, etc
    private List<GameState> getAllMoves(Peasant p, GameState initial) {
		List<GameState> validMoves = new LinkedList<GameState>();
		StripsAction curMove;
		//If this is a map where we can build peasants
		if(buildPeasants){
			//Try building a peasant
			curMove = new Build_Peasant();
			if(curMove.preconditionsMet(initial)){
				//If we can build a peasant, add all the states that would result from p moving
				//AFTER the new peasant is made
				validMoves.addAll(getAllMoves(p, curMove.apply(initial)));
			}
		}
		
		//If the peasant is not at a resource and has nothing
		if(p.isAtResource == -1 && p.hasNothing()){
			//Try moving to a resource
			
			//First, try moving to a mine, but only if we need gold
			if(initial.requiredGold - initial.amountGold - peasantsGetting(initial, true) > 0){
				//For each mine, check if we can move to it
				for(Resource r : initial.mines){
		    		curMove = new Move_To_Resource(p, r.id, distToTownhall.get(r.id), r);
		    		if(curMove.preconditionsMet(initial)){
		    			validMoves.add(curMove.apply(initial));
		    			//Since the mines are sorted in order of distance to townhall, we can
		    			//break as soon as we find one mine to move to
		    			break;
		    		}
		    	}
			}
			//Then, try moving to a forest, but only if we need wood
			if(initial.requiredWood - initial.amountWood - peasantsGetting(initial, false) > 0){
		    	for(Resource r : initial.forests){
					//System.out.println("Trying to move to "+r);
		    		curMove = new Move_To_Resource(p, r.id, distToTownhall.get(r.id), r);
	    			//System.out.println(r.id+" -- "+r.amount);
		    		if(curMove.preconditionsMet(initial)){
		    			validMoves.add(curMove.apply(initial));
		    			//Since the forests are sorted in order of distance to townhall, we can
		    			//break as soon as we find one forest to move to
		    			break;
		    		}
		    	}
			}
		}
		
		//If the peasant is at a resource and has nothing, try harvesting
		TRY_HARVEST:
		if(p.isAtResource != -1 && p.hasNothing()){
			//First, try harvesting each mine
			for(Resource r : initial.mines){
				curMove = new Harvest_Gold(p, r);
	    		if(curMove.preconditionsMet(initial)){
	    			validMoves.add(curMove.apply(initial));
	    			//We have found the resource the peasant is at, so we can break
	    			break TRY_HARVEST;
	    		}
			}
			//Then try harvesting forests
			for(Resource r : initial.forests){	    		
	    		curMove = new Harvest_Wood(p, r);
	    		if(curMove.preconditionsMet(initial)){
	    			validMoves.add(curMove.apply(initial));
	    			//We have found the resource the peasant is at, so we can break
	    			break TRY_HARVEST;
	    		}
	    	}
		}
		
		//If the peasant is at a resource and is holding a resource, go to the townhall
		if(p.isAtResource != -1 && !p.hasNothing()){
			//We have to calculate the distance to the townhall to properly increment the turn count
			Integer dist = distToTownhall.get(p.isAtResource);
			//Try moving to townhall, and add the resulting state to the list of validMoves
	    	curMove = new Move_To_Townhall(p, initial.townhall.id, dist);
	    	if(curMove.preconditionsMet(initial)){
				validMoves.add(curMove.apply(initial));
			}	
		}
		
		//If the peasant is at the town hall and has something, try depositing
		if(p.isAtTownhall != -1 && !p.hasNothing()){
	    	curMove = new Deposit(p, initial.townhall);
	    	if(curMove.preconditionsMet(initial)){
				validMoves.add(curMove.apply(initial));
			}
		}
		//return all the valid moves
		return validMoves;
	}

    //This function returns the amount of resources peasants are currently getting in GameState intial
    //  It returns the number of wood peasants are getting if gettingMine == false,
    //  It returns the number of gold the peasants are getting if gettingMine == true
    private int peasantsGetting(GameState initial, boolean gettingMine) {
		int ret = 0;
		//For each peasant
		for(Peasant p : initial.peasants){
			int at = p.isAtResource;
			//If the peasant is at a reasource and has nothing, it can be assumed it is about to harvest,
			// thus, it is getting gold or wood
			if(at != -1 && p.hasNothing()){
				//If it is getting gold and we are querying gettingGold, increment by 100
				if(gettingMine && resourceIsMine(initial, at)){
					ret += 100;
				}
				//If it is getting wood (the resource it is at is not a mine -- must be a forest),
				//increment by 100
				else if(!gettingMine && !resourceIsMine(initial, at)){
					ret += 100;
				}
			}
			//Otherwise, if the peasant is carrying something, increment if what is carrying is what we are querying
			else if(!p.hasNothing()){
				if(p.carrying == Item.GOLD && gettingMine){
					ret += 100;
				}else if(p.carrying == Item.WOOD && !gettingMine){
					ret += 100;
				}
			}
		}
		return ret;
	}

    //Returns true if the resource with id at is a mine
	private boolean resourceIsMine(GameState initial, int at) {
		for(Resource r : initial.mines){
			if(r.id == at){
				return true;
			}
		}
		return false;
	}
	
    /**
     * Write your heuristic function here. Remember this must be admissible for the properties of A* to hold. If you
     * can come up with an easy way of computing a consistent heuristic that is even better, but not strictly necessary.
     *
     * Add a description here in your submission explaining your heuristic.
     * The heuristc returns the number of moves estimated to complete the goal.  It does this by calculating the resources we need:
     * 	neededResource = required - amountWeHave - amountPeasantsHave
     * then, we return ((neededResource/100)*8)/numPeasants, because each 100 resourced need 1 more "move" and the minimum cost of a "move" is 8,
     * since the closest resource is 3 moves away, and we need to harvest and deposit: 2*3 + 2 = 8.  We assume the turn load is equally
     * distributed among peasants.
     * This is admissible (at least mostly) because 8 is a minimum value, and we do not take into account that the resources peasants
     * are still carrying still needs to be carried to the townhall.  Also, the assumption that the turn load will be equally distrubted among
     * the peasants is an idealized assumption, further simplifying the problem --> admissible heuristic.
     *
     * @return The value estimated remaining cost to reach a goal state from this state.
     */
    public double heuristic() {

    	//Calculate the number of wood and gold peasants have
        int peasantsHaveWood = 0;
        int peasantsHaveGold = 0;
        for(Peasant p : peasants){
        	if(p.carrying == Item.WOOD ){
        		peasantsHaveWood += 100;
        	}else if(p.carrying == Item.GOLD ){
        		peasantsHaveGold += 100;
        	}
        }
        //Calculate the needed Gold
        int neededGold = requiredGold - amountGold - peasantsHaveGold;
        if(neededGold < 0){
        	neededGold = 0;
        }
        //Calculate the needed wood
        int neededWood = requiredWood - amountWood - peasantsHaveWood;
        if(neededWood < 0){
        	neededWood = 0;
        }
        
        //Calculate the number of turns we need to get the remaining resources
        double turnsToGetNeeded = ((neededGold + neededWood)/100.0)*AVG_NUM_TURNS_TO_GET_RESOURCE;
        //return the number of turns divided by the number of peasants we have
        return turnsToGetNeeded/peasants.size();
    }

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     * 
     * The cost is just the current turn value
     * Note that the turn value is incremented (not always by the same amount) at each move, and when there are multiple peasants,
     * the increment is divided by the number of peasants, to assume that the load has been distributed evenly among peasants.
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
    	//We just have to compare the cost + heuristic
        return Double.compare(this.getCost() + this.heuristic(),o.getCost() + o.heuristic());
    }

    /**
     * This will be necessary to use the GameState as a key in a Set or Map.
     *
     * Two GameStates are equal if they have the same amount of wood and gold, and they have the same peasants.
     * 	Look to the Peasants class to see when peasants are considered equal
     *
     * @param o The game state to compare
     * @return True if this state equals the other state, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if(o instanceof GameState){
        	GameState g = (GameState) o;
        	//If the amountWoods/amountGolds are not the same, the gamestates are not equal
        	if(amountWood != g.amountWood || amountGold != g.amountGold){
        		return false;
        	}
        	//If this state has a peasant that g does not, or vice versa, the states are not equal
        	for(Peasant p : peasants){
        		if(!g.peasants.contains(p)){
        			return false;
        		}
        	}
        	for(Peasant p : g.peasants){
        		if(!peasants.contains(p)){
        			return false;
        		}
        	}
        	//The states can be considered equal otherwise
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
    	//Two equal states will have the same hashCode, since equal states have the same amountWood and amountGold
    	//But note that the hashCodes can be the same for two nonequal states, since equal() also looks at peasants
    	int result = ((Integer)amountWood).hashCode();
        result = 31*result + ((Integer)amountGold).hashCode();
        return result;
    }
}
