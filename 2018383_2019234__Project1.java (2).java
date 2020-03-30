package computerOrganization;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public  class Assembler {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		opCode[] optab=new opCode[13];
		int error=0;
		ArrayList<Symbols> symtab=new ArrayList<Symbols>();
		ArrayList<literal> littab=new ArrayList<literal>();
        optab[0]=new opCode("CLA","0000",0);
        optab[1]=new opCode("LAC","0001",1);
        optab[2]=new opCode("SAC","0010",1);
        optab[3]=new opCode("ADD","0011",1);
        optab[4]=new opCode("SUB","0100",1);
        optab[5]=new opCode("BRZ","0101",1);
        optab[6]=new opCode("BRN","0110",1);
        optab[7]=new opCode("BRP","0111",1);
        optab[8]=new opCode("INP","1000",1);
        optab[9]=new opCode("DSP","1001",1);
        optab[10]=new opCode("MUL","1010",1);
        optab[11]=new opCode("DIV","1011",1);
        optab[12]=new opCode("STP","1100",0);
        int r=pass_one(optab,symtab,littab,error);
        System.out.println();
        System.out.println();
        System.out.println("FINAL OUTPUT");
        System.out.println();
     
        pass_two(optab,symtab,littab,r);
	}
	public static int pass_one(opCode[] optab,ArrayList<Symbols> symtab,ArrayList<literal> littab,int error) throws IOException
	{   
        
         try
	     {
	    	 BufferedReader in = new BufferedReader(new FileReader("input.txt"));
	    	 int lc=0;
	    	 String l;
	    	 int counter=0;
	    	 int stop=0;
	    	 int strt=0;
	    	 int type=0;
	    	 
	    	 while((l=in.readLine())!=null)
	    	 {   
	    		 String[] line=l.split(" ");
	    		 if(line.length==0 || line.length>256) { error++;throw new InvalidInsError("INVALID INSTRUCTION"); }
	    		 if(stop!=0)
	    		 {
	    			 error++;throw new IllegalOpCode("STP FOUND ALREADY"); // STP found in first line error
	    		 }
	    		 if(strt==0)
	    		 {
	    			 if(line[0].equals("START") && line.length==2)
	    			 { lc=Integer.parseInt(line[1]);}
	    			 else if (line[0].equals("START") && line.length==1){lc=0;}
	    			 else {
	    				 error++;throw new IllegalOpCode("START NOT FOUND"); //START not found exception
	    			 }
	    			 strt++;
	    		 }
	    		 else 
	    		 {
	    			 if(line[0].equals("START")) {error++;throw new IllegalOpCode("START FOUND AGAIN");} // start found more than once error
	    			 else if(line[0].equals("//"))
	    			 {
	    				 continue;
	    			 }
	    			 else 
	    			 {   
	    				 lc++;
	    				 if(line.length>=2 && line[1].equals("DC"))
	    				 {
	    					type=1;
	    					symtab.add(new Symbols(line[0],"variable",line[2],lc));
	    				 }
	    				 else if(l.contains("'") && line.length==2)
	    				 {
	    					 // it is a literal
	    					 littab.add(new literal(line[0],Integer.parseInt(line[1].substring(2,line[1].length()-1))));
	    				 }
	    				 else
	    				 {   String f=null;
	    					 int opcf=0;
	    					 for(int i=0;i<optab.length;i++)
	    					 {
	    						 if(line[0].equals(optab[i].getName()))
	    						 {
	    							 opcf++;
	    						 }
	    						 f=line[0];
	    					 }

	    					 if(f==null && opcf==0) {error++;throw new IllegalOpCode("No legal opcode found");}//opCode out of list
	    					 else  
	    					 {   type=0;
	    						 for(int i=0;i<optab.length;i++)  // opcode is found and instrlen<2
	    						 {
	    							 if(line[0].equals(optab[i].getName())) {type++;}
	    						 }
	    						 if(type!=0)
	    						 {
	    							 if( (line[0].contentEquals("STP")||line[0].equals("CLA")))
	    							 {
	    								 if(line.length==1 && line[0].contentEquals("STP")) {stop++;}
	    								 else if(line.length>1) {error++;throw new MoreOperandsError("No operand required");} // STP or CLA don't require operand exception
	    							 }
	    							 else
	    							 {
	    								 if(line.length<2)
	    								 {
	    									 throw new LessOperandsError("One operand required"); //less operand error
	    								 }
	    								 else if(line.length>2) {error++;throw new MoreOperandsError("only one operand required");}
	    								 else {continue;}
	    							 }
	    							 
	    						 }
	    						 type=0;
		    					 if(line.length>=2) //label present at line[0]
	    						 {
	    							 for(int i=0;i<optab.length;i++)
	    							 {
	    								 if(line[1].equals(optab[i].getName())) {type++;}
	    							 }
	    							 if(type==0) {error++;throw new WrongFormatError("wrong format");}
	    							 else if(type!=0) 
	    							 {   String n=null;
	    								 if(line[1].equals("STP")||line[1].equals("CLA")) 
	    								 {
	    									 if(line.length==3) {error++;throw new MoreOperandsError("no operand required");}
	    									 else if(line.length==2) 
	    									 {  if(line[1].contentEquals("STP"))
	    										 {stop++;}
	    									 
	    									 else{symtab.add(new Symbols(line[0].substring(0,line[0].length()-1),"label",null,lc));}
	    									 }
	    									 
	    								 }
	    								 
	    								 else
	    								 {   
	    									 if(line.length==3) {
	    										 
	    								     if(line[2].substring(0,1).contentEquals("'"))
	    								     {symtab.add(new Symbols(line[0].substring(0,line[0].length()-1),"label",line[2].substring(2,line[2].length()-1),lc));
	    								      littab.add(new literal(line[0].substring(0,line[0].length()-1),Integer.parseInt(line[2].substring(2,line[2].length()-1))));
	    								     }
	    								     
	    								     else{symtab.add(new Symbols(line[0].substring(0,line[0].length()-1),"label",line[2],lc));}
	    										 
	    									 }
	    									 else if (line.length < 3) { // EXCEPTION

	    										 error++;throw new LessOperandsError(" We need atleast one operand");

												} 
	    									 else { // EXCEPTION

	    										 error++;throw new MoreOperandsError(" We need only one operand");

												}
	    								 }
	    							 }
	    						 }
	    					 }
	    				 }
	    				 
	    			 }
	    		 }
	    		 counter++;
	    	 }
	    	 if(counter>256) {error++;throw new InvalidInsError("INVALID INSTRUCTION");}
	    	 if(stop==0) {error++;throw new IllegalOpCode("STP NOT FOUND");}//STP NOT FOUND ERROR
	     }
	 	catch (LessOperandsError e) {
			System.out.println(e.getMessage());
		}
	 	catch (MoreOperandsError e) {
			System.out.println(e.getMessage());
		}
	 	catch (IllegalOpCode e) {
			System.out.println(e.getMessage());
		}
	     catch (InvalidInsError e) {
				System.out.println(e.getMessage());
			}
         catch (WrongFormatError e) {
     				System.out.println(e.getMessage());
     			}
             
	     finally 
	     {   
	    	
				System.out.println("Symbol Table");
				System.out.println("Name  Type    Value   Address");
				for (int j = 0; j < symtab.size(); j++) {
					System.out.println(symtab.get(j).getName()+"    "+symtab.get(j).gettype()+"    "+symtab.get(j).getValue()+"     "+symtab.get(j).getAdd());
				}
				System.out.println();
				
				
				System.out.println("Literal Table");
				System.out.println("Name    Value");
				for (int j = 0; j < littab.size(); j++) {
					
					System.out.println(littab.get(j).toString());
				}

				
	     }
	      return error;
	}

	public static String binGen(int i) {
		/*
		 * Generates 8 bit binary string of a given number
		 */
		String op = Integer.toBinaryString(i);
		while(op.length()<8) {
			op = "0"+op;
		}
		return op;
	}

	public static void pass_two(opCode[] optab,ArrayList<Symbols> symtab,ArrayList<literal> littab,int r) throws IOException
	{
		BufferedReader in = new BufferedReader(new FileReader("input.txt"));
		String l;
		PrintWriter out2 = new PrintWriter(new FileWriter("output.txt"));
		int x=-1;
		if(r==0) {
		while((l=in.readLine())!=null)
		{
			String[] line=l.split(" ");
			String[] newLine= new String[3];
			int flag=0,a=-1;
			if(line.length==3 && line[1].contentEquals("DC")) {continue;}
			if(line.length==1)
			{
				if(line[0].contentEquals("STP")) { System.out.println(binGen(x)+" "+ "1100"+" "+binGen(0));out2.println((binGen(x)+" "+ "1100"+" "+binGen(0)));}
				else if(line[0].contentEquals("CLA")) {  System.out.println(binGen(x)+" "+"0000"+" "+binGen(0));out2.println(binGen(x)+" "+"0000"+" "+binGen(0));}
				
			}
			else if(line.length==2) {
				
			for(int j=0;j<optab.length;j++)
			{   
				if(line[0].equals(optab[j].getName()))
				{  for (int k=0;k<symtab.size();k++) {
					if(symtab.get(k).getName().equals(line[1])) {
					a=symtab.get(k).getAdd();
					}
					}
				if(a==-1) {
				for (int m=0;m<littab.size();m++) {
					if(littab.get(m).getName().equals(line[0])) {
					a=littab.get(m).getValue();
					}
					}
				}
			
				 System.out.println(binGen(x)+" "+optab[j].getCode()+" "+binGen(a));
				 out2.println(binGen(x)+" "+optab[j].getCode()+" "+binGen(a));
				  flag++;
				}
			
		
	
		  }
		if(flag==0)
		{
		   String l0=line[0];
		   String b = null;
		   int add=0;
		   for(int j=0;j<symtab.size();j++)
		   {
			   if(symtab.get(j).getName()==l0) {b=symtab.get(j).gettype(); add=symtab.get(j).getAdd();break;}
		   }
		   if(b.contentEquals("label")) 
		   {
			   if(line[1].contentEquals("STP")||line[1].contentEquals("CLA"))
			   {
				   if(line[1].contentEquals("STP")) {System.out.println(binGen(x)+" "+binGen(add)+" "+binGen(12));out2.println(binGen(x)+" "+binGen(add)+" "+binGen(12));}
				   else { System.out.println(binGen(x)+" "+binGen(add)+" "+binGen(0));out2.println(binGen(x)+" "+binGen(add)+" "+binGen(0));}
			   }
		   }
		}
		}
	    else if(line.length==3)
	    {      String l3=null;
	    	   String l0=line[0].substring(0,line[0].length()-1);
	    	   if(line[2].substring(0,1).contentEquals("'") ) {l3=line[2].substring(2,line[2].length()-1);}
	    	   else {l3=line[2];}
			   String b = null;
			   int add=0;
			   String opadd=null;
			   String b2 = null;
			   int add2=0;
			   for(int j=0;j<symtab.size();j++)
			   {
				   if(symtab.get(j).getName().contentEquals(l0)) {b=symtab.get(j).gettype(); add=symtab.get(j).getAdd();}
				   
	
				   
			   }
			   for(int j=0;j<symtab.size();j++) {
			   if(symtab.get(j).getName().contentEquals(l3)) {b2=symtab.get(j).gettype(); add2=symtab.get(j).getAdd(); }
			   else if(l3.equals(line[2].substring(2,line[2].length()-1))) {add2=Integer.parseInt(l3);}
			   if(add2!=0) {break;}
			   }
			   for(int j=0;j<optab.length;j++)
				{   
					if(line[1].equals(optab[j].getName()))
					{
						opadd=optab[j].getCode();break;
					}
	            }
			    System.out.println(binGen(x)+" "+binGen(add)+" "+opadd+" "+binGen(add2));
			    
			    out2.println(binGen(x)+" "+binGen(add)+" "+opadd+" "+binGen(add2));
			    
	    }
	    x++;
	}

	}
		else {System.out.println("ERROR FOUND"); out2.println("ERROR FOUND");}
}
}
class opCode
{
	String name;
	String code;
	int operand; //no. of operands required
	public opCode(String n,String c,int o)
	{
		name=n;
		code=c;
		operand=o;
	}
	public String getName() {return name;}
	public String getCode() {return code;}
	
}
class Symbols
{
	String name;
	String type;
	String value;
	int addr;
	public Symbols(String n,String t,String val,int ar)
	{
		name=n;
		type=t;
		value=val;
		addr=ar;
	}
	public String getName() {return name;}
	public String gettype() {return type;}
	public String getValue() {return value;}
	public int getAdd() {return addr;}
	
}

class literal
{
	String name;
	int value;
	public literal(String n,int val)
	{
		name=n;
		value=val;
	}
	public String getName() {return name;}
	public int getValue() {return value;}
	
	public String toString() {return name+"      "+value;}
	
}
 class LessOperandsError extends Exception {

	public LessOperandsError(String message) {
		super(message);
	}
}
class MoreOperandsError extends Exception {

		public MoreOperandsError(String message) {
			super(message);
		}
	}
class IllegalOpCode extends Exception {

	public IllegalOpCode(String message) {
		super(message);
	}
}
class InvalidInsError extends Exception {

	public InvalidInsError(String message) {
		super(message);
	}
}
class WrongFormatError extends Exception {

	public WrongFormatError(String message) {
		super(message);
	}
}

