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

public class Space_JEM_Minimax extends StateMachineGamer {

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

	public int maxScore(Role role, MachineState state, StateMachine machine, int player) throws MoveDefinitionException, GoalDefinitionException, TransitionDefinitionException
	{
		if (machine.isTerminal(state)){
			return machine.getGoal(state, role);
		}
		List<Move> moves = machine.getLegalMoves(state, role);
		int score = 0;
		for (Move m : moves){
			List<Move> ms = new ArrayList<Move>();
			ms.add(m);
			int result = minScore(role, machine.getNextState(state, ms), machine, player);
			if (result > score) score = result;
		}
		return  score;
	}

	public int minScore(Role role, MachineState state, StateMachine machine, int player) throws MoveDefinitionException, GoalDefinitionException, TransitionDefinitionException
	{
		List<List<Move>> moves = machine.getLegalJointMoves(getCurrentState());
		int score = 0;
		for (Move m : moves.get(player)){
			List<Move> ms = new ArrayList<Move>();
			ms.add(m);
			ms.add(m);
			ms.add(Math.abs(player - 1), moves.get(Math.abs(player - 1)).get(0));
			int result = maxScore(role, machine.getNextState(state, ms), machine, player);
			if (result < score) score = result;
		}
		return  score;
	}


	public Move findBest(Role role, MachineState state) throws MoveDefinitionException, GoalDefinitionException, TransitionDefinitionException{
		StateMachine machine = getStateMachine();
		List<Move> moves = machine.getLegalMoves(state, role);
		Move move = moves.get(0);
		int score = 0;
		for (Move m : moves){
			int result = minScore(role, state, machine, 0);
			if (result > score){
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
		return "Space JEM - Minimax";
	}

}
