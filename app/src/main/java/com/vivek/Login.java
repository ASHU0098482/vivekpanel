package com.vivek;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.*;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Login {
    private Context context;
    private Utils utils;
    private ProgressBar loadingBar;
    private TextView loadingText;
    private boolean isSettingsVisible = false;

    private LinearLayout rootContainer, card;
    private LinearLayout inputContainer;
    private EditText inputLicense;
    private Button pasteButton;
    private Button loginButton;
    private TextView title, subtitle;
    private LinearLayout settingsLayout;
    private Switch suToggle;
    private TextView suLabel;
    public native void sendOwnerIDToNative(String ownerId);

    public static Context globalContext;

    private static final String APP_NAME = "vip panel";
    private static final String OWNER_ID = "8Z9qRQ2zph";
    private static final String SECRET = "fddc19ec5be9ebee148b808beaa5dad04f803aac21cf6f4a224a5f832ef97dbd";
    private static final String VERSION = "1.0";
    private static final String API_URL = "https://keyauth.win/api/1.3/";

    static {
        System.loadLibrary("hawdawdawdawda");
    }

    public Login(Context context) {
        Login.globalContext = context;
        this.context = context;
        this.utils = new Utils(context);
        Init();
    }

    private void triggerHaptic(int ms) {
        try {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(ms);
            }
        } catch (Exception ignored) {}
    }

    private void Init() {
        showNoticeIfAvailable();

        // =========================================================================
        // STEP 1: Build the Main Obsidian Glass Login Card
        // =========================================================================
        card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(
                utils.FixDP(20),
                utils.FixDP(20),
                utils.FixDP(20),
                utils.FixDP(20)
        );

        // Obsidian Glass Background with smooth 22dp squircle corners
        final GradientDrawable cardBg = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {
                        Color.parseColor("#EE080C16"),
                        Color.parseColor("#F404070F")
                }
        );
        cardBg.setCornerRadius(utils.FixDP(22));
        cardBg.setStroke(utils.FixDP(1.5f), Color.parseColor("#00D2FF"));
        card.setBackground(cardBg);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            card.setElevation(utils.FixDP(16));
        }

        // Breathing Animated Dual-Tone Neon Stroke on Card
        ValueAnimator cardBorderGlow = ValueAnimator.ofObject(
                new ArgbEvaluator(),
                Color.parseColor("#00D2FF"), // Golden Amber
                Color.parseColor("#38E1FF"), // Cyber Yellow Gold
                Color.parseColor("#0084FF"), // Deep Warm Amber
                Color.parseColor("#0055FF"), // Neon Dark Orange
                Color.parseColor("#00D2FF")
        );
        cardBorderGlow.setDuration(4000);
        cardBorderGlow.setRepeatCount(ValueAnimator.INFINITE);
        cardBorderGlow.setRepeatMode(ValueAnimator.RESTART);
        cardBorderGlow.addUpdateListener(anim -> {
            int color = (int) anim.getAnimatedValue();
            cardBg.setStroke(utils.FixDP(1.5f), color);
        });
        cardBorderGlow.start();

        // --- 1.1 Top VIP Pill Badge ---
        TextView vipBadge = new TextView(context);
        vipBadge.setText("✦ WELCOME TO VIVEK PANEL ✦");
        vipBadge.setTextColor(Color.parseColor("#00D2FF"));
        vipBadge.setTextSize(9.5f);
        vipBadge.setTypeface(Typeface.DEFAULT_BOLD);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            vipBadge.setLetterSpacing(0.14f);
        }
        vipBadge.setGravity(Gravity.CENTER);

        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(Color.parseColor("#22FFB800"));
        badgeBg.setCornerRadius(utils.FixDP(20));
        badgeBg.setStroke(utils.FixDP(1), Color.parseColor("#66FFB800"));
        vipBadge.setBackground(badgeBg);
        vipBadge.setPadding(utils.FixDP(12), utils.FixDP(4), utils.FixDP(12), utils.FixDP(4));

        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        badgeParams.setMargins(0, 0, 0, utils.FixDP(12));
        badgeParams.gravity = Gravity.CENTER_HORIZONTAL;
        vipBadge.setLayoutParams(badgeParams);
        card.addView(vipBadge);

        // --- 1.2 Logo in Glowing Halo Frame ---
        final FrameLayout logoRing = new FrameLayout(context);
        int ringSize = utils.FixDP(90);
        LinearLayout.LayoutParams ringParams = new LinearLayout.LayoutParams(ringSize, ringSize);
        ringParams.setMargins(0, 0, 0, utils.FixDP(10));
        ringParams.gravity = Gravity.CENTER_HORIZONTAL;
        logoRing.setLayoutParams(ringParams);

        GradientDrawable ringBg = new GradientDrawable();
        ringBg.setShape(GradientDrawable.OVAL);
        ringBg.setColor(Color.parseColor("#171825"));
        ringBg.setStroke(utils.FixDP(2.0f), Color.parseColor("#00D2FF"));
        logoRing.setBackground(ringBg);
        logoRing.setPadding(utils.FixDP(6), utils.FixDP(6), utils.FixDP(6), utils.FixDP(6));

        final ImageView logoView = new ImageView(context);
        FrameLayout.LayoutParams logoParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        logoParams.gravity = Gravity.CENTER;
        logoView.setLayoutParams(logoParams);
        logoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int iconResId = context.getResources().getIdentifier("ic_launcher", "mipmap", context.getPackageName());
        if (iconResId != 0) {
            logoView.setImageResource(iconResId);
        }
        logoRing.addView(logoView);
        card.addView(logoRing);

        // Soft breathing scale animation on the logo ring
        ValueAnimator logoGlowAnim = ValueAnimator.ofFloat(0.96f, 1.04f);
        logoGlowAnim.setDuration(1600);
        logoGlowAnim.setRepeatMode(ValueAnimator.REVERSE);
        logoGlowAnim.setRepeatCount(ValueAnimator.INFINITE);
        logoGlowAnim.addUpdateListener(anim -> {
            float s = (float) anim.getAnimatedValue();
            logoRing.setScaleX(s);
            logoRing.setScaleY(s);
        });
        logoGlowAnim.start();

        // Load dynamic logo from remote config if available
        if (RemoteConfig.logoUrl != null && !RemoteConfig.logoUrl.isEmpty()) {
            String logoFetchUrl = RemoteConfig.logoUrl;
            if (logoFetchUrl.contains("?")) {
                logoFetchUrl += "&t=" + System.currentTimeMillis();
            } else {
                logoFetchUrl += "?t=" + System.currentTimeMillis();
            }
            com.bumptech.glide.Glide.with(context)
                    .asBitmap()
                    .load(logoFetchUrl)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                        @Override
                        public void onResourceReady(@androidx.annotation.NonNull android.graphics.Bitmap resource,
                                @androidx.annotation.Nullable com.bumptech.glide.request.transition.Transition<? super android.graphics.Bitmap> transition) {
                            android.graphics.Bitmap transparentBitmap = Utils.makeBlackTransparent(resource);
                            logoView.setImageBitmap(transparentBitmap != null ? transparentBitmap : resource);
                        }

                        @Override
                        public void onLoadCleared(@androidx.annotation.Nullable android.graphics.drawable.Drawable placeholder) {}
                    });
        }

        // --- 1.3 App Title & Subtitle ---
        LinearLayout titleLayout = new LinearLayout(context);
        titleLayout.setOrientation(LinearLayout.HORIZONTAL);
        titleLayout.setGravity(Gravity.CENTER);

        String remoteAppName = RemoteConfig.appName;
        String firstWord = "VIVEK";
        String secondWord = "PANEL";
        if (remoteAppName != null && remoteAppName.contains(" ")) {
            int spaceIdx = remoteAppName.indexOf(" ");
            firstWord = remoteAppName.substring(0, spaceIdx);
            secondWord = remoteAppName.substring(spaceIdx + 1);
        } else if (remoteAppName != null && !remoteAppName.isEmpty()) {
            firstWord = remoteAppName;
            secondWord = "";
        }

        Typeface customGamingFont = null;
        try {
            customGamingFont = Typeface.createFromAsset(context.getAssets(), "fonts/aimkill_font.ttf");
        } catch (Exception ignored) {}

        TextView titleRed = new TextView(context);
        titleRed.setText(firstWord + "  ");
        titleRed.setTextSize(23);
        titleRed.setTextColor(Color.parseColor("#00D2FF"));
        titleRed.setTypeface(customGamingFont != null ? customGamingFont : Typeface.DEFAULT_BOLD);
        titleRed.setShadowLayer(18, 0, 0, Color.parseColor("#99FFB800")); // Radiant neon glow

        TextView titleWhite = new TextView(context);
        titleWhite.setText(secondWord);
        titleWhite.setTextSize(23);
        titleWhite.setTextColor(Color.WHITE);
        titleWhite.setTypeface(customGamingFont != null ? customGamingFont : Typeface.DEFAULT_BOLD);
        titleWhite.setShadowLayer(10, 0, 0, Color.parseColor("#44FFFFFF"));

        titleLayout.addView(titleRed);
        titleLayout.addView(titleWhite);
        card.addView(titleLayout);

        subtitle = new TextView(context);
        subtitle.setText("FREE FIRE MAX • SAFE & SECURE");
        subtitle.setTextSize(10.5f);
        subtitle.setTextColor(Color.parseColor("#94A3B8"));
        subtitle.setGravity(Gravity.CENTER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            subtitle.setLetterSpacing(0.08f);
        }
        subtitle.setPadding(0, utils.FixDP(2), 0, utils.FixDP(14));
        card.addView(subtitle);

        // --- 1.4 Settings Layout (Root Bypass - hidden until login) ---
        settingsLayout = new LinearLayout(context);
        settingsLayout.setOrientation(LinearLayout.VERTICAL);
        settingsLayout.setVisibility(View.GONE);

        GradientDrawable settingsCardBg = new GradientDrawable();
        settingsCardBg.setColor(Color.parseColor("#151724"));
        settingsCardBg.setCornerRadius(utils.FixDP(12));
        settingsCardBg.setStroke(utils.FixDP(1), Color.parseColor("#2D3147"));
        settingsLayout.setBackground(settingsCardBg);
        settingsLayout.setPadding(utils.FixDP(12), utils.FixDP(8), utils.FixDP(12), utils.FixDP(8));

        LinearLayout.LayoutParams settingsCardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        settingsCardParams.setMargins(0, 0, 0, utils.FixDP(10));
        settingsLayout.setLayoutParams(settingsCardParams);

        LinearLayout suRow = new LinearLayout(context);
        suRow.setOrientation(LinearLayout.HORIZONTAL);
        suRow.setGravity(Gravity.CENTER_VERTICAL);
        suRow.setPadding(0, utils.FixDP(4), 0, utils.FixDP(4));

        suLabel = new TextView(context);
        suLabel.setText("⚡ ENABLE ROOT BYPASS");
        suLabel.setTypeface(Typeface.DEFAULT_BOLD);
        suLabel.setTextSize(13);
        suLabel.setTextColor(Color.WHITE);
        suLabel.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        suToggle = new Switch(context);
        suToggle.setChecked(isSuRenamed());
        suToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            triggerHaptic(25);
            String from = isChecked ? "/system/xbin/su" : "/system/xbin/su1";
            String to = isChecked ? "/system/xbin/su1" : "/system/xbin/su";
            try {
                Process process = Runtime.getRuntime().exec(isChecked ? "su" : "su1");
                process.getOutputStream().write(("mount -o remount,rw /system\n").getBytes());
                process.getOutputStream().write(("mv " + from + " " + to + "\n").getBytes());
                process.getOutputStream().write("exit\n".getBytes());
                process.getOutputStream().flush();
                process.waitFor();
                showToast("BYPASS ROOT " + (isChecked ? "SUCCESSFUL" : "DISABLED"));
            } catch (Exception e) {
                showToast("ROOT FAILED: " + e.getMessage());
            }
        });

        suRow.addView(suLabel);
        suRow.addView(suToggle);
        settingsLayout.addView(suRow);
        card.addView(settingsLayout);

        // --- 1.5 Futuristic License Key Input Container ---
        inputContainer = new LinearLayout(context);
        inputContainer.setOrientation(LinearLayout.HORIZONTAL);
        inputContainer.setGravity(Gravity.CENTER_VERTICAL);

        final GradientDrawable inputContainerBg = new GradientDrawable();
        inputContainerBg.setColor(Color.parseColor("#141624")); // Sleek dark futuristic obsidian
        inputContainerBg.setCornerRadius(utils.FixDP(14));
        inputContainerBg.setStroke(utils.FixDP(1.2f), Color.parseColor("#2C3046"));
        inputContainer.setBackground(inputContainerBg);

        LinearLayout.LayoutParams inputContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        inputContainerParams.setMargins(0, utils.FixDP(4), 0, utils.FixDP(12));
        inputContainer.setLayoutParams(inputContainerParams);
        inputContainer.setPadding(
                utils.FixDP(12),
                utils.FixDP(4),
                utils.FixDP(6),
                utils.FixDP(4)
        );

        // Left Key Icon
        TextView keyIcon = new TextView(context);
        keyIcon.setText("🔑");
        keyIcon.setTextSize(14);
        keyIcon.setPadding(0, 0, utils.FixDP(6), 0);
        inputContainer.addView(keyIcon);

        // License EditText
        inputLicense = new EditText(context);
        inputLicense.setHint("ENTER LICENSE KEY");
        inputLicense.setTextSize(13.5f);
        inputLicense.setTextColor(Color.WHITE);
        inputLicense.setHintTextColor(Color.parseColor("#64748B"));
        inputLicense.setSingleLine(true);
        inputLicense.setBackground(null);
        inputLicense.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        inputLicense.setPadding(0, utils.FixDP(10), utils.FixDP(6), utils.FixDP(10));

        LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f
        );
        etParams.gravity = Gravity.CENTER_VERTICAL;
        inputLicense.setLayoutParams(etParams);

        inputLicense.setText(context.getSharedPreferences("VivekPrefs", Context.MODE_PRIVATE)
                .getString("saved_license", ""));

        // Interactive focus glow transition
        inputLicense.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                inputContainerBg.setStroke(utils.FixDP(1.5f), Color.parseColor("#00D2FF"));
            } else {
                inputContainerBg.setStroke(utils.FixDP(1.2f), Color.parseColor("#2C3046"));
            }
        });

        // Modern Glowing Paste Button
        pasteButton = new Button(context);
        pasteButton.setText("📋 PASTE");
        pasteButton.setTextColor(Color.parseColor("#0B0C10"));
        pasteButton.setTextSize(11);
        pasteButton.setTypeface(Typeface.DEFAULT_BOLD);
        pasteButton.setPadding(
                utils.FixDP(12),
                utils.FixDP(6),
                utils.FixDP(12),
                utils.FixDP(6)
        );

        GradientDrawable pasteBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] { Color.parseColor("#00D2FF"), Color.parseColor("#0084FF") }
        );
        pasteBg.setCornerRadius(utils.FixDP(10));
        pasteButton.setBackground(pasteBg);

        LinearLayout.LayoutParams pasteParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                utils.FixDP(38)
        );
        pasteParams.gravity = Gravity.CENTER_VERTICAL;
        pasteButton.setLayoutParams(pasteParams);

        pasteButton.setOnClickListener(v -> {
            triggerHaptic(25);
            try {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null && clipboard.hasPrimaryClip() && clipboard.getPrimaryClip().getItemCount() > 0) {
                    CharSequence pasteData = clipboard.getPrimaryClip().getItemAt(0).getText();
                    if (pasteData != null && pasteData.length() > 0) {
                        String cleanKey = pasteData.toString().trim();
                        inputLicense.setText(cleanKey);
                        inputLicense.setSelection(cleanKey.length());
                        showToast("Key Pasted! 📋");
                    } else {
                        showToast("Clipboard is empty!");
                    }
                } else {
                    showToast("Clipboard is empty!");
                }
            } catch (Exception e) {
                showToast("Failed to paste: " + e.getMessage());
            }
        });

        inputContainer.addView(inputLicense);
        inputContainer.addView(pasteButton);
        card.addView(inputContainer);

        // --- 1.6 High-Impact Primary Login Button ---
        loginButton = new Button(context);
        loginButton.setText("UNLOCK VIVEK PANEL ➔");
        loginButton.setTextColor(Color.parseColor("#0A0B10"));
        loginButton.setTextSize(14.5f);
        loginButton.setTypeface(Typeface.DEFAULT_BOLD);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            loginButton.setLetterSpacing(0.08f);
            loginButton.setElevation(utils.FixDP(6));
        }
        loginButton.setPadding(
                utils.FixDP(14),
                utils.FixDP(12),
                utils.FixDP(14),
                utils.FixDP(12)
        );

        GradientDrawable btnBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {
                        Color.parseColor("#00D2FF"),
                        Color.parseColor("#0055FF"),
                        Color.parseColor("#FFA000")
                }
        );
        btnBg.setCornerRadius(utils.FixDP(14));
        loginButton.setBackground(btnBg);
        loginButton.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, utils.FixDP(48));
        btnParams.gravity = Gravity.CENTER_HORIZONTAL;
        btnParams.setMargins(0, utils.FixDP(2), 0, utils.FixDP(10));
        loginButton.setLayoutParams(btnParams);

        // Interactive touch scale feedback with haptic vibration
        loginButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).start();
                triggerHaptic(20);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
            }
            return false;
        });

        card.addView(loginButton);

        // --- 1.7 Secondary Action Pill Grid (Get Key / Support) ---
        LinearLayout actionRow = new LinearLayout(context);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams actionRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        actionRowParams.setMargins(0, 0, 0, utils.FixDP(8));
        actionRow.setLayoutParams(actionRowParams);

        Button telegramBtn = createSecondaryActionButton("✈️ TELEGRAM", v -> {
            triggerHaptic(20);
            try {
                String tg = (RemoteConfig.telegramUrl != null) ? RemoteConfig.telegramUrl.trim() : "";
                if (tg.isEmpty()) {
                    showToast("Telegram support link will be updated soon!");
                } else {
                    String targetUrl = tg;
                    if (targetUrl.startsWith("@")) {
                        targetUrl = "https://t.me/" + targetUrl.substring(1);
                    } else if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
                        targetUrl = "https://t.me/" + targetUrl;
                    }
                    Intent tgIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl));
                    tgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(tgIntent);
                }
            } catch (Exception e) {
                showToast("Cannot open Telegram: " + e.getMessage());
            }
        });

        Button websiteBtn = createSecondaryActionButton("🌐 VISIT WEBSITE", v -> {
            triggerHaptic(20);
            try {
                String siteUrl = (RemoteConfig.websiteUrl != null && !RemoteConfig.websiteUrl.trim().isEmpty())
                        ? RemoteConfig.websiteUrl.trim()
                        : "https://www.ashutech.xyz/";
                if (!siteUrl.startsWith("http://") && !siteUrl.startsWith("https://")) {
                    siteUrl = "https://" + siteUrl;
                }
                Intent supportIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(siteUrl));
                supportIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(supportIntent);
            } catch (Exception e) {
                showToast("Cannot open website: " + e.getMessage());
            }
        });

        actionRow.addView(telegramBtn);
        // Small spacer
        View spacer = new View(context);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(utils.FixDP(8), ViewGroup.LayoutParams.MATCH_PARENT));
        actionRow.addView(spacer);
        actionRow.addView(websiteBtn);
        card.addView(actionRow);

        // --- 1.7b OBB 55 Highlighted Showcase Badge ---
        final LinearLayout obbBadge = new LinearLayout(context);
        obbBadge.setOrientation(LinearLayout.VERTICAL);
        obbBadge.setGravity(Gravity.CENTER);

        // Deep cyber luxury background with glowing neon gold border
        GradientDrawable obbBadgeBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {
                        Color.parseColor("#081426"),
                        Color.parseColor("#0E2244"),
                        Color.parseColor("#081426")
                }
        );
        obbBadgeBg.setCornerRadius(utils.FixDP(14));
        obbBadgeBg.setStroke(utils.FixDP(1.5f), Color.parseColor("#00D2FF"));
        obbBadge.setBackground(obbBadgeBg);
        obbBadge.setPadding(utils.FixDP(14), utils.FixDP(8), utils.FixDP(14), utils.FixDP(8));

        LinearLayout.LayoutParams obbParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        obbParams.setMargins(0, utils.FixDP(4), 0, utils.FixDP(8));
        obbBadge.setLayoutParams(obbParams);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            obbBadge.setElevation(utils.FixDP(4));
        }

        // Header Row: [🔥 NEW TAG] + "OBB 55 PANEL"
        LinearLayout obbHeaderRow = new LinearLayout(context);
        obbHeaderRow.setOrientation(LinearLayout.HORIZONTAL);
        obbHeaderRow.setGravity(Gravity.CENTER);

        // High-impact Fire "NEW" Tag
        TextView newTag = new TextView(context);
        newTag.setText("🔥 NEW");
        newTag.setTextColor(Color.WHITE);
        newTag.setTextSize(9.5f);
        newTag.setTypeface(Typeface.DEFAULT_BOLD);
        GradientDrawable newTagBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] { Color.parseColor("#FF416C"), Color.parseColor("#FF4B2B") }
        );
        newTagBg.setCornerRadius(utils.FixDP(6));
        newTag.setBackground(newTagBg);
        newTag.setPadding(utils.FixDP(7), utils.FixDP(2), utils.FixDP(7), utils.FixDP(2));
        LinearLayout.LayoutParams newTagParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        newTagParams.setMargins(0, 0, utils.FixDP(8), 0);
        newTag.setLayoutParams(newTagParams);
        obbHeaderRow.addView(newTag);

        // Main Showcase Title
        TextView obbTitle = new TextView(context);
        obbTitle.setText("OBB 55 PANEL");
        obbTitle.setTextColor(Color.parseColor("#00D2FF"));
        obbTitle.setTextSize(15f);
        obbTitle.setTypeface(customGamingFont != null ? customGamingFont : Typeface.DEFAULT_BOLD);
        obbTitle.setShadowLayer(16, 0, 0, Color.parseColor("#FFFFB800"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            obbTitle.setLetterSpacing(0.12f);
        }
        obbHeaderRow.addView(obbTitle);
        obbBadge.addView(obbHeaderRow);

        // Subtitle Status: ⚡ LATEST UPDATE • 100% SAFE & ACTIVE 🟢
        TextView obbSub = new TextView(context);
        obbSub.setText("⚡ LATEST UPDATE • 100% SAFE & ACTIVE 🟢");
        obbSub.setTextColor(Color.parseColor("#00FFA3"));
        obbSub.setTextSize(10f);
        obbSub.setTypeface(Typeface.DEFAULT_BOLD);
        obbSub.setGravity(Gravity.CENTER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            obbSub.setLetterSpacing(0.06f);
        }
        LinearLayout.LayoutParams obbSubParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        obbSubParams.setMargins(0, utils.FixDP(4), 0, 0);
        obbSub.setLayoutParams(obbSubParams);
        obbBadge.addView(obbSub);

        // Continuous breathing scale animation to draw attention
        ValueAnimator obbAnim = ValueAnimator.ofFloat(0.98f, 1.02f);
        obbAnim.setDuration(1300);
        obbAnim.setRepeatMode(ValueAnimator.REVERSE);
        obbAnim.setRepeatCount(ValueAnimator.INFINITE);
        obbAnim.addUpdateListener(anim -> {
            float s = (float) anim.getAnimatedValue();
            obbBadge.setScaleX(s);
            obbBadge.setScaleY(s);
        });
        obbAnim.start();

        card.addView(obbBadge);

        // --- 1.8 Loading / Verifying Indicator Container ---
        LinearLayout loadingLayout = new LinearLayout(context);
        loadingLayout.setOrientation(LinearLayout.HORIZONTAL);
        loadingLayout.setGravity(Gravity.CENTER);
        loadingLayout.setPadding(0, utils.FixDP(6), 0, utils.FixDP(6));

        loadingBar = new ProgressBar(context);
        loadingBar.setVisibility(View.GONE);
        loadingBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#00D2FF"), PorterDuff.Mode.SRC_IN);

        loadingText = new TextView(context);
        loadingText.setText("Verifying License Key...");
        loadingText.setTextColor(Color.parseColor("#00D2FF"));
        loadingText.setTextSize(13);
        loadingText.setTypeface(Typeface.DEFAULT_BOLD);
        loadingText.setPadding(utils.FixDP(12), 0, 0, 0);
        loadingText.setVisibility(View.GONE);

        loadingLayout.addView(loadingBar);
        loadingLayout.addView(loadingText);
        card.addView(loadingLayout);

        // =========================================================================
        // STEP 2: Root View, Ambient Cyber Glow, and ScrollView Setup
        // =========================================================================
        rootContainer = new LinearLayout(context);
        rootContainer.setOrientation(LinearLayout.VERTICAL);
        rootContainer.setGravity(Gravity.CENTER);

        // Deep rich cyber gradient background
        GradientDrawable cyberRootBg = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {
                        Color.parseColor("#08090E"),
                        Color.parseColor("#10121C"),
                        Color.parseColor("#0A0B10")
                }
        );
        rootContainer.setBackground(cyberRootBg);

        ScrollView scrollView = new ScrollView(context);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scrollView.setFillViewport(true);
        scrollView.setVerticalScrollBarEnabled(false);

        LinearLayout scrollContent = new LinearLayout(context);
        scrollContent.setOrientation(LinearLayout.VERTICAL);
        scrollContent.setGravity(Gravity.CENTER);
        scrollContent.setPadding(
                utils.FixDP(16),
                utils.FixDP(24),
                utils.FixDP(16),
                utils.FixDP(24)
        );

        LinearLayout.LayoutParams cardLayoutParam = new LinearLayout.LayoutParams(
                utils.FixDP(315),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardLayoutParam.gravity = Gravity.CENTER_HORIZONTAL;
        cardLayoutParam.setMargins(0, utils.FixDP(8), 0, utils.FixDP(14));
        card.setLayoutParams(cardLayoutParam);

        scrollContent.addView(card);
        // Disclaimer card removed as per request
        scrollView.addView(scrollContent);

        // Card entrance animation with smooth overshoot
        card.setAlpha(0f);
        card.setScaleX(0.94f);
        card.setScaleY(0.94f);
        card.setTranslationY(utils.FixDP(30));
        card.animate()
                .alpha(1f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .translationY(0)
                .setDuration(650)
                .setInterpolator(new OvershootInterpolator(1.1f))
                .start();

        boolean hasBackground = RemoteConfig.backgroundUrl != null && !RemoteConfig.backgroundUrl.isEmpty();

        if (hasBackground) {
            FrameLayout rootFrame = new FrameLayout(context);
            rootFrame.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            ImageView backgroundView = new ImageView(context);
            backgroundView.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            backgroundView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            rootFrame.addView(backgroundView);

            // Dark cinematic glass overlay over custom background
            View overlayDim = new View(context);
            overlayDim.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            overlayDim.setBackgroundColor(Color.parseColor("#CC08090E"));
            rootFrame.addView(overlayDim);

            rootFrame.addView(scrollView);
            rootContainer.addView(rootFrame);

            com.bumptech.glide.Glide.with(context).load(RemoteConfig.backgroundUrl).into(backgroundView);
        } else {
            rootContainer.addView(scrollView);
        }

        ((Activity) context).setContentView(rootContainer);

        // Button listener
        loginButton.setOnClickListener(v -> handleLogin());
    }

    private Button createSecondaryActionButton(String title, View.OnClickListener listener) {
        Button btn = new Button(context);
        btn.setText(title);
        btn.setTextColor(Color.parseColor("#00D2FF"));
        btn.setTextSize(11f);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btn.setLetterSpacing(0.06f);
        }

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#151724"));
        bg.setCornerRadius(utils.FixDP(12));
        bg.setStroke(utils.FixDP(1), Color.parseColor("#2C3148"));
        btn.setBackground(bg);
        btn.setPadding(utils.FixDP(10), utils.FixDP(8), utils.FixDP(10), utils.FixDP(8));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, utils.FixDP(38), 1.0f);
        btn.setLayoutParams(params);

        btn.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
            }
            return false;
        });

        btn.setOnClickListener(listener);
        return btn;
    }

    /* Disclaimer card removed */

    private TextView createCompactBadge(String text, String colorHex) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextColor(Color.parseColor(colorHex));
        tv.setTextSize(9f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        return tv;
    }

    private void handleLogin() {
        final String licenseKey = inputLicense.getText().toString().trim();
        if (licenseKey.isEmpty()) {
            triggerHaptic(40);
            showToast("License key required.");
            return;
        }

        if (!RemoteConfig.isOnline) {
            new Handler(Looper.getMainLooper()).post(() -> {
                showNoticeIfAvailable();
                showToast("❌ Access Disabled. Please check update notice.");
            });
            return;
        }

        loginButton.setEnabled(false);
        loadingBar.setVisibility(View.VISIBLE);
        loadingText.setVisibility(View.VISIBLE);
        setStatus("🔄 Connecting to VIP Gateway...", Color.parseColor("#00D2FF"), true);

        new Thread(() -> {
            try {
                String hwid = getHWID();

                String encodedName = java.net.URLEncoder.encode(
                        RemoteConfig.keyauthAppName != null ? RemoteConfig.keyauthAppName : "vip panel", "UTF-8");
                String encodedSecret = java.net.URLEncoder.encode(
                        RemoteConfig.keyauthSecret != null ? RemoteConfig.keyauthSecret : SECRET, "UTF-8");
                String initUrl = RemoteConfig.keyauthUrl + "?type=init&ver=" + RemoteConfig.keyauthVersion
                        + "&name=" + encodedName + "&ownerid=" + RemoteConfig.keyauthOwnerId
                        + "&secret=" + encodedSecret;
                JSONObject initRes = sendRequest(initUrl);

                if (!initRes.getBoolean("success")) {
                    postError("❌ Init failed: " + initRes.optString("message"));
                    return;
                }

                setStatus("🔐 Decrypting & verifying license...", Color.parseColor("#00D2FF"), true);

                String encodedKey = java.net.URLEncoder.encode(licenseKey, "UTF-8");
                String encodedHwid = java.net.URLEncoder.encode(hwid, "UTF-8");
                String loginUrl = RemoteConfig.keyauthUrl + "?type=license&key=" + encodedKey
                        + "&hwid=" + encodedHwid
                        + "&sessionid=" + initRes.getString("sessionid")
                        + "&name=" + encodedName
                        + "&ownerid=" + RemoteConfig.keyauthOwnerId
                        + "&ver=" + RemoteConfig.keyauthVersion;
                JSONObject loginRes = sendRequest(loginUrl);

                if (loginRes.getBoolean("success")) {
                    sendOwnerIDToNative(RemoteConfig.keyauthOwnerId);
                    context.getSharedPreferences("VivekPrefs", Context.MODE_PRIVATE)
                            .edit().putString("saved_license", licenseKey).apply();
                    Menu.userLicenseKey = licenseKey;

                    new Handler(Looper.getMainLooper()).post(() -> {
                        triggerHaptic(50);
                        new Menu(context, 1);
                        isSettingsVisible = true;
                        settingsLayout.setVisibility(View.VISIBLE);
                        if (inputContainer != null) inputContainer.setVisibility(View.GONE);
                        inputLicense.setVisibility(View.GONE);
                        loginButton.setVisibility(View.GONE);

                        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.dts.freefiremax");
                        if (launchIntent == null) {
                            launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.dts.freefireth");
                        }
                        if (launchIntent != null) {
                            context.startActivity(launchIntent);
                        } else {
                            try {
                                Intent fallback = new Intent();
                                fallback.setClassName("com.dts.freefiremax", "com.epicgames.ue4.SplashActivity");
                                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(fallback);
                            } catch (Exception e) {
                                showToast("Free Fire Max not found. Please install it.");
                            }
                        }
                    });

                } else {
                    postError("❌ Login failed: " + loginRes.optString("message"));
                }

            } catch (Exception e) {
                postError("❌ Error: " + e.getMessage());
            }
        }).start();
    }

    private void setStatus(String message, int color, boolean showProgress) {
        new Handler(Looper.getMainLooper()).post(() -> {
            loadingText.setText(message);
            loadingText.setTextColor(color);
            loadingBar.setVisibility(showProgress ? View.VISIBLE : View.GONE);
        });
    }

    private void postError(String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            triggerHaptic(40);
            showToast(message);
            loginButton.setEnabled(true);
            loadingBar.setVisibility(View.GONE);
            loadingText.setVisibility(View.GONE);
        });
    }

    private JSONObject sendRequest(String urlString) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        java.io.InputStream stream = conn.getResponseCode() >= 400
                ? conn.getErrorStream() : conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) response.append(line);
        reader.close();
        String raw = response.toString().trim();
        if (!raw.startsWith("{")) {
            return new JSONObject("{\"success\":false,\"message\":\"Server error: " + raw + "\"}");
        }
        return new JSONObject(raw);
    }

    private void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }

    public static void showToastFromNative(final Context context, final String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }

    private String getHWID() {
        String rawHwid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (rawHwid == null || rawHwid.isEmpty()) {
            rawHwid = "defaultandroidid12345";
        }
        String combined = rawHwid + "-vip-panel-hwid-secure";
        return combined.substring(0, Math.max(20, combined.length()));
    }

    private boolean isSuRenamed() {
        return !new java.io.File("/system/xbin/su").exists();
    }

    private void showNoticeIfAvailable() {
        if (RemoteConfig.showNotice && RemoteConfig.noticeMessage != null && !RemoteConfig.noticeMessage.isEmpty()) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    String noticeTitle = (RemoteConfig.noticeTitle != null && !RemoteConfig.noticeTitle.isEmpty())
                            ? RemoteConfig.noticeTitle : "📢 Notice";
                    new android.app.AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                            .setTitle(noticeTitle)
                            .setMessage(RemoteConfig.noticeMessage)
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .create()
                            .show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 600);
        }
    }
}
