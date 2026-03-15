package com.sgdigitalposter.app.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.sgdigitalposter.app.databinding.ActivityCategoryBinding;
import com.sgdigitalposter.app.Ads.BannerAdManager;
import com.sgdigitalposter.app.R;
import com.sgdigitalposter.app.adapters.CategoryAdapter;
import com.sgdigitalposter.app.items.CategoryItem;
import com.sgdigitalposter.app.listener.ClickListener;
import com.sgdigitalposter.app.utils.Constant;
import com.sgdigitalposter.app.utils.Util;
import com.sgdigitalposter.app.viewmodel.CategoryViewModel;

public class CategoryActivity extends AppCompatActivity implements ClickListener<CategoryItem> {

    ActivityCategoryBinding binding;
    CategoryAdapter categoryAdapter;
    CategoryViewModel categoryViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Util.applyStatusBarPadding(binding.clMain);
        BannerAdManager.showBannerAds(this, binding.llAdview);
        setUpUi();
        initViewModel();
    }

    private void setUpUi() {
        binding.toolbar.toolbarIvMenu.setBackground(getResources().getDrawable(R.drawable.ic_back));
        binding.toolbar.toolName.setText(getResources().getString(R.string.menu_category));

        binding.toolbar.toolbarIvMenu.setOnClickListener(v -> {
            onBackPressed();
        });

        categoryAdapter = new CategoryAdapter(this, this, false);
        binding.rvCategory.setAdapter(categoryAdapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            categoryViewModel.setCategoryObj("Category");
        });
    }

    private void initViewModel() {
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        categoryViewModel.setCategoryObj("Category");
        categoryViewModel.getCategories().observe(this, result -> {
            if (result.data != null) {
                binding.swipeRefresh.setRefreshing(false);
                if (result.data.size() > 0) {
                    binding.animationView.setVisibility(View.GONE);
                    categoryAdapter.setCategories(result.data);
                } else {
                    binding.animationView.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @Override
    public void onClick(CategoryItem data) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Constant.INTENT_TYPE, Constant.CATEGORY);
        intent.putExtra(Constant.INTENT_FEST_ID, data.id);
        intent.putExtra(Constant.INTENT_FEST_NAME, data.name);
        intent.putExtra(Constant.INTENT_POST_IMAGE, "");
        intent.putExtra(Constant.INTENT_VIDEO, data.video);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}