package com.owain.chinmanager.cookies.persistence;

import java.util.Collection;
import java.util.List;
import okhttp3.Cookie;

public interface CookiePersistor
{
	List<Cookie> loadAll();

	void saveAll(Collection<Cookie> cookies);

	void clear();

}
