package com.sgdigitalposter.app.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsControllerCompat;

import com.sgdigitalposter.app.R;

public class WindowInsetsHelper {

    /**
     * Setup window insets handling for Android 15+ compatibility
     * This method ensures the UI doesn't overlap with system bars
     *
     * @param activity The activity to setup insets for
     * @param rootView The root view of the activity (usually the main container)
     */
    public static void setupWindowInsets(Activity activity, View rootView) {
        Window window = activity.getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Edge-to-edge
            WindowCompat.setDecorFitsSystemWindows(window, false);

            // Transparent system bars
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);

            // Control bar icons
            WindowInsetsControllerCompat insetsController =
                    new WindowInsetsControllerCompat(window, rootView);
            insetsController.setAppearanceLightStatusBars(false); // true if background is light
            insetsController.setAppearanceLightNavigationBars(false);

            // Apply padding so content isn't cut off
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        } else {
            setupTraditionalStatusBar(activity);
        }
    }


    /**
     * Setup window insets with custom top padding only (useful for activities with custom layouts)
     *
     * @param activity            The activity to setup insets for
     * @param rootView            The root view of the activity
     * @param applyTopPaddingOnly If true, only applies top padding for status bar
     */
    public static void setupWindowInsetsWithTopPadding(Activity activity, View rootView, boolean applyTopPaddingOnly) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);

            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                if (applyTopPaddingOnly) {
                    // Only apply top padding for status bar
                    v.setPadding(
                            v.getPaddingLeft(),
                            systemBars.top,
                            v.getPaddingRight(),
                            v.getPaddingBottom()
                    );
                } else {
                    // Apply all system bar paddings
                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );
                }

                return insets;
            });
        } else {
            setupTraditionalStatusBar(activity);
        }
    }

    /**
     * Setup window insets for toolbar/action bar layouts
     *
     * @param activity The activity to setup insets for
     * @param toolbar  The toolbar view that should respect system bars
     */
    public static void setupWindowInsetsForToolbar(Activity activity, View toolbar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);

            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                // Apply top margin/padding to toolbar to avoid status bar overlap
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                params.topMargin = systemBars.top;
                v.setLayoutParams(params);

                return insets;
            });
        } else {
            setupTraditionalStatusBar(activity);
        }
    }

    /**
     * Traditional status bar setup for older Android versions
     */
    private static void setupTraditionalStatusBar(Activity activity) {
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * Get status bar height programmatically
     *
     * @param activity The activity context
     * @return Status bar height in pixels
     */
    public static int getStatusBarHeight(Activity activity) {
        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * Hide system UI for immersive experience (useful for full-screen activities)
     */
    public static void hideSystemUI(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
            activity.getWindow().getInsetsController().hide(
                    android.view.WindowInsets.Type.systemBars()
            );
        } else {
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    /**
     * Show system UI
     */
    public static void showSystemUI(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), true);
            activity.getWindow().getInsetsController().show(
                    android.view.WindowInsets.Type.systemBars()
            );
        } else {
            View decorView = activity.getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }
}