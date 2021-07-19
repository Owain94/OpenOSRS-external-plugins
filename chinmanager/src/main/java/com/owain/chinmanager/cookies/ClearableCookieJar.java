package com.owain.chinmanager.cookies;

import okhttp3.CookieJar;

public interface ClearableCookieJar extends CookieJar
{
	void clearSession();

	void clear();
}
