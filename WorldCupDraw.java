import java.util.Arrays;
import java.util.Random;

/*
 * 第一档：俄罗斯（东道主）、德国（欧洲）、巴西（南美）、葡萄牙（欧洲）、阿根廷（南美）、比利时（欧洲）、波兰（欧洲）、法国（欧洲）
 * 第二档：西班牙（欧洲）、秘鲁（南美）、瑞士（欧洲）、英格兰（欧洲）、哥伦比亚（南美）、墨西哥（中北美）、乌拉圭（南美）、克罗地亚（欧洲）
 * 第三档：丹麦（欧洲）、冰岛（欧洲）、哥斯达黎加（中北美）、瑞典（欧洲）、突尼斯（非洲）、埃及（非洲）、塞内加尔（非洲）、伊朗（亚洲）
 * 第四档：塞尔维亚（欧洲）、尼日利亚（非洲）、澳大利亚（亚洲）、日本（亚洲）、摩洛哥（非洲）、巴拿马（中北美）、韩国（亚洲）、沙特（亚洲）
 * 
 * 抽签时，非欧洲球队需要回避，如，第一档若巴西，那么，在第二档、第三档与第四档中，将不会出现来自南美的球队。
 * 虽然欧洲球队没有回避原则，但每个小组最多只能出现两支来自欧洲的球队。
 */

class CountryInfo {
	private int area;
	private String name;
	
	public CountryInfo(String name, int area) {
		this.area = area;
		this.name = name;
	}
	
	public int getArea() {
		return area;
	}
	
	public String toString() {
		return name;
	}
}

public class WorldCupDraw {

	public static final int EUROPE = 1;  // 欧洲
	public static final int ASIA = 2;    // 亚洲
	public static final int NORTH_AMERICA = 3; // 北美洲
	public static final int SOUTH_AMERICA = 4; // 南美洲
	public static final int AFRICA = 5;  // 非洲
  
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String[][] name = new String[4][];
		name[0] = new String[] {"俄罗斯","德国","巴西","葡萄牙","阿根廷","比利时","波兰","法国"};
		name[1] = new String[] {"西班牙","秘鲁","瑞士","英格兰","哥伦比亚","墨西哥","乌拉圭","克罗地亚"};
		name[2] = new String[] {"丹麦","冰岛","哥斯达黎加","瑞典","突尼斯","埃及","塞内加尔","伊朗"};
		name[3] = new String[] {"塞尔维亚","尼日利亚","澳大利亚","日本","摩洛哥","巴拿马","韩国","沙特"};
		int[][] area = new int[4][];
		area[0] = new int[]{EUROPE,EUROPE,SOUTH_AMERICA,EUROPE,SOUTH_AMERICA,EUROPE,EUROPE,EUROPE};
		area[1] = new int[]{EUROPE,SOUTH_AMERICA,EUROPE,EUROPE,SOUTH_AMERICA,NORTH_AMERICA,SOUTH_AMERICA,EUROPE};
		area[2] = new int[]{EUROPE,EUROPE,NORTH_AMERICA,EUROPE,AFRICA,AFRICA,AFRICA,ASIA};
		area[3] = new int[]{EUROPE,AFRICA,ASIA,ASIA,AFRICA,NORTH_AMERICA,ASIA,ASIA};
		
		CountryInfo[][] country = new CountryInfo[4][];
		for (int i = 0; i < 4; ++i) {
			country[i] = new CountryInfo[8];
			for (int j = 0; j < 8; ++j) {
				country[i][j] = new CountryInfo(name[i][j], area[i][j]);
			}
		}
		System.out.println("入围世界杯球队分档名单：");
		for (int i = 0; i < 4; ++i) {
			System.out.print("第" + (i+1) + "档：");
			System.out.println(Arrays.toString(country[i]));
		}
		
