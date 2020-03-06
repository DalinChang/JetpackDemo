package com.changlin.gallery.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.IntRange
import android.support.v4.widget.DrawerLayout
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.drawerlayout.widget.DrawerLayout

import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * author: 常林
 * date: 2019/12/9 16:18
 * company: 浙江单创品牌管理有限公司
 * description: TODO
 */
object StatusBarUtil {

    val DEFAULT_STATUS_BAR_ALPHA = 112
    private val FAKE_STATUS_BAR_VIEW_ID = 0x112
    private val FAKE_TRANSLUCENT_VIEW_ID = 0x113
    private val TAG_KEY_HAVE_SET_OFFSET = -123

    /**
     * 设置状态栏颜色
     *
     * @param activity       需要设置的activity
     * @param color          状态栏颜色值
     * @param statusBarAlpha 状态栏透明度
     */

    @JvmOverloads
    fun setColor(
        activity: Activity, @ColorInt color: Int, @IntRange(
            from = 0,
            to = 255
        ) statusBarAlpha: Int = DEFAULT_STATUS_BAR_ALPHA
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow()
                .addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.getWindow().setStatusBarColor(calculateStatusColor(color, statusBarAlpha))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            val decorView = activity.getWindow().getDecorView() as ViewGroup
            val fakeStatusBarView = decorView.findViewById(FAKE_STATUS_BAR_VIEW_ID)
            if (fakeStatusBarView != null) {
                if (fakeStatusBarView!!.getVisibility() === View.GONE) {
                    fakeStatusBarView!!.setVisibility(View.VISIBLE)
                }
                fakeStatusBarView!!.setBackgroundColor(calculateStatusColor(color, statusBarAlpha))
            } else {
                decorView.addView(createStatusBarView(activity, color, statusBarAlpha))
            }
            setRootView(activity)
        }
    }

    /**
     * 为滑动返回界面设置状态栏颜色
     *
     * @param activity       需要设置的activity
     * @param color          状态栏颜色值
     * @param statusBarAlpha 状态栏透明度
     */
    @JvmOverloads
    fun setColorForSwipeBack(
        activity: Activity, @ColorInt color: Int,
        @IntRange(from = 0, to = 255) statusBarAlpha: Int = DEFAULT_STATUS_BAR_ALPHA
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            val contentView = activity.findViewById(android.R.id.content) as ViewGroup
            val rootView = contentView.getChildAt(0)
            val statusBarHeight = getStatusBarHeight(activity)
            //            if (rootView != null && rootView instanceof CoordinatorLayout) {
            //                final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) rootView;
            //                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //                    coordinatorLayout.setFitsSystemWindows(false);
            //                    contentView.setBackgroundColor(calculateStatusColor(color, statusBarAlpha));
            //                    boolean isNeedRequestLayout = contentView.getPaddingTop() < statusBarHeight;
            //                    if (isNeedRequestLayout) {
            //                        contentView.setPadding(0, statusBarHeight, 0, 0);
            //                        coordinatorLayout.post(new Runnable() {
            //                            @Override
            //                            public void run() {
            //                                coordinatorLayout.requestLayout();
            //                            }
            //                        });
            //                    }
            //                } else {
            //                    coordinatorLayout.setStatusBarBackgroundColor(calculateStatusColor(color, statusBarAlpha));
            //                }
            //            } else {
            contentView.setPadding(0, statusBarHeight, 0, 0)
            contentView.setBackgroundColor(calculateStatusColor(color, statusBarAlpha))
            //            }
            setTransparentForWindow(activity)
        }
    }

    /**
     * 设置状态栏纯色 不加半透明效果
     *
     * @param activity 需要设置的 activity
     * @param color    状态栏颜色值
     */
    fun setColorNoTranslucent(activity: Activity, @ColorInt color: Int) {
        setColor(activity, color, 0)
    }

    /**
     * 设置状态栏颜色(5.0以下无半透明效果,不建议使用)
     *
     * @param activity 需要设置的 activity
     * @param color    状态栏颜色值
     */
    @Deprecated
    fun setColorDiff(activity: Activity, @ColorInt color: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return
        }
        transparentStatusBar(activity)
        val contentView = activity.findViewById(android.R.id.content) as ViewGroup
        // 移除半透明矩形,以免叠加
        val fakeStatusBarView = contentView.findViewById(FAKE_STATUS_BAR_VIEW_ID)
        if (fakeStatusBarView != null) {
            if (fakeStatusBarView!!.getVisibility() === View.GONE) {
                fakeStatusBarView!!.setVisibility(View.VISIBLE)
            }
            fakeStatusBarView!!.setBackgroundColor(color)
        } else {
            contentView.addView(createStatusBarView(activity, color))
        }
        setRootView(activity)
    }

    /**
     * 使状态栏半透明
     *
     *
     * 适用于图片作为背景的界面,此时需要图片填充到状态栏
     *
     * @param activity       需要设置的activity
     * @param statusBarAlpha 状态栏透明度
     */
    @JvmOverloads
    fun setTranslucent(
        activity: Activity, @IntRange(
            from = 0,
            to = 255
        ) statusBarAlpha: Int = DEFAULT_STATUS_BAR_ALPHA
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return
        }
        setTransparent(activity)
        addTranslucentView(activity, statusBarAlpha)
    }

    /**
     * 针对根布局是 CoordinatorLayout, 使状态栏半透明
     *
     *
     * 适用于图片作为背景的界面,此时需要图片填充到状态栏
     *
     * @param activity       需要设置的activity
     * @param statusBarAlpha 状态栏透明度
     */
    fun setTranslucentForCoordinatorLayout(
        activity: Activity, @IntRange(
            from = 0,
            to = 255
        ) statusBarAlpha: Int
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return
        }
        transparentStatusBar(activity)
        addTranslucentView(activity, statusBarAlpha)
    }

    /**
     * 设置状态栏全透明
     *
     * @param activity 需要设置的activity
     */
    fun setTransparent(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return
        }
        transparentStatusBar(activity)
        setRootView(activity)
    }

    /**
     * 使状态栏透明(5.0以上半透明效果,不建议使用)
     *
     *
     * 适用于图片作为背景的界面,此时需要图片填充到状态栏
     *
     * @param activity 需要设置的activity
     */
    @Deprecated
    fun setTranslucentDiff(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 设置状态栏透明
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            setRootView(activity)
        }
    }

    /**
     * 为DrawerLayout 布局设置状态栏颜色,纯色
     *
     * @param activity     需要设置的activity
     * @param drawerLayout DrawerLayout
     * @param color        状态栏颜色值
     */
    fun setColorNoTranslucentForDrawerLayout(
        activity: Activity,
        drawerLayout: DrawerLayout, @ColorInt color: Int
    ) {
        setColorForDrawerLayout(activity, drawerLayout, color, 0)
    }

    /**
     * 为DrawerLayout 布局设置状态栏变色
     *
     * @param activity       需要设置的activity
     * @param drawerLayout   DrawerLayout
     * @param color          状态栏颜色值
     * @param statusBarAlpha 状态栏透明度
     */
    @JvmOverloads
    fun setColorForDrawerLayout(
        activity: Activity, drawerLayout: DrawerLayout, @ColorInt color: Int,
        @IntRange(from = 0, to = 255) statusBarAlpha: Int = DEFAULT_STATUS_BAR_ALPHA
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow()
                .addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT)
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        // 生成一个状态栏大小的矩形
        // 添加 statusBarView 到布局中
        val contentLayout = drawerLayout.getChildAt(0) as ViewGroup
        val fakeStatusBarView = contentLayout.findViewById(FAKE_STATUS_BAR_VIEW_ID)
        if (fakeStatusBarView != null) {
            if (fakeStatusBarView!!.getVisibility() === View.GONE) {
                fakeStatusBarView!!.setVisibility(View.VISIBLE)
            }
            fakeStatusBarView!!.setBackgroundColor(color)
        } else {
            contentLayout.addView(createStatusBarView(activity, color), 0)
        }
        // 内容布局不是 LinearLayout 时,设置padding top
        if (contentLayout !is LinearLayout && contentLayout.getChildAt(1) != null) {
            contentLayout.getChildAt(1)
                .setPadding(
                    contentLayout.getPaddingLeft(),
                    getStatusBarHeight(activity) + contentLayout.getPaddingTop(),
                    contentLayout.getPaddingRight(),
                    contentLayout.getPaddingBottom()
                )
        }
        // 设置属性
        setDrawerLayoutProperty(drawerLayout, contentLayout)
        addTranslucentView(activity, statusBarAlpha)
    }

    /**
     * 设置 DrawerLayout 属性
     *
     * @param drawerLayout              DrawerLayout
     * @param drawerLayoutContentLayout DrawerLayout 的内容布局
     */
    private fun setDrawerLayoutProperty(
        drawerLayout: DrawerLayout,
        drawerLayoutContentLayout: ViewGroup
    ) {
        val drawer = drawerLayout.getChildAt(1) as ViewGroup
        drawerLayout.setFitsSystemWindows(false)
        drawerLayoutContentLayout.setFitsSystemWindows(false)
        drawerLayoutContentLayout.setClipToPadding(true)
        drawer.setFitsSystemWindows(false)
    }

    /**
     * 为DrawerLayout 布局设置状态栏变色(5.0以下无半透明效果,不建议使用)
     *
     * @param activity     需要设置的activity
     * @param drawerLayout DrawerLayout
     * @param color        状态栏颜色值
     */
    @Deprecated
    fun setColorForDrawerLayoutDiff(
        activity: Activity,
        drawerLayout: DrawerLayout, @ColorInt color: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            // 生成一个状态栏大小的矩形
            val contentLayout = drawerLayout.getChildAt(0) as ViewGroup
            val fakeStatusBarView = contentLayout.findViewById(FAKE_STATUS_BAR_VIEW_ID)
            if (fakeStatusBarView != null) {
                if (fakeStatusBarView!!.getVisibility() === View.GONE) {
                    fakeStatusBarView!!.setVisibility(View.VISIBLE)
                }
                fakeStatusBarView!!.setBackgroundColor(
                    calculateStatusColor(
                        color,
                        DEFAULT_STATUS_BAR_ALPHA
                    )
                )
            } else {
                // 添加 statusBarView 到布局中
                contentLayout.addView(createStatusBarView(activity, color), 0)
            }
            // 内容布局不是 LinearLayout 时,设置padding top
            if (contentLayout !is LinearLayout && contentLayout.getChildAt(1) != null) {
                contentLayout.getChildAt(1).setPadding(0, getStatusBarHeight(activity), 0, 0)
            }
            // 设置属性
            setDrawerLayoutProperty(drawerLayout, contentLayout)
        }
    }

    /**
     * 为 DrawerLayout 布局设置状态栏透明
     *
     * @param activity     需要设置的activity
     * @param drawerLayout DrawerLayout
     */
    @JvmOverloads
    fun setTranslucentForDrawerLayout(
        activity: Activity, drawerLayout: DrawerLayout,
        @IntRange(from = 0, to = 255) statusBarAlpha: Int = DEFAULT_STATUS_BAR_ALPHA
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return
        }
        setTransparentForDrawerLayout(activity, drawerLayout)
        addTranslucentView(activity, statusBarAlpha)
    }

    /**
     * 为 DrawerLayout 布局设置状态栏透明
     *
     * @param activity     需要设置的activity
     * @param drawerLayout DrawerLayout
     */
    fun setTransparentForDrawerLayout(activity: Activity, drawerLayout: DrawerLayout) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow()
                .addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT)
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }

        val contentLayout = drawerLayout.getChildAt(0) as ViewGroup
        // 内容布局不是 LinearLayout 时,设置padding top
        if (contentLayout !is LinearLayout && contentLayout.getChildAt(1) != null) {
            contentLayout.getChildAt(1).setPadding(0, getStatusBarHeight(activity), 0, 0)
        }

        // 设置属性
        setDrawerLayoutProperty(drawerLayout, contentLayout)
    }

    /**
     * 为 DrawerLayout 布局设置状态栏透明(5.0以上半透明效果,不建议使用)
     *
     * @param activity     需要设置的activity
     * @param drawerLayout DrawerLayout
     */
    @Deprecated
    fun setTranslucentForDrawerLayoutDiff(activity: Activity, drawerLayout: DrawerLayout) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 设置状态栏透明
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            // 设置内容布局属性
            val contentLayout = drawerLayout.getChildAt(0) as ViewGroup
            contentLayout.setFitsSystemWindows(true)
            contentLayout.setClipToPadding(true)
            // 设置抽屉布局属性
            val vg = drawerLayout.getChildAt(1) as ViewGroup
            vg.setFitsSystemWindows(false)
            // 设置 DrawerLayout 属性
            drawerLayout.setFitsSystemWindows(false)
        }
    }

    /**
     * 为头部是 ImageView 的界面设置状态栏全透明
     *
     * @param activity       需要设置的activity
     * @param needOffsetView 需要向下偏移的 View
     */
    fun setTransparentForImageView(activity: Activity, needOffsetView: View) {
        setTranslucentForImageView(activity, 0, needOffsetView)
    }

    /**
     * 为头部是 ImageView 的界面设置状态栏透明(使用默认透明度)
     *
     * @param activity       需要设置的activity
     * @param needOffsetView 需要向下偏移的 View
     */
    fun setTranslucentForImageView(activity: Activity, needOffsetView: View) {
        setTranslucentForImageView(activity, DEFAULT_STATUS_BAR_ALPHA, needOffsetView)
    }

    /**
     * 为头部是 ImageView 的界面设置状态栏透明
     *
     * @param activity       需要设置的activity
     * @param statusBarAlpha 状态栏透明度
     * @param needOffsetView 需要向下偏移的 View
     */
    fun setTranslucentForImageView(
        activity: Activity, @IntRange(from = 0, to = 255) statusBarAlpha: Int,
        needOffsetView: View?
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return
        }
        setTransparentForWindow(activity)
        addTranslucentView(activity, statusBarAlpha)
        if (needOffsetView != null) {
            val haveSetOffset = needOffsetView!!.getTag(TAG_KEY_HAVE_SET_OFFSET)
            if (haveSetOffset != null && haveSetOffset) {
                return
            }
            val layoutParams = needOffsetView!!.getLayoutParams() as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(
                layoutParams.leftMargin, layoutParams.topMargin + getStatusBarHeight(activity),
                layoutParams.rightMargin, layoutParams.bottomMargin
            )
            needOffsetView!!.setTag(TAG_KEY_HAVE_SET_OFFSET, true)
        }
    }

    /**
     * 为 fragment 头部是 ImageView 的设置状态栏透明
     *
     * @param activity       fragment 对应的 activity
     * @param needOffsetView 需要向下偏移的 View
     */
    fun setTranslucentForImageViewInFragment(activity: Activity, needOffsetView: View) {
        setTranslucentForImageViewInFragment(activity, DEFAULT_STATUS_BAR_ALPHA, needOffsetView)
    }

    /**
     * 为 fragment 头部是 ImageView 的设置状态栏透明
     *
     * @param activity       fragment 对应的 activity
     * @param needOffsetView 需要向下偏移的 View
     */
    fun setTransparentForImageViewInFragment(activity: Activity, needOffsetView: View) {
        setTranslucentForImageViewInFragment(activity, 0, needOffsetView)
    }

    /**
     * 为 fragment 头部是 ImageView 的设置状态栏透明
     *
     * @param activity       fragment 对应的 activity
     * @param statusBarAlpha 状态栏透明度
     * @param needOffsetView 需要向下偏移的 View
     */
    fun setTranslucentForImageViewInFragment(
        activity: Activity, @IntRange(from = 0, to = 255) statusBarAlpha: Int,
        needOffsetView: View
    ) {
        setTranslucentForImageView(activity, statusBarAlpha, needOffsetView)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            clearPreviousSetting(activity)
        }
    }

    /**
     * 隐藏伪状态栏 View
     *
     * @param activity 调用的 Activity
     */
    fun hideFakeStatusBarView(activity: Activity) {
        val decorView = activity.getWindow().getDecorView() as ViewGroup
        val fakeStatusBarView = decorView.findViewById(FAKE_STATUS_BAR_VIEW_ID)
        if (fakeStatusBarView != null) {
            fakeStatusBarView!!.setVisibility(View.GONE)
        }
        val fakeTranslucentView = decorView.findViewById(FAKE_TRANSLUCENT_VIEW_ID)
        if (fakeTranslucentView != null) {
            fakeTranslucentView!!.setVisibility(View.GONE)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun setLightMode(activity: Activity) {
        setMIUIStatusBarDarkIcon(activity, true)
        setMeizuStatusBarDarkIcon(activity, true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_VISIBLE)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun setDarkMode(activity: Activity) {
        setMIUIStatusBarDarkIcon(activity, false)
        setMeizuStatusBarDarkIcon(activity, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE)
        }
    }

    /**
     * 修改 MIUI V6  以上状态栏颜色
     */
    private fun setMIUIStatusBarDarkIcon(@NonNull activity: Activity, darkIcon: Boolean) {
        val clazz = activity.getWindow().getClass()
        try {
            val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
            val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
            val darkModeFlag = field.getInt(layoutParams)
            val extraFlagField = clazz.getMethod(
                "setExtraFlags",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            )
            extraFlagField.invoke(
                activity.getWindow(),
                if (darkIcon) darkModeFlag else 0,
                darkModeFlag
            )
        } catch (e: Exception) {
            //e.printStackTrace();
        }

    }

    /**
     * 修改魅族状态栏字体颜色 Flyme 4.0
     */
    private fun setMeizuStatusBarDarkIcon(@NonNull activity: Activity, darkIcon: Boolean) {
        try {
            val lp = activity.getWindow().getAttributes()
            val darkFlag =
                WindowManager.LayoutParams::class.java!!.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
            val meizuFlags = WindowManager.LayoutParams::class.java!!.getDeclaredField("meizuFlags")
            darkFlag.setAccessible(true)
            meizuFlags.setAccessible(true)
            val bit = darkFlag.getInt(null)
            var value = meizuFlags.getInt(lp)
            if (darkIcon) {
                value = value or bit
            } else {
                value = value and bit.inv()
            }
            meizuFlags.setInt(lp, value)
            activity.getWindow().setAttributes(lp)
        } catch (e: Exception) {
            //e.printStackTrace();
        }

    }

    ///////////////////////////////////////////////////////////////////////////////////

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun clearPreviousSetting(activity: Activity) {
        val decorView = activity.getWindow().getDecorView() as ViewGroup
        val fakeStatusBarView = decorView.findViewById(FAKE_STATUS_BAR_VIEW_ID)
        if (fakeStatusBarView != null) {
            decorView.removeView(fakeStatusBarView)
            val rootView =
                (activity.findViewById(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
            rootView.setPadding(0, 0, 0, 0)
        }
    }

    /**
     * 添加半透明矩形条
     *
     * @param activity       需要设置的 activity
     * @param statusBarAlpha 透明值
     */
    private fun addTranslucentView(
        activity: Activity, @IntRange(
            from = 0,
            to = 255
        ) statusBarAlpha: Int
    ) {
        val contentView = activity.findViewById(android.R.id.content) as ViewGroup
        val fakeTranslucentView = contentView.findViewById(FAKE_TRANSLUCENT_VIEW_ID)
        if (fakeTranslucentView != null) {
            if (fakeTranslucentView!!.getVisibility() === View.GONE) {
                fakeTranslucentView!!.setVisibility(View.VISIBLE)
            }
            fakeTranslucentView!!.setBackgroundColor(Color.argb(statusBarAlpha, 0, 0, 0))
        } else {
            contentView.addView(createTranslucentStatusBarView(activity, statusBarAlpha))
        }
    }

    /**
     * 生成一个和状态栏大小相同的半透明矩形条
     *
     * @param activity 需要设置的activity
     * @param color    状态栏颜色值
     * @param alpha    透明值
     * @return 状态栏矩形条
     */
    private fun createStatusBarView(
        activity: Activity, @ColorInt color: Int,
        alpha: Int = 0
    ): View {
        // 绘制一个和状态栏一样高的矩形
        val statusBarView = View(activity)
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            getStatusBarHeight(activity)
        )
        statusBarView.setLayoutParams(params)
        statusBarView.setBackgroundColor(calculateStatusColor(color, alpha))
        statusBarView.setId(FAKE_STATUS_BAR_VIEW_ID)
        return statusBarView
    }

    /**
     * 设置根布局参数
     */
    private fun setRootView(activity: Activity) {
        val parent = activity.findViewById(android.R.id.content) as ViewGroup
        var i = 0
        val count = parent.getChildCount()
        while (i < count) {
            val childView = parent.getChildAt(i)
            if (childView is ViewGroup) {
                childView.setFitsSystemWindows(true)
                (childView as ViewGroup).setClipToPadding(true)
            }
            i++
        }
    }

    /**
     * 设置透明
     */
    private fun setTransparentForWindow(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT)
            activity.getWindow()
                .getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow()
                .setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                )
        }
    }

    /**
     * 使状态栏透明
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun transparentStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow()
                .addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT)
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    /**
     * 创建半透明矩形 View
     *
     * @param alpha 透明值
     * @return 半透明 View
     */
    private fun createTranslucentStatusBarView(activity: Activity, alpha: Int): View {
        // 绘制一个和状态栏一样高的矩形
        val statusBarView = View(activity)
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            getStatusBarHeight(activity)
        )
        statusBarView.setLayoutParams(params)
        statusBarView.setBackgroundColor(Color.argb(alpha, 0, 0, 0))
        statusBarView.setId(FAKE_TRANSLUCENT_VIEW_ID)
        return statusBarView
    }

    /**
     * 获取状态栏高度
     *
     * @param context context
     * @return 状态栏高度
     */
    private fun getStatusBarHeight(context: Context): Int {
        // 获得状态栏高度
        val resourceId =
            context.getResources().getIdentifier("status_bar_height", "dimen", "android")
        return context.getResources().getDimensionPixelSize(resourceId)
    }

    /**
     * 计算状态栏颜色
     *
     * @param color color值
     * @param alpha alpha值
     * @return 最终的状态栏颜色
     */
    private fun calculateStatusColor(@ColorInt color: Int, alpha: Int): Int {
        if (alpha == 0) {
            return color
        }
        val a = 1 - alpha / 255f
        var red = color shr 16 and 0xff
        var green = color shr 8 and 0xff
        var blue = color and 0xff
        red = (red * a + 0.5).toInt()
        green = (green * a + 0.5).toInt()
        blue = (blue * a + 0.5).toInt()
        return 0xff shl 24 or (red shl 16) or (green shl 8) or blue
    }
}
/**
 * 设置状态栏颜色
 *
 * @param activity 需要设置的 activity
 * @param color    状态栏颜色值
 */
/**
 * 为滑动返回界面设置状态栏颜色
 *
 * @param activity 需要设置的activity
 * @param color    状态栏颜色值
 */
/**
 * 使状态栏半透明
 *
 *
 * 适用于图片作为背景的界面,此时需要图片填充到状态栏
 *
 * @param activity 需要设置的activity
 */
/**
 * 为DrawerLayout 布局设置状态栏变色
 *
 * @param activity     需要设置的activity
 * @param drawerLayout DrawerLayout
 * @param color        状态栏颜色值
 */
/**
 * 为 DrawerLayout 布局设置状态栏透明
 *
 * @param activity     需要设置的activity
 * @param drawerLayout DrawerLayout
 */
/**
 * 生成一个和状态栏大小相同的彩色矩形条
 *
 * @param activity 需要设置的 activity
 * @param color    状态栏颜色值
 * @return 状态栏矩形条
 */
