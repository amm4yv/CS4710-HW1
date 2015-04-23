
public class Node {

	public String expression;
	public Node left;
	public Node right;
	public boolean parentheses;
	
	public Node(String exp, Node left, Node right, boolean parentheses) {
		this.expression = exp;
		this.left = left;
		this.right = right;
		this.parentheses = parentheses;
	}
	

}
