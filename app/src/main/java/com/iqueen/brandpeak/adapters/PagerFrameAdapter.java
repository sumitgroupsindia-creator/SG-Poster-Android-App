package com.sgdigitalposter.app.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.sgdigitalposter.app.items.DynamicFrameItem;
import com.sgdigitalposter.app.ui.fragments.LoadFrameFragment;

import java.util.ArrayList;
import java.util.List;

public class PagerFrameAdapter extends FragmentPagerAdapter {

    List<DynamicFrameItem> list = new ArrayList<>();
    float wr = 1.0f;
    float hr = 1.0f;

    public interface OnLogoSelect{
        void sticker(String path);
    }

    public PagerFrameAdapter(@NonNull FragmentManager fm, List<DynamicFrameItem> list, float wr, float hr) {
        super(fm);
        this.list = list;
        this.wr = wr;
        this.hr = hr;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return new LoadFrameFragment(list.get(position), wr, hr);
    }

    @Override
    public int getCount() {
        return list.size();
    }

}
