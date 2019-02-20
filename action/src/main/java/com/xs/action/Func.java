package com.xs.action;

/**
 * Created by xs on 2019/2/20.
 */

public interface Func<C,E> {

    /**
     * change action
     *
     * execute code...
     * @param c
     */
    E func(C c);
}
