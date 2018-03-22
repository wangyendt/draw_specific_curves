package com.newdegreetech.draw_specific_curves.core;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.newdegreetech.draw_specific_curves.R;
import com.newdegreetech.draw_specific_curves.paths_utils.UriToAbsPathUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView tvPath;
    Button btnSelectFile;
    String strPath;
    LineChart lcDrawPlot;
    LinearLayout.LayoutParams params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //取消标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //取消状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        btnSelectFile = findViewById(R.id.btnSelectFile);
        lcDrawPlot = findViewById(R.id.lcPlotCurve);
        params = (LinearLayout.LayoutParams) lcDrawPlot.getLayoutParams();

        btnSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });

        lcDrawPlot.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int[] size = getScreenSize();
                Log.d("fuck", params.weight + " " + params.height);
                Log.d("fuck", size[0] + " " + size[1]);
                params.weight = size[0];
                params.height = size[1];
                lcDrawPlot.setLayoutParams(params);
                lcDrawPlot.setRotation(90);
                Log.d("fuck", ((LinearLayout.LayoutParams) lcDrawPlot.getLayoutParams()).weight + " " + lcDrawPlot.getLayoutParams().height);
                return true;
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
                    fdata.add(Float.parseFloat(strPhrase[0]));
//                    Log.v("fuck", strPhrase[0]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.v("fuck", fdata.size() + "");
            lcDrawPlot.setDrawBorders(true);
            //设置数据
            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < fdata.size(); i++) {
                entries.add(new Entry(i, fdata.get(i)));
            }
            //一个LineDataSet就是一条线
            LineDataSet lineDataSet = new LineDataSet(entries, "温度");
            LineData ldata = new LineData(lineDataSet);
            lcDrawPlot.setData(ldata);
        }
    }


    //获取运行屏幕宽度
    public int[] getScreenSize() {
        DisplayMetrics dm = new DisplayMetrics();
        Log.d("fuck", 1 + "");
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Log.d("fuck", 2 + "");
        //宽度 dm.widthPixels
        //高度 dm.heightPixels
        return new int[]{dm.widthPixels, dm.heightPixels};
    }

//    private List<Entry> get_filtered_data(ArrayList<Float> data, int avg_win_len) {
//        ArrayList<Float>filtered_data = new ArrayList<>();
//
//    }

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
