package threadmangagement;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * �̹߳�����
 * @author zxk
 *
 */
public class ThreadConsole {
	private static ThreadPoolExecutor threadpool;
	private static int threadCount;
	static {
		//TODO change 50, use XML
		threadCount = 50;
		threadpool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount);
	}
	private ThreadConsole() {
		
	}
	
	/**
	 * ����һ���̳߳�
	 * @return �̳߳�
	 */
	public static ThreadPoolExecutor useThreadPool() {
		return threadpool;
	}
}
