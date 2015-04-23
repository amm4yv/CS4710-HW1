import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

public class HW1 {

	private static ArrayList<Variable> variables;
	private static ArrayList<Variable> facts;
	private static ArrayList<Rule> rules;

	public static void main(String[] args) {

		variables = new ArrayList<Variable>();
		facts = new ArrayList<Variable>();
		rules = new ArrayList<Rule>();

		Scanner command = new Scanner(System.in);
		String input;

		do {
			input = command.next();

			if (input.equalsIgnoreCase("Teach")) {
				input = command.nextLine();
				if (input.contains("=")) {
					String[] info = input.split("=");
					String name = info[0].trim();
					String value = info[1].trim();

					// Value contains "", meaning it is declaring a variable
					if (value.contains("\"")) {
						Variable v = new Variable(name, value);
						variables.add(v);

					}
					// Otherwise, it is teaching an existing variable a truth
					// assignment
					else {
						for (int i = 0; i < variables.size(); i++) {
							if (variables.get(i).name.equals(name)) {
								boolean truth = Boolean.parseBoolean(value);
								//Fact f = facts.get(i);
								if (truth && !facts.contains(variables.get(i))) {
									facts.add(variables.get(i));
								} else if (!truth && facts.contains(variables.get(i))) {
									facts.remove(variables.get(i));
								}
								break;
							}
						}
					}
				}
				// input wants to create a rule
				else if (input.contains("->")) {
					String[] info = input.split("->");
					createNewRule(info[0].trim(), info[1].trim());
				}
			}
			// List
			else if (input.equalsIgnoreCase("List")) {
				list();
				command.nextLine();
			}
			// Learn
			else if (input.equalsIgnoreCase("Learn")) {
				learn();
				command.nextLine();
			}
			
			else if (input.equalsIgnoreCase("Query")) {
				String expression = command.nextLine().trim();
				Node n = makeTree(tokenize(expression), false);
				System.out.println(evaluate(n, facts));
			}
			
			else if (input.equalsIgnoreCase("Why")) {
				String expression = command.nextLine().trim();
				Node n = makeTree(tokenize(expression), false);
				ArrayList<String> reasoning = new ArrayList<String>();
				boolean result = why(n, reasoning);
				System.out.println(result);
								
				for (int i = 0; i < reasoning.size(); i++) {
					for (int j = 0; j < reasoning.size(); j++) {
						if (reasoning.get(i).equals(reasoning.get(j)) && i != j) {
							reasoning.remove(j);
							j--;
						}
					}
				}
				
				for (String reason : reasoning) {
					System.out.println(reason);
				}
				
				
			}
			else {
				input = "Exit";
			}

		} while (input != "Exit");
		
		command.close();

	}
	

