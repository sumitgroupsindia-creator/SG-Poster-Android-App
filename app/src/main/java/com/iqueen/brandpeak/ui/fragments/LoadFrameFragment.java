package com.sgdigitalposter.app.ui.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sgdigitalposter.app.Config;
import com.sgdigitalposter.app.adapters.FrameAdapter;
import com.sgdigitalposter.app.binding.GlideBinding;
import com.sgdigitalposter.app.databinding.ActivityDetailBinding;
import com.sgdigitalposter.app.databinding.FragmentLoadFrameBinding;
import com.sgdigitalposter.app.editor.PosterActivity;
import com.sgdigitalposter.app.items.BusinessItem;
import com.sgdigitalposter.app.items.DynamicFrameItem;
import com.sgdigitalposter.app.ui.stickers.ElementInfo;
import com.sgdigitalposter.app.ui.stickers.RelStickerView;
import com.sgdigitalposter.app.ui.stickers.Sticker_info;
import com.sgdigitalposter.app.R;
import com.sgdigitalposter.app.ui.stickers.TextInfo;
import com.sgdigitalposter.app.ui.stickers.ViewIdGenerator;
import com.sgdigitalposter.app.ui.stickers.text.AutofitTextRel;
import com.sgdigitalposter.app.ui.stickers.text.TextSTRInfo;
import com.sgdigitalposter.app.utils.Constant;
import com.sgdigitalposter.app.utils.PrefManager;
import com.sgdigitalposter.app.utils.StorageUtils;
import com.sgdigitalposter.app.utils.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class LoadFrameFragment extends Fragment {


    Activity context;
    DynamicFrameItem model;
//    FrameAdapter.OnFrameSelect onOverlaySelected;
    ArrayList<Sticker_info> stickerInfoArrayList = new ArrayList<>();
    ArrayList<Sticker_info> strPostList = new ArrayList<>();
    ArrayList<TextSTRInfo> textInfoArrayList = new ArrayList<>();
    ArrayList<TextSTRInfo> textPostList = new ArrayList<>();
    RelativeLayout relativeLayout;
    HashMap<Integer, Object> txtShapeList;
    float screenWidth;
    float screenHeight;
    float wr = 1.0f;
    float hr = 1.0f;
    public String realX;
    public String realY;
    public String calcWidth = "";
    public String calcHeight = "";
    public JSONObject bgObj;
    String fontName;
    int tAlpha = 100;
    private String ratio;
    public ArrayList<Pair<Long, View>> mItemArray;
    int sizeFull = 0;

    int tColor = -1;
    int bgAlpha = 0;
    int bgColor = ViewCompat.MEASURED_STATE_MASK;
    String bgDrawable = "0";
    int outerColor = 0;
    int outerSize = 0;
    int leftRightShadow = 0;
    int topBottomShadow = 0;
    int shadowColor = ViewCompat.MEASURED_STATE_MASK;
    int shadowProg = 0;
    float rotation = 0.0f;
    private BusinessItem businessItem;
    FragmentLoadFrameBinding binding;
    public LoadFrameFragment(DynamicFrameItem iArr, float wr, float hr) {
        this.model = iArr;
        this.wr = wr;
        this.hr = hr;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoadFrameBinding.inflate(getLayoutInflater());
        //prefManager = new PrefManager(requireActivity());
        context = getActivity();
        ratio = "1:1";
        businessItem = (BusinessItem) requireActivity().getIntent().getSerializableExtra(Constant.INTENT_BUSINESS_ITEM);
        setUpFrameData(model);


        return binding.getRoot();
    }

    private void setUpFrameData(DynamicFrameItem dynamicFrameItem) {
        Log.e("SB", "setUpFrameData dynamicFrameItem :" + dynamicFrameItem);

        if (dynamicFrameItem != null) {

            if (dynamicFrameItem.name.equals("USER") && dynamicFrameItem.aspectRatio.equals("USER")) {
                stickerInfoArrayList.clear();
                mItemArray.clear();
                textInfoArrayList.clear();

                binding.ivFrame.setVisibility(VISIBLE);
                Log.e("SB", "setUpFrameData thumbnail:" + dynamicFrameItem.thumbnail);
                GlideBinding.bindImage(binding.ivFrame, dynamicFrameItem.thumbnail);

            } else {
                binding.ivFrame.setVisibility(GONE);
                try {
                    JSONObject json = new JSONObject(dynamicFrameItem.frameData);
                    JSONArray layers = json.getJSONArray("layers");

                    // Create a new JSONArray without null values
                    JSONArray cleanedLayers = new JSONArray();

                    for (int i = 0; i < layers.length(); i++) {
                        Object layerObject = layers.opt(i);
                        if (layerObject != null && !JSONObject.NULL.equals(layerObject)) {
                            cleanedLayers.put(layerObject);
                        }
                    }
                    stickerInfoArrayList.clear();
                    mItemArray.clear();
                    textInfoArrayList.clear();

                    for (int i = 0; i < cleanedLayers.length(); i++) {
                        Object layerObject = cleanedLayers.get(i);

                        Log.d("test", "setUpFrameData: " + layerObject);

                        if (layerObject instanceof JSONObject) {
                            JSONObject jsonObject1 = (JSONObject) layerObject;
                            processJson(i, jsonObject1, dynamicFrameItem.name);
                        } else {
                            Log.w("test", "setUpFrameData: Unexpected type at index " + i);
                        }
                    }

                    LoadStickersAsync loadStickersAsync = new LoadStickersAsync();
                    loadStickersAsync.execute();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void setCustomData() {
        if (requireActivity().getIntent().hasExtra(Constant.INTENT_TYPE)) {
            if (requireActivity().getIntent().getStringExtra(Constant.INTENT_TYPE).equals(Constant.CUSTOM_EDITABLE)) {

                LoadCustomAsync loadCustomAsync = new LoadCustomAsync();
                loadCustomAsync.execute();
            }
        }

    }
    private class LoadStickersAsync extends AsyncTask<String, String, Boolean> {
        private LoadStickersAsync() {
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();
        }

        public Boolean doInBackground(String... strArr) {
            ArrayList<ElementInfo> stickerArrayList;
            ArrayList<TextInfo> newtextInfoArrayList;
            String str;

            newtextInfoArrayList = new ArrayList<>();
            stickerArrayList = new ArrayList<>();


            for (int i = 0; i < stickerInfoArrayList.size(); i++) {
                int newWidht = getNewSTRWidth(Float.valueOf(stickerInfoArrayList.get(i).getSt_x_pos()).floatValue(),
                        Float.valueOf(stickerInfoArrayList.get(i).getSt_width()).floatValue());
                int newHeight = getNewSTRHeight(Float.valueOf(stickerInfoArrayList.get(i).getSt_y_pos()).floatValue(),
                        Float.valueOf(stickerInfoArrayList.get(i).getSt_height()).floatValue());
//                int i2 = newWidht < 10 ? 20 : (newWidht <= 10 || newWidht > 20) ? newWidht : 35;
//                int i3 = newHeight < 10 ? 20 : (newHeight <= 10 || newHeight > 20) ? newHeight : 35;
                int i2 = newWidht;
                int i3 = newHeight;
                if (stickerInfoArrayList.get(i).getSt_field2() != null) {
                    str = stickerInfoArrayList.get(i).getSt_field2();
                } else {
                    str = "";
                }
                float parseInt = (stickerInfoArrayList.get(i).getSt_rotation() == null ||
                        stickerInfoArrayList.get(i).getSt_rotation().equals("")) ? 0.0f :
                        (float) Integer.parseInt(stickerInfoArrayList.get(i).getSt_rotation());

                float xpos = getXpos(Float.valueOf(stickerInfoArrayList.get(i).getSt_x_pos()).floatValue());
                float ypos = getYpos(Float.valueOf(stickerInfoArrayList.get(i).getSt_y_pos()).floatValue());
                ElementInfo el = new ElementInfo(stickerInfoArrayList.get(i).getName(), 0, xpos,
                        ypos,
                        i2,
                        i3,
                        parseInt, 0.0f, "",
                        "STICKER",
                        Integer.parseInt(stickerInfoArrayList.get(i).getSt_order()),
                        0, 255, 0, 0, 0, 0,
                        stickerInfoArrayList.get(i).getSt_image(), "colored",
                        1, 0, str, "", "", null, null);

                stickerArrayList.add(el);
            }
            for (int i5 = 0; i5 < textInfoArrayList.size(); i5++) {

                TextSTRInfo tsInfo = textInfoArrayList.get(i5);

                String text = textInfoArrayList.get(i5).getText();


                String font_family = textInfoArrayList.get(i5).getFont_family();


                int parseColor = Color.parseColor(textInfoArrayList.get(i5).getTxt_color());
                float xpos2 = getXpos(Float.valueOf(textInfoArrayList.get(i5).getTxt_x_pos()).floatValue());
                float ypos = getYpos(Float.valueOf(textInfoArrayList.get(i5).getTxt_y_pos()).floatValue());
                int newWidht2 = getNewSTRWidth(Float.valueOf(textInfoArrayList.get(i5).getTxt_x_pos()).floatValue(), Float.valueOf(textInfoArrayList.get(i5).getTxt_width()).floatValue());
                int newHeit2 = getNewSTRHeight(Float.valueOf(textInfoArrayList.get(i5).getTxt_y_pos()).floatValue(),
                        Float.valueOf(textInfoArrayList.get(i5).getTxt_height()).floatValue());

                boolean isBorder = false;
                if (textInfoArrayList.get(i5).getLineSize() != 0) {
                    isBorder = true;
                }

                boolean isShadow = false;
                if (textInfoArrayList.get(i5).getSdDistance() != -11111) {
                    isShadow = true;
                }

                TextInfo ti = new TextInfo(textInfoArrayList.get(i5).getName(), 0, text, font_family, parseColor, 100,
                        isShadow ? Color.parseColor(tsInfo.getSdColor()) : 0,
                        isShadow ? tsInfo.getSdBlur() : 0,
                        "0",
                        ViewCompat.MEASURED_STATE_MASK, 0,
                        xpos2,
                        ypos,
                        newWidht2,
                        newHeit2,
                        Float.parseFloat(textInfoArrayList.get(i5).getTxt_rotation()),
                        "TEXT", Integer.parseInt(textInfoArrayList.get(i5).getTxt_order()),
                        0, 0, 0, 0, 0, "",
                        "", "",
                        isShadow ? Util.getXDistance(tsInfo.getSdDistance(), tsInfo.getSdAngle()) : 0.0f,
                        isShadow ? Util.getYDistance(tsInfo.getSdDistance(), tsInfo.getSdAngle()) : 0.0f,
                        isBorder ? tsInfo.getLineSize() : 0,
                        isBorder ? Color.parseColor(tsInfo.getLineColor()) : 0,
                        isBorder ? tsInfo.getLineOpacity() : 100,
                        textInfoArrayList.get(i5).getJustification(), textInfoArrayList.get(i5).getUppercase());
                newtextInfoArrayList.add(ti);
            }


            txtShapeList.clear();
            Iterator<TextInfo> it = newtextInfoArrayList.iterator();
            while (it.hasNext()) {
                TextInfo next = it.next();
                txtShapeList.put(Integer.valueOf(next.getORDER()), next);
            }
            Iterator<ElementInfo> it2 = stickerArrayList.iterator();
            while (it2.hasNext()) {
                ElementInfo next2 = it2.next();
                txtShapeList.put(Integer.valueOf(next2.getORDER()), next2);
            }

            ArrayList arrayList = new ArrayList(txtShapeList.keySet());
            Collections.sort(arrayList);
            int size = arrayList.size();

            for (int i = 0; i < size; i++) {
                Object obj = txtShapeList.get(arrayList.get(i));
                if (obj instanceof ElementInfo) {
                    ElementInfo elementInfo = (ElementInfo) obj;
                    String stkr_path = elementInfo.getSTKR_PATH();
                    if (stkr_path.equals("")) {
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                RelStickerView stickerView = new RelStickerView(requireActivity(), false);
                                binding.txtStkrRel.addView(stickerView);
                                stickerView.optimizeScreen(screenWidth, screenHeight);
                                stickerView.setMainLayoutWH((float) binding.mainRel.getWidth(), (float) binding.mainRel.getHeight());
                                stickerView.setComponentInfo(elementInfo);
                                stickerView.setId(ViewIdGenerator.generateViewId());
                                stickerView.optimize(wr, hr);
                                stickerView.setBorderVisibility(false);
                                if (elementInfo.getName().contains("frame")) {
                                    stickerView.isMultiTouchEnabled = stickerView.setDefaultTouchListener(false);
                                }
                                sizeFull++;
                            }
                        });
                    } else {
                        File file2 = new File(stkr_path);
                        if (file2.exists()) {
                            requireActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    RelStickerView stickerView2 = new RelStickerView(requireActivity(), false);
                                    binding.txtStkrRel.addView(stickerView2);
                                    stickerView2.optimizeScreen(screenWidth, screenHeight);
                                    stickerView2.setMainLayoutWH((float) binding.mainRel.getWidth(), (float) binding.mainRel.getHeight());
                                    stickerView2.setComponentInfo(elementInfo);
                                    stickerView2.setId(ViewIdGenerator.generateViewId());
                                    stickerView2.optimize(wr, hr);
                                    stickerView2.setBorderVisibility(false);
                                    if (elementInfo.getName().contains("frame")) {
                                        stickerView2.isMultiTouchEnabled = stickerView2.setDefaultTouchListener(false);
                                    }
                                    sizeFull++;
                                }
                            });
                        } else if (file2.getName().replace(".png", "").length() < 7) {
//                                dialogShow = false;
//                                new SaveStickersAsync(obj).execute(stkr_path);
                        } else {
                            sizeFull++;
                        }
                    }
                } else {
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AutofitTextRel autofitTextRel = new AutofitTextRel(requireActivity());
                            binding.txtStkrRel.addView(autofitTextRel);
                            TextInfo textInfo = (TextInfo) obj;


                            if (textInfo.getFONT_NAME() != null) {

                                setTextFonts(textInfo.getFONT_NAME());

                            }

                            if (!textInfo.getJustification().equals("")) {
                                if (textInfo.getJustification().equals("center")) {
                                    autofitTextRel.setCenterAlignMent();
                                } else if (textInfo.getJustification().equals("left")) {
                                    autofitTextRel.setLeftAlignMent();
                                } else if (textInfo.getJustification().equals("right")) {
                                    autofitTextRel.setRightAlignMent();
                                }
                            }

                            if (!textInfo.getUPPERCASE().equals("")) {
                                if (textInfo.getUPPERCASE().equals("1")) {
                                    autofitTextRel.setCapitalFont();
                                } else if (textInfo.getUPPERCASE().equals("0")) {
                                    autofitTextRel.setLowerFont();
                                }
                            }

                            autofitTextRel.setTextInfo(textInfo, false);
                            autofitTextRel.setId(ViewIdGenerator.generateViewId());
                            autofitTextRel.optimize(wr, hr);
                            autofitTextRel.setBorderVisibility(false);
                            fontName = textInfo.getFONT_NAME();
                            tColor = textInfo.getTEXT_COLOR();
                            shadowColor = textInfo.getSHADOW_COLOR();
                            shadowProg = textInfo.getSHADOW_PROG();
                            tAlpha = textInfo.getTEXT_ALPHA();
                            bgDrawable = textInfo.getBG_DRAWABLE();
                            bgAlpha = textInfo.getBG_ALPHA();
                            rotation = textInfo.getROTATION();
                            bgColor = textInfo.getBG_COLOR();
                            outerColor = textInfo.getOutLineColor();
                            outerSize = textInfo.getOutLineSize();
                            leftRightShadow = (int) textInfo.getLeftRighShadow();
                            topBottomShadow = (int) textInfo.getTopBottomShadow();
                            topBottomShadow = (int) textInfo.getTopBottomShadow();
                            sizeFull++;
                        }
                    });
                }
            }

            if (txtShapeList.size() == sizeFull) {
                try {
//                    dialogIs.dismiss();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }

            return true;
        }

        @Override
        public void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);
