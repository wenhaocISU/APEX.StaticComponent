package apex.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import apex.staticFamily.StaticStmt;
import apex.symbolic.Expression;

public class DEXParser {
	public static String[] smaliStatements = {
		"nop",
		"move",
		"move/from16",
		"move/16",
		"move-wide",
		"move-wide/from16",
		"move-wide/16",
		"move-object",
		"move-object/from16",
		"move-object/16",
		"move-result",
		"move-result-wide",
		"move-result-object",
		"move-exception",
		"return-void",
		"return",
		"return-wide",
		"return-object",
		"const/4",
		"const/16",
		"const",
		"const/high16",
		"const-wide/16",
		"const-wide/32",
		"const-wide",
		"const-wide/high16",
		"const-string",
		"const-string/jumbo",
		"const-class",
		"monitor-enter",
		"monitor-exit",
		"check-cast",
		"instance-of",
		"array-length",
		"new-instance",
		"new-array",
		"filled-new-array",
		"filled-new-array/range",
		"fill-array-data",
		"throw",
		"goto",
		"goto/16",
		"goto/32",
		"packed-switch",
		"sparse-switch",
		"cmpl-float",
		"cmpg-float",
		"cmpl-double",
		"cmpg-double",
		"cmp-long",
		"if-eq",
		"if-ne",
		"if-lt",
		"if-ge",
		"if-gt",
		"if-le",
		"if-eqz",
		"if-nez",
		"if-ltz",
		"if-gez",
		"if-gtz",
		"if-lez",
		"aget",
		"aget-wide",
		"aget-object",
		"aget-boolean",
		"aget-byte",
		"aget-char",
		"aget-short",
		"aput",
		"aput-wide",
		"aput-object",
		"aput-boolean",
		"aput-byte",
		"aput-char",
		"aput-short",
		"iget",
		"iget-wide",
		"iget-object",
		"iget-boolean",
		"iget-byte",
		"iget-char",
		"iget-short",
		"iput",
		"iput-wide",
		"iput-object",
		"iput-boolean",
		"iput-byte",
		"iput-char",
		"iput-short",
		"sget",
		"sget-wide",
		"sget-object",
		"sget-boolean",
		"sget-byte",
		"sget-char",
		"sget-short",
		"sput",
		"sput-wide",
		"sput-object",
		"sput-boolean",
		"sput-byte",
		"sput-char",
		"sput-short",
		"invoke-virtual",
		"invoke-super",
		"invoke-direct",
		"invoke-static",
		"invoke-interface",
		"invoke-virtual/range",
		"invoke-virtual/range",
		"invoke-super/range",
		"invoke-direct/range",
		"invoke-static/range",
		"invoke-interface/range",
		"neg-int",
		"not-int",
		"neg-long",
		"not-long",
		"neg-float",
		"neg-double",
		"int-to-long",
		"int-to-float",
		"int-to-double",
		"long-to-int",
		"long-to-float",
		"long-to-double",
		"float-to-int",
		"float-to-long",
		"float-to-double",
		"double-to-int",
		"double-to-long",
		"double-to-float",
		"int-to-byte",
		"int-to-char",
		"int-to-short",
		"add-int",
		"sub-int",
		"mul-int",
		"div-int",
		"rem-int",
		"and-int",
		"or-int",
		"xor-int",
		"shl-int",
		"shr-int",
		"ushr-int",
		"add-long",
		"sub-long",
		"mul-long",
		"div-long",
		"rem-long",
		"and-long",
		"or-long",
		"xor-long",
		"shl-long",
		"shr-long",
		"ushr-long",
		"add-float",
		"sub-float",
		"mul-float",
		"div-float",
		"rem-float",
		"add-double",
		"sub-double",
		"mul-double",
		"div-double",
		"rem-double",
		"add-int/2addr",
		"sub-int/2addr",
		"mul-int/2addr",
		"div-int/2addr",
		"rem-int/2addr",
		"and-int/2addr",
		"or-int/2addr",
		"xor-int/2addr",
		"shl-int/2addr",
		"shr-int/2addr",
		"ushr-int/2addr",
		"add-long/2addr",
		"sub-long/2addr",
		"mul-long/2addr",
		"div-long/2addr",
		"rem-long/2addr",
		"and-long/2addr",
		"or-long/2addr",
		"xor-long/2addr",
		"shl-long/2addr",
		"shr-long/2addr",
		"ushr-long/2addr",
		"add-float/2addr",
		"sub-float/2addr",
		"mul-float/2addr",
		"div-float/2addr",
		"rem-float/2addr",
		"add-double/2addr",
		"sub-double/2addr",
		"mul-double/2addr",
		"div-double/2addr",
		"rem-double/2addr",
		"add-int/lit16",
		"rsub-int/lit16",
		"mul-int/lit16",
		"div-int/lit16",
		"rem-int/lit16",
		"and-int/lit16",
		"or-int/lit16",
		"xor-int/lit16",
		"add-int/lit8",
		"rsub-int/lit8",
		"mul-int/lit8",
		"div-int/lit8",
		"rem-int/lit8",
		"and-int/lit8",
		"or-int/lit8",
		"xor-int/lit8",
		"shl-int/lit8",
		"shr-int/lit8",
		"ushr-int/lit8"
	};
	/**
	Important Statements:
	 1-9: move
	 10-12: move result 
	 14-17: return
	 18-28: const
	 32: instance-of
	 33: array-length
	 34: new-instance
	 35-38: new-array ?
	 39: throw
	 40-42: goto
	 43-44: switch
	 45-49: complex number comparing
	 50-55: if
	 56-61: ifz
	 62-68: aget
	 69-75: aput
	 76-82: iget
	 83-89: iput
	 90-96: sget
	 97-103: sput
	 104-114: invoke
	 115-120: unop - operation
	 121-135: unop - number type conversion
	 136-167: binop - operation
	 168-199: binop/2addr - operation
	 200-218: binop/lit16(8) - operation
	**/
	
