package apex.symbolic.value;

import apex.symbolic.context.VMContext;

public class Thrower {

	
	public static void throwException(String text)
	{
		try
		{
			throw new Exception(text);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			//System.exit(1);
		}
	}
	
	public static void throwException(String text, VMContext vm)
	{
		try
		{
			throw new Exception(text);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			vm.printSnapshot();
			//System.exit(1);
		}
	}
	
}
