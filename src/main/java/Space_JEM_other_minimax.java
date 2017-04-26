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

public class Space_JEM_other_minimax extends StateMachineGamer {

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

	public int minScore(Role role, Move m, MachineState state, StateMachine machine) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
		List<Role> allroles = machine.getRoles();
		System.out.println("before opponent check");
		System.out.println(allroles.toString());
		System.out.println(role.toString());
		System.out.println("after opponent check");
		System.out.println("index 0: " + allroles.get(0).getName());
		System.out.println("index 1: " + allroles.get(1).getName());
		System.out.println("role: " + role.getName());
		Role opponent = null;
		if (allroles.get(0).getName().equals(role.getName())){
			opponent = allroles.get(1);
		} else {
			opponent = allroles.get(0);
		}
		System.out.println("opp: " + opponent.toString());
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
			int result = maxScore(role, newState, machine);
			if (result < score) score = result;
		}
		return score;
	}
	private int min(int a, int b) {
		if (a <= b) return a;
		return b;
	}

	public int maxScore(Role role, MachineState state, StateMachine machine) throws MoveDefinitionException, GoalDefinitionException, TransitionDefinitionException
	{
		if (machine.isTerminal(state)){
			return machine.getGoal(state, role);
		}
		int score = 0;
		//actions list; find legals
		List<Move> moves = machine.getLegalMoves(state, role);
		System.out.println("print moves in maxScore");
		for(Move m : moves){
			System.out.println(m.toString());
		}
		for (Move m : moves){
			//List<Move> ms = new ArrayList<Move>();
			//ms.add(m);
			int result = minScore(role, m, state, machine);
			if (result > score) score = result;
		}
		return score;
	}

	private int max(int a, int b) {
		if (a >= b) return a;
		return b;
	}

	public Move findBest(Role role, MachineState state) throws MoveDefinitionException, GoalDefinitionException, TransitionDefinitionException{
		StateMachine machine = getStateMachine();
		List<Move> moves = machine.getLegalMoves(state, role);
		System.out.println("print moves in findbest");
		int score = 0;
		for(Move m : moves){
			System.out.println(m.toString());
		}
		Move move = null;
		for (Move m : moves){
			System.out.println("new move");
			int result = minScore(role, m, state, machine);
			//line below should technically be removed
			if (result == 100) return m;
			if (result > score){
				System.out.println(result);
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
		return findBest(role, state);
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
		return "Space JEM - alphabeta";
	}

}