//            saveBitmapUndu();

        }
    }

    private class LoadCustomAsync extends AsyncTask<String, String, Boolean> {
        private LoadCustomAsync() {
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();
        }

        public Boolean doInBackground(String... strArr) {
            ArrayList<ElementInfo> stickerArrayList;
            ArrayList<TextInfo> newtextInfoArrayList;
            String str;

            newtextInfoArrayList = new ArrayList<>();
            stickerArrayList = new ArrayList<>();

            for (int i = 0; i < strPostList.size(); i++) {
                int newWidht = getNewSTRWidth(Float.valueOf(strPostList.get(i).getSt_x_pos()).floatValue(),
                        Float.valueOf(strPostList.get(i).getSt_width()).floatValue());
                int newHeight = getNewSTRHeight(Float.valueOf(strPostList.get(i).getSt_y_pos()).floatValue(),
                        Float.valueOf(strPostList.get(i).getSt_height()).floatValue());
                int i2 = newWidht;
                int i3 = newHeight;
                if (strPostList.get(i).getSt_field2() != null) {
                    str = strPostList.get(i).getSt_field2();
                } else {
                    str = "";
                }
                float parseInt = (strPostList.get(i).getSt_rotation() == null ||
                        strPostList.get(i).getSt_rotation().equals("")) ? 0.0f :
                        (float) Integer.parseInt(strPostList.get(i).getSt_rotation());

                float xpos = getXpos(Float.valueOf(strPostList.get(i).getSt_x_pos()).floatValue());
                float ypos = getYpos(Float.valueOf(strPostList.get(i).getSt_y_pos()).floatValue());
                ElementInfo el = new ElementInfo(strPostList.get(i).getName(), 0, xpos,
                        ypos,
                        i2,
                        i3,
                        parseInt, 0.0f, "",
                        "STICKER",
                        Integer.parseInt(strPostList.get(i).getSt_order()),
                        0, 255, 0, 0, 0, 0,
                        strPostList.get(i).getSt_image(), "colored",
                        1, 0, str, "", "", null, null);

                stickerArrayList.add(el);
            }
            for (int i5 = 0; i5 < textPostList.size(); i5++) {

                TextSTRInfo tsInfo = textPostList.get(i5);

                String text = textPostList.get(i5).getText();


                String font_family = textPostList.get(i5).getFont_family();


                int parseColor = Color.parseColor(textPostList.get(i5).getTxt_color());
                float xpos2 = getXpos(Float.valueOf(textPostList.get(i5).getTxt_x_pos()).floatValue());
                float ypos = getYpos(Float.valueOf(textPostList.get(i5).getTxt_y_pos()).floatValue());
                int newWidht2 = getNewSTRWidth(Float.valueOf(textPostList.get(i5).getTxt_x_pos()).floatValue(),
                        Float.valueOf(textPostList.get(i5).getTxt_width()).floatValue());
                int newHeit2 = getNewSTRHeight(Float.valueOf(textPostList.get(i5).getTxt_y_pos()).floatValue(),
                        Float.valueOf(textPostList.get(i5).getTxt_height()).floatValue());

                boolean isBorder = false;
                if (textPostList.get(i5).getLineSize() != 0) {
                    isBorder = true;
                }

                boolean isShadow = false;
                if (textPostList.get(i5).getSdDistance() != -11111) {
                    isShadow = true;
                }

                TextInfo ti = new TextInfo(textPostList.get(i5).getName(), 0, text, font_family, parseColor, 100,
                        isShadow ? Color.parseColor(tsInfo.getSdColor()) : 0,
                        isShadow ? tsInfo.getSdBlur() : 0,
                        "0",
                        ViewCompat.MEASURED_STATE_MASK, 0,
                        xpos2,
                        ypos,
                        newWidht2,
                        newHeit2,
                        Float.parseFloat(textPostList.get(i5).getTxt_rotation()),
                        "TEXT", Integer.parseInt(textPostList.get(i5).getTxt_order()),
                        0, 0, 0, 0, 0, "",
                        "", "",
                        isShadow ? Util.getXDistance(tsInfo.getSdDistance(), tsInfo.getSdAngle()) : 0.0f,
                        isShadow ? Util.getYDistance(tsInfo.getSdDistance(), tsInfo.getSdAngle()) : 0.0f,
                        isBorder ? tsInfo.getLineSize() : 0,
                        isBorder ? Color.parseColor(tsInfo.getLineColor()) : 0,
                        isBorder ? tsInfo.getLineOpacity() : 100,
                        textPostList.get(i5).getJustification(), textPostList.get(i5).getUppercase());
                newtextInfoArrayList.add(ti);
                Util.showLog("Text: " + ti.toString());
            }

            txtShapeList.clear();
            Iterator<TextInfo> it = newtextInfoArrayList.iterator();
            while (it.hasNext()) {
                TextInfo next = it.next();
                txtShapeList.put(Integer.valueOf(next.getORDER()), next);
            }
            Iterator<ElementInfo> it2 = stickerArrayList.iterator();
            while (it2.hasNext()) {
                ElementInfo next2 = it2.next();
                txtShapeList.put(Integer.valueOf(next2.getORDER()), next2);
            }

            ArrayList arrayList = new ArrayList(txtShapeList.keySet());
            Collections.sort(arrayList);
            int size = arrayList.size();


            for (int i = 0; i < size; i++) {
                Object obj = txtShapeList.get(arrayList.get(i));
                if (obj instanceof ElementInfo) {
                    ElementInfo elementInfo = (ElementInfo) obj;
                    String stkr_path = elementInfo.getSTKR_PATH();
                    if (stkr_path.equals("")) {
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                RelStickerView stickerView = new RelStickerView(requireActivity(), false);
                                binding.txtStkrRel.addView(stickerView);
                                stickerView.optimizeScreen(screenWidth, screenHeight);
                                stickerView.setMainLayoutWH((float) binding.mainRel.getWidth(), (float) binding.mainRel.getHeight());
                                stickerView.setComponentInfo(elementInfo);
                                stickerView.setId(ViewIdGenerator.generateViewId());
                                stickerView.optimize(wr, hr);

                                stickerView.setBorderVisibility(false);
                                if (elementInfo.getName().contains("frame")) {
                                    stickerView.isMultiTouchEnabled = stickerView.setDefaultTouchListener(false);
                                }
                                sizeFull++;
                            }
                        });
                    } else {
                        File file2 = new File(stkr_path);
                        if (file2.exists()) {
                            requireActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    RelStickerView stickerView2 = new RelStickerView(requireActivity(), false);
                                    binding.txtStkrRel.addView(stickerView2);
                                    stickerView2.optimizeScreen(screenWidth, screenHeight);
                                    stickerView2.setMainLayoutWH((float) binding.mainRel.getWidth(), (float) binding.mainRel.getHeight());
                                    stickerView2.setComponentInfo(elementInfo);
                                    stickerView2.setId(ViewIdGenerator.generateViewId());
                                    stickerView2.optimize(wr, hr);
                                    stickerView2.setBorderVisibility(false);
                                    if (elementInfo.getName().contains("frame")) {
                                        stickerView2.isMultiTouchEnabled = stickerView2.setDefaultTouchListener(false);
                                    }
                                    sizeFull++;
                                }
                            });
                        } else if (file2.getName().replace(".png", "").length() < 7) {
//                                dialogShow = false;
//                                new SaveStickersAsync(obj).execute(stkr_path);
                        } else {
                            sizeFull++;
                        }
                    }
                } else {

                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AutofitTextRel autofitTextRel = new AutofitTextRel(requireActivity());
                            binding.txtStkrRel.addView(autofitTextRel);
                            TextInfo textInfo = (TextInfo) obj;


                            if (textInfo.getFONT_NAME() != null) {

                                setTextFonts(textInfo.getFONT_NAME());

                            }

                            if (!textInfo.getJustification().equals("")) {
                                if (textInfo.getJustification().equals("center")) {
                                    autofitTextRel.setCenterAlignMent();
                                } else if (textInfo.getJustification().equals("left")) {
                                    autofitTextRel.setLeftAlignMent();
                                } else if (textInfo.getJustification().equals("right")) {
                                    autofitTextRel.setRightAlignMent();
                                }
                            }

                            if (!textInfo.getUPPERCASE().equals("")) {
                                if (textInfo.getUPPERCASE().equals("1")) {
                                    autofitTextRel.setCapitalFont();
                                } else if (textInfo.getUPPERCASE().equals("0")) {
                                    autofitTextRel.setLowerFont();
                                }
                            }

                            autofitTextRel.setTextInfo(textInfo, false);
                            autofitTextRel.setId(ViewIdGenerator.generateViewId());
                            autofitTextRel.optimize(wr, hr);
                            autofitTextRel.setBorderVisibility(false);
                            fontName = textInfo.getFONT_NAME();
                            tColor = textInfo.getTEXT_COLOR();
                            shadowColor = textInfo.getSHADOW_COLOR();
                            shadowProg = textInfo.getSHADOW_PROG();
                            tAlpha = textInfo.getTEXT_ALPHA();
                            bgDrawable = textInfo.getBG_DRAWABLE();
                            bgAlpha = textInfo.getBG_ALPHA();
                            rotation = textInfo.getROTATION();
                            bgColor = textInfo.getBG_COLOR();
                            outerColor = textInfo.getOutLineColor();
                            outerSize = textInfo.getOutLineSize();
                            leftRightShadow = (int) textInfo.getLeftRighShadow();
                            topBottomShadow = (int) textInfo.getTopBottomShadow();
                            sizeFull++;
                        }
                    });
                }
            }

            if (txtShapeList.size() == sizeFull) {
                try {
//                    dialogIs.dismiss();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }

            return true;
        }

        @Override
        public void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);
