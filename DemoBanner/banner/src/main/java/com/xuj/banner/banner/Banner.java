package com.xuj.banner.banner;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.xuj.banner.R;
import com.xuj.banner.loader.ImageLoaderInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/18.
 * <attr name="mIsShowIndicator" format="boolean"/>
 * <attr name="mIsAutoPlay" format="boolean"/>
 * <attr name="mDelayTime" format="integer"/>
 * <attr name="mTitleBackground" format="color|reference"/>
 * <attr name="mTitleTextColor" format="color"/>
 * <attr name="mTitleTextSize" format="dimension"/>
 * <attr name="mTitleHeight" format="dimension"/>
 * <attr name="mIndicatorRadius" format="dimension"/>
 * <attr name="mIndicatorMargin" format="dimension"/>
 * <attr name="mIndicatorDrawableSelected" format="reference"/>
 * <attr name="mIndicatorDrawableUnselected" format="reference"/>
 * <attr name="image_filter" format="reference"/>
 * <attr name="indicatorLocation">
 * <enum name="left" value="0"/>
 * <enum name="center" value="1"/>
 * <enum name="right" value="2"/>
 * </attr>
 */

public class Banner extends RelativeLayout {

    private final static String TAG = "Banner";
    private static final int WHAT_AUTO_PLAY = 1000;
    private int mIndicatorWidth = BannerConfig.INDICATOR_RADIUS;
    private int mIndicatorHeight = BannerConfig.INDICATOR_RADIUS;
    private RelativeLayout mIndicatorLayoutR;
    private LinearLayout mPointLayoutL;
    private LayoutParams mPointParamsR;
    private int mIndicatorContentBackground;
    //一些和控件相关的属性
    private boolean mIsShowIndicator = false;
    private boolean mIsAutoPlay = true;
    private int mIndicatorLocation = BannerConfig.INDICATOR_CENTER;
    private int mDelayTime = BannerConfig.DELAY_TIME;
    private int mTitleBackground;
    private int mTitleTextColor;
    private int mTitleHeight;
    private int mTitleTextSize;
    private int mIndicatorRadius;
    private int mIndicatorMargin;
    private int mIndicatorDrawableSelected;
    private int mIndicatorDrawableUnselected;
    private int image_filter;

    //一些用于判断的变量
    private boolean mIsPlaying;//是否正在播放
    private boolean mIsOneImage;//是否只有一张图片
    private boolean mIsImageUrl;//是否加载网络图片
    private boolean mIsFilter;//是否给图片增加过滤层
    private BannerAdapter mBannerAdapter;


    //
    private ViewPager mViewpager;
    private LinearLayout mIndicatorLayout;
    private List<ImageView> mIndicatorImages;
    private List<View> mImageViews;
    private List<?> mImageUrls;
    private int count = 0;
    private Context context;
    private int mCurrentPosition;

