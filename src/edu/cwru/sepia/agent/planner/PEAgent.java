package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.actions.*;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Template;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * This is an outline of the PEAgent. Implement the provided methods. You may add your own methods and members.
 */
public class PEAgent extends Agent {

    // The plan being executed
    private Stack<StripsAction> plan = null;

    // maps the real unit Ids to the plan's unit ids
    // when you're planning you won't know the true unit IDs that sepia assigns. So you'll use placeholders (1, 2, 3).
    // this maps those placeholders to the actual unit IDs.
    private Map<Integer, Integer> peasantIdMap;
    private int townhallId;
    private int peasantTemplateId;
    
    //The number of peasants at the start of this turn
    private int numPeasants;
    //The number of peasants at the end of this turn
    private int newNumPeasants;
    //A map of unit ids to that unit's previous action -- need this because some actions may fail
    //For instance, two peasants may run into each other, so they need to repeat their compountMove
    private Map<Integer, Action> prevAction;

    public PEAgent(int playernum, Stack<StripsAction> plan) {
        super(playernum);
        peasantIdMap = new HashMap<Integer, Integer>();
        this.plan = plan;
        numPeasants = 1;
        newNumPeasants = 1;
        prevAction = new HashMap<Integer, Action>();
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {
        // gets the townhall ID and the peasant ID
        for(int unitId : stateView.getUnitIds(playernum)) {
            Unit.UnitView unit = stateView.getUnit(unitId);
            String unitType = unit.getTemplateView().getName().toLowerCase();
            if(unitType.equals("townhall")) {
                townhallId = unitId;
                peasantIdMap.put(unitId, unitId);
            } else if(unitType.equals("peasant")) {
                peasantIdMap.put(unitId, unitId);
            }
        }

        // Gets the peasant template ID. This is used when building a new peasant with the townhall
        for(Template.TemplateView templateView : stateView.getTemplates(playernum)) {
            if(templateView.getName().toLowerCase().equals("peasant")) {
                peasantTemplateId = templateView.getID();
                break;
            }
        }

        return middleStep(stateView, historyView);
    }

    /**
     * This is where you will read the provided plan and execute it. If your plan is correct then when the plan is empty
     * the scenario should end with a victory. If the scenario keeps running after you run out of actions to execute
     * then either your plan is incorrect or your execution of the plan has a bug.
     *
     * You can create a SEPIA deposit action with the following method
     * Action.createPrimitiveDeposit(int peasantId, Direction townhallDirection)
     *
     * You can create a SEPIA harvest action with the following method
     * Action.createPrimitiveGather(int peasantId, Direction resourceDirection)
     *
     * You can create a SEPIA build action with the following method
     * Action.createPrimitiveProduction(int townhallId, int peasantTemplateId)
     *
     * You can create a SEPIA move action with the following method
     * Action.createCompoundMove(int peasantId, int x, int y)
     *
     * these actions are stored in a mapping between the peasant unit ID executing the action and the action you created.
     *
     * For the compound actions you will need to check their progress and wait until they are complete before issuing
     * another action for that unit. If you issue an action before the compound action is complete then the peasant
     * will stop what it was doing and begin executing the new action.
     *
     * To check an action's progress you can call getCurrentDurativeAction on each UnitView. If the Action is null nothing
     * is being executed. If the action is not null then you should also call getCurrentDurativeProgress. If the value is less than
     * 1 then the action is still in progress.
     *
     * Also remember to check your plan's preconditions before executing!
     */
    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
    	//Our return map
        Map<Integer, Action> toReturn = new HashMap<Integer, Action>();
        
        //First, find out which units are active, as we do not want to give them another command yet
    	Set<Integer> activeUnits = new HashSet<Integer>();
    	Map<Integer,ActionResult> feedback = historyView.getCommandFeedback(0, stateView.getTurnNumber() - 1);
    	ActionFeedback myFeedback;
    	ActionResult myResult;
    	for(UnitView unit : stateView.getAllUnits()){
    		myResult = feedback.get(unit.getID());
    		if(myResult != null){
	    		myFeedback = myResult.getFeedback();
	    		//If the unit's action is incomplete, add it to the activeUnits set
	    		if(myFeedback == ActionFeedback.INCOMPLETE ){
	    			activeUnits.add(unit.getID());
	    		}
	    		//If the unit's action failed, repeat it
	    		else if(myFeedback == ActionFeedback.FAILED){
	    			toReturn.put(unit.getID(), prevAction.get(unit.getID()));
	    			activeUnits.add(unit.getID());
	    		}
    		}
    	}

    	StripsAction action;
    	//We will look ahead a few moves to improve scheduling capability
    	//As a result, must remember the actions we skipped so we can put them back later
    	Stack<StripsAction> putback = new Stack<StripsAction>();
    	//As long as the plan is not empty and we have not given all the peasants commands
    	while( !plan.isEmpty() && activeUnits.size() < numPeasants ){
    		//Get the next action
        	action = plan.pop();
        	//Get the STRIPS unit id of the peasant to move
        	int unitId = action.getPeasantId() == -1? townhallId : action.getPeasantId();
        
        	//If this STRIPS id is not id out peasantIdMap, match it to a new peasant
        	if(!peasantIdMap.containsKey(unitId)){
        		matchPeasant(unitId, stateView);
        	}
        	//Get the sepia id of this unit
        	int sepiaId = peasantIdMap.get(unitId);
        	//If this unit is active or the preconditions are not met for this action
        	if(activeUnits.contains(sepiaId) || preconditionsNotMet(action, stateView)){
        		//Skip this action and continue
        		putback.add(action);
        		continue;
        	}else{
        		//Otherwise, we can now consider this unit to be active, since we are giving it a move
            	activeUnits.add(sepiaId);
        	}
        	//There appeared to be a bug where buildPeasant actions could not be parallelized
        	//So, I make sure to only buildPeasant actions alone
        	if(action instanceof Build_Peasant && !toReturn.isEmpty()){
        		break;
        	}
        	//Add the sepia action to our return map
        	Action curAction = createSepiaAction(action, stateView);
        	toReturn.put(sepiaId, curAction);
        	prevAction.put(sepiaId, curAction);
        	
        	//We want to always return BuildPeasant actions alone -- there was a weird bug where SEPIA would say it made a peasant
        	//when it actually did not if we didnt do this
        	if(action instanceof Build_Peasant){
        		break;
        	}
        }
    	//Put the moves we skipped back on the plan stack
    	while(!putback.empty()){
    		plan.push(putback.pop());
    	}
    	//If a peasant was made, update the numPeasants
    	//It was necessary to save the numPeasants as two values, otherwise the value would update mid-loop above,
    	//and it could cause problems
    	numPeasants = newNumPeasants;
    	
    	return toReturn;
    }

