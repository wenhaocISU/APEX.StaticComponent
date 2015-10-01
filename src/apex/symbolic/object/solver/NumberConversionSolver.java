package apex.symbolic.object.solver;

public class NumberConversionSolver {

	
	private static String[] signatures = {
		"Ljava/lang/Float;->valueOf(Ljava/lang/String;)Ljava/lang/Float;",
		"Ljava/lang/Float;->floatValue()F",

		"Ljava/lang/Double;->valueOf(Ljava/lang/String;)Ljava/lang/Double;",
		"Ljava/lang/Double;->valueOf(D)Ljava/lang/Double;",
		"Ljava/lang/Double;->doubleValue()D",
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
	
	
	public static void solve(String signature)
	{
		int index = getSignatureIndex(signature);
		
	}
	
}
