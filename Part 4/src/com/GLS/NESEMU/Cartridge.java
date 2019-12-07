package com.GLS.NESEMU;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.GLS.NESEMU.Mappers.*;
import com.GLS.NESEMU.Mappers.Mapper.RBI;

public class Cartridge {
	
	byte[] vPRGMemory, vCHRMemory;
	byte nMapperID, nPRGBanks, nCHRBanks;
	Mapper pMapper;
	
	class sHeader {
		char[] name = new char[4]; //0-3
		byte prg_rom_chunks; //4
		byte chr_rom_chunks; //5
		byte mapper1; //6
		byte mapper2; //7
		byte prg_ram_size; //8
		byte tv_system1; //9
		byte tv_system2; //10
		char[] unused = new char[5]; //11-15
	}
	
	public Cartridge(String fileName) {
		vPRGMemory = null;
		vCHRMemory = null;
		try(InputStream in = new FileInputStream(new File(fileName))) {
			byte[] header = new byte[16];
			in.read(header);
			if(((header[6]&0xFF)&0x04)!=0) in.read(new byte[512]);
			nMapperID = (byte) ((((header[7]&0xFF)>>4)<<4)|(header[6]&0xFF)>>4);
			byte nFileType = 1;
			if(nFileType == 1) {
				nPRGBanks = header[4];
				vPRGMemory = new byte[nPRGBanks*16384];
				in.read(vPRGMemory);
				nCHRBanks = header[5];
				if(nCHRBanks == 0) vCHRMemory = new byte[8192]; else vCHRMemory = new byte[nCHRBanks*8192];
				in.read(vCHRMemory);
			}
			switch(nMapperID) {
			case 0:
				pMapper = new Mapper_000(nPRGBanks, nCHRBanks);
				break;
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean cpuWrite(short saddr, byte data) {
		RBI m = pMapper.cpuMapWrite(saddr);
		if(m.bool) vPRGMemory[m.b] = data;
		return m.bool;
	}
	
	public RBB cpuRead(short saddr) {
		RBI m = pMapper.cpuMapRead(saddr);
		if(m.bool) return new RBB(true,vPRGMemory[m.b]);
		return new RBB(false);
	}
	
	public boolean ppuWrite(short saddr, byte data) {
		RBI m = pMapper.ppuMapWrite(saddr);
		if(m.bool) vCHRMemory[m.b] = data;
		return m.bool;
	}
	
	public RBB ppuRead(short saddr) {
		RBI m = pMapper.ppuMapRead(saddr);
		if(m.bool) return new RBB(true,vCHRMemory[m.b]);
		return new RBB(false);
	}
	
	class RBB {
		public boolean bool;
		public byte b;
		public RBB(boolean bool, byte b) {
			this.bool = bool;
			this.b = b;
		}
		public RBB(boolean bool) {
			this.bool = bool;
			this.b = 0x00;
		}
	}

}