	private static List<String> arithmaticalOp = 
		new ArrayList<String>
		(
			Arrays.asList
			(
				/**
				 * rsub: reverse subtraction
				 * */
				new String[]
				{
				"not", "neg", "add", "sub", "rsub", "mul", "div",
				"rem", "and", "or", "xor", "shl", "shr", "ushr"
				}
			)
		);
	
	public static Expression generateExpression(StaticStmt s)
	{
		Expression ex = new Expression("=");
		String stmt = s.getSmaliStmt();
		/** first find the bytecode instruction index */
		int stmtIndex = getBytecodeOpIndex(s.getBytecodeOperator());
		/**	move vA, vB	*/
		if (stmtIndex >= 1 && stmtIndex <= 9)
		{
			String vA = stmt.substring(stmt.indexOf(" ")+1, stmt.indexOf(", "));
			String vB = stmt.substring(stmt.indexOf(", ")+2);
			ex.add(vA);
			ex.add(vB);
		}
		/**	move-result vA	*/
		else if (stmtIndex >= 10 && stmtIndex <= 12)
		{
			// move-result either follows "filled-new-arary" or "invoke..."
			// result is already there for "filled-new-array"
			// result of "invoke" requires symbolic execution
			String vA = stmt.substring(stmt.indexOf(" ")+1);
			ex.add(vA);
			ex.add("$result");
		}
		/**	const	
		 * 	NOTE(09/21/2015): since bytecode is not type sensitive,
		 * 	we are depending on Apktool to comment the type of this const number
		 * */
		else if (stmtIndex >= 18 && stmtIndex <= 21)
		{
			String vA = stmt.substring(stmt.indexOf(" ")+1, stmt.indexOf(", "));
			String hex = stmt.substring(stmt.indexOf(", ")+2);
			String dec = "";
			if (hex.contains("# "))
			{
				dec = hex.substring(hex.indexOf("# ")+2);
				hex = hex.substring(0, hex.indexOf(" "));
			}
			else
			{
				dec = Integer.parseInt(hex.replace("0x", ""), 16) + "";
			}
			Expression numberEx = new Expression("$number");
			numberEx.add(dec);
			ex.add(vA);
			ex.add(numberEx);
		}
		/**
		 * const-wide
		 * */
		else if (stmtIndex >= 22 && stmtIndex <= 25)
		{
			String vA = stmt.substring(stmt.indexOf(" ")+1, stmt.indexOf(", "));
			String hex = stmt.substring(stmt.indexOf(", ")+2);
			String dec = "";
			if (hex.contains("# "))	// double
			{
				dec = hex.substring(hex.indexOf("# ")+2);
				hex = hex.substring(0, hex.indexOf(" "));
			}
			else	// long
			{
				dec = Long.parseLong(hex.replace("0x", "").replace("L", ""), 16) + "";
			}
			Expression numberEx = new Expression("$number");
			numberEx.add(dec);
			ex.add(vA);
			ex.add(numberEx);
		}
		/**	const-string	*/
		else if (stmtIndex == 26 || stmtIndex == 27)
		{
			String vA = stmt.substring(stmt.indexOf(" ")+1, stmt.indexOf(", "));
			String stringValue = stmt.substring(stmt.indexOf(", ")+2);
			Expression classEx = new Expression("$const-string");
			classEx.add(stringValue);
			ex.add(vA);
			ex.add(classEx);
		}
		/**	const-class	*/
		else if (stmtIndex == 28)
		{
			String vA = stmt.substring(stmt.indexOf(" ")+1, stmt.indexOf(", "));
			String className = stmt.substring(stmt.indexOf(", ")+2);
			Expression classEx = new Expression("$const-class");
			classEx.add(new Expression(className));
			ex.add(new Expression(vA));
			ex.add(classEx);
		}
		/** instance-of vA, vB, type@CCCC */
		else if (stmtIndex == 32)
		{
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			String type = vs[2];
			Expression opEx = new Expression("$instance-of");
			opEx.add(vB);
			opEx.add(type);
			ex.add(vA);
			ex.add(opEx);
		}
		/** array-length vA, vB */
		else if (stmtIndex == 33)
		{
			String vA = stmt.substring(stmt.indexOf(" ")+1, stmt.indexOf(", "));
			String vB = stmt.substring(stmt.indexOf(", ")+2);
			Expression opEx = new Expression("$array-length");
			opEx.add(vB);
			ex.add(vA);
			ex.add(opEx);
		}
		/** new-instance vA, type@BBBB */
		else if (stmtIndex == 34)
		{
			String vA = stmt.substring(stmt.indexOf(" ")+1, stmt.indexOf(", "));
			String className = stmt.substring(stmt.indexOf(", ")+2);
			Expression opEx = new Expression("$new-instance");
			opEx.add(className);
			ex.add(vA);
			ex.add(opEx);
		}
		/** new-array vA, vB, type@CCCC 
		 * 	result:
		 * 		$array
		 * 		[length] [type]
		 * */
		else if (stmtIndex == 35)
		{
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			String type = vs[2].substring(1);// get rid of the '[' at the begining
			Expression arrayEx = new Expression("$array");
			Expression lengthEx = new Expression(vB);
			Expression typeEx = new Expression(type);
			arrayEx.add(lengthEx);
			arrayEx.add(typeEx);
			ex.add(vA);
			ex.add(arrayEx);
		}
		/** filled-new-array(/range) {vC,vD,vE,vF,vG}, type@BBBB */
		else if (stmtIndex == 36 || stmtIndex == 37)
		{
			String elements = stmt.substring(stmt.indexOf("{")+1, stmt.indexOf("}"));
			String type = stmt.substring(stmt.lastIndexOf(", ")).substring(1);
			Expression arrayEx = new Expression("$array");
			int length = 1;
			if (elements.contains(", "))
			{
				ArrayList<String> eles = new ArrayList<String>();
				eles.addAll(Arrays.asList(elements.split(", ")));
				length = eles.size();
				arrayEx.add(new Expression(length + ""));
				arrayEx.add(new Expression(type));
				int ele_index = 0;
				for (String ele : eles)
				{
					Expression eleEx = new Expression("$element");
					eleEx.add(new Expression("" + ele_index++));
					eleEx.add(new Expression(ele));
					arrayEx.add(eleEx);
				}
			}
			else
			{
				arrayEx.add(new Expression(length + ""));
				arrayEx.add(new Expression(type));
				Expression eleEx = new Expression("$element");
				eleEx.add(new Expression("0"));
				eleEx.add(new Expression(elements));
				arrayEx.add(eleEx);
			}
			ex.add("$result");
			ex.add(arrayEx);
		}
		/** fill-array-data vAA, :array_0 */
		else if (stmtIndex == 38)
		{
			String arrayDataLabel = stmt.substring(stmt.indexOf(", ")+2);
			ArrayList<String> arrayData = s.getContainingMethod().getDataChunk(arrayDataLabel);
			String lengthInfo = arrayData.get(0);
			int length = Integer.parseInt(
					lengthInfo.substring(lengthInfo.lastIndexOf(" ")+1));
			int size = 0;
			ArrayList<Expression> elementExList = new ArrayList<Expression>();
			for (int i = 1; i < arrayData.size()-1; i++)
			{
				String element = arrayData.get(i).trim();
				String hex = "", dec = "";
				switch (length)
				{
					case 1: // value has suffix 't'
					{
						hex = element;
						dec = Integer.parseInt(hex.replace("0x", "").replace("t", ""), 16) + "";
						break;
					}
					case 2: // value has suffix 's'
					{
						hex = element;
						dec = Integer.parseInt(hex.replace("0x", "").replace("s", ""), 16) + "";
						break;
					}
					case 4: // int or float (distinguish by the # annotation)
					{
						if (element.contains("# "))
						{
							hex = element.substring(0, element.indexOf(" "));
							dec = element.substring(element.indexOf("# ")+2).replace("f", "");
						}
						else
						{
							hex = element;
							dec = Integer.parseInt(hex.replace("0x", ""), 16) + "";
						}
						break;
					}
					case 8: // long or double (distinguish by the # annotation)
					{
						if (element.contains("# "))
						{
							hex = element.substring(0, element.indexOf(" "));
							dec = element.substring(element.indexOf("# ")+2);
						}
						else
						{
							hex = element;
							dec = Long.parseLong(hex.replace("0x", "").replace("L", ""), 16) + "";
						}
						break;
					}
				}
				Expression valueEx = new Expression("$number");
				valueEx.add(dec);
				Expression eleEx = new Expression("$element");
				eleEx.add(size+"");
				eleEx.add(valueEx);
				elementExList.add(eleEx);
				size++;
			}
			Expression arrayEx = new Expression("$array");
			arrayEx.add(size+"");
			arrayEx.add("number");
			for (Expression eleEx : elementExList)
			{
				arrayEx.add(eleEx);
			}
			String vA = stmt.substring(stmt.indexOf(" ")+1, stmt.indexOf(", "));
			ex.add(vA);
			ex.add(arrayEx);
		}
		/** float, long, double comparison:	cpmkind vAA, vBB, vCC 
		 Note: this statement is always followed by an ifz-test statement 
		 */
		else if (stmtIndex >= 45 && stmtIndex <= 49)
		{
			StaticStmt nextS = s.getContainingMethod().getStatements().get(s.getStatementID()+1);
			if (!nextS.getBytecodeOperator().startsWith("if")
					|| !nextS.getBytecodeOperator().endsWith("z"))
			{
				System.out.println("cmp stmt not followed by ifz stmt!!!");
				System.exit(1);
			}
			ex = new Expression("");
		}
		/** aget vAA, vBB, vCC */
		else if (stmtIndex >= 62 && stmtIndex <= 68)
		{
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String arrayName = vs[1];
			String arrayIndex = vs[2];
			Expression opEx = new Expression("$aget");
			opEx.add(arrayName);
			opEx.add(arrayIndex);
			ex.add(vA);
			ex.add(opEx);
		}
		/** aput vAA, vBB, vCC */
		else if (stmtIndex >= 69 && stmtIndex <= 75)
		{
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String arrayName = vs[1];
			String arrayIndex = vs[2];
			Expression opEx = new Expression("$aput");
			opEx.add(arrayName);
			opEx.add(arrayIndex);
			ex.add(vA);
			ex.add(opEx);
		}
		/** iget vA, vB, field@CCCC */
		else if (stmtIndex >= 76 && stmtIndex <= 82)
		{
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			String fieldSig = vs[2];
			Expression opEx;
			String fieldName = fieldSig.substring(
					fieldSig.indexOf("->")+2, fieldSig.indexOf(":"));
			// simplifying synthetic field signatures
			if (fieldName.startsWith("this$"))
			{
				String fieldType = fieldSig.substring(fieldSig.indexOf(":")+1);
				opEx = new Expression("$this");
				opEx.add(fieldType);
			}
			else
			{
				opEx = new Expression("$Finstance");
				opEx.add(fieldSig);
				opEx.add(vB);
			}
			ex.add(vA);
			ex.add(opEx);
		}
		/** iput vA, vB, field@CCCC */
		else if (stmtIndex >= 83 && stmtIndex <= 89)
		{
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			String fieldSig = vs[2];
			Expression opEx = new Expression("$Finstance");
			opEx.add(fieldSig);
			opEx.add(vB);
			ex.add(opEx);
			ex.add(vA);
		}
		/** sget vAA, field@BBBB */
		else if (stmtIndex >= 90 && stmtIndex <= 96)
		{
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String fieldSig = vs[1];
			Expression opEx;
			String fieldName = fieldSig.substring(
					fieldSig.indexOf("->")+2, fieldSig.indexOf(":"));
			// simplifying synthetic fields
			if (fieldName.startsWith("this$"))
			{
				String fieldType = fieldSig.substring(fieldSig.indexOf(":")+1);
				opEx = new Expression("$this");
				opEx.add(fieldType);
			}
			else
			{
				opEx = new Expression("$Fstatic");
				opEx.add(fieldSig);
			}
			ex.add(vA);
			ex.add(opEx);
		}
		/** sput vAA, field@BBBB */
		else if (stmtIndex >= 97 && stmtIndex <= 103)
		{
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String fieldSig = vs[1];
			Expression opEx = new Expression("$Fstatic");
			opEx.add(new Expression(fieldSig));
			ex.add(opEx);
			ex.add(vA);
		}
		/** neg, not vA, vB */
		else if (stmtIndex >= 115 && stmtIndex <= 120)
		{
			String operator = stmt.substring(0, stmt.indexOf("-"));
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			Expression opEx = new Expression(operator);
			opEx.add(vB);
			ex.add(vA);
			ex.add(opEx);
		}
		/** primitive type conversion */
		else if (stmtIndex >= 121 && stmtIndex <= 135)
		{
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			ex.add(vA);
			ex.add(vB);
		}
		/** binop operation (3 addresses: a = b op c) */
		else if (stmtIndex >= 136 && stmtIndex <= 167)
		{
			String operator = stmt.substring(0, stmt.indexOf("-"));
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			String vC = vs[2];
			Expression opEx = new Expression(operator);
			opEx.add(vB);
			opEx.add(vC);
			ex.add(vA);
			ex.add(opEx);
		}
		/** binop operation (2 addresses: a = a op b) */
		else if (stmtIndex >= 168 && stmtIndex <= 199)
		{
			String operator = stmt.substring(0, stmt.indexOf("-"));
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			Expression opEx = new Expression(operator);
			opEx.add(vA);
			opEx.add(vB);
			ex.add(vA);
			ex.add(opEx);
		}
		/** binop/lit# vA, vB, #+CCCC (int only) 
	 	lit16 - 16 bit constant
	 	lit8 - 8 bit constant
		*/
		else if (stmtIndex >= 200 && stmtIndex <= 218)
		{
			String operator = stmt.substring(0, stmt.indexOf("-"));
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			String intLiteral = vs[2];
			String dec = Integer.parseInt(intLiteral.replace("0x", ""), 16) + "";
			Expression litEx = new Expression("$number");
			Expression decEx = new Expression(dec);
			litEx.add(decEx);
			Expression opEx = new Expression(operator);
			opEx.add(vB);
			opEx.add(litEx);
			ex.add(vA);
			ex.add(opEx);
		}
		/**
		 * return null for other statements
		 * */
		else
		{
			ex = new Expression("");
		}
		return ex;
	}
	
