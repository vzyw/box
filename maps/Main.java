package box;
import java.awt.BorderLayout;
//方块移动 障碍物
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;



public class Main{
	public static void main(String[] args) {
		Window window = new Window("0.map");
	}
}
class BlockType{
	public static final Integer SPACE = 0;
	public static final Integer BORDER = 1;
	public static final Integer ROAD = 2;
	public static final Integer BOX = 3;
	public static final Integer TERMINAL = 4;
	public static final Integer MEN_DOWN = 5;
	public static final Integer MEN_LEFT = 6;
	public static final Integer MEN_RIGHT = 7;
	public static final Integer MEN_UP = 8;
	public static final Integer BOX_DONE = 9;	
	
	public static boolean isMen(int type) {
		if(type==MEN_DOWN || type==MEN_LEFT || 
				type==MEN_RIGHT || type == MEN_UP)return true;
		return false;
	}
	public static boolean isBox(int type) {
		if(type==BOX || type==BOX_DONE)return true;
		return false;
	}
	public static boolean isRoad(int type) {
		if(type==ROAD || type==TERMINAL)return true;
		return false;
	}
}


class Window extends JFrame implements KeyListener,ActionListener{
	private List<String> mapData;
	private PicSrc imgSrc;
	private int column;
	private int row;
	private Block[] blocks;
	private Vector<Block> terminals;
	private Block men;
	private Dimension picSize;
	private MyCanvas canva;
	private Stack<List<String>> backStack;
	
	public Window(String mapName) {
		backStack = new Stack<>();
		init(new Map(mapName).getMapData());
		
		/* 菜单 */
		JMenu pick = new JMenu("选关");
		List<String> map = Map.getAllMapName();
		for(String name:map){
			JMenuItem item = new JMenuItem(name);
			item.addActionListener(this);
			pick.add(item);
		}
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(pick);
		setJMenuBar(menuBar);
		/*----*/
		
		
		/* 按钮 */
		JButton back = new JButton("回退");
		back.addActionListener(this);
		/*----*/
		
		/* 画布 */
		canva = new MyCanvas();
		addKeyListener(this);
		canva.addKeyListener(this);
		/* ---- */
		
		setLayout(new BorderLayout());
		add(back,BorderLayout.NORTH);
		add(canva,BorderLayout.CENTER);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);	
		
	}
	
	
	public void init(List<String> md) {
		terminals = new Vector<>();
		mapData = md;
		imgSrc  = PicSrc.getInstance();
		
		column = mapData.get(0).length();
		row	   = mapData.size();
		
		blocks  = new Block[column*row];
		picSize = new Dimension(imgSrc.getImgByIndex(0).getIconWidth(), imgSrc.getImgByIndex(0).getIconHeight());
		
		//通过map数据初始化所有的块
		for(int i = 0;i<row;i++){
			for(int j = 0;j<column;j++){
				ImageIcon imageIcon = imgSrc.getImgByIndex(mapData.get(i).charAt(j)-'0');
				int type = Integer.parseInt(imageIcon.getDescription());
				int pos = i*column+j;
				blocks[pos] = new Block(new Point(j*picSize.width, i*picSize.height), picSize,imageIcon);
				if(BlockType.isMen(type)) men = blocks[pos];
				if(BlockType.TERMINAL==type)   terminals.add(blocks[pos]);
			}
		}
		setSize(column*picSize.width,row*picSize.height);
	}
	
	//键盘监测
	@Override
	public void keyPressed(KeyEvent e) {
		if(blockMove(men.getLocation(), e.getKeyCode())){
			backStack.push(Map.creatMapData(blocks, row, column));
		}
		canva.repaint();
		if(isWin())JOptionPane.showMessageDialog(null, "You win!!", "", JOptionPane.OK_CANCEL_OPTION);;
	}
	@Override
	public void keyReleased(KeyEvent e) {}
	@Override
	public void keyTyped(KeyEvent e) {}	
	
	//按钮检测
	@Override
	public void actionPerformed(ActionEvent e){
		if(e.getActionCommand()=="回退"){
			if(backStack.size()==0)return;
			this.init(backStack.pop());
		}else{
			backStack = new Stack<>();
			this.init(new Map(e.getActionCommand()).getMapData());
		}
		canva.repaint();
	}

	private  class MyCanvas extends Canvas{
		@Override
		public void paint(Graphics g) {
			Graphics2D g2d = (Graphics2D)g;
			 //Integer i = 0;
			for (Block block : blocks) {
				g2d.draw(block);
				g2d.drawImage(block.getIcon().getImage(), block.getLocation().x, block.getLocation().y,this);
				 //g2d.drawString(i.toString(),  block.getLocation().x,  block.getLocation().y+picSize.height);
				 //i++;
			}
		}
		
	}

	//块移动检测
	private boolean blockMove(Point point,int keyCode){
		int x = point.x,
		    y = point.y;
		int currPos = (y/picSize.height)*column + (x/picSize.width);
		int men_type = BlockType.MEN_DOWN;
		switch (keyCode) {
			case KeyEvent.VK_RIGHT:
				x+=picSize.width;
				men_type = BlockType.MEN_RIGHT;
				break;
			case KeyEvent.VK_DOWN:	
				y+=picSize.height;
				men_type = BlockType.MEN_DOWN;
				break;
			case KeyEvent.VK_LEFT:
				x-=picSize.width;
				men_type = BlockType.MEN_LEFT;
				break;
			case KeyEvent.VK_UP:	
				y-=picSize.height;
				men_type = BlockType.MEN_UP;
				break;
			default:	break;
		}
		int nextPos = (y/picSize.height)*column + (x/picSize.width);
		
		
		System.out.println("currPos:"+ currPos+"  "+"newtPos:" +nextPos);
		
		int curr_box_type =blocks[currPos].getIconType(),
			next_box_type =blocks[nextPos].getIconType();
		
		if(BlockType.BORDER==next_box_type) return false;
		if(BlockType.isBox(curr_box_type) && BlockType.isBox(next_box_type) )return false;
		
		if(BlockType.isBox(curr_box_type) && BlockType.isRoad(next_box_type)){
			blocks[nextPos].setIcon(imgSrc.getImgByIndex(next_box_type==BlockType.TERMINAL?BlockType.BOX_DONE:BlockType.BOX));
			blocks[currPos].setIcon(imgSrc.getImgByIndex(blocks[currPos].getType()));
			return true;
		}

        if(BlockType.isBox(next_box_type)){
        	if(!blockMove(new Point(x, y), keyCode))return false;
        }
		
        next_box_type =blocks[nextPos].getIconType();
		if(BlockType.isRoad(next_box_type)) {
			System.out.println("check road");
			blocks[nextPos].setIcon(imgSrc.getImgByIndex(men_type));
			blocks[currPos].setIcon(imgSrc.getImgByIndex(blocks[currPos].getType()));
			men = blocks[nextPos];
			return true;
		}
		return false;
	}
	
	//检测是否完成
	private boolean isWin(){
		for (Block block : terminals) {
			if(block.getIconType() != BlockType.BOX_DONE)return false;
		}
		return true;
	}


	
}








