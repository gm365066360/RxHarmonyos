package com.example.myapplication.net.rxhm;

import com.example.myapplication.utils.Log;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.plugins.RxJavaPlugins;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.InnerEvent;

import java.util.concurrent.TimeUnit;

final class HandlerScheduler extends Scheduler {
    private final EventHandler handler;

    HandlerScheduler(EventHandler handler) {
        this.handler = handler;
    }

    @Override
    public Disposable scheduleDirect(Runnable run, long delay, TimeUnit unit) {
        if (run == null) throw new NullPointerException("run == null");
        if (unit == null) throw new NullPointerException("unit == null");

        run = RxJavaPlugins.onSchedule(run);
        ScheduledRunnable scheduled = new ScheduledRunnable(handler, run);
        Log.d("Scheduler=scheduleDirect");
        handler.postTask(scheduled, unit.toMillis(delay));
        return scheduled;
    }

    private   static  int InnerEventID =9819;
    @Override
    public Worker createWorker() {
        return new HandlerWorker(handler);
    }

    private static final class HandlerWorker extends Worker {
        private final EventHandler handler;

        private volatile boolean disposed;

        HandlerWorker(EventHandler handler) {
            this.handler = handler;
        }

        @Override
        public Disposable schedule(Runnable run, long delay, TimeUnit unit) {
            if (run == null) throw new NullPointerException("run == null");
            if (unit == null) throw new NullPointerException("unit == null");

            Log.d("Scheduler=1");
            if (disposed) {
                return Disposables.disposed();
            }

            run = RxJavaPlugins.onSchedule(run);

            ScheduledRunnable scheduled = new ScheduledRunnable(handler, run);

            InnerEvent event1 = InnerEvent.get(InnerEventID, 0, null);

//            Message message = Message.obtain(handler, scheduled);
//            message.obj = this; // Used as token for batch disposal of this worker's runnables.

            Log.d("Scheduler=2="+ unit.toMillis(delay));
//            handler.sendEvent(event1, unit.toMillis(delay));
            //
            handler.postTask(scheduled,unit.toMillis(delay));

            // Re-check disposed state for removing in case we were racing a call to dispose().
            if (disposed) {
                Log.d("Scheduler=3");
                //不能remove Runnable
                handler.removeAllEvent( );
//                handler.removeCallbacks(scheduled);
                return Disposables.disposed();
            }

            return scheduled;
        }

        @Override
        public void dispose() {
            disposed = true;
            Log.d("Scheduler=dispose");

            //不能remove token
            handler.removeAllEvent( );
//            handler.removeCallbacksAndMessages(this /* token */);
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }

    private static final class ScheduledRunnable implements Runnable, Disposable {
        private final EventHandler handler;
        private final Runnable delegate;

        private volatile boolean disposed;

        ScheduledRunnable(EventHandler handler, Runnable delegate) {
            this.handler = handler;
            this.delegate = delegate;
        }

        @Override
        public void run() {
            try {
                Log.d("Scheduler=delegate.run");
                delegate.run();
            } catch (Throwable t) {
                RxJavaPlugins.onError(t);
            }
        }

        @Override
        public void dispose() {
            disposed = true;
            Log.d("Scheduler=delegate.dispose");
            //不能remove Runnable
            handler.removeAllEvent( );
//            handler.removeCallbacks(this);
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }
}
