package apex.symbolic.context;

import java.util.ArrayList;

import apex.parser.DEXParser;
import apex.staticFamily.StaticMethod;
import apex.staticFamily.StaticStmt;
import apex.symbolic.Expression;
import apex.symbolic.value.ReferenceValue;
import apex.symbolic.value.Value;

public class MethodContext {

	ArrayList<Register> registers = new ArrayList<Register>();
	Value recentResult;
	private VMContext vm;
	private StaticMethod m;
	
	public MethodContext(StaticMethod m, VMContext vm)
	{
		this.m = m;
		this.vm = vm;
		for (int i = 0; i < m.getLocalRegisterCount(); i++)
		{
			Register reg = new Register("v"+i, false);
			this.registers.add(reg);
		}
		int paramIndex = 0;
		for (int i = 0; i < m.getParamTypes().size(); i++)
		{
			String paramType = m.getParamTypes().get(i);
			Register reg = new Register("p"+paramIndex++, true);
			this.registers.add(reg);
			if (paramType.equals("J") || paramType.equals("D"))
			{
				Register secondHalf = new Register("p"+paramIndex++, true);
				secondHalf.isLocked = true;
				this.registers.add(secondHalf);
			}
			if (i == 0 && !m.isStatic())
			{
				String address = this.vm.createOrFindObjectThis(m.getDeclaringClass().getDexName());
				ReferenceValue v = new ReferenceValue(new Expression(address));
				reg.putValue(v);
			}
		}
	}
	/**
	 * Parse statement, and apply corresponding operations
	 * on method context and/or vm context.
	 * Possible operation types:
	 * 	vA = vB
	 * 	vA = $result
	 * 	vA = $number
	 * 	vA = $const-string
	 * 	vA = $const-class
	 * 	vA = $instance-of
	 * 	vA = $array-length
	 * 	vA = $new-instance
	 * 	vA = $array
	 * 	vA = $aget
	 * 	vA = $aput
	 * 	vA = $Finstance		$Finstance = vA
	 * 	vA = $Fstatic		$Fstatic = vA
	 * 	vA = [arithmatical op]
	 * 	$result = $array
	 * */
	public void applyStatement(StaticStmt s)
	{
		Expression ex = s.getOperationExpression();
		if (ex.getContent().equals(""))
			return;
		Expression left = ex.getChild(0);
		Expression right = ex.getChild(1);
// $result = $array
		if (left.getContent().equals("$result"))
		{
			if (right.getContent().equals("$array"))
			{
				String address = this.vm.createObject(right);
				ReferenceValue arrayRef = new ReferenceValue(new Expression(address));
				this.putResult(arrayRef.clone());
			}
		}
// $Finstance = vA
		else if (left.getContent().equals("$Finstance"))
		{
			Register sourceReg = this.findRegister(right.getContent());
			String fieldSig = left.getChild(0).getContent();
			String objRegName = left.getChild(1).getContent();
			Register objReg = this.findRegister(objRegName);
			if (!(objReg.getValue() instanceof ReferenceValue))
			{
				System.out.println("$Finstance object register not holding ReferenceValue!");
				System.exit(1);
			}
			ReferenceValue objRef = (ReferenceValue)objReg.getValue();
			this.vm.iput(objRef, fieldSig, sourceReg.getValue().clone());
		}
// $Fstatic = vA
		else if (left.getContent().equals("$Fstatic"))
		{
			Register sourceReg = this.findRegister(right.getContent());
			String fieldSig = left.getChild(0).getContent();
			this.vm.sput(fieldSig, sourceReg.getValue().clone());
		}
/** 	vA = vB
	 * 	vA = $result
	 * 	vA = $number
	 * 	vA = $const-string
	 * 	vA = $const-class
	 * 	vA = $instance-of
	 * 	vA = $array-length
	 * 	vA = $new-instance
	 * 	vA = $array
	 * 	vA = $aget
	 * 	vA = $aput
	 * 	vA = $Finstance		$Finstance = vA
	 * 	vA = $Fstatic		$Fstatic = vA
	 * 	vA = [arithmatical op]*/
		else if (right.getContent().startsWith("v"))
		{
			
		}
		else if (right.getContent().equals("$Finstance"))
		{
			
		}
		else if (right.getContent().equals("$Fstatic"))
		{
			
		}
		else if (right.getContent().equals("$result"))
		{
			
		}
		else if (right.getContent().equals("$number"))
		{
			
		}
		else if (right.getContent().startsWith("$const"))
		{
			
		}
		else if (right.getContent().equals("$instance-of"))
		{
			
		}
		else if (right.getContent().equals("$array-length"))
		{
			
		}
		else if (right.getContent().equals("$new-instance"))
		{
			
		}
		else if (right.getContent().equals("$array"))
		{
			
		}
		else if (right.getContent().equals("$aget"))
		{
			
		}
		else if (right.getContent().equals("$aput"))
		{
			
		}
		else if (DEXParser.isArithmaticalOperator(right.getContent()))
		{
			
		}
	}
	
	public void putResult(Value v)
	{
		this.recentResult = v;
	}
	
	public Register findRegister(String name)
	{
		for (Register reg : this.registers)
		{
			if (reg.name.equals(name))
				return reg;
		}
		return null;
	}
	
	public void printSnapshot()
	{
		System.out.println("\nMethodContext snapshot - " + m.getSignature());
		for (Register reg : this.registers)
		{
			reg.printSnapshot();
		}
		
	}
	
	public StaticMethod getStaticMethod()
	{
		return this.m;
	}
	
}
