### Action（simple rxjava）
  
使用rxjava之后觉得非常好用，逻辑处理和切换线程变的so eazy，开发App当然没有什么问题，
但开发sdk时rxjava太大了。为了解决这个问题，开始有想法实现一个简单的库，空闲的时候把之
前的想法完善一下，Action就蛋生了。

#### 使用
```
public class MainActivity extends AppCompatActivity {


    private Action.Execute<Integer> exec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button action = (Button) findViewById(R.id.action);
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testAction();
            }
        });
    }

    private void testAction() {
        exec = new Action<String, Boolean>("test start") {
            @Override
            public void act(String s, Call<Boolean> call) {
                showLog(s);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
                call.success(true);
            }
        }
                .onAction(Scheduler.IO)
                .map(new Func<Boolean, First>() {
                    @Override
                    public First func(Boolean aBoolean) {
                        showLog(aBoolean.toString());
                        return new First("first");
                    }
                })
                .onAction(Scheduler.MAIN)
                .flatMap(new Func<First, Action<Second, Third>>() {
                    @Override
                    public Action<Second, Third> func(First first) {
                        showLog(first.toString());
                        return getAction();
                    }
                })
                .onAction(Scheduler.POOL)
                .map(new Func<Third, String>() {
                    @Override
                    public String func(Third third) {
                        showLog(third.toString());
                        return "test map";
                    }
                })
                .onAction(Scheduler.IO)
                .flatMap(new Func<String, Action<Boolean, Integer>>() {
                    @Override
                    public Action<Boolean, Integer> func(String s) {
                        showLog(s);
                        return new Action<Boolean, Integer>(true) {
                            @Override
                            public void act(Boolean aBoolean, Call<Integer> call) {
                                showLog(aBoolean.toString());
                                call.success(123456);
                            }
                        };
                    }
                })
                .onAction(Scheduler.MAIN)
                .flatMap(new Func<Integer, Action<Integer,Integer>>() {
                    @Override
                    public Action<Integer, Integer> func(Integer integer) {
                        showLog(integer.toString());
                        return Action.create(67890);
                    }
                })
                .onExecute(Scheduler.MAIN)
                .exec(new Call<Integer>() {
                    @Override
                    public void success(Integer integer) {
                        showLog(integer.toString());
                    }

                    @Override
                    public void fail(Exception e) {
                        showLog(e.getMessage());
                    }
                });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exec.cancel();
    }

    @NonNull
    private Action<Second, Third> getAction() {
        return new Action<Second, Third>(new Second(111)) {
            @Override
            public void act(Second second, Call<Third> call) {
                showLog(second.toString());
                call.success(new Third(false));
            }
        };
    }

    private void showLog(String msg) {

        Log.w("xssss", "current time:" + System.currentTimeMillis()
                + "  current thread:" + Thread.currentThread().getName()
                + "  current msg:" + msg);
    }
}

```
结果
```
02-20 14:30:22.590 5804-5835/com.xs.action W/xssss: current time:1550644222590  current thread:action_io  current msg:aaa
02-20 14:30:23.594 5804-5804/com.xs.action W/xssss: current time:1550644223594  current thread:main  current msg:true
02-20 14:30:23.602 5804-5837/com.xs.action W/xssss: current time:1550644223602  current thread:pool-1-thread-1  current msg:First{name='first'}
02-20 14:30:23.603 5804-5837/com.xs.action W/xssss: current time:1550644223603  current thread:pool-1-thread-1  current msg:Second{age=111}
02-20 14:30:23.604 5804-5835/com.xs.action W/xssss: current time:1550644223604  current thread:action_io  current msg:Third{flag=false}
02-20 14:30:23.605 5804-5804/com.xs.action W/xssss: current time:1550644223605  current thread:main  current msg:test map
02-20 14:30:23.605 5804-5804/com.xs.action W/xssss: current time:1550644223605  current thread:main  current msg:true
02-20 14:30:23.605 5804-5804/com.xs.action W/xssss: current time:1550644223606  current thread:main  current msg:123456
02-20 14:30:23.605 5804-5804/com.xs.action W/xssss: current time:1550644223606  current thread:main  current msg:67890
```

#### 介绍

Action是一个简版的rxjava库，只实现了最简单实用的api，能满足大部分使用场景。

* com.xs.action.Action
* com.xs.action.Call
* com.xs.action.Func
* com.xs.action.Scheduler
* com.xs.action.ThreadPool


###### Action
核心类，双向链表结构，递归调用核心方法 **act(T t, Call<C> call)**。

* Action创建两种方式
```
    new Action<String, Boolean>("入参") {
        @Override
        public void act(String s, Call<Boolean> call) {
            // s : 入参数据
            // 逻辑处理
            // success : 继续流程成功回调
            // fail : 中断流程失败回调
            // 如果没有加调，中断流程
            call.success(true);
            //call.fail(new Exception("error"));
        }
    };
```

```
入参就是成功回调参数
 Action.create("入参");
```

* Action转换
```
    .map(new Func<String, Boolean>() {
            @Override
            public Boolean func(String s) {
                //逻辑处理
                return true;
            }
        })
```
```
     .flatMap(new Func<String, Action<Boolean, Integer>>() {
            @Override
            public Action<Boolean, Integer> func(String s) {
                //逻辑处理
                return new Action<Boolean, Integer>(true) {
                    @Override
                    public void act(Boolean aBoolean, Call<Integer> call) {
                        call.success(123)
                    }
                };
            }
        });
```

* Action执行
```
    Action.Execute<String> exec = Action.create("test")
            .exec(new Call<String>() {
                @Override
                public void success(String s) {
                    //结果
                }

                @Override
                public void fail(Exception e) {
                    //失败
                }
            });
    //中断流程，在onDestroy中使用
    exec.cancel();
```
* Action切换线程

onAction指定上一个Action的act()执行线程；onExecute指定exec（）执行线程，
且只在exec（）方法前指定线程生效；默认线程都是在主线程中，如果不指定线程就在主线程中执行。
```
    new Action<String, Boolean>("teset") {
        @Override
        public void act(String s, Call<Boolean> call) {
            // 逻辑处理
            call.success(true);
            //
            call.fail(new Exception("error"));
        }
    }
            .onAction(Scheduler.IO)
            .onExecute(Scheduler.MAIN)
            .exec(new Call<Boolean>() {
                @Override
                public void success(Boolean aBoolean) {
                    
                }

                @Override
                public void fail(Exception e) {

                }
            });
```
###### Call
Call回调
###### Func
Action转换
###### Scheduler
线程切换管理，主线程(MAIN)、io线程(IO)、线程池(POOL)。
###### ThreadPool
线程池实现
