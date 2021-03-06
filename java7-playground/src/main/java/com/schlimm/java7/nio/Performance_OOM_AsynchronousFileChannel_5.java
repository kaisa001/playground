package com.schlimm.java7.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.schlimm.java7.benchmark.original.Average;
import com.schlimm.java7.benchmark.original.PerformanceChecker;
import com.schlimm.java7.benchmark.original.PerformanceHarness;

/**
 * 
 * @author Niklas Schlimm
 * 
 */
public class Performance_OOM_AsynchronousFileChannel_5 implements Runnable {

	private static AsynchronousFileChannel outputfile;
	private static AtomicInteger fileindex = new AtomicInteger(0);
	private static AtomicInteger threadcound = new AtomicInteger(0);
	private static ThreadPoolExecutor pool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 0, TimeUnit.MILLISECONDS,
			new SynchronousQueue<Runnable>(), new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r);
					t.setDaemon(true);
					int c = threadcound.getAndIncrement();
					boolean b = (c % 1000 == 0) ? true : false;
					if (b) System.out.println("1000");
					return t;
				}
			});

	public static void main(String[] args) throws InterruptedException, IOException {
		try {
			outputfile = AsynchronousFileChannel.open(
					Paths.get("E:/temp/afile.out"),
					new HashSet<StandardOpenOption>(Arrays.asList(StandardOpenOption.WRITE, StandardOpenOption.CREATE,
							StandardOpenOption.DELETE_ON_CLOSE)), pool);
			Average average = new PerformanceHarness().calculatePerf(new PerformanceChecker(1000,
					new Performance_OOM_AsynchronousFileChannel_5()), 10);
			System.out.println("Mean: " + DecimalFormat.getInstance().format(average.mean()));
			System.out.println("Std. Deviation: " + DecimalFormat.getInstance().format(average.stddev()));
		} finally {
			outputfile.close();
			System.out.println("Pool threads created: " + threadcound.get());
		}
	}

	@Override
	public void run() {
		outputfile.write(ByteBuffer.wrap("Hello".getBytes()), fileindex.getAndIncrement() * 5);
	}
}
