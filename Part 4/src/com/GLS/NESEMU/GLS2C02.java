package com.GLS.NESEMU;

import java.awt.Color;
import java.util.Random;

import com.GLS.NESEMU.Cartridge.RBB;
import com.GLS.NESEMU.Main.Sprite;

public class GLS2C02 {
	
	Cartridge cart;
	
	Random rand = new Random();
	
	byte[][] tblName = new byte[2][1024];
	byte[] tblPalette = new byte[32];
	byte[][] tblPattern = new byte[2][4096];
	
	Color[] palScreen = new Color[0x40];
	Sprite sprScreen = new Sprite(256,240);
	Sprite[] sprNameTable = { new Sprite(256,240), new Sprite(256,240) };
	Sprite[] sprPatternTable = { new Sprite(128,128), new Sprite(128,128) };

	public boolean frame_complete = false;
	public boolean nmi = false;
	
	byte status; //unused:1-5 sprite_overflow:6 sprite_zero_hit:7 vertical blank:8
	byte mask; //gs, r_b_l, r_s_l, r_b, r_s, e_r, e_g, e_b
	byte control; //n_x, n_y, i_m, p_s, p_b, s_s, s_m, e_n
	
	byte address_latch, ppu_data_buffer;
	short ppu_address;

	boolean getBit(byte s, byte b) {
	    return (s&b)!=0;
	}

	byte setBit(byte s, byte b, boolean v) {
	    s&=(0xFF-b);
	    s|=(v)?b:0x00;
	    return s;
	}
	
	int scanline, cycle;
	
	public GLS2C02() {
		palScreen[0x00] = new Color(84, 84, 84);
		palScreen[0x01] = new Color(0, 30, 116);
		palScreen[0x02] = new Color(8, 16, 144);
		palScreen[0x03] = new Color(48, 0, 136);
		palScreen[0x04] = new Color(68, 0, 100);
		palScreen[0x05] = new Color(92, 0, 48);
		palScreen[0x06] = new Color(84, 4, 0);
		palScreen[0x07] = new Color(60, 24, 0);
		palScreen[0x08] = new Color(32, 42, 0);
		palScreen[0x09] = new Color(8, 58, 0);
		palScreen[0x0A] = new Color(0, 64, 0);
		palScreen[0x0B] = new Color(0, 60, 0);
		palScreen[0x0C] = new Color(0, 50, 60);
		palScreen[0x0D] = new Color(0, 0, 0);
		palScreen[0x0E] = new Color(0, 0, 0);
		palScreen[0x0F] = new Color(0, 0, 0);

		palScreen[0x10] = new Color(152, 150, 152);
		palScreen[0x11] = new Color(8, 76, 196);
		palScreen[0x12] = new Color(48, 50, 236);
		palScreen[0x13] = new Color(92, 30, 228);
		palScreen[0x14] = new Color(136, 20, 176);
		palScreen[0x15] = new Color(160, 20, 100);
		palScreen[0x16] = new Color(152, 34, 32);
		palScreen[0x17] = new Color(120, 60, 0);
		palScreen[0x18] = new Color(84, 90, 0);
		palScreen[0x19] = new Color(40, 114, 0);
		palScreen[0x1A] = new Color(8, 124, 0);
		palScreen[0x1B] = new Color(0, 118, 40);
		palScreen[0x1C] = new Color(0, 102, 120);
		palScreen[0x1D] = new Color(0, 0, 0);
		palScreen[0x1E] = new Color(0, 0, 0);
		palScreen[0x1F] = new Color(0, 0, 0);

		palScreen[0x20] = new Color(236, 238, 236);
		palScreen[0x21] = new Color(76, 154, 236);
		palScreen[0x22] = new Color(120, 124, 236);
		palScreen[0x23] = new Color(176, 98, 236);
		palScreen[0x24] = new Color(228, 84, 236);
		palScreen[0x25] = new Color(236, 88, 180);
		palScreen[0x26] = new Color(236, 106, 100);
		palScreen[0x27] = new Color(212, 136, 32);
		palScreen[0x28] = new Color(160, 170, 0);
		palScreen[0x29] = new Color(116, 196, 0);
		palScreen[0x2A] = new Color(76, 208, 32);
		palScreen[0x2B] = new Color(56, 204, 108);
		palScreen[0x2C] = new Color(56, 180, 204);
		palScreen[0x2D] = new Color(60, 60, 60);
		palScreen[0x2E] = new Color(0, 0, 0);
		palScreen[0x2F] = new Color(0, 0, 0);

		palScreen[0x30] = new Color(236, 238, 236);
		palScreen[0x31] = new Color(168, 204, 236);
		palScreen[0x32] = new Color(188, 188, 236);
		palScreen[0x33] = new Color(212, 178, 236);
		palScreen[0x34] = new Color(236, 174, 236);
		palScreen[0x35] = new Color(236, 174, 212);
		palScreen[0x36] = new Color(236, 180, 176);
		palScreen[0x37] = new Color(228, 196, 144);
		palScreen[0x38] = new Color(204, 210, 120);
		palScreen[0x39] = new Color(180, 222, 120);
		palScreen[0x3A] = new Color(168, 226, 144);
		palScreen[0x3B] = new Color(152, 226, 180);
		palScreen[0x3C] = new Color(160, 214, 228);
		palScreen[0x3D] = new Color(160, 162, 160);
		palScreen[0x3E] = new Color(0, 0, 0);
		palScreen[0x3F] = new Color(0, 0, 0);
	}
	
	public void connectCartridge(Cartridge cartridge) {
		this.cart = cartridge;
	}
	
