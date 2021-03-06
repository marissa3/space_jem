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


public class mcts_jonas extends StateMachineGamer {
	private long timeout;
	int buffTime = 3000; //in milliseconds



	@Override
	public StateMachine getInitialStateMachine() {
		// TODO Auto-generated method stub
		//return new CachedStateMachine(new ProverStateMachine());
		return new CachedStateMachine(new PropNetStateMachine());
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

	private Node select(Node node){
		if (node.visits == 0) {return node;}
		for (int i = 0; i<node.children.size(); i++){
			if (node.children.get(i).visits == 0) {
				return node.children.get(i);
			}
		}
		double score = 0;
		Node result = node;
		for (int i=0; i < node.children.size(); i++){
			double newscore = selectfn(node.children.get(i));
			if (newscore > score){
				score = newscore;
				result = node.children.get(i);
			}
		}
		return select(result);
	}

	private boolean expand (Node node, StateMachine machine, Role role){

		//var actions = findlegals(role, node.state, game);
		List<Move> actions = machine.getLegalMoves(node.state, role);
		for (int i=0; i<actions.size(); i++){
			MachineState newstate = machine.getNextState(node.state, actions.get(i));

			Node newNode = new Node(newstate, 0, 0, node, seq());
			//var newnode = makenode(newstate,0,0,node,seq());
			node.children[node.children.length] = newnode;
		}
	  return true;
	}

	private double selectfn(Node node){
		return node.utility/node.visits + Math.sqrt(2*Math.log(node.parent.visits)/node.visits);
	}

	private boolean backpropagate (Node node,int score){
		node.visits = node.visits+1;
		node.utility = node.utility+score;
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
		List<Move> moves = machine.getLegalMoves(state, role);
		int limit = 2;
		int level = 0;
		int score = 0;

		int count = 4;
		boolean isTimeToSendMove = false;
		Move move = null;
		//Move last_best_move = null;
		while (!isTimeToSendMove){
			for (Move m : moves){
				if (timeout - System.currentTimeMillis() < buffTime) {
					isTimeToSendMove = true;
					break;
				}
				int result = minScore(role, m, state, machine, level, limit, count);
				//line below should technically be removed
				if (result == 100) return m;
				if (result >= score){
					score = result;
					move = m;
				}
			}
			//if (!isTimeToSendMove) last_best_move = move;
			//if (count % 4 == 0){
				limit ++;
				System.out.println("curr lim = " + limit);
			//}
			//count += 2;
			//System.out.println("curr count = " + count);

		}
		//return last_best_move;
		return move;
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
		return "Space JEM - mcs";
	}

}
