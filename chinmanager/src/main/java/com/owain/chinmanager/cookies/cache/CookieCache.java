package com.owain.chinmanager.cookies.cache;

import java.util.Collection;
import okhttp3.Cookie;

public interface CookieCache extends Iterable<Cookie>
{
	void addAll(Collection<Cookie> cookies);

	void clear();
}