    private int gravity = -1;
    private int lastPosition = 1;
    private int scaleType = 0;
    private ImageLoaderInterface imageLoader;
    /**
     * 自动播放线程
     */
    private Handler mAutoPlayHandler = new Handler();
    final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            mCurrentPosition++;
            mViewpager.setCurrentItem(mCurrentPosition);
            mAutoPlayHandler.postDelayed(mRunnable,mDelayTime);
        }
    };


    /**
     * @param time
     * @return 设置轮播间隔时间
     */
    public Banner setDelayTime(int time) {
        this.mDelayTime=time;
        return this;
    }

    /**
     * @param gravity
     * @return 设置Indicator显示位置
     */
    public Banner setIndicatorGravity(int gravity) {
        return this;
    }


    public Banner(Context context) {
        this(context, null);
    }

    public Banner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Banner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        mImageUrls = new ArrayList<>();
        mIndicatorImages = new ArrayList<>();
        mImageViews = new ArrayList<>();
        initView(context, attrs);
    }

    /**
     * @param context
     * @param attrs   获取xml中申明的属性
     */
    private void initView(Context context, AttributeSet attrs) {

        mImageViews.clear();
        setOverScrollMode(OVER_SCROLL_NEVER);
        View view = LayoutInflater.from(context).inflate(R.layout.banner, this, true);
        mViewpager = (ViewPager) view.findViewById(R.id.viewpager);
        mIndicatorLayout = (LinearLayout) view.findViewById(R.id.indicator);
        handleTypedArray(context, attrs);
        initViewPager();
    }

    private void initViewPager() {
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!mIsOneImage && mIsAutoPlay) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    stopAutoPlay();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    startAutoPlay();
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public Banner start() {
        setBannerStyleUI();
        setImageList(mImageUrls);
        if (mIsAutoPlay && !mIsOneImage)
            startAutoPlay();
        return this;
    }

    public void setImageList(List<?> imageUrls) {
        if (imageUrls == null || imageUrls.size() <= 0) {
            Log.e(TAG, "Please set the images data.");
            return;
        }
        count = imageUrls.size();
        initImages();
        for (int i = 0; i <= count + 1; i++) {
            View imageView = null;
            if (imageLoader != null) {
                imageView = imageLoader.createImageView(context);
            }
            if (imageView == null) {
                imageView = new ImageView(context);
            }
            if (imageView instanceof ImageView) {
                if (scaleType == 0) {
                    ((ImageView) imageView).setScaleType(ImageView.ScaleType.FIT_XY);
                } else {
                    ((ImageView) imageView).setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }
            Object url;
            if (i == 0) {
                url = imageUrls.get(count - 1);
            } else if (i == count + 1) {
                url = imageUrls.get(0);
            } else {
                url = imageUrls.get(i - 1);
            }
            mImageViews.add(imageView);
            if (imageLoader != null)
                imageLoader.displayImage(context, url, imageView);
            else
                Log.e(TAG, "Please set images loader.");
        }
        setData();

    }

    private void setData() {
        currentItem = 1;
        if (mBannerAdapter == null) {
            mBannerAdapter = new BannerAdapter();
        }
        mViewpager.setAdapter(mBannerAdapter);
        mViewpager.setFocusable(true);
        mViewpager.setCurrentItem(1, false);
        mViewpager.addOnPageChangeListener(mOnPageChangeListener);
    }

    private int currentItem;
    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

            mCurrentPosition = position % (mImageUrls.size() + 2);
//            mIndicatorImages.get((lastPosition - 1 + count) % count).setImageResource(mIndicatorDrawableUnselected);
//            mIndicatorImages.get((position - 1 + count) % count).setImageResource(mIndicatorDrawableSelected);
//            lastPosition = position;
//
//            if (position == 0) position = 1;

            switchToPoint(toRealPosition(mCurrentPosition));


        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                int current = mViewpager.getCurrentItem();
                int lastReal = mViewpager.getAdapter().getCount() - 2;
                if (current == 0) {
                    mViewpager.setCurrentItem(lastReal, false);
                } else if (current == lastReal + 1) {
                    mViewpager.setCurrentItem(1, false);
                }
            }


        }
    };

    private int toRealPosition(int position) {
        int realPosition;
        realPosition = (position - 1) % mImageUrls.size();
        if (realPosition < 0)
            realPosition += mImageUrls.size();
        return realPosition;
    }

    private void switchToPoint(final int currentPoint) {
        for (int i = 0; i < mIndicatorLayout.getChildCount(); i++) {
            mIndicatorImages.get(i).setImageResource(mIndicatorDrawableUnselected);
        }
        mIndicatorImages.get(currentPoint).setImageResource(mIndicatorDrawableSelected);
    }

    private void initImages() {
        mImageViews.clear();
        createIndicator();
    }

    private void createIndicator() {
        mIndicatorImages.clear();
        mIndicatorLayout.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mIndicatorWidth, mIndicatorHeight);
        params.leftMargin = mIndicatorMargin;
        params.rightMargin = mIndicatorMargin;
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (i == 0) {
                imageView.setImageResource(mIndicatorDrawableSelected);
            } else {
                imageView.setImageResource(mIndicatorDrawableUnselected);
            }
            mIndicatorImages.add(imageView);
            mIndicatorLayout.addView(imageView, params);

        }

    }

    private void setBannerStyleUI() {
        if (mIsShowIndicator) {
            mIndicatorLayout.setVisibility(View.VISIBLE);
        } else {
            mIndicatorLayout.setVisibility(View.INVISIBLE);
        }
    }


    private void startAutoPlay() {
        if (mIsAutoPlay && !mIsPlaying) {
            mIsPlaying = true;
            mAutoPlayHandler.removeCallbacks(mRunnable);
            mAutoPlayHandler.postDelayed(mRunnable, 800);

        }

    }

    /**
     * 停止播放
     */
    public void stopAutoPlay() {
        if (mIsAutoPlay && mIsPlaying) {
            mIsPlaying = false;
            mAutoPlayHandler.removeCallbacks(mRunnable);
            mAutoPlayHandler.removeMessages(WHAT_AUTO_PLAY);
        }

    }


    private void handleTypedArray(Context context, AttributeSet attrs) {
        if (null == attrs) {
            return;
        }
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Banner);
        //指示器相关
        mIndicatorRadius = typedArray.getDimensionPixelOffset(R.styleable.Banner_indicator_radius,
                BannerConfig.INDICATOR_RADIUS);
        mIndicatorMargin = typedArray.getDimensionPixelSize(R.styleable.Banner_indicator_margin,
                BannerConfig.PADDING_SIZE);
        mIndicatorDrawableSelected = typedArray.getResourceId(R.styleable.Banner_indicator_drawable_selected,
                R.drawable.shape_banner_selected);
        mIndicatorDrawableUnselected = typedArray.getResourceId(R.styleable.Banner_indicator_drawable_unselected,
                R.drawable.shape_banner_unselected);
        mIsShowIndicator = typedArray.getBoolean(R.styleable.Banner_is_show_indicator,
                BannerConfig.IS_SHOW_INDICATOR);
        mIndicatorLocation = typedArray.getInt(R.styleable.Banner_indicator_location,
                BannerConfig.INDICATOR_CENTER);
        //Title相关
        mTitleBackground = typedArray.getColor(R.styleable.Banner_title_background,
                BannerConfig.TITLE_BACKGROUND);
        mTitleHeight = typedArray.getDimensionPixelOffset(R.styleable.Banner_title_height,
                BannerConfig.TITLE_HEIGHT);
        mTitleTextColor = typedArray.getColor(R.styleable.Banner_title_text_color,
                BannerConfig.TITLE_TEXT_COLOR);
        mTitleTextSize = typedArray.getDimensionPixelSize(R.styleable.Banner_title_text_size,
                BannerConfig.TITLE_TEXT_SIZE);
        mIndicatorContentBackground = typedArray.getColor(R.styleable.Banner_indicator_background,
                BannerConfig.INDICATOR_BACKGROUND);
        //滑动相关
        mIsAutoPlay = typedArray.getBoolean(R.styleable.Banner_is_auto_play,
                BannerConfig.IS_AUTO_PLAY);

        //图片遮罩
        image_filter = typedArray.getColor(R.styleable.Banner_image_filter,
                BannerConfig.IMAGE_FILTER);
        typedArray.recycle();
    }

    public void setOnBannerClickListener(OnBannerClickListener listener) {
        this.mOnBannerClickListener = listener;
    }


    public Banner setImageLoader(ImageLoaderInterface imageLoader) {
        this.imageLoader = imageLoader;
        return this;
    }


    public Banner setImages(List<?> imagesUrl) {
        this.mImageUrls = imagesUrl;
        return this;
    }

    public Banner isAutoPlay(boolean isAutoPlay) {
        this.mIsAutoPlay=isAutoPlay;
        return this;
    }


    public interface OnBannerClickListener {
        void OnBannerClick(int position);
    }


    public OnBannerClickListener mOnBannerClickListener;

    private class BannerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return mImageViews.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            container.addView(mImageViews.get(position));
            View view = mImageViews.get(position);
            if (mOnBannerClickListener != null) {
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnBannerClickListener.OnBannerClick(position);
                    }
                });
            }
            return view;

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mImageViews.get(position));
        }
    }
}
