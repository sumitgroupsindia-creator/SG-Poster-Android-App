package com.sgdigitalposter.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.sgdigitalposter.app.api.common.common.Resource;
import com.sgdigitalposter.app.items.BusinessCategoryItem;
import com.sgdigitalposter.app.items.BusinessSubCategoryItem;
import com.sgdigitalposter.app.items.CategoryItem;
import com.sgdigitalposter.app.items.CustomCategory;
import com.sgdigitalposter.app.items.CustomModel;
import com.sgdigitalposter.app.repository.CategoryRepository;
import com.sgdigitalposter.app.utils.AbsentLiveData;
import com.sgdigitalposter.app.utils.Constant;
import com.sgdigitalposter.app.utils.PrefManager;

import java.util.List;

public class CategoryViewModel extends AndroidViewModel {

    public LiveData<Resource<List<CategoryItem>>> result;
    public MutableLiveData<TmpDataHolder> categoryObj = new MutableLiveData<>();

    public LiveData<Resource<List<CustomCategory>>> custom_result;
    public MutableLiveData<TmpDataHolder> custom_categoryObj = new MutableLiveData<>();

    public LiveData<Resource<CustomModel>> customModelData;
    public MutableLiveData<String> customModelObj = new MutableLiveData<>();

    public LiveData<Resource<List<BusinessCategoryItem>>> busCategoriesData;
    public MutableLiveData<String> busCategoriesObj = new MutableLiveData<>();


    public LiveData<Resource<List<BusinessSubCategoryItem>>> subCatData;
    public MutableLiveData<String> subCatObj = new MutableLiveData<>();

    CategoryRepository categoryRepository;
    PrefManager prefManager;

    public CategoryViewModel(@NonNull Application application) {
        super(application);

        categoryRepository = new CategoryRepository(application);
        prefManager = new PrefManager(application);

        result = Transformations.switchMap(categoryObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }
            return categoryRepository.getCategory(prefManager.getString(Constant.api_key), obj.page);
        });

        custom_result = Transformations.switchMap(custom_categoryObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }
            return categoryRepository.getCustomCategory(prefManager.getString(Constant.api_key), obj.page);
        });

        busCategoriesData = Transformations.switchMap(busCategoriesObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }
            return categoryRepository.getBusinessCategories(prefManager.getString(Constant.api_key));
        });

        customModelData = Transformations.switchMap(customModelObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }
            return categoryRepository.getCustomModel(prefManager.getString(Constant.api_key));
        });

        subCatData = Transformations.switchMap(subCatObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }
            return categoryRepository.getBusinessSubCategory(prefManager.getString(Constant.api_key), obj);
        });

    }

    public void setSubCategory(String categoryId){
        subCatObj.setValue(categoryId);
    }

    public LiveData<Resource<List<BusinessSubCategoryItem>>> getSubCategory(){
        return subCatData;
    }

    public void setCategoryObj(String page) {
        TmpDataHolder tmpDataHolder = new TmpDataHolder();
        tmpDataHolder.page = page;
        categoryObj.setValue(tmpDataHolder);
    }

    public LiveData<Resource<List<CategoryItem>>> getCategories() {
        return result;
    }

    public void setCustomCategoryObj(String page) {
        TmpDataHolder tmpDataHolder = new TmpDataHolder();
        tmpDataHolder.page = page;
        custom_categoryObj.setValue(tmpDataHolder);
    }

    public LiveData<Resource<List<CustomCategory>>> getCustomCategories() {
        return custom_result;
    }

    public void setBusinessCategoryObj(String category) {
        busCategoriesObj.setValue(category);
    }

    public LiveData<Resource<List<BusinessCategoryItem>>> getBusinessCategories() {
        return busCategoriesData;
    }

    public void setCustomModelObj(String category) {
        customModelObj.setValue(category);
    }

    public LiveData<Resource<CustomModel>> getCustomModel() {
        return customModelData;
    }

    static class TmpDataHolder {
        public String page = "";
    }
}
