[TOC]

# ThreadLocal

ThreadLocal相当于一个Map

```
Map threadLocal = new HashMap<Thread, object>();
```

1. 实现线程内的数据共享，即对于在不同线程中相同的程序代码，多个模块在同一个线程中运行时要共享一份数据，而在另外线程中运行时又共享另外一份数据。
2. Thread通过set方法，相当于在内部的map中增加了一条记录。线程结束后通过ThreadLocal.clear()方法，这样会更块的释放内存，不调用也可以，会自动释放。
3. Thread多用在事务操作中
4. 需要多个变量共享，可以定义过个ThreadLocal或者将变量放在一个对象中来一起处理

# ThreadLocal设计案例

```
class MyThreadScopeData{
	private MyThreadScopeData(){}
	public static /*synchronized*/ MyThreadScopeData getThreadInstance(){
		MyThreadScopeData instance = map.get();
		if(instance == null){
			instance = new MyThreadScopeData();
			map.set(instance);
		}
		return instance;
	}
	//private static MyThreadScopeData instance = null;//new MyThreadScopeData();
	private static ThreadLocal<MyThreadScopeData> map = new ThreadLocal<MyThreadScopeData>();
	
	private String name;
	private int age;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
}
```

# 线程内共享变量

如果每个线程内执行的代码相同，可以使用同一个Runnable对象，这个Runnable对象中有那个共享数据，例如买票系统可以那样做。

```
public class TicketMultiThread {
	public static void main(String[] args) {
		for (int i = 0; i < 5; i++) {
			new MyThread().start();
		}
	}
}

class MyThread extends Thread {
	static int j = 10;
	@Override
	public void run() {
		while (true) {
			synchronized (MyThread.class) {
				if(j>0) {
					j--;
					System.out.println(Thread.currentThread().getName() + "--" + j);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				else 
					break;
			}
		}
	}
}
```

运行结果：

```
Thread-0--9
Thread-4--8
Thread-3--7
Thread-2--6
Thread-2--5
Thread-2--4
Thread-2--3
Thread-2--2
Thread-2--1
Thread-2--0
```

如果每个线程执行的代码不同，这时候需要用不同的Runnable对象，有两种方式来实现这些Runnable对象之间的数据共享：

1. 将共享数据封装在另外一个对象中，然后将这个对象逐一传递给各个Runnable对象，每个线程对共享数据的操作方法也分配到那个对象身上去完成，这样容易实现针对给数据的各个操作的互斥和通信

```
public class MultiThread1 {
	public static void main(String[] args) {
		ShareData data = new ShareData();
		new Thread(new MyRunnable_1(data)).start();
		new Thread(new MyRunnable_2(data)).start();
	}
}
//不同的runnbale对象
class MyRunnable_1 implements Runnable{
	private ShareData data;
	public MyRunnable_1(ShareData data){
		this.data = data;
	}
	public void run() {
		while(true) {
			data.decrement();
		}
		
	}
}
class MyRunnable_2 implements Runnable{
	private ShareData data;
	public MyRunnable_2(ShareData data){
		this.data = data;
	}
	public void run() {
		while(true) {
			data.increment();
		}
	}
}
//共享数据
class ShareData{
	private int j = 0;
	public synchronized void increment(){
		j++;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
		System.out.println(Thread.currentThread().getName() + ":" + j);
	}
	public synchronized void decrement(){
		j--;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
		System.out.println(Thread.currentThread().getName() + ":" + j);
	}
}
```

运行结果：

```
Thread-0:-1
Thread-0:-2
Thread-0:-3
Thread-0:-4
Thread-0:-5
Thread-0:-6
Thread-0:-7
Thread-0:-8
Thread-0:-9
Thread-0:-10
Thread-0:-11
Thread-0:-12
Thread-0:-13
Thread-0:-14
Thread-0:-15
Thread-0:-16
Thread-0:-17
Thread-0:-18
Thread-0:-19
Thread-0:-20
Thread-0:-21
Thread-0:-22
Thread-0:-23
Thread-0:-24
Thread-0:-25
Thread-0:-26
Thread-1:-25
Thread-1:-24
Thread-1:-23
Thread-1:-22
Thread-1:-21
Thread-1:-20
Thread-1:-19
Thread-0:-20
Thread-0:-21
Thread-0:-22
Thread-1:-21
Thread-0:-22
Thread-1:-21
Thread-0:-22
Thread-0:-23
Thread-1:-22
......
```

2. 将这些Runnable对象作为一个类中的内部类，共享数据作为这个外部类的成员变量，每个线程对共享数据的操作方法也分配该外部类，以便实现对共享数据进行的各个操作的互斥和通信，作为内部类的各个Runnable对象调用外部类的这个方法

```
public class MultiThread2 {
	int j = 0;
	public synchronized void increment(){
		j++;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
		System.out.println(Thread.currentThread().getName() + ":" + j);
	}
	public synchronized void decrement(){
		j--;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
		System.out.println(Thread.currentThread().getName() + ":" + j);
	}
	public static void main(String[] args) {
		final MultiThread2 mh = new MultiThread2();
		new Thread(new Runnable(){
			@Override
			public void run() {
				while(true) {
					mh.decrement();
				}
				
			}
		}).start();
		new Thread(new Runnable(){
			@Override
			public void run() {
				while(true) {
					mh.increment();
				}
			}
		}).start();
	}
}
```

3. 上面两种方式的组合：将共享数据封装在另外一个对象中，每个线程对共享数据的操作方法也分配到那个对象去完成，对象作为这个外部类中的成员变量或方法中的局部变量，每个线程的Runnable对象作为外部类中的成员内部类或局部内部类

```
public class MultiThread1 {
	public static void main(String[] args) {
		final ShareData data = new ShareData();
		new Thread(new Runnable(){
			@Override
			public void run() {
				while(true) {
					data.decrement();
				}
			}
		}).start();
		new Thread(new Runnable(){
			@Override
			public void run() {
				while(true) {
					data.increment();
				}
			}
		}).start();
	}
}
//共享数据
class ShareData{
	private int j = 0;
	public synchronized void increment(){
		j++;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
		System.out.println(Thread.currentThread().getName() + ":" + j);
	}
	public synchronized void decrement(){
		j--;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
		System.out.println(Thread.currentThread().getName() + ":" + j);
	}
}
```

总之，要同步互斥的几段代码最好分别放在几个独立的方法中，这些方法再放在同一个类中，这样比较容易实现他们之间的同步互斥和通信。