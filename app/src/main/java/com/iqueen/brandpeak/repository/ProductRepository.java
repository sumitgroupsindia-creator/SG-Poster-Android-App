package com.sgdigitalposter.app.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sgdigitalposter.app.Config;
import com.sgdigitalposter.app.api.ApiClient;
import com.sgdigitalposter.app.api.ApiResponse;
import com.sgdigitalposter.app.api.ApiStatus;
import com.sgdigitalposter.app.api.common.NetworkBoundResource;
import com.sgdigitalposter.app.api.common.common.Resource;
import com.sgdigitalposter.app.database.AppDatabase;
import com.sgdigitalposter.app.database.ProductDao;
import com.sgdigitalposter.app.items.ProductCatItem;
import com.sgdigitalposter.app.items.ProductItem;
import com.sgdigitalposter.app.items.ProductModel;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class ProductRepository {

    AppDatabase db;
    ProductDao productDao;

    public ProductRepository(Application application) {
        db = AppDatabase.getInstance(application);
        productDao = db.getProductDao();
    }

    public LiveData<Resource<ProductModel>> getProductModels(String apiKey) {
        return new NetworkBoundResource<ProductModel, ProductModel>() {
            @Override
            protected void saveCallResult(@NonNull ProductModel item) {

                try {
                    db.runInTransaction(() -> {

                        productDao.deleteAllModels();
                        productDao.insertProductModel(item);

                        productDao.deleteAllCategory();
                        productDao.deleteAllProduct();

                        productDao.insertProduct(item.productItemList);
                        productDao.insetProductCategory(item.productCatItemList);

                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            protected boolean shouldFetch(@Nullable ProductModel data) {
                return Config.IS_CONNECTED;
            }

            @NonNull
            @Override
            protected LiveData<ProductModel> loadFromDb() {
                return productDao.getModels();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ProductModel>> createCall() {
                return ApiClient.getApiService().getProducts(apiKey);
            }
        }.asLiveData();
    }

    public LiveData<List<ProductCatItem>> getProductCategoryList() {
        return productDao.getCategory();
    }

    public LiveData<List<ProductItem>> getProductBySearch(String keyword) {
        return productDao.getProductBySearch(keyword);
    }

    public LiveData<List<ProductItem>> getProductByCategory(String categoryId) {
        if(categoryId.equals("ALL")){
            return productDao.getAllProducts();
        }
        return productDao.getProductByCategory(categoryId);
    }

    public LiveData<Resource<Boolean>> sendContact(String apiKey, String name, String email,String mobile, String massage, String id) {
        final MutableLiveData<Resource<Boolean>> statusLiveData = new MutableLiveData<>();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {

            Response<ApiStatus> response;

            try {
                response = ApiClient.getApiService().sendEnquiry(apiKey, name, email, mobile, massage, id).execute();


                if (response.isSuccessful()) {
                    statusLiveData.postValue(Resource.success(true));
                } else {
                    statusLiveData.postValue(Resource.error("error", false));
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

}
