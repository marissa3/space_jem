import java.util.ArrayList;
import java.util.List;

import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;


public class Space_JEM_mcts__multi extends StateMachineGamer {
	private long timeout;
	int buffTime = 10000; //in milliseconds

	private Node_multi root = null;


	@Override
	public StateMachine getInitialStateMachine() {
		// TODO Auto-generated method stub
		return new CachedStateMachine(new ProverStateMachine());
	}

	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// TODO Auto-generated method stub

	}

	public int minScore(Role role, Move m, MachineState state, StateMachine machine, int level, int limit, int count) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
		if (timeout - System.currentTimeMillis() < buffTime) {
			return 0;
		}
		List<Role> allroles = machine.getRoles();
		int score = 100;
		if (allroles.size() > 1){ //only for 2 players
			Role opponent = null;
			if (allroles.get(0).getName().equals(role.getName())){
				opponent = allroles.get(1);
			} else {
				opponent = allroles.get(0);
			}
			score = 100;
			List<Move> moves = machine.getLegalMoves(state, opponent);
			for (Move move : moves){
				List<Move> jointMove = new ArrayList<Move>();
				List<Role> roles = machine.getRoles();
				if (role.equals(roles.get(0))){
					jointMove.add(m);
					jointMove.add(move);
				} else {
					jointMove.add(move);
					jointMove.add(m);
				}
				MachineState newState = machine.getNextState(state, jointMove);
				int result = maxScore(role, newState, machine, level + 1, limit, count);
				if (result == 0) return 0;
				if (result < score) score = result;
			}
		} else {
			score = 100;
			List<Move> moves = machine.getLegalMoves(state, role);
			for (Move move : moves){
				List<Move> jointMove = new ArrayList<Move>();
				List<Role> roles = machine.getRoles();
				jointMove.add(m);
				jointMove.add(move);
				MachineState newState = machine.getNextState(state, jointMove);
				int result = maxScore(role, newState, machine, level + 1, limit, count);
				if (result == 0) return 0;
				if (result < score) score = result;
			}
		}
		return score;
	}
	private int min(int a, int b) {
		if (a <= b) return a;
		return b;
	}

	public int maxScore(Role role, MachineState state, StateMachine machine, int level, int limit, int count) throws MoveDefinitionException, GoalDefinitionException, TransitionDefinitionException
	{
		if (timeout - System.currentTimeMillis() < buffTime) {
			return 0;
		}
		if (machine.isTerminal(state)){
			return machine.getGoal(state, role);
		}
		List<Role> allroles = machine.getRoles();
		if(level >= limit) {
			return montecarlo(role, state, machine, count);
		}
		int score = 0;
		//actions list; find legals
		List<Move> moves = machine.getLegalMoves(state, role);

		for (Move m : moves){
			int result = minScore(role, m, state, machine, level, limit, count);
			if(result == 100) return 100;
			if (result > score) score = result;
		}
		return score;
	}

	private int montecarlo(Role role, MachineState state, StateMachine machine, int count) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		int total = 0;
		for(int i = 0; i < count; i++){
			total += depthcharge(role, state, machine);
		}
		return total/count;
	}

	private int depthcharge(Role role, MachineState state, StateMachine machine) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		if (machine.isTerminal(state)){
			return machine.getGoal(state, role);
		}
		List<Move> m = new ArrayList<Move>();
		List<Role> roles = machine.getRoles();
		if (timeout - System.currentTimeMillis() < buffTime) {
			return 0;
		}
		for(int i = 0; i < roles.size(); i++){
			List<Move> moves= machine.getLegalMoves(state, roles.get(i));
			int move_i = (int)(Math.random() * moves.size());
			m.add(moves.get(move_i));
		}
		MachineState newState = machine.getNextState(state, m);
		return depthcharge(role, newState, machine);
	}

	private Node_multi select(Node_multi node){
		//List<Move> actions = machine.getLegalMoves(state, role);
		//List<Node_multi> grandchildren = new ArrayList<Node_multi>();
		int numGrandchildren = 0;
		for (Node_multi child : node.children){
			numGrandchildren += child.children.size();
		}
		if(numG != node.numTotalChildren){
			return node;
		}

		int score = 0;
		Node_multi result = node;
		for (int i=0; i < node.children.size(); i++){
			int newscore = (int)selectfn(node.children.get(i));
			if (newscore >= score){
				score = newscore;
				result = node.children.get(i);
			}
		}
		return select(result);
	}

	private MachineState findAvailState(Node_multi node, MachineState state, StateMachine machine, Role role) throws MoveDefinitionException, TransitionDefinitionException{
		List<Move> moves = machine.getLegalMoves(state, role);
		return machine.getNextState(state, moves);
		//if parent, check if it has visits
		//if no visits, choose the parent
		//if visited, choose a child, then recurse
	}

	//single
	/*private boolean expand (Node_multi node, MachineState state, StateMachine machine, Role role) throws MoveDefinitionException, TransitionDefinitionException{
		List<Move> actions = machine.getLegalMoves(state, role);
		//List<List<Move>> jointActions = machine.getLegalJointMoves(state);
		MachineState newstate = findAvailState(node, state, machine, role);
		for (int i = 0; i < actions.size(); i++){
			MachineState newstate = machine.getNextState(state, actions);// = simulate(seq(actions[i]),state);
			Node_multi newnode = new Node_multi(0, 0, node, newstate, actions.get(i));
			node.children.add(newnode);
		}
		return true;
	}*/

	private List<String> movesList(Node_multi node){
		List<String> moves = new ArrayList<String>();
		for(List<Node_multi> gchild : node.grandchildren){
			moves.add(gchild.move.toString());
		}
		return moves;
	}

	private int get_role_index(StateMachine machine, Role role){
		List<Role> roles = machine.getRoles();
		int i = 0;
		for (Role r : roles){
			if (r.getName().equals(role.getName())){
				return i;
			}
			i++;
		}
		return 0;
	}

	private Node_multi create_blue_node(Node_multi parent, MachineState node_state, Move my_move, int numChildren){
		Node_multi newnode = new Node_multi(0, 0, parent, node_state, my_move, numChildren);
		return newnode;
	}

	private Node_multi create_red_node(Node_multi parent, MachineState node_state, Move my_move, int numChildren){
		Node_multi newnode = new Node_multi(0, 0, parent, node_state, my_move, numChildren);
		return newnode;
	}

	private Node_multi expand (Node_multi node, MachineState state, StateMachine machine, Role role) throws MoveDefinitionException, TransitionDefinitionException{
		List<Move> actions = machine.getLegalMoves(state, role);
		if(node.children.size() == 0){
			//List of moves for newstate is for single player
			List<Move> currMove = new ArrayList<Move>();
			currMove.add(actions.get(0));
			MachineState newstate = machine.getNextState(state, currMove);// = simulate(seq(actions[i]),state);
			int numChildren = (machine.getLegalMoves(newstate, role)).size();
			Node_multi newnode = new Node_multi(node, newstate, actions.get(0), numChildren);
			node.children.add(newnode);

		List<List<Move>> jointMoves = machine.getLegalJointMoves(state);
		if(node.grandchildren.size() == 0){
			List<Move> firstJointMove = jointMoves.get(0);
			int role_index = get_role_index(machine, role);
			Move my_move = firstJointMove.get(role_index);
			int numChildren = jointMoves.size() / machine.getLegalMoves(state, role).size();
			Node_multi new_blue_node = create_blue_node(node, null, my_move, numChildren);
			node.children.add(new_blue_node);

			MachineState newstate = machine.getNextState(state, firstJointMove);
			int numChildrenRed = machine.getLegalMoves(newstate, role).size();
			Node_multi new_red_node = create_red_node(new_blue_node, newstate, null, numChildrenRed);
			new_red_node.jointMove = firstJointMove;

			new_blue_node.children.add(new_red_node);
			node.grandchildren.add(new_red_node);

			//System.out.println("in expand function; first baby " + newnode.move.toString());
			return new_red_node;
		}
		List<String> childMoves = movesList(node);
		for (int i = 0; i < jointMoves.size(); i++){
			List<Move> iJointMove = jointMoves.get(i);
			int role_index = get_role_index(machine, role);
			Move my_move = iJointMove.get(role_index);
			//find the child that hasn't been expanded on yet
			//if(!childMoves.contains(my_move.toString())){

			//Check if the child needs more kids

			//Check if child exists
			if(childMoves.contains(my_move.toString())){
				//check if grandchild exists
				if(!childMoves.contains(my_move.toString())){
				List<Move> currMove = new ArrayList<Move>();
				currMove.add(jointMoves.get(i));
				MachineState newstate = machine.getNextState(state, currMove);// = simulate(seq(actions[i]),state);
				int numChildren = (machine.getLegalMoves(newstate, role)).size();
				Node_multi newnode = new Node_multi(node, newstate, actions.get(i), numChildren);
				Node_multi newnode = new Node_multi(0, 0, node, newstate, actions.get(i), numChildren);
				node.children.add(newnode);
				//System.out.println("in expand function " + newnode.move.toString());
				return newnode;
			}
		}
		System.out.println("should never get here,,,,,,,,");
		return node;
	}

	private double selectfn(Node_multi node){
		return node.utility/node.visits + 50 * Math.sqrt(Math.log(node.parent.visits)/node.visits);
	}

	private boolean backpropagate (Node_multi node, int score){
		node.my_visits.get(index)
		node.visits = node.visits + 1;
		node.utility = node.utility + score;
		if (node.parent != null) {
			backpropagate(node.parent,score);
		}
	  return true;
	}

	private int max(int a, int b) {
		if (a >= b) return a;
		return b;
	}

	public Move findBest(Role role, MachineState state) throws MoveDefinitionException, GoalDefinitionException, TransitionDefinitionException{
		StateMachine machine = getStateMachine();
		int count = 4;
		int level = 0;
		int limit = 3;
		boolean isTimeToSendMove = false;
		if(root == null){
			int numChildren = (machine.getLegalMoves(state, role)).size();
			System.out.println("MADE NEW ROOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOT!!");
			root = new Node_multi (null, state, null, numChildren);
		}
		Move bestMove = null;
		//Move last_best_move = null;
		//Node_multi parent = root;
		int numChildren = (machine.getLegalMoves(state, role)).size();
		System.out.println("******NumTotalChildren******* " + numChildren);
		while (!isTimeToSendMove){
		//for (Move m : moves){

<<<<<<< HEAD
			Node_multi selectedNode = select(root);
			Node_multi expandedNode = null;
			if (machine.isTerminal(selectedNode.state)){
=======
			Node_multi selectedNode_multi = select(root);
			Node_multi expandedNode_multi = null;
			if (machine.isTerminal(selectedNode_multi.state)){
>>>>>>> branch 'remote/master' of https://github.com/marissa3/space_jem.git
				//System.out.println("******LEAF NODE******");
<<<<<<< HEAD
				expandedNode = selectedNode;
=======
				expandedNode_multi = selectedNode_multi;
>>>>>>> branch 'remote/master' of https://github.com/marissa3/space_jem.git
				//backpropagate()
			} else {
<<<<<<< HEAD
				expandedNode = expand(selectedNode, selectedNode.state, machine, role);
=======
				expandedNode_multi = expand(selectedNode_multi, selectedNode_multi.state, machine, role);
>>>>>>> branch 'remote/master' of https://github.com/marissa3/space_jem.git
			}
			//System.out.println(expandedNode_multi.move.toString());
			List<Move> moves = machine.getLegalMoves(expandedNode_multi.state, role);
			//for (Move m : moves){
				int score = montecarlo(role, expandedNode_multi.state, machine, count);
				//System.out.println("move" + expandedNode_multi.move.toString());
				//int score = minScore(role, m, expandedNode_multi.state, machine, level, limit, count);
				backpropagate(expandedNode_multi, score);
			//}


			//
			//for (Node_multi child : parent.children){
			//System.out.println("Expanded node move " + expandedNode_multi.move);
			//}

			if (timeout - System.currentTimeMillis() < buffTime) {
				isTimeToSendMove = true;
				bestMove = highMoveUtil(root);
				break;
			}
			//return last_best_move;
			//System.out.println("parent: " + parent.utility + " " + parent.visits);
			//for (Node_multi child : parent.children){
			//System.out.println("expandedNode_multi: " + expandedNode_multi.move + expandedNode_multi.utility + " " + expandedNode_multi.visits + " = " + expandedNode_multi.utility/expandedNode_multi.visits);
			//}
			//System.out.println();
		}

		return bestMove;
	}

	private Move highMoveUtil(Node_multi parent) {
		System.out.println("***********In HMU: ");
		//if(parent.children.size() == 0){ System.out.println("children is 0"); return null;}
		Node_multi best = null;
		double score = 0;
		for(Node_multi child : parent.children){
			System.out.println("HMU: " + child.utility + " " + child.visits);
			System.out.println(child.move);
			System.out.println(child.utility/child.visits);
			if((child.utility/child.visits) >= score){
				best = child;
				//System.out.print("in highMU: ");
				System.out.println("BEST: " + best.move);
				score = child.utility/child.visits;
			}
		}
		root = best;
		root.parent = null;
		System.out.println("**********Done with HMU: \n");
		return best.move;
	}

	@Override
	public Move stateMachineSelectMove(long timeout)
		throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		this.timeout = timeout;
		MachineState state = getCurrentState();
		Role role = getRole();
		Move best = findBest(role, state);
		return best;
	}

	@Override
	public void stateMachineStop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stateMachineAbort() {
		// TODO Auto-generated method stub

	}

	@Override
	public void preview(Game g, long timeout) throws GamePreviewException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Space JEM - multi mcts";
	}

}
