package com.owain.chinmanager.cookies.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Singleton;
import okhttp3.Cookie;

@Singleton
class IdentifiableCookie
{
	private final Cookie cookie;

	IdentifiableCookie(Cookie cookie)
	{
		this.cookie = cookie;
	}

	static List<IdentifiableCookie> decorateAll(Collection<Cookie> cookies)
	{
		List<IdentifiableCookie> identifiableCookies = new ArrayList<>(cookies.size());

		for (Cookie cookie : cookies)
		{
			identifiableCookies.add(new IdentifiableCookie(cookie));
		}

		return identifiableCookies;
	}

	Cookie getCookie()
	{
		return cookie;
	}

	@Override
	public int hashCode()
	{
		int hash = 17;

		hash = 31 * hash + cookie.name().hashCode();
		hash = 31 * hash + cookie.domain().hashCode();
		hash = 31 * hash + cookie.path().hashCode();
		hash = 31 * hash + (cookie.secure() ? 0 : 1);
		hash = 31 * hash + (cookie.hostOnly() ? 0 : 1);

		return hash;
	}

	@Override
	public boolean equals(Object other)
	{
		if (!(other instanceof IdentifiableCookie))
		{
			return false;
		}

		IdentifiableCookie that = (IdentifiableCookie) other;

		return that.cookie.name().equals(this.cookie.name())
			&& that.cookie.domain().equals(this.cookie.domain())
			&& that.cookie.path().equals(this.cookie.path())
			&& that.cookie.secure() == this.cookie.secure()
			&& that.cookie.hostOnly() == this.cookie.hostOnly();
	}
}
