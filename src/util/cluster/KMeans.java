package util.cluster;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

public class KMeans {
	/**
	 * 记录簇中心在数据中的索引，它长度即是k值
	 */
	private int[] centers; 
	
	/**
	 * 用来判断收敛，记录上一次簇中心点坐标
	 */
	private double[] centersXOld; 
	private double[] centersYOld; 

	/**
	 * 簇中心坐标 
	 */
	private double[] centersX;
	private double[] centersY;

	/**
	 * 初始化centers数组，将所有元素的值赋为num
	 * 
	 * @param num
	 */
	private void initCenters(int num) {
		for (int i = 0; i < centers.length; i++)
			centers[i] = num;
	}

	/**
	 * 遍历centers数组
	 */
	public void displayCenters() {
		for (int i = 0; i < centers.length; i++) {
			System.out.print(centers[i] + " ");
		}
		System.out.println();
	}

	/**
	 *  x.get(i)表示第i个点的x坐标， y.get(i)表示第i个点的y坐标
	 */
	private LinkedList<Double> x = new LinkedList<Double>();
	private LinkedList<Double> y = new LinkedList<Double>();

	/**
	 *  xx.get(i)表示第i个点所属的簇中心的x坐标,yy.get(i)表示第i个点所属的簇中心的y坐标
	 */
	private LinkedList<Double> xx = new LinkedList<Double>();
	private LinkedList<Double> yy = new LinkedList<Double>();

	/**
	 * 带参构造函数，参数为k值，并且生成的centers数组默认值为-1
	 * 
	 * @param k
	 */
	public KMeans(int k) {
		this.centers = new int[k];
		initCenters(-1);
		// 将生成k个簇中心坐标
		centersX = new double[k];
		centersXOld = new double[k];
		centersY = new double[k];
		centersYOld = new double[k];
	}

	/**
	 * 无参构造函数，默认k值为3，并且生成的centers数组默认值为-1
	 */
	public KMeans() {
		this.centers = new int[3];
		initCenters(-1);
		// 将生成k个簇中心坐标
		centersX = new double[3];
		centersXOld = new double[3];
		centersY = new double[3];
		centersYOld = new double[3];
	}

	/**
	 * @return k值
	 */
	public int getK() {
		return this.centers.length;
	}

	/**
	 * 加载数据
	 * 
	 * @param file
	 *            数据文件
	 * @throws IOException
	 */
	public void loadData(File file) throws IOException {
		FileReader fr = new FileReader(file);
		BufferedReader bf = new BufferedReader(fr);
		String lineTxt;
		while ((lineTxt = bf.readLine()) != null) {
			// 匹配一个或多个空格以及一个tab或多个tab以及它们间不同组合
			String[] data = lineTxt.split("\\s{1,}|\t{1,}");
			if (data.length == 2 && data[0] != "" && data[1] != "") {
				// 设置各点坐标
				x.add(Double.parseDouble(data[0]));
				y.add(Double.parseDouble(data[1]));
				// 初始化点各自的簇中心坐标为本身
				xx.add(Double.parseDouble(data[0]));
				yy.add(Double.parseDouble(data[1]));
			}
		}
		bf.close();
	}

	/**
	 * 将结果写入文件中
	 * @param file
	 * @throws IOException
	 */
	public void outputData(File file) throws IOException {
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		for (int i = 0; i < centersX.length; i++) {
			bw.write("第" + i + "个站点儿坐标：(" + centersX[i] + "," + centersY[i] + ")\r\n");
		}
		bw.flush();
		bw.close();
	}
	/**
	 * 输出指定内容到文件中
	 * @param file
	 * @param str
	 * @throws IOException
	 */
	public void outputData(File file, String str) throws IOException {
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(str);
		bw.flush();
		bw.close();
	}
	
	/**
	 * 以坐标形式显示所加载的数据
	 */
	public void displayData() {
		Iterator<Double> itx = x.iterator();
		Iterator<Double> ity = y.iterator();

		while (itx.hasNext() && ity.hasNext()) {
			System.out.println("(" + itx.next() + "," + ity.next() + ")");
		}
	}

	// --------------------------算法如下--------------------------
	public void train() {
		
		int iterations = 0; // 统计迭代次数
		
		// 1.初始化：在数据集中随机选取k个点，作为簇中心
		findDiffKValueFromRange(0, x.size(), centers); // 随机选取k个点
		for (int i = 0; i < centers.length; i++) { // k个点坐标作为簇中心坐标,centersXOld和centersYOld都初始化为-1；
			centersX[i] = x.get(centers[i]);
			centersY[i] = y.get(centers[i]);
			centersXOld[i] = -1;
			centersYOld[i] = -1;
		}
		
		// 如果不收敛，就一直迭代下去
		while (!isAstringed()) {
			iterations++;
			// 2.划分k个簇：计算所有点与这k个簇中心的距离，找距离最小的那个簇中心作为该点的簇中心，并更新该点所属的簇中心坐标
			dividePoints2KClusters();
			
			// 3.计算均值求新簇中心：计算每个簇中所有点的平均值坐标， 更新簇中心坐标
			updateCenters();
		}
		for (int i = 0; i < centersX.length; i++) {
			System.out.println("第" + i + "个簇中心坐标：(" + centersX[i] + "," + centersY[i] + ")");
		}
	}
	/**
	 * 判断是否收敛，条件为：簇中心坐标不在变化。
	 * 即，centersXOld，centersYOld数组的所有值与更新后的centersX，centersY数组的所有值对应相等。
	 * @return
	 */
	private boolean isAstringed() {
		for (int i = 0; i < centersX.length; i++) {
			if (centersXOld[i] != centersX[i] || centersYOld[i] != centersY[i]) {
				return false;
			} else continue;
		}
		return true;
	}

