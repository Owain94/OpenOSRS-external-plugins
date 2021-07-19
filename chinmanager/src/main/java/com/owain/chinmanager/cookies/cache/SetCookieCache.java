package com.owain.chinmanager.cookies.cache;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.inject.Singleton;
import okhttp3.Cookie;

@Singleton
public class SetCookieCache implements CookieCache
{
	private final Set<IdentifiableCookie> cookies;

	public SetCookieCache()
	{
		cookies = new HashSet<>();
	}

	@Override
	public void addAll(Collection<Cookie> newCookies)
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
	public Iterator<Cookie> iterator()
	{
		return new SetCookieCacheIterator();
	}

	private class SetCookieCacheIterator implements Iterator<Cookie>
	{
		private final Iterator<IdentifiableCookie> iterator;

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
