package com.owain.chintasks;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import javax.inject.Inject;
import lombok.Getter;
import net.runelite.client.callback.ClientThread;

public class TaskExecutor
{
	@Inject
	private ClientThread clientThread;

	private static final AtomicLong counter = new AtomicLong(1);

	private final String threadName = this.getClass().getName() + "-" + counter.getAndIncrement();

	private Thread thread;

	@Getter
	private boolean running = false;

	private final PriorityBlockingQueue<TaskContext<?>> queue = new PriorityBlockingQueue<>();

	public final synchronized void start()
	{
		if (running)
		{
			throw new IllegalStateException("Execution was already started");
		}
		running = true;

		thread = new Thread(() ->
		{
			try
			{
				while (!thread.isInterrupted())
				{
					queue.take().run();
				}
			}
			catch (InterruptedException ignored)
			{
			}
		});

		thread.setName(threadName);
		thread.start();
	}

	public final synchronized void stop()
	{
		if (running)
		{
			running = false;

			try
			{
				thread.interrupt();
				thread.join();
			}
			catch (InterruptedException ignored)
			{
			}
		}
	}

	public <R> Observable<R> prepareTask(Task<R> task, int priority)
	{
		return Observable
			.<R>create(emitter -> queue.add(new TaskContext<>(clientThread, task, emitter, priority)))
			.observeOn(Schedulers.computation());
	}

	public <R> Observable<R> prepareTask(Task<R> task)
	{
		return prepareTask(task, Task.PRIORITY_NORMAL);
	}
}
