# P3
# Zachary Jaffee zij
# George Hodulik gmh73
Project 3 for intro to AI

NOTE that when using the visual agent, when we drag the speed towards going very fast sometimes the program does not act in a desired manner. 
If this happens to you, try running just not on the fastest speed.

Please use our code in full, as a significant amount of functions were modified in order to make this work. 
We recommend importing the project fully to test it, particularly because of the extra files.

use the data/FileName.xml tag in the arguments
we use main2 as the class to run the code.

We added a couple different files and classes
these include Resource, Townhall, and Peasant for the planner package, which contain the relevant information we need to plan.
and Build_Peasant, Deposit, Harvest, Harvest_Gold, Harvest_Wood, Move_To_Resourse, and Move_To_Townhall in the action package
we also modified the StripsAction interface slightly to require a function that returns the peasant id that that action is being acted on,
or -1 if it is the build peasant action.

Our cost and heuristic functions are the following.
Cost = # of steps to get to this state.  # of steps starts at 0 and is incremented for each strips action.  Actions like deposit, harvest, etc,
increment by 1, while moving actions increment by the distance between the two locations.  When there are multiple peasants, the increment value
is the # of turns divided by the number of peasants -- this is why we changed the turn variable to a double, not an int.  This assumes that 
all work is divided evenly by all peasants.

Heuristic = Minimum number of turns estimated to reach goal / number of peasants.  Minimum number of turns is, put simply, the number of resources
 we need, divided by 100, times private static variable MIN_NUM_TURNS_TO_GET_RESOURCE, which is the calculated minimum number of turns to get a
 resource.  In the given maps, this value is 6, because the closest resource to the townhall is 4 away, meaning a peasant has to travel at least 2
 moves to get from one to the other.  Adding the harvest and deposit step, that's 2*2 + 2 = 6.
 The nature of the min value keeps our heuristic admissible.  
 Since the MIN_NUM_TURNS_TO_GET_RESOURCE is calculated, our solution should still be optimal in different maps.
 
 
We do not implement the Movek, Harvestk, etc actions that the assignment suggests.  Instead, we calculate cost in the way we specified, and schedule
accordingly in PEAgent.  PEAgent looks ahead in the plan to be able to return multiple, parallel actions.
