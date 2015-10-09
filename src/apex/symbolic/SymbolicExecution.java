package apex.symbolic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import apex.parser.DEXParser;
import apex.staticFamily.StaticApp;
import apex.staticFamily.StaticClass;
import apex.staticFamily.StaticMethod;
import apex.staticFamily.StaticStmt;
import apex.symbolic.context.MethodContext;
import apex.symbolic.context.VMContext;
import apex.symbolic.value.LiteralValue;
import apex.symbolic.value.ReferenceValue;
import apex.symbolic.value.Thrower;

public class SymbolicExecution {

	private StaticApp staticApp;
	public boolean printStmtInfo = false;
	public boolean printVMStatus = false;
	public boolean printTDPSteps = false;
	
	public SymbolicExecution(StaticApp staticApp)
	{
		this.staticApp = staticApp;
	}
	
	public List<PathSummary> doFullSymbolic(StaticMethod m)
	{
		if (m == null)
		{
			Thrower.throwException("StaticMethod object is null!");
		}
		List<PathSummary> result = new ArrayList<PathSummary>();
		List<ToDoPath> pathList = this.generateToDoPaths(m, 0, -1);
		int id = 0;
		for (ToDoPath p : pathList)
		{
			VMContext vm = new VMContext(this.staticApp);
			System.out.println("Symbolically executing: " + m.getSignature() + " #" + id);
			result.add(this.doFullSymbolic(vm, p, m.getSignature(), id++, true));
		}
		return result;
	}
	
	private void execute(PathSummary ps, ArrayList<String> execLog, VMContext vm, boolean invokesMethod)
	{
		for (int index = 0; index < execLog.size(); index++)
		{
			String stmtInfo = execLog.get(index);
			if (this.printStmtInfo)
			{
				System.out.println("[" + stmtInfo + "]");
				if (this.printVMStatus)
					vm.printSnapshot();
			}
			if (stmtInfo.contains(",")) //if or switch, need to update path constraint
			{
				String choice = stmtInfo.substring(stmtInfo.indexOf(",")+1);
				stmtInfo = stmtInfo.substring(0, stmtInfo.indexOf(","));
				StaticStmt s = staticApp.getStmt(stmtInfo);
				if (this.printStmtInfo)
				{
					System.out.println(" " + s.getSmaliStmt());
				}
				if (s.isFirstStmtOfMethod())
				{
					MethodContext mc = new MethodContext(s.getContainingMethod(), vm);
					vm.push(mc);
				}
				if (s.isIfStmt())
				{
					Expression cond = s.getIfJumpCondition();
					if (choice.equals("flow"))
						cond = cond.getReverseCondition();
					ps.updatePathConstraint(cond);
				}
				else if (s.isSwitchStmt())
				{
					if (choice.equals("flow"))
					{
						for (Expression cond : s.getSwitchFlowThroughConditions())
						{
							ps.updatePathConstraint(cond);
						}
					}
					else if (choice.startsWith("case"))
					{
						int caseValue = Integer.parseInt(choice.replace("case", ""));
						ps.updatePathConstraint(s.getSwitchCaseCondition(caseValue));
					}
				}
			}
			else // this statement might change registers or objects
			{
				StaticStmt s = staticApp.getStmt(stmtInfo);
				if (this.printStmtInfo)
				{
					System.out.println(" " + s.getSmaliStmt());
				}
				if (s.isInvokeStmt() && !invokesMethod)
				{
					if (s.isFirstStmtOfMethod())
					{
						MethodContext mc = new MethodContext(s.getContainingMethod(), vm);
						vm.push(mc);
					}
					String invokeSig = s.getInvokeSignature();
					String returnType = invokeSig.substring(invokeSig.lastIndexOf(")")+1);
					if (!returnType.equals("V"))
					{
						if (DEXParser.isPrimitiveType(returnType))
						{
							Expression ex = new Expression("123");
							LiteralValue v = new LiteralValue(ex, returnType);
							vm.peek().putResult(v);
						}
						else if (returnType.equals("Ljava/lang/String;"))
						{
							Expression ex = new Expression("$const-string");
							ex.add("this is a stub!");
							LiteralValue v = new LiteralValue(ex, returnType);
							vm.peek().putResult(v);
						}
						else if (returnType.startsWith("["))
						{
							Expression ex = new Expression("$array");
							ex.add("1");
							ex.add(returnType.substring(1));
							String address = vm.createObject(ex, returnType);
							ReferenceValue v = new ReferenceValue(new Expression(address), returnType);
							vm.peek().putResult(v);
						}
						else
						{
							Expression ex = new Expression("$new-instance");
							ex.add(returnType);
							String address = vm.createObject(ex, returnType);
							ReferenceValue v = new ReferenceValue(new Expression(address), returnType);
							vm.peek().putResult(v);
						}
					}
				}
				else
				{
					vm.applyOperation(s);
				}
			}
		}
	}
	
