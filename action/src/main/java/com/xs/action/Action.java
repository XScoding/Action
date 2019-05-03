package com.xs.action;

/**
 * Created by xs on 2019/2/20.
 */

public abstract class Action<T, C> {

    /**
     * action thread type (default main)
     */
    private int actionThread = Scheduler.MAIN;

    /**
     * execute thread type (default main)
     */
    private int executeThread = Scheduler.MAIN;

    /**
     * cancel flag
     */
    private boolean cancel = false;

    /**
     * father action
     */
    private Action pre;

    /**
     * child action
     */
    private Action next;

    /**
     * params
     */
    protected T t;

    /**
     * constuctor
     *
     * @param t
     */
    public Action(T t) {
        this.t = t;
    }

    /**
     * constuctor
     *
     * @param action
     */
    private Action(Action action) {
        this.pre = action;
    }

    /**
     * set params
     *
     * @param t
     */
    private void setT(T t) {
        this.t = t;
    }

    /**
     * set pre action
     *
     * @param action
     */
    private void setPre(Action action) {
        this.pre = action;
    }

    /**
     * clear father action
     */
    private void clearPre() {
        pre = null;
    }

    /**
     * clear child action
     */
    private void clearNext() {
        next = null;
    }

    /**
     * run action
     *
     * @param t
     * @param call
     */
    public abstract void act(T t, Call<C> call);

    /**
     * create simple action
     *
     * @param a
     * @param <A>
     * @return
     */
    public static <A> Action<A, A> create(A a) {
        return new Action<A, A>(a) {
            @Override
            public void act(A a, Call<A> call) {
                call.success(a);
            }
        };
    }

    /**
     * change action run thread type
     *
     * @param thread
     * @return
     */
    public Action<T, C> onAction(int thread) {
        this.actionThread = thread;
        return this;
    }

    /**
     * change execute run thread type
     *
     * @param thread
     * @return
     */
    public Action<T, C> onExecute(int thread) {
        this.executeThread = thread;
        return this;
    }

    /**
     * action to action
     * <p>
     * execute code...
     *
     * @param func
     * @param <E>
     * @return
     */
    public <E> Action<C, E> map(final Func<C, E> func) {
        next = new Action<C, E>(this) {
            @Override
            public void act(C c, Call<E> call) {
                call.success(func.func(c));
            }
        };
        return next;
    }

    /**
     * action to action
     *
     * @param func
     * @param <E>
     * @param <F>
     * @return
     */
    public <E, F> Action<C, F> flatMap(final Func<C, Action<E, F>> func) {
        next = new Action<C, F>(this) {
            @Override
            public void act(C c, Call<F> call) {
                Action<E, F> action = func.func(c);
                action.act(action.t, call);
            }
        };
        return next;
    }

    /**
     * start run action
     *
     * @param call
     * @return
     */
    public Execute<C> exec(Call<C> call) {
        Execute<C> execute = new Execute(call, this, executeThread);
        execute.exec();
        return execute;
    }

    /**
     * stop action
     */
    protected void cancel() {
        cancel = true;
        if (pre != null) {
            pre.cancel();
        }
    }

    /**
     * launch action
     *
     * @param call
     */
    private void launch(final Call<C> call) {
        if (pre != null) {
            pre.launch(call);
        } else {
            if (cancel) {
                return;
            }
            Scheduler.INSTANCE.execute(actionThread, new Runnable() {
                @Override
                public void run() {
                    try {
                        act(t, new Call<C>() {
                            @Override
                            public void success(C c) {
                                try {
                                    if (next != null) {
                                        next.clearPre();
                                        next.setT(c);
                                        next.launch(call);
                                        clearNext();
                                    } else {
                                        call.success(c);
                                    }
                                } catch (Exception e) {
                                    call.fail(e);
                                }
                            }

                            @Override
                            public void fail(Exception e) {
                                call.fail(e);
                            }
                        });
                    } catch (Exception e) {
                        call.fail(e);
                    }
                }
            });
        }
    }

    public static class Execute<C> {

        /**
         * call
         */
        private Call call;

        /**
         * action
         */
        private Action action;

        /**
         * run thread type
         */
        private int executeThread;

        /**
         * constructor
         *
         * @param call
         * @param action
         * @param thread
         */
        private Execute(Call<C> call, Action action, int thread) {
            this.call = call;
            this.action = action;
            this.executeThread = thread;
        }

        /**
         * exec
         * <p>
         * start launch action
         */
        private void exec() {
            this.action.launch(new Call<C>() {
                @Override
                public void success(C c) {
                    successE(c);
                }

                @Override
                public void fail(Exception e) {
                    failE(e);
                }
            });
        }

        /**
         * call success
         *
         * @param c
         */
        private void successE(final C c) {
            if (call != null) {
                Scheduler.INSTANCE.execute(executeThread, new Runnable() {
                    @Override
                    public void run() {
                        call.success(c);
                    }
                });

            }
        }

        /**
         * call fail
         *
         * @param e
         */
        private void failE(final Exception e) {
            if (call != null) {
                Scheduler.INSTANCE.execute(executeThread, new Runnable() {
                    @Override
                    public void run() {
                        call.fail(e);
                    }
                });
            }
        }

        /**
         * stop action
         */
        public void cancel() {
            if (action != null) {
                action.cancel();
                action = null;
            }
            call = null;
        }

    }
}