    //The only precondition we must check is that we have enough gold to build peasants
    //Otherwise, the townhall tries to make a peasant before enough deposits have been made
    private boolean preconditionsNotMet(StripsAction action, StateView stateView) {
		if(action instanceof Build_Peasant){
			//The preconditons for a Build_Peasant action are not met if tthe amount of gold for player 0 is less than 400
			return stateView.getResourceAmount(0, ResourceType.GOLD) < 400;
		}
		return false;
	}

    //Given an unmatched unitId in STRIPS space, match it to an unmatched SEPIA unit
	private void matchPeasant(int unitId, StateView stateView) {
		//Loop through each SEPIA peasant
		for(UnitView unit : stateView.getAllUnits()){
			//If this SEPIA unit does not have a STRIPS match in our map
			if(!peasantIdMap.containsValue(unit.getID())){
				//match it
				peasantIdMap.put(unitId, unit.getID());
				break;
			}
		}
	}

	/**
     * Returns a SEPIA version of the specified Strips Action.
     * @param action StripsAction
     * @return SEPIA representation of same action
     */
    private Action createSepiaAction(StripsAction action, State.StateView stateView) {
       /* You can create a SEPIA deposit action with the following method
        * Action.createPrimitiveDeposit(int peasantId, Direction townhallDirection)
        *
        * You can create a SEPIA harvest action with the following method
        * Action.createPrimitiveGather(int peasantId, Direction resourceDirection)
        *
        * You can create a SEPIA build action with the following method
        * Action.createPrimitiveProduction(int townhallId, int peasantTemplateId)
        *
        * You can create a SEPIA move action with the following method
        * Action.createCompoundMove(int peasantId, int x, int y)
        */

    	//If this is a buildPeasant action
    	if(action instanceof Build_Peasant){
    		//Increment newNumPeasants and return a production action
    		newNumPeasants++;
    		return Action.createPrimitiveProduction(townhallId, peasantTemplateId);
    	}
    	
    	//Otherwise, get the UnitView of this peasant and its position
    	UnitView peasant = null;
    	UnitView townhall = stateView.getUnit(townhallId);
    	Position peasantPos = null;
    	for(UnitView c : stateView.getAllUnits()){
    		if( c.getID() == peasantIdMap.get(action.getPeasantId())){
    			peasant = c;
    			peasantPos = new Position(peasant.getXPosition(), peasant.getYPosition());
    		}
    	}
    	//If we are harvesting
    	if(action instanceof Harvest){
    		Harvest hAction = (Harvest) action;
    		ResourceView toHarvest = null;
    		//Get the resourceView we are harvesting from
    		for(ResourceView rv : stateView.getAllResourceNodes()){
    			if(rv.getID() == hAction.resource.id){
    				toHarvest = rv;
    				break;
    			}
    		}
    		//Get the position of the resource
    		Position resourcePos = new Position(toHarvest.getXPosition(), toHarvest.getYPosition());
    		//Return a primitive gather on this peasant in the direction of the resource
    		return Action.createPrimitiveGather(peasant.getID(), peasantPos.getDirection(resourcePos));
    	}
    	
    	//If the action is a move to a resource
    	if(action instanceof Move_To_Resource){
    		Move_To_Resource mAction = (Move_To_Resource) action;
    		ResourceView moveTo = null;
    		//Get the resource we are moving to
    		for(ResourceView rv : stateView.getAllResourceNodes()){
    			if(rv.getID() == mAction.toResourceId){
    				moveTo = rv;
    				break;
    			}
    		}
    		//Return a compound move to the location of that resource
    		return Action.createCompoundMove(peasant.getID(), moveTo.getXPosition(), moveTo.getYPosition());
    	}
    	
    	//If the action is to move to a townhall, just return a compound move at the town hall
    	if(action instanceof Move_To_Townhall){
    		return Action.createCompoundMove(peasant.getID(), townhall.getXPosition(), townhall.getYPosition());
    	}
    	
    	//If the action is a deposit
    	if(action instanceof Deposit){
    		//Return a deposit in the direction of the townhall
    		Position townhallPos = new Position(townhall.getXPosition(), townhall.getYPosition());
    		return Action.createPrimitiveDeposit(peasant.getID(), peasantPos.getDirection(townhallPos));
    	}
    	
    	//An unknown StripsAction has been given to us
    	System.err.println("I do not know how to create this action.");
    	return null;
    }

	@Override
    public void terminalStep(State.StateView stateView, History.HistoryView historyView) {

    }

    @Override
    public void savePlayerData(OutputStream outputStream) {

    }

    @Override
    public void loadPlayerData(InputStream inputStream) {

    }
}
