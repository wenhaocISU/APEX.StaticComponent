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
	
	private int length = -1;
	private String elementType;
	private ArrayList<Aput> aputHistory = new ArrayList<Aput>();
	private Expression initArrayEx;
	
	
	SymbolicArray()
	{
	}
	
	public SymbolicArray(int birthday, Expression initialArrayEx)
	{
		super(birthday);
		
		this.initArrayEx = initialArrayEx.clone();
		this.expression = this.initArrayEx.clone();
		if (initialArrayEx.getContent().equals("$array"))
		{
			String lengthStr = initialArrayEx.getChild(0).getContent();
			if (lengthStr.equals("$number"))
			{
				lengthStr = initialArrayEx.getChild(0).getChild(0).getContent();
			}
			try
			{	// this length might not be a number, it could be something like: ("BattleStat".length)
				this.length = Integer.parseInt(initialArrayEx.getChild(0).getContent());
			}
			catch (Exception e)
			{}
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
		String operator = this.initArrayEx.getContent().equals("$array")? "" : "$aget";
		// if aput history are all constant index, then try to find
		// the result from there.
		// (if any of the aput history index is not number, exception will be caught)
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
			if (result != null)
				return result;
		}
		catch (NumberFormatException e)
		{}
		// otherwise leave it to yices
		Expression resultEx = new Expression(operator);
		resultEx.add(this.getArrayExpression().clone());
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
	
	public SymbolicArray clone()
	{
		SymbolicArray result = new SymbolicArray();
		result.address = this.address;
		if (this.expression != null)
			result.expression = this.expression.clone();
		result.length = this.length;
		result.elementType = this.elementType;
		result.aputHistory = new ArrayList<Aput>();
		for (Aput aput : this.aputHistory)
		{
			result.aputHistory.add(aput.clone());
		}
		if (this.initArrayEx != null)
			result.initArrayEx = this.initArrayEx.clone();
		return result;
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
		protected Aput clone()
		{
			return new Aput(this.index.clone(), this.value.clone());
		}
	}
	
	public void print()
	{
		System.out.println("\n[" + this.address + "] *SymbolicArray");
		if (this.expression != null)
		{
			System.out.println(" *array expression: " + this.expression.toYicesStatement());
		}
		System.out.println(" *length = " + (this.length==-1?"unknown":this.length));
		System.out.println(" *element type = " + this.elementType);
	}
	
}
