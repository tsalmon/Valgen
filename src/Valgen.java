import java.util.* ;

/*
 * An ValgenState is a node with a set of outgoing edges to other
 * NFAStates.
 *
 * There are two kinds of edges:
 *
 * (1) Empty edges allow the Valgen to transition to that state without
 *     consuming a character of input.
 * 
 * (2) Character-labelled edges allow the Valgen to transition to that
 *     state only by consuming the character on the label.
 *
 */
class ValgenState
{
    /*
     * WARNING:
     *
     * The maximum integer character code we'll match is 255, which
     * is sufficient for the ASCII character set.
     *
     * If we were to use this on the Unicode character set, we'd get
     * an array index out-of-bounds exception.
     *
     * A ``proper'' implementation of this would not use arrays but
     * rather a dynamic data structure like Vector.
     */
    public static final int MAX_CHAR = 255 ;

	    public boolean isFinal               = false ;
    private ArrayList<ValgenState> onChar[] = new ArrayList[MAX_CHAR] ;
    private ArrayList<ValgenState> onEmpty  = new ArrayList() ;

    /*
     * Add a transition edge from this state to next which consumes
     * the character c.
     */
    public void addCharEdge(char c, ValgenState next) {
		onChar[(int)c].add(next) ;
    }

    /*
     * Add a transition edge from this state to next that does not
     * consume a character.
     */
    public void addEmptyEdge(ValgenState next) {
		onEmpty.add(next) ;
    }

    public ValgenState () {
	for (int i = 0; i < onChar.length; i++)
	    onChar[i] = new ArrayList() ;
    }

    public boolean matches(String s) {
		return matches(s,new ArrayList()) ;
    }

    private boolean matches(String s, ArrayList visited) {
		/*
		 * When matching, we work character by character.
		 *
		 * If we're out of characters in the string, we'll check to
		 * see if this state if final, or if we can get to a final
		 * state from here through empty edges.
		 *
		 * If we're not out of characters, we'll try to consume a
		 * character and then match what's left of the string.
		 *
		 * If that fails, we'll ask if empty-edge neighbors can match
		 * the entire string.
		 *
		 * If that fails, the match fails.
		 *
		 * Note: Because we could have a circular loop of empty
		 * transitions, we'll have to keep track of the states we
		 * visited through empty transitions so we don't end up
		 * looping forever.
		 */

		if (visited.contains(this)) 
		    /* We've found a path back to ourself through empty edges;
		     * stop or we'll go into an infinite loop. */
		    return false ;
		
		/* In case we make an empty transition, we need to add this
		 * state to the visited list. */
		visited.add(this) ;

		if (s.length() == 0) {
		    /* The string is empty, so we match this string only if
		     * this state is a final state, or we can reach a final
		     * state without consuming any input. */
		    if (isFinal)
			return true ;

		    /* Since this state is not final, we'll ask if any
		     * neighboring states that we can reach on empty edges can
		     * match the empty string. */
		    for (ValgenState next : onEmpty) {
			if (next.matches("",visited))
			    return true ;
		    }
		    return false ;
		} else {
		    /* In this case, the string is not empty, so we'll pull
		     * the first character off and check to see if our
		     * neighbors for that character can match the remainder of
		     * the string. */

		    int c = (int)s.charAt(0) ;

		    for (ValgenState next : onChar[c]) {
			if (next.matches(s.substring(1)))
			    return true ;
		    }

		    /* It looks like we weren't able to match the string by
		     * consuming a character, so we'll ask our
		     * empty-transition neighbors if they can match the entire
		     * string. */
		    for (ValgenState next : onEmpty) {
			if (next.matches(s,visited))
			    return true ;
		    }
		    return false ;
		}
    }
}

/*
 * Here, an Valgen is represented by an entry state and an exit state.
 *
 * Any Valgen can be represented by an Valgen with a single exit state by
 * creating a special exit state, and then adding empty transitions
 * from all final states to the special one.
 *
 */
public class Valgen
{
    public ValgenState entry ;
    public ValgenState exit ;

    public Valgen(ValgenState entry, ValgenState exit) {
	this.entry = entry ;
	this.exit  = exit;
    } 

    public boolean matches(String str) {
	return entry.matches(str);
    }

    /*
     * c() : Creates an Valgen which just matches the character `c'.
     */
    public static final Valgen c(char c) {
	ValgenState entry = new ValgenState() ;
	ValgenState exit = new ValgenState() ;
	exit.isFinal = true ;
	entry.addCharEdge(c,exit) ;
	return new Valgen(entry,exit) ;
    }

    /*
     * e() : Creates an Valgen which matches the empty string.
     */
    public static final Valgen e() {
	ValgenState entry  = new ValgenState() ;
	ValgenState exit = new ValgenState() ;
	entry.addEmptyEdge(exit) ;
	exit.isFinal = true ;
	return new Valgen(entry,exit) ;
    }

    public static final Valgen rep(Valgen nfa) {
	nfa.exit.addEmptyEdge(nfa.entry) ;
        nfa.entry.addEmptyEdge(nfa.exit) ;
	return nfa ;	
    }

    public static final Valgen g(Valgen first, Valgen second) {
	first.exit.isFinal = false ;
	second.exit.isFinal = true ;
	first.exit.addEmptyEdge(second.entry) ;
	return new Valgen(first.entry,second.exit) ;
    }

    public static final Valgen or(Valgen choice1, Valgen choice2) {
		choice1.exit.isFinal = false ;
		choice2.exit.isFinal = false ;
		ValgenState entry = new ValgenState() ;
		ValgenState exit  = new ValgenState() ;
		exit.isFinal = true ;
		entry.addEmptyEdge(choice1.entry) ;
		entry.addEmptyEdge(choice2.entry) ;
		choice1.exit.addEmptyEdge(exit) ;
		choice2.exit.addEmptyEdge(exit) ;
		return new Valgen(entry,exit) ;
	    }

	    /* Syntactic sugar. */
	    public static final Valgen re(Object o) {
		if (o instanceof Valgen)
		    return (Valgen)o ;
		else if (o instanceof Character)
		    return c((Character)o) ;
		else if (o instanceof String)
		    return fromString((String)o) ;
		else {
		    throw new RuntimeException("bad regexp") ;
		}
    }

    public static final Valgen or(Object... rexps) {
		Valgen exp = re(rexps[0]) ;
		for (int i = 1; i < rexps.length; i++) {
		    exp = or(exp,re(rexps[i])) ;
		}
		return exp ;
    }

    public static final Valgen g(Object... rexps) {
		Valgen exp = e() ;
		for (int i = 0; i < rexps.length; i++) {
		    exp = g(exp,re(rexps[i])) ;
		}
		return exp ;
    }

    public static final Valgen fromString(String str) {
		if (str.length() == 0)
		    return e() ;
		else
		    return g(re(str.charAt(0)),fromString(str.substring(1))) ;
    }

    public static void main(String[] args) {
	Valgen pat = g(rep(or("foo","bar")),"") ;
	String[] strings = 
	    { "foo" , "bar" , 
	      "foobar", "farboo", "boofar" , "barfoo" ,
	      "foofoobarfooX" ,
	      "foofoobarfoo" ,
	    } ;
	for (String s : strings) {
	    System.out.println(s + "\t:\t" +pat.matches(s)) ;
	}
    }
}
