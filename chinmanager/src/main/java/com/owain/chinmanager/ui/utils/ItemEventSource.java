/**
 * Copyright 2015 Netflix, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.owain.chinmanager.ui.utils;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Scheduler;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public enum ItemEventSource
{
	; // no instances

	public static Observable<ItemEvent> fromItemEventsOf(final ItemSelectable itemSelectable, final Scheduler scheduler)
	{
		return Observable.create((ObservableOnSubscribe<ItemEvent>) subscriber -> {
				final ItemListener listener = subscriber::onNext;
				itemSelectable.addItemListener(listener);
				subscriber.setCancellable(() -> itemSelectable.removeItemListener(listener));
			}).subscribeOn(scheduler)
			.unsubscribeOn(scheduler);
	}
}