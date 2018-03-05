package com.newdegreetech.draw_specific_curves;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView tvPath;
    Button btnSelectFile;
    String strPath;
    LineChart lcDrawPlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvPath = findViewById(R.id.tvPath);
        btnSelectFile = findViewById(R.id.btnSelectFile);
        lcDrawPlot = findViewById(R.id.lcPlotCurve);

        btnSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (shouldShowRequestPermissionRationale(
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // Explain to the user why we need to read the contacts
                    }

                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            1);

                    // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                    // app-defined int constant that should be quite unique

                    return;
                }
            }

            String path = UriToAbsPathUtil.getRealFilePath(this, uri);
            Log.v("fuck", path);
            Toast.makeText(MainActivity.this, path, Toast.LENGTH_SHORT).show();

            ArrayList<Float> fdata = new ArrayList<>();
            try {
                String str = readFileSdcardFile(path);
                for (String strline : str.split("\n")) {
                    String[] strPhrase = strline.split(" ");
                    fdata.add(Float.parseFloat(strPhrase[1]));
                    Log.v("fuck", strPhrase[1]);
                }
//                Log.v("fuck",str);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.v("fuck",fdata.size()+"");
            lcDrawPlot.setDrawBorders(true);
            //设置数据
            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < fdata.size(); i++) {
                entries.add(new Entry(i, fdata.get(i)));
            }
//            for (int i = 0; i < 10; i++) {
//                entries.add(new Entry(i, (float) (Math.random()) * 80));
//            }
            //一个LineDataSet就是一条线
            LineDataSet lineDataSet = new LineDataSet(entries, "温度");
            LineData ldata = new LineData(lineDataSet);
            lcDrawPlot.setData(ldata);
        }
    }

    // write SDCard
    private void writeFileSdcardFile(String fileName, String writeStr) throws IOException {
        try {

            FileOutputStream fout = new FileOutputStream(fileName);
            byte[] bytes = writeStr.getBytes();

            fout.write(bytes);
            fout.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    read SDCard
    private String readFileSdcardFile(String fileName) throws IOException {
        String res = "";
        try {

            FileInputStream fin = new FileInputStream(fileName);
            int length = fin.available();

            byte[] buffer = new byte[length];
            fin.read(buffer);

            res = new String(buffer, "UTF-8");

            fin.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
}
