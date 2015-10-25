package apex.gui;

public class Thrower {

	
	public static void throwException(String msg)
	{
		try
		{
			throw new Exception(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
}
