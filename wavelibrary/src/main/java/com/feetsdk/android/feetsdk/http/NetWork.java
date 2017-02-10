package com.feetsdk.android.feetsdk.http;

import android.content.Context;
import android.support.annotation.NonNull;

import com.feetsdk.android.common.exception.HttpException;
import com.feetsdk.android.common.utils.Logger;
import com.feetsdk.android.feetsdk.annotation.EventType;
import com.feetsdk.android.feetsdk.annotation.RequestMethod;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by cuieney on 16/11/9.
 */
public class NetWork {


    public static final String DEFAULT_JOSN_CACHE = "temp";
    public static final long DEFAULT_CACHE_SIZE = 50 * 1024 * 1024;

    public WeakReference<Context> contextWeakReference;

    private OkHttpClient client;
    private String url;
    private int method;

    private List<String> strings;

    private RequestBody requestBody;

    public NetWork(@NonNull Context context) {
        this.contextWeakReference = new WeakReference<>(context);
    }

    private NetWork(Builder builder) {

        Cache cache = builder.cache;
        InterceptorFactory interceptor = builder.interceptor;
        CookieJar cookieJar = builder.cookieJar;
        url = builder.url;
        method = builder.method;
        client = provideClient(cache, interceptor, cookieJar);
    }


    public Builder newBuilder(String url, @RequestMethod int method) {
        return new Builder(contextWeakReference.get(), url, method);
    }

    public static final class Builder {
        String url;
        int method;
        private WeakReference<Context> contextWeakReference;
        InterceptorFactory interceptor;
        Cache cache;
        CookieJar cookieJar;


        public Builder(Context context, String url, @RequestMethod int method) {
            this.url = url;
            this.method = method;
            this.contextWeakReference = new WeakReference<>(context);
        }


        public void setInterceptor(InterceptorFactory interceptor) {
            this.interceptor = interceptor;
        }

        public void setCache(Cache cache) {
            this.cache = cache;
        }

        public void setCookies(CookieJar cookies) {
            this.cookieJar = cookies;
        }

        public NetWork build() {
            if (cache == null) {
                cache = provideCache();
            }
            if (interceptor == null) {
                interceptor = providesCacheInterceptor(contextWeakReference.get());
            }

            if (cookieJar == null) {
                cookieJar = providesCookies();
            }
            NetWork netWork = new NetWork(this);
            return netWork;
        }


        public InterceptorFactory providesCacheInterceptor(Context context) {

            return new InterceptorFactory(context);
        }


        public Cache provideCache() {
            return new Cache(contextWeakReference.get().getExternalFilesDir(DEFAULT_JOSN_CACHE), DEFAULT_CACHE_SIZE);
        }

        public CookiesManager providesCookies() {
            return new CookiesManager();
        }

    }


    @NonNull
    public NetWork setPost(RequestBody requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    public void enqueue(final HttpCallBack callBack) {
        client.newCall(provideRequest()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.e(call.toString()+"  ");
                HttpException httpException = new HttpException(call.toString(), e);
                callBack.failed(httpException);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                HttpResponse httpResponse = new HttpResponse(response.code(), response.body().string());
                callBack.response(httpResponse);
            }
        });
    }

    private Request provideRequest() {
        Request.Builder builder = new Request.Builder();
        builder.url(url);

        if (method == EventType.METHOD_POST) {
            builder.post(requestBody);
        }
        if (method == EventType.METHOD_DELETE)
            builder.delete();

        return builder.build();
    }


    private OkHttpClient provideClient(Cache cache, InterceptorFactory interceptor, CookieJar cookiesManager) {
        X509TrustManager xtm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                X509Certificate[] x509Certificates = new X509Certificate[0];
                return x509Certificates;
            }
        };

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");

            sslContext.init(null, new TrustManager[]{xtm}, new SecureRandom());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };




        return new OkHttpClient.Builder()
                .addInterceptor(interceptor.ADD_HEAD_INTERCEPTOR)
                .addInterceptor(interceptor.REWRITE_RESPONSE_INTERCEPTOR)
                .sslSocketFactory(sslContext.getSocketFactory())
                .hostnameVerifier(DO_NOT_VERIFY)
                .cache(cache)
                .build();
    }

//    private SSLSocketFactory getCertificates() {
//        try {
//
////            InputStream inputStream = AppConfig.getApp().getAssets().open("https.cer");
//            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
//
//            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
//            keyStore.load(null);
//
//            int index = 0;
//            String certificateAlias = Integer.toString(index++);
//            keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(null));
//
//            SSLContext tls = SSLContext.getInstance("TLS");
//            TrustManagerFactory trustManagerFactory =
//                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//            trustManagerFactory.init(keyStore);
//            tls.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
//            return tls.getSocketFactory();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }


}
