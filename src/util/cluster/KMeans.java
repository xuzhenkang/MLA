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
	 * ��¼�������������е������������ȼ���kֵ
	 */
	private int[] centers; 
	
	/**
	 * �����ж���������¼��һ�δ����ĵ�����
	 */
	private double[] centersXOld; 
	private double[] centersYOld; 

	/**
	 * ���������� 
	 */
	private double[] centersX;
	private double[] centersY;

	/**
	 * ��ʼ��centers���飬������Ԫ�ص�ֵ��Ϊnum
	 * 
	 * @param num
	 */
	private void initCenters(int num) {
		for (int i = 0; i < centers.length; i++)
			centers[i] = num;
	}

	/**
	 * ����centers����
	 */
	public void displayCenters() {
		for (int i = 0; i < centers.length; i++) {
			System.out.print(centers[i] + " ");
		}
		System.out.println();
	}

	/**
	 *  x.get(i)��ʾ��i�����x���꣬ y.get(i)��ʾ��i�����y����
	 */
	private LinkedList<Double> x = new LinkedList<Double>();
	private LinkedList<Double> y = new LinkedList<Double>();

	/**
	 *  xx.get(i)��ʾ��i���������Ĵ����ĵ�x����,yy.get(i)��ʾ��i���������Ĵ����ĵ�y����
	 */
	private LinkedList<Double> xx = new LinkedList<Double>();
	private LinkedList<Double> yy = new LinkedList<Double>();

	/**
	 * ���ι��캯��������Ϊkֵ���������ɵ�centers����Ĭ��ֵΪ-1
	 * 
	 * @param k
	 */
	public KMeans(int k) {
		this.centers = new int[k];
		initCenters(-1);
		// ������k������������
		centersX = new double[k];
		centersXOld = new double[k];
		centersY = new double[k];
		centersYOld = new double[k];
	}

	/**
	 * �޲ι��캯����Ĭ��kֵΪ3���������ɵ�centers����Ĭ��ֵΪ-1
	 */
	public KMeans() {
		this.centers = new int[3];
		initCenters(-1);
		// ������k������������
		centersX = new double[3];
		centersXOld = new double[3];
		centersY = new double[3];
		centersYOld = new double[3];
	}

	/**
	 * @return kֵ
	 */
	public int getK() {
		return this.centers.length;
	}

	/**
	 * ��������
	 * 
	 * @param file
	 *            �����ļ�
	 * @throws IOException
	 */
	public void loadData(File file) throws IOException {
		FileReader fr = new FileReader(file);
		BufferedReader bf = new BufferedReader(fr);
		String lineTxt;
		while ((lineTxt = bf.readLine()) != null) {
			// ƥ��һ�������ո��Լ�һ��tab����tab�Լ����Ǽ䲻ͬ���
			String[] data = lineTxt.split("\\s{1,}|\t{1,}");
			if (data.length == 2 && data[0] != "" && data[1] != "") {
				// ���ø�������
				x.add(Double.parseDouble(data[0]));
				y.add(Double.parseDouble(data[1]));
				// ��ʼ������ԵĴ���������Ϊ����
				xx.add(Double.parseDouble(data[0]));
				yy.add(Double.parseDouble(data[1]));
			}
		}
		bf.close();
	}

	/**
	 * �����д���ļ���
	 * @param file
	 * @throws IOException
	 */
	public void outputData(File file) throws IOException {
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		for (int i = 0; i < centersX.length; i++) {
			bw.write("��" + i + "��վ������꣺(" + centersX[i] + "," + centersY[i] + ")\r\n");
		}
		bw.flush();
		bw.close();
	}
	/**
	 * ���ָ�����ݵ��ļ���
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
	 * ��������ʽ��ʾ�����ص�����
	 */
	public void displayData() {
		Iterator<Double> itx = x.iterator();
		Iterator<Double> ity = y.iterator();

		while (itx.hasNext() && ity.hasNext()) {
			System.out.println("(" + itx.next() + "," + ity.next() + ")");
		}
	}

	// --------------------------�㷨����--------------------------
	public void train() {
		
		int iterations = 0; // ͳ�Ƶ�������
		
		// 1.��ʼ���������ݼ������ѡȡk���㣬��Ϊ������
		findDiffKValueFromRange(0, x.size(), centers); // ���ѡȡk����
		for (int i = 0; i < centers.length; i++) { // k����������Ϊ����������,centersXOld��centersYOld����ʼ��Ϊ-1��
			centersX[i] = x.get(centers[i]);
			centersY[i] = y.get(centers[i]);
			centersXOld[i] = -1;
			centersYOld[i] = -1;
		}
		
		// �������������һֱ������ȥ
		while (!isAstringed()) {
			iterations++;
			// 2.����k���أ��������е�����k�������ĵľ��룬�Ҿ�����С���Ǹ���������Ϊ�õ�Ĵ����ģ������¸õ������Ĵ���������
			dividePoints2KClusters();
			
			// 3.�����ֵ���´����ģ�����ÿ���������е��ƽ��ֵ���꣬ ���´���������
			updateCenters();
		}
		for (int i = 0; i < centersX.length; i++) {
			System.out.println("��" + i + "�����������꣺(" + centersX[i] + "," + centersY[i] + ")");
		}
	}
	/**
	 * �ж��Ƿ�����������Ϊ�����������겻�ڱ仯��
	 * ����centersXOld��centersYOld���������ֵ����º��centersX��centersY���������ֵ��Ӧ��ȡ�
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
	 * �����ֵ���´����ģ�����ÿ���������е��ƽ��ֵ���꣬ ���´���������
	 */
	private void updateCenters() {
		for (int i = 0; i < centersX.length; i++) { // ����������
			// ����ǰ���Ƚ������������¼������centersXOld,centersYOld�����У������ж�������
			centersXOld[i] = centersX[i];
			centersYOld[i] = centersY[i];
			
			LinkedList<Integer> pointIndexs = new LinkedList<Integer>(); // ��ŵ�ǰ���ڵĵ������
			pointIndexs = findPointByCenter(centersX[i], centersY[i]);
			// �����ֵ
			double xSum = 0, ySum = 0;
			for (int index : pointIndexs) {
				xSum += x.get(index);
				ySum += y.get(index);
			}
			double everageX = xSum / pointIndexs.size(), everageY = ySum / pointIndexs.size();
			// ���´���������
			centersX[i] = everageX;
			centersY[i] = everageY;
		}
	}
	
	/**
	 * ���Ҹ��������ĸ����ĵ㼯
	 * @param x �����ĵ�x����
	 * @param y �����ĵ�y����
	 * @return �������ݼ��е���������ɵļ���
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
	 * �������е�����k�������ĵľ��룬�Ҿ�����С���Ǹ���������Ϊ�õ�Ĵ����ģ������¸õ�Ĵ��������꣬��ʵ�ǰ����е㻮��Ϊk����
	 */
	private void dividePoints2KClusters() {
		for (int i = 0; i < x.size(); i++) { // ����������
			// ����õ�(x.get(i), y.get(i))����������ĵľ���,�ҳ�������С���Ǹ�������
			double minDistance = Double.MAX_VALUE;
			for (int j = 0; j < centersX.length; j++) { // ��������������
				double distance = getDistanceAB(x.get(i), y.get(i), centersX[j], centersY[j]);
				//double distance = Earth.getDistance(x.get(i), y.get(i), centersX[j], centersY[j]);
				if (minDistance > distance) {
					minDistance = distance;
					// ���õ������Ĵ���������Ϊ(centersX[j], centersY[j])
					xx.set(i, centersX[j]);
					yy.set(i, centersY[j]);
				}
			} // ������for��䣬�õ�(x.get(i),  y.get(i))�����Ĵ����ı��ҵ�������minDistance��ŵ��Ǹõ���õ������Ĵ�����֮��ľ��롣
		}
	}

	// -----------------------�㷨Ҫʹ�õ���������---------------------

	private double getDistanceAB(double aX, double aY, double bX, double bY) {

		return Math.sqrt((aX - bX) * (aX - bX) + (aY - bY) * (aY - bY));
	}

	/**
	 * ���ָ����Χ��n�����ظ�����,����centers�����Ų�ͬ��ֵ
	 * 
	 * @param min
	 * @param max
	 * @param centers
	 */
	private void findDiffKValueFromRange(int min, int max, int[] centers) {
		TreeSet<Integer> set = new TreeSet<Integer>();
		// ��������randomSet()ֱ��centers����Ϊֹ
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
	 * ���ָ����Χ��n�����ظ�����,����Set��������ֻ�ܴ�Ų�ͬ��ֵ ��
	 * ע�⣺�÷������������ɵ����ֵΪͬһֵ����SortedSet������Ԫ����Ŀ��С��nֵ
	 * ����Ҫ�������øú�����SortedSet����Ԫ����Ŀ����nֵ��
	 * 
	 * @param min
	 *            ָ����Χ��Сֵ
	 * @param max
	 *            ָ����Χ���ֵ
	 * @param n
	 *            ���������
	 * @param set
	 *            ����������
	 */
	private void randomSet(int min, int max, int n, SortedSet<Integer> set) {
		if (n > (max - min + 1) || max < min) {
			return;
		}
		for (int i = 0; i < n; i++) {
			int num = (int) (Math.random() * (max - min)) + min;
			set.add(num); // ����ͬ��������HashSet��
		}
	}

	/**
	 * ��ȡ������x��������
	 * @return
	 */
	public double[] getCentersX() {
		return centersX;
	}

	/**
	 * ��ȡ������y��������
	 * @return
	 */
	public double[] getCentersY() {
		return centersY;
	}

	/**
	 * ��ȡ���ݵ�x��������
	 * @return
	 */
	public LinkedList<Double> getX() {
		return x;
	}

	/**
	 * ��ȡ���ݵ�y��������
	 * @return
	 */
	public LinkedList<Double> getY() {
		return y;
	}

	/**
	 * ��ȡ���ݵ������Ĵ����ĵ��x��������
	 * @return
	 */
	public LinkedList<Double> getXx() {
		return xx;
	}

	/**
	 * ��ȡ���ݵ������Ĵ����ĵ��y��������
	 * @return
	 */
	public LinkedList<Double> getYy() {
		return yy;
	}
}
