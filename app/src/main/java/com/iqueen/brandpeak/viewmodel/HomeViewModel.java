package com.sgdigitalposter.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.sgdigitalposter.app.api.common.common.Resource;
import com.sgdigitalposter.app.items.BusinessCategoryItem;
import com.sgdigitalposter.app.items.HomeItem;
import com.sgdigitalposter.app.items.PersonalItem;
import com.sgdigitalposter.app.repository.HomeRepository;
import com.sgdigitalposter.app.utils.AbsentLiveData;
import com.sgdigitalposter.app.utils.Constant;
import com.sgdigitalposter.app.utils.PrefManager;

import java.util.List;

public class HomeViewModel extends AndroidViewModel  {

    HomeRepository homeRepository;
    public LiveData<Resource<HomeItem>> result;
    public MutableLiveData<String> homeObj = new MutableLiveData<>();

    public MutableLiveData<String> personalObj = new MutableLiveData<>();
    PrefManager prefManager;

    private final MutableLiveData<Boolean> loadingState = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);

        prefManager = new PrefManager(application);
        homeRepository = new HomeRepository(application);

        result = Transformations.switchMap(homeObj, obj->{
            if(obj == null){
                return AbsentLiveData.create();
            }
            return homeRepository.getHomeData(prefManager.getString(Constant.api_key));
        });

    }

    public void setHomeObj(String obj){
        homeObj.setValue(obj);
    }

    public LiveData<Resource<HomeItem>> getHomeData(){
        return result;
    }

    public boolean isLoading = false;


    //region For loading status
    public void setLoadingState(Boolean state) {
        isLoading = state;
        loadingState.setValue(state);
    }

    public MutableLiveData<Boolean> getLoadingState() {
        return loadingState;
    }

    public LiveData<List<BusinessCategoryItem>> getBusinessCategory() {
        return homeRepository.getBusinessCategory();
    }

    public LiveData<Resource<List<PersonalItem>>> getPersonalData() {
        personalObj.setValue("PS");
        return Transformations.switchMap(personalObj, obj->{
            if (obj==null){
                return AbsentLiveData.create();
            }
            return homeRepository.getPersonalItems(prefManager.getString(Constant.api_key));
        });
    }
}