	public static void doInvokeStmt(StaticStmt s)
	{
		//TODO
		Expression ex = new Expression("$invoke");
		String stmt = s.getSmaliStmt();
		int stmtIndex = DEXParser.getBytecodeOpIndex(s.getBytecodeOperator());
		/** invoke-kind {vC,vD,vE,vF,vG}, method@BBBB 
		 * 	invoke-kind/range {vAAAA .. vNNNN}, method@MMMM
		 * */
		if (stmtIndex >= 104 && stmtIndex <= 114)
		{
			String params = stmt.substring(stmt.indexOf("{")+1, stmt.indexOf("}"));
			String methodSig = stmt.substring(stmt.lastIndexOf(", ")+2);
			ex = new Expression("$invoke");
			ex.add(methodSig);
			if (params.contains(", "))
			{
				for (String p : params.split(", "))
				{
					ex.add(p);
				}
			}
			else if (params.contains(" .. "))
			{
				String firstV = params.substring(0, params.indexOf(" .. "));
				String lastV = params.substring(params.indexOf(" .. ")+4);
				String prefix = firstV.substring(0, 1);
				int first = Integer.parseInt(firstV.substring(1));
				int last = Integer.parseInt(lastV.substring(1));
				while (first <= last)
				{
					ex.add(prefix + first);
					first++;
				}
			}
			else if (!params.equals(""))
			{
				ex.add(params);
			}
		}
	}
	
