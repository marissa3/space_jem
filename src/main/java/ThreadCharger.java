import java.util.List;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class ThreadCharger extends Thread {
	Node_multi node;
	Role role;
	MachineState state;
	StateMachine machine;
	int dc_val = 0;
	int numDepthCharges = 0;

	ThreadCharger(Role role, MachineState state, StateMachine machine){
		this.role = role;
		this.state = state;
		this.machine = machine;
	}

	@Override
	public void run() {
		int val = 0;
		try {
			val = depthcharge(role, state, machine);
		} catch (GoalDefinitionException | MoveDefinitionException | TransitionDefinitionException e) {
			e.printStackTrace();
		}
		dc_val = val;
	}

	public int getDepthchargeValue(){
		return dc_val;
	}

	public int getNumDepthcharges(){
		return numDepthCharges;
	}

	private int depthcharge(Role role, MachineState state, StateMachine machine) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		if (machine.isTerminal(state)){
			numDepthCharges++;
			return machine.getGoal(state, role);
		}
		List<Move> m = machine.getRandomJointMove(state);
		MachineState newState = machine.getNextState(state, m);
		return depthcharge(role, newState, machine);
	}
}
