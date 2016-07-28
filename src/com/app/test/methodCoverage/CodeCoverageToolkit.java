package com.app.test.methodCoverage;

public class CodeCoverageToolkit {
	/**
	 * 初始化二个三维数组 bbb：存储第i个类中第j个方法第k个分支是否已经isReached 0表示false， 1表示true
	 * lll：存储第i个类中第j个方法第k个分支的lines
	 */
	public static int[][][] bbb = new int[10000][][];
	public static int[][][] lll = new int[10000][][];
	public static int classCount = 0;

	/**
	 * @return 获得reach的行数
	 */
	public static int calculateLines() {
		int reachLines = 0;
		for (int i = 0; i < classCount; i++) {
			for (int j = 0; j < bbb[i].length; j++) {
				for (int k = 0; k < bbb[i][j].length; k++) {
					int total = bbb[i][j][k] == 0 ? 0 : lll[i][j][k];
					reachLines = reachLines + total;
				}
			}
		}
		return reachLines;
	}

	/**
	 * 初始化一个二维数组
	 * 
	 * @param i
	 *            第i个class
	 * @param j
	 *            包含j个method
	 */
	public static void initbbblllij(int i, int j) {
		bbb[i - 1] = new int[j][];
		lll[i - 1] = new int[j][];
		classCount = i;
	}

	/**
	 * 第i个class第j个method包含k个branch
	 * 
	 * @param i
	 *            第i个class
	 * @param j
	 *            第j个method
	 * @param k
	 *            包含k个branch
	 */
	public static void initbbblllijk(int i, int j, int k) {
		bbb[i - 1][j - 1] = new int[k];
		lll[i - 1][j - 1] = new int[k];
	}

	/**
	 * 设置第i个class第j个method第k个branch: isReached和length
	 * 
	 * @param i
	 *            第i个class
	 * @param j
	 *            第j个method
	 * @param k
	 *            第k个branch
	 * @param length
	 *            length of lines
	 */
	public static void instrumentData(int i, int j, int k, int length) {
		bbb[i - 1][j - 1][k - 1] = 1;
		lll[i - 1][j - 1][k - 1] = length;
	}

	public static int getClassCount() {
		return classCount;
	}

	public static void setClassCount(int classCount) {
		CodeCoverageToolkit.classCount = classCount;
	}

	/**
	 * 打印输出reach的结果
	 */
	public static void printResult() {
		int calculateLines = calculateLines();
		System.out.println("Reached Lines: " + calculateLines);
	}

}
