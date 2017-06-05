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
	int buffTime = 15000; //in milliseconds

	private Node_multi root = null;
	private int numDepthCharges = 0;


	@Override
	public StateMachine getInitialStateMachine() {
		// TODO Auto-generated method stub
		//return new CachedStateMachine(new PropNetStateMachine());
		return new CachedStateMachine(new ProverStateMachine());

	}

	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// TODO Auto-generated method stub
		root = null;
		// start creating tree

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
			numDepthCharges++;
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
		for(Node_multi gchild : node.grandchildren){
			moves.add(gchild.jointMove.toString());
		}
		return moves;
	}

	private List<String> childMovesList(Node_multi node){
		List<String> moves = new ArrayList<String>();
		for(Node_multi child : node.children){
			moves.add(child.move.toString());
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
		//System.out.println("BLUE : move = " + my_move.toString());
		return newnode;
	}

	private Node_multi create_red_node(Node_multi parent, MachineState node_state, Move my_move, int numChildren, List<Move> jointMove){
		Node_multi newnode = new Node_multi(0, 0, parent, node_state, my_move, numChildren, jointMove);
		//System.out.println("RED : move = " + jointMove.toString());
		//System.out.println("RED : state = " + node_state.toString());
		return newnode;
	}

	// expand done?
	private Node_multi expand (Node_multi node, MachineState state, StateMachine machine, Role role) throws MoveDefinitionException, TransitionDefinitionException{
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
			Node_multi new_red_node = create_red_node(new_blue_node, newstate, null, numChildrenRed, firstJointMove);

			new_blue_node.children.add(new_red_node);
			node.grandchildren.add(new_red_node);

			//System.out.println("in expand function; first baby " + newnode.move.toString());
			return new_red_node;
		}
		List<String> strJointMoves = movesList(node);
		int role_index = get_role_index(machine, role);
		for (int i = 0; i < jointMoves.size(); i++){
			List<Move> iJointMove = jointMoves.get(i);

			//Check if current joint move is in childMoves
			if(!strJointMoves.contains(iJointMove.toString())){
				Node_multi blue_node = null;
				//my_move is blue node move
				Move my_move = iJointMove.get(role_index);
				//list of all blue node moves
				List<String> strChildMoves = childMovesList(node);
				//check to see if blue node already exists
				if(!strChildMoves.contains(my_move.toString())){
					//Blue node not found; create blue node
					int numChildren = jointMoves.size() / machine.getLegalMoves(state, role).size();
					blue_node = create_blue_node(node, null, my_move, numChildren);
					node.children.add(blue_node);
				} else {
					// find corresponding blue node
					for (Node_multi blue_child : node.children){
						if (blue_child.move.toString().equals(my_move.toString())){
							blue_node = blue_child;
							break;
						}
					}
				}

				MachineState newstate = machine.getNextState(state, iJointMove);
				int numChildrenRed = machine.getLegalMoves(newstate, role).size();
				Node_multi new_red_node = create_red_node(blue_node, newstate, null, numChildrenRed, iJointMove);

				blue_node.children.add(new_red_node);
				node.grandchildren.add(new_red_node);

				return new_red_node;
			}
		}
		System.out.println("should never get here,,,,,,,,");
		return node;
	}

	private void print_grandchildren(Node_multi node){
		System.out.println("**PRINT GCHILDREN**");

		for (Node_multi gchild : node.grandchildren){
			System.out.println(gchild.jointMove);
			System.out.println(gchild.state);
			System.out.println();
		}
	}

	private Node_multi select(Node_multi node) throws MoveDefinitionException{

		//print_grandchildren(node);
		StateMachine machine = getStateMachine();
		//List<Move> actions = machine.getLegalMoves(state, role);
		int amtofgrandchildren = node.grandchildren.size();
		List<List<Move>> jointMoves = machine.getLegalJointMoves(node.state);
		if(amtofgrandchildren != jointMoves.size()){
			return node;
		}

		//System.out.println("AMT OF GCHILDREN == JOINTMOVES.SIZE");

		int score = Integer.MIN_VALUE;

		Node_multi blue_picked = null;
		for (int i = 0; i < node.children.size(); i++){
			int blue_score = (int)selectfn(node.children.get(i));
			if (blue_score >= score){
				score = blue_score;
				blue_picked = node.children.get(i);
			}
		}

		//System.out.println("Blue picked: " + blue_picked.move);

		score = Integer.MIN_VALUE;

		Node_multi red_picked = null;
		for (int i=0; i < blue_picked.children.size(); i++){
			int red_score = -(int)selectfn(blue_picked.children.get(i));
			if (red_score >= score){
				//if (!machine.isTerminal(blue_picked.children.get(i).state)){
					score = red_score;
					// result should be a red node
					red_picked = blue_picked.children.get(i);
				//}
			}
		}

		//System.out.println("Red picked: " + red_picked.jointMove);

		return select(red_picked);
	}

	private double selectfn(Node_multi node){
		return node.utility/node.visits + 50 * Math.sqrt(Math.log(node.parent.visits)/node.visits);
	}

	private boolean backpropagate (Node_multi node, int score){
		node.visits = node.visits + 1;
		node.utility = node.utility + score;
		if (node.parent != null) {
			backpropagate(node.parent,score);
		}
	  return true;
	}

	public Move findBest(Role role, MachineState state) throws MoveDefinitionException, GoalDefinitionException, TransitionDefinitionException{
		StateMachine machine = getStateMachine();
		int count = 4;
		boolean isTimeToSendMove = false;

		Move bestMove = null;
		//int numChildren = (machine.getLegalMoves(state, role)).size();
		//int numGrandchildren = machine.getLegalJointMoves(state).size();
		//System.out.println("******NumTotalChildren******* " + numChildren);
		//System.out.println("******NumTotalGrandchildren******* " + numGrandchildren);
		while (!isTimeToSendMove){

			Node_multi selectedNode_multi = select(root);
			Node_multi expandedNode_multi = null;
			if (machine.isTerminal(selectedNode_multi.state)){
				expandedNode_multi = selectedNode_multi;
			} else {
				expandedNode_multi = expand(selectedNode_multi, selectedNode_multi.state, machine, role);
			}
			//List<Move> moves = machine.getLegalMoves(expandedNode_multi.state, role);
			int score = montecarlo(role, expandedNode_multi.state, machine, count);
			backpropagate(expandedNode_multi, score);

			if (timeout - System.currentTimeMillis() < buffTime) {
				isTimeToSendMove = true;
				bestMove = highMoveUtil(root);
				break;
			}
		}

		return bestMove;
	}

	private Move highMoveUtil(Node_multi parent) {
		//System.out.println("***********In HMU: ");
		//if(parent.children.size() == 0){ System.out.println("children is 0"); return null;}
		Node_multi best = null;
		double score = 0;
		for(Node_multi child : parent.children){
			//System.out.println("HMU: " + child.utility + " " + child.visits);
			//System.out.println(child.move);
			//System.out.println(child.utility/child.visits);
			if((child.utility/child.visits) >= score){
				best = child;
				//System.out.print("in highMU: ");
				//System.out.println("BEST: " + best.move);
				score = child.utility/child.visits;
			}
		}
		//System.out.println("**********Done with HMU: \n");
		System.out.println("BEST: " + best.move);
		System.out.print(best.utility / best.visits + "\n");
		return best.move;
	}

	private void set_new_root(MachineState state){
		for (Node_multi grandchild : root.grandchildren){
			if (grandchild.state.toString().equals(state.toString())){
				root = grandchild;
				root.parent = null;
				break;
			}
		}
		return;
	}

	@Override
	public Move stateMachineSelectMove(long timeout)
		throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		numDepthCharges = 0;
		this.timeout = timeout;
		MachineState state = getCurrentState();
		Role role = getRole();

		if(root == null){
			int numChildren = (getStateMachine().getLegalMoves(state, role)).size();
			System.out.println("MADE NEW ROOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOT!!");
			root = new Node_multi (0, 0, null, state, null, numChildren);
		} else {
			set_new_root(state);
		}

		Move best = findBest(role, state);
		System.out.println("Depth charges per move: " + numDepthCharges);
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