	/**
	 * 计算均值求新簇中心：计算每个簇中所有点的平均值坐标， 更新簇中心坐标
	 */
	private void updateCenters() {
		for (int i = 0; i < centersX.length; i++) { // 迭代各个簇
			// 更新前，先将簇中心坐标记录下来到centersXOld,centersYOld数组中，用来判断收敛。
			centersXOld[i] = centersX[i];
			centersYOld[i] = centersY[i];
			
			LinkedList<Integer> pointIndexs = new LinkedList<Integer>(); // 存放当前簇内的点的索引
			pointIndexs = findPointByCenter(centersX[i], centersY[i]);
			// 计算均值
			double xSum = 0, ySum = 0;
			for (int index : pointIndexs) {
				xSum += x.get(index);
				ySum += y.get(index);
			}
			double everageX = xSum / pointIndexs.size(), everageY = ySum / pointIndexs.size();
			// 更新簇中心坐标
			centersX[i] = everageX;
			centersY[i] = everageY;
		}
	}
	
	/**
	 * 查找给定簇中心附近的点集
	 * @param x 簇中心的x坐标
	 * @param y 簇中心的y坐标
	 * @return 点在数据集中的索引所组成的集合
	 */
	public LinkedList<Integer> findPointByCenter(double x, double y) {
		LinkedList<Integer> result = new LinkedList<Integer>();
		for (int i = 0; i < this.x.size(); i++) {
			if (xx.get(i) == x && yy.get(i) == y) {
				result.add(i);
			}
		}
		return result;
	}

	/**
	 * 计算所有点与这k个簇中心的距离，找距离最小的那个簇中心作为该点的簇中心，并更新该点的簇中心坐标，其实是把所有点划分为k个簇
	 */
	private void dividePoints2KClusters() {
		for (int i = 0; i < x.size(); i++) { // 遍历各个点
			// 计算该点(x.get(i), y.get(i))与各个簇中心的距离,找出距离最小的那个簇中心
			double minDistance = Double.MAX_VALUE;
			for (int j = 0; j < centersX.length; j++) { // 遍历各个簇中心
				double distance = getDistanceAB(x.get(i), y.get(i), centersX[j], centersY[j]);
				//double distance = Earth.getDistance(x.get(i), y.get(i), centersX[j], centersY[j]);
				if (minDistance > distance) {
					minDistance = distance;
					// 将该点所属的簇中心设置为(centersX[j], centersY[j])
					xx.set(i, centersX[j]);
					yy.set(i, centersY[j]);
				}
			} // 经过该for语句，该点(x.get(i),  y.get(i))所属的簇中心被找到，并且minDistance存放的是该点与该点所属的簇中心之间的距离。
		}
	}

	// -----------------------算法要使用的其他函数---------------------

	private double getDistanceAB(double aX, double aY, double bX, double bY) {

		return Math.sqrt((aX - bX) * (aX - bX) + (aY - bY) * (aY - bY));
	}

	/**
	 * 随机指定范围内n个不重复的数,利用centers数组存放不同的值
	 * 
	 * @param min
	 * @param max
	 * @param centers
	 */
	private void findDiffKValueFromRange(int min, int max, int[] centers) {
		TreeSet<Integer> set = new TreeSet<Integer>();
		// 反复调用randomSet()直到centers布满为止
		while (set.size() < centers.length) {
			randomSet(min, max, centers.length - set.size(), set);
		}
		Iterator<Integer> it = set.iterator();
		int index = 0;
		while (it.hasNext()) {
			centers[index] = it.next();
			index++;
		}
	}

	/**
	 * 随机指定范围内n个不重复的数,利用Set的特征，只能存放不同的值 。
	 * 注意：该方法如果多次生成的随机值为同一值，则SortedSet所含的元素数目会小于n值
	 * 。需要反复调用该函数将SortedSet所含元素数目等于n值。
	 * 
	 * @param min
	 *            指定范围最小值
	 * @param max
	 *            指定范围最大值
	 * @param n
	 *            随机数个数
	 * @param set
	 *            随机数结果集
	 */
	private void randomSet(int min, int max, int n, SortedSet<Integer> set) {
		if (n > (max - min + 1) || max < min) {
			return;
		}
		for (int i = 0; i < n; i++) {
			int num = (int) (Math.random() * (max - min)) + min;
			set.add(num); // 将不同的数存入HashSet中
		}
	}

	/**
	 * 获取簇中心x坐标数组
	 * @return
	 */
	public double[] getCentersX() {
		return centersX;
	}

	/**
	 * 获取簇中心y坐标数组
	 * @return
	 */
	public double[] getCentersY() {
		return centersY;
	}

	/**
	 * 获取数据点x坐标链表
	 * @return
	 */
	public LinkedList<Double> getX() {
		return x;
	}

	/**
	 * 获取数据点y坐标链表
	 * @return
	 */
	public LinkedList<Double> getY() {
		return y;
	}

	/**
	 * 获取数据点所属的簇中心点的x坐标数组
	 * @return
	 */
	public LinkedList<Double> getXx() {
		return xx;
	}

	/**
	 * 获取数据点所属的簇中心点的y坐标数组
	 * @return
	 */
	public LinkedList<Double> getYy() {
		return yy;
	}
}
