package apex.symbolic;
 
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
 
public class Expression extends DefaultMutableTreeNode{ 
	public static int availableId = 0;
	public final int creationId;
	
	private static final long serialVersionUID = 1L;
	public Expression(String operator){
		this.setUserObject(operator);
		creationId = availableId++;
	}
	
	public String getContent(){
		return this.getUserObject().toString();
	}
	
	public void add(String string)
	{
		this.add(new Expression(string));
	}
	
	public Expression getChild(int childID)
	{
		return (Expression) this.getChildAt(childID);
	}
	
	public Expression clone(){
		Expression root = new Expression(this.getUserObject().toString());
		for(int i=0; i< this.getChildCount();i++){
			Expression subNode = (Expression) this.getChildAt(i);
			root.add(subNode.clone());
		}
		return root;
	}
	
	public String toYicesStatement(){
		if(this.isLeaf()){
			String result = this.getUserObject().toString();
			for(int index = 0; index < this.getChildCount(); index++){
				Expression expr = (Expression) this.getChildAt(index);
				result += " "+expr.toYicesStatement();
			}
			return result;
		}
		
		String result = "("+this.getUserObject().toString();
		for(int index = 0; index < this.getChildCount(); index++){
			Expression expr = (Expression) this.getChildAt(index);
			result += " "+expr.toYicesStatement();
		}
		return result + " )";
	}

	
	public boolean replace(String toReplace, Expression replacement){
		if(replacement == null) return false;
		boolean anyChange = false;
		int count = this.getChildCount();
		for(int i=0; i < count; i++){
			Expression expre = (Expression) this.getChildAt(i);
			if(expre.toYicesStatement().equals(toReplace)){
				this.remove(i);
				this.insert(replacement, i);
				anyChange = true;
			}else{
				anyChange = expre.replace(toReplace, replacement) || anyChange;
			}
		}
		return anyChange;
	}
	
	public boolean replace(Expression toReplace, Expression replacement){
		if(replacement == null) return false;
		boolean anyChange = false;
		int count = this.getChildCount();
		for(int i=0; i < count; i++){
			Expression expre = (Expression) this.getChildAt(i);
			if(toReplace.equals(expre)){
				this.remove(i);
				this.insert(replacement.clone(), i);
				anyChange = true;
			}else{
				anyChange = expre.replace(toReplace, replacement.clone()) || anyChange;
			}
		}
		return anyChange;
	}
	
	@Override
	public int hashCode(){
		return this.getContent().hashCode();
	}
	
	@Override
	public boolean equals(Object object){
		if( object instanceof Expression){
			Expression input = (Expression)object;
			String state1 = this.toYicesStatement();
			String state2 = input.toYicesStatement();
			boolean result = state1.equals(state2);
			return result;
		}
		return false;
	}
	
	public boolean isEquivalentTo(Expression ex) {
		Expression xe = new Expression(ex.getUserObject().toString());
		xe.add(((Expression) ex.getChildAt(1)).clone());
		xe.add(((Expression) ex.getChildAt(0)).clone());
		return (equals(ex) || equals(xe));
	}
	
	public boolean isOppsiteTo(Expression ex) {
		Expression xe = ex.getReverseCondition();
		return isEquivalentTo(xe);
	}
	
	public boolean contains(Expression containee) {
		if (this.equals(containee))
			return true;
		else {
			for (int i = 0; i < this.getChildCount(); i++) {
				Expression child  = (Expression) this.getChildAt(i);
				if (child.contains(containee))
					return true;
			}
			return false;
		}
	}
	
	public boolean contains(String s) {
		if (this.getUserObject().toString().equals(s))
			return true;
		else {
			for (int i = 0; i < this.getChildCount(); i++) {
				Expression child = (Expression) this.getChildAt(i);
				if (child.contains(s))
					return true;
			}
			return false;
		}
	}
	
	public Expression getReverseCondition() {
		String op = this.getUserObject().toString();
		if (op.equals("="))		op = "/=";
		else if (op.equals("/="))	op = "=";
		else if (op.equals("<"))	op = ">=";
		else if (op.equals("<="))	op = ">";
		else if (op.equals(">"))	op = "<=";
		else if (op.equals(">="))	op = "<";
		else return null;
		Expression result = new Expression(op);
		for (int i = 0; i < this.getChildCount(); i++) {
			Expression child = (Expression) this.getChildAt(i);
			result.add(child.clone());
		}
		return result;
	}
	
	public void toDecimal() {
		for (int i = 0; i < getChildCount(); i++) {
			Expression child = (Expression) getChildAt(i);
			if (child.getUserObject().toString().equals("#number")) {
				Expression theNumber = (Expression) child.getChildAt(0);
				String hex = theNumber.getUserObject().toString().replace("0x", "");
				int decimal = Integer.parseInt(hex, 16);
				remove(i);
				insert(new Expression(decimal + ""), i);
			}
			else child.toYiceFormat();
		}
	}
	
	public void toYiceFormat() {
		toDecimal();
	}
	
	
	
	public static String createAssertion(String yices){
		return "(assert "+yices+")";
	}

	public List<String> getYicesDefineStatements()
	{
		List<String> results = new ArrayList<String>();
		
		if (getChildCount() > 0)
		{
			for (int i = 0; i < getChildCount(); i++)
			{
				Expression child = (Expression) getChildAt(i);
				for (String childResult : child.getYicesDefineStatements())
				{
					if (!results.contains(childResult))
						results.add(childResult);
				}
			}
		}
		return results;
	}
	
	public String getYicesAssertionStatement()
	{
		//TODO
		
		return "";
	}
	
}