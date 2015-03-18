import java.util.* ;

/**
 * 
 * Cette classe gere les transition et l'état d'un noeud de l'automate Valgen
 * @author salmon
 * @see Valgen
 */
class ValgenNode{

	public static final int MAX_CHAR = 255 ;

	public boolean isFinal                 = false ;
	@SuppressWarnings("unchecked")
	private ArrayList<ValgenNode>[] onChar = new ArrayList[MAX_CHAR] ;
	private ArrayList<ValgenNode> onEmpty  = new ArrayList<ValgenNode>() ;

	/**
	 * Pour une transition c on ajoute un noeud
	 * @param c la valeur de la transition
	 * @param next le noeud a ajouter
	 * @see ArrayList#add(Object) add 
	 */
	public void addCharEdge(char c, ValgenNode next) {
		onChar[c].add(next) ;
	}

	/**
	 * Relier le noeud sur l'objet par une epsilon-transition
	 * @param next noeud a relier
	 */
	public void addEmptyEdge(ValgenNode next) {
		onEmpty.add(next) ;
	}

	public ValgenNode () {
		for (int i = 0; i < onChar.length; i++)
			onChar[i] = new ArrayList<ValgenNode>() ;
	}

	/**
	 * On appel {@link ValgenNode#matches(String, ArrayList)}, 
	 * en initialisant visited par un ArrayList vide
	 * @param s Chaine a verifier
	 * @return reponse
	 * @see ValgenNode#matches(String, ArrayList) matches
	 */
	public boolean matches(String s) {
		return matches(s,new ArrayList<ValgenNode>()) ;
	}

	/**
	 * On appel {@link ValgenNode#generate(String)}, on initialise une chaine vide
	 * qui sera celle du resultat a retourner
	 * @return Chaine generée par generate
	 * @see ValgenNode#generate(ArrayList) generate
	 */
	public String generate(){
		return generate("");
	}

	/* -------------------------------------------------------------------- */	

	/**
	 * Tirage booleen
	 * @return vrai ou faux
	 */
	private boolean pile_ou_face(){
		Random random = new Random();
		return random.nextBoolean();
	}

	/* -------------------------------------------------------------------- */	

	/**
	 * Parcourir les voisins d'un noeud pour verifier qu'une chaine corr
	 * @param s Chaine a verifier
	 * @param visited Liste des noeuds deja parcours
	 * @return reponse
	 */
	private boolean matches(String s, ArrayList<ValgenNode> visited) {
		if (visited.contains(this)) //si on est deja passé sur le noeud, on l'ignore
			return false;
		visited.add(this) ;
		if (s.length() == 0) {
			if (isFinal)
				return true ;
			for (ValgenNode next : onEmpty) {
				if (next.matches("",visited))
					return true ;
			}
			return false ;
		} else { //recursion tant que s non vide, on lui retire la premiere lettre a chaque fois qu'on match
			int c = (int)s.charAt(0) ;
			for (ValgenNode next : onChar[c]) { // transition avec valeur
				if (next.matches(s.substring(1)))
					return true ;
			}
			for (ValgenNode next : onEmpty) { // epsilon-transition
				if (next.matches(s,visited))
					return true ;
			}
			return false ;
		}
	}

	/**
	 * On genere une chaine, qui correspond a un motif aléatoire, reconnaissable par l'automate
	 * @param visited noeuds deja visite
	 * @return String correspondant a ce motif
	 */
	private String generate(String s){
		if(isFinal)
			return s;
		while(true){ //tant qu'on a pas tiré Vrai au pile ou face on recommence
			for(int i = 0; i < onChar.length; i++)
				if(onChar[i].size() > 0)
					for(int j = 0; j < onChar[i].size(); j++)
						if(pile_ou_face())
							return onChar[i].get(j).generate(s + (char)i);												
			for(int i = 0; i < onEmpty.size(); i++)
				if(pile_ou_face())
					return onEmpty.get(i).generate(s);
		}
	}

}

/**
 * 
 * @author salmon
 *
 */
public class Valgen{
	public ValgenNode entry ;
	public ValgenNode exit ;

	public Valgen(ValgenNode entry, ValgenNode exit) {
		this.entry = entry ;
		this.exit  = exit;
	} 

	/* -------------------------------------------------------------------- */	

	/**
	 * Verifier qu'une chaine de caratere peut etre reconnue par une expression reguliere , 
	 * On se contente d'appeler le point d'entrée de l'automate, et d'itéré de voisins en voisins
	 *  en respectant les caracteres de la chaine
	 * c'est a dire pouvant atteindre un etat final de l'automate valgen
	 * @param str Chaine a verifier
	 * @return reponse booleenne
	 * @see ValgenNode#matches(String) matches
	 */
	public boolean matches(String str) {
		return entry.matches(str);
	}

	public String generate(){
		return entry.generate();
	}

	/**
	 * Affecte un caractere sur une transition entre deux noeuds
	 * @param c caractere
	 * @return la transition avec le caractere
	 */
	public static final Valgen c(char c) {
		ValgenNode entry = new ValgenNode() ;
		ValgenNode exit = new ValgenNode() ;
		exit.isFinal = true ;
		entry.addCharEdge(c,exit) ;
		return new Valgen(entry,exit) ;
	}

	/* -------------------------------------------------------------------- */