	public static Expression getIfJumpCondition(StaticStmt s)
	{
		Expression ex = null;
		String stmt = s.getSmaliStmt();
		int stmtIndex = DEXParser.getBytecodeOpIndex(s.getBytecodeOperator());
		/** if-test vA, vB, :cond_0 */
		if (stmtIndex >= 50 && stmtIndex <= 55)
		{
			String operator = stmt.substring(0, stmt.indexOf(" ")).split("-")[1];
			String vs[] = stmt.substring(stmt.indexOf(" ")+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			ex = new Expression(getYicesOperator(operator));
			ex.add(vA);
			ex.add(vB);
		}
		/** ifz vA, :cond_0 */
		else if (stmtIndex >= 56 && stmtIndex <= 61)
		{
			String operator = stmt.substring(0, stmt.indexOf(" ")).split("-")[1];
			String vA = stmt.substring(stmt.indexOf(" ")+1, stmt.indexOf(", "));
			String right = "0";
			if (s.getStatementID() > 0)
			{
				StaticStmt lastS = s.getContainingMethod().getStatements().get(s.getStatementID()-1);
				if (lastS.getBytecodeOperator().startsWith("cmp"))
				{
					String vs[] = lastS.getSmaliStmt().substring(lastS.getSmaliStmt().indexOf(" ")+1).split(", ");
					vA = vs[1];
					right = vs[2];
					operator = operator.replace("z", "");
				}
			}
			ex = new Expression(getYicesOperator(operator));
			ex.add(new Expression(vA));
			ex.add(new Expression(right));
		}
		return ex;
	}
	
	private static String getYicesOperator(String operator)
	{
		String newOp = "";
		
		if (operator.equals("eq"))			newOp = "=";
		else if (operator.equals("ne"))		newOp = "/=";
		else if (operator.equals("lt"))		newOp = "<";
		else if (operator.equals("ge"))		newOp = ">=";
		else if (operator.equals("gt"))		newOp = ">";
		else if (operator.equals("le"))		newOp = "<=";
		else if (operator.equals("eqz"))	newOp = "=";
		else if (operator.equals("nez"))	newOp = "/=";
		else if (operator.equals("ltz"))	newOp = "<";
		else if (operator.equals("gez"))	newOp = ">=";
		else if (operator.equals("gtz"))	newOp = ">";
		else if (operator.equals("lez"))	newOp = "<=";
		
		return newOp;
	}
	
	private static int getBytecodeOpIndex(String bytecodeOp)
	{
		for (int i = 0; i < DEXParser.smaliStatements.length; i++)
		{
			if (bytecodeOp.equals(DEXParser.smaliStatements[i]))
			{
				return i;
			}
		}
		return -1;
	}
	
	public static boolean isArithmaticalOperator(String operator)
	{
		return DEXParser.arithmaticalOp.contains(operator);
	}
	
	public static List<List<String>> getRegisterAccess(StaticStmt s)
	{
		List<List<String>> result = new ArrayList<List<String>>();
		List<String> read = new ArrayList<String>();
		List<String> write = new ArrayList<String>();
		
		String stmt = s.getSmaliStmt();
		/** first find the bytecode instruction index */
		int stmtIndex = getBytecodeOpIndex(s.getBytecodeOperator());
		/**	move vA, vB	*/
		if (stmtIndex >= 1 && stmtIndex <= 9)
		{
			String vA = stmt.substring(stmt.indexOf(" ")+1, stmt.indexOf(", "));
			String vB = stmt.substring(stmt.indexOf(", ")+2);
			read.add(vB);
			write.add(vA);
		}
		/** move-result vA */
		else if (stmtIndex >= 10 && stmtIndex <= 12)
		{
			String vA = stmt.substring(stmt.indexOf(" ")+1);
			read.add(vA);
		}
		/** return variable */
		else if (stmtIndex > 14 && stmtIndex <= 17)
		{
			String vA = stmt.substring(stmt.indexOf(" ")+1);
			read.add(vA);
			if (s.getBytecodeOperator().contains("wide")
					|| s.getBytecodeOperator().contains("object"))
			{
				int vAIndex = Integer.parseInt(vA.substring(1));
				read.add(vA.substring(0, 1) + (vAIndex+1));
			}
		}
		/**	const	*/
		else if (stmtIndex >= 18 && stmtIndex <= 21)
		{
			String vA = stmt.substring(stmt.indexOf(" ")+1, stmt.indexOf(", "));
			write.add(vA);
		}
		/**	const-wide	*/
		else if (stmtIndex >= 22 && stmtIndex <= 25)
		{	// this is for wide(64 bit) const
			String vA = stmt.substring(stmt.indexOf(" ")+1, stmt.indexOf(", "));
			int vAIndex = Integer.parseInt(vA.replace("v", ""));
			write.add(vA);
			write.add("v" + (vAIndex+1));
		}
		/**	const-string, const-class	*/
		else if (stmtIndex >= 26 && stmtIndex <= 28)
		{
			String vA = stmt.substring(stmt.indexOf(" ")+1, stmt.indexOf(", "));
			write.add(vA);
		}
		/** instance-of vA, vB, type@CCCC */
		else if (stmtIndex == 32)
		{
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			read.add(vB);
			write.add(vA);
		}
		/** array-length vA, vB */
		else if (stmtIndex == 33)
		{
			String vA = stmt.substring(stmt.indexOf(" ")+1, stmt.indexOf(", "));
			String vB = stmt.substring(stmt.indexOf(", ")+2);
			read.add(vB);
			write.add(vA);
		}
		/** new-instance vA, type@BBBB */
		else if (stmtIndex == 34)
		{
			String vA = stmt.substring(stmt.indexOf(" ")+1, stmt.indexOf(", "));
			write.add(vA);
		}
		/** new-array vA, vB, type@CCCC */
		else if (stmtIndex == 35)
		{
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			read.add(vB);
			write.add(vA);
		}
		/** filled-new-array(/range) {vC,vD,vE,vF,vG}, type@BBBB */
		else if (stmtIndex == 36 || stmtIndex == 37)
		{
			String elements = stmt.substring(stmt.indexOf("{")+1, stmt.indexOf("}"));
			if (elements.contains(", "))
			{
				read.addAll(Arrays.asList(elements.split(", ")));
			}
			else
			{
				read.add(elements);
			}
		}
		/** fill-array-data vAA, :array_0 */
		else if (stmtIndex == 38)
		{
			String vA = stmt.substring(stmt.indexOf(" ")+1, stmt.indexOf(", "));
			write.add(vA);
		}
		/** throw vAA - no action needed*/
		else if (stmtIndex == 39)
		{
			String vA = stmt.substring(stmt.indexOf(" ")+1);
			read.add(vA);
		}
		/** packed/sparse switch */
		else if (stmtIndex == 43 || stmtIndex == 44)
		{
			String vA = stmt.substring(stmt.indexOf(" ")+1, stmt.indexOf(", "));
			read.add(vA);
		}
		/** cpmkind vAA, vBB, vCC	*/
		else if (stmtIndex >= 45 && stmtIndex <= 49)
		{
			String vs[] = stmt.substring(stmt.indexOf(" ")+1).split(", ");
			read.add(vs[1]);
			read.add(vs[2]);
			write.add(vs[0]);
		}
		/** if-test vA, vB, :cond_0 */
		else if (stmtIndex >= 50 && stmtIndex <= 55)
		{
			String operator = stmt.substring(0, stmt.indexOf(" ")).split("-")[1];
			String vs[] = stmt.substring(stmt.indexOf(" ")+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			read.add(vA);
			read.add(vB);
		}
		/** ifz vA, :cond_0 */
		else if (stmtIndex >= 56 && stmtIndex <= 61)
		{
			String operator = stmt.substring(0, stmt.indexOf(" ")).split("-")[1];
			String vA = stmt.substring(stmt.indexOf(" ")+1, stmt.indexOf(", "));
			read.add(vA);
		}
		/** aget vAA, vBB, vCC */
		else if (stmtIndex >= 62 && stmtIndex <= 68)
		{
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String arrayName = vs[1];
			String arrayIndex = vs[2];
			read.add(arrayName);
			read.add(arrayIndex);
			write.add(vA);
		}
		/** aput vAA, vBB, vCC */
		else if (stmtIndex >= 69 && stmtIndex <= 75)
		{
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String arrayName = vs[1];
			String arrayIndex = vs[2];
			read.add(vA);
			read.add(arrayIndex);
			write.add(arrayName);
		}
		/** iget vA, vB, field@CCCC */
		else if (stmtIndex >= 76 && stmtIndex <= 82)
		{
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			read.add(vB);
			write.add(vA);
		}
		/** iput vA, vB, field@CCCC */
		else if (stmtIndex >= 83 && stmtIndex <= 89)
		{
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			read.add(vA);
			write.add(vB);
		}
		/** sget vAA, field@BBBB */
		else if (stmtIndex >= 90 && stmtIndex <= 96)
		{
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			write.add(vA);
		}
		/** sput vAA, field@BBBB */
		else if (stmtIndex >= 97 && stmtIndex <= 103)
		{
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			read.add(vA);
		}
		/** invoke-kind {vC,vD,vE,vF,vG}, method@BBBB 
		 * 	invoke-kind/range {vAAAA .. vNNNN}, method@MMMM
		 * */
		else if (stmtIndex >= 104 && stmtIndex <= 114)
		{
			//String invokeType = line.substring(0, line.indexOf(" "));
			String params = stmt.substring(stmt.indexOf("{")+1, stmt.indexOf("}"));
			if (params.contains(", "))
			{
				read.addAll(Arrays.asList(params.split(", ")));
			}
			else if (params.contains(" .. "))
			{
				String firstV = params.substring(0, params.indexOf(" .. "));
				String lastV = params.substring(params.indexOf(" .. ")+4);
				String prefix = firstV.substring(0, 1);
				int first = Integer.parseInt(firstV.substring(1));
				int last = Integer.parseInt(lastV.substring(1));
				while (first <= last)
				{
					read.add(prefix + first);
					first++;
				}
			}
			else if (!params.equals(""))
			{
				read.add(params);
			}
		}
		/** neg, not vA, vB */
		else if (stmtIndex >= 115 && stmtIndex <= 120)
		{
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			read.add(vB);
			write.add(vA);
		}
		/** primitive type conversion */
		else if (stmtIndex >= 121 && stmtIndex <= 135)
		{
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			read.add(vB);
			write.add(vA);
		}
		/** binop operation (3 addresses: a = b op c) */
		else if (stmtIndex >= 136 && stmtIndex <= 167)
		{
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			String vC = vs[2];
			read.add(vB);
			read.add(vC);
			write.add(vA);
		}
		/** binop operation (2 addresses: a = a op b) */
		else if (stmtIndex >= 168 && stmtIndex <= 199)
		{
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			read.add(vA);
			read.add(vB);
			write.add(vA);
		}
		/** binop/lit# vA, vB, #+CCCC (int only) 
	 	lit16 - 16 bit constant
	 	lit8 - 8 bit constant
		 */
		else if (stmtIndex >= 200 && stmtIndex <= 218)
		{
			String vs[] = stmt.substring(stmt.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			read.add(vB);
			write.add(vA);
		}
		result.add(read);
		result.add(write);
		return result;
	}
	
}
