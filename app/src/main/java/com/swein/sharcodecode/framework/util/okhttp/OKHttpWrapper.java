package com.swein.sharcodecode.framework.util.okhttp;

import com.swein.sharcodecode.framework.util.debug.ILog;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 *
 * After Android 9.0
 * OKHttp can not access http
 *
 * so add this in AndroidManifest.xml between the <application></application>
 *
 * <uses-library android:name="org.apache.http.legacy" android:required="false"/>
 *
 * and add android:usesCleartextTraffic="true" in the <application>
 */
public class OKHttpWrapper {

    public interface OKHttpWrapperDelegate {
        void onFailure(@NotNull Call call, @NotNull IOException e);
        void onResponse(@NotNull Call call, @NotNull Response response);
    }

    private final static String TAG = "OKHttpWrapper";

    public static OKHttpWrapper instance = new OKHttpWrapper();

    private OkHttpClient okHttpClient;


    private OKHttpWrapper(){}

    public void cancelCall(Call call) {
        if(!call.isCanceled()) {
            call.cancel();
        }
    }


    public String getStringResponse(Response response) throws IOException {

        ResponseBody responseBody = response.body();
        if(responseBody == null) {
            return "";
        }

        return responseBody.string();
    }

    public String getStringResponseWithCustomHeader(Response response) throws IOException {

        ResponseBody responseBody = response.body();
        if(responseBody == null) {
            return "";
        }

        return responseBody.string();
    }

    public void requestGet(String url, OKHttpWrapperDelegate okHttpWrapperDelegate) {

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }

