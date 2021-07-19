package com.owain.chinmanager.cookies;

import com.owain.chinmanager.cookies.cache.CookieCache;
import com.owain.chinmanager.cookies.persistence.CookiePersistor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

public class PersistentCookieJar implements ClearableCookieJar
{
	private final CookieCache cache;
	private final CookiePersistor persistor;

	public PersistentCookieJar(CookieCache cache, CookiePersistor persistor)
	{
		this.cache = cache;
		this.persistor = persistor;

		this.cache.addAll(persistor.loadAll());
	}

	@Override
	synchronized public void saveFromResponse(HttpUrl url, List<Cookie> cookies)
	{
		cache.addAll(cookies);
		persistor.saveAll(filterPersistentCookies(cookies));
	}

	private static List<Cookie> filterPersistentCookies(List<Cookie> cookies)
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

	@Override
	synchronized public List<Cookie> loadForRequest(HttpUrl url)
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

	private static boolean isCookieExpired(Cookie cookie)
	{
		return cookie.expiresAt() < System.currentTimeMillis();
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
