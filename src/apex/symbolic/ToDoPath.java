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
	int branchIndex = 0;
	ArrayList<String> branchChoices = new ArrayList<String>();

	
	ToDoPath(int startingStmtID, int endingStmtID)
	{
		this.startingStmtID = startingStmtID;
		this.endingStmtID = endingStmtID;
	}
	
	String getDirection(int stmtID)
	{
		if (branchIndex >= branchChoices.size())
		{
			for (String choice : branchChoices)
			{
				if (choice.startsWith("" + stmtID + ","))
				{
					// this means we have looped back to this if statement
					String oldDirection = choice.split(",")[1];
					if (oldDirection.equals("jump"))
						return "forceFlow";
					if (oldDirection.equals("flow"))
						return "forceJump";
				}
			}
			return "";
		}
		String branchChoice = this.branchChoices.get(branchIndex++);
		if (!branchChoice.startsWith("" + stmtID + ","))
		{
			System.out.println("ToDoPath getDirection(I) is broken at stmt id " + stmtID);
			System.out.println("got: " + branchChoice);
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
		result.branchChoices = new ArrayList<String>(this.branchChoices);
		return result;
	}
	
	public void print()
	{
		System.out.println("ToDoPath from stmt " + this.startingStmtID + " to " + this.endingStmtID);
		for (String s : branchChoices)
		{
			System.out.println(" " + s);
		}
	}
}
