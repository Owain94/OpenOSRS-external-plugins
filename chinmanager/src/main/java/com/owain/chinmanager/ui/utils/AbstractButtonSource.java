package com.owain.chinmanager.ui.utils;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Scheduler;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;

public enum AbstractButtonSource
{
	; // no instances

	public static Observable<ActionEvent> fromActionOf(final AbstractButton button, final Scheduler scheduler)
	{
		return Observable.create((ObservableOnSubscribe<ActionEvent>) subscriber -> {
				final ActionListener listener = subscriber::onNext;
				button.addActionListener(listener);
				subscriber.setCancellable(() -> button.removeActionListener(listener));
			}).subscribeOn(scheduler)
			.unsubscribeOn(scheduler);
	}
}