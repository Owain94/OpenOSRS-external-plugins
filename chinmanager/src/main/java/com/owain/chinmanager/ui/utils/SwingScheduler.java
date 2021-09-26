package com.owain.chinmanager.ui.utils;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Singleton;
import javax.swing.Timer;
import org.jetbrains.annotations.Nullable;

@Singleton
public final class SwingScheduler extends Scheduler
{
	@Override
	public @NonNull Worker createWorker()
	{
		return new SwingWorker();
	}

	@Override
	public @NonNull Disposable scheduleDirect(Runnable run)
	{
		DirectTask dt = new DirectTask(run);
		EventQueue.invokeLater(dt);
		return dt;
	}

	@Override
	public @NonNull Disposable scheduleDirect(Runnable run, long delay, @NonNull TimeUnit unit)
	{
		DirectTimedTask dtt = new DirectTimedTask(
			run,
			(int) unit.toMillis(delay),
			(int) Math.max(0, unit.toMillis(delay)), false
		);
		dtt.start();
		return dtt;
	}

	@Override
	public @NonNull Disposable schedulePeriodicallyDirect(Runnable run, long initialDelay, long period, @NonNull TimeUnit unit)
	{
		DirectTimedTask dtt = new DirectTimedTask(
			run,
			(int) unit.toMillis(initialDelay),
			(int) Math.max(0, unit.toMillis(period)), true
		);
		dtt.start();
		return dtt;
	}

	static final class SwingWorker extends Worker
	{

		CompositeDisposable tasks;

		SwingWorker()
		{
			this.tasks = new CompositeDisposable();
		}

		@Override
		public void dispose()
		{
			tasks.dispose();
		}

		@Override
		public boolean isDisposed()
		{
			return tasks.isDisposed();
		}

		void remove(@NonNull Disposable d)
		{
			tasks.delete(d);
		}

		boolean add(@NonNull Disposable d)
		{
			return tasks.add(d);
		}

		@Override
		public @NonNull Disposable schedule(Runnable run)
		{
			WorkerTask wt = new WorkerTask(run);
			if (add(wt))
			{
				EventQueue.invokeLater(wt);
				return wt;
			}
			return EmptyDisposable.INSTANCE;
		}

		@Override
		public @NonNull Disposable schedule(Runnable run, long delay, @NonNull TimeUnit unit)
		{
			WorkerTimedTask wtt = new WorkerTimedTask(
				run,
				(int) unit.toMillis(delay),
				(int) Math.max(0, unit.toMillis(delay)), false
			);
			if (add(wtt))
			{
				wtt.start();
				return wtt;
			}
			return EmptyDisposable.INSTANCE;
		}

		@Override
		public @NonNull Disposable schedulePeriodically(Runnable run, long initialDelay, long period, @NonNull TimeUnit unit)
		{
			WorkerTimedTask wtt = new WorkerTimedTask(
				run,
				(int) unit.toMillis(initialDelay),
				(int) Math.max(0, unit.toMillis(period)), true
			);
			if (add(wtt))
			{
				wtt.start();
				return wtt;
			}
			return EmptyDisposable.INSTANCE;
		}

		final class WorkerTask extends AtomicReference<Runnable> implements Runnable, Disposable
		{
			private static final long serialVersionUID = 3954858753004137205L;

			WorkerTask(Runnable run)
			{
				lazySet(run);
			}

			@Override
			public void dispose()
			{
				if (getAndSet(null) != null)
				{
					remove(this);
				}
			}

			@Override
			public boolean isDisposed()
			{
				return get() == null;
			}

			@Override
			public void run()
			{
				Runnable r = getAndSet(null);
				if (r != null)
				{
					try
					{
						r.run();
					}
					catch (Throwable ex)
					{
						Exceptions.throwIfFatal(ex);
						RxJavaPlugins.onError(ex);
					}
					remove(this);
				}
			}
		}

		final class WorkerTimedTask extends Timer implements ActionListener, Disposable
		{
			private static final long serialVersionUID = 1146820542834025296L;

			final boolean periodic;

			@Nullable
			Runnable run;

			WorkerTimedTask(Runnable run, int initialDelayMillis, int periodMillis, boolean periodic)
			{
				super(0, null);
				this.run = run;
				this.periodic = periodic;
				setInitialDelay(initialDelayMillis);
				setDelay(periodMillis);
				addActionListener(this);
			}

			@Override
			public void dispose()
			{
				run = null;
				stop();
				remove(this);
			}

			@Override
			public boolean isDisposed()
			{
				return run == null;
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				Runnable r = run;
				if (r != null)
				{
					try
					{
						r.run();
					}
					catch (Throwable ex)
					{
						run = null;
						stop();
						remove(this);
						Exceptions.throwIfFatal(ex);
						RxJavaPlugins.onError(ex);
						return;
					}
					if (!periodic)
					{
						run = null;
						stop();
						remove(this);
					}
				}
			}
		}
	}

	static final class DirectTask extends AtomicReference<Runnable> implements Runnable, Disposable
	{
		private static final long serialVersionUID = -4645934389976373118L;

		DirectTask(Runnable run)
		{
			lazySet(run);
		}

		@Override
		public void run()
		{
			Runnable r = getAndSet(null);
			if (r != null)
			{
				try
				{
					r.run();
				}
				catch (Throwable ex)
				{
					Exceptions.throwIfFatal(ex);
					RxJavaPlugins.onError(ex);
				}
			}
		}

		@Override
		public void dispose()
		{
			getAndSet(null);
		}

		@Override
		public boolean isDisposed()
		{
			return get() == null;
		}
	}

	static final class DirectTimedTask extends Timer implements ActionListener, Disposable
	{
		private static final long serialVersionUID = 1146820542834025296L;

		final boolean periodic;

		@Nullable
		Runnable run;

		DirectTimedTask(Runnable run, int initialDelayMillis, int periodMillis, boolean periodic)
		{
			super(0, null);
			this.run = run;
			this.periodic = periodic;
			setInitialDelay(initialDelayMillis);
			setDelay(periodMillis);
			addActionListener(this);
		}

		@Override
		public void dispose()
		{
			run = null;
			stop();
		}

		@Override
		public boolean isDisposed()
		{
			return run == null;
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			Runnable r = run;
			if (r != null)
			{
				try
				{
					r.run();
				}
				catch (Throwable ex)
				{
					run = null;
					stop();
					Exceptions.throwIfFatal(ex);
					RxJavaPlugins.onError(ex);
					return;
				}
				if (!periodic)
				{
					run = null;
					stop();
				}
			}
		}
	}
}