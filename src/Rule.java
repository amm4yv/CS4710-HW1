
public class Rule {

	private String consequence;
	private String condition;

	
	public Rule(String condition, String consequence) {
		// TODO Auto-generated constructor stub
		this.condition = condition;
		this.consequence = consequence;
	}


	public String getCondition() {
		// TODO Auto-generated method stub
		return this.condition;
	}

	public String getConsequence() {
		// TODO Auto-generated method stub
		return this.consequence;
	}

}
