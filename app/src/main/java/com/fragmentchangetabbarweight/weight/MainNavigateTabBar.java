package com.fragmentchangetabbarweight.weight;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fragmentchangetabbarweight.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : zcq
 * @Data : 2016/7/20
 * 自定义一个底部导航用来切换Fragment
 */
public class MainNavigateTabBar extends LinearLayout implements View.OnClickListener {

    //当前tag
    private static final String KEY_CURRENT_TAG = "com.fragmentchangetabbarweight.weight.currentTag";

    private List<ViewHolder> mViewHolderList;
    private OnTabSelectedListener onTabSelectedListener;//用户选择按钮监听
    private FragmentActivity mFragmentActivity;
    private String mCurrentTag;//现在的tag
    private String mRestoreTag;//恢复tag
    /*主内容显示区域View的id*/
    private int mMainContentLayoutId;
    /*选中的Tab文字颜色*/
    private ColorStateList mSelectedTextColor;
    /*正常的Tab文字颜色*/
    private ColorStateList mNormalTextColor;
    /*Tab文字的颜色*/
    private float mTabTextSize;
    /*默认选中的tab索引*/
    private int mDefaultSelectedTab = 0;
    /*当前选中的tab索引*/
    private int mCurrentSelectedTab;

    /**
     * 定义一个用户选择监听接口
     */
    private interface OnTabSelectedListener {
        void onTabSelected(ViewHolder holder);
    }

    public MainNavigateTabBar(Context context) {
        this(context, null);
    }

