import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;

public class ThreadTimer extends Thread {
	Node_multi root;
	Role role;
	MachineState state;
	StateMachine machine;
	long timeout;
	long buffTime;

	ThreadTimer(long timeout, Node_multi root, long buffTime, Role role, StateMachine machine, MachineState state){
		this.timeout = timeout;
		this.root = root;
		this.buffTime = buffTime;
		this.role = role;
		this.state = state;
		this.machine = machine;
	}

	public Move wait_and_return_move(Node_multi root){
		while (timeout - System.currentTimeMillis() > this.buffTime){
			try {
				return_best_move(root);
			    Thread.sleep(1000);                 //1000 milliseconds is one second.
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
		}
		return return_best_move(root);
	}

	private Move return_best_move(Node_multi root){
		Node_multi best = null;
		double score = 0;
		for(Node_multi child : root.children){
			if((child.utility/child.visits) >= score){
				best = child;
				score = child.utility/child.visits;
			}
		}
		System.out.println("BEST: " + best.move);
		System.out.print(best.utility / best.visits + "\n");
		return best.move;
	}
}
