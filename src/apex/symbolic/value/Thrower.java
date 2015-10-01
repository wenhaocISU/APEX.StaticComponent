package apex.symbolic.value;

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
			System.exit(1);
		}
	}
	
}
