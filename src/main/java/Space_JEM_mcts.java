import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class Space_JEM_mcts extends StateMachineGamer {

	@Override
	public StateMachine getInitialStateMachine() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// TODO Auto-generated method stub

	}

	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// TODO Auto-generated method stub
		return null;
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
		return null;
	}

/*	@Override
	public StateMachine getInitialStateMachine() {
		// TODO Auto-generated method stub
		return new CachedStateMachine(new ProverStateMachine());
	}

	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// TODO Auto-generated method stub

	}

	public int minScore(Role role, Move m, MachineState state, StateMachine machine, int level, int limit) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
		List<Role> allroles = machine.getRoles();
		//System.out.println("before opponent check");
		//System.out.println(allroles.toString());
		//System.out.println(role.toString());
		//System.out.println("after opponent check");
		//System.out.println("index 0: " + allroles.get(0).getName());
		//System.out.println("index 1: " + allroles.get(1).getName());
		//System.out.println("role: " + role.getName());
		Role opponent = null;
		if (allroles.get(0).getName().equals(role.getName())){
			opponent = allroles.get(1);
		} else {
			opponent = allroles.get(0);
		}
		//System.out.println("opp: " + opponent.toString());
		int score = 100;
		List<Move> moves = machine.getLegalMoves(state, opponent);
		//List<List<Move>> moves = machine.getLegalJointMoves(state, opponent, m);
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
			int result = maxScore(role, newState, machine, level + 1, limit);
			if (result == 0) return 0;
			if (result < score) score = result;
		}
		return score;
	}
	private int min(int a, int b) {
		if (a <= b) return a;
		return b;
	}

	public int maxScore(Role role, MachineState state, StateMachine machine, int level, int limit) throws MoveDefinitionException, GoalDefinitionException, TransitionDefinitionException
	{
		if (machine.isTerminal(state)){
			return machine.getGoal(state, role);
		}
		List<Role> allroles = machine.getRoles();
		Role opponent = null;
		if (allroles.get(0).getName().equals(role.getName())){
			opponent = allroles.get(1);
		} else {
			opponent = allroles.get(0);
		}
		if(level >= limit) {
			int count = 0;
			return montecarlo(role, state, machine, count);
		}
		int score = 0;
		//actions list; find legals
		List<Move> moves = machine.getLegalMoves(state, role);

		for (Move m : moves){
			//List<Move> ms = new ArrayList<Move>();
			//ms.add(m);
			int result = minScore(role, m, state, machine, level, limit);
			if(result == 100) return 100;
			if (result > score) score = result;
		}
		return score;
	}

	private int montecarlo(Role role, MachineState state, StateMachine machine, int count) {
		int total = 0;
		for(int i = 0; i < count; i++){
			total += depthcharge(role, state, machine);
		}
		return total/count;
	}

	private int depthcharge(Role role, MachineState state, StateMachine machine) {
		if (machine.isTerminal(state)){
			return machine.getGoal(state, role);
		}
		List<Move> m = new ArrayList<Move>();
		List<Role> roles = machine.getRoles();
		Role opponent = null;
		if (roles.get(0).getName().equals(role.getName())){
			opponent = roles.get(1);
		} else {
			opponent = roles.get(0);
		}
		for(int i = 0; i < roles.size(); i++){
			List<Move> moves= machine.getLegalMoves(state, role);
			m.add(moves.get((int)(Math.random())%moves.size()));
		}
		//MachineState newState = simulate(m, state);
		//return depthcharge(role, newState, machine);
	}

	function select (node){
		if (node.visits==0) {return node;}
		for (var i=0; i<node.children.length; i++){
			if (node.children[i].visits==0) {
				return node.children[i];
			}
		}
		int score = 0;
		result = node;
		for (var i=0; i<node.children.length; i++){
			int newscore = selectfn(node.children[i]);
			if (newscore>score){
				score = newscore;
				result=node.children[i];
			}
		}
		return select(result);
	}

	function expand (node){

		var actions = findlegals(role, node.state, game);
		for (var i=0; i<actions.length; i++){
			var newstate = simulate(seq(actions[i]),state);
			var newnode = makenode(newstate,0,0,node,seq());
			node.children[node.children.length] = newnode;
		}
	  return true;
	}

	function selectfn(node){
		return node.utility/node.visits+Math.sqrt(2*Math.log(node.parent.visits)/node.visits);
	}

	function backpropagate (node,score){
		node.visits = node.visits+1;
		node.utility = node.utility+score;
		if (node.parent) {
			backpropagate(node.parent,score);
		}
	  return true;
	}


	//mobility
	private int evalfnMobility(Role role, MachineState state, StateMachine machine) throws MoveDefinitionException {
		List<Move> moves = machine.getLegalMoves(state, role);
		List<Move> feasibleMoves = machine.findActions(role);
		return (moves.size()/feasibleMoves.size() * 100);
	}

	//opponent mobility
	private int evalfnOppMobility(Role opp, MachineState state, StateMachine machine) throws MoveDefinitionException {
		List<Move> moves = machine.getLegalMoves(state, opp);
		List<Move> feasibleMoves = machine.findActions(opp);
		return 100 - (moves.size()/feasibleMoves.size() * 100);
	}

	//focus
	private int evalfnFocus(Role role, MachineState state, StateMachine machine) throws MoveDefinitionException {
		List<Move> moves = machine.getLegalMoves(state, role);
		List<Move> feasibleMoves = machine.findActions(role);
		return (100 - moves.size()/feasibleMoves.size() * 100);
	}
	private int max(int a, int b) {
		if (a >= b) return a;
		return b;
	}

	public Move findBest(Role role, MachineState state) throws MoveDefinitionException, GoalDefinitionException, TransitionDefinitionException{
		StateMachine machine = getStateMachine();
		List<Move> moves = machine.getLegalMoves(state, role);
		//System.out.println("print moves in findbest");
		int limit = 5;
		int level = 0;
		int score = 0;
		for(Move m : moves){
			//System.out.println(m.toString());
		}
		Move move = null;
		for (Move m : moves){
			//System.out.println("new move");
			int result = minScore(role, m, state, machine, level, limit);
			//line below should technically be removed
			if (result == 100) return m;
			if (result >= score){
				//System.out.println(result);
				score = result;
				move = m;
			}
		}
		return move;
	}

	@Override
	public Move stateMachineSelectMove(long timeout)
		throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		MachineState state = getCurrentState();
		Role role = getRole();
		Move best = findBest(role, state);
		System.out.println(best);
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
		return "Space JEM - mcts";
	}
*/
}
