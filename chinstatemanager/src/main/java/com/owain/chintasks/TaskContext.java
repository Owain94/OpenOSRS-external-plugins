package com.owain.chintasks;

import io.reactivex.rxjava3.core.ObservableEmitter;
import net.runelite.client.callback.ClientThread;

class TaskContext<R> implements Comparable<TaskContext<?>>
{
	private final ClientThread clientThread;

	private final Task<R> task;

	private final ObservableEmitter<R> emitter;

	private final int priority;

	public TaskContext(ClientThread clientThread, Task<R> task, ObservableEmitter<R> emitter, int priority)
	{
		this.clientThread = clientThread;
		this.task = task;
		this.emitter = emitter;
		this.priority = priority;
	}

	@Override
	public int compareTo(TaskContext<?> o)
	{
		return Integer.compare(priority, o.priority);
	}

	public void run()
	{
		clientThread.invoke(this::routine);
	}

	private void routine()
	{
		try
		{
			task.routine(emitter);

			emitter.onComplete();
		}
		catch (Exception e)
		{
			emitter.onError(e);
		}
	}
}
