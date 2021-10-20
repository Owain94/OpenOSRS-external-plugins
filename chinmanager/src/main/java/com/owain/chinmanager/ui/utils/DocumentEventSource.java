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
import javax.swing.event.ChangeEvent;
import javax.swing.text.Document;


public enum DocumentEventSource
{
	; // no instances

	public static Observable<ChangeEvent> fromDocumentEventsOf(final Document document, final Scheduler scheduler)
	{
		return Observable.create((ObservableOnSubscribe<ChangeEvent>) subscriber -> {
				final DeferredDocumentChangedListener listener = new DeferredDocumentChangedListener();
				listener.addChangeListener(subscriber::onNext);

				document.addDocumentListener(listener);
				subscriber.setCancellable(() -> document.removeDocumentListener(listener));
			}).subscribeOn(scheduler)
			.unsubscribeOn(scheduler);
	}
}