	/**
	 * On fabrique un automate simple, constitué de 2 noeuds, relié par une epsilon-transition
	 * @return un automate Valgen
	 */
	private static final Valgen e() {
		ValgenNode entry  = new ValgenNode() ;
		ValgenNode exit = new ValgenNode() ;
		entry.addEmptyEdge(exit) ;
		exit.isFinal = true ;
		return new Valgen(entry,exit) ;
	}

	/**
	 * Concatenation de deux automates, on prend la sortie du premier et on la relie au second.
	 * La sortie du 1ere automate n'est plus un etat final
	 * @param first automate Valgne
	 * @param second un autre automate Valgen
	 * @return un automate Valgen
	 */
	private static final Valgen concat(Valgen first, Valgen second) {
		first.exit.isFinal = false ;
		first.exit.addEmptyEdge(second.entry) ;
		return new Valgen(first.entry,second.exit) ;
	}

	/**
	 * Union entre deux automates
	 * @param choice1 un automate qui servira de premier choix dans l'union
	 * @param choice2 un autre automate pour le second choix
	 * @return on prend les deux automates, et on en produit un nouveau faisant l'union entre les deux
	 */	
	private static final Valgen or(Valgen choice1, Valgen choice2) {
		choice1.exit.isFinal = false ;
		choice2.exit.isFinal = false ;

		ValgenNode entry = new ValgenNode() ;
		ValgenNode exit  = new ValgenNode() ;
		exit.isFinal = true ;

		entry.addEmptyEdge(choice1.entry) ;
		entry.addEmptyEdge(choice2.entry) ;

		choice1.exit.addEmptyEdge(exit) ;
		choice2.exit.addEmptyEdge(exit) ;
		return new Valgen(entry,exit) ;
	}

	/**
	 * Sucre syntaxique, pour distinger les automates des characters et des Strings passées en paramètre
	 * Si il s'agit d'un automate, on le retourne
	 * Si il s'agit d'une chaine de caracteres on revoit un automate qui découpe cette chaine caracteres par caracteres entre des etats
	 * Si il s'agit d'un caractere, on renvoit un automate qui fait la transition entre deux noeuds sur ce caractere
	 * Sinon on renvoit une erreur car on n'a pas reconnu le terme
	 *
	 * @param o un Object
	 * @return un automate
	 * @throws RuntimeException
	 */
	private static final Valgen re(Object o) {
		if (o instanceof Valgen){
			return (Valgen)o ;
		}
		else if (o instanceof Character){
			return c((Character)o) ;
		}
		else if (o instanceof String){
			return fromString((String)o) ;
		}
		else {
			throw new RuntimeException("bad regexp") ;
		}
	}

	/**
	 * On execute une operation bouclable de Valgen sur des parametres
	 * @param id_op l'indice de l'operation, soit l'union, soit une concatenation
	 * @param rexps Object correspondant a Valgen, ou a String ou a Character
	 * @return l'automate de l'operation
	 */
	private static final Valgen loop(int id_op, Object[] rexps){
		Valgen exp = re(rexps[0]) ;
		for (int i = 1; i < rexps.length; i++) {
			switch(id_op){
			case 0: // union
				exp = or(exp, re(rexps[i]));
				break;
			case 1: // concat			
				exp = concat(exp, re(rexps[i]));
				break;
			default: // errors
				break;
			}
		}
		return exp;
	}

	/* -------------------------------------------------------------------- */

	/**
	 * Facade pour la repetion
	 * On creer un automate a partir de l'objet rexps, où le final et 
	 * @param rexps l'expression a repeter
	 * @return l'automate Valgen
	 */
	public static final Valgen r(Object rexps) {
		Valgen nfa = group(rexps);
		nfa.exit.addEmptyEdge(nfa.entry) ;
		nfa.entry.addEmptyEdge(nfa.exit) ;
		return nfa;
	}

	/**
	 * Facade pour les unions
	 * @param rexps les expressions a relier en unions
	 * @return l'automate  Valgen de l'union
	 * @see Valgen#or(Valgen, Valgen) or
	 */
	public static final Valgen o(Object ... rexps) {
		return loop(0, rexps);
	}

	/**
	 * @param rexps des automates
	 * @return un automate qui est la concatenation, dans l'ordre, des automates passés en parametre
	 * @see Valgen#concat(Valgen, Valgen) concat
	 */
	public static final Valgen group(Object ... rexps) {
		return loop(1, rexps);
	}

	/**
	 * On découpe une chaine caractere par caractere, en groupant les etats qu'on obtient
	 * @param str la chaine a decouper
	 * @return l'automate qui concatene les caracteres sur des transitions
	 */
	private static final Valgen fromString(String str) {

		if (str.length() == 0){
			return e() ;
		}
		else{
			return group(re(str.charAt(0)),fromString(str.substring(1))) ;
		}
	}

	public static void main(String[] args) {

		Valgen pat = group("foo",o("bar", "foo"), r("6")) ;
		String[] strings =
			{ "foo" , "bar" ,
				"foobar", "farboo", "boofar" , "barfoo" ,
				"foofoobarfooX" ,
				"foofoobarfoomdr" ,
				"foofoobarmdr",
				"aaa",
				"a"
			} ;
		for(int i = 0; i < 10; i++){
			System.out.println("--> " + pat.generate());
		}
		//for (String s : strings) {
		//	System.out.println(s + "\t:\t" +pat.matches(s)) ;
		//}
	}
}
