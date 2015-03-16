import java.util.* ;
class ValgenState
{

    public static final int MAX_CHAR = 255 ;

	    public boolean isFinal               = false ;
    private ArrayList<ValgenState> onChar[] = new ArrayList[MAX_CHAR] ;
    private ArrayList<ValgenState> onEmpty  = new ArrayList() ;

    public void addCharEdge(char c, ValgenState next) {
		onChar[(int)c].add(next) ;
    }

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

		if (visited.contains(this)) 

		    return false ;

		visited.add(this) ;

		if (s.length() == 0) {

		    if (isFinal)
				return true ;

		    for (ValgenState next : onEmpty) {
			if (next.matches("",visited))
			    return true ;
		    }
		    return false ;
		} else {
		    int c = (int)s.charAt(0) ;

		    for (ValgenState next : onChar[c]) {
				if (next.matches(s.substring(1)))
				    return true ;
		    }

		    for (ValgenState next : onEmpty) {
			if (next.matches(s,visited))
			    return true ;
		    }
		    return false ;
		}
    }
}

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

    public static final Valgen c(char c) {
	ValgenState entry = new ValgenState() ;
	ValgenState exit = new ValgenState() ;
	exit.isFinal = true ;
	entry.addCharEdge(c,exit) ;
	return new Valgen(entry,exit) ;
    }

    public static final Valgen e() {
	ValgenState entry  = new ValgenState() ;
	ValgenState exit = new ValgenState() ;
	entry.addEmptyEdge(exit) ;
	exit.isFinal = true ;
	return new Valgen(entry,exit) ;
    }

    public static final Valgen rep(Object nfa) {
	nfa.exit.addEmptyEdge(nfa.entry) ;
        nfa.entry.addEmptyEdge(nfa.exit) ;
	return nfa ;	
    }

    public static final Valgen g(Object first, Valgen second) {
	first.exit.isFinal = false ;
	second.exit.isFinal = true ;
	first.exit.addEmptyEdge(second.entirey) ;
	return new Valgen(first.entry,second.exit) ;
    }

    public static final Valgen or(Object choice1, Object choice2) {
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
