package com.owain.chinmanager.cookies.cache;

import io.reactivex.rxjava3.annotations.NonNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.inject.Singleton;
import okhttp3.Cookie;

@Singleton
public class SetCookieCache implements CookieCache
{
	private final @NonNull Set<IdentifiableCookie> cookies;

	public SetCookieCache()
	{
		cookies = new HashSet<>();
	}

	@Override
	public void addAll(@NonNull Collection<Cookie> newCookies)
	{
		for (IdentifiableCookie cookie : IdentifiableCookie.decorateAll(newCookies))
		{
			this.cookies.remove(cookie);
			this.cookies.add(cookie);
		}
	}

	@Override
	public void clear()
	{
		cookies.clear();
	}

	@Override
	public @NonNull Iterator<Cookie> iterator()
	{
		return new SetCookieCacheIterator();
	}

	private class SetCookieCacheIterator implements Iterator<Cookie>
	{
		private final @NonNull Iterator<IdentifiableCookie> iterator;

		public SetCookieCacheIterator()
		{
			iterator = cookies.iterator();
		}

		@Override
		public boolean hasNext()
		{
			return iterator.hasNext();
		}

		@Override
		public Cookie next()
		{
			return iterator.next().getCookie();
		}

		@Override
		public void remove()
		{
			iterator.remove();
		}
	}
}
