package game;

import gfx.Colors;
import gfx.Font;
import gfx.Screen;
import gfx.SpriteSheet;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;

import level.Level;

public class Game extends Canvas implements Runnable{


	private static final long serialVersionUID = 1L;
	
	public static final int WIDTH=160;
	public static final int HEIGHT = WIDTH/12*9;
	public static final int SCALE = 3;
	public static final String NAME = "Game";
	
	private JFrame frame;
	
	public boolean running = false;
	
	public int tickCount = 0;
	
	private BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
	private int[] colors = new int[6*6*6]; //216 bit color array
	
	private Screen screen;
	public InputHandler input;
	public Level level;
	
	
	public Game(){
		setMinimumSize(new Dimension(WIDTH* SCALE, HEIGHT* SCALE));
		setMaximumSize(new Dimension(WIDTH* SCALE, HEIGHT* SCALE));
		setPreferredSize(new Dimension(WIDTH* SCALE, HEIGHT* SCALE));
		
		frame = new JFrame(NAME);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		
		frame.add(this, BorderLayout.CENTER);
		frame.pack();
		
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	public synchronized void start() {
		running = true; 
		new Thread(this).start();
		
		
	}
	
	public void init(){
		int index = 0;
		for (int r = 0; r < 6; r++){
			for (int g = 0; g < 6; g++){
				for (int b = 0; b < 6; b++){
					int rr = (r*255 / 5);
					int gg = (g*255 / 5);
					int bb = (b*255 / 5);
					
					colors[index++] = rr <<16 | gg << 8 | bb;  
				}
			}
		}
		
		
		screen = new Screen(WIDTH, HEIGHT, new SpriteSheet("/sprite_sheet.png"));
		input = new InputHandler(this);
		level = new Level(64,64);
	}
	public synchronized void stop() {
		running = false; 
		
	}
	
	@Override
	public void run() {
		long lastTime = System.nanoTime();
		double nsPerTick=1000000000D/60D;
		
		int frames = 0;
		int ticks = 0;
		
		long lastTimer=System.currentTimeMillis();
		double delta = 0; //how many nanoseconds have gone by so far
		
		init();
		
		while(running){
			long now = System.nanoTime();
			delta +=(now - lastTime) / nsPerTick;
			lastTime=now;
			boolean shouldRender = true;
			while(delta >= 1){
				ticks++;
				tick();
				delta -=1;
				shouldRender = true;
			}
		
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(shouldRender){
			frames++;
			render();
			}
			
			if(System.currentTimeMillis() - lastTimer > 1000){
				lastTimer +=1000;
				System.out.println(ticks + " ticks," + frames + " frames");
				frames = 0;
				ticks = 0;
			}
		}
	}
	
	private int x = 0,y = 0;
	
	public void tick(){ //shifts screen based on input handled by InputhHandler
		tickCount++;
		
		if(input.up.isPressed()){ y--;}
		if(input.down.isPressed()){ y++;}
		if(input.left.isPressed()){ x--;}
		if(input.right.isPressed()){ x++;}
		
		level.tick();
		
		for (int i = 0; i < pixels.length; i++){
			pixels[i] = i + tickCount;
			
		}
	}
	

	
	public void render(){
		BufferStrategy bs = getBufferStrategy();
		if (bs == null){
			createBufferStrategy(3);
			return;    
		}
		
		int xOffset = x - (screen.width/ 2);
		int yOffset = y -(screen.height/ 2);
		
		level.renderTiles(screen, xOffset, yOffset);
		
		/*
		
		for (int y = 0; y < 32; y++){
			for(int x = 0; x < 32; x++){
				boolean flipX = x % 2 == 1;
				boolean flipY = y % 2 == 1;
				screen.render(x<<3,y<<3, 0, Colors.get(555,505,055,550),flipX,flipY);
			}
		}
		*/
		
		
		
		
		for (int y =0 ; y < screen.height; y++){
			for(int x = 0; x < screen.width; x++){
				int colorCode = screen.pixels[x+y*screen.width];
				if (colorCode < 255) pixels[x + y * WIDTH]=colors[colorCode];
			}
			}
		

		Graphics g = bs.getDrawGraphics();
		
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		g.drawImage(image,0,0,getWidth(),getHeight(),null);
		
		g.dispose();
		bs.show();
	}
		
		public static void main(String[] args) {
			 new Game().start();
			
		}

	}