    public MainNavigateTabBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainNavigateTabBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        /*获取自定义属性的值*/
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MainNavigateTabBar, 0, 0);
        ColorStateList tabTextColor = typedArray.getColorStateList(R.styleable.MainNavigateTabBar_navigateTabTextColor);
        ColorStateList selectedTabTextColor = typedArray.getColorStateList(R.styleable.MainNavigateTabBar_navigateTabSelectedTextColor);

        mTabTextSize = typedArray.getDimensionPixelSize(R.styleable.MainNavigateTabBar_navigateTabTextSize, 0);
        mMainContentLayoutId = typedArray.getResourceId(R.styleable.MainNavigateTabBar_containerId, 0);

        mNormalTextColor = (tabTextColor != null ? tabTextColor : context.getResources().getColorStateList(R.color.tab_text_normal));

        if (selectedTabTextColor != null) {
            //如果被选择的文字颜色不为null
            mSelectedTextColor = selectedTabTextColor;
        } else {
            ThemeUtils.checkAppCompatTheme(context);
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
            mSelectedTextColor = context.getResources().getColorStateList(typedValue.resourceId);
        }
        mViewHolderList = new ArrayList<>();
    }

    /**
     * 添加视图关联到窗体
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mMainContentLayoutId == 0) {
            throw new RuntimeException("mFrameLayoutId Cannot be 0");
        }
        if (mViewHolderList.size() == 0) {
            throw new RuntimeException("mViewHolderList.size Cannot be 0, Please call addTab()");
        }
        if (!(getContext() instanceof FragmentActivity)) {
            throw new RuntimeException("parent activity must is extends FragmentActivity");
        }

        mFragmentActivity = (FragmentActivity) getContext();
        ViewHolder defaultHolder = null;

        /*隐藏所有fragment*/
        hideAllFragment();
        /*判断是否要显示回复的fragment*/
        if (!TextUtils.isEmpty(mRestoreTag)) {
            for (ViewHolder holder : mViewHolderList) {
                if (TextUtils.equals(mRestoreTag, holder.tag)) {
                    defaultHolder = holder;
                    mRestoreTag = null;
                    break;
                }
            }
        } else {
            defaultHolder = mViewHolderList.get(mDefaultSelectedTab);
        }
        showFragment(defaultHolder);
    }

    /**
     * 点击监听事件
     */
    @Override
    public void onClick(View view) {
        Object object = view.getTag();
        if (object != null && object instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) view.getTag();
            //显示对应的fragment
            showFragment(holder);
            if (onTabSelectedListener != null) {
                onTabSelectedListener.onTabSelected(holder);
            }
        }
    }

    //region 隐藏所有的fragment
    private void hideAllFragment() {
        if (mViewHolderList == null || mViewHolderList.size() == 0) {
            return;
        }

        FragmentTransaction transaction = mFragmentActivity.getSupportFragmentManager().beginTransaction();

        for (ViewHolder holder : mViewHolderList) {
            Fragment fragment = mFragmentActivity.getSupportFragmentManager().findFragmentByTag(holder.tag);
            if (fragment != null && fragment.isHidden()) {
                transaction.hide(fragment);
            }
        }
        transaction.commit();
    }
    //endregion

    //region 添加tab
    public void addTab(Class frameLayoutClass, TabParam tabParam) {
        int defaultLayout = R.layout.comui_tab_view;//默认布局模板

        if (TextUtils.isEmpty(tabParam.title)) {
            //如果没有设置title,则从它的String 资源id中获取
            tabParam.title = getContext().getString(tabParam.titleStringRes);
        }

        //使用布局压缩器把布局压缩进去
        View view = LayoutInflater.from(getContext()).inflate(defaultLayout, null);
        view.setFocusable(true);//设置可以获取焦点

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.tabIndex = mViewHolderList.size();//tab索引是集合的长度
        viewHolder.fragmentClass = frameLayoutClass;//fragment设置
        viewHolder.tag = tabParam.title;//设置tag
        viewHolder.pageParam = tabParam;

        viewHolder.tabIcon = (ImageView) view.findViewById(R.id.tab_icon);
        viewHolder.tabTitle = (TextView) view.findViewById(R.id.tab_title);

        /*如果title为空时，设置隐藏，否则设置标题*/
        if (TextUtils.isEmpty(tabParam.title)) {
            viewHolder.tabTitle.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.tabTitle.setText(tabParam.title);
        }

        /*设置文字尺寸*/
        if (mTabTextSize != 0) {
            viewHolder.tabTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabTextSize);
        }

        /*设置文字的颜色*/
        if (mNormalTextColor != null) {
            viewHolder.tabTitle.setTextColor(mNormalTextColor);
        }

        /*设置背景颜色*/
        if (tabParam.backgroundColor > 0) {
            view.setBackgroundResource(tabParam.backgroundColor);
        }

        /*设置图标*/
        if (tabParam.iconResId > 0) {
            viewHolder.tabIcon.setImageResource(tabParam.iconResId);
        } else {
            viewHolder.tabIcon.setVisibility(View.INVISIBLE);
        }

        /*设置选中图标的显示，和不选中图标的显示*/
        if (tabParam.iconResId > 0 && tabParam.iconSelectedResId > 0) {
            view.setTag(viewHolder);
            view.setOnClickListener(this);
            mViewHolderList.add(viewHolder);
        }
        /*把视图添加进去*/
        addView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
    }
    //endregion

    //region 显示holder对应的fragment
    private void showFragment(ViewHolder holder) {
        FragmentTransaction transaction = mFragmentActivity.getSupportFragmentManager().beginTransaction();
        //判断fragment是否有显示
        if (isFragmentShown(transaction, holder.tag)) {
            return;
        }
        //根据tag设置当前图标
        setCurrentSelectedTabByTag(holder.tag);

        Fragment fragment = mFragmentActivity.getSupportFragmentManager().findFragmentByTag(holder.tag);
        if (fragment == null) {
            fragment = getFragmentInstance(holder.tag);
            transaction.add(mMainContentLayoutId, fragment, holder.tag);
        } else {
            transaction.show(fragment);
        }
        transaction.commit();
        mCurrentSelectedTab = holder.tabIndex;//当前选中tab
    }
    //endregion

    //region 得到fragment的实例
    private Fragment getFragmentInstance(String tag) {
        Fragment fragment = null;
        for (ViewHolder holder : mViewHolderList) {
            if (TextUtils.equals(tag, holder.tag)) {
                //找到和tag相同的fragment获取实例，跳出循环
                try {
                    fragment = (Fragment) Class.forName(holder.fragmentClass.getName()).newInstance();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return fragment;
    }
    //endregion

    //region 判断fragment是否显示
    private boolean isFragmentShown(FragmentTransaction transaction, String newTag) {
        /*如果newTag和currentTag值相同*/
        if (TextUtils.equals(newTag, mCurrentTag)) {
            return true;
        }

        /*如果现在的tag为空*/
        if (TextUtils.isEmpty(mCurrentTag)) {
            return false;
        }

        Fragment fragment = mFragmentActivity.getSupportFragmentManager().findFragmentByTag(mCurrentTag);
        if (fragment != null && !fragment.isHidden()) {
            transaction.hide(fragment);
        }
        return false;
    }
    //endregion

    //region 设置当前选中tab的图片和文字颜色
    private void setCurrentSelectedTabByTag(String tag) {
        if (TextUtils.equals(mCurrentTag, tag)) {
            return;
        }

        for (ViewHolder holder : mViewHolderList) {
            if (TextUtils.equals(mCurrentTag, holder.tag)) {
                //如果当前tag和holder中的tag相同，则设置没选中图标和正常文字
                holder.tabIcon.setImageResource(holder.pageParam.iconResId);
                holder.tabTitle.setTextColor(mNormalTextColor);
            } else if (TextUtils.equals(tag, holder.tag)) {
                //如果选中的tag和holder中的tag相同则设置选中后的图片和文字颜色
                holder.tabIcon.setImageResource(holder.pageParam.iconSelectedResId);
                holder.tabTitle.setTextColor(mSelectedTextColor);
            }
        }

        mCurrentTag = tag;//设置当前tag
    }
    //endregion

    //region viewHolder用来缓存tabBar中的每一项
    private static class ViewHolder {
        public String tag;//tag
        public TabParam pageParam;//每个tab中的参数
        public ImageView tabIcon;//图标
        public TextView tabTitle;//标题
        public Class fragmentClass;//fragment
        public int tabIndex;//tab索引
    }
    //endregion

    //region tab中的每一项的参数
    public static class TabParam {
        public int backgroundColor = android.R.color.white;
        public int iconResId;//图标ID
        public int iconSelectedResId;//被选中图标资源的ID
        public int titleStringRes;//图标下的文字资源ID
        public String title;//图标下的标题

        //直接设置title
        public TabParam(int iconResId, int iconSelectedResId, String title) {
            this.iconResId = iconResId;
            this.iconSelectedResId = iconSelectedResId;
            this.title = title;
        }

        //通过title中的ID来设置title
        public TabParam(int iconResId, int iconSelectedResId, int titleStringRes) {
            this.iconResId = iconResId;
            this.iconSelectedResId = iconSelectedResId;
            this.titleStringRes = titleStringRes;
        }

        //可以设置背景
        public TabParam(int backgroundColor, int iconResId, int iconSelectedResId, int titleStringRes) {
            this.backgroundColor = backgroundColor;
            this.iconResId = iconResId;
            this.iconSelectedResId = iconSelectedResId;
            this.titleStringRes = titleStringRes;
        }

        //可以设置背景
        public TabParam(int backgroundColor, int iconResId, int iconSelectedResId, String title) {
            this.backgroundColor = backgroundColor;
            this.iconResId = iconResId;
            this.iconSelectedResId = iconSelectedResId;
            this.title = title;
        }
    }
    //endregion

    //region tab项中被选中text颜色
    public void setSelectedTabColor(int color) {
        mSelectedTextColor = ColorStateList.valueOf(color);
    }
    //endregion

    //region 设置tab中的text颜色
    public void setTabTextColor(ColorStateList color) {
        mNormalTextColor = color;
    }
    //endregion

    //region 设置tab中的text颜色
    public void setTabTextColor(int color) {
        mNormalTextColor = ColorStateList.valueOf(color);
    }
    //endregion

    //region 设置fragment要显示的id
    public void setFragmeLayoutId(int frameLayoutId) {
        mMainContentLayoutId = frameLayoutId;
    }
    //endregion

    //region 设置tab选中的监听
    public void setTabSelectListener(OnTabSelectedListener tabSelectListener) {
        onTabSelectedListener = tabSelectListener;
    }
    //endregion

    //region 设置默认选中的tab项
    public void setDefaultSelectedTab(int index) {
        if (index >= 0 && index < mViewHolderList.size()) {
            mDefaultSelectedTab = index;
        }
    }
    //endregion

    //region  设置当前选中的tab项
    public void setCurrentSelectedTab(int index) {
        if (index >= 0 && index < mViewHolderList.size()) {
            ViewHolder holder = mViewHolderList.get(index);
            showFragment(holder);
        }
    }
    //endregion

    //region 得到当前选中项
    public int getCurrentSelectedTab(){
        return mCurrentSelectedTab;
    }
    //endregion

    //region 恢复保存得state
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mRestoreTag = savedInstanceState.getString(KEY_CURRENT_TAG);
        }
    }
    //endregion

    //region  保存状态结合activity的生命周期使用
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_CURRENT_TAG, mCurrentTag);
    }
    //endregion


}
