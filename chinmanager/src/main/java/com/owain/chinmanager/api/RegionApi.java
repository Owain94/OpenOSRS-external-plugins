package com.owain.chinmanager.api;

import com.owain.chinmanager.ChinManagerPlugin;
import static com.owain.chinmanager.ChinManagerPlugin.JSON;
import io.reactivex.rxjava3.core.Observable;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class RegionApi
{
	private static final String COLLISION_MAP_API_URL = "collisionmap.xyz";
	private static final int COLLISION_MAP_API_VERSION = 3;

	private final ChinManagerPlugin chinManagerPlugin;

	@Inject
	RegionApi(ChinManagerPlugin chinManagerPlugin)
	{
		this.chinManagerPlugin = chinManagerPlugin;
	}

	public Observable<Boolean> sendXtea(String json)
	{
		return Observable.defer(() ->
		{
			HttpUrl httpUrl = BaseApi.xtea();
			RequestBody body = RequestBody.create(json, JSON);

			Request request = new Request.Builder()
				.addHeader("accept", "application/json")
				.url(httpUrl)
				.post(body)
				.build();

			try (Response response = chinManagerPlugin.getOkHttpClient().newCall(request).execute())
			{
				int code = response.code();

				if (code != 200 && code != 201)
				{
					log.error("Xtea request was unsuccessful: {}", code);
					return Observable.just(false);
				}

				log.debug("Xtea request was successful: {}", code);
				return Observable.just(true);
			}
			catch (Exception ignored)
			{
				return Observable.just(false);
			}
		});
	}

	public Observable<Boolean> sendCollisionMap(String json)
	{
		return Observable.defer(() ->
		{
			HttpUrl httpUrl = new HttpUrl.Builder()
				.scheme("https")
				.host(COLLISION_MAP_API_URL)
				.addPathSegment("regions")
				.addPathSegment(String.valueOf(COLLISION_MAP_API_VERSION))
				.build();

			RequestBody body = RequestBody.create(json, JSON);

			Request request = new Request.Builder()
				.addHeader("accept", "application/json")
				.url(httpUrl)
				.post(body)
				.build();

			try (Response response = chinManagerPlugin.getOkHttpClient().newCall(request).execute())
			{
				int code = response.code();

				if (code != 200 && code != 201)
				{
					log.error("Collisionmap request was unsuccessful: {}", code);
					return Observable.just(false);
				}

				log.debug("Collisionmap request was successful: {}", code);
				return Observable.just(true);
			}
			catch (Exception ignored)
			{
				return Observable.just(false);
			}
		});
	}
}
