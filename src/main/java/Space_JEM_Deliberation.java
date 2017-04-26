import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

public class Space_JEM_Deliberation extends StateMachineGamer {

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

	private boolean timeLimitExceeded(long timeout){
		long minTime = 8;
		if(timeout - System.currentTimeMillis() > minTime) return false;
		return true;

	}

	public int maxScore(Role role, MachineState state, StateMachine machine, long timeout, int level, int limit) throws MoveDefinitionException, GoalDefinitionException, TransitionDefinitionException
	{

		if (machine.isTerminal(state)){
			return machine.getGoal(state, role);
		}
		if(level >= limit){
			Random rand = new Random();
			int rnum = rand.nextInt(2);
			System.out.println("chosen func: " + rnum);
			if (rnum == 0){
				return evalfnMobility(role, state, machine);
			} else {
				return evalfnFocus(role, state, machine);
			}
		}
		List<Move> moves = machine.getLegalMoves(state, role);
		int score = 0;
		System.out.println("print moves in maxScore");
		for(Move m : moves){
			System.out.println(m.toString());
		}
		for (Move m : moves){
			List<Move> ms = new ArrayList<Move>();
			ms.add(m);
			int result = maxScore(role, machine.getNextState(state, ms), machine, timeout, level + 1, limit);
			if (result > score) score = result;
		}
		return  score;
	}

	//mobility
	private int evalfnMobility(Role role, MachineState state, StateMachine machine) throws MoveDefinitionException {
		List<Move> moves = machine.getLegalMoves(state, role);
		List<Move> feasibleMoves = machine.findActions(role);
		return (moves.size()/feasibleMoves.size() * 100);
	}

	//focus
	private int evalfnFocus(Role role, MachineState state, StateMachine machine) throws MoveDefinitionException {
		List<Move> moves = machine.getLegalMoves(state, role);
		List<Move> feasibleMoves = machine.findActions(role);
		return (100 - moves.size()/feasibleMoves.size() * 100);
	}
	public Move findBest(Role role, MachineState state, long timeout) throws MoveDefinitionException, GoalDefinitionException, TransitionDefinitionException{
		StateMachine machine = getStateMachine();
		List<Move> moves = machine.getLegalMoves(state, role);
		System.out.println("print moves in findbest");
		for(Move m : moves){
			System.out.println(m.toString());
		}
		int level = 0;
		int limit = 6;
		Move move = moves.get(0);
		int score = 0;
		for (Move m : moves){
			if(timeLimitExceeded(timeout)){
				return move;
			}
			System.out.println("new move");
			List<Move> ms = new ArrayList<Move>();
			ms.add(m);
			MachineState nextState = machine.getNextState(state, ms);
			int result = maxScore(role, nextState, machine, timeout, level, limit);
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
		return findBest(role, state, timeout);
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
		return "Space JEM - Delib";
	}

}