//            saveBitmapUndu();

        }
    }
    private float getNewHeight(int i, int i2, float f, float f2) {
        return (((float) i2) * f) / ((float) i);
    }

    private float getNewWidth(int i, int i2, float f, float f2) {
        return (((float) i) * f2) / ((float) i2);
    }

    public int getNewSTRWidth(float f, float f2) {
        return (int) ((((float) binding.mainRel.getWidth()) * (f2 - f)) / 100.0f);
    }

    public int getNewSTRHeight(float f, float f2) {
        return (int) ((((float) binding.mainRel.getHeight()) * (f2 - f)) / 100.0f);
    }

    public float getXpos(float f) {
        return (((float) binding.mainRel.getWidth()) * f) / 100.0f;
    }

    public float getYpos(float f) {
        return (((float) binding.mainRel.getHeight()) * f) / 100.0f;
    }
    public void setTextFonts(String str) {
        this.fontName = str;
        int childCount = binding.txtStkrRel.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = binding.txtStkrRel.getChildAt(i);
            if (childAt instanceof AutofitTextRel) {
                AutofitTextRel autofitTextRel = (AutofitTextRel) childAt;
                if (autofitTextRel.getBorderVisibility()) {

                    autofitTextRel.setTextFont(str);

                }
            }
        }
    }
    private void processJson(int i, JSONObject jsonObject1, String name) throws JSONException {

        String type = jsonObject1.getString("type");
        String lName = jsonObject1.getString("name");
        String width = jsonObject1.getString("width");
        String height = jsonObject1.getString("height");

        String x = jsonObject1.getString("x");
        String y = jsonObject1.getString("y");

        realX = x;
        realY = y;

        calcWidth = "";
        calcHeight = "";

        float templateRealWidth = Float.parseFloat(ratio.split(":")[0]);
        float templateRealHeight = Float.parseFloat(ratio.split(":")[1]);

        if (bgObj != null) {
            templateRealWidth = Float.parseFloat(bgObj.getString("width"));
            templateRealHeight = Float.parseFloat(bgObj.getString("height"));
        }

        if (i == 0) {
            calcWidth = width;
            calcHeight = height;
            bgObj = jsonObject1;

        } else {

            realX = String.valueOf((Float.parseFloat(x) * 100)
                    / Float.parseFloat(bgObj.getString("width")));
            realY = String.valueOf((Float.parseFloat(y) * 100)
                    / Float.parseFloat(bgObj.getString("height")));

            calcWidth = String.valueOf(Float.parseFloat(width) * 100
                    / templateRealWidth + Float.parseFloat(realX));
            calcHeight = String.valueOf(Float.parseFloat(height) * 100
                    / templateRealHeight + Float.parseFloat(realY));

        }

        if (type != null) {

            if (type.contains("image")) {


                String stickerUrl = "uploads/template/" + name + ""
                        + jsonObject1.getString("src").replace("..", "");

                if (jsonObject1.getString("name").equals("logo")) {
                    String str = saveSticker(name, businessItem.name,
                            businessItem.logo);
                } else {
//                    if (prefManager.getString(Constant.DIGITAL_ENABLE).equals(Config.ONE)) {
//                        String str = saveSticker(name, jsonObject1.getString("name"),
//                                prefManager.getString(Constant.DIGITAL_END_URL) + stickerUrl);
//                    } else {
//                        String str = saveSticker(name, jsonObject1.getString("name"),
//                                Config.APP_API_URL + stickerUrl);
//                    }
                }
                String directory = new StorageUtils(requireActivity()).getPackageStorageDir("/." + name + "/").getAbsolutePath();

                String stickerPath;
                if (jsonObject1.getString("name").equals("logo")) {
                    stickerPath = directory + "/" + businessItem.name + ".png";
                } else {
                    stickerPath = directory + "/" + jsonObject1.getString("name") + ".png";
                }
                String rotation = jsonObject1.has("rotation") ? jsonObject1.getString("rotation") : "0";

                // Bg
                if (i == 0) {

//                    bgUrl = stickerUrl;

                } else {
                    Sticker_info info = new Sticker_info();
                    info.setName(lName);
                    info.setSticker_id(String.valueOf(i));
                    info.setSt_image(stickerPath);
                    info.setSt_order(String.valueOf(i));
                    info.setSt_height(calcHeight);
                    info.setSt_width(calcWidth);
                    info.setSt_x_pos(realX);
                    info.setSt_y_pos(realY);
                    info.setSt_rotation(rotation);
                    stickerInfoArrayList.add(info);
                }
            } else if (type.contains("text")) {

                String color = jsonObject1.getString("color");
                String font = jsonObject1.getString("font");

                String layerName = jsonObject1.getString("name");
                String text = jsonObject1.getString("text");
                if (layerName.equals("name")) {
                    text = businessItem.name;
                } else if (layerName.equals("mobile")) {
                    text = businessItem.phone;
                } else if (layerName.equals("email")) {
                    text = businessItem.email;
                } else if (layerName.equals("website") && !businessItem.website.equals("DEMO")) {
                    text = businessItem.website;
                } else if (layerName.equals("address")) {
                    text = businessItem.address;
                }

//                if (businessItem.website.equals("DEMO")) {
//                    binding.cbWebsite.setEnabled(false);
//                    binding.cbWebsite.setChecked(false);
//                } else {
//                    binding.cbWebsite.setEnabled(true);
//                    binding.cbWebsite.setChecked(true);
//                }

                String size = jsonObject1.getString("size");

                if (!jsonObject1.has("rotation")) {

                    jsonObject1.put("size", Integer.parseInt(size) + 15);
                    jsonObject1.put("y", Integer.parseInt(y) + 5);
                    y = jsonObject1.getString("y");

                    size = jsonObject1.getString("size");

                    String calSizeHeight = String.valueOf(Float.parseFloat(size) - Float.parseFloat(height)).replace("-", "");

                    String calRealY = String.valueOf(Float.parseFloat(y) - Float.parseFloat(calSizeHeight));

                    realY = String.valueOf((Float.parseFloat(calRealY) * 100) / templateRealHeight);

                    calcHeight = String.valueOf(Float.parseFloat(size) * 100 / templateRealHeight + Float.parseFloat(realY));
                }


                String directory = new StorageUtils(requireActivity()).getPackageStorageDir("/."
                        + "font" + "/").getAbsolutePath();

                File file = new File(directory + "/" + font + ".ttf");
                Util.showLog("FILE: " + file.exists() + " FILE : " + file.getName());
                if (!file.exists()) {
                    file = new File(directory + "/" + font + ".TTF");
                    if (!file.exists()) {
                        file = new File(directory + "/" + font + ".otf");
                        if (!file.exists()) {
                            LoadFonts loadFonts = new LoadFonts(name, directory, font);
                            loadFonts.execute();
                        }
                    }
                } else {

                }

                String rotation = "0";
                if (jsonObject1.has("rotation")) {
                    rotation = jsonObject1.getString("rotation");
                }

                String justification = "";
                if (jsonObject1.has("justification")) {
                    justification = jsonObject1.getString("justification");
                }
                String upperCase = "0";
                if (jsonObject1.has("uppercase")) {
                    boolean upperCaseb = jsonObject1.getBoolean("uppercase");
                    upperCase = upperCaseb ? "1" : "0";
                }

                int lineSize = 0;
                String lineColor = "0xffffff";
                int lineOpacity = 100;
                if (jsonObject1.has("effects")) {
                    JSONObject effect = jsonObject1.getJSONObject("effects");
                    if (effect.has("frameFX")) {
                        if (effect.getJSONObject("frameFX").has("lineSize")) {
                            lineSize = effect.getJSONObject("frameFX").getInt("lineSize");
                        }
                        if (effect.getJSONObject("frameFX").has("color")) {
                            lineColor = effect.getJSONObject("frameFX").getString("color");
                        }
                        if (effect.getJSONObject("frameFX").has("alpha")) {
                            lineOpacity = effect.getJSONObject("frameFX").getInt("alpha");
                        }
                    }
                }

                int distance = -11111;
                int angle = 0;
                int blurX = 0;
                int alpha = 100;
                String colorSD = "0xffffff";

                if (jsonObject1.has("effects")) {
                    JSONObject effect = jsonObject1.getJSONObject("effects");
                    if (effect.has("dropShadow")) {
                        if (effect.getJSONObject("dropShadow").has("distance")) {
                            distance = effect.getJSONObject("dropShadow").getInt("distance");
                        }
                        if (effect.getJSONObject("dropShadow").has("color")) {
                            colorSD = effect.getJSONObject("dropShadow").getString("color");
                        }
                        if (effect.getJSONObject("dropShadow").has("angle")) {
                            angle = effect.getJSONObject("dropShadow").getInt("angle");
                        }
                        if (effect.getJSONObject("dropShadow").has("alpha")) {
                            alpha = effect.getJSONObject("dropShadow").getInt("alpha");
                        }
                        if (effect.getJSONObject("dropShadow").has("blurX")) {
                            blurX = effect.getJSONObject("dropShadow").getInt("blurX");
                        }
                    }
                }

                TextSTRInfo textInfo = new TextSTRInfo();

                textInfo.setText_id(String.valueOf(i));
                textInfo.setName(lName);
                textInfo.setText(text);
                textInfo.setTxt_height(calcHeight);
                textInfo.setTxt_width(calcWidth);
                textInfo.setTxt_x_pos(realX);
                textInfo.setJustification(justification);
                textInfo.setTxt_y_pos(realY);
                textInfo.setUppercase(upperCase);
                textInfo.setTxt_rotation(rotation);
                textInfo.setTxt_color(color.replace("0x", "#"));
                textInfo.setTxt_order("" + i);
                textInfo.setFont_family(font);
                textInfo.setLineSize(lineSize);
                textInfo.setLineColor(lineColor.replace("0x", "#"));
                textInfo.setLineOpacity(lineOpacity);
                textInfo.setSdDistance(distance);
                textInfo.setSdAngle(angle);
                textInfo.setSdBlur(blurX);
                textInfo.setSdOpacity(alpha);
                textInfo.setSdColor(colorSD.replace("0x", "#"));
                textInfoArrayList.add(textInfo);

            }

        }

    }
    private String saveSticker(String templateName, String name, String stickerUrl) {
        String directory = new StorageUtils(requireActivity()).getPackageStorageDir("/." + templateName + "/").getAbsolutePath();

        File file = new File(directory + "/" + name + ".png");
        String[] newPath = {""};
        Util.showLog("FILE: " + file.exists() + " FILE : " + file.getName());
        if (!file.exists()) {
            LoadLogo loadLogo = new LoadLogo(stickerUrl, directory, name, new PosterActivity.SaveListener() {
                @Override
                public void onSave(String path) {
                    newPath[0] = path;
                }
            });
            loadLogo.execute();
            return newPath[0];
        } else {
            return file.getAbsolutePath();
        }
    }
    class LoadLogo extends AsyncTask<String, String, String> {

        private String message = "", verifyStatus = "0", urls, directory, name;
        Drawable drawable;
        Bitmap bitmap2;
        PosterActivity.SaveListener saveListener;

        public LoadLogo(String urls, String directory, String name, PosterActivity.SaveListener listener) {
            this.urls = urls;
            this.directory = directory;
            this.name = name;
            this.saveListener = listener;
        }

        @Override
        protected String doInBackground(String... strings) {
            Util.showLog("URL: "+"Sticker Image Url" + urls);
            bitmap2 = null;
            InputStream inputStream;
            try {
                inputStream = new java.net.URL(urls).openStream();
                bitmap2 = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();

            }

            drawable = new BitmapDrawable(Resources.getSystem(), bitmap2);
            return "0";
        }

        @SuppressLint("WrongThread")
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            File file = new File(directory);

            if (!file.exists()) {
                file.mkdirs();
            }
            try {
                String filePath = directory + "/" + name + ".png";
                FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                bitmap2.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.close();
                saveListener.onSave(filePath);
            } catch (Exception e) {
                e.printStackTrace();
                Util.showErrorLog(e.getMessage(), e);

            }
        }
    }
    class LoadFonts extends AsyncTask<String, String, String> {

        private String message = "", verifyStatus = "0", zipName, directory, name;
        InputStream inputStream;

        public LoadFonts(String zipName, String directory, String name) {
            this.zipName = zipName;
            this.directory = directory;
            this.name = name;
        }

        @Override
        protected String doInBackground(String... strings) {


            String font_url = getFontUrl(zipName, name, ".ttf");

            Util.showLog("URL: " + font_url);

            File file = new File(directory);

            if (!file.exists()) {
                file.mkdirs();
            }
            try {
                inputStream = new java.net.URL(font_url).openStream();
                FileOutputStream fileOutputStream = new FileOutputStream(directory + "/" + name + ".ttf");
                int length;
                byte[] buffer = new byte[1024];
                while ((length = inputStream.read(buffer)) > -1) {
                    fileOutputStream.write(buffer, 0, length);
                }
                fileOutputStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    inputStream = new java.net.URL(getFontUrl(zipName, name, ".TTF")).openStream();
                    FileOutputStream fileOutputStream = new FileOutputStream(directory + "/" + name + ".TTF");
                    int length;
                    byte[] buffer = new byte[1024];
                    while ((length = inputStream.read(buffer)) > -1) {
                        fileOutputStream.write(buffer, 0, length);
                    }
                    fileOutputStream.close();
                    inputStream.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                    try {
                        inputStream = new java.net.URL(getFontUrl(zipName, name, ".otf")).openStream();
                        FileOutputStream fileOutputStream = new FileOutputStream(directory + "/" + name + ".otf");
                        int length;
                        byte[] buffer = new byte[1024];
                        while ((length = inputStream.read(buffer)) > -1) {
                            fileOutputStream.write(buffer, 0, length);
                        }
                        fileOutputStream.close();
                        inputStream.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();

                    }
                }
            }

            return "0";
        }

        @SuppressLint("WrongThread")
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
    private String getFontUrl(String mZipName, String mName, String ext) {
        String font_url = "";
//        if (prefManager.getString(Constant.DIGITAL_ENABLE).equals(Config.ONE)) {
//            font_url = prefManager.getString(Constant.DIGITAL_END_URL) + "uploads/template/" + mZipName + "/fonts/"
//                    + mName + ext;
//        } else {
//            font_url = Config.APP_API_URL + "uploads/template/" + mZipName + "/fonts/" + mName + ext;
//        }
        return font_url;
    }
}
