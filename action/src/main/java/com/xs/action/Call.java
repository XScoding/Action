package com.xs.action;

/**
 * Created by xs on 2019/2/20.
 */

public interface Call<C> {

    /**
     * success
     *
     * @param c
     */
    void success(C c);

    /**
     * fail
     *
     * @param e
     */
    void fail(Exception e);
}
