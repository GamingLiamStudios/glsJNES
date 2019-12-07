package com.GLS.NESEMU;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.HashMap;

import javax.swing.JFrame;

public class Main extends Canvas implements Runnable, KeyListener{
	
	private static final long serialVersionUID = -3543314193422502419L;
	
	Thread thread;
	boolean running;
	int pixelSize = 1;
	
	Sprite fontSprite;
	HashMap<Integer,String> mapAsm = new HashMap<Integer,String>();
	
	boolean bEmulationRun = false;
	byte nSelectedPalette = 0x00;
	
	Bus nes;
	Cartridge cart;

	public static void main(String[] args) {
		new Main();
	}
	
	public Main() {
		createDisplay();	
		nes = new Bus();
		constructFontSheet();
		onUserCreate();
		start();
	}
	
	void createDisplay() {
		pixelSize = 2;
		JFrame frame = new JFrame();
		addKeyListener(this);
		frame.setTitle("NES Emulator");
		frame.setSize(780, 480);	
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.add(this);
		frame.setVisible(true);	
	}
	
	void onUserCreate() {
		cart = new Cartridge("nestest.nes");
		nes.insertCartridge(cart);
		mapAsm = nes.cpu.disassemble(0x0000, 0xFFFF);
		nes.reset();
	}

	public void start() {
		thread = new Thread(this);
		thread.start();
		running = true;
	}

	public void run() {
		long lastTime = System.nanoTime();
		double amountOfTicks = 120.0;
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		long timer = System.currentTimeMillis();
		while(running){
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while(delta >= 1){
				delta--;
				tick();
			}
			render();
			if(System.currentTimeMillis() - timer > 1000){
				timer += 1000;
			}
		}
		stop();
	}
	
	void tick() {
		if(bEmulationRun) {
			do nes.clock(); while(!nes.ppu.frame_complete);			
			do nes.clock(); while(nes.cpu.complete());
			nes.ppu.frame_complete = false;
		}
	}
	
	void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if(bs == null){
			this.createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		
		g.setColor(new Color(0,0,139));
		g.fillRect(0, 0, 780, 480);	

		drawCpu(516,2,g);
		drawCode(516,72,26,g);
		
		final int nSwatchSize = 6;
		for(int p = 0; p<8; p++) {
			for(int s = 0; s<4; s++) {
				g.setColor(nes.ppu.getColorFromPaletteRam(p, s));
				g.fillRect(516+p*(nSwatchSize*5)+s*nSwatchSize, 340, nSwatchSize, nSwatchSize);
			}
		}
		
		g.setColor(Color.WHITE);
		g.drawRect(516+(nSelectedPalette&0xFF)*(nSwatchSize*5)-1, 339, nSwatchSize*4, nSwatchSize);
		
		drawSprite(516,348,nes.ppu.getPatternTable(0, nSelectedPalette),1,g);
		drawSprite(648,348,nes.ppu.getPatternTable(1, nSelectedPalette),1,g);
		
		drawSprite(0,0,nes.ppu.getScreen(),2,g);
		
		g.dispose();
		bs.show();
	}

