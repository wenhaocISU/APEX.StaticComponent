package apex.symbolic;

import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

import apex.staticFamily.StaticApp;
import apex.staticFamily.StaticMethod;
import apex.staticFamily.StaticStmt;

public class ToDoPath {
	
	
	int id = -1;
	/**
	 * Attributes in exploring stage
	 * */
	int startingStmtID = 0;
	int endingStmtID = -1;
	boolean isLegit = true;
	boolean explored = false;
	StaticMethod m;
	
	/**
	 * members in exploring stage
	 * */
	int orderIndex = -1;
	ArrayList<String> branchOrders = new ArrayList<String>();
	int choiceIndex = 0;
	ArrayList<String> branchChoices = new ArrayList<String>();
	ArrayList<String> execLog = new ArrayList<String>();

	ToDoPath(StaticMethod m)
	{
		this.m = m;
	}
	
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
	
	public int getID()
	{
		return this.id;
	}
	
	
	public void generateExecLogFromOrders(StaticApp staticApp)
	{
		System.out.println("___________________________");
		this.print();
		System.out.println("---------------------------");
		this.execLog = new ArrayList<String>();
		Stack<StaticMethod> methods = new Stack<StaticMethod>();
		Stack<Integer> stmtIDs = new Stack<Integer>();
		methods.push(this.m);
		stmtIDs.push(this.startingStmtID);
		int nextStmtID = stmtIDs.peek();
		while (!methods.isEmpty())
		{
			nextStmtID = stmtIDs.pop();
			stmtIDs.push(nextStmtID+1);
			StaticStmt s = methods.peek().getStatements().get(nextStmtID);
			if (s == null)
			{
				System.out.println("ToDoPath.generateExecLogFromOrders() ran into null StaticStmt.");
				System.exit(1);
			}
			System.out.println(s.getUniqueID());
			this.execLog.add(s.getUniqueID());
			if (this.endingStmtID == s.getStatementID() && methods.peek().getSignature().equals(this.m.getSignature()))
			{
				methods.pop();
				stmtIDs.pop();
			}
			else if (s.isIfStmt())
			{
				String order = this.getOrder(s.getUniqueID());
				String choice = order;
				if (order.equals("jump"))
				{
					stmtIDs.pop();
					stmtIDs.push(s.getIfJumpTargetID());
				}
				else if (order.equals("flow"))
				{}
				else if (order.startsWith("force")) // breaking out of loop
				{
					choice = order.replace("force", "");
					if (order.endsWith("jump"))
					{
						stmtIDs.pop();
						stmtIDs.push(s.getIfJumpTargetID());
					}
				}
				else
				{
					System.out.println("ToDoPath.generateExecLogFromOrders() failed to find an order.");
					System.exit(1);
				}
				this.branchChoices.add(s.getUniqueID()+","+choice);
			}
			else if (s.isSwitchStmt())
			{
				String order = this.getOrder(s.getUniqueID());
				String choice = order;
				Map<Integer, String> switchMap = s.getSwitchMap();
				if (order.equals("flow"))
				{}
				else if (order.startsWith("case"))
				{
					int caseValue = Integer.parseInt(order.substring(order.indexOf("case")+4));
					String label = switchMap.get(caseValue);
					stmtIDs.pop();
					stmtIDs.push(m.getFirstStmtOfBlock(label).getStatementID());
				}
				else
				{
					System.out.println("ToDoPath.generateExecLogFromOrders() failed to find an order.");
					System.exit(1);
				}
				this.branchChoices.add(s.getUniqueID() + "," + choice);
			}
			else if (s.isGotoStmt())
			{
				stmtIDs.pop();
				stmtIDs.push(s.getGotoTargetID());
			}
			else if (s.isReturnStmt() || s.isThrowStmt())
			{
				methods.pop();
				stmtIDs.pop();
			}
			else if (s.isInvokeStmt())
			{
				String targetSig = s.getInvokeSignature();
				StaticMethod targetM = staticApp.getMethod(targetSig);
				if (targetM != null && !targetM.isAbstract())
				{
					methods.push(targetM);
					stmtIDs.push(0);
				}
			}
		}
		if (this.endingStmtID == nextStmtID)
			this.execLog.add(this.m.getSignature() + ":" + nextStmtID);
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
		System.out.println("Branch orders:");
		for (String s : this.branchOrders)
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
