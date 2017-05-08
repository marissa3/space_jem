import java.util.ArrayList;
import java.util.List;

public class Node {
	public int visits;
	public int utility;
	public List<Node> children;
	public Node parent;

	public Node(int visits, int utility, Node parent){
		this.visits = visits;
		this.utility = utility;
		this.children = new ArrayList<Node>();
		this.parent = parent;
	}

	//public int visits() {return visits;}
	//public int utility() {return utility;}
	//public List<Node> children() {return children;}
}