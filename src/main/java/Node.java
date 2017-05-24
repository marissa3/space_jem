import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;



public class Node {
	public int visits;
	public double utility;
	public List<Node> children;
	public Node parent;
	public MachineState state;
	public Move move;
	public int numTotalChildren;
	//public boolean isLeaf;

	public Node(int visits, double utility, Node parent, MachineState state, Move move, int numTotalChildren){
		this.visits = visits;
		this.utility = utility;
		this.numTotalChildren = numTotalChildren;
		this.children = new ArrayList<Node>();
		//List<Node> children = new ArrayList<Node>();
		this.parent = parent;
		this.state = state;
		this.move = move;
	}

	//public Node(int visits2, int utility2, Node node, MachineState newstate, Move move2, int numChildren,
		//	boolean isLeaf2) {
		// TODO Auto-generated constructor stub
	//}

	//public int visits() {return visits;}
	//public int utility() {return utility;}
	//public List<Node> children() {return children;}
}