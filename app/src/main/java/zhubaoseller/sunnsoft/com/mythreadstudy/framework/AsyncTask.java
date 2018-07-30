package zhubaoseller.sunnsoft.com.mythreadstudy.framework;

import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import java.util.concurrent.Executor;
import java.util.ArrayDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
//		当我们调用execute(Params… params)方法后，其内部直接调用executeOnExecutor方法，接着onPreExecute()被调用方法
//		执行异步任务的WorkerRunnable对象(实质为Callable对象)最终被封装成FutureTask实例，
//		FutureTask实例将由线程池sExecutor执行去执行，这个过程中doInBackground(Params… params)
//		将被调用（在WorkerRunnable对象的call方法中被调用），
//		如果我们覆写的doInBackground(Params… params)方法中调用了publishProgress(Progress… values)方法，
//		则通过InternalHandler实例sHandler发送一条MESSAGE_POST_PROGRESS消息，更新进度，
//		sHandler处理消息时onProgressUpdate(Progress… values)方法将被调用；
//		最后如果FutureTask任务执行成功并返回结果，则通过postResult方法发送一条MESSAGE_POST_RESULT的消息去执行AsyncTask的finish方法，
//		在finish方法内部onPostExecute(Result result)方法被调用，在onPostExecute方法中我们可以更新UI或者释放资源等。