	public PathSummary doFullSymbolic(VMContext vm, ToDoPath p, String methodSig, int id, boolean invokesMethod)
	{
		PathSummary ps = new PathSummary(vm, p, methodSig, id);
		execute(ps, p.execLog, vm, invokesMethod);
		return ps;
	}
	
	public PathSummary doFullSymbolic(VMContext vm, ToDoPath p, ArrayList<Expression> pathConditions, String methodSig, int id)
	{
		PathSummary ps = new PathSummary(vm, p, methodSig, id);
		ps.setPathCondition(pathConditions);
		execute(ps, p.execLog, vm, true);
		return ps;
	}
	
	public PathSummary doFullSymbolic(ArrayList<String> logcatOutput)
	{
		ToDoPath p = this.expandLogcatOutput(logcatOutput);
		VMContext vm = new VMContext(staticApp);
		String methodSig = p.m.getSignature();
		int id = 0;
		PathSummary ps = new PathSummary(vm, p, methodSig, id);
		execute(ps, p.execLog, vm, true);
		return ps;
	}
	
	public ToDoPath expandLogcatOutput(ArrayList<String> logcat)
	{
		Stack<ToDoPath> paths = new Stack<ToDoPath>();
		int i = 0;
		while (i < logcat.size())
		{
			String line = logcat.get(i++);
			if (line.startsWith("Method_Starting,"))
			{
				StaticMethod m = this.staticApp.getMethod(line.split(",")[1]);
				ToDoPath path = new ToDoPath(m);
				paths.push(path);
			}
			else if (line.startsWith("Method_Returning,"))
			{
				ToDoPath path = paths.pop();
				path.endingStmtID = -1;
				if (!paths.isEmpty())
				{
					ToDoPath outerPath = paths.peek();
					outerPath.branchOrders.addAll(path.branchOrders);
				}
				else
				{
					paths.push(path);
				}
			}
			else if (line.startsWith("Method_Throwing,"))
			{
				String stmtInfo = line.split(",")[2];
				int throwStmtID = Integer.parseInt(stmtInfo.split(":")[1]);
				ToDoPath path = paths.pop();
				path.endingStmtID = throwStmtID;
				if (!paths.isEmpty())
				{
					ToDoPath outerPath = paths.peek();
					outerPath.branchOrders.addAll(path.branchOrders);
				}
				else
				{
					paths.push(path);
				}
			}
			else if (line.startsWith("execLog,"))
			{
				String stmtInfo = line.split(",")[1];
				ToDoPath path = paths.peek();
				
				if (line.endsWith(",if"))
				{
					String nextLine = logcat.get(i++);
					String order = stmtInfo + ",jump";
					if (nextLine.endsWith(",flow_through"))
					{
						order = stmtInfo + ",flow";
					}
					path.branchOrders.add(order);
				}
				
				else if (line.endsWith(",switch"))
				{
					String nextLine = logcat.get(i++);
					String order = stmtInfo + ",flow";
					if (!nextLine.endsWith(",flow_through"))
					{
						String blockName = line.split(",")[2];
						blockName = blockName.substring(blockName.indexOf("block")+5);
						StaticStmt s = this.staticApp.getStmt(stmtInfo);
						for (Map.Entry<Integer, String> entry : s.getSwitchMap().entrySet())
						{
							if (entry.getValue().equals(blockName))
							{
								order = stmtInfo + ",case" + entry.getKey();
								break;
							}
						}
					}
					path.branchOrders.add(order);
				}
				
				else if (line.endsWith(",try"))
				{
					//TODO TBC
				}
				else if (line.contains(",block:"))
				{
					// maybe nothing needs to be done here
				}
			}
		}
		if (paths.size() != 1)
		{
			Thrower.throwException("Expanding logcat output has failed.");
		}
		ToDoPath p = paths.pop();
		p.generateExecLogFromOrders(staticApp);
		return p;
	}
	
