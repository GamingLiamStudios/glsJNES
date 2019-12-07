package com.GLS.NESEMU;

import com.GLS.NESEMU.Cartridge.RBB;

public class Bus {
	
	GLS6502 cpu;
	GLS2C02 ppu;
	
	byte[] cpuRam = new byte[2048];
	Cartridge cart;
	
	long nSystemClockCounter = 0;
	
	public Bus() {
		cpu = new GLS6502();
		ppu = new GLS2C02();
		cpu.connectBus(this);
	}
	
	public void insertCartridge(Cartridge cartridge) {
		this.cart = cartridge;
		ppu.connectCartridge(cartridge);
	}
	
	public void reset() {
		cpu.reset();
		nSystemClockCounter = 0;
	}
	
	public void clock() {
		ppu.clock();
		if(nSystemClockCounter%3==0) cpu.clock();
		if(ppu.nmi) {
			ppu.nmi = false;
			cpu.nmi();
		}
		nSystemClockCounter++;
	}
	
	public void cpuWrite(short saddr, byte data) {
		int addr = saddr&0xFFFF;
		if(cart.cpuWrite(saddr, data));
		else if(addr >= 0x0000 && addr <= 0x1FFF) cpuRam[addr&0x07FF] = data;
		else if(addr >= 0x2000 && addr <= 0x3FFF) ppu.cpuWrite((short)(addr&0x0007), data);
	}
	
	public byte cpuRead(short saddr, boolean readOnly) {
		int addr = saddr&0xFFFF;
		byte data = 0x00;
		RBB r = cart.cpuRead(saddr);
		if(r.bool) data = r.b;
		if(addr >= 0x0000 && addr <= 0x1FFF) data = cpuRam[addr&0x07FF];
		else if(addr >= 0x2000 && addr <= 0x3FFF) data = ppu.cpuRead((short)(addr&0x0007), readOnly);
		return data;
	}

}
