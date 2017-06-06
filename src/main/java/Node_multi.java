import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;



public class Node_multi {
	public int visits;
	public double utility;
	public List<Node_multi> children;
	public int numTotalGrandChildren;
	public List<Node_multi> grandchildren;
	public Node_multi parent;
	public MachineState state;
	public Move move;
	public List<Move> jointMove;
	public int numTotalChildren;
	//public boolean isLeaf;

	public Node_multi(int visits, double utility, Node_multi parent, MachineState state, Move move, int numTotalChildren){
		this.visits = visits;
		this.utility = utility;
		this.numTotalChildren = numTotalChildren;
		this.children = new ArrayList<Node_multi>();
		//this.numTotalGrandChildren = numTotalGrandChildren;
		this.grandchildren = new ArrayList<Node_multi>();
		this.parent = parent;
		this.state = state;
		this.move = move;
	}

	public Node_multi(int visits, double utility, Node_multi parent, MachineState state, Move move, int numTotalChildren, List<Move> jointMove){
		this.visits = visits;
		this.utility = utility;
		this.numTotalChildren = numTotalChildren;
		this.children = new ArrayList<Node_multi>();
		this.grandchildren = new ArrayList<Node_multi>();
		this.parent = parent;
		this.state = state;
		this.move = move;
		this.jointMove = jointMove;
	}

	public Node_multi(int visits, double utility, Node_multi parent, MachineState state, Move move, int numTotalChildren, List<Move> jointMove, int numTotalGrandChildren){
		this.visits = visits;
		this.utility = utility;
		this.numTotalChildren = numTotalChildren;
		this.children = new ArrayList<Node_multi>();
		this.numTotalGrandChildren = numTotalGrandChildren;
		this.grandchildren = new ArrayList<Node_multi>();
		this.parent = parent;
		this.state = state;
		this.move = move;
		this.jointMove = jointMove;
	}




	//public Node(int visits2, int utility2, Node node, MachineState newstate, Move move2, int numChildren,
		//	boolean isLeaf2) {
		// TODO Auto-generated constructor stub
	//}

	//public int visits() {return visits;}
	//public int utility() {return utility;}
	//public List<Node> children() {return children;}
}