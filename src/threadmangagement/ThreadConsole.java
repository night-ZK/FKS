package threadmangagement;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程管理类
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
	 * 返回一个线程池
	 * @return 线程池
	 */
	public static ThreadPoolExecutor useThreadPool() {
		return threadpool;
	}
}
