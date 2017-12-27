import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.text.SimpleDateFormat;

import javax.swing.*;
import javax.swing.Timer;

import java.util.*;
import java.util.List;

class Pair {
	public int i = 0;
	public int j = 0;
	public Pair(int i, int j) {
		this.i = i;
		this.j = j;
	}
	public String toString() {
		return "[" + this.i + "," + this.j + "]"; 
	}
}

class ScoreInfo implements Comparable<ScoreInfo>{
	private int score;
	private long date;
	
	public ScoreInfo(int score, long date) {
		this.score = score;
		this.date = date;
	}
	
	public String toString() {
		return "" + score + " " + date;
	}
	
	public int compareTo(ScoreInfo other) {
		if (score != other.score)
			return other.score - score;
		else
			return (int) (date - other.date);
	}
}

@SuppressWarnings("serial")
class EatingSnakeFrame extends JFrame {
	public EatingSnakeFrame() {
		setTitle("贪吃蛇");
		height = 420;
		width = 600;
		setSize(width, height);  // 设置大小
		setLocationByPlatform(true); // 设置定位

		setLayout(null);
		leftPanel = new JPanel();
		leftPanel.setLayout(null);
		leftPanel.setSize(500,420);
		leftPanel.setLocation(0,0);
		scoreLabel = new JLabel("得分:0");
		scoreLabel.setLocation(0, 0);
		scoreLabel.setSize(500, 30);
		snakeComponent = new SnakeComponent();
		snakeComponent.setLocation(0,30);
		snakeComponent.setSize(500,360);
		leftPanel.add(scoreLabel);
		leftPanel.add(snakeComponent);
		add(leftPanel);
		
		rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		rightPanel.setSize(100, 420);
		rightPanel.setLocation(500,0);
		btPanel = new JPanel();
		btPanel.setLayout(new GridLayout(6,1));
		startButton = new JButton("开  始");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				snakeComponent.startGame();
				snakeComponent.requestFocus();
			}
		});
		suspendButton = new JButton("暂  停");
		suspendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (snakeComponent.getStatus() != 0) {
					if (suspendButton.getText() == "暂  停") {
						suspendButton.setText("继  续");
						snakeComponent.suspendResumeGame(1);
					} else {
						snakeComponent.requestFocus();
						snakeComponent.suspendResumeGame(2);
						suspendButton.setText("暂  停");
					}
				}
			}
		});
		stopButton = new JButton("停  止");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				snakeComponent.stopGame();
			}
		});
		normalRB = makeRadioButton("初  级", true, 3);
		middleRB = makeRadioButton("中  级", false, 2);
		hardRB = makeRadioButton("高  级", false, 1);
		levelSelect = new ButtonGroup();
		levelSelect.add(normalRB);
		levelSelect.add(middleRB);
		levelSelect.add(hardRB);
		btPanel.add(startButton);
		btPanel.add(suspendButton);
		btPanel.add(stopButton);
		btPanel.add(normalRB);
		btPanel.add(middleRB);
		btPanel.add(hardRB);
		rightPanel.add(btPanel, BorderLayout.NORTH);
		rankInfo = new JTextArea();
		rankInfo.setEditable(false);
		rankInfo.setBackground(Color.GRAY);
		rightPanel.add(rankInfo, BorderLayout.CENTER);
		add(rightPanel);
		
		snakeComponent.init();
		snakeComponent.requestFocus();
		loadScoreinfo();
	}
	
	private JRadioButton makeRadioButton(String name, boolean selected, final int level) {
		JRadioButton bt = new JRadioButton(name, selected);
		bt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				snakeComponent.setLevel(level);
				snakeComponent.requestFocus();
			}
		});
		return bt;
	}
	
	private void loadScoreinfo() {
		File f = new File("./eatingsnakescore.txt");
		try {
			StringBuilder sb = new StringBuilder();
			if (f.exists()) {
				 BufferedReader reader = new BufferedReader(new FileReader(f));
				 String tempString = null;
	             while ((tempString = reader.readLine()) != null) {
		           String score = tempString.substring(0, tempString.indexOf(' '));
		           String time = tempString.substring(tempString.indexOf(' ') + 1);
		           sb.append(score);
		           sb.append(" ");
		           SimpleDateFormat matter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
		           String nowtime = matter.format(new Date(Long.parseLong(time)));
		           sb.append(nowtime);
		           sb.append('\n');
		         }
		         reader.close();
		         rankInfo.setText(sb.toString());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// 布局 组件
	private JPanel leftPanel;
	private JPanel rightPanel;
	private JLabel scoreLabel; // 得分
	private SnakeComponent snakeComponent; // 游戏区
	private JPanel btPanel;
	private JButton startButton; // 开始
	private JButton suspendButton; // 暂停
	private JButton stopButton; // 停止
	private ButtonGroup levelSelect; // 级别选择
	private JRadioButton normalRB; // 初级难度
	private JRadioButton middleRB; // 中级难度
	private JRadioButton hardRB; // 高级难度
	private JTextArea rankInfo; // 排行榜
	// frame size
	private int height;
	private int width;

	class SnakeComponent extends JComponent {
		public SnakeComponent () {
			
		}
		public void init() {
			sign = new int[WIDTH_SIZE][HEIGHT_SIZE];
			squares = new Rectangle2D[WIDTH_SIZE][HEIGHT_SIZE];
			int swidth = getWidth();
			int sheight = getHeight();
			double pwidth = 1.0 * swidth / WIDTH_SIZE;
			double pheight = 1.0 * sheight / HEIGHT_SIZE;
			for (int i = 0; i < WIDTH_SIZE; ++i) {
				for (int j = 0; j < HEIGHT_SIZE; ++j) {
					sign[i][j] = AREA_TYPE;
					squares[i][j] = new Rectangle2D.Double(i*pwidth, j*pheight, pwidth, pheight);
				}
			}
			snake = new LinkedList<Pair>();
			repaint();
			
			addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent arg0) {
					// TODO Auto-generated method stub
					if (status == 1) {
						changeDiretion(arg0.getKeyCode());
					}
				}

				@Override
				public void keyReleased(KeyEvent arg0) {
					// TODO Auto-generated method stub
				}

				@Override
				public void keyTyped(KeyEvent arg0) {
					// TODO Auto-generated method stub
				}
				
			});
		}
		
		// 画蛇
		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			int i = 0, j = 0;
			for (; i < squares.length; ++i) {
				for (j = 0; j < squares[i].length; ++j) {
					if (sign[i][j] == FOOD_TYPE) {
						g2.setColor(Color.RED);  // 食物
					} else if (sign[i][j] == SNAKE_TYPE) {
						g2.setColor(Color.BLUE);  // 蛇
					} else {
						g2.setColor(Color.WHITE); // 其他区域
					}
					g2.fill(squares[i][j]);
				}
			}
		}
		// 开始   随机出现蛇头，事物，方向
		public void startGame() {
			if (status == 0) {
				status = 1;
				for (int i = 0; i < WIDTH_SIZE; ++i) {
					for (int j = 0; j < HEIGHT_SIZE; ++j) {
						sign[i][j] = AREA_TYPE;
					}
				}
				snake.clear();
				Random r = new Random();
				int si = r.nextInt(WIDTH_SIZE);
				int sj = r.nextInt(HEIGHT_SIZE);
				sign[si][sj] = SNAKE_TYPE;
				snake.addFirst(new Pair(si,sj));
				do {
					int fi = r.nextInt(WIDTH_SIZE);
					int fj = r.nextInt(HEIGHT_SIZE);
					if (fi != si || fj != sj) {
						sign[fi][fj] = FOOD_TYPE;
						food = new Pair(fi, fj);
						break;
					}
				} while(true);
				direction = r.nextInt(4) + 1;
				repaint();
				timer = new Timer(level * 100, new TimeAction());
				timer.start();
			}
		}
		
		public void suspendResumeGame(int type) {
			if (timer != null) {
				if (timer.isRunning() && type == 1) {
					timer.stop();
					status = 2;
				} else if (!timer.isRunning() && type == 2) {
					timer= new Timer(level * 100, new TimeAction());
					timer.start();
					status = 1;
				}
			}
		}
		
		public void stopGame() {
			status = 0;
			if (timer != null) {
				timer.stop();
				timer = null;
				String message = "您的" + scoreLabel.getText() + ",是否提交?";
				int ret = JOptionPane.showConfirmDialog(snakeComponent, message,"游戏结束",JOptionPane.YES_NO_OPTION);
				if (ret == JOptionPane.YES_OPTION) {
					saveScore();
				}
			}
		}
		
		public int getStatus() {
			return status;
		}
		
		public void setLevel(int level) {
			this.level = level;
		}
		
		class TimeAction implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				Pair head = snake.getFirst();
				int i = head.i;
				int j = head.j;
				switch (direction) {
				case 1:
					i += 1;
					break;
				case 2:
					i -= 1;
					break;
				case 3:
					j -= 1;
					break;
				case 4:
					j += 1;
					break;
				default:
					return;
				}
				if (i < 0 || i >= WIDTH_SIZE || j < 0 || j >= HEIGHT_SIZE ||
					sign[i][j] == SNAKE_TYPE) {
					if (i < 0 || i >= WIDTH_SIZE || j < 0 || j >= HEIGHT_SIZE) {
						System.err.println("撞墙撞死了");
					} else {
						System.err.println("撞到自己了");
					}
					timer.stop();
					timer = null;
					status = 0;
					String message = "您的" + scoreLabel.getText() + ",是否提交?";
					int ret = JOptionPane.showConfirmDialog(snakeComponent, message,"游戏结束",JOptionPane.YES_NO_OPTION);
					if (ret == JOptionPane.YES_OPTION) {
						saveScore();
					}
					return;
				}
				sign[i][j] = SNAKE_TYPE;
				snake.addFirst(new Pair(i,j));
				if (i != food.i || j != food.j) {
					// 改变蛇尾
					Pair tail = snake.pollLast();
					sign[tail.i][tail.j] = AREA_TYPE;	
				} else {
					scoreLabel.setText("得分:" + (snake.size() - 1));
					Random r = new Random();
					do {
						int fi = r.nextInt(WIDTH_SIZE);
						int fj = r.nextInt(HEIGHT_SIZE);
						if (sign[fi][fj] == AREA_TYPE) {
							sign[fi][fj] = FOOD_TYPE;
							food = new Pair(fi, fj);
							break;
						}
					} while(true);
				}
				repaint();
			}
		}
		
		private void changeDiretion(int key) {
			switch (key) {
		    case 37: // 左
		      direction =  KEY_RIGHT;
		      break;
			case 38:
			  direction = KEY_UP;
		      break;
		   	case 39:
		   	  direction = KEY_LEFT;
		      break;
		   	case 40:
		      direction = KEY_DOWN;
		      break;
		    default:
		      break;
		    }
		}
		
		private void saveScore() {
			// 如果得分进入前十，保存到文件
			File f = new File("./eatingsnakescore.txt");
			try {
				StringBuilder sb = new StringBuilder();
				if (f.exists()) {
					 BufferedReader reader = new BufferedReader(new FileReader(f));
					 String tempString = null;
					 List<ScoreInfo> list = new LinkedList<ScoreInfo>();
		             while ((tempString = reader.readLine()) != null) {
			           String score = tempString.substring(0, tempString.indexOf(' '));
			           String time = tempString.substring(tempString.indexOf(' ') + 1);
			           list.add(new ScoreInfo(Integer.parseInt(score),Long.parseLong(time)));
			         }
			         reader.close();
			         list.add(new ScoreInfo(snake.size() - 1, new Date().getTime()));
			         Collections.sort(list);
			         for (int i = 0; i < list.size() && i < 10; ++i) {
			        	 sb.append("" + list.get(i));
			        	 sb.append("\n");
			         }
				} else {
					long ts = new Date().getTime();
					int score = snake.size() - 1;
					String message = "" + score + " " + ts + "\n";
					sb.append(message);
				}
				FileWriter fw = new FileWriter("./eatingsnakescore.txt");
				fw.write(sb.toString());
				fw.close();
				loadScoreinfo();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private Rectangle2D[][] squares;
		private int[][] sign;
		private Deque<Pair> snake; // 蛇身
		private Pair food;
		private int direction = 0; // 方向  1 左 2 右 3 上 4 下
		private int status = 0; // 状态 0结束 1运行 2暂停
		private int level = 3; // 难度等级 3初级 2中级 1高级
		private Timer timer;
		private final int WIDTH_SIZE = 100;
		private final int HEIGHT_SIZE = 60;
		private final int SNAKE_TYPE = 2;
		private final int FOOD_TYPE = 1;
		private final int AREA_TYPE = 0;
		private final int KEY_LEFT = 1;
		private final int KEY_RIGHT = 2;
		private final int KEY_UP = 3;
		private final int KEY_DOWN = 4;
	}
}

public class EatingSnake {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EatingSnakeFrame frame = new EatingSnakeFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setResizable(false); // 是否可以改变窗口大小
				frame.setVisible(true);
			}
		});
	}
}

