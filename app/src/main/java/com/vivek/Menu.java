package com.vivek;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.text.Html;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.view.animation.DecelerateInterpolator;

public class Menu {

    // Native Functions
    public static native void Functions();

    public static native void ChangesID(int ID, int Value);

    public static native void Init();

    private String target = "com.dts.freefiremax";
    private int injectType;

    // Variables Menu
    public static String userLicenseKey = "admin";
    private int buttonClick = 0;
    public static int PrimaryColor = 0xFF00D2FF; // Golden accent
    public static int TabSelectedColor = 0xFF00D2FF; // Golden accent for selected tabs
    private static Context context;
    private static Utils utils;

    private native String imageBase64();

    // Parte Do Sistema De Janela
    private WindowManager windowManager;
    private WindowManager.LayoutParams windowManagerParams;
    private FrameLayout frameLayout;

    // DrawView Global
    DrawView drawView;

    // Tab Management
    private static Map<String, LinearLayout> tabContentContainers = new HashMap<>();
    private static List<TextView> tabButtons = new ArrayList<>();
    private static String currentTab = "";

    // Parte do Draw
    WindowManager.LayoutParams windowManagerDrawViewParams;

    public static native void OnDrawLoad(DrawView drawView, Canvas canvas);

    public void DrawCanvas() {
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        drawView = new DrawView(context);
        windowManagerDrawViewParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSPARENT);
        windowManagerDrawViewParams.gravity = Gravity.TOP | Gravity.LEFT;
        windowManagerDrawViewParams.x = 0;
        windowManagerDrawViewParams.y = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            windowManagerDrawViewParams.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        windowManager.addView(drawView, windowManagerDrawViewParams);
    }

    // Parte Do Template Do Menu
    private static ScrollView scrollView_center;
    private static LinearLayout tabsContainer;
    private static LinearLayout featuresScrollContainer;

    public Menu(Context globContext, int glob_injectType) {
        context = globContext;
        utils = new Utils(context);
        injectType = glob_injectType;
        if (context != null) {
            String saved = context.getSharedPreferences("VivekPrefs", Context.MODE_PRIVATE)
                    .getString("saved_license", "");
            if (saved != null && !saved.trim().isEmpty()) {
                userLicenseKey = saved.trim();
            }
        }
        System.loadLibrary("hawdawdawdawda");
        onCreate();
    }

    public void onCreate() {
        onCreateSystemWindow();
        onCreateTemplate();
        showNoticeIfAvailable();
    }

    private void showNoticeIfAvailable() {
        if (RemoteConfig.showNotice && RemoteConfig.noticeMessage != null && !RemoteConfig.noticeMessage.isEmpty()) {
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                try {
                    String title = (RemoteConfig.noticeTitle != null && !RemoteConfig.noticeTitle.isEmpty())
                            ? RemoteConfig.noticeTitle
                            : "📢 Notice";
                    new android.app.AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                            .setTitle(title)
                            .setMessage(RemoteConfig.noticeMessage)
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .create()
                            .show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 500);
        }
    }

    public static class FontUtil {
        public static android.graphics.Typeface getAimkillFont(Context context) {
            return android.graphics.Typeface.createFromAsset(context.getAssets(), "fonts/aimkill_font.ttf");
        }
    }

    public static android.graphics.Bitmap logoBitmap;
    public static TextView statusBannerView;

    public static void showActiveToast(final String featureName) {
        if (context == null) return;
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            try {
                if (statusBannerView != null) {
                    statusBannerView.setText("⚡ ACTIVE: " + featureName.toUpperCase());
                    statusBannerView.setTextColor(Color.parseColor("#00E676"));
                }
                Toast toast = Toast.makeText(context, "🟢 " + featureName + " : ACTIVE", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, utils.FixDP(70));
                toast.show();
            } catch (Exception e) {
                Toast.makeText(context, "🟢 " + featureName + " : ACTIVE", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static String getBrandName() {
        return (RemoteConfig.appName != null && !RemoteConfig.appName.isEmpty())
                ? RemoteConfig.appName : "VIVEK PANEL";
    }

    // Criar Template - Modern Cyber Dark VIP Menu
    public void onCreateTemplate() {
        // Luxury Obsidian Glass Container with refined hairline border
        GradientDrawable gradientDrawable_container = new GradientDrawable();
        gradientDrawable_container.setColor(Color.parseColor("#EE070B16")); // Ultra deep luxury dark glass (93% opacity)
        gradientDrawable_container.setCornerRadius(utils.FixDP(14));
        gradientDrawable_container.setStroke(utils.FixDP(1.0f), Color.parseColor("#1B3358")); // Elegant subtle border

        LinearLayout container = new LinearLayout(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            container.setLayoutTransition(new android.animation.LayoutTransition());
        }
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // Main menu container - Compact sleek width (218dp)
        final LinearLayout container_menu = new LinearLayout(context);
        container_menu.setLayoutParams(new LinearLayout.LayoutParams(
                utils.FixDP(218),
                ViewGroup.LayoutParams.WRAP_CONTENT));
        container_menu.setVisibility(View.GONE);
        container_menu.setOrientation(LinearLayout.VERTICAL);
        container_menu.setBackground(gradientDrawable_container);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            container_menu.setElevation(utils.FixDP(14));
        }

        // Floating icon - Compact & sleek (46dp down from 60dp)
        final ImageBase64 icon_cheat = new ImageBase64(context);
        floatingIconView = icon_cheat;
        icon_cheat.setLayoutParams(new LinearLayout.LayoutParams(
                utils.FixDP(46),
                utils.FixDP(46)));
        android.graphics.drawable.Drawable placeholderDrawable = null;
        try {
            byte[] decodeImageBase64 = android.util.Base64.decode(imageBase64(), android.util.Base64.DEFAULT);
            logoBitmap = android.graphics.BitmapFactory.decodeByteArray(decodeImageBase64, 0, decodeImageBase64.length);
            placeholderDrawable = new android.graphics.drawable.BitmapDrawable(context.getResources(), logoBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (placeholderDrawable != null) {
            icon_cheat.setImageDrawable(placeholderDrawable);
        }
        if (RemoteConfig.floatingIconUrl != null && !RemoteConfig.floatingIconUrl.isEmpty()) {
            String floatUrl = RemoteConfig.floatingIconUrl;
            if (floatUrl.contains("?")) {
                floatUrl += "&t=" + System.currentTimeMillis();
            } else {
                floatUrl += "?t=" + System.currentTimeMillis();
            }
            com.bumptech.glide.Glide.with(context)
                    .asBitmap()
                    .load(floatUrl)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                        @Override
                        public void onResourceReady(@androidx.annotation.NonNull android.graphics.Bitmap resource,
                                @androidx.annotation.Nullable com.bumptech.glide.request.transition.Transition<? super android.graphics.Bitmap> transition) {
                            if (resource != null) {
                                logoBitmap = resource;
                                icon_cheat.setImageBitmap(logoBitmap);
                            }
                        }

                        @Override
                        public void onLoadCleared(
                                @androidx.annotation.Nullable android.graphics.drawable.Drawable placeholder) {
                        }
                    });
        }
        GradientDrawable iconBackground = new GradientDrawable();
        iconBackground.setShape(GradientDrawable.OVAL);
        iconBackground.setColor(Color.parseColor("#DD111114"));
        iconBackground.setStroke(utils.FixDP(1.2f), Color.parseColor("#8800D2FF"));
        icon_cheat.setBackground(iconBackground);
        icon_cheat.setPadding(utils.FixDP(4), utils.FixDP(4), utils.FixDP(4), utils.FixDP(4));
        icon_cheat.setOnTouchListener(onTouchListener());
        icon_cheat.setOnClickListener(view -> {
            icon_cheat.setVisibility(View.GONE);
            container_menu.setVisibility(View.VISIBLE);
            try {
                windowManager.updateViewLayout(frameLayout, windowManagerParams);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Top section of the menu with refined header
        LinearLayout container_top = new LinearLayout(context);
        container_top.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        container_top.setPadding(
                utils.FixDP(10),
                utils.FixDP(7),
                utils.FixDP(10),
                utils.FixDP(6));
        container_top.setGravity(Gravity.CENTER_VERTICAL);
        container_top.setOrientation(LinearLayout.HORIZONTAL);

        // Menu icon in top bar - Compact (26x26)
        ImageBase64 icon_menu = new ImageBase64(context);
        icon_menu.setLayoutParams(new LinearLayout.LayoutParams(
                utils.FixDP(26),
                utils.FixDP(26)));
        if (placeholderDrawable != null) {
            icon_menu.setImageDrawable(placeholderDrawable);
        }
        if (RemoteConfig.floatingIconUrl != null && !RemoteConfig.floatingIconUrl.isEmpty()) {
            com.bumptech.glide.Glide.with(context)
                    .load(RemoteConfig.floatingIconUrl)
                    .placeholder(placeholderDrawable)
                    .error(placeholderDrawable)
                    .into(icon_menu);
        }

        // Title layout
        LinearLayout titleCol = new LinearLayout(context);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams titleColParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        titleColParams.setMargins(utils.FixDP(7), 0, 0, 0);
        titleCol.setLayoutParams(titleColParams);

        TextView menuTitle = new TextView(context);
        String appDisplayName = (RemoteConfig.appName != null && !RemoteConfig.appName.isEmpty())
                ? RemoteConfig.appName : "VIVEK PANEL";
        menuTitle.setText(appDisplayName);
        menuTitle.setTextSize(11.5f);
        menuTitle.setTextColor(PrimaryColor);
        menuTitle.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        titleCol.addView(menuTitle);

        TextView menuSub = new TextView(context);
        menuSub.setText("VIP AIMBOT & ESP");
        menuSub.setTextSize(7.5f);
        menuSub.setTextColor(Color.parseColor("#8E8EA0"));
        menuSub.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        titleCol.addView(menuSub);

        // Live badge on top-right - Sleek mini badge
        TextView liveBadge = new TextView(context);
        liveBadge.setText("● LIVE");
        liveBadge.setTextSize(7.5f);
        liveBadge.setTextColor(Color.parseColor("#00E676"));
        liveBadge.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        liveBadge.setPadding(utils.FixDP(4.5f), utils.FixDP(1.5f), utils.FixDP(4.5f), utils.FixDP(1.5f));
        GradientDrawable liveBadgeBg = new GradientDrawable();
        liveBadgeBg.setColor(Color.parseColor("#0C2417"));
        liveBadgeBg.setCornerRadius(utils.FixDP(5));
        liveBadgeBg.setStroke(utils.FixDP(0.8f), Color.parseColor("#00E676"));
        liveBadge.setBackground(liveBadgeBg);

        container_top.addView(icon_menu);
        container_top.addView(titleCol);
        container_top.addView(liveBadge);

        // Glowing divider line - Hairline
        View headerDivider = new View(context);
        headerDivider.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, utils.FixDP(0.8f)));
        headerDivider.setBackgroundColor(Color.parseColor("#22222B"));

        // Center section where features will be displayed - Compact height (210dp)
        final LinearLayout container_center = new LinearLayout(context);
        container_center.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                utils.FixDP(210)));
        container_center.setGravity(Gravity.CENTER);
        container_center.setPadding(utils.FixDP(6), utils.FixDP(3), utils.FixDP(6), utils.FixDP(3));

        // Scroll view for features
        scrollView_center = new ScrollView(context);
        scrollView_center.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        scrollView_center.setVerticalScrollBarEnabled(false);
        scrollView_center.setPadding(0, utils.FixDP(1), 0, utils.FixDP(1));

        // Container for all feature tabs
        featuresScrollContainer = new LinearLayout(context);
        featuresScrollContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        featuresScrollContainer.setOrientation(LinearLayout.VERTICAL);

        scrollView_center.addView(featuresScrollContainer);

        // Progress bar
        final ProgressBar progressBar = new ProgressBar(context);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(
                utils.FixDP(36),
                utils.FixDP(36)));
        progressBar.getIndeterminateDrawable().setColorFilter(PrimaryColor, PorterDuff.Mode.SRC_IN);

        // Bottom section with status banner and close button - Compact
        LinearLayout container_bottom = new LinearLayout(context);
        container_bottom.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        container_bottom.setPadding(
                utils.FixDP(8),
                utils.FixDP(2),
                utils.FixDP(8),
                utils.FixDP(8));
        container_bottom.setOrientation(LinearLayout.VERTICAL);
        container_bottom.setGravity(Gravity.CENTER);

        // Active Status Chip
        statusBannerView = new TextView(context);
        statusBannerView.setText("⚡ SYSTEM: READY");
        statusBannerView.setTextSize(8.5f);
        statusBannerView.setTextColor(Color.parseColor("#8E8EA0"));
        statusBannerView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        statusBannerView.setGravity(Gravity.CENTER);
        statusBannerView.setPadding(0, 0, 0, utils.FixDP(3));
        container_bottom.addView(statusBannerView);

        // Button styling - Compact Golden Gradient Pill Button
        GradientDrawable gradientDrawable_inject_close = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#FFB800"), Color.parseColor("#E09600")});
        gradientDrawable_inject_close.setCornerRadius(utils.FixDP(16));
        RippleDrawable rippleDrawable = new RippleDrawable(
                ColorStateList.valueOf(0x33000000),
                gradientDrawable_inject_close,
                null);

        // Inject/Close button
        final Button inject_close = new Button(context);
        inject_close.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                utils.FixDP(32)));
        inject_close.setPadding(0, 0, 0, 0);
        inject_close.setText("INJECT");
        inject_close.setTextSize(11f);
        inject_close.setTextColor(0xFF000000); // Crisp dark contrast on gold
        inject_close.setBackground(rippleDrawable);
        inject_close.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        inject_close.setOnClickListener(view -> {
            if (buttonClick == 0) {
                Toast.makeText(context, "Processing injection...", Toast.LENGTH_SHORT).show();

                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        Init();
                        Functions();
                    } catch (UnsatisfiedLinkError e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                    inject_close.setText("CLOSE");
                    container_center.removeAllViews();
                    container_center.addView(scrollView_center);
                    buttonClick++;
                    Toast.makeText(context, "✅ Injection successful!", Toast.LENGTH_SHORT).show();
                }, 800);

            } else if (buttonClick == 1) {
                icon_cheat.setVisibility(View.VISIBLE);
                container_menu.setVisibility(View.GONE);
                try {
                    windowManager.updateViewLayout(frameLayout, windowManagerParams);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Add all views to their respective containers
        frameLayout.addView(container);
        container.addView(icon_cheat);
        container.addView(container_menu);

        container_menu.addView(container_top);
        container_menu.addView(headerDivider);

        container_menu.addView(container_center);
        container_center.addView(progressBar);

        container_menu.addView(container_bottom);
        container_bottom.addView(inject_close);
    }

    // Create System Window
    public void onCreateSystemWindow() {
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        frameLayout = new FrameLayout(context);
        frameLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        frameLayout.setOnTouchListener(onTouchListener());
        frameLayout.setAlpha(0.95f);

        windowManagerParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM |
                        WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                PixelFormat.TRANSPARENT);
        windowManagerParams.gravity = Gravity.TOP | Gravity.LEFT;
        windowManagerParams.x = 50;
        windowManagerParams.y = 100;

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DrawCanvas();
        windowManager.addView(frameLayout, windowManagerParams);
    }

    // OnTouchListener for menu
    private View.OnTouchListener onTouchListener() {
        return new View.OnTouchListener() {
            private static final int TOUCH_MOVE_THRESHOLD = 8;
            private int x;
            private int y;
            private int initialX;
            private int initialY;
            private boolean isMoving = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = (int) event.getRawX();
                        y = (int) event.getRawY();
                        initialX = x;
                        initialY = y;
                        isMoving = false;
                        frameLayout.setAlpha(0.8f);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        int nowX = (int) event.getRawX();
                        int nowY = (int) event.getRawY();

                        int totalMoveX = Math.abs(nowX - initialX);
                        int totalMoveY = Math.abs(nowY - initialY);

                        if (!isMoving && (totalMoveX > TOUCH_MOVE_THRESHOLD || totalMoveY > TOUCH_MOVE_THRESHOLD)) {
                            isMoving = true;
                        }

                        if (isMoving) {
                            int movedX = nowX - x;
                            int movedY = nowY - y;
                            x = nowX;
                            y = nowY;
                            windowManagerParams.x = windowManagerParams.x + movedX;
                            windowManagerParams.y = windowManagerParams.y + movedY;
                            windowManager.updateViewLayout(frameLayout, windowManagerParams);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!isMoving) {
                            v.performClick();
                        }
                        frameLayout.setAlpha(0.95f);
                        return true;

                    default:
                        break;
                }
                return false;
            }
        };
    }

    // -------------------- NEW TAB METHODS --------------------

    /**
     * Create a new tab and its content container (hidden buttons)
     * 
     * @param tabName name of the tab
     */
    public static void addTab(final String tabName) {
        final boolean isFirstTab = tabButtons.isEmpty();

        // Tab button (hidden by default)
        final TextView tabButton = new TextView(context);
        tabButton.setVisibility(View.GONE); // ⬅️ HIDE IT
        tabButtons.add(tabButton);

        if (tabsContainer != null) {
            // Still add to container but invisible
            tabsContainer.addView(tabButton);
        }

        // Create content for this tab - Compact padding
        LinearLayout tabContent = new LinearLayout(context);
        tabContent.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        tabContent.setOrientation(LinearLayout.VERTICAL);
        tabContent.setPadding(utils.FixDP(6), utils.FixDP(2), utils.FixDP(6), utils.FixDP(2));
        tabContent.setVisibility(isFirstTab ? View.VISIBLE : View.GONE);

        // Store & add it
        tabContentContainers.put(tabName, tabContent);
        featuresScrollContainer.addView(tabContent);

        if (isFirstTab)
            currentTab = tabName;
    }

    /**
     * Select a tab and show its content
     */
    private static void selectTab(String tabName) {
        if (tabName.equals(currentTab))
            return;

        for (Map.Entry<String, LinearLayout> entry : tabContentContainers.entrySet()) {
            entry.getValue().setVisibility(entry.getKey().equals(tabName) ? View.VISIBLE : View.GONE);
        }

        currentTab = tabName;
    }

    /**
     * Add a category heading within the current tab
     */
    public static void addCategory(String name) {
        if (currentTab.isEmpty() || !tabContentContainers.containsKey(currentTab)) {
            return; // No tab selected
        }

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.parseColor("#18140E")); // Subtle amber tinted cyber pill
        gradientDrawable.setCornerRadius(utils.FixDP(5));
        gradientDrawable.setStroke(utils.FixDP(0.8f), Color.parseColor("#33FFB800"));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                utils.FixDP(20)));
        linearLayout.setBackground(gradientDrawable);
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);
        linearLayout.setPadding(utils.FixDP(7), 0, utils.FixDP(7), 0);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) linearLayout.getLayoutParams();
        params.setMargins(0, utils.FixDP(5), 0, utils.FixDP(2.5f));
        linearLayout.setLayoutParams(params);

        TextView textView = new TextView(context);
        textView.setText("⚡  " + name.toUpperCase());
        textView.setTextSize(9f);
        textView.setTextColor(PrimaryColor);
        textView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        linearLayout.addView(textView);
        tabContentContainers.get(currentTab).addView(linearLayout);
    }

    public static boolean isMasterActive = false;
    private static final java.util.List<View> lockedRowViews = new java.util.ArrayList<>();
    private static final java.util.List<SwitchStyle> lockedSwitches = new java.util.ArrayList<>();
    private static final java.util.List<SeekBar> lockedSeekBars = new java.util.ArrayList<>();

    public static ImageBase64 floatingIconView;

    public static void vibrateClick() {
        if (context == null) return;
        try {
            android.os.Vibrator v = (android.os.Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(android.os.VibrationEffect.createOneShot(22, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    v.vibrate(22);
                }
            }
        } catch (Exception ignored) {}
    }

    public static void vibrateWarning() {
        if (context == null) return;
        try {
            android.os.Vibrator v = (android.os.Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(android.os.VibrationEffect.createWaveform(new long[]{0, 35, 50, 45}, -1));
                } else {
                    v.vibrate(new long[]{0, 35, 50, 45}, -1);
                }
            }
        } catch (Exception ignored) {}
    }

    public static void vibrateMasterActivate() {
        if (context == null) return;
        try {
            android.os.Vibrator v = (android.os.Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(android.os.VibrationEffect.createWaveform(new long[]{0, 50, 70, 75}, -1));
                } else {
                    v.vibrate(new long[]{0, 50, 70, 75}, -1);
                }
            }
        } catch (Exception ignored) {}
    }

    public static void showLockedToast() {
        vibrateWarning();
        if (context == null) return;
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            try {
                Toast toast = Toast.makeText(context, "🔒 Please turn ON 'Activate All' first!", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, utils.FixDP(60));
                toast.show();
            } catch (Exception ignored) {}
        });
    }

    public static void setFeaturesLocked(boolean unlocked) {
        isMasterActive = unlocked;
        for (View v : lockedRowViews) {
            v.animate().alpha(unlocked ? 1.0f : 0.40f).setDuration(220).start();
        }
        for (SwitchStyle sw : lockedSwitches) {
            sw.setEnabled(unlocked);
            if (!unlocked && sw.isChecked()) {
                sw.setChecked(false);
            }
        }
        for (SeekBar sb : lockedSeekBars) {
            sb.setEnabled(unlocked);
        }
        if (statusBannerView != null) {
            if (unlocked) {
                statusBannerView.setText("⚡ ALL FEATURES UNLOCKED");
                statusBannerView.setTextColor(Color.parseColor("#00E676"));
            } else {
                statusBannerView.setText("🔒 LOCKED: ACTIVATE ALL REQUIRED");
                statusBannerView.setTextColor(Color.parseColor("#FF5252"));
            }
        }
    }

    /**
     * Add a switch to the current tab - Compact row with locking support
     */
    public static void addSwitch(String name, final int ID) {
        LinearLayout rowCard = new LinearLayout(context);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, utils.FixDP(2.2f), 0, utils.FixDP(2.2f));
        rowCard.setLayoutParams(rowParams);
        rowCard.setPadding(utils.FixDP(10), utils.FixDP(5.5f), utils.FixDP(8), utils.FixDP(5.5f));
        rowCard.setOrientation(LinearLayout.HORIZONTAL);
        rowCard.setGravity(Gravity.CENTER_VERTICAL);

        GradientDrawable rowBg = new GradientDrawable();
        rowBg.setColor(Color.parseColor("#15151B"));
        rowBg.setCornerRadius(utils.FixDP(8));
        rowBg.setStroke(utils.FixDP(0.8f), Color.parseColor("#262634"));
        rowCard.setBackground(rowBg);

        final TextView textView = new TextView(context);
        textView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        textView.setText(name);
        textView.setTextSize(11.5f);
        textView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        final SwitchStyle switchStyle = new SwitchStyle(context);
        switchStyle.setLayoutParams(new LinearLayout.LayoutParams(utils.FixDP(38), utils.FixDP(21)));

        final int colorOff = 0xFF8E8EA2;
        final int colorOn = 0xFFFFFFFF;

        textView.setTextColor(switchStyle.isChecked() ? colorOn : colorOff);

        final boolean isMasterSwitch = (ID == 102);

        if (!isMasterSwitch) {
            lockedRowViews.add(rowCard);
            lockedSwitches.add(switchStyle);
            rowCard.setAlpha(isMasterActive ? 1.0f : 0.40f);
            switchStyle.setEnabled(isMasterActive);
        } else {
            rowCard.setAlpha(1.0f);
            switchStyle.setEnabled(true);
        }

        switchStyle.setOnCheckedChangeListener((view, isChecked) -> {
            if (!isMasterSwitch && !isMasterActive) {
                if (isChecked) {
                    switchStyle.setChecked(false);
                    showLockedToast();
                }
                return;
            }

            vibrateClick();
            ChangesID(ID, 0);

            if (isMasterSwitch) {
                setFeaturesLocked(isChecked);
                if (isChecked) {
                    vibrateMasterActivate();
                }
            }

            if (isChecked) {
                showActiveToast(name);
            }

            int startColor = textView.getCurrentTextColor();
            int endColor = isChecked ? colorOn : colorOff;

            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
            colorAnimation.setDuration(200);
            colorAnimation.setInterpolator(new DecelerateInterpolator());
            colorAnimation.addUpdateListener(animator -> textView.setTextColor((int) animator.getAnimatedValue()));
            colorAnimation.start();
        });

        rowCard.setOnClickListener(view -> {
            if (!isMasterSwitch && !isMasterActive) {
                showLockedToast();
                return;
            }
            switchStyle.setChecked(!switchStyle.isChecked());
        });

        rowCard.addView(textView);
        rowCard.addView(switchStyle);
        tabContentContainers.get(currentTab).addView(rowCard);
    }

    public static void addSeekBar(final String name, int value, int max, final String type, final int ID) {
        LinearLayout rowCard = new LinearLayout(context);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, utils.FixDP(2.2f), 0, utils.FixDP(2.2f));
        rowCard.setLayoutParams(rowParams);
        rowCard.setPadding(utils.FixDP(10), utils.FixDP(6f), utils.FixDP(10), utils.FixDP(6f));
        rowCard.setOrientation(LinearLayout.VERTICAL);

        GradientDrawable rowBg = new GradientDrawable();
        rowBg.setColor(Color.parseColor("#15151B"));
        rowBg.setCornerRadius(utils.FixDP(8));
        rowBg.setStroke(utils.FixDP(0.8f), Color.parseColor("#262634"));
        rowCard.setBackground(rowBg);

        final TextView textView = new TextView(context);
        textView.setText(name + ": " + value + type);
        textView.setTextSize(10.5f);
        textView.setTextColor(0xFFFFFFFF);
        textView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        if (type.equals("Color")) {
            if (value == 0) {
                textView.setText(Html.fromHtml(name + ": <font color='#ffffff'>" + "White" + "</font>"));
            } else if (value == 1) {
                textView.setText(Html.fromHtml(name + ": <font color='#00FF00'>" + "Green" + "</font>"));
            } else if (value == 2) {
                textView.setText(Html.fromHtml(name + ": <font color='#0000FF'>" + "Blue" + "</font>"));
            } else if (value == 3) {
                textView.setText(Html.fromHtml(name + ": <font color='#FF0000'>" + "Red" + "</font>"));
            } else if (value == 4) {
                textView.setText(Html.fromHtml(name + ": <font color='#000000'>" + "Black" + "</font>"));
            } else if (value == 5) {
                textView.setText(Html.fromHtml(name + ": <font color='#FFFF00'>" + "Yellow" + "</font>"));
            } else if (value == 6) {
                textView.setText(Html.fromHtml(name + ": <font color='#00FFFF'>" + "Cyan" + "</font>"));
            } else if (value == 7) {
                textView.setText(Html.fromHtml(name + ": <font color='#FF00FF'>" + "Magenta" + "</font>"));
            } else if (value == 8) {
                textView.setText(Html.fromHtml(name + ": <font color='#808080'>" + "Gray" + "</font>"));
            } else if (value == 9) {
                textView.setText(Html.fromHtml(name + ": <font color='#A020F0'>" + "Purple" + "</font>"));
            }
        } else if (type.equals("BoxType")) {
            if (value == 0) {
                textView.setText(name.concat(": Stroke"));
            } else if (value == 1) {
                textView.setText(name.concat(": Filled"));
            } else if (value == 2) {
                textView.setText(name.concat(": Rounded"));
            }
        } else if (type.equals("LineType")) {
            if (value == 0) {
                textView.setText(name.concat(": Top"));
            } else if (value == 1) {
                textView.setText(name.concat(": Center"));
            } else if (value == 2) {
                textView.setText(name.concat(": Bottom"));
            }
        }

        SeekBar seekBar = new SeekBar(context);
        seekBar.getThumb().setColorFilter(PrimaryColor, PorterDuff.Mode.SRC_IN);
        seekBar.getProgressDrawable().setColorFilter(PrimaryColor, PorterDuff.Mode.SRC_IN);
        seekBar.setProgress(value);
        seekBar.setMax(max);
        if (type.equals("Color")) {
            seekBar.setMax(9);
        } else if (type.equals("BoxType")) {
            seekBar.setMax(2);
        } else if (type.equals("LineType")) {
            seekBar.setMax(2);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (type.equals("Color")) {
                    if (i == 0) {
                        textView.setText(Html.fromHtml(name + ": <font color='#ffffff'>" + "White" + "</font>"));
                    } else if (i == 1) {
                        textView.setText(Html.fromHtml(name + ": <font color='#00FF00'>" + "Green" + "</font>"));
                    } else if (i == 2) {
                        textView.setText(Html.fromHtml(name + ": <font color='#0000FF'>" + "Blue" + "</font>"));
                    } else if (i == 3) {
                        textView.setText(Html.fromHtml(name + ": <font color='#FF0000'>" + "Red" + "</font>"));
                    } else if (i == 4) {
                        textView.setText(Html.fromHtml(name + ": <font color='#000000'>" + "Black" + "</font>"));
                    } else if (i == 5) {
                        textView.setText(Html.fromHtml(name + ": <font color='#FFFF00'>" + "Yellow" + "</font>"));
                    } else if (i == 6) {
                        textView.setText(Html.fromHtml(name + ": <font color='#00FFFF'>" + "Cyan" + "</font>"));
                    } else if (i == 7) {
                        textView.setText(Html.fromHtml(name + ": <font color='#FF00FF'>" + "Magenta" + "</font>"));
                    } else if (i == 8) {
                        textView.setText(Html.fromHtml(name + ": <font color='#808080'>" + "Gray" + "</font>"));
                    } else if (i == 9) {
                        textView.setText(Html.fromHtml(name + ": <font color='#A020F0'>" + "Purple" + "</font>"));
                    }
                } else if (type.equals("BoxType")) {
                    if (i == 0) {
                        textView.setText(name.concat(": Stroke"));
                    } else if (i == 1) {
                        textView.setText(name.concat(": Filled"));
                    } else if (i == 2) {
                        textView.setText(name.concat(": Corner"));
                    }
                } else if (type.equals("LineType")) {
                    if (i == 0) {
                        textView.setText(name.concat(": Top"));
                    } else if (i == 1) {
                        textView.setText(name.concat(": Center"));
                    } else if (i == 2) {
                        textView.setText(name.concat(": Bottom"));
                    }
                } else {
                    textView.setText(name.concat(": ") + i + type);
                }

                ChangesID(ID, i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                vibrateClick();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        lockedRowViews.add(rowCard);
        lockedSeekBars.add(seekBar);
        rowCard.setAlpha(isMasterActive ? 1.0f : 0.40f);
        seekBar.setEnabled(isMasterActive);

        rowCard.setOnClickListener(view -> {
            if (!isMasterActive) {
                showLockedToast();
            }
        });

        seekBar.setOnTouchListener((view, motionEvent) -> {
            if (!isMasterActive) {
                showLockedToast();
                return true;
            }
            return false;
        });

        rowCard.addView(textView);
        rowCard.addView(seekBar);
        tabContentContainers.get(currentTab).addView(rowCard);
    }

    private static int currentFovColor = 0xFF00FFFF;

    public static void addColorPicker(final String name, final int ID) {
        LinearLayout rowCard = new LinearLayout(context);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, utils.FixDP(2.2f), 0, utils.FixDP(2.2f));
        rowCard.setLayoutParams(rowParams);
        rowCard.setPadding(utils.FixDP(10), utils.FixDP(7f), utils.FixDP(10), utils.FixDP(7f));
        rowCard.setOrientation(LinearLayout.HORIZONTAL);
        rowCard.setGravity(Gravity.CENTER_VERTICAL);

        GradientDrawable rowBg = new GradientDrawable();
        rowBg.setColor(Color.parseColor("#15151B"));
        rowBg.setCornerRadius(utils.FixDP(8));
        rowBg.setStroke(utils.FixDP(0.8f), Color.parseColor("#262634"));
        rowCard.setBackground(rowBg);

        // Left Col: Title and Hint
        LinearLayout textCol = new LinearLayout(context);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView titleView = new TextView(context);
        titleView.setText("🎨 " + name);
        titleView.setTextSize(11f);
        titleView.setTextColor(0xFFFFFFFF);
        titleView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        textCol.addView(titleView);

        TextView subView = new TextView(context);
        subView.setText("Tap to open 360° color wheel");
        subView.setTextSize(7.5f);
        subView.setTextColor(Color.parseColor("#8E8EA0"));
        subView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        textCol.addView(subView);

        // Right Col: Circular Preview Swatch Disc
        final View previewSwatch = new View(context);
        int swatchSize = utils.FixDP(22);
        LinearLayout.LayoutParams swatchParams = new LinearLayout.LayoutParams(swatchSize, swatchSize);
        previewSwatch.setLayoutParams(swatchParams);
        final GradientDrawable swatchBg = new GradientDrawable();
        swatchBg.setShape(GradientDrawable.OVAL);
        swatchBg.setColor(currentFovColor);
        swatchBg.setStroke(utils.FixDP(1.5f), Color.parseColor("#FFB800"));
        previewSwatch.setBackground(swatchBg);

        rowCard.addView(textCol);
        rowCard.addView(previewSwatch);

        rowCard.setOnClickListener(v -> {
            if (!isMasterActive) {
                showLockedToast();
                return;
            }
            vibrateClick();
            showColorWheelDialog(ID, previewSwatch, swatchBg);
        });

        lockedRowViews.add(rowCard);
        rowCard.setAlpha(isMasterActive ? 1.0f : 0.40f);

        tabContentContainers.get(currentTab).addView(rowCard);
    }

    public static void showColorWheelDialog(final int ID, final View previewSwatch, final GradientDrawable swatchBg) {
        if (context == null) return;
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            try {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert);
                LinearLayout dialogLayout = new LinearLayout(context);
                dialogLayout.setOrientation(LinearLayout.VERTICAL);
                dialogLayout.setPadding(utils.FixDP(18), utils.FixDP(14), utils.FixDP(18), utils.FixDP(14));

                TextView title = new TextView(context);
                title.setText("🎨 FOV COLOR SPECTRUM WHEEL");
                title.setTextSize(12f);
                title.setTextColor(Color.parseColor("#FFB800"));
                title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                title.setGravity(Gravity.CENTER);
                dialogLayout.addView(title);

                final View liveBox = new View(context);
                LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, utils.FixDP(42));
                boxParams.setMargins(0, utils.FixDP(10), 0, utils.FixDP(10));
                liveBox.setLayoutParams(boxParams);
                final GradientDrawable liveBoxBg = new GradientDrawable();
                liveBoxBg.setCornerRadius(utils.FixDP(8));
                liveBoxBg.setColor(currentFovColor);
                liveBoxBg.setStroke(utils.FixDP(1.5f), Color.WHITE);
                liveBox.setBackground(liveBoxBg);
                dialogLayout.addView(liveBox);

                final TextView hexLabel = new TextView(context);
                hexLabel.setText("COLOR CODE: #" + Integer.toHexString(currentFovColor).toUpperCase());
                hexLabel.setTextSize(10f);
                hexLabel.setTextColor(Color.WHITE);
                hexLabel.setGravity(Gravity.CENTER);
                dialogLayout.addView(hexLabel);

                TextView sliderLabel = new TextView(context);
                sliderLabel.setText("Slide 360° Color Spectrum Wheel:");
                sliderLabel.setTextSize(9.5f);
                sliderLabel.setTextColor(Color.parseColor("#AAAAAA"));
                sliderLabel.setPadding(0, utils.FixDP(8), 0, utils.FixDP(4));
                dialogLayout.addView(sliderLabel);

                SeekBar hueBar = new SeekBar(context);
                hueBar.setMax(360);
                hueBar.setProgress(180);
                hueBar.getThumb().setColorFilter(Color.parseColor("#FFB800"), PorterDuff.Mode.SRC_IN);
                hueBar.getProgressDrawable().setColorFilter(Color.parseColor("#00E676"), PorterDuff.Mode.SRC_IN);

                final int[] pickedColor = new int[]{currentFovColor};

                hueBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        float[] hsv = new float[]{(float) progress, 1.0f, 1.0f};
                        int c = Color.HSVToColor(hsv);
                        pickedColor[0] = c;
                        liveBoxBg.setColor(c);
                        liveBox.invalidate();
                        hexLabel.setText("COLOR CODE: #" + Integer.toHexString(c).toUpperCase());
                        ChangesID(ID, c);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        vibrateClick();
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        vibrateClick();
                    }
                });
                dialogLayout.addView(hueBar);

                builder.setView(dialogLayout);
                builder.setPositiveButton("APPLY COLOR", (dialog, which) -> {
                    vibrateClick();
                    currentFovColor = pickedColor[0];
                    ChangesID(ID, pickedColor[0]);
                    swatchBg.setColor(pickedColor[0]);
                    previewSwatch.invalidate();
                    dialog.dismiss();
                });
                builder.setNeutralButton("🌈 RGB RAINBOW", (dialog, which) -> {
                    vibrateClick();
                    ChangesID(ID, -1);
                    swatchBg.setColor(Color.parseColor("#FFB800"));
                    previewSwatch.invalidate();
                    dialog.dismiss();
                });
                builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());

                android.app.AlertDialog dialog = builder.create();
                if (dialog.getWindow() != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                    } else {
                        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
                    }
                }
                dialog.show();
            } catch (Exception ignored) {}
        });
    }

    // Injection methods
    private boolean InjectX86(String Lib) {
        try {
            String injector = context.getApplicationInfo().nativeLibraryDir + File.separator + "libupakul.so";
            String payload_source = context.getApplicationInfo().nativeLibraryDir + File.separator + Lib;
            String payload_dest = "/data/local/" + Lib;
            String payload_dest2 = "/data/local/libifuhiufoi.so";

            Shell.su("cp " + payload_source + " " + payload_dest).exec();
            Shell.su("cp " + injector + " " + payload_dest2).exec();
            Shell.su("su -c chmod 777 " + payload_dest).exec();
            Shell.su("su -c chmod 777 " + payload_dest2).exec();
            Shell.su("su -c " + payload_dest2 + " " + target + " " + payload_dest).exec();
            Shell.su("rm -f " + payload_dest).exec();
            Shell.su("rm -f " + payload_dest2).exec();
            Functions();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean InjectX32(String Lib) {
        try {
            String injector = context.getApplicationInfo().nativeLibraryDir + File.separator + "libinjectMobile.so";
            String payload_source = context.getApplicationInfo().nativeLibraryDir + File.separator + Lib;
            String payload_dest = "/data/local/" + Lib;
            String payload_dest2 = "/data/local/libinject.so";

            Shell.su("cp " + payload_source + " " + payload_dest).exec();
            Shell.su("cp " + injector + " " + payload_dest2).exec();
            Shell.su("su -c chmod 755 " + payload_dest).exec();
            Shell.su("su -c chmod 777 " + payload_dest2).exec();
            Shell.su("su -c " + payload_dest2 + " -f -n " + target + " -so " + payload_dest + " --hide-memory").exec();
            Shell.su("rm -f " + payload_dest).exec();
            Shell.su("rm -f " + payload_dest2).exec();
            Functions();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }
}