package com.example.valeriyasin.authorization;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by valeriyasin on 12/13/16.
 */

public class AsyncTaskHandler {
    private static AsyncTaskHandler instance;

//    ReentrantLock locker;
//    Condition condition;

    private static AtomicBoolean asyncTaskStarted  = new AtomicBoolean(false);

    private static AuthorizationActivity authorizationActivity;

    private static Object authorizationActivityAlive  = new Object();


    private AsyncTaskHandler() {
    }

    public static synchronized AsyncTaskHandler getInstance() {
        if (instance == null) {
            instance = new AsyncTaskHandler();
        }
        return instance;
    }

    public void onError(String result) {
        while (authorizationActivity ==null) {
            synchronized (authorizationActivityAlive) {
                try {
//                locker.lock();
//                condition.await();
                    authorizationActivityAlive.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                } finally {
//                    locker.unlock();
//                }
            }
        }
        authorizationActivity.onError(result);
    }

    public void onComplete(TokenObject tokenObject) {
        while (authorizationActivity ==null) {
            synchronized (authorizationActivityAlive) {
                try {
//                locker.lock();
//                condition.await();
                    authorizationActivityAlive.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                } finally {
//                    locker.unlock();
//                }
            }
        }
        authorizationActivity.onComplete(tokenObject);
    }


    public void turnOnAuthorizationActivityAlive(AuthorizationActivity authorizationActivity) {
        this.authorizationActivity = authorizationActivity;
        synchronized (authorizationActivityAlive) {
            authorizationActivityAlive.notifyAll();
        }
//        condition.signalAll();
    }

//    public AtomicBoolean getAuthorizationActivityAlive() {
//        return this.authorizationActivityAlive;
//    }

    public AtomicBoolean getAsyncTaskStarted() {
        return asyncTaskStarted;
    }

    public void turnOnAsyncTaskStarted() {
        asyncTaskStarted.set(true);
    }
}