	public void clock() {
		if(scanline == -1 && cycle == 1) status = setBit(status,(byte)0x80,false);
		if(scanline == 241 && cycle == 1) {
			status = setBit(status,(byte)0x80,true);
			if(getBit(control,(byte)0x80)) nmi = true;
		}
		sprScreen.SetPixel(cycle - 1, scanline, palScreen[rand.nextInt()%2!=0?0x3F:0x30]);
		cycle++;
		if(cycle >= 341) {
			cycle = 0;
			scanline++;
			if(scanline >= 261) {
				scanline = -1;
				frame_complete = true;
			}
		}
	}
	
	public void cpuWrite(short saddr, byte data) {
		int addr = saddr&0xFFFF;
		switch(addr) {
		case 0x0:
			control = data;
			break;
		case 0x1:
			mask = data;
			break;
		case 0x2:
			break;
		case 0x3:
			break;
		case 0x4:
			break;
		case 0x5:
			break;
		case 0x6:
			if(address_latch==0) {
				ppu_address = (short) ((ppu_address&0x00FF)|((data&0x3F)<<8));
				address_latch = 1;
			} else {
				ppu_address = (short) ((ppu_address&0xFF00)|(data&0xFF));
				address_latch = 0;
			}
			break;
		case 0x7:
			ppuWrite(ppu_address,data);
			ppu_address = (short) ((ppu_address&0xFFFF)+1);
		}
	}
	
	public byte cpuRead(short saddr, boolean readOnly) {
		int addr = saddr&0xFFFF;
		byte data = 0x00;
		if(readOnly) {
			switch(addr) {
			case 0:
				data = control;
				break;
			case 1:
				data = mask;
				break;
			case 2:
				data = status;
				break;
			case 0x0003: // OAM Address
				break;
			case 0x0004: // OAM Data
				break;
			case 0x0005: // Scroll
				break;
			case 0x0006: // PPU Address
				break;
			case 0x0007: // PPU Data
				break;
			}
		} else {
			switch(addr) {
			case 0x0000: break;
			case 0x0001: break;
			case 0x0002:
				data = (byte) (((status&0xFF)&0xE0)| ((ppu_data_buffer&0xFF)&0x1F));
				status = setBit(status,(byte)0x80,false);
				address_latch = 0;
				break;
			case 0x0003: break;
			case 0x0004: break;
			case 0x0005: break;
			case 0x0006: break;
			case 0x0007:
				data = ppu_data_buffer;
				ppu_data_buffer = ppuRead(ppu_address,false);
				if(ppu_address>=0x3F00) data = ppu_data_buffer;
				ppu_address = (short) ((ppu_address&0xFFFF)+1);
				break;
			}
		}
		return data;
	}
	
	public void ppuWrite(short saddr, byte data) {
		int addr = saddr&0xFFFF;
		addr&=0x3FFF;
		if(cart.ppuWrite((short)addr, data));
		else if(addr>0x0000&&addr<=0x1FFF) {
			tblPattern[(addr&0x1000)>>12][addr&0x0FFF] = data;
		} else if(addr>0x2000&&addr<=0x3EFF) {
			
		} else if(addr>0x3F00&&addr<=0x3FFF) {
			addr&=0x001F;
			if(addr==0x0010) addr = 0x0;
			if(addr==0x0014) addr = 0x4;
			if(addr==0x0018) addr = 0x8;
			if(addr==0x001C) addr = 0xC;
			tblPalette[addr] = data;
		}
	}
	
	public byte ppuRead(short saddr, boolean readOnly) {
		int addr = saddr&0xFFFF;
		addr&=0x3FFF;
		byte data = 0x00;
		RBB r = cart.ppuRead(saddr);
		if(r.bool) data = r.b;
		else if(addr>0x0000&&addr<=0x1FFF) {
			data = tblPattern[(addr&0x1000)>>12][addr&0x0FFF];
		} else if(addr>0x2000&&addr<=0x3EFF) {
			
		} else if(addr>0x3F00&&addr<=0x3FFF) {
			addr&=0x001F;
			if(addr==0x0010) addr = 0x0;
			if(addr==0x0014) addr = 0x4;
			if(addr==0x0018) addr = 0x8;
			if(addr==0x001C) addr = 0xC;
			data = tblPalette[addr];
		}
		return data;
	}

	public Sprite getScreen() {
		return sprScreen;
	}

	public Sprite getNameTable(int i) {
		return sprNameTable[i];
	}

	public Sprite getPatternTable(int i, byte palette) {
		for(int nTileY = 0; nTileY < 16; nTileY++) {
			for(int nTileX = 0; nTileX < 16; nTileX++) {
				int nOffset = nTileY*256+nTileX*16;
				for(int row = 0; row < 8; row++) {
					byte tile_lsb = ppuRead((short)(i*0x1000+nOffset+row),false);
					byte tile_msb = ppuRead((short)(i*0x1000+nOffset+row+0x8),false);
					for(int col = 0; col < 8; col++) {
						byte pixel = (byte) (((tile_lsb&0xFF)&0x01)+((tile_msb&0xFF)&0x01));
						tile_lsb >>= 1;
						tile_msb >>= 1;
						sprPatternTable[i].SetPixel(nTileX*8+(7-col), nTileY*8+row, getColorFromPaletteRam(palette&0xFF,pixel&0xFF));
					}
				}
			}
		}
		return sprPatternTable[i];
	}

	Color getColorFromPaletteRam(int palette, int pixel) {
		return palScreen[ppuRead((short)((0x3F00+(palette<<2)+pixel)&0x3F),false)];
	}

}
