import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.Timer;

public class Tetris {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				TetrisFrame frame = new TetrisFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				//frame.setResizable(false);
				frame.setVisible(true);
			}
			
		});
	}

}

@SuppressWarnings("serial")
class TetrisFrame extends JFrame {
	public TetrisFrame() {
		setTitle("俄罗斯方块");
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		setLocationByPlatform(true);
		
		setLayout(null);
		tetrisComponent = new TetrisComponent();
		tetrisComponent.setSize(DEFAULT_WIDTH * 2 / 3 + 2, DEFAULT_HEIGHT-50 + 2);
		add(tetrisComponent);
		tetrisComponent.setLocation(0, 0);
		controlPanel = new JPanel();
		controlPanel.setSize(DEFAULT_WIDTH * 1 / 3 - 5, DEFAULT_HEIGHT-50);
		add(controlPanel);
		controlPanel.setLocation(DEFAULT_WIDTH * 2 / 3, 0);
		controlPanel.setLayout(new BorderLayout());
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(3,1));
		startButton = new JButton("开  始");
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				tetrisComponent.startGame();
				tetrisComponent.requestFocus();
			}
		});
		buttonPanel.add(startButton);
		
		suspendButton = new JButton("暂  停");
		suspendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if (tetrisComponent.getStatus() != 0) {
					if (suspendButton.getText() == "暂  停") {
						suspendButton.setText("继  续");
						tetrisComponent.suspendResumeGame(1);
					} else {
						tetrisComponent.requestFocus();
						tetrisComponent.suspendResumeGame(2);
						suspendButton.setText("暂  停");
					}
				}
			}
		});
		buttonPanel.add(suspendButton);
		
		stopButton = new JButton("停  止");
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				tetrisComponent.stopGame();
			}
		});
		buttonPanel.add(stopButton);
		controlPanel.add(buttonPanel, BorderLayout.NORTH);
		
		nextTetrisPanel = new JPanel();
		controlPanel.add(nextTetrisPanel, BorderLayout.CENTER);
		
		scoreLabel = new JLabel();
		controlPanel.add(scoreLabel, BorderLayout.SOUTH);
		scoreLabel.setText("得分：0");
		tetrisComponent.init(nextTetrisPanel, scoreLabel);
	}
	
	// 组件定义
	private TetrisComponent tetrisComponent; // 游戏区
	private JPanel controlPanel; // 游戏控制区
	private JPanel buttonPanel;
	private JButton startButton; // 开始
	private JButton suspendButton;  // 暂停
	private JButton stopButton; // 结束
	private JPanel nextTetrisPanel; // 下一个方块类别
	private JLabel scoreLabel; // 得分信息
	
	private final int DEFAULT_WIDTH = 300;
	private final int DEFAULT_HEIGHT = 450;
}

@SuppressWarnings("serial")
class TetrisComponent extends JComponent {
	public TetrisComponent() {
		sign = new int[ROW][COLUMN];
		squares = new Rectangle2D[ROW][COLUMN];
	}
	
