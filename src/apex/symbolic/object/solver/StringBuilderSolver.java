package apex.symbolic.object.solver;

import apex.staticFamily.StaticStmt;
import apex.symbolic.Expression;
import apex.symbolic.context.MethodContext;
import apex.symbolic.context.VMContext;
import apex.symbolic.object.SymbolicObject;
import apex.symbolic.object.SymbolicStringBuilder;
import apex.symbolic.value.LiteralValue;
import apex.symbolic.value.ReferenceValue;
import apex.symbolic.value.Value;

public class StringBuilderSolver {

	private static final String[] SB_signatures = {
		"Ljava/lang/StringBuilder;-><init>()V",
		"Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V",
		"Ljava/lang/StringBuilder;->append(",
		"Ljava/lang/StringBuilder;->toString()Ljava/lang/String;",
	};
	
	public static boolean isSolvableStringBuilderAPI(String invokeSig)
	{
		for (String ss : SB_signatures)
		{
			if (invokeSig.startsWith(ss))
				return true;
		}
		return false;
	}
	
	private static int getSignatureIndex(String signature)
	{
		for (int i = 0; i < SB_signatures.length; i++)
		{
			if (signature.startsWith(SB_signatures[i]))
				return i;
		}
		return -1;
	}
	
	public static void solve(VMContext vm, MethodContext mc, StaticStmt s)
	{
		SymbolicStringBuilder ss = findSymbolicStringObject(vm, mc, s);
		String methodSig = s.getInvokeSignature();
		int index = getSignatureIndex(methodSig);
		if (index == 0)
		{}
		else if (index == 1)	// no return
		{
			LiteralValue v = findParameterValue(vm, mc, s);
			ss.init(v);
		}
		else if (index == 2)	// return the StringBuilder
		{
			LiteralValue v = findParameterValue(vm, mc, s);
			ReferenceValue result = ss.append(methodSig, v);
			mc.putResult(result);
		}
		else if (index == 3)	// return a String
		{
			Expression stringEx = ss.toStringExpression();
			LiteralValue result = new LiteralValue(stringEx, "Ljava/lang/String;");
			mc.putResult(result);
		}

	}
	
	private static SymbolicStringBuilder findSymbolicStringObject(VMContext vm, MethodContext mc, StaticStmt s)
	{
		String p0RegName = s.getInvokeParameters().get(0);
		Value p0RegValue = mc.getRegister(p0RegName).getValue();
		if (!(p0RegValue instanceof ReferenceValue))
		{
			System.out.println("StringBuilder API p0 is not a Reference Value at " + s.getUniqueID());
			System.exit(1);
		}
		SymbolicObject stringObject = vm.getObject(p0RegValue.getExpression().getContent());
		if (!(stringObject instanceof SymbolicStringBuilder))
		{
			System.out.println("StringBuilder object type is not SymbolicString at " + s.getUniqueID());
			System.exit(1);
		}
		return ((SymbolicStringBuilder) stringObject);
	}
	
	private static LiteralValue findParameterValue(VMContext vm, MethodContext mc, StaticStmt s)
	{
		if (s.getInvokeParameters().size() < 2)
			return null;
		String p1RegName = s.getInvokeParameters().get(1);
		Value p1RegValue = mc.getRegister(p1RegName).getValue();
		if (p1RegValue instanceof ReferenceValue)
		{
			SymbolicObject obj = vm.getObject(((ReferenceValue) p1RegValue).getAddress());
			return new LiteralValue(obj.getExpression(), p1RegValue.getType());
		}
		return new LiteralValue(p1RegValue.getExpression(), p1RegValue.getType());
	}
	
}
