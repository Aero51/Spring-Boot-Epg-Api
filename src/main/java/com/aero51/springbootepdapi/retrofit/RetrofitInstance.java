package com.aero51.springbootepdapi.retrofit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.jaxb.JaxbConverterFactory;

public class RetrofitInstance {

	private static Retrofit epdRetrofit = null;
	private static Retrofit pubProxyRetrofit = null;
	private static Retrofit gimmeProxyRetrofit = null;
	private static final String EPG_URL = "http://epg.iptvhr.net/"; // "https://epg.phoenixrebornbuild.com.hr/"; // ;
	private static final String PUB_PROXY_URL = "http://pubproxy.com/api/";
	private static final String GIMME_PROXY_URL = "http://gimmeproxy.com/api/";

	public static synchronized RetrofitApi getEpdApi(String proxyHost, int proxyPort) {
		if (epdRetrofit == null) {

			HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
			loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));

			// addInterceptor(REWRITE_CONTENT_LENGTH_INTERCEPTOR)
			// .proxy(proxy)

			OkHttpClient okHttpClient = new OkHttpClient.Builder().followRedirects(true).followSslRedirects(true)
					.connectTimeout(20, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
					.callTimeout(50, TimeUnit.SECONDS).retryOnConnectionFailure(false)

					.addInterceptor(loggingInterceptor)

					/*
					 * .addNetworkInterceptor(new Interceptor() {
					 * 
					 * @NotNull
					 * 
					 * @Override public Response intercept(@NotNull Chain chain) throws IOException
					 * { Request req = chain.request(); Headers.Builder headersBuilder =
					 * req.headers().newBuilder(); // String credential = Credentials.basic("test",
					 * "password"); // headersBuilder.set("Authorization", credential); Response res
					 * = chain.proceed(req.newBuilder().headers(headersBuilder.build()).build()); //
					 * .header("Connection", "close") return
					 * res.newBuilder().header("Content-Encoding", "gzip") .header("Content-Type",
					 * "application/xml").header("Accept-Encoding", "identity") .build(); } })
					 */

					.build();

			epdRetrofit = new Retrofit.Builder().baseUrl(EPG_URL).addConverterFactory(JaxbConverterFactory.create())
					.client(okHttpClient).build();

		}
		return epdRetrofit.create(RetrofitApi.class);
	}

	public static synchronized RetrofitApi getPubProxyApi() {
		if (pubProxyRetrofit == null) {

			HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
			loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

			// .addInterceptor(loggingInterceptor)
			OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
			pubProxyRetrofit = new Retrofit.Builder().baseUrl(PUB_PROXY_URL)
					.addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build();

		}
		return pubProxyRetrofit.create(RetrofitApi.class);
	}

	public static synchronized RetrofitApi getGimmeProxyApi() {
		if (gimmeProxyRetrofit == null) {

			HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
			loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

			// .addInterceptor(loggingInterceptor)
			OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
			gimmeProxyRetrofit = new Retrofit.Builder().baseUrl(GIMME_PROXY_URL)
					.addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build();

		}
		return gimmeProxyRetrofit.create(RetrofitApi.class);
	}

	private static final Interceptor REWRITE_CONTENT_LENGTH_INTERCEPTOR = new Interceptor() {
		@Override
		public Response intercept(Interceptor.Chain chain) throws IOException {
			Response originalResponse = chain.proceed(chain.request());
			return originalResponse.newBuilder().removeHeader("Content-Length").build();
		}
	};
}