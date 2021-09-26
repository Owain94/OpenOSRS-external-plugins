package com.owain.chinmanager.cookies;

import com.owain.chinmanager.cookies.cache.CookieCache;
import com.owain.chinmanager.cookies.persistence.CookiePersistor;
import io.reactivex.rxjava3.annotations.NonNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

public class PersistentCookieJar implements ClearableCookieJar
{
	private final CookieCache cache;
	private final @NonNull CookiePersistor persistor;

	public PersistentCookieJar(CookieCache cache, @NonNull CookiePersistor persistor)
	{
		this.cache = cache;
		this.persistor = persistor;

		this.cache.addAll(persistor.loadAll());
	}

	private static @NonNull List<Cookie> filterPersistentCookies(@NonNull List<Cookie> cookies)
	{
		List<Cookie> persistentCookies = new ArrayList<>();

		for (Cookie cookie : cookies)
		{
			if (cookie.persistent())
			{
				persistentCookies.add(cookie);
			}
		}
		return persistentCookies;
	}

	private static boolean isCookieExpired(@NonNull Cookie cookie)
	{
		return cookie.expiresAt() < System.currentTimeMillis();
	}

	@Override
	synchronized public @NonNull List<Cookie> loadForRequest(@NonNull HttpUrl url)
	{
		List<Cookie> validCookies = new ArrayList<>();

		for (Iterator<Cookie> it = cache.iterator(); it.hasNext(); )
		{
			Cookie currentCookie = it.next();

			if (isCookieExpired(currentCookie))
			{
				it.remove();
			}
			else if (currentCookie.matches(url))
			{
				validCookies.add(currentCookie);
			}
		}

		persistor.saveAll(validCookies);

		return validCookies;
	}

	@Override
	synchronized public void saveFromResponse(HttpUrl url, @NonNull List<Cookie> cookies)
	{
		cache.addAll(cookies);
		persistor.saveAll(filterPersistentCookies(cookies));
	}

	@Override
	synchronized public void clearSession()
	{
		cache.clear();
		cache.addAll(persistor.loadAll());
	}

	@Override
	synchronized public void clear()
	{
		cache.clear();
		persistor.clear();
	}
}
