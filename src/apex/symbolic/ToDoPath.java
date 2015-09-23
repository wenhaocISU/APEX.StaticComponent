package apex.symbolic;

import java.util.ArrayList;

public class ToDoPath {

	/**
	 * Attributes in exploring stage
	 * */
	int startingStmtID = 0;
	int endingStmtID = -1;
	boolean isLegit = true;
	boolean explored = false;
	
	/**
	 * members in exploring stage
	 * */
	int orderIndex = -1;
	ArrayList<String> branchOrders = new ArrayList<String>();
	int choiceIndex = 0;
	ArrayList<String> branchChoices = new ArrayList<String>();
	ArrayList<String> execLog = new ArrayList<String>();

	
	ToDoPath(int startingStmtID, int endingStmtID)
	{
		this.startingStmtID = startingStmtID;
		this.endingStmtID = endingStmtID;
	}
	
	String getOrder(String stmtID)
	{
		orderIndex++;
		if (orderIndex >= branchOrders.size())
		{
			for (String choice : branchOrders)
			{
				if (choice.startsWith(stmtID + ","))
				{
					// this means we have looped back to this if statement
					String oldDirection = choice.split(",")[1];
					if (oldDirection.equals("jump"))
						return "forceflow";
					if (oldDirection.equals("flow"))
						return "forcejump";
				}
			}
			return "";
		}
		String branchChoice = this.branchOrders.get(orderIndex);
		if (!branchChoice.startsWith(stmtID + ","))
		{
			System.out.println("ToDoPath getDirection(I) is broken at stmt id " + stmtID);
			System.out.println("oder index = " + this.orderIndex);
			this.print();
			System.exit(1);
		}
		return branchChoice.split(",")[1];
	}
	
	/**
	 * return a new instance of ToDoPath which has the same
	 * startingStmtID, endingStmtID, and a cloned version of
	 * branchChoices. Everything else is the initial value
	 * */
	ToDoPath copy()
	{
		ToDoPath result = new ToDoPath(this.startingStmtID, this.endingStmtID);
		result.branchOrders = new ArrayList<String>(this.branchChoices);
		return result;
	}
	
	public ToDoPath clone()
	{
		ToDoPath result = new ToDoPath(this.startingStmtID, this.endingStmtID);
		
		result.isLegit = this.isLegit;
		result.explored = this.explored;
		
		result.orderIndex = this.orderIndex;
		result.branchOrders = new ArrayList<String>(this.branchOrders);
		result.choiceIndex = this.choiceIndex;
		result.branchChoices = new ArrayList<String>(this.branchChoices);
		result.execLog = new ArrayList<String>(this.execLog);
		
		return result;
	}
	
	public void print()
	{
		System.out.println("\nToDoPath from stmt " + this.startingStmtID + " to " + this.endingStmtID);
		System.out.println("Full execution log:");
		for (String s : this.execLog)
		{
			System.out.println(" " + s);
		}
		System.out.println("Branch choices:");
		for (String s : this.branchChoices)
		{
			System.out.println(" " + s);
		}
		System.out.println("");
	}
	
	public String branchChoicesToString()
	{
		String result = "";
		for (String s: this.branchChoices)
			result+=s;
		return result;
	}
	
	public String execLogToString()
	{
		String result = "";
		for (String s: this.execLog)
			result+=s;
		return result;
	}
}
