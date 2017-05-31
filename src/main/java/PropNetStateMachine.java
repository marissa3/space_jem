import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.propnet.architecture.Component;
import org.ggp.base.util.propnet.architecture.PropNet;
import org.ggp.base.util.propnet.architecture.components.And;
import org.ggp.base.util.propnet.architecture.components.Constant;
import org.ggp.base.util.propnet.architecture.components.Not;
import org.ggp.base.util.propnet.architecture.components.Or;
import org.ggp.base.util.propnet.architecture.components.Proposition;
import org.ggp.base.util.propnet.architecture.components.Transition;
import org.ggp.base.util.propnet.factory.OptimizingPropNetFactory;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.query.ProverQueryBuilder;


@SuppressWarnings("unused")
public class PropNetStateMachine extends StateMachine {
    /** The underlying proposition network  */
    private PropNet propNet;
    /** The topological ordering of the propositions */
    private List<Proposition> ordering;
    /** The player roles */
    private List<Role> roles;

    /**
     * Initializes the PropNetStateMachine. You should compute the topological
     * ordering here. Additionally you may compute the initial state here, at
     * your discretion.
     */
    @Override
    public void initialize(List<Gdl> description) {
        try {
            propNet = OptimizingPropNetFactory.create(description);
            roles = propNet.getRoles();
            ordering = getOrdering();
            propNet.renderToFile("propnetfile");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Computes if the state is terminal. Should return the value
     * of the terminal proposition for the state.
     */
    @Override
    public boolean isTerminal(MachineState state) {
    	markbases(state.getContents());
    	return propmarkp(propNet.getTerminalProposition());
    }

    /**
     * Computes the goal for a role in the current state.
     * Should return the value of the goal proposition that
     * is true for that role. If there is not exactly one goal
     * proposition true for that role, then you should throw a
     * GoalDefinitionException because the goal is ill-defined.
     */
    @Override
    public int getGoal(MachineState state, Role role)
            throws GoalDefinitionException {
    	  markbases(state.getContents());
    	  List<Role> roles = propNet.getRoles();
    	  Set<Proposition> rewards = new HashSet<Proposition>();
    	  rewards = propNet.getGoalPropositions().get(role);

    	  for (Proposition i : rewards){
    		  if (propmarkp(i)) {
    			  return getGoalValue(i);
    		  }
    	  }
    	  return 0;
    }

    /**
     * Returns the initial state. The initial state can be computed
     * by only setting the truth value of the INIT proposition to true,
     * and then computing the resulting state.
     */
    @Override
    public MachineState getInitialState() {
    	Proposition init = propNet.getInitProposition();
    	init.setValue(true);

    	return getStateFromBase();
    }

    /**
     * Computes all possible actions for role.
     */
    @Override
    public List<Move> findActions(Role role)
            throws MoveDefinitionException {
    	List<Role> roles = propNet.getRoles();
		Set<Proposition> legals = new HashSet<Proposition>();
		for (int i=0; i<roles.size(); i++){
		    if (role==roles.get(i)) {
		    	legals = propNet.getLegalPropositions().get(i);
		    	break;
		    }
		}
		List<Move> actions = new ArrayList<Move>();
		for (Proposition i : legals){
			Move move = getMoveFromProposition(i);
			actions.add(move);
		}
		return actions;
    }

    /**
     * Computes the legal moves for role in state.
     */
    @Override
    public List<Move> getLegalMoves(MachineState state, Role role)
            throws MoveDefinitionException {
    	markbases(state.getContents());
    	List<Role> roles = propNet.getRoles();
		Set<Proposition> legals = new HashSet<Proposition>();
		legals = propNet.getLegalPropositions().get(role);
		List<Move> actions = new ArrayList<Move>();
		for (Proposition i : legals){
			if (propmarkp(i)){
				Move move = getMoveFromProposition(i);
				actions.add(move);
			}
		}
		return actions;
    }

    public boolean propmarkp(Component p) {
      	if (propNet.getBasePropositions().containsValue(p)) return (p.getValue());
      	if (propNet.getInputPropositions().containsValue(p)) return (p.getValue());
      	if (propNet.getInitProposition().equals(p)) return (p.getValue());
	    if (p instanceof Not) {return propmarknegation(p);}
	    if (p instanceof And) {return propmarkconjunction(p);}
	    if (p instanceof Or) {return propmarkdisjunction(p);}
	    if (p instanceof Transition) return (p.getValue());
	    if (p instanceof Constant) return (p.getValue());
	    return propmarkp(p.getSingleInput()); //if view
    }

    public boolean propmarknegation(Component p){
    	return !propmarkp(p.getSingleInput());
    }

    public boolean propmarkconjunction (Component p){
    	Set<Component> sources = p.getInputs();
    	for (Component i : sources){
    		if (!propmarkp(i)) {
    			return false;
    		}
    	}
	    return true;
     }

    public boolean propmarkdisjunction (Component p){
    	Set<Component> sources = p.getInputs();
    	for (Component i : sources){
	        if (propmarkp(i)) {
	        	return true;
	        }
	    }
	    return false;
    }

    public boolean markbases(Set<GdlSentence> vector) {
    	Map<GdlSentence, Proposition> props = propNet.getBasePropositions();

    	for (GdlSentence i : props.keySet()) {
    		if (vector.contains(props.get(i).getName())){
    			props.get(i).setValue(true);
    		}
    		else {
    			props.get(i).setValue(false);
    		}
    	}
    	return true;
    }

    public boolean markactions (Set<GdlSentence> vector) {
    	Map<GdlSentence, Proposition> props = propNet.getInputPropositions();
    	for (GdlSentence i : props.keySet()){
    		if (vector.contains(props.get(i).getName())){
    			props.get(i).setValue(true);
    		}
    		else {
    			props.get(i).setValue(false);
    		}
    	}
        return true;
    }

    public boolean clearpropnet() {
    	Map<GdlSentence, Proposition> props = propNet.getBasePropositions();
    	for (int i=0; i<props.size(); i++) {
    		 props.get(i).setValue(false);
    	}
    	return true;
    }

    /**
     * Computes the next state given state and the list of moves.
     */
    @SuppressWarnings("unchecked")
	@Override
    public MachineState getNextState(MachineState state, List<Move> moves)
            throws TransitionDefinitionException {
    	Set<GdlSentence> gdls = state.getContents();
    	List <GdlSentence> gdls_move = new ArrayList<GdlSentence>();
    	gdls_move = toDoes(moves);
    	Set<GdlSentence> gdls_move_set = new HashSet<GdlSentence>();
    	for (GdlSentence g : gdls_move){
    		gdls_move_set.add(g);
    	}
    	markactions(gdls_move_set);
    	markbases(gdls);
    	Map<GdlSentence, Proposition> bases = propNet.getBasePropositions();
    	Set<GdlSentence> nexts = new HashSet<GdlSentence>();
    	for (GdlSentence i : bases.keySet()){
    	    if (propmarkp(bases.get(i).getSingleInput().getSingleInput())){ //TODO ERROR NULL??
    	    	nexts.add(bases.get(i).getName());
    	    }
   	    }
    	return new MachineState(nexts);
    }

    /**
     * This should compute the topological ordering of propositions.
     * Each component is either a proposition, logical gate, or transition.
     * Logical gates and transitions only have propositions as inputs.
     *
     * The base propositions and input propositions should always be exempt
     * from this ordering.
     *
     * The base propositions values are set from the MachineState that
     * operations are performed on and the input propositions are set from
     * the Moves that operations are performed on as well (if any).
     *
     * @return The order in which the truth values of propositions need to be set.
     */
    public List<Proposition> getOrdering()
    {
        // List to contain the topological ordering.
        List<Proposition> order = new LinkedList<Proposition>();

        // All of the components in the PropNet
        List<Component> components = new ArrayList<Component>(propNet.getComponents());

        // All of the propositions in the PropNet.
        List<Proposition> propositions = new ArrayList<Proposition>(propNet.getPropositions());

        // TODO: Compute the topological ordering.

        return order;
    }

    /* Already implemented for you */
    @Override
    public List<Role> getRoles() {
        return roles;
    }

    /* Helper methods */

    /**
     * The Input propositions are indexed by (does ?player ?action).
     *
     * This translates a list of Moves (backed by a sentence that is simply ?action)
     * into GdlSentences that can be used to get Propositions from inputPropositions.
     * and accordingly set their values etc.  This is a naive implementation when coupled with
     * setting input values, feel free to change this for a more efficient implementation.
     *
     * @param moves
     * @return
     */
    private List<GdlSentence> toDoes(List<Move> moves)
    {
        List<GdlSentence> doeses = new ArrayList<GdlSentence>(moves.size());
        Map<Role, Integer> roleIndices = getRoleIndices();

        for (int i = 0; i < roles.size(); i++)
        {
            int index = roleIndices.get(roles.get(i));
            doeses.add(ProverQueryBuilder.toDoes(roles.get(i), moves.get(index)));
        }
        return doeses;
    }

    /**
     * Takes in a Legal Proposition and returns the appropriate corresponding Move
     * @param p
     * @return a PropNetMove
     */
    public static Move getMoveFromProposition(Proposition p)
    {
        return new Move(p.getName().get(1));
    }

    /**
     * Helper method for parsing the value of a goal proposition
     * @param goalProposition
     * @return the integer value of the goal proposition
     */
    private int getGoalValue(Proposition goalProposition)
    {
        GdlRelation relation = (GdlRelation) goalProposition.getName();
        GdlConstant constant = (GdlConstant) relation.get(1);
        return Integer.parseInt(constant.toString());
    }

    /**
     * A Naive implementation that computes a PropNetMachineState
     * from the true BasePropositions.  This is correct but slower than more advanced implementations
     * You need not use this method!
     * @return PropNetMachineState
     */
    public MachineState getStateFromBase()
    {
        Set<GdlSentence> contents = new HashSet<GdlSentence>();
        for (Proposition p : propNet.getBasePropositions().values())
        {
            p.setValue(p.getSingleInput().getValue());
            if (p.getValue())
            {
                contents.add(p.getName());
            }

        }
        return new MachineState(contents);
    }
}