package apex.symbolic.object.model;

import apex.staticFamily.StaticStmt;
import apex.symbolic.context.MethodContext;
import apex.symbolic.context.VMContext;

public class Controller {

	/**
	 * Try to find an existing solver that can deal with this invoke API signature.
	 * Return true if solver exist, return false otherwise.
	 * 
	 * */
	public static boolean tryAllModelers(VMContext vm, MethodContext mc, StaticStmt s)
	{
		boolean applicable = true;
		
		if (StringBuilderModel.canHandle(s.getInvokeSignature()))
		{
			StringBuilderModel.apply(vm, mc, s);
		}
		
		else if (NumberConversionAPIModel.canHandle(s.getInvokeSignature()))
		{
			NumberConversionAPIModel.apply(vm, mc, s);
		}
		
		else
		{
			applicable = false;
		}
		return applicable;
	}
	
}
