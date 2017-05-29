import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;



public class Node_multi {
	public int my_visits;
	public int opp_visits;

	public double my_utilities;
	public double opp_utilities;

	public List<Node_multi> children;
	public Node_multi parent;
	public MachineState state;
	public Move move;
	public int numTotalChildren;
	//public boolean isLeaf;

	public Node_multi(int my_visits, int opp_visits, double my_utilities, double opp_utilities, Node_multi parent, MachineState state, Move move, int numTotalChildren){
		this.my_visits = my_visits;
		this.opp_visits = opp_visits;
		this.my_utilities = my_utilities;
		this.opp_utilities = opp_utilities;
		this.numTotalChildren = numTotalChildren;
		this.children = new ArrayList<Node_multi>();
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