//块 每个块含有一个图片和一个类型,每个图片也有自己的类型
class Block extends Rectangle{
	private ImageIcon icon;
	private int type;
	public Block(Point point, Dimension dimension ,ImageIcon icon) {
		super(point,dimension);
		this.icon = icon;
		type = Integer.parseInt(icon.getDescription());
		if(BlockType.isMen(type)|| BlockType.BOX==type){
			type= BlockType.ROAD;
		}else if(BlockType.BOX_DONE==type){
			type=BlockType.TERMINAL;
		}
	}
	public int getType() {
		return type;
	}
	public ImageIcon getIcon() {
		return icon;
	}
	public void setIcon(ImageIcon icon) {
		this.icon =icon;
	}
	public int getIconType() {
		return Integer.parseInt(icon.getDescription());
	}
}




//初始化所有图片;
class PicSrc{
	private static String PATH = "imgs/";
	private static int PICNUMS = 10;
	private static PicSrc singlePicSrc = null;
	private static ImageIcon[] imgs;
	private PicSrc(){
		imgs = new ImageIcon[PICNUMS];
		for(int i = 0;i<PICNUMS;i++){
			imgs[i] = new ImageIcon(PATH + i + ".gif",Integer.toString(i));
		}
		singlePicSrc = this;
	}
	public static PicSrc getInstance() {
		if(singlePicSrc == null){
			new PicSrc();
		}
		return singlePicSrc;
	}
	public ImageIcon getImgByIndex(int index) {
		return imgs[index];
	}
}





//读入地图数据
class Map{
	private static String path = "maps/";
	private List<String> mapData;
	public Map(String mapName) {
		BufferedReader stream;
		try {
		    stream = new BufferedReader(new FileReader(new File(path + mapName)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		String s = null;
		mapData = new ArrayList<>();
		try {
			while((s = stream.readLine()) != null){
				mapData.add(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public List<String> getMapData() {
		return mapData;
	}
	public static List<String> creatMapData(Block[] blocks,int row,int column){
		List<String> res = new ArrayList<>();
		for(int i = 0;i<row;i++){
			String data="";
			for(int j = 0;j<column;j++){
				data += blocks[i*column + j].getIconType();
			}
			res.add(data);
		}
		return res; 
	}
	
	public static List<String> getAllMapName() {
		 File[] files = new File(path).listFiles();  
		 List<String> res = new ArrayList<>();
		 for (File file : files) {
			res.add(file.getName());
		 }
		 return res;
	}
//	public static void main(String[] args) {
//		List<String> tList  = new Map("1.map").getMapData();
//		for (String string : tList) {
//			System.out.println(string);
//		}
//	}
}