		DRAW_CONTINUE:
		for (;;) {
			// 抽签
			CountryInfo[][] result = new CountryInfo[4][8];
			// 第一轮抽签，种子队
			result[0][0] = country[0][0];
			int[] cIndex = new int[] {1,2,3,4,5,6,7};  // 球队在该档中序号
			int[] gIndex = new int[] {1,2,3,4,5,6,7};  // 分组序号
			for (int j = 0; j < 7; ++j) {
				Random r = new Random();
				int index = r.nextInt(7-j);
				int group = r.nextInt(7-j);
				result[0][gIndex[group]] = country[0][cIndex[index]];
				cIndex[index] = cIndex[7-j-1];
				gIndex[group] = gIndex[7-j-1];
			}
			
			for (int i = 1; i < 4; ++i) {  // 抽2，3，4轮
				cIndex = new int[] {0,1,2,3,4,5,6,7};
				int left = 8; // 剩余球队
				while (left > 0) {
					// 第一步：计算剩下的球队每个队可能的组
					int[][] tryIndex = new int[left][8];
					for (int ii = 0; ii < left; ++ii) {
						CountryInfo info = country[i][cIndex[ii]];
						for (int iii = 0; iii < 8; ++iii) { // 每个组来遍历一下
							if (result[i][iii] != null) { // 该组已经被选了
								tryIndex[ii][iii] = 1;
							} else {
								if (info.getArea() == EUROPE) { // 欧洲球队，该组不能超过两支
									int count = 0;
									for (int jj = 0; jj < i; ++jj) {
										if (result[jj][iii].getArea() == EUROPE) {
											count++;
										}
									}
									if (count >= 2) {
										tryIndex[ii][iii] = 1;
									}
								} else { // 非欧洲球队，同一个洲回避
									for (int jj = 0; jj < i; ++jj) {
										if (result[jj][iii].getArea() == info.getArea()) {
											tryIndex[ii][iii] = 1;
											break;
										}
									}
								}
							}
						}
					}
					// 第二步：判断是否有唯一的位置的，如果有则该队只能分到唯一的组
					int uniPos = 0;
					for (int ii = 0; ii < left; ++ii) {
						int count = 0, pos = 0;
						for (int jj = 0; jj < 8; ++jj) {
							if (tryIndex[ii][jj] == 0) { count++; pos = jj;}
						}
						if (count == 0) {
							System.out.println("这种情况就复杂了，不过不太常见，请重新执行一下吧！");
							continue DRAW_CONTINUE;
						}
						if (count == 1) {
							uniPos += 1;
							if (result[i][pos] != null) {
								System.out.println("有多个队都抽到唯一位置相同，这就得重新抽签了！");
								continue DRAW_CONTINUE;
							}
							result[i][pos] = country[i][cIndex[ii]];
							cIndex[ii] = -1;
						}
					}
					if (uniPos > 0) {
						for (int tmp1 = 0, tmp2 = 0; tmp1 < left; ++tmp1) {
							if (cIndex[tmp1] != -1) {
								cIndex[tmp2++] = cIndex[tmp1];
							}
						}
						left -= uniPos;
					} else {
						// 都没有唯一位置，随机选择一个球队，一个位置
						Random r = new Random();
						int index = r.nextInt(left); // 选中球队
						CountryInfo info = country[i][cIndex[index]];
						cIndex[index] = cIndex[left-1];
						left -= 1;
						int canPos = 0;
						//int[] group = tryIndex[index];
						int[] group = new int[8];
						for (int tmp = 0; tmp < 8; ++tmp) {
							if (tryIndex[index][tmp] == 0) {
								group[canPos++] = tmp; // 可选择的序号
							}
						}
						index = r.nextInt(canPos);
						result[i][group[index]] = info;
					}
				}
				
			}
			System.out.println("抽签结果：");
			for (int i = 0; i < 8; ++i) {
				System.out.printf("%c组【%s,%s,%s,%s】\n",'A'+i,result[0][i],result[1][i],result[2][i],result[3][i]);
			}
			break DRAW_CONTINUE;
		}
	}
}