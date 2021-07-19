package com.owain.chintasks;

import io.reactivex.rxjava3.core.ObservableEmitter;

public interface Task<R>
{
	int PRIORITY_LOWEST = Integer.MAX_VALUE;

	int PRIORITY_NORMAL = 0;

	int PRIORITY_HIGHEST = Integer.MIN_VALUE;

	void routine(ObservableEmitter<R> emitter) throws Exception;
}
