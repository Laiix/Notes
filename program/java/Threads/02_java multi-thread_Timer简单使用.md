[TOC]

# Timer 定时器类

```
new Timer().schedule(new TimerTask() {
	@override
	public void run() {

	}
}, time1, time2);
```

只保留time1参数代表什么时候启动，保留time1和time2代表time1启动后，每隔time2后再次启动

# 使用实例

#### 代码片段一

```
class MyTimerTask extends TimerTask{
	@Override
	public void run() {
		System.out.println("bombing!");
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				System.out.println("bombing!");
			}
		},2000);
	}
}
```

#### 代码片段二

```
class MyTimerTask extends TimerTask{
	@Override
	public void run() {
		System.out.println("bombing!");
		new Timer().schedule(new MyTimerTask(),2000);
	}
}
```

启动上面两端代码：

```
new Timer().schedule(new MyTimerTask(), 2000);
```

注意这两段代码的逻辑，一现象是启动两次调度，间隔两秒，二现象是不停的启动定时器，间隔两秒
二会不停的启动在于，启动定时器之后，这个定时器会创建一个新的调度，新的调度又包含一个创建定时器的代码，而一中则没有。