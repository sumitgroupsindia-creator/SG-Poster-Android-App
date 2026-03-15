package com.sgdigitalposter.app.ui.fragments;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;


import static androidx.media3.ui.PlayerView.SHOW_BUFFERING_WHEN_PLAYING;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSourceFactory;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.TrackSelector;
import androidx.media3.ui.PlayerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sgdigitalposter.app.MyApplication;
import com.sgdigitalposter.app.R;
import com.sgdigitalposter.app.adapters.DownloadAdapter;
import com.sgdigitalposter.app.binding.GlideApp;
import com.sgdigitalposter.app.binding.GlideBinding;
import com.sgdigitalposter.app.databinding.FragmentDownloadBinding;
import com.sgdigitalposter.app.items.DownloadItem;
import com.sgdigitalposter.app.ui.dialog.DialogMsg;
import com.sgdigitalposter.app.utils.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DownloadFragment extends Fragment {

    public DownloadFragment() {
        // Required empty public constructor
    }

    FragmentDownloadBinding binding;
    DownloadAdapter adapter;
    List<DownloadItem> uriList;
    int DELETE_REQUEST_URI_R = 11, DELETE_REQUEST_URI_Q = 12;
    DownloadItem downloadItem;
    DialogMsg dialogMsg;
    ExoPlayer sharePlayer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDownloadBinding.inflate(getLayoutInflater());
        uriList = new ArrayList<>();
        dialogMsg = new DialogMsg(getActivity(), false);
        setData();

        Util.showLog("DownloadItem : " + downloadItem);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(false);
            binding.shimmerViewContainer.startShimmer();
            binding.shimmerViewContainer.setVisibility(VISIBLE);
            binding.rvDownload.setVisibility(GONE);
            setData();
        });

        return binding.getRoot();
    }

    private void setData() {
        new LoadImages().execute();
    }

    public class LoadImages extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            adapter = new DownloadAdapter(getActivity(), uriList, item -> {
                if (Util.doubleClick()){

                }else {
                    downloadItem = item;
                    showPreviewDialog();
                }
            });
            binding.rvDownload.setAdapter(adapter);
            if (uriList.size() > 0) {
                binding.shimmerViewContainer.stopShimmer();
                binding.shimmerViewContainer.setVisibility(View.GONE);
                binding.rvDownload.setVisibility(View.VISIBLE);
                binding.animationView.setVisibility(GONE);
            } else {
                binding.shimmerViewContainer.stopShimmer();
                binding.rvDownload.setVisibility(View.GONE);
                binding.shimmerViewContainer.setVisibility(View.GONE);
                binding.animationView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            uriList.clear();
           /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                String selection = MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME + " like? ";
                String[] selectionArgs = {getContext().getString(R.string.app_name)};

                if (getActivity() != null) {
                    Cursor cursor = getActivity().getContentResolver().query(
                            MediaStore.AUTHORITY_URI,
                            new String[]{MediaStore.Files.FileColumns._ID},
                            selection,
                            selectionArgs,
                            null);

                    if (cursor != null && cursor.moveToFirst()) {
                        for (int i = 0; i < cursor.getCount(); i++) {
                            @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));

                            uriList.add(Uri.withAppendedPath(MediaStore.AUTHORITY_URI, id));

                            cursor.moveToNext();
                        }
                        Collections.reverse(uriList);

                        cursor.close();
                    }
                }
            }
            else {*/
            try {
                File root = new File(Environment.getExternalStorageDirectory() + File.separator
                        + Environment.DIRECTORY_PICTURES + File.separator + getContext().getString(R.string.app_name) + "/");
                if (root.exists()) {
                    String[] okFileExtensions = new String[]{"jpg", "jpeg", "png"};
                    File[] files = root.listFiles();

                    for (int i = 0; i < files.length; i++) {
                        if (files[i].getName().contains("jpg") || files[i].getName().contains("jpeg") || files[i].getName().contains("png")) {
                            Bitmap bitmap = null;
                            ContentResolver contentResolver = getContext().getContentResolver();
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, getImageContentUri(files[i], false));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Log.e("SB", "doInBackground files[i] :" + files[i] );
                            uriList.add(new DownloadItem(getImageContentUri(files[i], false), bitmap, false));
                        }
                        if (files[i].getName().contains("mp4")) {
                            uriList.add(new DownloadItem(Uri.parse(files[i].getAbsolutePath()), null, true));
                        }
                    }

                    Collections.reverse(uriList);
                        Log.e("SB", "doInBackground Reverse UriList:" + uriList.toString() );

                }
            } catch (Exception e) {
                Util.showErrorLog(e.getMessage(), e);
            }
            return null;
        }
    }


    public Uri getImageContentUri(File imageFile, boolean isVideo) {
        if (isVideo) {
            String filePath = imageFile.getAbsolutePath();
            Cursor cursor = getActivity().getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Video.Media._ID}, MediaStore.Video.Media.DATA + "=? ", new String[]{filePath}, null);
            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                cursor.close();
                return Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "" + id);
            }
        } else {
            try {
                String filePath = imageFile.getAbsolutePath();
                Cursor cursor = getActivity().getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Images.Media._ID},
                        MediaStore.Images.Media.DATA + "=? ",
                        new String[]{filePath}, null);
                if (cursor != null && cursor.moveToFirst()) {
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                    cursor.close();
                    return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
                }
            } catch (Exception e) {
                Util.showErrorLog(e.getMessage(), e);
            }
        }
        return null;
    }

    @OptIn(markerClass = UnstableApi.class) private void showPreviewDialog() {
        int screenWidth;
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(R.layout.download_dialog);
        dialog.setCancelable(false);
        screenWidth = MyApplication.getColumnWidth(1, getResources().getDimension(com.intuit.ssp.R.dimen._10ssp));

        CardView cv_base = dialog.findViewById(R.id.cv_base);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) cv_base.getLayoutParams();
        params.width = screenWidth;
        params.height = screenWidth;

        cv_base.setLayoutParams(params);

        TextView title = dialog.findViewById(R.id.save_title);
        ImageView iv_cancel = dialog.findViewById(R.id.iv_close);
        ImageView iv_preview = dialog.findViewById(R.id.iv_save_image);
        ImageView iv_download = dialog.findViewById(R.id.iv_download);
        ImageView iv_whatsapp = dialog.findViewById(R.id.ic_whatsapp);
        ImageView iv_facebook = dialog.findViewById(R.id.ic_facebook);
        ImageView iv_instagram = dialog.findViewById(R.id.ic_instagram);
        ImageView iv_twitter = dialog.findViewById(R.id.ic_twitter);
        ImageView iv_share = dialog.findViewById(R.id.ic_share);
        ImageView icThumb = dialog.findViewById(R.id.icThumb);

        iv_download.setImageDrawable(getResources().getDrawable(R.drawable.ic_delete));

        GlideBinding.setTextSize(title, "font_title_size");
        title.setText(getContext().getString(R.string.share_via));

        PlayerView videoPlayer = dialog.findViewById(R.id.videoPlayer);
        ImageView ivPlayVideo = dialog.findViewById(R.id.iv_play_video);

        ivPlayVideo.setOnClickListener(v -> {
            ivPlayVideo.setVisibility(GONE);
            if (sharePlayer != null) {
                sharePlayer.seekTo(0);
                sharePlayer.setPlayWhenReady(true);
            }
        });

        if (downloadItem.isVideo) {
            if (sharePlayer != null) {
                sharePlayer.setPlayWhenReady(false);
                sharePlayer.stop();
                sharePlayer.seekTo(0);
            }
            videoPlayer.setShutterBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.yellow_50));
            DefaultRenderersFactory rf = new DefaultRenderersFactory(requireActivity()).setEnableDecoderFallback(true);
            if(sharePlayer != null)
                sharePlayer.release();

            sharePlayer = new ExoPlayer.Builder(requireActivity(), rf).build();

            GlideApp.with(this)
                    .load(downloadItem.uri)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(icThumb);

            icThumb.setVisibility(VISIBLE);
            videoPlayer.setVisibility(VISIBLE);
            videoPlayer.setUseController(false);
            videoPlayer.setControllerHideOnTouch(true);
            videoPlayer.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);

            /*int appNameStringRes = R.string.app_name;
            String userAgent = androidx.media3.common.util.Util.getUserAgent(getContext(), this.getString(appNameStringRes));
            DefaultDataSourceFactory defdataSourceFactory = new DefaultDataSourceFactory(getContext(), userAgent);
            Uri uriOfContentUrl = downloadItem.uri;
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(defdataSourceFactory).createMediaSource(MediaItem.fromUri(uriOfContentUrl));  // creating a media source

            sharePlayer.prepare(mediaSource);
            sharePlayer.setPlayWhenReady(true); // start loading video and play it at the moment a chunk of it is available offline

            videoPlayer.setPlayer(sharePlayer); // attach surface to the view
            videoPlayer.setShowBuffering(SHOW_BUFFERING_WHEN_PLAYING);

            sharePlayer.addListener(new Player.Listener() {

                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    Player.Listener.super.onPlaybackStateChanged(playbackState);
                    switch (playbackState) {
                        case ExoPlayer.STATE_ENDED:
                            ivPlayVideo.setVisibility(View.VISIBLE);
                            break;
                    }
                }
            });
*/


            MediaItem mediaItem = MediaItem.fromUri(downloadItem.uri);

            sharePlayer.setMediaItem(mediaItem);
            sharePlayer.prepare();
            sharePlayer.setPlayWhenReady(true); // start loading video and play it at the moment a chunk of it is available offline
            sharePlayer.play();

            videoPlayer.setPlayer(sharePlayer);

            sharePlayer.addListener(new Player.Listener() {
                @Override
                public void onEvents(Player player, Player.Events events) {
                    Player.Listener.super.onEvents(player, events);
                }

                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    Player.Listener.super.onPlaybackStateChanged(playbackState);
                    switch (playbackState) {

                        case ExoPlayer.STATE_ENDED:
                            ivPlayVideo.setVisibility(View.VISIBLE);
                            icThumb.setVisibility(View.INVISIBLE);

                            break;
                        case ExoPlayer.STATE_READY:
                            icThumb.setVisibility(View.INVISIBLE);
                            ivPlayVideo.setVisibility(View.INVISIBLE);
                            videoPlayer.setVisibility(VISIBLE);
                    }
                }

                @Override
                public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                    Player.Listener.super.onPlayWhenReadyChanged(playWhenReady, reason);
                }

                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    Player.Listener.super.onIsPlayingChanged(isPlaying);
                }

                @Override
                public void onPlayerError(PlaybackException error) {
                    Player.Listener.super.onPlayerError(error);
                }
            });


        } else {
            iv_preview.setVisibility(VISIBLE);
            Glide.with(this)
                    .asBitmap()
                    .load(downloadItem.uri)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            int imageHeight = resource.getHeight();
                            int imageWidth = resource.getWidth();
                            float ratio = (float) imageWidth / (float) imageHeight;

                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) cv_base.getLayoutParams();
                            params.height = (int) (screenWidth / ratio);
                            params.width = screenWidth;
                            cv_base.setLayoutParams(params);

                            iv_preview.setImageBitmap(resource);

                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        }

        iv_cancel.setOnClickListener(v -> {
            if (sharePlayer != null) {
                sharePlayer.setPlayWhenReady(false);
                sharePlayer.stop();
                sharePlayer.seekTo(0);
                ivPlayVideo.setVisibility(VISIBLE);
            }
            dialog.dismiss();
        });
        iv_download.setOnClickListener(v -> {
//            Util.showLog("DownloadItem iv_download click : " + downloadItem.uri.getPath());
            if (sharePlayer != null) {
                sharePlayer.setPlayWhenReady(false);
                sharePlayer.stop();
                sharePlayer.seekTo(0);
                ivPlayVideo.setVisibility(VISIBLE);
            }
            showDeleteDialog();
            dialog.dismiss();
        });
        iv_whatsapp.setOnClickListener(v -> {
            if (sharePlayer != null) {
                sharePlayer.setPlayWhenReady(false);
                sharePlayer.stop();
                sharePlayer.seekTo(0);
                ivPlayVideo.setVisibility(VISIBLE);
            }
            shareFileImageUri(downloadItem.uri, "", "whtsapp");
            dialog.dismiss();
        });
        iv_facebook.setOnClickListener(v -> {
            if (sharePlayer != null) {
                sharePlayer.setPlayWhenReady(false);
                sharePlayer.stop();
                sharePlayer.seekTo(0);
                ivPlayVideo.setVisibility(VISIBLE);
            }
            shareFileImageUri(downloadItem.uri, "", "fb");
            dialog.dismiss();
        });
        iv_instagram.setOnClickListener(v -> {
            if (sharePlayer != null) {
                sharePlayer.setPlayWhenReady(false);
                sharePlayer.stop();
                sharePlayer.seekTo(0);
                ivPlayVideo.setVisibility(VISIBLE);
            }
            shareFileImageUri(downloadItem.uri, "", "insta");
            dialog.dismiss();
        });
        iv_twitter.setOnClickListener(v -> {
            if (sharePlayer != null) {
                sharePlayer.setPlayWhenReady(false);
                sharePlayer.stop();
                sharePlayer.seekTo(0);
                ivPlayVideo.setVisibility(VISIBLE);
            }
            shareFileImageUri(downloadItem.uri, "", "twter");
            dialog.dismiss();
        });
        iv_share.setOnClickListener(v -> {
            if (sharePlayer != null) {
                sharePlayer.setPlayWhenReady(false);
                sharePlayer.stop();
                sharePlayer.seekTo(0);
                ivPlayVideo.setVisibility(VISIBLE);
            }
            shareFileImageUri(downloadItem.uri, "", "Share Via");
            dialog.dismiss();
        });
        dialog.show();
    }

    private void showDeleteDialog() {
        dialogMsg.showConfirmDialog(getString(R.string.delete), getString(R.string.sure_delete), getString(R.string.delete), getString(R.string.cancel));
        dialogMsg.show();
        dialogMsg.okBtn.setOnClickListener(v -> {
//            Util.showLog("DownloadItem showDeleteDialog : " + downloadItem);
//            Util.showLog("DownloadItem showDeleteDialog : " + uriList.get(0));
//            Util.showLog("DownloadItem showDeleteDialog : " + uriList.get(1));
            dialogMsg.cancel();
            try {
                /*for (int i = 0; i < uriList.size() ; i++){
//                    Util.showLog("Position : " + i +"Uri : "+ uriList.get(i).uri);
                }*/
                if (downloadItem.isVideo) {
//                    Util.showLog("Video Uri : " + downloadItem.uri.getPath());
                    File fdelete = new File(downloadItem.uri.getPath());
                    if (fdelete.exists()) {
                        if (fdelete.delete()) {
                            Util.showLog("file Deleted :" + downloadItem.uri.getPath());
//                            Toast.makeText(getContext(), getResources().getString(R.string.video_deleted), Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(getContext(),"Video Not Deleted" ,Toast.LENGTH_LONG).show();
                        }
                        if (adapter != null){
                            adapter.notifyDataSetChanged();
                        }
                    }

                } else {
                    int delete = getActivity().getContentResolver().delete(downloadItem.uri, null, null);
                    if (delete == 1) {
                        for (int i = 0; i <uriList.size(); i++) {
                            if(uriList.get(i).uri == downloadItem.uri){
                                uriList.remove(i);
                                Toast.makeText(getContext(), getResources().getString(R.string.image_deleted), Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                        if(adapter!=null){
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(getContext(), getResources().getString(R.string.error_delete), Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception exception) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && exception instanceof RecoverableSecurityException) {
                    try {
                        ArrayList<Uri> arrayListUri = new ArrayList<>();
                        arrayListUri.add(downloadItem.uri);
                        PendingIntent editPendingIntent = MediaStore.createDeleteRequest(getActivity().getContentResolver(), arrayListUri);
                        getActivity().startIntentSenderForResult(editPendingIntent.getIntentSender(), DELETE_REQUEST_URI_R, null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        Util.showErrorLog(e.getMessage(), e);
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && exception instanceof RecoverableSecurityException) {
                    try {
                        getActivity().startIntentSenderForResult(((RecoverableSecurityException) exception).getUserAction().getActionIntent().getIntentSender(), DELETE_REQUEST_URI_Q, null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        Util.showErrorLog(e.getMessage(), e);
                    }
                }
            }
        });
    }

    public void shareFileImageUri(Uri path, String name, String shareTo) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        switch (shareTo) {
            case "whtsapp":
                shareIntent.setPackage("com.whatsapp");
                break;
            case "fb":
                shareIntent.setPackage("com.facebook.katana");
                break;
            case "insta":
                shareIntent.setPackage("com.instagram.android");
                break;
            case "twter":
                shareIntent.setPackage("com.twitter.android");
                break;
        }
        if (downloadItem.isVideo) {
            shareIntent.setDataAndType(path, "video/*");
        } else {
            shareIntent.setDataAndType(path, "image/*");
        }
        shareIntent.putExtra(Intent.EXTRA_STREAM, path);
        if (!name.equals("")) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, name);
        }
        startActivity(Intent.createChooser(shareIntent, MyApplication.context.getString(R.string.share_via)));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == DELETE_REQUEST_URI_R) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getContext(), getResources().getString(R.string.image_deleted), Toast.LENGTH_SHORT).show();
                for (int i = 0; i <uriList.size(); i++) {
                    if(uriList.get(i).uri == downloadItem.uri){
                        uriList.remove(i);
                        break;
                    }
                }
                if(adapter!=null){
                    adapter.notifyDataSetChanged();
                }
            }
        } else if (requestCode == DELETE_REQUEST_URI_Q) {
            if (resultCode == RESULT_OK) {
                int delete = getContext().getContentResolver().delete(downloadItem.uri, null, null);
                if (delete == 1) {
                    Toast.makeText(getContext(), getResources().getString(R.string.image_deleted), Toast.LENGTH_SHORT).show();
                    for (int i = 0; i <uriList.size(); i++) {
                        if(uriList.get(i).uri == downloadItem.uri){
                            uriList.remove(i);
                            break;
                        }
                    }
                    if(adapter!=null){
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(getContext(), getResources().getString(R.string.error_delete), Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}