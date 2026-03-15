package com.sgdigitalposter.app.repository;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.sgdigitalposter.app.Config;
import com.sgdigitalposter.app.api.ApiClient;
import com.sgdigitalposter.app.api.ApiResponse;
import com.sgdigitalposter.app.api.common.NetworkBoundResource;
import com.sgdigitalposter.app.api.common.common.Resource;
import com.sgdigitalposter.app.database.AppDatabase;
import com.sgdigitalposter.app.database.FestivalDao;
import com.sgdigitalposter.app.items.FestivalItem;
import com.sgdigitalposter.app.utils.Util;

import java.util.List;

public class FestivalRepository {

    public AppDatabase db;
    public FestivalDao festivalDao;

    private MediatorLiveData<Resource<List<FestivalItem>>> result = new MediatorLiveData<>();

    public FestivalRepository(Application application) {

        db = AppDatabase.getInstance(application);
        festivalDao = db.getFestivalDao();

    }

    public LiveData<Resource<List<FestivalItem>>> getResult(String apiKey, String page) {
        return new NetworkBoundResource<List<FestivalItem>, List<FestivalItem>>() {
            @Override
            protected void saveCallResult(@NonNull List<FestivalItem> item) {
                try {
                    db.runInTransaction(() -> {
                        festivalDao.deleteTable();
                        festivalDao.insert(item);
                    });
                } catch (Exception ex) {
                    Util.showErrorLog("Error at ", ex);
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<FestivalItem> data) {
                return Config.IS_CONNECTED;
            }

            @NonNull
            @Override
            protected LiveData<List<FestivalItem>> loadFromDb() {
                return festivalDao.getFestivals();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<FestivalItem>>> createCall() {
                return ApiClient.getApiService().getFestival(apiKey, page);
            }
        }.asLiveData();
    }

}
