package com.sgdigitalposter.app.repository;

import android.app.Activity;
import android.app.Application;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.sgdigitalposter.app.Config;
import com.sgdigitalposter.app.api.ApiClient;
import com.sgdigitalposter.app.api.ApiResponse;
import com.sgdigitalposter.app.api.ApiStatus;
import com.sgdigitalposter.app.api.common.NetworkBoundResource;
import com.sgdigitalposter.app.api.common.common.Resource;
import com.sgdigitalposter.app.database.AppDatabase;
import com.sgdigitalposter.app.database.SubsPlanDao;
import com.sgdigitalposter.app.items.CouponItem;
import com.sgdigitalposter.app.items.SubsPlanItem;
import com.sgdigitalposter.app.utils.Util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class SubsPlanRepository {

    AppDatabase db;
    SubsPlanDao subsplanDao;
    public MediatorLiveData<Resource<List<SubsPlanItem>>> result = new MediatorLiveData<>();
    List<SubsPlanItem> planList;

    public SubsPlanRepository(Application application) {
        db = AppDatabase.getInstance(application);
        subsplanDao = db.getSubsPlanDao();
    }

    public LiveData<Resource<List<SubsPlanItem>>> getSubsPlanItems(Activity activity, String apiKey) {

        return new NetworkBoundResource<List<SubsPlanItem>, List<SubsPlanItem>>() {
            @Override
            protected void saveCallResult(@NonNull List<SubsPlanItem> item) {
                try {
                    db.runInTransaction(() -> {
                        planList = item;
                        subsplanDao.deleteData();
                        subsplanDao.insertAll(item);
                    });
                } catch (Exception e) {

                }

            }

            @Override
            protected boolean shouldFetch(@Nullable List<SubsPlanItem> data) {
                return Config.IS_CONNECTED;
            }

            @NonNull
            @Override
            protected LiveData<List<SubsPlanItem>> loadFromDb() {
                return subsplanDao.getAllPlan();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<SubsPlanItem>>> createCall() {
                return ApiClient.getApiService().getPlanData(apiKey);
            }
        }.asLiveData();
    }

    public LiveData<Resource<ApiStatus>> loadPayment(String apiKey, String userId, String planId, String paymentId, String planPrice,
                                                     String couponCode, String referralCode, String type) {
        final MutableLiveData<Resource<ApiStatus>> statusLiveData = new MutableLiveData<>();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {

            Response<ApiStatus> response;

            try {
                response = ApiClient.getApiService().loadPayment(apiKey, userId, planId, paymentId, planPrice,
                        couponCode, type, referralCode).execute();

                if (response.isSuccessful()) {
                    Util.showLog("SUCCESS: " + response.body().toString());
                    statusLiveData.postValue(Resource.success(response.body()));
                } else {
                    Util.showLog("SSS: " + response.errorBody().string().toString());
                    statusLiveData.postValue(Resource.error(response.message(), null));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            handler.post(() -> {
                //UI Thread work here

            });
        });

        return statusLiveData;
    }

    public LiveData<Resource<ApiStatus>> offlinePayment(String apiKey, String filePath,
                                                               Uri uri, String userId,
                                                               String planId, String paymentAmount, String couponCode, String refCode) {
        MultipartBody.Part body = null;
        RequestBody fullName = null;
        if (filePath!=null && !filePath.equals("")) {
            File file = new File(filePath);
            RequestBody requestFile =
                    RequestBody.create(MediaType.parse("multipart/form-data"), file);

            // MultipartBody.Part is used to send also the actual file news_title
            body = MultipartBody.Part.createFormData("payment_receipt", file.getName(), requestFile);

            fullName =
                    RequestBody.create(
                            MediaType.parse("multipart/form-data"), file.getName());
        }
        RequestBody useIdRB =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), userId);

        RequestBody planRB =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), planId);

        RequestBody amountRB =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), paymentAmount);

        RequestBody couponRB =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), couponCode);

        RequestBody refRB =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), refCode);


        MultipartBody.Part finalBody = body;
        RequestBody finalFullName = fullName;

        final MutableLiveData<Resource<ApiStatus>> statusLiveData = new MutableLiveData<>();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {

            Response<ApiStatus> response;

            try {
                response = ApiClient.getApiService().offlinePayment(apiKey, useIdRB, planRB, amountRB,
                        finalFullName, finalBody, couponRB, refRB).execute();

                if (response.isSuccessful()) {
                    Util.showLog("SUCCESS: " + response.body().toString());
                    statusLiveData.postValue(Resource.success(response.body()));
                } else {
                    Util.showLog("SSS: " + response.errorBody().string().toString());
                    statusLiveData.postValue(Resource.error(response.message(), null));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            handler.post(() -> {
                //UI Thread work here

            });
        });

        return statusLiveData;
    }


    public LiveData<Resource<CouponItem>> checkCoupon(String apiKey, String userId, String couponCode) {
        final MutableLiveData<Resource<CouponItem>> statusLiveData =
                new MutableLiveData<>();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            //Background work here
            try {

                // Call the API Service
                Response<CouponItem> response = ApiClient.getApiService().checkCoupon(apiKey,
                        userId,
                        couponCode).execute();


                // Wrap with APIResponse Class
                ApiResponse<CouponItem> apiResponse = new ApiResponse<>(response);

                // If response is successful
                if (apiResponse.isSuccessful()) {

                    statusLiveData.postValue(Resource.success(response.body()));

                } else {
                    statusLiveData.postValue(Resource.error(apiResponse.errorMessage, null));
                }

            } catch (IOException e) {
                statusLiveData.postValue(Resource.error(e.getMessage(), null));
            }
            handler.post(() -> {
                //UI Thread work here

            });
        });

        return statusLiveData;
    }


    public void updatePrice(String price, String id) {
        subsplanDao.updateData(price, id);
    }

}
