package com.example.studyplayer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class SavedVideoAdaptor extends ArrayAdapter<Video> {
    ArrayList<Video> list;
    public SavedVideoAdaptor(@NonNull Context context, ArrayList<Video> list) {
        super(context, 0, list);
        this.list=list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.saved_video_item, parent, false);
        Video video = getItem(position);
        ImageView imageView = convertView.findViewById(R.id.imageView);
        setImageClickListener(imageView,video.getId());
        Button delete = convertView.findViewById(R.id.delete);
        setDeleteListener(delete,video.getId(),position);
        TextView title = convertView.findViewById(R.id.title);
        title.setText(video.getTitle());
        return convertView;
    }
    private void setImageClickListener(ImageView image,final String id)
    {
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    final ProgressDialog dialog=new ProgressDialog(getContext());
                    dialog.setMessage("Video is being processed...");
                    dialog.setCancelable(false);
                    final Thread th=new Thread(){
                        public void run()
                        {
                            try {
                                decrypt(id);
                            }catch (Exception e){}
                            finally {
                                Intent playVideo = new Intent(getContext(), Player.class);
                                final String url = Environment.getExternalStorageDirectory() + "/Android/data/" + getContext().getApplicationContext().getPackageName() + "/files/" + id+".tmp";
                                playVideo.putExtra("url", url);
                                getContext().startActivity(playVideo);
                                dialog.dismiss();
                            }
                        }
                    };
                    th.start();
                    dialog.show();
                }
                catch (Exception e){}
            }
        });
    }
    void decrypt(String id) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        FileInputStream fis = new FileInputStream(Environment.getExternalStorageDirectory() + "/Android/data/" + getContext().getApplicationContext().getPackageName() + "/files/" + id + ".enc");
        FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Android/data/" + getContext().getApplicationContext().getPackageName() + "/files/" + id + ".tmp");
        SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, sks);
        CipherInputStream cis = new CipherInputStream(fis, cipher);
        int b;
        byte[] d = new byte[10000];
        while((b = cis.read(d)) != -1) {
            fos.write(d, 0, b);
        }
        fos.flush();
        fos.close();
        cis.close();
    }
    private void setDeleteListener(Button delete, final String id, final int position) {
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File toDelete = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + getContext().getApplicationContext().getPackageName() + "/files/" + id+".enc");
                if(toDelete.exists())
                    toDelete.delete();
                SavedVideoDatabase database=new SavedVideoDatabase(getContext());
                database.delete(id);
                database.close();
                list.remove(position);
                notifyDataSetChanged();
            }
        });
    }
}
