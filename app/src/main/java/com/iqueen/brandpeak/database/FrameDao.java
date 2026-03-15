package com.sgdigitalposter.app.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.sgdigitalposter.app.items.DynamicFrameItem;
import com.sgdigitalposter.app.items.FrameCategoryItem;

import java.util.List;

@Dao
public interface FrameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DynamicFrameItem> frameItemList);

    @Query("SELECT * FROM dynamic_frame WHERE aspectRatio =:ratio AND category_name = :category")
    LiveData<List<DynamicFrameItem>> getFrameByAspectRatio(String ratio, String category);

    @Query("SELECT * FROM dynamic_frame WHERE aspectRatio =:ratio")
    LiveData<List<DynamicFrameItem>> getFrameByAspectRatio(String ratio);

    @Query("DELETE FROM dynamic_frame")
    void deleteByRatio();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategoryAll(List<FrameCategoryItem> frameCategoryItemList);

    @Query("SELECT * FROM frame_category")
    LiveData<List<FrameCategoryItem>> getFrameCategory();

    @Query("DELETE FROM frame_category")
    void deleteFrameCategory();

}
