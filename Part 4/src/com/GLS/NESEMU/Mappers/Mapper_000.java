package com.GLS.NESEMU.Mappers;

public class Mapper_000 extends Mapper{

	public Mapper_000(byte prgBanks, byte chrBanks) {
		super(prgBanks, chrBanks);
	}

	public RBI cpuMapRead(short saddr) {
		int addr = saddr&0xFFFF;
		if(addr >= 0x8000 && addr <= 0xFFFF) {
			int map = (nPRGBanks>1?0x7FFF:0x3FFF);
			int mapped_addr = addr&map;
			return new RBI(true, mapped_addr);
		}
		return new RBI(false);
	}

	public RBI cpuMapWrite(short saddr) {
		int addr = saddr&0xFFFF;
		if(addr >= 0x8000 && addr <= 0xFFFF) {
			int mapped_addr = addr&((nPRGBanks>1)?0x7FFF:0x3FFF);
			return new RBI(true, mapped_addr);
		}
		return new RBI(false);
	}

	public RBI ppuMapRead(short saddr) {
		int addr = saddr&0xFFFF;
		if(addr >= 0x0000 && addr <= 0x1FFF) {
			return new RBI(true,addr);
		}
		return new RBI(false);
	}

	public RBI ppuMapWrite(short saddr) {
		int addr = saddr&0xFFFF;
		if(addr >= 0x0000 && addr <= 0x1FFF) {
			if(nCHRBanks==0) {
				return new RBI(true,addr);
			}	
		}
		return new RBI(false);
	}

}