	public static boolean baseNode(Node n) {
		String variable = n.expression;
		for (Rule r : rules) {
			if (r.getConsequence().equals(variable)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean trueNode(Node n) {
		String variable = n.expression;
		for (Variable f : facts) {
			if (f.name.equals(variable)) {
				return true;
			}
		}
		return false;
	}
	
	public static ArrayList<String> getExpression(Node n) {
		String variable = n.expression;
		ArrayList<String> array = new ArrayList<String>();
		for (Rule r : rules) {
			if (r.getConsequence().equals(variable)) {
				array.add(r.getCondition());
			}
		}
//		System.out.println(array);
		return array;
	}
	
	public static boolean why(Node n, ArrayList<String> reasoning) {
		
		if (n.expression.equals("&")) {
			// If first was found to be false, wouldn't evaluate second, so broke it up
			boolean truth1 = why(n.left, reasoning);
			boolean truth2 = why(n.right, reasoning);
			boolean truth = truth1 && truth2;
			if (truth)
				reasoning.add("I THUS KNOW THAT " + toString(n));
			else
				reasoning.add("THUS I CANNOT PROVE THAT " + toString(n));
			return truth;
			
		}
		else if (n.expression.equals("|")) {
			// If first was found to be true, wouldn't evaluate second, so broke it up
			boolean truth1 = why(n.left, reasoning);
			boolean truth2 = why(n.right, reasoning);
			boolean truth = truth1 || truth2;
			if (truth)
				reasoning.add("I THUS KNOW THAT " + toString(n));
			else
				reasoning.add("THUS I CANNOT PROVE THAT " + toString(n));
			return truth;
		}
		else if (n.expression.equals("!")) {
			boolean truth = !why(n.right, reasoning);
			if (truth)
				reasoning.add("I THUS KNOW THAT " + toString(n));
			else
				reasoning.add("THUS I CANNOT PROVE THAT " + toString(n));
			return truth;
		}
		else if (baseNode(n)) {
			if (trueNode(n)) {
				reasoning.add("I KNOW THAT " + returnValue(n));
				return true;
			} else {
				reasoning.add("I KNOW IT IS NOT TRUE THAT " + returnValue(n));
				return false;
			}
		}
		else {
			ArrayList<String> array = getExpression(n);
			ArrayList<String> temp = reasoning;
			for (String exp : array) {
				Node node = makeTree(tokenize(exp), false);
				if (why(node, reasoning)) {	
					reasoning = temp; //Back to original
					why(node, reasoning); //Add the node's true reasoning
					reasoning.add("BECAUSE " + toString(node) + " I KNOW THAT " + returnValue(n));
					return true;
				} else {
					//We want all reasons it is false
					reasoning.add("BECAUSE IT IS NOT TRUE THAT " + toString(node) + " I CANNOT PROVE " + returnValue(n));				
				}
			}
		}
		return false;
	}
	
	
	public static String returnValue(Node n) {
		String name = n.expression;
		for (int i = 0; i < variables.size(); i++) {
			if (name.equals(variables.get(i).name)) {
				return variables.get(i).value.substring(1, variables.get(i).value.length()-1);
			}
		}
		System.out.println(n.expression);
		return "not correct";
	}
	
	
	
	public static String toString(Node n) {
				
		if (n != null) { 
			
			String s = n.expression;

			if (!s.equals("&") && !s.equals("|") && !s.equals("!")
					&& !s.equals("") && !s.equals("(") && !s.equals(")")) {
				
				for (Variable v : variables) {
					if (s.equals(v.name)) {
						String name = v.value.substring(1,
								v.value.length() - 1);
						if (n.parentheses)
							name = "(" + name + ")";
						return name;
					}
				}

			}
			
			String result = (toString(n.left) + n.expression + toString(n.right)).replace("&", " AND ").replace("|", " OR ")
					.replace("!", "NOT ");
			
			if (n.parentheses)
				result = "(" + result + ")";
			
			return result;
		}
		
		return "";

	}
	

	private static void learn() {
		boolean ruleApplied = true;
		while (ruleApplied) {
			ruleApplied = false;
			for (Rule r : rules) {
				Node n = makeTree(tokenize(r.getCondition()), false);
				if (evaluate(n, facts)) {
					String cond = r.getConsequence();
					for (Variable v : variables) {
						if (cond.equals(v.name) && !facts.contains(v)) {
							facts.add(v);
							ruleApplied = true;
							break;
						}
					}
				}
			}
		}
		
	}
	
	public static boolean evaluate(Node n, ArrayList<Variable> facts) {
		if (evaluateNode(n, facts)) {
			return true;
		}
		else if (n.expression.equals("&")) {
			return (evaluate(n.left, facts) && evaluate(n.right, facts));
		}
		else if (n.expression.equals("|")) {
			return (evaluate(n.left, facts) || evaluate(n.right, facts));
		}
		else if (n.expression.equals("!")) {
			return !(evaluate(n.right, facts));
		}
		else {
			ArrayList<String> array = getExpression(n);
			for (String exp : array) {
				Node node = makeTree(tokenize(exp), false);
				return evaluate (node, facts);
			}
		}
		return false;
	}
	
	
	public static boolean evaluateNode(Node n, ArrayList<Variable> facts) {
		for (Variable v : facts) {
			if (n.expression.equals(v.name)) {
				return true;
			}
		}
		return false;
	}

	public static void list() {

		System.out.println("Variables:");
		for (Variable v : variables) {
			System.out.println("\t" + v.name + " = " + v.value);
		}
		System.out.println("Facts:");
		for (Variable v : facts) {
				System.out.println("\t" + v.name);
		}
		System.out.println("Rules");
		for (Rule r : rules) {
			System.out.println("\t" + r.getCondition() + " -> " + r.getConsequence());
		}
		
	}

	private static void createNewRule(String condition, String consequence) {
		Rule r = new Rule(condition, consequence);
		rules.add(r);
	}
	
	
	public static Node makeTree(ArrayList<String> array, boolean parentheses) {
		if (array.size() == 1 && !array.get(0).contains("("))  {
			Node n = new Node(array.get(0), null, null, parentheses);
			return n;
		}
		else if (array.size() == 1) {
			String exp = array.get(0);
			exp = exp.substring(1, exp.length()-1);
			return (makeTree(tokenize(exp), true));
		}
		else {
			for (int i = array.size() - 1; i >= 0; i--) {
				if (array.get(i).equals("|")) {
					String left = "";
					String right = "";
					for (int j = 0; j < array.size(); j++) {
						if (j < i) {
							left += array.get(j);
						}
						if (j > i) {
							right += array.get(j);
						}
					}
					Node n = new Node(array.get(i), makeTree(tokenize(left), false), makeTree(tokenize(right), false), parentheses);
					return n;
				}
			}
			for (int i = array.size() - 1; i >= 0; i--) {
				if (array.get(i).equals("&")) {
					String left = "";
					String right = "";
					for (int j = 0; j < array.size(); j++) {
						if (j < i) {
							left += array.get(j);
						}
						if (j > i) {
							right += array.get(j);
						}
					}

					Node n = new Node(array.get(i), makeTree(tokenize(left), false), makeTree(tokenize(right), false), parentheses);
					return n;
				}
			}
			for (int i = array.size() - 1; i >= 0; i--) {

				if (array.get(i).equals("!")) {
					String right = "" + array.get(i + 1);
					Node n = new Node(array.get(i), null, makeTree(tokenize(right), false), parentheses);
					for (int j = 0; j < i; j++) {
						n = new Node(array.get(i), null, n, parentheses);
					}
					return n;
				}
			}
		}
		return null;
	}
	
	public static ArrayList<String> tokenize(String exp) {
		boolean parens = false;
		boolean variable = false;
		int startParen = 0;
		int numParen = 0;
		int startVar = 0;
		ArrayList<String> result = new ArrayList<String>();

		for (int i = 0; i < exp.length(); i++) {
			if (parens == false) {
				if (variable == true) {
					if (i == exp.length() -1) {
						variable = false;
						result.add(exp.substring(startVar, i+1));
						break;
					}
					else if (exp.charAt(i) == '&' || exp.charAt(i) == '|' || exp.charAt(i) == '!' || exp.charAt(i) == '(') {
						variable = false;
						result.add(exp.substring(startVar, i));
					}
				} 
				if (variable == false) {
					if (exp.charAt(i) == '(') {
						parens = true;
						startParen = i;
						numParen = 1;
					}
					else if (exp.charAt(i) != '&' && exp.charAt(i) != '|' && exp.charAt(i) != '!') {
						if (i == exp.length() -1) {
							result.add(exp.charAt(i) + "");
							break;
						}
						variable = true;
						startVar = i;
					}
					else {
						result.add("" + exp.charAt(i));
					}
				}
			}
			else if (parens == true) {
				if (exp.charAt(i) == '(') {
					numParen ++;
				}
				if (exp.charAt(i) == ')') {
					numParen --;
					if (numParen == 0) {
						result.add(exp.substring(startParen, i+1));
						parens = false;
					}
				}
			}
			
			
			
		}
		return result;
	}

}