	/**
	 * Generate a list of ToDoPath from StaticMethod
	 * from the given range of statements.
	 * If startingStmtID == 0 then start from beginning
	 * If endingStmtID == -1 then end in return or throw
	 * */
	
	public ArrayList<ToDoPath> generateToDoPaths(StaticMethod m, int startingStmtID, int endingStmtID)
	{
		return this.generateToDoPaths(m, startingStmtID, endingStmtID, true);
	}
	
	public ArrayList<ToDoPath> generateToDoPaths(StaticMethod m, int startingStmtID, int endingStmtID, boolean invokeMethods)
	{
		ArrayList<ToDoPath> result = new ArrayList<ToDoPath>();
		ArrayList<ToDoPath> unfinished = new ArrayList<ToDoPath>();
		ToDoPath tdP = new ToDoPath(startingStmtID, endingStmtID);
		unfinished.add(tdP);
		while (unfinished.size() > 0)
		{
			ToDoPath p = unfinished.remove(unfinished.size()-1);
			ArrayList<ToDoPath> finishedTDPs = exploreTDP(unfinished, p, m, true, invokeMethods);
			if (p.isLegit)
				result.addAll(finishedTDPs);
		}
		result = removeDupe(result);
		return result;
	}
	
	
/*	
	private ArrayList<ToDoPath> generateToDoPaths(StaticMethod m, int startingStmtID, int endingStmtID, boolean print)
	{
		ArrayList<ToDoPath> result = new ArrayList<ToDoPath>();
		ArrayList<ToDoPath> unfinished = new ArrayList<ToDoPath>();
		ToDoPath tdP = new ToDoPath(startingStmtID, endingStmtID);
		unfinished.add(tdP);
		while (unfinished.size() > 0)
		{
			ToDoPath p = unfinished.remove(unfinished.size()-1);
			ArrayList<ToDoPath> finishedTDPs = exploreTDP(unfinished, p, m, true);
			for (ToDoPath newP : finishedTDPs)
			{
				if (newP.isLegit)
					result.add(newP);
			}
		}
		result = removeDupe(result);
		return result;
	}*/
	
	
	private ArrayList<ToDoPath> exploreTDP(ArrayList<ToDoPath> tdPList, ToDoPath p, StaticMethod m, boolean addToDoList, boolean invokeMethods)
	{
		ArrayList<ToDoPath> result = new ArrayList<ToDoPath>();
		result.add(p.clone());
		int nextStmtID = p.startingStmtID;
		while (nextStmtID != p.endingStmtID)
		{
			StaticStmt s = m.getStatements().get(nextStmtID++);
			if (s == null)
			{
				Thrower.throwException("SymbolicExecution.exploreTDP() ran into null StaticStmt.");
			}
			if (this.printTDPSteps)
			{
				System.out.println("[" + s.getUniqueID() + "]");
			}
			for (ToDoPath tdP : result)
				tdP.execLog.add(s.getUniqueID());
			if (p.endingStmtID == s.getStatementID()) // reached specified ending
			{
				for (ToDoPath tdP : result)
					tdP.isLegit = true;
				break;
			}
			else if (s.isIfStmt()) // follow order or make own choice
			{
				for (ToDoPath tdP : result)
				{
					String order = tdP.getOrder(s.getUniqueID());
					String choice = order;
					if (order.equals("jump"))
					{
						nextStmtID = s.getIfJumpTargetID();
					}
					else if (order.equals("flow"))
					{}
					else if (order.startsWith("force")) // breaking out of loop
					{
						choice = order.replace("force", "");
						if (order.endsWith("jump"))
						{
							nextStmtID = s.getIfJumpTargetID();
						}
					}
					else // if no orders to follow, then choose flow through
					{
						choice = "flow";
						if (addToDoList)
						{
							ToDoPath newTDP = tdP.copy();
							newTDP.branchOrders.add(s.getUniqueID() + ",jump");
							tdPList.add(newTDP);
						}
					}
					tdP.branchChoices.add(s.getUniqueID()+ "," + choice);
				}
			}
			else if (s.isSwitchStmt()) // follow order or make own choice
			{
				for (ToDoPath tdP : result)
				{
					String order = tdP.getOrder(s.getUniqueID());
					String choice = order;
					Map<Integer, String> switchMap = s.getSwitchMap();
					if (order.equals("flow"))
					{}
					else if (order.startsWith("case"))
					{
						int caseValue = Integer.parseInt(order.substring(order.indexOf("case")+4));
						String label = switchMap.get(caseValue);
						nextStmtID = m.getFirstStmtOfBlock(label).getStatementID();
					}
					else // no orders to follow, choose flow through
					{
						choice = "flow";
						if (addToDoList)
						{
							for (int caseValue : switchMap.keySet())
							{
								ToDoPath newTDP = tdP.copy();
								newTDP.branchOrders.add(s.getUniqueID() + ",case" + caseValue);
								tdPList.add(newTDP);
							}
						}
					}
					tdP.branchChoices.add(s.getUniqueID() + "," + choice);
				}
			}
			else if (s.isGotoStmt())
			{
				nextStmtID = s.getGotoTargetID();
			}
			else if (s.isReturnStmt() || s.isThrowStmt())
			{
				for (ToDoPath tdP : result)
				{
					if (tdP.endingStmtID == -1) // natural ending
					{
						tdP.isLegit = true;
					}
					else // this isn't the ending we wanted, this ToDoPath will be discarded
					{
						tdP.isLegit = false;
					}
				}
				break;
			}
			else if (s.isInvokeStmt() && invokeMethods)
			{
				//TODO deal with inheritance sometime
				// can use ranged symbolic execution to find out the type of p0
				String targetSig = s.getInvokeSignature();
				String className = targetSig.split("->")[0];
				StaticClass c = this.staticApp.getClassByDexName(className);
				if (c != null && !SymbolicExecutionBlacklist.classInBlackList(className))
				{
					StaticMethod targetM = this.staticApp.getMethod(targetSig);
					if (targetM != null && !targetM.isAbstract())
					{
						ArrayList<ToDoPath> invokedTDPs = this.generateToDoPaths(targetM, 0, -1);
						ArrayList<ToDoPath> newResult = new ArrayList<ToDoPath>();
						for (ToDoPath tdP : result)
						{
							for (ToDoPath invkTDP: invokedTDPs)
							{
								ToDoPath newP = tdP.clone();
								newP.branchChoices.addAll(invkTDP.branchChoices);
								newP.execLog.addAll(invkTDP.execLog);
								newP.orderIndex += invkTDP.branchChoices.size();
								newResult.add(newP);
							}
						}
						result = new ArrayList<ToDoPath>(newResult);
					}
				}
			}
		}
		for (ToDoPath tdP : result)
		{
			if (nextStmtID == tdP.endingStmtID && tdP.isLegit)
				tdP.execLog.add(m.getSignature() + ":" + nextStmtID);
			if (tdP.endingStmtID != -1 && tdP.execLog.get(tdP.execLog.size()-1).endsWith(":"+tdP.endingStmtID))
				tdP.isLegit = false;
			for (String choice : tdP.branchChoices)
			{
				String stmtInfo = choice.split(",")[0];
				int index = tdP.execLog.indexOf(stmtInfo);
				if (index > -1)
				{
					tdP.execLog.remove(index);
					tdP.execLog.add(index, choice);
				}
			}
		}
		return result;
	}
	
	private ArrayList<ToDoPath> removeDupe(ArrayList<ToDoPath> result)
	{
		ArrayList<String> choices = new ArrayList<String>();
		ArrayList<ToDoPath> newResult = new ArrayList<ToDoPath>();
		for (ToDoPath p : result)
		{
			String choice = p.branchChoicesToString();
			if (!choices.contains(choice))
			{
				choices.add(choice);
				p.id = newResult.size();
				newResult.add(p);
			}
		}
		return newResult;
	}
}