public abstract class AsyncTask<Params, Progress, Result> {
	//线程池
	public static final Executor THREAD_POOL_EXECUTOR;
	/**
	 * An {@link Executor} that executes tasks one at a time in serial
	 * order.  This serialization is global to a particular process.
	 */
	public static final Executor SERIAL_EXECUTOR = new SerialExecutor();
	private static final String LOG_TAG = "AsyncTask";
	//CUP核数
	private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
	//核心线程数量
	private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
	//最大线程数量
	private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
	//非核心线程的存活时间1s
	private static final int KEEP_ALIVE_SECONDS = 30;
	//线程工厂类   Thread thread = sThreadFactory.newThread(r)
	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		public Thread newThread(Runnable r) {
			return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
		}
	};
	//线程队列，核心线程不够用时，任务会添加到该队列中，队列满后，会去调用非核心线程执行任务
	private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(128);
	private static final int MESSAGE_POST_RESULT = 0x1;
	private static final int MESSAGE_POST_PROGRESS = 0x2;
	private static volatile Executor sDefaultExecutor = SERIAL_EXECUTOR;
	private static InternalHandler sHandler;

	static {
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
				CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
				sPoolWorkQueue, sThreadFactory);
		threadPoolExecutor.allowCoreThreadTimeOut(true);
		THREAD_POOL_EXECUTOR = threadPoolExecutor;
	}

	private final WorkerRunnable<Params, Result> mWorker;
	private final FutureTask<Result> mFuture;
	private final AtomicBoolean mCancelled = new AtomicBoolean();
	private final AtomicBoolean mTaskInvoked = new AtomicBoolean();
	private final Handler mHandler;
	private volatile Status mStatus = Status.PENDING;

	/**
	 * 创建一个新的异步任务。必须在UI线程上调用此构造函数。
	 */
	public AsyncTask() {
		this((Looper) null);
	}

	/**
	 * 创建一个新的异步任务。必须在UI线程上调用此构造函数。
	 */
	public AsyncTask(@Nullable Handler handler) {
		this(handler != null ? handler.getLooper() : null);
	}

	/**
	 * 创建一个新的异步任务。必须在UI线程上调用此构造函数。
	 */
	public AsyncTask(@Nullable Looper callbackLooper) {
		//在初始化AsyncTask时，不仅创建了mWorker（本质实现了Callable接口的实例类）而且也创建了FutureTask对象，
		//并把mWorker对象封装在FutureTask对象中，最后FutureTask对象将在executeOnExecutor方法中通过线程池去执行。
		mHandler = callbackLooper == null || callbackLooper == Looper.getMainLooper()
				? getMainHandler()
				: new Handler(callbackLooper);
		//创建WorkerRunnable mWorker，本质上就是一个实现了Callable接口对象
		mWorker = new WorkerRunnable<Params, Result>() {
			public Result call() throws Exception {
				mTaskInvoked.set(true);//设置标志
				Result result = null;
				try {
//					Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
					//执行doInBackground，并传递mParams参数
					//这就是子线程了，可以在做一些耗时的任务，
					//可以调用publishProgress，
					result = doInBackground(mParams);
					Binder.flushPendingCommands();
				} catch (Throwable tr) {
					mCancelled.set(true);
					throw tr;
				} finally {
					postResult(result);
				}
				return result;
			}
		};
		//把mWorker（即Callable实现类）封装成FutureTask实例
		//最终执行结果也就封装在FutureTask中
		mFuture = new FutureTask<Result>(mWorker) {
			//任务执行完成后被调用
			@Override
			protected void done() {
				try {
					//如果还没更新结果通知就执行postResultIfNotInvoked
					//通过postResult 来通知任务已经执行完成
					postResultIfNotInvoked(get());
				} catch (InterruptedException e) {
					android.util.Log.w(LOG_TAG, e);
				} catch (ExecutionException e) {
					throw new RuntimeException("An error occurred while executing doInBackground()", e.getCause());
				} catch (CancellationException e) {
					postResultIfNotInvoked(null);
				}
			}
		};
	}

	private static Handler getMainHandler() {
		synchronized (AsyncTask.class) {
			if (sHandler == null) {
				sHandler = new InternalHandler(Looper.getMainLooper());
			}
			return sHandler;
		}
	}

	public static void setDefaultExecutor(Executor exec) {
		sDefaultExecutor = exec;
	}

	@MainThread
	public static void execute(Runnable runnable) {
		sDefaultExecutor.execute(runnable);
	}

	private Handler getHandler() {
		return mHandler;
	}

	private void postResultIfNotInvoked(Result result) {
		final boolean wasTaskInvoked = mTaskInvoked.get();
		if (!wasTaskInvoked) {
			postResult(result);
		}
	}

	private Result postResult(Result result) {
		//通过Handler去执行结果更新的，在执行结果成返回后，会把result封装到一个AsyncTaskResult对象中，
		//最后把MESSAGE_POST_RESULT标示和AsyncTaskResult存放到Message中并发送给Handler去处理
		@SuppressWarnings("unchecked")
		Message message = getHandler().obtainMessage(MESSAGE_POST_RESULT, new AsyncTaskResult<Result>(this, result));
		message.sendToTarget();
		return result;
	}

	public final Status getStatus() {
		return mStatus;
	}

	@WorkerThread
	protected abstract Result doInBackground(Params... params);

	@MainThread
	protected void onPreExecute() {
	}

	@SuppressWarnings({"UnusedDeclaration"})
	@MainThread
	protected void onPostExecute(Result result) {
	}

	@SuppressWarnings({"UnusedDeclaration"})
	@MainThread
	protected void onProgressUpdate(Progress... values) {
	}

	@SuppressWarnings({"UnusedParameters"})
	@MainThread
	protected void onCancelled(Result result) {
		onCancelled();
	}

	@MainThread
	protected void onCancelled() {
	}

	public final boolean isCancelled() {
		return mCancelled.get();
	}

	public final boolean cancel(boolean mayInterruptIfRunning) {
		mCancelled.set(true);
		return mFuture.cancel(mayInterruptIfRunning);
	}

	public final Result get() throws InterruptedException, ExecutionException {
		return mFuture.get();
	}

	public final Result get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		return mFuture.get(timeout, unit);
	}

	@MainThread
	public final AsyncTask<Params, Progress, Result> execute(Params... params) {
		return executeOnExecutor(sDefaultExecutor, params);
	}

	/**
	 * 执行任务前先会去判断当前AsyncTask的状态，如果处于RUNNING和FINISHED状态就不可再执行，
	 * 直接抛出异常，只有处于Status.PENDING时，AsyncTask才会去执行。然后onPreExecute()被执行的，
	 * 该方法可以用于线程开始前做一些准备工作。接着会把我们传递进来的参数赋值给 mWorker.mParams ，
	 * 并执行开始执行mFuture任务
	 */
	@MainThread
	public final AsyncTask<Params, Progress, Result> executeOnExecutor(Executor exec, Params... params) {
		//判断在那种状态
		if (mStatus != Status.PENDING) {
			switch (mStatus) {
				case RUNNING:
					throw new IllegalStateException("Cannot execute task:" + " the task is already running.");
				case FINISHED://只能执行一次！
					throw new IllegalStateException("Cannot execute task:"
							+ " the task has already been executed " + "(a task can be executed only once)");
			}
		}
		mStatus = Status.RUNNING;
		//onPreExecute()在此执行了！！！
		onPreExecute();
		//参数传递给了mWorker.mParams
		mWorker.mParams = params;
		//执行mFuture任务，其中exec就是传递进来的sDefaultExecutor
		//把mFuture交给线程池去执行任务
		exec.execute(mFuture);
		return this;
	}

	@WorkerThread
	protected final void publishProgress(Progress... values) {
		if (!isCancelled()) {
			//送MESSAGE_POST_PROGRESS，通知更新进度条
			getHandler().obtainMessage(MESSAGE_POST_PROGRESS,
					new AsyncTaskResult<Progress>(this, values)).sendToTarget();
		}
	}

	//该方法先判断任务是否被取消，如果没有被取消则去执行onPostExecute(result)方法，
	//外部通过onPostExecute方法去更新相关信息，如UI，消息通知等。
	//最后更改AsyncTask的状态为已完成。到此AsyncTask的全部流程执行完。
	private void finish(Result result) {
		//判断任务是否被取消
		if (isCancelled()) {
			onCancelled(result);
		} else {
			//执行onPostExecute(result)并传递result结果
			onPostExecute(result);
		}
		//更改AsyncTask的状态为已完成
		mStatus = Status.FINISHED;
	}

	public enum Status {
		/**
		 * 指示任务尚未执行。
		 */
		PENDING,
		/**
		 * 指示任务正在运行。
		 */
		RUNNING,
		/**
		 * 指示已完成。
		 */
		FINISHED,
	}

	private static class SerialExecutor implements Executor {
		final ArrayDeque<Runnable> mTasks = new ArrayDeque<Runnable>();
		Runnable mActive;

		public synchronized void execute(final Runnable r) {
			mTasks.offer(new Runnable() {
				public void run() {
					try {
						r.run();
					} finally {
						scheduleNext();
					}
				}
			});
			if (mActive == null) {
				scheduleNext();
			}
		}

		protected synchronized void scheduleNext() {
			if ((mActive = mTasks.poll()) != null) {
				THREAD_POOL_EXECUTOR.execute(mActive);
			}
		}
	}

	//handler绑定的线程为主线线程，这也就是为什么AsyncTask必须在主线程创建并执行的原因了。
	//接着通过handler发送过来的不同标志去决定执行那种结果，
	//如果标示为MESSAGE_POST_RESULT则执行AsyncTask的finish方法并传递执行结果给该方法
	private static class InternalHandler extends Handler {
		public InternalHandler(Looper looper) {
			//获取主线程的Looper传递给当前Handler，
			//这也是为什么AsyncTask只能在主线程创建并执行的原因
			super(looper);
		}

		@SuppressWarnings({"unchecked", "RawUseOfParameterizedType"})
		@Override
		public void handleMessage(Message msg) {
			//获取AsyncTaskResult
			AsyncTaskResult<?> result = (AsyncTaskResult<?>) msg.obj;
			switch (msg.what) {
				//执行完成
				case MESSAGE_POST_RESULT:
					// There is only one result
					result.mTask.finish(result.mData[0]);
					break;
				//更新进度条的标志
				case MESSAGE_POST_PROGRESS:
					//执行onProgressUpdate方法，自己实现。
					result.mTask.onProgressUpdate(result.mData);
					break;
			}
		}
	}

	//抽象类
	private static abstract class WorkerRunnable<Params, Result> implements Callable<Result> {
		/**
		 * WorkerRunnable抽象类实现了Callable接口，因此WorkerRunnable本质上也算一个Callable对象，
		 * 其内部还封装了一个mParams的数组参数，因此我们在外部执行execute方法时传递的可变参数
		 * 最终会赋值给WorkerRunnable的内部数组mParams，这些参数最后会传递给doInBackground方法处理，
		 * 这时我们发现doInBackground方法也是在WorkerRunnable的call方法中被调用的
		 */
		Params[] mParams;
	}

	@SuppressWarnings({"RawUseOfParameterizedType"})
	private static class AsyncTaskResult<Data> {
		//封装了执行结果的数组以及AsyncTask本身
		final AsyncTask mTask;
		final Data[] mData;

		AsyncTaskResult(AsyncTask task, Data... data) {
			mTask = task;
			mData = data;
		}
	}
}

