package apex.symbolic.object;

import java.util.ArrayList;

import apex.parser.DEXParser;
import apex.symbolic.Expression;
import apex.symbolic.value.LiteralValue;
import apex.symbolic.value.ReferenceValue;
import apex.symbolic.value.Value;

public class SymbolicArray extends SymbolicObject{

	/**
	 * Seems like there's not a good way to deal with
	 * arrays with object elements...
	 * So let's just assume array elements are primitives...
	 * 
	 * inherited field 'expression' is used to store array
	 * declarations, e.g., $array - length - type
	 * 
	 * */
	
	private int length;
	private String elementType;
	private ArrayList<Aput> aputHistory = new ArrayList<Aput>();
	private Expression initArrayEx;
	
	public SymbolicArray(int birthday, Expression initialArrayEx)
	{
		super(birthday);
		
		this.initArrayEx = initialArrayEx.clone();
		if (initialArrayEx.getContent().equals("$array"))
		{
			this.length = Integer.parseInt(initialArrayEx.getChild(0).getContent());
			this.elementType = initialArrayEx.getChild(1).getContent();
		}
	}
	
	public SymbolicArray(int birthday, Expression initialArrayEx, String elementType)
	{
		this(birthday, initialArrayEx);
		this.elementType = elementType;
	}
	
	
	public Expression getArrayExpression()
	{
		return this.expression;
	}
	
	public String getArrayType()
	{
		return "[" + this.elementType;
	}
	
	public String getElementType()
	{
		return this.elementType;
	}
	
	public int getLength()
	{
		return this.length;
	}

	
	public void aput(LiteralValue indexV, Value value)
	{
		// first remove any previous assignments on the same index
		Aput newAput = new Aput(indexV, value);
		int toDelete = -1;
		for (int i = 0; i < this.aputHistory.size(); i++)
		{
			if (aputHistory.get(i).index.equals(newAput.index))
			{
				toDelete = i;
				break;
			}
		}
		if (toDelete != -1)
		{
			aputHistory.remove(toDelete);
		}
		aputHistory.add(newAput);
		// then rebuild array expression
		this.expression = this.initArrayEx.clone();
		for (int i = 0; i < aputHistory.size(); i++)
		{
			Aput aput = aputHistory.get(i);
			Expression index = new Expression("");
			index.add(aput.index.getExpression().clone());
			Expression newArrayEx = new Expression("update");
			newArrayEx.add(this.expression.clone());
			newArrayEx.add(index);
			newArrayEx.add(aput.value.getExpression().clone());
			this.expression = newArrayEx.clone();
		}
	}
	
	
	public Value aget(LiteralValue indexV)
	{
		// if aput history are all constant index, then try to find
		// the result from there
		try
		{
			Value result = null;
			Integer targetIndex = Integer.parseInt(indexV.getExpression().getContent());
			for (Aput aput : this.aputHistory)
			{
				Integer const_index = Integer.parseInt(aput.index.getExpression().getContent());
				if (const_index == targetIndex)
				{
					result = aput.value.clone();
				}
			}
			return result;
		}
		catch (NumberFormatException e)
		{
			// otherwise let yices solve it
			Expression resultEx = new Expression("");
			resultEx.add(this.getArrayExpression());
			resultEx.add(indexV.getExpression().clone());
			if (DEXParser.isPrimitiveType(this.elementType) || this.elementType.equals("Ljava/lang/String;"))
			{
				return new LiteralValue(resultEx, this.elementType);
			}
			else
			{
				return new ReferenceValue(resultEx, this.elementType);
			}
		}
	}
	
	
	
	private class Aput
	{
		LiteralValue index;
		Value value;
		Aput(LiteralValue index, Value value)
		{
			this.index = index;
			this.value = value;
		}
	}
	
}
