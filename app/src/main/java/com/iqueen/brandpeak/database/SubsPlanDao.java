package com.sgdigitalposter.app.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.sgdigitalposter.app.items.SubsPlanItem;

import java.util.List;

@Dao
public interface SubsPlanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SubsPlanItem subPlanItem);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SubsPlanItem> subPlanItem);

    @Query("SELECT *FROM subs_plan")
    LiveData<List<SubsPlanItem>> getAllPlan();

    @Query("SELECT *FROM subs_plan WHERE id = :id")
    SubsPlanItem getDataById(String id);

    @Query("DELETE FROM subs_plan")
    void deleteData();

    @Query("UPDATE subs_plan SET gPrice = :price WHERE id = :id")
    void updateData(String price, String id);
}
