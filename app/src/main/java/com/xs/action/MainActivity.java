package com.xs.action;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.xs.action.bean.First;
import com.xs.action.bean.Second;
import com.xs.action.bean.Third;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

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
