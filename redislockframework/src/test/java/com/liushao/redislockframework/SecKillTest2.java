package com.liushao.redislockframework;


import java.util.concurrent.CountDownLatch;

import org.junit.Test;

public class SecKillTest2 {
	
	@Test
	public void testSecKill(){
		int size=100;
		CountDownLatch beginCount = new CountDownLatch(1);
		CountDownLatch endCount = new CountDownLatch(size);
		Thread[] threads = new Thread[size];
		RedisLock lock = new RedisLock("abc", "123");
		RedisClient redisClient=RedisFactory.getDefaultClient();
		redisClient.set("execute", "0");
		redisClient.set("no-execute", "0");
		for(int i= 0;i < size;i++){
			threads[i] = new Thread(new MyTask(beginCount, endCount, redisClient, lock));
			threads[i].start();

		}
		long startTime=System.currentTimeMillis();
		beginCount.countDown();
		try {
			endCount.await();
			System.out.println(System.currentTimeMillis()-startTime);
			System.out.println("excuteSize="+redisClient.getByKey("execute"));
			System.out.println("notExcuteSize="+redisClient.getByKey("no-execute"));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
class MyTask implements Runnable{
	private CountDownLatch beginCount;
	private CountDownLatch endCount;
	private RedisClient redisClient;
	private RedisLock lock;
	



	public MyTask(CountDownLatch beginCount, CountDownLatch endCount, RedisClient redisClient, RedisLock lock) {
		super();
		this.beginCount = beginCount;
		this.endCount = endCount;
		this.redisClient = redisClient;
		this.lock = lock;
	}




	@Override
	public void run() {

		try {
			beginCount.await();
			boolean result=lock.lock(30, 20000);
			if(result) {
				incr("execute",1000);
			}else {
				incr("no-execute",1000);
			}
			Thread.sleep(35);
			lock.unlock();
		} catch (InterruptedException e) {
		}finally {
			endCount.countDown();
		}
	
		
	}




	private void incr(String key,int expireSeonds) {
		this.redisClient.incr(key);
		this.redisClient.expire(key, expireSeonds);
	}
	
}
