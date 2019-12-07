package com.GLS.NESEMU.Mappers;

public abstract class Mapper {
	
	protected byte nPRGBanks, nCHRBanks;
	
	public Mapper(byte prgBanks, byte chrBanks) {
		nPRGBanks = prgBanks;
		nCHRBanks = chrBanks;
	}
	
	public abstract RBI cpuMapRead(short saddr);
	public abstract RBI cpuMapWrite(short saddr);
	public abstract RBI ppuMapRead(short saddr);
	public abstract RBI ppuMapWrite(short saddr);
	
	public class RBI {
		public boolean bool;
		public int b;
		public RBI(boolean bool, int b) {
			this.bool = bool;
			this.b = b;
		}
		public RBI(boolean bool) {
			this.bool = bool;
			this.b = 0;
		}
	}

}
