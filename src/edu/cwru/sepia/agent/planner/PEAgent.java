package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.action.ActionType;
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
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
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
    private int numPeasants;
    private int newNumPeasants;
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
        Map<Integer, Action> toReturn = new HashMap<Integer, Action>();
    	StripsAction action;
    	Set<Integer> activeUnits = new HashSet<Integer>();
    	Map<Integer,ActionResult> feedback = historyView.getCommandFeedback(0, stateView.getTurnNumber() - 1);
    	ActionFeedback myFeedback;
    	ActionResult myResult;
    	for(UnitView unit : stateView.getAllUnits()){
    		myResult = feedback.get(unit.getID());
    		//System.out.println(myResult);
    		if(myResult != null){
	    		myFeedback = myResult.getFeedback();
	    		if(myFeedback == ActionFeedback.INCOMPLETE ){
	    			activeUnits.add(unit.getID());
	    		}else if(myFeedback == ActionFeedback.FAILED){
	    			toReturn.put(unit.getID(), prevAction.get(unit.getID()));
	    			return toReturn;
	    		}
    		}
    	}

    	Stack<StripsAction> putback = new Stack<StripsAction>();
    	while( !plan.isEmpty() &&/* toReturn.size() < 1 &&*/ putback.size() <= numPeasants + 1){
        	action = plan.pop();
        	int unitId = action.getPeasantId() == -1? townhallId : action.getPeasantId();
        
        	if(!peasantIdMap.containsKey(unitId)){
        		matchPeasant(unitId, stateView);
        	}
        	//System.out.println(unitId);
        	int sepiaId = peasantIdMap.get(unitId);
        	if(activeUnits.contains(sepiaId) || preconditionsNotMet(action, stateView)){
        		putback.add(action);
        		continue;
        	}else{
            	activeUnits.add(sepiaId);
        	}
        	if(action instanceof Build_Peasant && !toReturn.isEmpty()){
        		return toReturn;
        	}
        	Action curAction = createSepiaAction(action, stateView);
        	toReturn.put(sepiaId, curAction);
        	prevAction.put(sepiaId, curAction);
        	if(action instanceof Build_Peasant){
        		break;
        	}
        }
    	while(!putback.empty()){
    		plan.push(putback.pop());
    	}
    	System.out.println(toReturn);
    	numPeasants = newNumPeasants;
    	return toReturn;
    }

    private boolean preconditionsNotMet(StripsAction action, StateView stateView) {
		if(action instanceof Build_Peasant){
			return stateView.getResourceAmount(0, ResourceType.GOLD) < 400;
		}
		return false;
	}

	private void matchPeasant(int unitId, StateView stateView) {
		for(UnitView unit : stateView.getAllUnits()){
			if(!peasantIdMap.containsValue(unit.getID())){
				peasantIdMap.put(unitId, unit.getID());
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

    	
    	if(action instanceof Build_Peasant){
    		newNumPeasants++;
    		return Action.createPrimitiveProduction(townhallId, peasantTemplateId);
    	}
    	
    	UnitView peasant = null;
    	UnitView townhall = stateView.getUnit(townhallId);
    	Position peasantPos = null;
    	for(UnitView c : stateView.getAllUnits()){
    		if( c.getID() == peasantIdMap.get(action.getPeasantId())){
    			peasant = c;
    			peasantPos = new Position(peasant.getXPosition(), peasant.getYPosition());
    		}
    	}
    	if(action instanceof Harvest){
    		Harvest hAction = (Harvest) action;
    		ResourceView toHarvest = null;
    		for(ResourceView rv : stateView.getAllResourceNodes()){
    			if(rv.getID() == hAction.resource.id){
    				toHarvest = rv;
    				break;
    			}
    		}
    		Position resourcePos = new Position(toHarvest.getXPosition(), toHarvest.getYPosition());
    		action.getPeasantId();
    		peasantPos.getDirection(resourcePos);
    		return Action.createPrimitiveGather(peasant.getID(), peasantPos.getDirection(resourcePos));
    	}
    	
    	if(action instanceof Move_To_Resource){
    		Move_To_Resource mAction = (Move_To_Resource) action;
    		ResourceView moveTo = null;
    		for(ResourceView rv : stateView.getAllResourceNodes()){
    			if(rv.getID() == mAction.toResourceId){
    				moveTo = rv;
    				break;
    			}
    		}
    		return Action.createCompoundMove(peasant.getID(), moveTo.getXPosition(), moveTo.getYPosition());
    	}
    	
    	if(action instanceof Move_To_Townhall){
    		return Action.createCompoundMove(peasant.getID(), townhall.getXPosition(), townhall.getYPosition());
    	}
    	
    	if(action instanceof Deposit){
    		Position townhallPos = new Position(townhall.getXPosition(), townhall.getYPosition());
    		return Action.createPrimitiveDeposit(peasant.getID(), peasantPos.getDirection(townhallPos));
    	}
    	System.err.println("I do not know how to create this action.");
    	return null;
    }

    /*private void assignPeasantId(int id) {
		Set<Entry<Integer, Integer>> peasants = peasantIdMap.entrySet();
		for(Entry<Integer,Integer> p : peasants){
			if(p.getValue() == -1){
				peasantIdMap.put(p.getKey(), id);
				System.out.println("Matched " + p.getKey() + " to "+id);
				return;
			}
		}
	}
*/
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
