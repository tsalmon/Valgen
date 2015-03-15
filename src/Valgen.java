/*inspire de 
http://matt.might.net/articles/implementation-of-nfas-and-regular-expressions-in-java/
*/
public class Valgen {
	private Edge start;

	Valgen(String regex){
		this.start = this.parser(regex);
	}

	/* Fonctions generales */

	public Edge parser(String regex){
		/*renvoie le noeud de depart*/
		Edge e = null;

		System.out.println(regex);
		regex = this.Addpoints(regex);
		System.out.println(regex);

		int p = 0;
		for(int i = 0; i < regex.length(); i++){
			if(regex.charAt(i) == '.'){
				p++;
			}
		}
		System.out.println("points = " + p);

		return e;
	}

	public String generate(){
		return null;
	}

	/* Fonctions d'aides */

	public String Addpoints(String regex){
		/*place des points dansla regex (positions des noeuds)*/
		for (int i = 0; i <= regex.length(); i++) {
			if(i + 1 < regex.length()){
				if(regex.charAt(i+1) == '*'){
					regex = regex.substring(0,i) + "." + regex.substring(i,regex.length());
					i+=2;
					continue;
				}
				if(regex.charAt(i+1) == '|'){
					regex = regex.substring(0,i) + "." + regex.substring(i,regex.length());
					i+=3;
					continue;
				}
			}
			regex = regex.substring(0,i) + "." + regex.substring(i,regex.length());
			i++;
		}		
		return regex;
	}

	public boolean isLetter(char c){
		switch(c){
			case '*':
			case '|':
			case ')':
			case '(': return false;
		}
		return true;
	}

	public static void main(String[] args) {
		Valgen v = new Valgen("(012)(abc)|(f(gh)*)*xy*z");
		System.out.println(v.generate());
	}
}
