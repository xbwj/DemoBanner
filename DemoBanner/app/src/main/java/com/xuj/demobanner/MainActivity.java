package com.xuj.demobanner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.xuj.banner.banner.Banner;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    Banner mBanner;
    String[] mImagesUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBanner = (Banner) findViewById(R.id.banner);
        mImagesUrl = getResources().getStringArray(R.array.url);
        mBanner.isAutoPlay(true);
        mBanner.setDelayTime(3000);
        mBanner.setImages(Arrays.asList(mImagesUrl))
                .setImageLoader(new FrescoImageLoader())
                .start();
        mBanner.setOnBannerClickListener(new Banner.OnBannerClickListener() {
            @Override
            public void OnBannerClick(int position) {
                toast("点击了第" + position + "张图片");
            }
        });

    }

    private void toast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mBanner.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBanner.stopAutoPlay();
    }
}
