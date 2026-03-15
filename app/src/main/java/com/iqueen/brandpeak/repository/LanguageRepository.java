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
import com.sgdigitalposter.app.database.LanguageDao;
import com.sgdigitalposter.app.items.LanguageItem;
import com.sgdigitalposter.app.utils.Util;

import java.util.List;

public class LanguageRepository {

    public Application application;
    public LanguageDao languageDao;
    public AppDatabase db;

    private MediatorLiveData<Resource<List<LanguageItem>>> result = new MediatorLiveData<>();

    public LanguageRepository(Application application) {
        this.application = application;
        db = AppDatabase.getInstance(application);
        languageDao = db.getLanguageDao();
    }

    public LiveData<Resource<List<LanguageItem>>> getLanguages(String apiKey) {
       return new NetworkBoundResource<List<LanguageItem>, List<LanguageItem>>() {
           @Override
           protected void saveCallResult(@NonNull List<LanguageItem> item) {
               try {
                   db.runInTransaction(() -> {
                       languageDao.deleteTable();
                       languageDao.insetAll(item);
                   });
               } catch (Exception ex) {
                   Util.showErrorLog("Error at ", ex);
               }
           }

           @Override
           protected boolean shouldFetch(@Nullable List<LanguageItem> data) {
               return Config.IS_CONNECTED;
           }

           @NonNull
           @Override
           protected LiveData<List<LanguageItem>> loadFromDb() {
               return languageDao.getLanguages();
           }

           @NonNull
           @Override
           protected LiveData<ApiResponse<List<LanguageItem>>> createCall() {
               return ApiClient.getApiService().getLanguages(apiKey);
           }
       }.asLiveData();
    }

    public void updateLanguage(String languageId, boolean isUpdate){
        languageDao.UpdateLanguage(isUpdate, languageId);
    }
}