        Request.Builder builder = new Request.Builder();
        Request request = builder.get().url(url).build();

        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }

    public void requestGetWithHeader(String url, HashMap<String, String> header, OKHttpWrapperDelegate okHttpWrapperDelegate) {

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }

        Request.Builder builder = new Request.Builder();

        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = builder.get().url(url).build();


        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
                                                                                                                                                                                                                                                                });
    }

    public void requestGetWithAuthorizationBearer(String url, OKHttpWrapperDelegate okHttpWrapperDelegate, String token) {

         if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }

        Request.Builder builder = new Request.Builder();
        Request request = builder.get().url(url).addHeader("Authorization", "Bearer " + token).build();

        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }

    public void requestPostWithJSON(String url, OKHttpWrapperDelegate okHttpWrapperDelegate, JSONObject jsonObject) {

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }

        MediaType mediaType = MediaType.get("application/x-www-form-urlencoded; charset=utf-8");
        RequestBody requestBody = RequestBody.create(String.valueOf(jsonObject), mediaType);

        Request.Builder builder = new Request.Builder();
        Request request = builder.url(url).post(requestBody).build();

        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }

    public void requestPost(String url, OKHttpWrapperDelegate okHttpWrapperDelegate) {

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }


        MediaType mediaType = MediaType.get("application/x-www-form-urlencoded; charset=utf-8");
        RequestBody requestBody = RequestBody.create("", mediaType);

        Request.Builder builder = new Request.Builder();
        Request request = builder.url(url).post(requestBody).build();

        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }

    public void requestPostWithHeaderAndJSONBodyRaw(String url, HashMap<String, String> header, JSONObject jsonObject, OKHttpWrapperDelegate okHttpWrapperDelegate) {

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(String.valueOf(jsonObject), mediaType);

        Request.Builder builder = new Request.Builder();

        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = builder.url(url).post(requestBody).build();

        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }

    public void requestPatchWithHeaderAndJSONBodyRaw(String url, HashMap<String, String> header, JSONObject jsonObject, OKHttpWrapperDelegate okHttpWrapperDelegate) {

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(String.valueOf(jsonObject), mediaType);

        Request.Builder builder = new Request.Builder();

        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = builder.url(url).patch(requestBody).build();

        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }

    public void requestPostWithHeader(String url, HashMap<String, String> header, OKHttpWrapperDelegate okHttpWrapperDelegate) {

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }


        MediaType mediaType = MediaType.get("application/x-www-form-urlencoded; charset=utf-8");
        RequestBody requestBody = RequestBody.create("", mediaType);

        Request.Builder builder = new Request.Builder();

        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = builder.post(requestBody).url(url).build();

        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }

    public void requestDeleteWithHeader(String url, HashMap<String, String> header, OKHttpWrapperDelegate okHttpWrapperDelegate) {

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }


        MediaType mediaType = MediaType.get("application/x-www-form-urlencoded; charset=utf-8");
        RequestBody requestBody = RequestBody.create("", mediaType);

        Request.Builder builder = new Request.Builder();

        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = builder.delete(requestBody).url(url).build();

        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }

    public void requestPutWithHeader(String url, HashMap<String, String> header, OKHttpWrapperDelegate okHttpWrapperDelegate) {

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }


        MediaType mediaType = MediaType.get("application/x-www-form-urlencoded; charset=utf-8");
        RequestBody requestBody = RequestBody.create("", mediaType);

        Request.Builder builder = new Request.Builder();

        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = builder.put(requestBody).url(url).build();

        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }

    public void requestPutModelNameWithHeader(String url, HashMap<String, String> header, String modelId, String name, String jsonObj, OKHttpWrapperDelegate okHttpWrapperDelegate) {

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }

        RequestBody requestBody = new FormBody.Builder()
                .add("modelId", String.valueOf(modelId))
                .add("name", name)
                .add("jsonObj", jsonObj)
                .build();

        Request.Builder builder = new Request.Builder();

        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = builder.put(requestBody).url(url).build();

        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }

    public void requestPostImageFile(String url, HashMap<String, String> header, String filePath, OKHttpWrapperDelegate okHttpWrapperDelegate) {

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }

        final MediaType MEDIA_TYPE = filePath.endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");

        File file = new File(filePath);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", file.getName(),  RequestBody.create(MEDIA_TYPE, file))
                .build();

        Request.Builder builder = new Request.Builder();

        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = builder.post(requestBody).url(url).build();

        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }

    public void requestPostFormDataId(String url, int affiliateId, HashMap<String, String> header, OKHttpWrapperDelegate okHttpWrapperDelegate) {

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }

        RequestBody requestBody = new FormBody.Builder()
                .add("affiliateId", String.valueOf(affiliateId))
                .build();


        Request.Builder builder = new Request.Builder();

        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = builder.post(requestBody).url(url).build();

        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }

    public void requestPostFormDataEmail(String url, HashMap<String, String> header, String email, OKHttpWrapperDelegate okHttpWrapperDelegate) {

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }

        RequestBody requestBody = new FormBody.Builder()
                .add("email", String.valueOf(email))
                .build();


        Request.Builder builder = new Request.Builder();

        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = builder.post(requestBody).url(url).build();

        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }

    public void requestPostWithFormDataAndFiles(String url, HashMap<String, String> header, HashMap<String, String> formData, List<String> imageList, List<String> imageNameList, OKHttpWrapperDelegate okHttpWrapperDelegate) {

        ILog.iLogDebug(TAG, "requestPostWithFormDataAndFiles");

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }

        final

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        multipartBodyBuilder.setType(MultipartBody.FORM);

        ILog.iLogDebug(TAG, "add form data");
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            ILog.iLogDebug(TAG, entry.getKey() + " " + entry.getValue());
            multipartBodyBuilder.addFormDataPart(entry.getKey(), entry.getValue());
        }

        ILog.iLogDebug(TAG, "add files");
        File file;
        for(int i = 0; i < imageList.size(); i++) {
            ILog.iLogDebug(TAG, imageNameList.get(i));
            MediaType mediaType = imageList.get(i).endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");
            file = new File(imageList.get(i));

            multipartBodyBuilder.addFormDataPart("files", imageNameList.get(i), RequestBody.create(mediaType, file));
        }

        RequestBody requestBody = multipartBodyBuilder.build();

        Request.Builder builder = new Request.Builder();

        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = builder.post(requestBody).url(url).build();

        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }

    public void requestPostWithFormDataFiles(String url, HashMap<String, String> header, HashMap<String, String> formData, List<String> imageList, OKHttpWrapperDelegate okHttpWrapperDelegate) {

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }

        final

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        multipartBodyBuilder.setType(MultipartBody.FORM);
//        multipartBodyBuilder.addFormDataPart("affiliateId", affiliateId);
//        multipartBodyBuilder.addFormDataPart("content", content);
//        multipartBodyBuilder.addFormDataPart("rateTotal", rateTotal);

        File file;
        for(int i = 0; i < imageList.size(); i++) {
            MediaType mediaType = imageList.get(i).endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");
            file = new File(imageList.get(i));

            multipartBodyBuilder.addFormDataPart("files", file.getName(), RequestBody.create(mediaType, file));
        }

        RequestBody requestBody = multipartBodyBuilder.build();

        Request.Builder builder = new Request.Builder();

        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = builder.post(requestBody).url(url).build();

        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }

    public void requestPostWithMultipartFormDataJSONAndFiles(String url, HashMap<String, String> header,
                                                             String hashtag, String contents,
                                                             List<String> imageList, OKHttpWrapperDelegate okHttpWrapperDelegate) {

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }


        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        multipartBodyBuilder.setType(MultipartBody.FORM);
        multipartBodyBuilder.addFormDataPart("hashtag", hashtag);
        multipartBodyBuilder.addFormDataPart("contents", contents);

        File file;
        for(int i = 0; i < imageList.size(); i++) {
            MediaType mediaType = imageList.get(i).endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");
            file = new File(imageList.get(i));

            multipartBodyBuilder.addFormDataPart("files", file.getName(), RequestBody.create(mediaType, file));
        }

        RequestBody requestBody = multipartBodyBuilder.build();

        Request.Builder builder = new Request.Builder();

        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = builder.post(requestBody).url(url).build();

        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }

    public void requestPostWithMultipartFormDataAndCustomNameFiles(String url, HashMap<String, String> header,
                                                                   String hashtag, String contents,
                                                                   List<String> imageList, List<String> imageNameList, OKHttpWrapperDelegate okHttpWrapperDelegate) {

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }


        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        multipartBodyBuilder.setType(MultipartBody.FORM);
        multipartBodyBuilder.addFormDataPart("hashtag", hashtag);
        multipartBodyBuilder.addFormDataPart("contents", contents);

        File file;
        for(int i = 0; i < imageList.size(); i++) {
            MediaType mediaType = imageList.get(i).endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");
            file = new File(imageList.get(i));

            multipartBodyBuilder.addFormDataPart("files", imageNameList.get(i), RequestBody.create(mediaType, file));
        }

        RequestBody requestBody = multipartBodyBuilder.build();

        Request.Builder builder = new Request.Builder();

        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = builder.post(requestBody).url(url).build();

        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }

    public void requestPutWithMultipartFormDataAndCustomNameFiles(String url, HashMap<String, String> header,
                                                                  String imageFile, String nickname, String title,
                                                                  String description, String address1, String address2,
                                                                  String extraAdder, String area, OKHttpWrapperDelegate okHttpWrapperDelegate) {

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }


        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        multipartBodyBuilder.setType(MultipartBody.FORM);
        multipartBodyBuilder.addFormDataPart("nickname", nickname);
        multipartBodyBuilder.addFormDataPart("title", title);
        multipartBodyBuilder.addFormDataPart("description", description);
        multipartBodyBuilder.addFormDataPart("address1", address1);
        multipartBodyBuilder.addFormDataPart("address2", address2);
        multipartBodyBuilder.addFormDataPart("extraAddr", extraAdder);
        multipartBodyBuilder.addFormDataPart("area", area);

        if(imageFile != null && !imageFile.equals("")) {

            MediaType mediaType = imageFile.endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");
            File file = new File(imageFile);

            multipartBodyBuilder.addFormDataPart("files", file.getName(), RequestBody.create(mediaType, file));
        }

        RequestBody requestBody = multipartBodyBuilder.build();

        Request.Builder builder = new Request.Builder();

        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = builder.put(requestBody).url(url).build();

        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }


    public void requestPostWithMultipartFormDataAndCustomNameFiles(String url, HashMap<String, String> header,
                                                                       String hashtag, String contents,
                                                                       List<String> imageList, OKHttpWrapperDelegate okHttpWrapperDelegate) {

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }


        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        multipartBodyBuilder.setType(MultipartBody.FORM);
        multipartBodyBuilder.addFormDataPart("hashtag", hashtag);
        multipartBodyBuilder.addFormDataPart("contents", contents);

        File file;
        for(int i = 0; i < imageList.size(); i++) {
            MediaType mediaType = imageList.get(i).endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");
            file = new File(imageList.get(i));

            multipartBodyBuilder.addFormDataPart("files", file.getName(), RequestBody.create(mediaType, file));
        }

        RequestBody requestBody = multipartBodyBuilder.build();

        Request.Builder builder = new Request.Builder();

        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = builder.post(requestBody).url(url).build();

        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }

    public void requestPostWithMultipartFiles(String url, HashMap<String, String> header, List<String> imageList, OKHttpWrapperDelegate okHttpWrapperDelegate) {

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }


        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        multipartBodyBuilder.setType(MultipartBody.FORM);

        File file;
        for(int i = 0; i < imageList.size(); i++) {
            MediaType mediaType = imageList.get(i).endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");
            file = new File(imageList.get(i));

            multipartBodyBuilder.addFormDataPart("files", file.getName(), RequestBody.create(mediaType, file));
        }

        RequestBody requestBody = multipartBodyBuilder.build();

        Request.Builder builder = new Request.Builder();

        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = builder.post(requestBody).url(url).build();

        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }

    public void requestPostFormDataModel(String url, HashMap<String, String> header,
                                         String name, String jsonObj,
                                         String filePath, OKHttpWrapperDelegate okHttpWrapperDelegate) {

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }

        final

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        multipartBodyBuilder.setType(MultipartBody.FORM);
        multipartBodyBuilder.addFormDataPart("name", name);
        multipartBodyBuilder.addFormDataPart("jsonObj", jsonObj);

        if(filePath != null && !filePath.equals("")) {

            MediaType mediaType = filePath.endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");
            File file = new File(filePath);

            multipartBodyBuilder.addFormDataPart("files", file.getName(), RequestBody.create(mediaType, file));
        }

        RequestBody requestBody = multipartBodyBuilder.build();

        Request.Builder builder = new Request.Builder();

        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = builder.post(requestBody).url(url).build();

        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }


    public void requestPostWithMultipartFormDataJSONAndFiles(String url, HashMap<String, String> header,
                                                             String content, String rateTotal,
                                                             List<String> imageList, List<String> imageWillKeepList, OKHttpWrapperDelegate okHttpWrapperDelegate) {

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }

        final

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        multipartBodyBuilder.setType(MultipartBody.FORM);
        multipartBodyBuilder.addFormDataPart("content", content);
        multipartBodyBuilder.addFormDataPart("rateTotal", rateTotal);

        if(imageWillKeepList.isEmpty()) {
            multipartBodyBuilder.addFormDataPart("imageUrl", "");
        }
        else {

            StringBuilder stringBuilder = new StringBuilder();
            for(int i = 0; i < imageWillKeepList.size(); i++) {

                stringBuilder.append(imageWillKeepList.get(i));

                if(i < imageWillKeepList.size() - 1) {
                    stringBuilder.append(",");
                }
            }

            multipartBodyBuilder.addFormDataPart("imageUrl", stringBuilder.toString());
        }

        File file;
        for(int i = 0; i < imageList.size(); i++) {
            MediaType mediaType = imageList.get(i).endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");
            file = new File(imageList.get(i));

            multipartBodyBuilder.addFormDataPart("imageFile", file.getName(), RequestBody.create(mediaType, file));
        }

        RequestBody requestBody = multipartBodyBuilder.build();

        Request.Builder builder = new Request.Builder();

        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = builder.post(requestBody).url(url).build();

        Call call = okHttpClient.newCall(request);

        // auto  thread
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }

    /**
     * example:
     *
     * OKHttpWrapper.getInstance().requestDownloadFile("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1564642051087&di=a5a0ecddbceeaf44e2b55d0439eaed44&imgtype=0&src=http%3A%2F%2Fen.pimg.jp%2F017%2F747%2F873%2F1%2F17747873.jpg",
     *                 new OKHttpWrapper.OKHttpWrapperDelegate() {
     *                     @Override
     *                     public void onFailure(@NotNull Call call, @NotNull IOException e) {
     *                         OKHttpWrapper.getInstance().cancelCall(call);
     *                     }
     *
     *                     @Override
     *                     public void onResponse(@NotNull Call call, @NotNull Response response) {
     *                         try {
     *                             OKHttpWrapper.getInstance().storageFileResponse(response, Environment.getExternalStorageDirectory().toString() + "/default.png");
     *                         }
     *                         catch (IOException e) {
     *                             e.printStackTrace();
     *                         }
     *                         finally {
     *                             OKHttpWrapper.getInstance().cancelCall(call);
     *                         }
     *                     }
     *                 });
     */
    public void requestDownloadFile(String url, OKHttpWrapperDelegate okHttpWrapperDelegate) {

        if(okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            okHttpClient = builder.build();
        }

        Request request = new Request.Builder().url(url).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpWrapperDelegate.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                okHttpWrapperDelegate.onResponse(call, response);

            }
        });
    }
    public void storageFileResponse(Response response, String fileName) throws IOException {

        ResponseBody responseBody = response.body();
        if(responseBody == null) {
            return;
        }

        File file = new File(fileName);

        if(!file.exists()) {
            file.createNewFile();
        }

        byte[] buf = new byte[2048];
        int len = 0;
        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;

        try {
            inputStream = responseBody.byteStream();
            fileOutputStream = new FileOutputStream(file);
            while ((len = inputStream.read(buf)) != -1) {
                fileOutputStream.write(buf, 0, len);
            }
            fileOutputStream.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public void clear() {
        if(okHttpClient != null) {
            okHttpClient = null;
        }
    }
}
