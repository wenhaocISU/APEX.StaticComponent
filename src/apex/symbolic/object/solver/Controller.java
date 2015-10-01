package apex.symbolic.object.solver;

import apex.staticFamily.StaticStmt;
import apex.symbolic.context.MethodContext;
import apex.symbolic.context.VMContext;

public class Controller {

	/**
	 * Try to find an existing solver that can deal with this invoke API signature.
	 * Return true if solver exist, return false otherwise.
	 * 
	 * */
	public static boolean tryAllSolvers(VMContext vm, MethodContext mc, StaticStmt s)
	{
		boolean solvable = true;
		
		if (StringBuilderSolver.solvable(s.getInvokeSignature()))
		{
			StringBuilderSolver.solve(vm, mc, s);
		}
		
		else if (NumberConversionSolver.solvable(s.getInvokeSignature()))
		{
			NumberConversionSolver.solve(vm, mc, s);
		}
		
		else
		{
			solvable = false;
		}
		return solvable;
	}
	
}
