package com.example.studyplayer;

import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public final class VideoAdaptor extends ArrayAdapter<Video> {
    private long enqueue;
    private DownloadManager downloadManager;
    private BroadcastReceiver receiver;
    private ProgressDialog dialog;

    public VideoAdaptor(@NonNull Context context, @NonNull ArrayList<Video> list,ProgressDialog dialog) {
        super(context, 0, list);
        this.dialog=dialog;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.video_item, parent, false);
        Video video = getItem(position);

        TextView size = convertView.findViewById(R.id.size);
        size.setText(video.getSize() + " MB");
        TextView title = convertView.findViewById(R.id.title);
        title.setText(video.getTitle());
        Button save = convertView.findViewById(R.id.save);
        setSaveListener(save, video);
        ImageView view = convertView.findViewById(R.id.imageView);
        setPlayListener(view, video.getFilename());
        setBroadcastReceiver(video);
        return convertView;
    }

    private void setBroadcastReceiver(final Video video) {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(
                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enqueue);
                    Cursor c = downloadManager.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            final ProgressDialog dialog=new ProgressDialog(getContext());
                            dialog.setMessage("Video is being processed...");
                            dialog.setCancelable(false);
                            Thread th=new Thread(){
                                public void run(){
                                    try {
                                        encryptVideo(video);
                                    } catch (Exception e) {
                                    }
                                    finally {
                                        dialog.dismiss();
                                    }
                                }
                            };
                            th.start();
                            dialog.show();
                        }
                    }
                }
            }
        };
        getContext().registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void encryptVideo(Video video) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        // Here you read the cleartext.

        File toDelete = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + getContext().getApplicationContext().getPackageName() + "/files/" + video.getId() + ".tmp");
        FileInputStream fis = new FileInputStream(toDelete);
        // This stream write the encrypted text. This stream will be wrapped by another stream.
        FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Android/data/" + getContext().getApplicationContext().getPackageName() + "/files/" + video.getId() + ".enc");

        SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(), "AES");
        // Create cipher
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, sks);
        // Wrap the output stream
        CipherOutputStream cos = new CipherOutputStream(fos, cipher);
        // Write bytes
        int b;
        byte[] d = new byte[10000];
        while ((b = fis.read(d)) != -1) {
            cos.write(d, 0, b);
        }
        toDelete.delete();
        // Flush and close streams.
        cos.flush();
        cos.close();
        fis.close();

        SavedVideoDatabase database = new SavedVideoDatabase(getContext());
        database.add_record(video);
        database.close();
    }

    private void setPlayListener(ImageView view, final String filename) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent playVideo = new Intent(getContext(), Player.class);
                final String url = "https://studyplayer.000webhostapp.com/videos/" + filename;
                playVideo.putExtra("url", url);
                getContext().startActivity(playVideo);
            }
        });
    }

    private void setSaveListener(Button save, final Video video) {
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Saving video...", Toast.LENGTH_SHORT).show();
                //Delete if existing

                File file = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + getContext().getApplicationContext().getPackageName() + "/files/" + video.getId() + ".tmp");
                if (file.exists())
                    file.delete();

                Uri uri = Uri.parse("https://studyplayer.000webhostapp.com/videos/" + video.getFilename());
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setTitle(video.getTitle());
                request.setMimeType("video/mp4");
                request.setDescription("Saving Video...");
                request.allowScanningByMediaScanner();
                request.setDestinationInExternalFilesDir(getContext(), "", video.getId() + ".tmp");

                // include this line if permission has been granted by user to external directory
                downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                enqueue = downloadManager.enqueue(request);
            }
        });

    }
}
