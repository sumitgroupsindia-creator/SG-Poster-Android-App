package com.sgdigitalposter.app.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.sgdigitalposter.app.items.AppInfo;
import com.sgdigitalposter.app.items.AppVersion;
import com.sgdigitalposter.app.items.BusinessCategoryItem;
import com.sgdigitalposter.app.items.BusinessItem;
import com.sgdigitalposter.app.items.BusinessSubCategoryItem;
import com.sgdigitalposter.app.items.CategoryItem;
import com.sgdigitalposter.app.items.CustomCategory;
import com.sgdigitalposter.app.items.CustomModel;
import com.sgdigitalposter.app.items.DynamicFrameItem;
import com.sgdigitalposter.app.items.EarningItem;
import com.sgdigitalposter.app.items.FestivalItem;
import com.sgdigitalposter.app.items.FrameCategoryItem;
import com.sgdigitalposter.app.items.HomeItem;
import com.sgdigitalposter.app.items.ItemVcard;
import com.sgdigitalposter.app.items.LanguageItem;
import com.sgdigitalposter.app.items.MainStrModel;
import com.sgdigitalposter.app.items.NewsItem;
import com.sgdigitalposter.app.items.OfferItem;
import com.sgdigitalposter.app.items.PersonalItem;
import com.sgdigitalposter.app.items.PostItem;
import com.sgdigitalposter.app.items.ProductCatItem;
import com.sgdigitalposter.app.items.ProductItem;
import com.sgdigitalposter.app.items.ProductModel;
import com.sgdigitalposter.app.items.ReferDetail;
import com.sgdigitalposter.app.items.StickerCategory;
import com.sgdigitalposter.app.items.StickerItem;
import com.sgdigitalposter.app.items.StickerModel;
import com.sgdigitalposter.app.items.StoryItem;
import com.sgdigitalposter.app.items.SubjectItem;
import com.sgdigitalposter.app.items.SubsPlanItem;
import com.sgdigitalposter.app.items.UserFrame;
import com.sgdigitalposter.app.items.UserItem;
import com.sgdigitalposter.app.items.UserLogin;

@Database(entities = {StoryItem.class, FestivalItem.class, CategoryItem.class, PostItem.class,
        LanguageItem.class, UserItem.class,
        UserLogin.class, BusinessItem.class, SubsPlanItem.class,
        SubjectItem.class, NewsItem.class, AppVersion.class, AppInfo.class, CustomCategory.class, HomeItem.class,
        BusinessCategoryItem.class, CustomModel.class, UserFrame.class, ItemVcard.class,
        StickerItem.class, StickerCategory.class, StickerModel.class, MainStrModel.class, OfferItem.class,
        DynamicFrameItem.class, ProductCatItem.class, ProductItem.class, ProductModel.class, ReferDetail.class,
        EarningItem.class, BusinessSubCategoryItem.class, PersonalItem.class, FrameCategoryItem.class}, version = 39, exportSchema = false)
@TypeConverters({DataConverters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "festival_database";

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                INSTANCE = Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .build();
            }
        }
        return INSTANCE;
    }

    public abstract StoryDao getStoryDao();

    public abstract FestivalDao getFestivalDao();

    public abstract CategoryDao getCategoryDao();

    public abstract PostDao getPostDao();

    public abstract LanguageDao getLanguageDao();

    public abstract UserDao getUserDao();

    public abstract BusinessDao getBusinessDao();

    public abstract SubsPlanDao getSubsPlanDao();

    public abstract NewsDao getNewsDao();

    public abstract UserLoginDao getUserLoginDao();

    public abstract CustomCategoryDao getCustomCategoryDao();

    public abstract HomeDao getHomeDao();

    public abstract VCardDao getVCardDao();

    public abstract FrameDao getFrameDao();

    public abstract ProductDao getProductDao();
}

