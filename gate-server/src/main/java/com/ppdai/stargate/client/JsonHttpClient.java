package com.ppdai.stargate.client;

import com.alibaba.fastjson.JSON;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JsonHttpClient {

    private static final MediaType JSONTYPE = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType DOCKERTYPE = MediaType.parse("application/vnd.docker.distribution.manifest.v2+json");

    private OkHttpClient client;

    public JsonHttpClient(int connTimeout, int readTimeout) {
        client = new OkHttpClient.Builder().connectTimeout(connTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS).build();
    }

    public JsonHttpClient() {
        this(10000, 10000);
    }

    public String post(String url, Object reqObj) throws IOException {
        String json = null;
        if (reqObj instanceof String) {
            json = reqObj.toString();
        } else {
            json = JSON.toJSONString(reqObj);
        }

        RequestBody body = RequestBody.create(JSONTYPE, json);
        Request request = new Request.Builder().url(url).post(body).build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("code=" + response.code() + ", status=" + response.message());
            }
            return response.body().string();
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (Exception e) {

            }
        }
    }

    public String post(String url, Map<String, String> headers, Object reqObj) throws IOException {
        String json = null;
        if (reqObj instanceof String) {
            json = reqObj.toString();
        } else {
            json = JSON.toJSONString(reqObj);
        }

        RequestBody body = RequestBody.create(JSONTYPE, json);

        Headers.Builder hb = new Headers.Builder();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            hb.add(entry.getKey(), entry.getValue());
        }

        Request request = new Request.Builder().url(url).headers(hb.build()).post(body).build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("code=" + response.code() + ", status=" + response.message());
            }
            return response.body().string();
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (Exception e) {

            }
        }
    }

    public String put(String url, String body) throws IOException {

        Request request = new Request.Builder().url(url).put(RequestBody.create(JSONTYPE, body)).build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("code=" + response.code() + ", status=" + response.message());
            }
            return response.body().string();
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (Exception e) {

            }
        }
    }

    public String put(String url, Map<String, String> headers, String body) throws IOException {
        Headers.Builder hb = new Headers.Builder();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            hb.add(entry.getKey(), entry.getValue());
        }

        Request request = new Request.Builder().url(url).headers(hb.build()).put(RequestBody.create(DOCKERTYPE, body)).build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            String responseBody = response.body().string();
            if (!response.isSuccessful()) {
                throw new IOException(responseBody);
            }
            return responseBody;
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (Exception e) {

            }
        }
    }

    public String delete(String url, Map<String, String> headers) throws IOException {
        Headers.Builder hb = new Headers.Builder();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            hb.add(entry.getKey(), entry.getValue());
        }

        Request request = new Request.Builder().url(url).headers(hb.build()).delete().build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("code=" + response.code() + ", status=" + response.message());
            }
            return response.body().string();
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (Exception e) {

            }
        }
    }

    public String delete(String url) throws IOException {

        Request request = new Request.Builder().url(url).delete().build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("code=" + response.code() + ", status=" + response.message());
            }
            return response.body().string();
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (Exception e) {

            }
        }
    }

    public <T> T post(String url, Object request, Class<T> tClass) throws IOException {
        String rs = post(url, request);
        if (rs == null || rs.length() == 0 || rs.trim().length() == 0) {
            return null;
        } else {
            return JSON.parseObject(rs, tClass);
        }
    }

    public String get(String url) throws IOException {
        Request request = new Request.Builder().url(url).get().build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("code=" + response.code() + ", status=" + response.message());
            }
            return response.body().string();
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (Exception e) {

            }
        }
    }

    public String get(String url, Map<String, String> headers) throws IOException {
        Headers.Builder hb = new Headers.Builder();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            hb.add(entry.getKey(), entry.getValue());
        }

        Request request = new Request.Builder().url(url).headers(hb.build()).get().build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("code=" + response.code() + ", status=" + response.message());
            }
            return response.body().string();
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (Exception e) {

            }
        }
    }

    public <T> T get(String url, Class<T> tClass) throws IOException {
        Request request = new Request.Builder().url(url).get().build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("code=" + response.code() + ", status=" + response.message());
            }
            return JSON.parseObject(response.body().string(), tClass);
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (Exception e) {

            }
        }
    }
}