	public void init(JPanel nextTetris, JLabel scoreLabel) {
		//this.nextTetris = nextTetris;
		this.scoreLabel = scoreLabel;
		int height = getHeight();
		int width = getWidth();
		double pWidth = width / COLUMN;
		double pHeight = height / ROW;
		for (int i = 0; i < ROW; ++i) {
			for (int j = 0; j < COLUMN; ++j) {
				squares[i][j] = new Rectangle2D.Double(j*pWidth, i*pHeight, pWidth, pHeight);
			}
		}
		repaint();
		addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				int key = arg0.getKeyCode();
				if ((key == 37 || key == 38 || key == 39) &&
					(status == 1)) {
					keyControlGame(key);
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
	
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		int i = 0, j = 0;
		for (; i < squares.length; ++i) {
			for (j = 0; j < squares[i].length; ++j) {
				if (sign[i][j] == 1) {
					g2.setColor(Color.ORANGE);  // 已经堆积的方块
				} else if (sign[i][j] == 0) {
					g2.setColor(Color.WHITE);  // 其他空白区域
				} else {
					g2.setColor(color[sign[i][j]-2]); // 待处理方块
				}
				g2.fill(squares[i][j]);
				g2.setColor(Color.BLACK);
				g2.draw(squares[i][j]);
			}
		}
	}
	
	public void startGame() {
		if (status == 0) {
			status = 1;
			sign = new int[ROW][COLUMN];
			getNextSquare();
			for (Pair e : nextSquare) {
				sign[e.i][e.j] = colorType;
			}
			repaint();
			scoreLabel.setText("得分：0");
			timer = new Timer(300, new TimerAction());
			timer.start();
		}
	}
	
	public void suspendResumeGame(int type) {
		if (timer != null) {
			if (timer.isRunning() && type == 1) {
				timer.stop();
				status = 2;
			} else if (!timer.isRunning() && type == 2) {
				timer= new Timer(300, new TimerAction());
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
			String message = "游戏结束，您的得分为：" + score;
			JOptionPane.showMessageDialog(this, message);
		}
	}
	
	public int getStatus() {
		return status;
	}
	
	private void keyControlGame(int key) {
		lock.lock();
		switch (key) {
		case 37:{ // 左
	   		int minj = COLUMN - 1;
		    for (Pair e : nextSquare) {
		      if (e.j < minj)
		    	  minj = e.j;
		      if (e.j == 0 || sign[e.i][e.j - 1] == 1) {
		    	  minj = 0;
		    	  break;
		      }
		    }
		    if (minj > 0) {
		      for (Pair e : nextSquare) {
		    	sign[e.i][e.j] = 0;
		      }
		      for (Pair e : nextSquare) {
		    	sign[e.i][--e.j] = colorType;
		      }
		    }
	      break;
	   	}
		case 38:{ // 上，变换图像形状
		  ArrayList<Pair> tmp = initTmpPairList();
		  boolean is_change = true;
		  switch (nextType) {
		  case 1:{
			  Pair p1 = tmp.get(0);
			  Pair p2 = tmp.get(1);
			  if (p1.i == p2.i) {
				  p2.i += 1;
				  p2.j = p1.j;
			  } else {
				  p2.i = p1.i;
				  p2.j = p1.j + 1;
			  }
			  break;
		  }
		  case 4:{
			  Pair p1 = tmp.get(0);
			  Pair p2 = tmp.get(1);
			  Pair p3 = tmp.get(2);
			  Pair p4 = tmp.get(3);
			  if (p1.j == p4.j) {
				  p1.j -= 1;
				  p2.j -= 1;
				  p3.j += 1;
				  p4.j += 1;
			  } else {
				  p1.j += 1;
				  p2.j += 1;
				  p3.j -= 1;
				  p4.j -= 1;
			  }
			  break;
		  }
		  case 5:{
			  Pair p1 = tmp.get(0);
			  Pair p4 = tmp.get(3);
			  if (p1.j < p4.j) {
				  p1.j += 1;
				  p4.j -= 1;
			  } else {
				  p1.j -= 1;
				  p4.j += 1;
			  }
			  break;
		  }
		  case 6:{
			  Pair p1 = tmp.get(0);
			  Pair p4 = tmp.get(3);
			  Pair p5 = tmp.get(4);
			  if (p1.j < p4.j) {
				  p4.j -= 2;
				  p5.j -= 2;
			  } else {
				  p4.j += 2;
				  p5.j += 2;
			  }
			  break;
		  }
		  case 7:{
			  Pair p1 = tmp.get(0);
			  Pair p2 = tmp.get(1);
			  Pair p5 = tmp.get(4);
			  if (p1.j < p5.j) {
				  p1.j += 2;
				  p2.j += 2;
			  } else {
				  p1.j -= 2;
				  p2.j -= 2;
			  }
			  break;
		  }
		  case 8:{
			  Pair p1 = tmp.get(0);
			  Pair p2 = tmp.get(1);
			  Pair p3 = tmp.get(2);
			  Pair p4 = tmp.get(3);
			  Pair p5 = tmp.get(4);
			  Pair p6 = tmp.get(5);
			  Pair p7 = tmp.get(6);
			  if (p1.i == p4.i) {
				  p1.i -= 3;
				  p2.i -= 2;
				  p3.i -= 1;
				  p5.i += 1;
				  p6.i += 2;
				  p7.i += 3;
				  p1.j = p2.j = p3.j = p5.j = p6.j = p7.j = p4.j;
			  } else {
				  p1.j += 3;
				  p2.j += 2;
				  p3.j += 1;
				  p5.j -= 1;
				  p6.j -= 2;
				  p7.j -= 3;
				  p1.i = p2.i = p3.i = p5.i = p6.i = p7.i = p4.i;
			  }
			  break;
		  }
		  default:
			  is_change = false;
			  break;
		  }
		  if (is_change) {
			  for (Pair e : tmp) {
				  if (e.i < 0 || e.i > ROW - 1 || e.j < 0 || e.j > COLUMN - 1 || sign[e.i][e.j] == 1) {
					  is_change = false;
					  break;
				  }
			  }
		  }
		  if (is_change) {
			  for (Pair e : nextSquare) {
				  sign[e.i][e.j] = 0;
			  }
			  nextSquare = tmp;
			  for (Pair e : nextSquare) {
				  sign[e.i][e.j] = colorType;
			  }
		  }
	      break;
		}
		case 39:{ // 右
		  int maxj = 0;
		  for (Pair e : nextSquare) {
		    if (e.j > maxj)
		  	  maxj = e.j;
		    if (e.j == COLUMN - 1 || sign[e.i][e.j + 1] == 1) {
		  	  maxj = COLUMN - 1;
		  	  break;
		    }
		  }
		  if (maxj < COLUMN - 1) {
		    for (Pair e : nextSquare) {
		  	  sign[e.i][e.j] = 0;
		    }
		    for (Pair e : nextSquare) {
		      sign[e.i][++e.j] = colorType;
		    }
		  }
		  break;
		}
		}
		lock.unlock();
		repaint();
	}
	
	private void getNextSquare() {
		nextSquare = new ArrayList<Pair>();
		Random r = new Random();
		nextType = r.nextInt(9);
		switch (nextType) {
		case 0: // 一个方块
			nextSquare.add(new Pair(0,4));
			colorType = 2;
			break;
		case 1:// 两个方块
			nextSquare.add(new Pair(0,4));
			nextSquare.add(new Pair(0,5));
			colorType = 3;
			break;
		case 2:
			nextSquare.add(new Pair(0,4));
			nextSquare.add(new Pair(0,5));
			nextSquare.add(new Pair(1,4));
			nextSquare.add(new Pair(1,5));
			colorType = 4;
			break;
		case 3:
			nextSquare.add(new Pair(0,4));
			nextSquare.add(new Pair(0,5));
			nextSquare.add(new Pair(0,6));
			nextSquare.add(new Pair(1,4));
			nextSquare.add(new Pair(1,5));
			nextSquare.add(new Pair(1,6));
			nextSquare.add(new Pair(2,4));
			nextSquare.add(new Pair(2,5));
			nextSquare.add(new Pair(2,6));
			colorType = 2;
			break;
		case 4:
			nextSquare.add(new Pair(0,4));
			nextSquare.add(new Pair(0,5));
			nextSquare.add(new Pair(1,5));
			nextSquare.add(new Pair(1,6));
			colorType = 3;
			break;
		case 5:
			nextSquare.add(new Pair(0,4));
			nextSquare.add(new Pair(1,4));
			nextSquare.add(new Pair(1,5));
			nextSquare.add(new Pair(2,5));
			colorType = 4;
			break;
		case 6:
			nextSquare.add(new Pair(0,4));
			nextSquare.add(new Pair(0,5));
			nextSquare.add(new Pair(0,6));
			nextSquare.add(new Pair(1,6));
			nextSquare.add(new Pair(2,6));
			colorType = 2;
			break;
		case 7:
			nextSquare.add(new Pair(0,4));
			nextSquare.add(new Pair(1,4));
			nextSquare.add(new Pair(2,4));
			nextSquare.add(new Pair(2,5));
			nextSquare.add(new Pair(2,6));
			colorType = 3;
			break;
		case 8:
			nextSquare.add(new Pair(0,2));
			nextSquare.add(new Pair(0,3));
			nextSquare.add(new Pair(0,4));
			nextSquare.add(new Pair(0,5));
			nextSquare.add(new Pair(0,6));
			nextSquare.add(new Pair(0,7));
			nextSquare.add(new Pair(0,8));
			colorType = 4;
			break;
		}
	}
	
	// 深拷贝
	private ArrayList<Pair> initTmpPairList() {
		ArrayList<Pair> tmp = new ArrayList<Pair>();
		for (Pair p : nextSquare) {
			tmp.add(new Pair(p.i,p.j));
		}
		return tmp;
	}
	
	// 连成一排消除
	private void removeAndScored() {
		int removeRows = 0;
		for (int i = 0; i < ROW; ++i) {
			int j = 0;
			for (; j < COLUMN; ++j) {
				if (sign[i][j] != 1) {
					break;
				}
			}
			if (j == COLUMN) {
				++removeRows;
				for (j = 0; j < COLUMN; ++j) {
					sign[i][j] = 0;
				}
				for (int k = i-1; k >= 0; k--) {
					for (int t = 0; t < COLUMN; t++) {
						if (sign[k][t] == 1) {
							sign[k][t] = 0;
							sign[k+1][t] = 1;
						}
					}
				}
			}
		}
		if (removeRows > 0) {
			score += (2 * removeRows - 1) * COLUMN;
			scoreLabel.setText("得分：" + score);
			repaint();
		}
	}
	
	// 判断程序结束
	private void judgeGameOver() {
		boolean over = false;
		for (Pair p : nextSquare) {
			if (sign[p.i][p.j] == 1) {
				over = true;
				break;
			}
		}
		if (over) {
			timer.stop();
			timer = null;
			status = 0;
			for (Pair p : nextSquare) {
				if (sign[p.i][p.j] == 0)
					sign[p.i][p.j] = colorType;
			}
			repaint();
			String message = "无处安放，游戏结束，您的得分为：" + score;
			JOptionPane.showMessageDialog(this, message);
		}
	}
	
	class TimerAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			lock.lock();
			ArrayList<Pair> tmp = initTmpPairList();
			for (Pair p : tmp) {
				p.i += 1;
			}
			for (Pair p : nextSquare) {
				sign[p.i][p.j] = 0;
			}
			nextSquare = tmp;
			boolean over = false;
			for (Pair p : nextSquare) {
				if (p.i == ROW) {
					over = true;
					break;
				}
				if (sign[p.i][p.j] == 1) {
					over = true;
					break;
				}
			}
			if (over) {
				for (Pair p : nextSquare) {
					sign[p.i-1][p.j] = 1;
				}
			} else {
				for (Pair p : nextSquare) {
					sign[p.i][p.j] = colorType;
				}
			}
			repaint();
			if (over) {
				// 判断是否得分
				removeAndScored();
				// 获取下一个方块
				getNextSquare();
				// 判断是否结束
				judgeGameOver();
			}
			lock.unlock();
		}
		
	}
	
	private int status;
	private int score;
	private int[][] sign;
	private Rectangle2D[][] squares;
	//private JPanel nextTetris;
	private JLabel scoreLabel;
	private ArrayList<Pair> nextSquare; // 下一个需处理方块
	/*
	 * 0 一个方块 2 两个方块 3 四个方块 4 九个方块 5 横梯子 6 竖梯子 7 下三角 8 上三角 9 7个横排方块 
	 */
	private int nextType; // 下一个需处理方块类型
	private int colorType;
	private Timer timer;
	private Lock lock = new ReentrantLock();
	
	private final int ROW = 20;
	private final int COLUMN = 10;
	// 颜色定义
	private final Color[] color = new Color[] {Color.RED,Color.BLUE,Color.GREEN};
}