	public synchronized void stop(){
		try{
			thread.join();
			running = false;
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if(!bEmulationRun) {
			if(e.getKeyCode()==KeyEvent.VK_C) {
				do nes.clock(); while(!nes.cpu.complete());
				do nes.clock(); while(nes.cpu.complete());
			}
			if(e.getKeyCode()==KeyEvent.VK_F) {
				do nes.clock(); while(!nes.ppu.frame_complete);
				do nes.clock(); while(nes.cpu.complete());
				nes.ppu.frame_complete = false;
			}
		}
		if(e.getKeyCode()==KeyEvent.VK_R) nes.reset();
		if(e.getKeyCode()==KeyEvent.VK_SPACE) bEmulationRun=!bEmulationRun;
		if(e.getKeyCode()==KeyEvent.VK_P) nSelectedPalette = (byte) (((nSelectedPalette&0xFF)+1)&0x7);
	}
	
	public static class Sprite {
		Color[] pColData;
		int width, height;
		public Sprite(int w, int h){
			width = w;		
			height = h;
			pColData = new Color[width * height];
		}
		Color GetPixel(int x, int y) {
			if (x >= 0 && x < width && y >= 0 && y < height)
				return pColData[y*width + x];
			else
				return new Color(0, 0, 0, 0);
			
		}
		void SetPixel(int x, int y, Color c) {
			if (x >= 0 && x < width && y >= 0 && y < height)
				pColData[y*width + x] = c;
			else return;
		}
	}
	
	void drawSprite(int x, int y, Sprite sprite, long scale, Graphics g) {
		if (sprite == null)
			return;

		if (scale > 1) {
			for (int i = 0; i < sprite.width; i++)
				for (int j = 0; j < sprite.height; j++)
					for (int is = 0; is < scale; is++)
						for (int js = 0; js < scale; js++) {
							g.setColor(sprite.GetPixel(i, j));
							g.fillRect((int) (x + (i*scale) + is),(int) (y + (j*scale) + js), (int)(pixelSize*scale), (int)(pixelSize*scale));
						}
							
		}
		else {
			for (int i = 0; i < sprite.width; i++)
				for (int j = 0; j < sprite.height; j++) {
					g.setColor(sprite.GetPixel(i, j));
					g.fillRect(x + j, y + i, (int)(pixelSize*scale), (int)(pixelSize*scale));
				}		
		}
	}
	
	void drawRam(int x, int y, int nAddr, int nRows, int nColumns, Graphics g)
	{
		int nRamX = x, nRamY = y;
		for (int row = 0; row < nRows; row++) {
			String sOffset = "$" + hex(nAddr, 4) + ":";
			for (int col = 0; col < nColumns; col++) {
				sOffset += " " + hex(nes.cpuRead((short)nAddr, false)&0xFF, 2);
				nAddr++;
			}
			drawString(nRamX, nRamY, sOffset, Color.white, g);
			nRamY += 10;
		}
	}
	
	void drawCpu(int x, int y, Graphics g) {
		drawString(x , y , "STATUS:", Color.WHITE, g);
		drawString(x  + 64, y , "N", (nes.cpu.status & GLS6502.FLAGS6502.N.data)!=0 ? Color.GREEN : Color.RED, g);
		drawString(x  + 80, y , "V", (nes.cpu.status & GLS6502.FLAGS6502.V.data)!=0 ? Color.GREEN : Color.RED, g);
		drawString(x  + 96, y , "-", (nes.cpu.status & GLS6502.FLAGS6502.U.data)!=0 ? Color.GREEN : Color.RED, g);
		drawString(x  + 112, y, "B", (nes.cpu.status & GLS6502.FLAGS6502.B.data)!=0 ? Color.GREEN : Color.RED, g);
		drawString(x  + 128, y, "D", (nes.cpu.status & GLS6502.FLAGS6502.D.data)!=0 ? Color.GREEN : Color.RED, g);
		drawString(x  + 144, y, "I", (nes.cpu.status & GLS6502.FLAGS6502.I.data)!=0 ? Color.GREEN : Color.RED, g);
		drawString(x  + 160, y, "Z", (nes.cpu.status & GLS6502.FLAGS6502.Z.data)!=0 ? Color.GREEN : Color.RED, g);
		drawString(x  + 178, y, "C", (nes.cpu.status & GLS6502.FLAGS6502.C.data)!=0 ? Color.GREEN : Color.RED, g);
		drawString(x , y + 10, "PC: $" + hex(nes.cpu.pc&0xFFFF, 4), Color.white, g);
		drawString(x , y + 20, "A: $" +  hex(nes.cpu.a&0xFF, 2) + "  [" + Integer.toString(nes.cpu.a&0xFF) + "]", Color.white, g);
		drawString(x , y + 30, "X: $" +  hex(nes.cpu.x&0xFF, 2) + "  [" + Integer.toString(nes.cpu.x&0xFF) + "]", Color.white, g);
		drawString(x , y + 40, "Y: $" +  hex(nes.cpu.y&0xFF, 2) + "  [" + Integer.toString(nes.cpu.y&0xFF) + "]", Color.white, g);
		drawString(x , y + 50, "Stack P: $" + hex(nes.cpu.stkp&0xFF, 4), Color.white, g);
	}
	
	void drawCode(int x, int y, int nLines, Graphics g) {
		int pc = nes.cpu.pc&0xFFFF;
		/*
		 // ORIGINAL
		String it_a = mapAsm.get(pc);
		int nLineY = (nLines >> 1) * 10 + y;
		if (it_a != null) {
			drawString(x, nLineY, it_a, Color.cyan, g);
		}
		pc++;
		while (nLineY < (nLines * 10) + y) {	
			nLineY += 10;
			it_a = mapAsm.get(pc++);
			while(it_a == null) it_a = mapAsm.get(pc++);
			drawString(x, nLineY, it_a, Color.white, g);
		}
		pc = nes.cpu.pc&0xFFFF;
		it_a = mapAsm.get(pc);
		nLineY = (nLines >> 1) * 10 + y;
		pc--;
		while (nLineY > y) {	
			nLineY -= 10;
			it_a = mapAsm.get(pc--);
			while(it_a == null) it_a = mapAsm.get(pc--);
			drawString(x, nLineY, it_a, Color.white, g);
		}
		*/
		/*
		 // PURE HEX
		int nLineY = (nLines >> 1) * 10 + y;
		drawString(x,nLineY,hex(pc&0xFF,4)+": "+hex(nes.cpu.opcode&0xFF,2),Color.cyan,g);
		pc++;
		while (nLineY < (nLines * 10) + y) {	
			nLineY += 10;
			drawString(x,nLineY,hex(pc++&0xFF,4)+": "+hex(nes.cpuRead((short)pc, true)&0xFF,2),Color.cyan,g);
		}
		pc = nes.cpu.pc&0xFFFF;
		nLineY = (nLines >> 1) * 10 + y;
		pc--;
		while (nLineY > y) {	
			nLineY -= 10;
			drawString(x,nLineY,hex(pc--&0xFF,4)+": "+hex(nes.cpuRead((short)pc, true)&0xFF,2),Color.cyan,g);
		}
		*/
		//COMBINATION
		String it_a = mapAsm.get(pc);
		int nLineY = (nLines >> 1) * 10 + y;
		if (it_a != null) {
			drawString(x, nLineY, it_a+" OP: "+hex(nes.cpuRead((short)pc, true)&0xFF,2), Color.cyan, g);
		}
		pc++;
		while (nLineY < (nLines * 10) + y) {	
			nLineY += 10;
			it_a = mapAsm.get(pc++);
			while(it_a == null) it_a = mapAsm.get(pc++);
			drawString(x, nLineY, it_a+" OP: "+hex(nes.cpuRead((short)pc, true)&0xFF,2), Color.white, g);
		}
		pc = nes.cpu.pc&0xFFFF;
		it_a = mapAsm.get(pc);
		nLineY = (nLines >> 1) * 10 + y;
		pc--;
		while (nLineY > y) {	
			nLineY -= 10;
			it_a = mapAsm.get(pc--);
			while(it_a == null) it_a = mapAsm.get(pc--);
			drawString(x, nLineY, it_a+" OP: "+hex(nes.cpuRead((short)pc, true)&0xFF,2), Color.white, g);
		}
	}
	
	String hex(int n, int d) {
		String s = new String(new char[d]);
		for (int i = d - 1; i >= 0; i--, n >>= 4)
			s = changeCharInString(i,"0123456789ABCDEF".charAt(n & 0xF),s);
		return s;
	}
	
	String changeCharInString(int pos, char c, String s) {
		return s.substring(0,pos) + c + s.substring(pos+1);
	}
	
	void constructFontSheet(){
		String data = "";
		data += "?Q`0001oOch0o01o@F40o0<AGD4090LAGD<090@A7ch0?00O7Q`0600>00000000";
		data += "O000000nOT0063Qo4d8>?7a14Gno94AA4gno94AaOT0>o3`oO400o7QN00000400";
		data += "Of80001oOg<7O7moBGT7O7lABET024@aBEd714AiOdl717a_=TH013Q>00000000";
		data += "720D000V?V5oB3Q_HdUoE7a9@DdDE4A9@DmoE4A;Hg]oM4Aj8S4D84@`00000000";
		data += "OaPT1000Oa`^13P1@AI[?g`1@A=[OdAoHgljA4Ao?WlBA7l1710007l100000000";
		data += "ObM6000oOfMV?3QoBDD`O7a0BDDH@5A0BDD<@5A0BGeVO5ao@CQR?5Po00000000";
		data += "Oc``000?Ogij70PO2D]??0Ph2DUM@7i`2DTg@7lh2GUj?0TO0C1870T?00000000";
		data += "70<4001o?P<7?1QoHg43O;`h@GT0@:@LB@d0>:@hN@L0@?aoN@<0O7ao0000?000";
		data += "OcH0001SOglLA7mg24TnK7ln24US>0PL24U140PnOgl0>7QgOcH0K71S0000A000";
		data += "00H00000@Dm1S007@DUSg00?OdTnH7YhOfTL<7Yh@Cl0700?@Ah0300700000000";
		data += "<008001QL00ZA41a@6HnI<1i@FHLM81M@@0LG81?O`0nC?Y7?`0ZA7Y300080000";
		data += "O`082000Oh0827mo6>Hn?Wmo?6HnMb11MP08@C11H`08@FP0@@0004@000000000";
		data += "00P00001Oab00003OcKP0006@6=PMgl<@440MglH@000000`@000001P00000000";
		data += "Ob@8@@00Ob@8@Ga13R@8Mga172@8?PAo3R@827QoOb@820@0O`0007`0000007P0";
		data += "O`000P08Od400g`<3V=P0G`673IP0`@3>1`00P@6O`P00g`<O`000GP800000000";
		data += "?P9PL020O`<`N3R0@E4HC7b0@ET<ATB0@@l6C4B0O`H3N7b0?P01L3R000000020";

		fontSprite = new Sprite(128, 48);
		int px = 0, py = 0;
		for (int b = 0; b < 1024; b += 4)
		{
			long sym1 = (long)data.charAt(b) - 48;
			long sym2 = (long)data.charAt(b+1) - 48;
			long sym3 = (long)data.charAt(b+2) - 48;
			long sym4 = (long)data.charAt(b+3) - 48;
			long r = sym1 << 18 | sym2 << 12 | sym3 << 6 | sym4;

			for (int i = 0; i < 24; i++)
			{
				int k = (r & (1 << i))!=0 ? 255 : 0;
				fontSprite.SetPixel(px, py, new Color(k,k,k,k));
				if (++py == 48) {
					px++; 
					py = 0; 
				}
			}
		}
	}
	
	void drawString(int x, int y, String sText, Color col, Graphics g){
		int sx = 0;
		int sy = 0;
		for (char c : sText.toCharArray()){
			if (c == '\n') {
				sx = 0; sy += 8;
			} else {
				int ox = (c - 32) % 16;
				int oy = (c - 32) / 16;
				g.setColor(col);
				for (int i = 0; i < 8; i++)
					for (int j = 0; j < 8; j++)
						if (fontSprite.GetPixel(i + ox * 8, j + oy * 8).getRed() > 0)
							for (int is = 0; is < 1; is++)
								for (int js = 0; js < 1; js++)
									g.fillRect(x + sx + (i*1) + is, y + sy + (j*1) + js, 1, 1);
				sx += 8;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

}
