package com.GLS.NESEMU;

import java.util.HashMap;

public class GLS6502 {
	
	Bus bus;
	
	enum FLAGS6502 {
		C(1<<0),
		Z(1<<1),
		I(1<<2),
		D(1<<3),
		B(1<<4),
		U(1<<5),
		V(1<<6),
		N(1<<7);
		public byte data;
		private FLAGS6502(int data) {
			this.data = (byte)data;
		}
	}
	
	class INSTRUCTION {
		public String name, operate, addrmode;
		public byte cycles = 0;
		public INSTRUCTION(String n, String o, String a, byte c) {
			this.name = n;
			this.operate = o;
			this.addrmode = a;
			this.cycles = c;
		}
	}
	
	byte a, x, y, stkp, status;
	short pc, addr_rel, addr_abs, temp;
	
	byte fetched, opcode;
	short cycles;
	
	INSTRUCTION[] lookup;
	
	public GLS6502() {
		INSTRUCTION[] lookup1 = {
				new INSTRUCTION( "BRK", "BRK", "IMM", (byte)7 ),new INSTRUCTION( "ORA", "ORA", "IZX", (byte)6 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)8 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)3 ),new INSTRUCTION( "ORA", "ORA", "ZP0", (byte)3 ),new INSTRUCTION( "ASL", "ASL", "ZP0", (byte)5 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)5 ),new INSTRUCTION( "PHP", "PHP", "IMP", (byte)3 ),new INSTRUCTION( "ORA", "ORA", "IMM", (byte)2 ),new INSTRUCTION( "ASL", "ASL", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)2 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)4 ),new INSTRUCTION( "ORA", "ORA", "ABS", (byte)4 ),new INSTRUCTION( "ASL", "ASL", "ABS", (byte)6 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)6 ),
				new INSTRUCTION( "BPL", "BPL", "REL", (byte)2 ),new INSTRUCTION( "ORA", "ORA", "IZY", (byte)5 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)8 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)4 ),new INSTRUCTION( "ORA", "ORA", "ZPX", (byte)4 ),new INSTRUCTION( "ASL", "ASL", "ZPX", (byte)6 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)6 ),new INSTRUCTION( "CLC", "CLC", "IMP", (byte)2 ),new INSTRUCTION( "ORA", "ORA", "ABY", (byte)4 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)7 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)4 ),new INSTRUCTION( "ORA", "ORA", "ABX", (byte)4 ),new INSTRUCTION( "ASL", "ASL", "ABX", (byte)7 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)7 ),
				new INSTRUCTION( "JSR", "JSR", "ABS", (byte)6 ),new INSTRUCTION( "AND", "AND", "IZX", (byte)6 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)8 ),new INSTRUCTION( "BIT", "BIT", "ZP0", (byte)3 ),new INSTRUCTION( "AND", "AND", "ZP0", (byte)3 ),new INSTRUCTION( "ROL", "ROL", "ZP0", (byte)5 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)5 ),new INSTRUCTION( "PLP", "PLP", "IMP", (byte)4 ),new INSTRUCTION( "AND", "AND", "IMM", (byte)2 ),new INSTRUCTION( "ROL", "ROL", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)2 ),new INSTRUCTION( "BIT", "BIT", "ABS", (byte)4 ),new INSTRUCTION( "AND", "AND", "ABS", (byte)4 ),new INSTRUCTION( "ROL", "ROL", "ABS", (byte)6 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)6 ),
				new INSTRUCTION( "BMI", "BMI", "REL", (byte)2 ),new INSTRUCTION( "AND", "AND", "IZY", (byte)5 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)8 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)4 ),new INSTRUCTION( "AND", "AND", "ZPX", (byte)4 ),new INSTRUCTION( "ROL", "ROL", "ZPX", (byte)6 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)6 ),new INSTRUCTION( "SEC", "SEC", "IMP", (byte)2 ),new INSTRUCTION( "AND", "AND", "ABY", (byte)4 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)7 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)4 ),new INSTRUCTION( "AND", "AND", "ABX", (byte)4 ),new INSTRUCTION( "ROL", "ROL", "ABX", (byte)7 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)7 ),
				new INSTRUCTION( "RTI", "RTI", "IMP", (byte)6 ),new INSTRUCTION( "EOR", "EOR", "IZX", (byte)6 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)8 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)3 ),new INSTRUCTION( "EOR", "EOR", "ZP0", (byte)3 ),new INSTRUCTION( "LSR", "LSR", "ZP0", (byte)5 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)5 ),new INSTRUCTION( "PHA", "PHA", "IMP", (byte)3 ),new INSTRUCTION( "EOR", "EOR", "IMM", (byte)2 ),new INSTRUCTION( "LSR", "LSR", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)2 ),new INSTRUCTION( "JMP", "JMP", "ABS", (byte)3 ),new INSTRUCTION( "EOR", "EOR", "ABS", (byte)4 ),new INSTRUCTION( "LSR", "LSR", "ABS", (byte)6 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)6 ),
				new INSTRUCTION( "BVC", "BVC", "REL", (byte)2 ),new INSTRUCTION( "EOR", "EOR", "IZY", (byte)5 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)8 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)4 ),new INSTRUCTION( "EOR", "EOR", "ZPX", (byte)4 ),new INSTRUCTION( "LSR", "LSR", "ZPX", (byte)6 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)6 ),new INSTRUCTION( "CLI", "CLI", "IMP", (byte)2 ),new INSTRUCTION( "EOR", "EOR", "ABY", (byte)4 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)7 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)4 ),new INSTRUCTION( "EOR", "EOR", "ABX", (byte)4 ),new INSTRUCTION( "LSR", "LSR", "ABX", (byte)7 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)7 ),
				new INSTRUCTION( "RTS", "RTS", "IMP", (byte)6 ),new INSTRUCTION( "ADC", "ADC", "IZX", (byte)6 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)8 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)3 ),new INSTRUCTION( "ADC", "ADC", "ZP0", (byte)3 ),new INSTRUCTION( "ROR", "ROR", "ZP0", (byte)5 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)5 ),new INSTRUCTION( "PLA", "PLA", "IMP", (byte)4 ),new INSTRUCTION( "ADC", "ADC", "IMM", (byte)2 ),new INSTRUCTION( "ROR", "ROR", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)2 ),new INSTRUCTION( "JMP", "JMP", "IND", (byte)5 ),new INSTRUCTION( "ADC", "ADC", "ABS", (byte)4 ),new INSTRUCTION( "ROR", "ROR", "ABS", (byte)6 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)6 ),
				new INSTRUCTION( "BVS", "BVS", "REL", (byte)2 ),new INSTRUCTION( "ADC", "ADC", "IZY", (byte)5 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)8 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)4 ),new INSTRUCTION( "ADC", "ADC", "ZPX", (byte)4 ),new INSTRUCTION( "ROR", "ROR", "ZPX", (byte)6 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)6 ),new INSTRUCTION( "SEI", "SEI", "IMP", (byte)2 ),new INSTRUCTION( "ADC", "ADC", "ABY", (byte)4 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)7 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)4 ),new INSTRUCTION( "ADC", "ADC", "ABX", (byte)4 ),new INSTRUCTION( "ROR", "ROR", "ABX", (byte)7 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)7 ),
				new INSTRUCTION( "???", "NOP", "IMP", (byte)2 ),new INSTRUCTION( "STA", "STA", "IZX", (byte)6 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)6 ),new INSTRUCTION( "STY", "STY", "ZP0", (byte)3 ),new INSTRUCTION( "STA", "STA", "ZP0", (byte)3 ),new INSTRUCTION( "STX", "STX", "ZP0", (byte)3 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)3 ),new INSTRUCTION( "DEY", "DEY", "IMP", (byte)2 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)2 ),new INSTRUCTION( "TXA", "TXA", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)2 ),new INSTRUCTION( "STY", "STY", "ABS", (byte)4 ),new INSTRUCTION( "STA", "STA", "ABS", (byte)4 ),new INSTRUCTION( "STX", "STX", "ABS", (byte)4 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)4 ),
				new INSTRUCTION( "BCC", "BCC", "REL", (byte)2 ),new INSTRUCTION( "STA", "STA", "IZY", (byte)6 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)6 ),new INSTRUCTION( "STY", "STY", "ZPX", (byte)4 ),new INSTRUCTION( "STA", "STA", "ZPX", (byte)4 ),new INSTRUCTION( "STX", "STX", "ZPY", (byte)4 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)4 ),new INSTRUCTION( "TYA", "TYA", "IMP", (byte)2 ),new INSTRUCTION( "STA", "STA", "ABY", (byte)5 ),new INSTRUCTION( "TXS", "TXS", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)5 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)5 ),new INSTRUCTION( "STA", "STA", "ABX", (byte)5 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)5 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)5 ),
				new INSTRUCTION( "LDY", "LDY", "IMM", (byte)2 ),new INSTRUCTION( "LDA", "LDA", "IZX", (byte)6 ),new INSTRUCTION( "LDX", "LDX", "IMM", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)6 ),new INSTRUCTION( "LDY", "LDY", "ZP0", (byte)3 ),new INSTRUCTION( "LDA", "LDA", "ZP0", (byte)3 ),new INSTRUCTION( "LDX", "LDX", "ZP0", (byte)3 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)3 ),new INSTRUCTION( "TAY", "TAY", "IMP", (byte)2 ),new INSTRUCTION( "LDA", "LDA", "IMM", (byte)2 ),new INSTRUCTION( "TAX", "TAX", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)2 ),new INSTRUCTION( "LDY", "LDY", "ABS", (byte)4 ),new INSTRUCTION( "LDA", "LDA", "ABS", (byte)4 ),new INSTRUCTION( "LDX", "LDX", "ABS", (byte)4 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)4 ),
				new INSTRUCTION( "BCS", "BCS", "REL", (byte)2 ),new INSTRUCTION( "LDA", "LDA", "IZY", (byte)5 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)5 ),new INSTRUCTION( "LDY", "LDY", "ZPX", (byte)4 ),new INSTRUCTION( "LDA", "LDA", "ZPX", (byte)4 ),new INSTRUCTION( "LDX", "LDX", "ZPY", (byte)4 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)4 ),new INSTRUCTION( "CLV", "CLV", "IMP", (byte)2 ),new INSTRUCTION( "LDA", "LDA", "ABY", (byte)4 ),new INSTRUCTION( "TSX", "TSX", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)4 ),new INSTRUCTION( "LDY", "LDY", "ABX", (byte)4 ),new INSTRUCTION( "LDA", "LDA", "ABX", (byte)4 ),new INSTRUCTION( "LDX", "LDX", "ABY", (byte)4 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)4 ),
				new INSTRUCTION( "CPY", "CPY", "IMM", (byte)2 ),new INSTRUCTION( "CMP", "CMP", "IZX", (byte)6 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)8 ),new INSTRUCTION( "CPY", "CPY", "ZP0", (byte)3 ),new INSTRUCTION( "CMP", "CMP", "ZP0", (byte)3 ),new INSTRUCTION( "DEC", "DEC", "ZP0", (byte)5 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)5 ),new INSTRUCTION( "INY", "INY", "IMP", (byte)2 ),new INSTRUCTION( "CMP", "CMP", "IMM", (byte)2 ),new INSTRUCTION( "DEX", "DEX", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)2 ),new INSTRUCTION( "CPY", "CPY", "ABS", (byte)4 ),new INSTRUCTION( "CMP", "CMP", "ABS", (byte)4 ),new INSTRUCTION( "DEC", "DEC", "ABS", (byte)6 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)6 ),
				new INSTRUCTION( "BNE", "BNE", "REL", (byte)2 ),new INSTRUCTION( "CMP", "CMP", "IZY", (byte)5 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)8 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)4 ),new INSTRUCTION( "CMP", "CMP", "ZPX", (byte)4 ),new INSTRUCTION( "DEC", "DEC", "ZPX", (byte)6 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)6 ),new INSTRUCTION( "CLD", "CLD", "IMP", (byte)2 ),new INSTRUCTION( "CMP", "CMP", "ABY", (byte)4 ),new INSTRUCTION( "NOP", "NOP", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)7 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)4 ),new INSTRUCTION( "CMP", "CMP", "ABX", (byte)4 ),new INSTRUCTION( "DEC", "DEC", "ABX", (byte)7 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)7 ),
				new INSTRUCTION( "CPX", "CPX", "IMM", (byte)2 ),new INSTRUCTION( "SBC", "SBC", "IZX", (byte)6 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)8 ),new INSTRUCTION( "CPX", "CPX", "ZP0", (byte)3 ),new INSTRUCTION( "SBC", "SBC", "ZP0", (byte)3 ),new INSTRUCTION( "INC", "INC", "ZP0", (byte)5 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)5 ),new INSTRUCTION( "INX", "INX", "IMP", (byte)2 ),new INSTRUCTION( "SBC", "SBC", "IMM", (byte)2 ),new INSTRUCTION( "NOP", "NOP", "IMP", (byte)2 ),new INSTRUCTION( "???", "SBC", "IMP", (byte)2 ),new INSTRUCTION( "CPX", "CPX", "ABS", (byte)4 ),new INSTRUCTION( "SBC", "SBC", "ABS", (byte)4 ),new INSTRUCTION( "INC", "INC", "ABS", (byte)6 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)6 ),
				new INSTRUCTION( "BEQ", "BEQ", "REL", (byte)2 ),new INSTRUCTION( "SBC", "SBC", "IZY", (byte)5 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)8 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)4 ),new INSTRUCTION( "SBC", "SBC", "ZPX", (byte)4 ),new INSTRUCTION( "INC", "INC", "ZPX", (byte)6 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)6 ),new INSTRUCTION( "SED", "SED", "IMP", (byte)2 ),new INSTRUCTION( "SBC", "SBC", "ABY", (byte)4 ),new INSTRUCTION( "NOP", "NOP", "IMP", (byte)2 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)7 ),new INSTRUCTION( "???", "NOP", "IMP", (byte)4 ),new INSTRUCTION( "SBC", "SBC", "ABX", (byte)4 ),new INSTRUCTION( "INC", "INC", "ABX", (byte)7 ),new INSTRUCTION( "???", "XXX", "IMP", (byte)7 )
		};
		lookup = lookup1;
		lookup1 = null;
	}
	
	public void connectBus(Bus bus) {
		this.bus = bus;
	}
	
	byte read(short addr) {
		byte data = bus.cpuRead(addr, false);
		return data;
	}
	
	void write(short addr, byte data) {
		bus.cpuWrite(addr, data);
	}
	
	boolean getFlag(FLAGS6502 f) {
		return (status&f.data)!=0;
	}
	
	void setFlag(FLAGS6502 f, boolean b) {
		if (b)
			status |= f.data;
		else
			status &= ~f.data;
	}
	
	Object methodCaller(Object theObject, String methodName) {
		Object method = null;
		try {
			method = theObject.getClass().getMethod(methodName).invoke(theObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return method;
	}
	
	public void clock() {
		if(cycles == 0) {
			setFlag(FLAGS6502.U,true);
			opcode = read(pc);
			pc++;	
			cycles = lookup[opcode&0xFF].cycles;
			byte additionalCycles1 = (byte) methodCaller(this, lookup[opcode&0xFF].addrmode);
			byte additionalCycles2 = (byte) methodCaller(this, lookup[opcode&0xFF].operate);
			cycles+=(additionalCycles1&additionalCycles2);
			setFlag(FLAGS6502.U,true);
		}
		cycles--;
	}
	
	public void reset() {
		a = 0;
		x = 0;
		y = 0;
		stkp = (byte) 0xFD;
		status = (byte) (0x00 | FLAGS6502.U.data);
		addr_abs = (short) 0xFFFC;
		int lo = read(addr_abs)&0xFF;
		int hi = read((short) ((addr_abs&0xFFFF) + 1))&0xFF;
		pc = (short) ((hi << 8) | lo);
		addr_rel = 0x0000;
		addr_abs = 0x0000;
		fetched = 0x00;
		cycles = 8;
	}
	
	public void irq() {
		if(getFlag(FLAGS6502.I)==false) {
			write((short)(0x0100+stkp),(byte)((pc>>8)&0x00FF));
			stkp--;
			write((short)(0x0100+stkp),(byte)(pc&0x00FF));
			stkp--;
			setFlag(FLAGS6502.B, false);
			setFlag(FLAGS6502.U, true);
			setFlag(FLAGS6502.I, true);
			write((short)(0x0100+stkp),status);
			stkp--;
			addr_abs = (short) 0xFFFE;
			short lo = read(addr_abs);
			short hi = read((short) ((addr_abs&0xFFFF)+1));
			pc = (short) ((hi<<8)|lo);
			cycles = 7;
		}
	}
	
	public void nmi() {
		write((short)(0x0100+stkp),(byte)((pc>>8)&0x00FF));
		stkp--;
		write((short)(0x0100+stkp),(byte)(pc&0x00FF));
		stkp--;
		setFlag(FLAGS6502.B, false);
		setFlag(FLAGS6502.U, true);
		setFlag(FLAGS6502.I, true);
		write((short)(0x0100+stkp),status);
		stkp--;
		addr_abs = (short) 0xFFFA;
		short lo = read(addr_abs);
		short hi = read((short) ((addr_abs&0xFFFF)+1));
		pc = (short) ((hi<<8)|lo);
		cycles = 8;
	}
	
	public byte IMP()
	{
		fetched = a;
		return 0;
	}

	public byte IMM()
	{
		addr_abs = pc++;	
		return 0;
	}

	public byte ZP0()
	{
		addr_abs = (short) (read(pc)&0xFF);	
		pc++;
		addr_abs &= 0x00FF;
		return 0;
	}

	public byte ZPX()
	{
		addr_abs = (short) (read(pc)&0xFF + x&0xFF);
		pc++;
		addr_abs &= 0x00FF;
		return 0;
	}

	public byte ZPY()
	{
		addr_abs = (short) (read(pc)&0xFF + y&0xFF);
		pc++;
		addr_abs &= 0x00FF;
		return 0;
	}

	public byte REL()
	{
		addr_rel = (short) (read(pc)&0xFFFF);
		pc++;
		if (((addr_rel&0xFF) & 0x80) !=0) {
			addr_rel = (short)((addr_rel&0xFFFF)|0xFF00);
		}		
		return 0;
	}

	public byte ABS()
	{
		short lo = (short) (read(pc)&0xFF);
		pc++;
		short hi = (short) (read(pc)&0xFF);
		pc++;

		addr_abs = (short) ((hi << 8) | lo);
		return 0;
	}

	public byte ABX()
	{
		short lo = (short) (read(pc)&0xFF);
		pc++;
		short hi = (short) (read(pc)&0xFF);
		pc++;

		addr_abs = (short) ((hi << 8) | lo);
		addr_abs += x;

		if ((addr_abs & 0xFF00) != (hi << 8))
			return 1;
		else
			return 0;	
	}

	public byte ABY()
	{
		short lo = (short) (read(pc)&0xFF);
		pc++;
		short hi = (short) (read(pc)&0xFF);
		pc++;

		addr_abs = (short) ((hi << 8) | lo);
		addr_abs += y;

		if ((addr_abs & 0xFF00) != (hi << 8))
			return 1;
		else
			return 0;
	}

	public byte IND()
	{
		short ptr_lo = (short) (read(pc)&0xFF);
		pc++;
		short ptr_hi = (short) (read(pc)&0xFF);
		pc++;

		short ptr = (short) (((ptr_hi&0x00FF) << 8) | ptr_lo&0x00FF);

		if (ptr_lo == 0x00FF) // Simulate page boundary hardware bug
		{
			addr_abs = (short) ((read((short) (ptr & 0xFF00))&0xFF << 8) | read(ptr)&0xFF);
		}
		else // Behave normally
		{
			addr_abs = (short) ((read((short) (ptr + 1))&0xFF << 8) | read(ptr)&0xFF);
		}
		
		return 0;
	}

	public byte IZX()
	{
		short t = (short) (read(pc)&0xFF);
		pc++;

		short lo = (short) (read((short) ((short)(t + (short)x) & 0x00FF))&0xFF);
		short hi = (short) (read((short) ((short)(t + (short)x + 1) & 0x00FF))&0xFF);

		addr_abs = (short) ((hi << 8) | lo);
		
		return 0;
	}

	public byte IZY()
	{
		short t = (short) (read(pc)&0xFF);
		pc++;

		short lo = (short) (read((short) (t & 0x00FF))&0xFF);
		short hi = (short) (read((short) ((t + 1) & 0x00FF))&0xFF);

		addr_abs = (short) ((hi << 8) | lo);
		addr_abs += y;
		
		if ((addr_abs & 0xFF00) != (hi << 8))
			return 1;
		else
			return 0;
	}

	public byte fetch()
	{
		if (!(lookup[opcode&0xFF].addrmode == "IMP"))
			fetched = read(addr_abs);
		return fetched;
	}

	public byte ADC()
	{
		fetch();
		
		temp = (short) ((short)a + (short)fetched + (getFlag(FLAGS6502.C)?1:0));
		
		setFlag(FLAGS6502.C, temp > 255);
		
		setFlag(FLAGS6502.Z, (temp & 0x00FF) == 0);
		
		setFlag(FLAGS6502.V, ((~((short)a ^ (short)fetched) & ((short)a ^ (short)temp)) & 0x0080) !=0);
		
		setFlag(FLAGS6502.N, (temp & 0x80) != 0);
		
		a = (byte) (temp & 0x00FF);
		
		return 1;
	}

	public byte SBC()
	{
		fetch();
		
		short value = (short) (((short)fetched) ^ 0x00FF);
		
		temp = (short) ((short)a + value + (getFlag(FLAGS6502.C)?1:0));
		setFlag(FLAGS6502.C, (temp & 0xFF00) !=0);
		setFlag(FLAGS6502.Z, ((temp & 0x00FF) == 0));
		setFlag(FLAGS6502.V, ((temp ^ (short)a) & (temp ^ value) & 0x0080) !=0);
		setFlag(FLAGS6502.N, (temp & 0x0080) !=0);
		a = (byte) (temp & 0x00FF);
		return 1;
	}

	public byte AND()
	{
		fetch();
		a = (byte) (a & fetched);
		setFlag(FLAGS6502.Z, a == 0x00);
		setFlag(FLAGS6502.N, (a & 0x80)!=0);
		return 1;
	}

	public byte ASL()
	{
		fetch();
		temp = (short) ((short)fetched << 1);
		setFlag(FLAGS6502.C, (temp & 0xFF00) > 0);
		setFlag(FLAGS6502.Z, (temp & 0x00FF) == 0x00);
		setFlag(FLAGS6502.N, (temp & 0x80)!=0);
		if (lookup[opcode&0xFF].addrmode == "IMP")
			a = (byte) (temp & 0x00FF);
		else
			write(addr_abs, (byte) (temp & 0x00FF));
		return 0;
	}

	public byte BCC()
	{
		if (!getFlag(FLAGS6502.C))
		{
			cycles++;
			addr_abs = (short) (pc + addr_rel);
			
			if((addr_abs & 0xFF00) != (pc & 0xFF00))
				cycles++;
			
			pc = addr_abs;
		}
		return 0;
	}

	public byte BCS()
	{
		if (getFlag(FLAGS6502.C))
		{
			cycles++;
			addr_abs = (short) (pc + addr_rel);

			if ((addr_abs & 0xFF00) != (pc & 0xFF00))
				cycles++;

			pc = addr_abs;
		}
		return 0;
	}

	public byte BEQ()
	{
		if (getFlag(FLAGS6502.Z))
		{
			cycles++;
			addr_abs = (short) (pc + addr_rel);

			if ((addr_abs & 0xFF00) != (pc & 0xFF00))
				cycles++;

			pc = addr_abs;
		}
		return 0;
	}

	public byte BIT()
	{
		fetch();
		temp = (short) (a & fetched);
		setFlag(FLAGS6502.Z, (temp & 0x00FF) == 0x00);
		setFlag(FLAGS6502.N, (fetched & (1 << 7))!=0);
		setFlag(FLAGS6502.V, (fetched & (1 << 6))!=0);
		return 0;
	}

	public byte BMI()
	{
		if (getFlag(FLAGS6502.N))
		{
			cycles++;
			addr_abs = (short) (pc + addr_rel);

			if ((addr_abs & 0xFF00) != (pc & 0xFF00))
				cycles++;

			pc = addr_abs;
		}
		return 0;
	}

	public byte BNE()
	{
		if (!getFlag(FLAGS6502.Z))
		{
			cycles++;
			addr_abs = (short) ((pc&0xFFFF) + (addr_rel&0xFFFF));

			if ((addr_abs & 0xFF00) != (pc & 0xFF00))
				cycles++;
			
			pc = addr_abs;
		}
		return 0;
	}

	public byte BPL()
	{
		if (!getFlag(FLAGS6502.N))
		{
			cycles++;
			addr_abs = (short) (pc + addr_rel);

			if ((addr_abs & 0xFF00) != (pc & 0xFF00))
				cycles++;

			pc = addr_abs;
		}
		return 0;
	}

	public byte BRK()
	{
		pc++;
		
		setFlag(FLAGS6502.I, true);
		write((short) (0x0100 + stkp), (byte)((pc >> 8) & 0x00FF));
		stkp--;
		write((short) (0x0100 + stkp), (byte)(pc & 0x00FF));
		stkp--;

		setFlag(FLAGS6502.B, true);
		write((short) (0x0100 + stkp), status);
		stkp--;
		setFlag(FLAGS6502.B, false);

		pc = (short) ((short)read((short) 0xFFFE)&0xFF | ((short)read((short) 0xFFFF)&0xFF << 8));
		return 0;
	}

	public byte BVC()
	{
		if (!getFlag(FLAGS6502.V))
		{
			cycles++;
			addr_abs = (short) (pc + addr_rel);

			if ((addr_abs & 0xFF00) != (pc & 0xFF00))
				cycles++;

			pc = addr_abs;
		}
		return 0;
	}

	public byte BVS()
	{
		if (getFlag(FLAGS6502.V))
		{
			cycles++;
			addr_abs = (short) (pc + addr_rel);

			if ((addr_abs & 0xFF00) != (pc & 0xFF00))
				cycles++;

			pc = addr_abs;
		}
		return 0;
	}

	public byte CLC()
	{
		setFlag(FLAGS6502.C, false);
		return 0;
	}

	public byte CLD()
	{
		setFlag(FLAGS6502.D, false);
		return 0;
	}

	public byte CLI()
	{
		setFlag(FLAGS6502.I, false);
		return 0;
	}

	public byte CLV()
	{
		setFlag(FLAGS6502.V, false);
		return 0;
	}

	public byte CMP()
	{
		fetch();
		temp = (short) ((short)a - (short)fetched);
		setFlag(FLAGS6502.C, a >= fetched);
		setFlag(FLAGS6502.Z, (temp & 0x00FF) == 0x0000);
		setFlag(FLAGS6502.N, (temp & 0x0080)!=0);
		return 1;
	}

	public byte CPX()
	{
		fetch();
		temp = (short) ((short)x - (short)fetched);
		setFlag(FLAGS6502.C, x >= fetched);
		setFlag(FLAGS6502.Z, (temp & 0x00FF) == 0x0000);
		setFlag(FLAGS6502.N, (temp & 0x0080)!=0);
		return 0;
	}

	public byte CPY()
	{
		fetch();
		temp = (short) ((short)y - (short)fetched);
		setFlag(FLAGS6502.C, y >= fetched);
		setFlag(FLAGS6502.Z, (temp & 0x00FF) == 0x0000);
		setFlag(FLAGS6502.N, (temp & 0x0080)!=0);
		return 0;
	}

	public byte DEC()
	{
		fetch();
		temp = (short) (fetched - 1);
		write(addr_abs, (byte) (temp & 0x00FF));
		setFlag(FLAGS6502.Z, (temp & 0x00FF) == 0x0000);
		setFlag(FLAGS6502.N, (temp & 0x0080)!=0);
		return 0;
	}

	public byte DEX()
	{
		x--;
		setFlag(FLAGS6502.Z, x == 0x00);
		setFlag(FLAGS6502.N, (x & 0x80)!=0);
		return 0;
	}

	public byte DEY()
	{
		y--;
		setFlag(FLAGS6502.Z, y == 0x00);
		setFlag(FLAGS6502.N, (y & 0x80)!=0);
		return 0;
	}

	public byte EOR()
	{
		fetch();
		a = (byte) (a ^ fetched);	
		setFlag(FLAGS6502.Z, a == 0x00);
		setFlag(FLAGS6502.N, (a & 0x80)!=0);
		return 1;
	}

	public byte INC()
	{
		fetch();
		temp = (short) (fetched + 1);
		write(addr_abs, (byte) (temp & 0x00FF));
		setFlag(FLAGS6502.Z, (temp & 0x00FF) == 0x0000);
		setFlag(FLAGS6502.N, (temp & 0x0080)!=0);
		return 0;
	}

	public byte INX()
	{
		x++;
		setFlag(FLAGS6502.Z, x == 0x00);
		setFlag(FLAGS6502.N, (x & 0x80)!=0);
		return 0;
	}

	public byte INY()
	{
		y++;
		setFlag(FLAGS6502.Z, y == 0x00);
		setFlag(FLAGS6502.N, (y & 0x80)!=0);
		return 0;
	}

	public byte JMP()
	{
		pc = addr_abs;
		return 0;
	}

	public byte JSR()
	{
		pc--;

		write((short) (0x0100 + (stkp&0xFF)), (byte)((pc&0xFF00 >> 8) & 0x00FF));
		stkp--;
		write((short) (0x0100 + (stkp&0xFF)), (byte)(pc & 0x00FF));
		stkp--;

		pc = addr_abs;
		return 0;
	}

	public byte LDA()
	{
		fetch();
		a = fetched;
		setFlag(FLAGS6502.Z, a == 0x00);
		setFlag(FLAGS6502.N, (a & 0x80)!=0);
		return 1;
	}

	public byte LDX()
	{
		fetch();
		x = fetched;
		setFlag(FLAGS6502.Z, x == 0x00);
		setFlag(FLAGS6502.N, (x & 0x80)!=0);
		return 1;
	}

	public byte LDY()
	{
		fetch();
		y = fetched;
		setFlag(FLAGS6502.Z, y == 0x00);
		setFlag(FLAGS6502.N, (y & 0x80)!=0);
		return 1;
	}

	public byte LSR()
	{
		fetch();
		setFlag(FLAGS6502.C, (fetched & 0x0001)!=0);
		temp = (short) (fetched >> 1);	
		setFlag(FLAGS6502.Z, (temp & 0x00FF) == 0x0000);
		setFlag(FLAGS6502.N, (temp & 0x0080)!=0);
		if (lookup[opcode&0xFF].addrmode == "IMP")
			a = (byte) (temp & 0x00FF);
		else
			write(addr_abs, (byte) (temp & 0x00FF));
		return 0;
	}

	public byte NOP()
	{
		if((opcode&0xFF)==0x1C||(opcode&0xFF)==0x3C||(opcode&0xFF)==0x5C||(opcode&0xFF)==0x7C||(opcode&0xFF)==0xDC||(opcode&0xFF)==0xFC)
			return 1;
		return 0;
	}

	public byte ORA()
	{
		fetch();
		a = (byte) (a | fetched);
		setFlag(FLAGS6502.Z, a == 0x00);
		setFlag(FLAGS6502.N, (a & 0x80)!=0);
		return 1;
	}

	public byte PHA()
	{
		write((short) (0x0100 + stkp), a);
		stkp--;
		return 0;
	}

	public byte PHP()
	{
		write((short) (0x0100 + stkp), (byte)(status | FLAGS6502.B.data | FLAGS6502.U.data));
		setFlag(FLAGS6502.B, false);
		setFlag(FLAGS6502.U, false);
		stkp--;
		return 0;
	}

	public byte PLA()
	{
		stkp++;
		a = read((short) (0x0100 + stkp));
		setFlag(FLAGS6502.Z, a == 0x00);
		setFlag(FLAGS6502.N, (a & 0x80)!=0);
		return 0;
	}

	public byte PLP()
	{
		stkp++;
		status = read((short) (0x0100 + stkp));
		setFlag(FLAGS6502.U, true);
		return 0;
	}

	public byte ROL()
	{
		fetch();
		temp = (short) ((short)(fetched << 1) | (getFlag(FLAGS6502.C)?1:0));
		setFlag(FLAGS6502.C, (temp & 0xFF00)!=0);
		setFlag(FLAGS6502.Z, (temp & 0x00FF) == 0x0000);
		setFlag(FLAGS6502.N, (temp & 0x0080)!=0);
		if (lookup[opcode&0xFF].addrmode == "IMP")
			a = (byte) (temp & 0x00FF);
		else
			write(addr_abs, (byte) (temp & 0x00FF));
		return 0;
	}

	public byte ROR()
	{
		fetch();
		temp = (short) ((short)((getFlag(FLAGS6502.C)?1:0) << 7) | (fetched >> 1));
		setFlag(FLAGS6502.C, (fetched & 0x01)!=0);
		setFlag(FLAGS6502.Z, (temp & 0x00FF) == 0x00);
		setFlag(FLAGS6502.N, (temp & 0x0080)!=0);
		if (lookup[opcode&0xFF].addrmode == "IMP")
			a = (byte) (temp & 0x00FF);
		else
			write(addr_abs, (byte) (temp & 0x00FF));
		return 0;
	}

	public byte RTI()
	{
		stkp++;
		status = read((short) (0x0100 + stkp));
		status &= ~FLAGS6502.B.data;
		status &= ~FLAGS6502.U.data;

		stkp++;
		pc = (short) (read((short) (0x0100 + stkp))&0xFF);
		stkp++;
		pc |= (short)(read((short) (0x0100 + stkp))&0xFF) << 8;
		return 0;
	}

	public byte RTS()
	{
		stkp++;
		pc = (short)(read((short) (0x0100 + stkp))&0xFF);
		stkp++;
		pc |= (short)(read((short) (0x0100 + stkp))&0xFF) << 8;
		
		pc++;
		return 0;
	}

	public byte SEC()
	{
		setFlag(FLAGS6502.C, true);
		return 0;
	}

	public byte SED()
	{
		setFlag(FLAGS6502.D, true);
		return 0;
	}

	public byte SEI()
	{
		setFlag(FLAGS6502.I, true);
		return 0;
	}

	public byte STA()
	{
		write(addr_abs, a);
		return 0;
	}

	public byte STX()
	{
		write(addr_abs, x);
		return 0;
	}

	public byte STY()
	{
		write(addr_abs, y);
		return 0;
	}

	public byte TAX()
	{
		x = a;
		setFlag(FLAGS6502.Z, x == 0x00);
		setFlag(FLAGS6502.N, (x & 0x80)!=0);
		return 0;
	}

	public byte TAY()
	{
		y = a;
		setFlag(FLAGS6502.Z, y == 0x00);
		setFlag(FLAGS6502.N, (y & 0x80)!=0);
		return 0;
	}

	public byte TSX()
	{
		x = stkp;
		setFlag(FLAGS6502.Z, x == 0x00);
		setFlag(FLAGS6502.N, (x & 0x80)!=0);
		return 0;
	}

	public byte TXA()
	{
		a = x;
		setFlag(FLAGS6502.Z, a == 0x00);
		setFlag(FLAGS6502.N, (a & 0x80)!=0);
		return 0;
	}

	public byte TXS()
	{
		stkp = x;
		return 0;
	}

	public byte TYA()
	{
		a = y;
		setFlag(FLAGS6502.Z, a == 0x00);
		setFlag(FLAGS6502.N, (a & 0x80)!=0);
		return 0;
	}

	public byte XXX()
	{
		return 0;
	}
	
	public boolean complete() {
		return cycles == 0;
	}
	
	String hex(int n, int d) {
		String s = new String(new char[d]);
		for (int i = d - 1; i >= 0; i--, n >>= 4)
			s = changeCharInString(i, "0123456789ABCDEF".charAt(n & 0xF), s);
		return s;
	}

	String changeCharInString(int pos, char c, String s) {
		return s.substring(0, pos) + c + s.substring(pos + 1);
	}

	public HashMap<Integer, String> disassemble(int i, int j){
		int addr = i;
		byte value = 0x00, lo = 0x00, hi = 0x00;
		HashMap<Integer, String> mapLines = new HashMap<Integer, String>();
		int line_addr = 0;

		while (addr <= (int)j) {
			line_addr = addr;
			String sInst = "$" + hex(addr, 4) + ": ";
			byte opcode = bus.cpuRead((short)addr, true); addr++;
			sInst += lookup[opcode&0xFF].name + " ";
			if (lookup[opcode&0xFF].addrmode == "IMP")
			{
				sInst += " {IMP}";
			}
			else if (lookup[opcode&0xFF].addrmode == "IMM")
			{
				value = bus.cpuRead((short)addr, true); addr++;
				sInst += "#$" + hex(value&0xFF, 2) + " {IMM}";
			}
			else if (lookup[opcode&0xFF].addrmode == "ZP0")
			{
				lo = bus.cpuRead((short)addr, true); addr++;
				hi = 0x00;												
				sInst += "$" + hex(lo&0xFF, 2) + " {ZP0}";
			}
			else if (lookup[opcode&0xFF].addrmode == "ZPX")
			{
				lo = bus.cpuRead((short)addr, true); addr++;
				hi = 0x00;														
				sInst += "$" + hex(lo&0xFF, 2) + ", X {ZPX}";
			}
			else if (lookup[opcode&0xFF].addrmode == "ZPY")
			{
				lo = bus.cpuRead((short)addr, true); addr++;
				hi = 0x00;														
				sInst += "$" + hex(lo&0xFF, 2) + ", Y {ZPY}";
			}
			else if (lookup[opcode&0xFF].addrmode == "IZX")
			{
				lo = bus.cpuRead((short)addr, true); addr++;
				hi = 0x00;								
				sInst += "($" + hex(lo&0xFF, 2) + ", X) {IZX}";
			}
			else if (lookup[opcode&0xFF].addrmode == "IZY")
			{
				lo = bus.cpuRead((short)addr, true); addr++;
				hi = 0x00;								
				sInst += "($" + hex(lo&0xFF, 2) + "), Y {IZY}";
			}
			else if (lookup[opcode&0xFF].addrmode == "ABS")
			{
				lo = bus.cpuRead((short)addr, true); addr++;
				hi = bus.cpuRead((short)addr, true); addr++;
				sInst += "$" + hex(((hi << 8) | lo)&0xFF, 4) + " {ABS}";
			}
			else if (lookup[opcode&0xFF].addrmode == "ABX")
			{
				lo = bus.cpuRead((short)addr, true); addr++;
				hi = bus.cpuRead((short)addr, true); addr++;
				sInst += "$" + hex(((hi << 8) | lo)&0xFF, 4) + ", X {ABX}";
			}
			else if (lookup[opcode&0xFF].addrmode == "ABY")
			{
				lo = bus.cpuRead((short)addr, true); addr++;
				hi = bus.cpuRead((short)addr, true); addr++;
				sInst += "$" + hex(((hi << 8) | lo)&0xFF, 4) + ", Y {ABY}";
			}
			else if (lookup[opcode&0xFF].addrmode == "IND")
			{
				lo = bus.cpuRead((short)addr, true); addr++;
				hi = bus.cpuRead((short)addr, true); addr++;
				sInst += "($" + hex(((hi << 8) | lo)&0xFF, 4) + ") {IND}";
			}
			else if (lookup[opcode&0xFF].addrmode == "REL")
			{
				value = bus.cpuRead((short)addr, true); addr++;
				sInst += "$" + hex(value&0xFF, 2) + " [$" + hex(addr + (byte)value, 4) + "] {REL}";
			}
			mapLines.put(line_addr, sInst);
		}

		return mapLines;
	}
}
