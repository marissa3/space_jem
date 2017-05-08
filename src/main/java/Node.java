import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.statemachine.MachineState;



public class Node {
	public int visits;
	public double utility;
	public List<Node> children;
	public Node parent;
	public MachineState state;

	public Node(int visits, double utility, Node parent, MachineState state){
		this.visits = visits;
		this.utility = utility;
		this.children = new ArrayList<Node>();
		this.parent = parent;
		this.state = state;
	}

	//public int visits() {return visits;}
	//public int utility() {return utility;}
	//public List<Node> children() {return children;}
}