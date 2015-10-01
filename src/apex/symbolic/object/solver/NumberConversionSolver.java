package apex.symbolic.object.solver;

import apex.staticFamily.StaticStmt;
import apex.symbolic.Expression;
import apex.symbolic.context.MethodContext;
import apex.symbolic.context.Register;
import apex.symbolic.context.VMContext;
import apex.symbolic.object.SymbolicObject;
import apex.symbolic.value.LiteralValue;
import apex.symbolic.value.ReferenceValue;

public class NumberConversionSolver {

	
	private static String[] signatures = {
		"Ljava/lang/Float;->valueOf(Ljava/lang/String;)Ljava/lang/Float;",		//static
		"Ljava/lang/Double;->valueOf(Ljava/lang/String;)Ljava/lang/Double;",	//static
		
		"Ljava/lang/Float;->floatValue()F",		//virtual
		"Ljava/lang/Double;->doubleValue()D",	//virtual
		
		"Ljava/lang/Double;->valueOf(D)Ljava/lang/Double;",	//static
	};
	
	public static boolean solvable(String invokeSig)
	{
		for (String sig : signatures)
		{
			if (sig.equals(invokeSig))
				return true;
		}
		return false;
	}
	
	
	private static int getSignatureIndex(String signature)
	{
		for (int i = 0; i < signatures.length; i++)
		{
			if (signature.startsWith(signatures[i]))
				return i;
		}
		return -1;
	}
	
	
	public static void solve(VMContext vm, MethodContext mc, StaticStmt s)
	{
		String invokeSig = s.getInvokeSignature();
		int index = getSignatureIndex(invokeSig);
		String returnType = invokeSig.substring(invokeSig.lastIndexOf(")")+1);
		if (index == 0 || index == 1)
		{
			// static methods, p0 = string, return number object
			// keep $api signature
			Expression resultEx = new Expression("$api");
			resultEx.add(invokeSig);
			Register p0Reg = mc.getRegister(s.getInvokeParameters().get(0));
			LiteralValue p0Value = (LiteralValue) p0Reg.getValue();
			resultEx.add(p0Value.getExpression().clone());
			String address = vm.createObject(resultEx, returnType, false);
			ReferenceValue v = new ReferenceValue(new Expression(address), returnType);
			mc.putResult(v);
		}
		else if (index == 2 || index == 3)
		{
			// virtual methods, p0 = number obj, return primitive number
			// no need to keep $api signature
			Register p0Reg = mc.getRegister(s.getInvokeParameters().get(0));
			ReferenceValue p0Value = (ReferenceValue) p0Reg.getValue();
			SymbolicObject obj = vm.getObject(p0Value.getAddress());
			LiteralValue v = new LiteralValue(obj.getExpression(), returnType);
			mc.putResult(v);
		}
		else if (index == 4)
		{
			// static methods, p0 = primitive number, return number object
			// no need to keep $api signature
			Register p0Reg = mc.getRegister(s.getInvokeParameters().get(0));
			LiteralValue p0Value = (LiteralValue) p0Reg.getValue();
			String address = vm.createObject(p0Value.getExpression().clone(), returnType, false);
			ReferenceValue v = new ReferenceValue(new Expression(address), returnType);
			mc.putResult(v);
		}
	}
	
}
