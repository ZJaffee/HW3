package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.*;
import java.util.*;

/**
 * Created by Devin on 3/15/15.
 */
public class PlannerAgent extends Agent {

    final int requiredWood;
    final int requiredGold;
    final boolean buildPeasants;

    // Your PEAgent implementation. This prevents you from having to parse the text file representation of your plan.
    PEAgent peAgent;

    public PlannerAgent(int playernum, String[] params) {
        super(playernum);

        if(params.length < 3) {
            System.err.println("You must specify the required wood and gold amounts and whether peasants should be built");
        }

        requiredWood = Integer.parseInt(params[0]);
        requiredGold = Integer.parseInt(params[1]);
        buildPeasants = Boolean.parseBoolean(params[2]);


        System.out.println("required wood: " + requiredWood + " required gold: " + requiredGold + " build Peasants: " + buildPeasants);
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {

        Stack<StripsAction> plan = AstarSearch(new GameState(stateView, playernum, requiredGold, requiredWood, buildPeasants));
        if(plan == null) {
            System.err.println("No plan was found");
            System.exit(1);
            return null;
        }

        // write the plan to a text file
        savePlan(plan);


        // Instantiates the PEAgent with the specified plan.
        peAgent = new PEAgent(playernum, plan);

        return peAgent.initialStep(stateView, historyView);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
        if(peAgent == null) {
            System.err.println("Planning failed. No PEAgent initialized.");
            return null;
        }

        return peAgent.middleStep(stateView, historyView);
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

    /**
     * Perform an A* search of the game graph. This should return your plan as a stack of actions. This is essentially
     * the same as your first assignment. The implementations should be very similar. The difference being that your
     * nodes are now GameState objects not MapLocation objects.
     *
     * @param startState The state which is being planned from
     * @return The plan or null if no plan is found.
     */
    private Stack<StripsAction> AstarSearch(GameState startState) {
    	//Initialize the open and closed sets
    	//Closed set is a HashSet in order to have constant .contains() check
    	Set<GameState> closedSet = new HashSet<GameState>();
    	
    	//Open set is a priority queue so the GameStates will be sorted
    	PriorityQueue<GameState> openSet = new PriorityQueue<GameState>();
    	
    	//Add the initial state to the open set
    	openSet.add(startState);
    	
    	GameState current;
    	//While the open set is not empty
    	while(!openSet.isEmpty())
    	{
    		//Remove the best GameState from the open set
    		current = openSet.poll();
    		
    		//If this is the goal, we've completed the search
    		if(current.isGoal())
    		{
    			System.out.println("done");
    			return reconstructPath(current);
    		}
    		//Otherwise, add this location to the closed set
    		closedSet.add(current);
    		//Get the successors of this node
    		List<GameState> sucessors = current.generateChildren();
    		
    		//Check each successor
    		for(GameState neighbor : sucessors)
    		{
    			//I found that including the closedSet check resulted in no plans being found
    			//while not including this check, optimal solutions are still being found,
    			//So I think it is good to omit this step
    			//if(closedSet.contains(neighbor)){
    				// do nothing
    			//}
    			
    			//If it is not in the open set
				if(!openSet.contains(neighbor)){
					//Add it to the open set
					openSet.add( neighbor );
				}
    			
    		}
    	}
        // there is no path
    	return null;
    }
    
    //Reconstructs the StripsActions path by getting the StripsAction of GameState parents
    private Stack<StripsAction> reconstructPath(GameState goalState) {
		Stack<StripsAction> actions = new Stack<StripsAction>();
		GameState parent = goalState;
		System.out.printf("The estimated number of turns to execute this plan is %.0f\n",Math.ceil(goalState.getCost()));
		while( parent.parent != null ){
			actions.push(parent.action);
			parent = parent.parent;
		}
		return actions;
	}

    /**
     * This has been provided for you. Each strips action is converted to a string with the toString method. This means
     * each class implementing the StripsAction interface should override toString. Your strips actions should have a
     * form matching your included Strips definition writeup. That is <action name>(<param1>, ...). So for instance the
     * move action might have the form of Move(peasantID, X, Y) and when grounded and written to the file
     * Move(1, 10, 15).
     *
     * @param plan Stack of Strips Actions that are written to the text file.
     */
    private void savePlan(Stack<StripsAction> plan) {
    	if (plan == null) {
            System.err.println("Cannot save null plan");
            return;
        }

        File outputDir = new File("saves");
        outputDir.mkdirs();

        File outputFile = new File(outputDir, "plan.txt");

        PrintWriter outputWriter = null;
        try {
            outputFile.createNewFile();

            outputWriter = new PrintWriter(outputFile.getAbsolutePath());

            Stack<StripsAction> tempPlan = (Stack<StripsAction>) plan.clone();
            while(!tempPlan.isEmpty()) {
                outputWriter.println(tempPlan.pop().toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputWriter != null)
                outputWriter.close();
        }
    }
}
