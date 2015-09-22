package apex.symbolic;

import java.util.HashMap;
import java.util.Map;

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
	Map<Integer, String> branchChoices = new HashMap<Integer, String>();
	

	
	ToDoPath(int startingStmtID, int endingStmtID)
	{
		this.startingStmtID = startingStmtID;
		this.endingStmtID = endingStmtID;
	}
	
	
	void print()
	{
		System.out.println("ToDoPath from stmt " + this.startingStmtID + " to " + this.endingStmtID);
		for (Map.Entry<Integer, String> entry : branchChoices.entrySet())
		{
			System.out.println(" At stmt " + entry.getKey() + ", " + entry.getValue());
		}
	}
}
