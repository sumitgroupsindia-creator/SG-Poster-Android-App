package com.sgdigitalposter.app;

import com.sgdigitalposter.app.items.OfferItem;

public class Config {


    /**
     * AppVersion
     * Current App Version
     */
    public static String APP_VERSION = "1.8.2";

    /**
     * API Related
     * Change your API URL Here
     * Your API KEY no need to add because we pass api key through firebase
     */
//    public static final String APP_API_URL = "http://172.16.17.8/brand_peak/";
//    public static String API_KEY = "d65sfd54sdf5";
//    public static final String APP_API_URL = "http://172.16.17.15/hardipbhai/BrandPeak/9125-brandpeak-backend-500/";
//    public static String API_KEY = "vIc2609p87";
//    public static final String APP_API_URL = "http://172.16.17.8/brand-peak_with_500post_10frams/";
//    public static final String APP_API_URL = "https://iqueenstudio.com/demo/"; //Demo
//    public static String API_KEY = "123456";

    public static final String APP_API_URL = "https://panel.sumitgroups.com/"; //Live
    public static String API_KEY = "036a1d38e08642d184f8c5be7fb09346";



    /**
     * Personal Section Enable/Disable
     */
    public static final boolean PERSONAL_SECTION = true;

    public static final int LIMIT = 12; // Keep More Then 10

    public static final boolean IS_DEVELOPING = true;
    public static boolean isFromNotifications = false;
    public static final int IMAGE_CACHE_LIMIT = 200;
    public static int BUSINESS_SIZE;
    public static boolean IS_CONNECTED = false;
    public static final boolean PRE_LOAD_IMAGE = false;

    public static final String EMPTY_STRING = "";
    public static final String ZERO = "0";
    public static final String ONE = "1";
    public static final String TWO = "2";
    public static final String THREE = "3";

    public static final int HOME_CATEGORY_SHOW = 8;
    public static final int HOME_BUSINESS_CATEGORY_SHOW = 8;

    public static OfferItem offerItem = new OfferItem(0, "", "", "", "", "");

    public static boolean whatsAvailable = false;
    public static String whatsappNumber = "";

    /**
     * Sticker List
     */
    public static int[] stickers = new int[] {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20
    };


    /**
     * Error Codes
     */
    public static int ERROR_CODE_10001 = 10001; // Totally No Record
    public static int ERROR_CODE_10002 = 10002; // No More Record at pagination

}
