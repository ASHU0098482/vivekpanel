package com.vivek;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.view.View;

import java.util.Date;

public class DrawView extends View implements Runnable {
    Paint mStrokePaint;
    Paint mFilledPaint;
    Paint mTextPaint;
    Paint mBitMapPaint;
    Paint mRectPaint1;
    Paint mRectPaint2;
    Paint mSignaturePaint;
    Typeface mSignatureTypeface;
    Paint mSwashPaint;
    Paint mPenTipPaint;
    Paint mPenGlowPaint;
    Paint mSparkPaint;
    Paint mAnimBrandPaint;
    Paint mAnimSubPaint;
    Paint mAnimFeaturePaint;
    Paint mAnimProgressBgPaint;
    Paint mAnimProgressFillPaint;
    Paint mAnimCheckPaint;
    Paint mAnimCheckCirclePaint;
    private static boolean soundPlayed = false;
    private static Context sContext;
    android.graphics.Path mClipPath = new android.graphics.Path();
    android.graphics.Path mSwashPath = new android.graphics.Path();

    Thread mThread;
    int FPS = 240;
    long sleepTime;
    Date time;

    public DrawView(Context context)
    {
        super(context, null, 0);
        sContext = context;
        InitializePaints();
        setFocusableInTouchMode(false);
        setBackgroundColor(0);
        time = new Date();
        sleepTime = (long)(1000 / FPS);
        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if (canvas != null && getVisibility() == VISIBLE)
        {
            ClearCanvas(canvas);
            time.setTime(System.currentTimeMillis());
            Menu.OnDrawLoad(this, canvas);
        }
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        while (mThread.isAlive() && !mThread.isInterrupted())
        {
            try
            {
                long t1 = System.currentTimeMillis();
                postInvalidate();
                long td = System.currentTimeMillis() - t1;
                Thread.sleep(Math.max(Math.min(0, sleepTime - td), sleepTime));
            }
            catch (InterruptedException it)
            {
                return;
            }
        }
    }

    public void InitializePaints()
    {
        mStrokePaint = new Paint();
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setAntiAlias(true);

        mFilledPaint = new Paint();
        mFilledPaint.setStyle(Paint.Style.FILL);
        mFilledPaint.setAntiAlias(true);

        mTextPaint = new Paint();
        mTextPaint.setTypeface(Typeface.MONOSPACE);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mRectPaint1 = new Paint();
        mRectPaint1.setTypeface(Typeface.MONOSPACE);
        mRectPaint1.setAntiAlias(true);
        mRectPaint1.setTextAlign(Paint.Align.CENTER);

        mRectPaint2 = new Paint();
        mRectPaint2.setTypeface(Typeface.MONOSPACE);
        mRectPaint2.setAntiAlias(true);
        mRectPaint2.setTextAlign(Paint.Align.CENTER);

        mBitMapPaint = new Paint();
        mBitMapPaint.setAntiAlias(true);

        mSignaturePaint = new Paint();
        mSignaturePaint.setAntiAlias(true);
        mSignaturePaint.setTextAlign(Paint.Align.LEFT);
        try {
            mSignatureTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/signature.ttf");
            mSignaturePaint.setTypeface(mSignatureTypeface);
        } catch (Exception e) {
            mSignaturePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.ITALIC));
        }

        mSwashPaint = new Paint();
        mSwashPaint.setAntiAlias(true);
        mSwashPaint.setStyle(Paint.Style.STROKE);
        mSwashPaint.setStrokeCap(Paint.Cap.ROUND);
        mSwashPaint.setStrokeJoin(Paint.Join.ROUND);

        mPenTipPaint = new Paint();
        mPenTipPaint.setAntiAlias(true);
        mPenTipPaint.setStyle(Paint.Style.FILL);

        mPenGlowPaint = new Paint();
        mPenGlowPaint.setAntiAlias(true);
        mPenGlowPaint.setStyle(Paint.Style.FILL);

        mSparkPaint = new Paint();
        mSparkPaint.setAntiAlias(true);
        mSparkPaint.setStyle(Paint.Style.FILL);

        mAnimBrandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAnimBrandPaint.setTextAlign(Paint.Align.CENTER);
        mAnimBrandPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));

        mAnimSubPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAnimSubPaint.setTextAlign(Paint.Align.CENTER);
        mAnimSubPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));

        mAnimFeaturePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAnimFeaturePaint.setTextAlign(Paint.Align.CENTER);
        mAnimFeaturePaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));

        mAnimProgressBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAnimProgressBgPaint.setStyle(Paint.Style.FILL);

        mAnimProgressFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAnimProgressFillPaint.setStyle(Paint.Style.FILL);

        mAnimCheckPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAnimCheckPaint.setStyle(Paint.Style.STROKE);
        mAnimCheckPaint.setStrokeCap(Paint.Cap.ROUND);
        mAnimCheckPaint.setStrokeJoin(Paint.Join.ROUND);

        mAnimCheckCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAnimCheckCirclePaint.setStyle(Paint.Style.FILL);
    }

    public static void playSuccessSound() {
        // Sound effects removed per user request
    }

    public void DrawActivationLoading(Canvas cvs, String brandName, float progress, boolean isDone, float alpha) {
        if (cvs == null || alpha <= 0.01f) return;

        // Reset soundPlayed state when starting a new animation run
        if (progress < 0.1f && !isDone) {
            soundPlayed = false;
        }

        // Trigger success sound when reaching completion
        if ((isDone || progress >= 1.0f) && !soundPlayed) {
            soundPlayed = true;
            playSuccessSound();
        }

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        float cx = width / 2.0f;
        float cy = height / 2.0f;
        float H = (float) height;
        float W = (float) width;

        // 1. Full-Screen Dark Cinematic Backdrop
        int bgAlpha = (int) (240 * alpha);
        mFilledPaint.setColor(Color.argb(bgAlpha, 6, 6, 10));
        cvs.drawRect(0, 0, width, height, mFilledPaint);

        // Futuristic glowing horizontal guideline behind the brand name
        Paint glowLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowLinePaint.setStyle(Paint.Style.STROKE);
        glowLinePaint.setStrokeWidth(3.0f);
        glowLinePaint.setColor(Color.argb((int)(80 * alpha), 255, 184, 0));
        float lineHalfWidth = Math.min(W * 0.42f, 650.0f);
        cvs.drawLine(cx - lineHalfWidth, cy - (H * 0.22f), cx + lineHalfWidth, cy - (H * 0.22f), glowLinePaint);

        // 2. Huge Full-Screen Brand Title (e.g. VIVEK PANEL)
        String brand = (brandName != null && !brandName.isEmpty()) ? brandName : Menu.getBrandName();
        float brandSize = Math.max(72.0f, H * 0.13f);
        mAnimBrandPaint.setTextSize(brandSize);
        mAnimBrandPaint.setColor(Color.argb((int)(255 * alpha), 255, 184, 0)); // Intense Gold
        mAnimBrandPaint.setShadowLayer(36.0f, 0f, 0f, Color.argb((int)(230 * alpha), 255, 184, 0));
        float brandY = cy - (H * 0.22f);
        cvs.drawText(brand, cx, brandY, mAnimBrandPaint);
        mAnimBrandPaint.clearShadowLayer();

        // 3. Prominent "INITIALIZING CONNECTION..." Subtitle
        float subSize = Math.max(24.0f, H * 0.046f);
        mAnimSubPaint.setTextSize(subSize);
        mAnimSubPaint.setColor(Color.argb((int)(245 * alpha), 0, 230, 118)); // Cyber Emerald Green
        mAnimSubPaint.setShadowLayer(22.0f, 0f, 0f, Color.argb((int)(200 * alpha), 0, 230, 118));
        float subY = brandY + (subSize * 2.1f);
        cvs.drawText("INITIALIZING CONNECTION...", cx, subY, mAnimSubPaint);
        mAnimSubPaint.clearShadowLayer();

        // 4. Dynamic Cycling Feature Activation Text (4.0-second lifecycle)
        String featureText;
        if (isDone || progress >= 1.0f) {
            featureText = "✨ All Systems Synchronized & Protected";
        } else if (progress < 0.20f) {
            featureText = "⚡ Activating Auto ESP System...";
        } else if (progress < 0.40f) {
            featureText = "🎯 Initializing Line & Box Trackers...";
        } else if (progress < 0.60f) {
            featureText = "⚡ Calibrating Silent Aim & Headshot...";
        } else if (progress < 0.80f) {
            featureText = "🎯 Configuring Sniper Auto Aim...";
        } else {
            featureText = "🛡️ Synchronizing Bullet Track & Security...";
        }

        float featureSize = Math.max(22.0f, H * 0.040f);
        mAnimFeaturePaint.setTextSize(featureSize);
        mAnimFeaturePaint.setColor(Color.argb((int)(240 * alpha), 241, 245, 249)); // High-Contrast Slate White
        float featureY = subY + (featureSize * 2.3f);
        cvs.drawText(featureText, cx, featureY, mAnimFeaturePaint);

        if (!isDone && progress < 1.0f) {
            // 5. Large Full-Screen Loading Bar & Percentage Counter (4.0s progress)
            float barWidth = Math.min(W * 0.68f, 920.0f);
            float barHeight = Math.max(20.0f, H * 0.024f);
            float barLeft = cx - (barWidth / 2.0f);
            float barTop = featureY + (H * 0.06f);
            float barRight = cx + (barWidth / 2.0f);
            float barBottom = barTop + barHeight;

            // Track background
            mAnimProgressBgPaint.setColor(Color.argb((int)(200 * alpha), 22, 22, 32));
            android.graphics.RectF trackRect = new android.graphics.RectF(barLeft, barTop, barRight, barBottom);
            cvs.drawRoundRect(trackRect, barHeight * 0.5f, barHeight * 0.5f, mAnimProgressBgPaint);

            // Track border
            Paint trackBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
            trackBorder.setStyle(Paint.Style.STROKE);
            trackBorder.setStrokeWidth(2.0f);
            trackBorder.setColor(Color.argb((int)(140 * alpha), 70, 70, 95));
            cvs.drawRoundRect(trackRect, barHeight * 0.5f, barHeight * 0.5f, trackBorder);

            // Fill
            float clampedProgress = Math.max(0.015f, Math.min(1.0f, progress));
            float fillRight = barLeft + (barWidth * clampedProgress);
            android.graphics.RectF fillRect = new android.graphics.RectF(barLeft, barTop, fillRight, barBottom);

            LinearGradient fillGrad = new LinearGradient(
                    barLeft, barTop, barRight, barBottom,
                    Color.argb((int)(255 * alpha), 255, 184, 0),
                    Color.argb((int)(255 * alpha), 0, 230, 118),
                    Shader.TileMode.CLAMP
            );
            mAnimProgressFillPaint.setShader(fillGrad);
            mAnimProgressFillPaint.setShadowLayer(26.0f, 0f, 0f, Color.argb((int)(220 * alpha), 0, 230, 118));
            cvs.drawRoundRect(fillRect, barHeight * 0.5f, barHeight * 0.5f, mAnimProgressFillPaint);
            mAnimProgressFillPaint.clearShadowLayer();
            mAnimProgressFillPaint.setShader(null);

            // Percentage Text
            int pct = (int) (clampedProgress * 100);
            String pctStr = "[ " + pct + "% ]";
            float pctSize = Math.max(26.0f, H * 0.052f);
            mAnimSubPaint.setTextSize(pctSize);
            mAnimSubPaint.setColor(Color.argb((int)(255 * alpha), 255, 184, 0));
            mAnimSubPaint.setShadowLayer(16.0f, 0f, 0f, Color.argb((int)(180 * alpha), 255, 184, 0));
            cvs.drawText(pctStr, cx, barBottom + (pctSize * 1.35f), mAnimSubPaint);
            mAnimSubPaint.clearShadowLayer();
        } else {
            // 6. Massive Full-Screen Completion State: Glowing Emerald Checkmark + "GOOD TO GO"
            float checkCenterY = featureY + (H * 0.13f);
            float circleRadius = Math.max(48.0f, H * 0.09f);

            // Glowing Green Background Circle
            mAnimCheckCirclePaint.setColor(Color.argb((int)(245 * alpha), 0, 200, 83));
            mAnimCheckCirclePaint.setShadowLayer(45.0f, 0f, 0f, Color.argb((int)(240 * alpha), 0, 230, 118));
            cvs.drawCircle(cx, checkCenterY, circleRadius, mAnimCheckCirclePaint);
            mAnimCheckCirclePaint.clearShadowLayer();

            // Inner circle ring
            Paint checkRing = new Paint(Paint.ANTI_ALIAS_FLAG);
            checkRing.setStyle(Paint.Style.STROKE);
            checkRing.setStrokeWidth(3.5f);
            checkRing.setColor(Color.argb((int)(255 * alpha), 255, 255, 255));
            cvs.drawCircle(cx, checkCenterY, circleRadius, checkRing);

            // Large Checkmark Vector Path (✓)
            android.graphics.Path checkPath = new android.graphics.Path();
            checkPath.moveTo(cx - (circleRadius * 0.44f), checkCenterY + (circleRadius * 0.05f));
            checkPath.lineTo(cx - (circleRadius * 0.12f), checkCenterY + (circleRadius * 0.40f));
            checkPath.lineTo(cx + (circleRadius * 0.48f), checkCenterY - (circleRadius * 0.32f));

            mAnimCheckPaint.setStrokeWidth(8.5f);
            mAnimCheckPaint.setColor(Color.argb((int)(255 * alpha), 255, 255, 255));
            mAnimCheckPaint.setShadowLayer(16.0f, 0f, 0f, Color.argb((int)(200 * alpha), 0, 0, 0));
            cvs.drawPath(checkPath, mAnimCheckPaint);
            mAnimCheckPaint.clearShadowLayer();

            // Massive "GOOD TO GO" Text
            float goodToGoSize = Math.max(54.0f, H * 0.125f);
            mAnimBrandPaint.setTextSize(goodToGoSize);
            mAnimBrandPaint.setColor(Color.argb((int)(255 * alpha), 0, 230, 118)); // Neon Green
            mAnimBrandPaint.setShadowLayer(35.0f, 0f, 0f, Color.argb((int)(240 * alpha), 0, 230, 118));
            float goodToGoY = checkCenterY + circleRadius + (goodToGoSize * 1.15f);
            cvs.drawText("GOOD TO GO", cx, goodToGoY, mAnimBrandPaint);
            mAnimBrandPaint.clearShadowLayer();

            // Subtitle status below GOOD TO GO
            float subGoodSize = Math.max(18.0f, H * 0.036f);
            mAnimFeaturePaint.setTextSize(subGoodSize);
            mAnimFeaturePaint.setColor(Color.argb((int)(210 * alpha), 148, 163, 184));
            cvs.drawText("ALL FEATURES ACTIVE & SECURE", cx, goodToGoY + (subGoodSize * 1.5f), mAnimFeaturePaint);
        }
    }

    public void ClearCanvas(Canvas cvs) {
        cvs.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    public void DrawLogo(Canvas cvs, float posX, float posY, float width, float height, float alpha) {
        if (Menu.logoBitmap != null) {
            Paint p = new Paint(mBitMapPaint);
            p.setAlpha((int) alpha);
            android.graphics.RectF dst = new android.graphics.RectF(posX - width / 2, posY - height / 2, posX + width / 2, posY + height / 2);
            cvs.drawBitmap(Menu.logoBitmap, null, dst, p);
        }
    }

    public void DrawSignatureText(Canvas cvs, int a, int r, int g, int b, String txt, float posX, float posY, float size) {
        DrawSmoothSignatureWriting(cvs, a, r, g, b, txt, posX, posY, size, 1.0f);
    }

    public void DrawSmoothSignatureWriting(Canvas cvs, int a, int r, int g, int b, String txt, float posX, float posY, float size, float progress) {
        if (txt == null || txt.isEmpty() || progress <= 0f || a <= 0) return;

        mSignaturePaint.setColor(Color.rgb(r, g, b));
        mSignaturePaint.setAlpha(a);
        mSignaturePaint.setTextSize(size);
        mSignaturePaint.setFakeBoldText(true);
        mSignaturePaint.setTextAlign(Paint.Align.LEFT);

        int totalLen = txt.length();
        float[] charWidths = new float[totalLen];
        mSignaturePaint.getTextWidths(txt, charWidths);

        float totalWidth = 0f;
        for (int i = 0; i < totalLen; i++) {
            totalWidth += charWidths[i];
        }

        float startX = posX - (totalWidth / 2f);
        float yPos = posY - ((mSignaturePaint.descent() + mSignaturePaint.ascent()) / 2f);

        float swashStartX = startX - (size * 0.10f);
        float swashTotalWidth = totalWidth + (size * 0.25f);
        float swashBaseY = yPos + (size * 0.36f);
        float swashDepth = size * 0.14f;

        // Progress split: 0.0 -> 0.82 is letter-by-letter handwriting, 0.82 -> 1.0 is underline swash flourish
        float textPhaseEnd = 0.82f;

        if (progress >= 1.0f) {
            // Full glowing signature rendering
            mSignaturePaint.setShadowLayer(32.0f, 0.0f, 0.0f, Color.argb(Math.min(a, 220), r, g, b));
            cvs.drawText(txt, startX, yPos, mSignaturePaint);
            mSignaturePaint.clearShadowLayer();
            cvs.drawText(txt, startX, yPos, mSignaturePaint);

            // Full underline flourish
            drawSignatureFlourish(cvs, a, r, g, b, swashStartX, swashTotalWidth, swashBaseY, swashDepth, size, 1.0f);
        } else {
            float clampedProgress = Math.max(0.001f, Math.min(0.999f, progress));
            float penX = startX;
            float penY = yPos;

            if (clampedProgress < textPhaseEnd) {
                // Phase 1: Letter-by-letter handwriting progression
                float textProgress = clampedProgress / textPhaseEnd;
                float charProgress = textProgress * totalLen;
                int activeChar = Math.min((int) Math.floor(charProgress), totalLen - 1);
                float letterFrac = charProgress - activeChar;

                float currentLetterStartX = startX;
                for (int i = 0; i < activeChar; i++) {
                    currentLetterStartX += charWidths[i];
                }
                float currentLetterWidth = charWidths[activeChar];

                // Smooth handwriting pen stroke curve
                float smoothFrac = letterFrac * letterFrac * (3.0f - 2.0f * letterFrac);
                penX = currentLetterStartX + (currentLetterWidth * smoothFrac);

                // Natural handwriting micro-bounce along cursive loops
                float strokeWave = (float) Math.sin(letterFrac * Math.PI * 2.0);
                float strokeLift = (float) Math.cos(letterFrac * Math.PI);
                penY = yPos + (strokeWave * size * 0.10f) - (strokeLift * size * 0.05f);

                // Slanted clipping polygon for smooth cursive letter reveal without vertical edge chop
                float slantOffset = size * 0.38f;
                mClipPath.reset();
                mClipPath.moveTo(startX - 60f, posY - size * 1.8f);
                mClipPath.lineTo(penX + slantOffset, posY - size * 1.8f);
                mClipPath.lineTo(penX - slantOffset, posY + size * 1.8f);
                mClipPath.lineTo(startX - 60f, posY + size * 1.8f);
                mClipPath.close();

                cvs.save();
                cvs.clipPath(mClipPath);

                mSignaturePaint.setShadowLayer(30.0f, 0.0f, 0.0f, Color.argb(Math.min(a, 220), r, g, b));
                cvs.drawText(txt.substring(0, activeChar + 1), startX, yPos, mSignaturePaint);
                mSignaturePaint.clearShadowLayer();
                cvs.drawText(txt.substring(0, activeChar + 1), startX, yPos, mSignaturePaint);

                cvs.restore();
            } else {
                // Phase 2: All letters complete, pen sweeps the underline flourish swoosh
                mSignaturePaint.setShadowLayer(30.0f, 0.0f, 0.0f, Color.argb(Math.min(a, 220), r, g, b));
                cvs.drawText(txt, startX, yPos, mSignaturePaint);
                mSignaturePaint.clearShadowLayer();
                cvs.drawText(txt, startX, yPos, mSignaturePaint);

                float flourishProgress = (clampedProgress - textPhaseEnd) / (1.0f - textPhaseEnd);
                float smoothFlourish = flourishProgress * flourishProgress * (3.0f - 2.0f * flourishProgress);

                drawSignatureFlourish(cvs, a, r, g, b, swashStartX, swashTotalWidth, swashBaseY, swashDepth, size, smoothFlourish);

                float t = smoothFlourish;
                float p0Y = swashBaseY - (size * 0.04f);
                float p1Y = swashBaseY + swashDepth;
                float p2Y = swashBaseY - (size * 0.12f);
                penX = swashStartX + (swashTotalWidth * smoothFlourish);
                penY = (1f - t) * (1f - t) * p0Y + 2f * (1f - t) * t * p1Y + t * t * p2Y;
            }

            // Draw glowing golden pen nib & sparkle ink trail
            drawPenNibAndSparks(cvs, a, r, g, b, penX, penY, size, clampedProgress);
        }

        mSignaturePaint.clearShadowLayer();
        mSignaturePaint.setTextAlign(Paint.Align.CENTER);
    }

    private void drawSignatureFlourish(Canvas cvs, int a, int r, int g, int b, float startX, float totalWidth, float baseY, float depth, float size, float progress) {
        if (progress <= 0f) return;

        float strokeWidth = Math.max(3.5f, size * 0.022f);
        mSwashPaint.setColor(Color.rgb(r, g, b));
        mSwashPaint.setAlpha(Math.min(a, 235));
        mSwashPaint.setStrokeWidth(strokeWidth);
        mSwashPaint.setShadowLayer(24.0f, 0f, 0f, Color.argb(Math.min(a, 200), r, g, b));

        mSwashPath.reset();
        int steps = Math.max(6, (int) (progress * 40));
        float p0X = startX;
        float p0Y = baseY - (size * 0.04f);
        float p1X = startX + (totalWidth * 0.48f);
        float p1Y = baseY + depth;
        float p2X = startX + totalWidth;
        float p2Y = baseY - (size * 0.12f);

        mSwashPath.moveTo(p0X, p0Y);
        for (int i = 1; i <= steps; i++) {
            float t = (float) i / 40.0f;
            if (t > progress) t = progress;
            float oneMinusT = 1.0f - t;
            float px = oneMinusT * oneMinusT * p0X + 2.0f * oneMinusT * t * p1X + t * t * p2X;
            float py = oneMinusT * oneMinusT * p0Y + 2.0f * oneMinusT * t * p1Y + t * t * p2Y;
            mSwashPath.lineTo(px, py);
        }

        cvs.drawPath(mSwashPath, mSwashPaint);
        mSwashPaint.clearShadowLayer();
    }

    private void drawPenNibAndSparks(Canvas cvs, int a, int r, int g, int b, float penX, float penY, float size, float progress) {
        if (a <= 20) return;

        // Outer soft golden/neon glow aura
        mPenGlowPaint.setColor(Color.rgb(255, 215, 0));
        mPenGlowPaint.setAlpha(Math.min(a, 160));
        mPenGlowPaint.setShadowLayer(22.0f, 0f, 0f, Color.rgb(255, 215, 0));
        cvs.drawCircle(penX, penY, size * 0.075f, mPenGlowPaint);
        mPenGlowPaint.clearShadowLayer();

        // Inner bright diamond white nib point
        mPenTipPaint.setColor(Color.WHITE);
        mPenTipPaint.setAlpha(Math.min(a, 255));
        mPenTipPaint.setShadowLayer(14.0f, 0f, 0f, Color.WHITE);
        cvs.drawCircle(penX, penY, size * 0.035f, mPenTipPaint);
        mPenTipPaint.clearShadowLayer();

        // Trailing luminous ink micro-sparks
        mSparkPaint.setColor(Color.rgb(255, 235, 120));
        for (int s = 1; s <= 3; s++) {
            float sparkOffset = s * (size * 0.04f);
            float sparkX = penX - sparkOffset;
            float sparkY = penY + (float) Math.sin((progress * 24.0f) + s) * (size * 0.035f);
            int sparkAlpha = (int) (Math.min(a, 190) * (1.0f - (s * 0.28f)));
            if (sparkAlpha > 0) {
                mSparkPaint.setAlpha(sparkAlpha);
                cvs.drawCircle(sparkX, sparkY, size * 0.016f * (1.0f - (s * 0.2f)), mSparkPaint);
            }
        }
    }

    public void DrawLine(Canvas cvs, int a, int r, int g, int b, float lineWidth, float fromX, float fromY, float toX, float toY) {
        mStrokePaint.setColor(Color.rgb(r, g, b));
        mStrokePaint.setAlpha(a);
        mStrokePaint.setStrokeWidth(lineWidth);
        cvs.drawLine(fromX, fromY, toX, toY, mStrokePaint);
    }

    public void DrawGradientRect(Canvas canvas, int startA, int startR, int startG, int startB,
                                 int endA, int endR, int endG, int endB,
                                 float x, float y, float width, float height) {
        Paint paint = new Paint();
        LinearGradient gradient = new LinearGradient(
                x, y, x + width, y + height,
                Color.argb(startA, startR, startG, startB),
                Color.argb(endA, endR, endG, endB),
                Shader.TileMode.CLAMP
        );
        paint.setShader(gradient);
        canvas.drawRect(x, y, x + width, y + height, paint);
    }


    public void DrawText(Canvas cvs, int a, int r, int g, int b, float stroke, String txt, float posX, float posY, float size)
    {
        mTextPaint.setColor(Color.rgb(r, g, b));
        mTextPaint.setAlpha(a);
        mTextPaint.setTextSize(size);

        if (getRight() > 1920 || getBottom() > 1920) {
            mTextPaint.setTextSize(4.0f + size);
        }
        else if (getRight() == 1920 || getBottom() == 1920) {
            mTextPaint.setTextSize(size + 2.0f);
        }
        else
            mTextPaint.setTextSize(size);

        cvs.drawText(txt, posX, posY, mTextPaint);
    }

    public void DrawTextLeft(Canvas cvs, int a, int r, int g, int b, float stroke, String txt, float posX, float posY, float size)
    {
        mTextPaint.setColor(Color.rgb(r, g, b));
        mTextPaint.setAlpha(a);
        mTextPaint.setTextSize(size);

        if (getRight() > 1920 || getBottom() > 1920) {
            mTextPaint.setTextSize(4.0f + size);
        }
        else if (getRight() == 1920 || getBottom() == 1920) {
            mTextPaint.setTextSize(size + 2.0f);
        }
        else
            mTextPaint.setTextSize(size);

        mTextPaint.setTextAlign(Paint.Align.LEFT);
        cvs.drawText(txt, posX, posY, mTextPaint);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }



    public void DrawText2(Canvas cvs, int a, int r, int g, int b, String txt, float posX, float posY, float size) {
        mTextPaint.clearShadowLayer();
        mTextPaint.setColor(Color.rgb(r, g, b));
        mTextPaint.setAlpha(a);
        if (getRight() > 1920 || getBottom() > 1920) {
            mTextPaint.setShadowLayer(12.0f, 0.0f, 0.0f, Color.rgb(r, g, b));
            mTextPaint.setTextSize(4 + size);
        }
        else if (getRight() == 1920 || getBottom() == 1920) {
            mTextPaint.setTextSize(2 + size);
            mTextPaint.setShadowLayer(12.0f, 0.0f, 0.0f, Color.rgb(r, g, b));
        }
        else
            mTextPaint.setTextSize(size);
        mTextPaint.setShadowLayer(12.0f, 0.0f, 0.0f, Color.rgb(r, g, b));
        cvs.drawText(txt, posX, posY, mTextPaint);
    }

    public void DrawCircle(Canvas cvs, int a, int r, int g, int b, float stroke, float posX, float posY, float radius)
    {
        mStrokePaint.setColor(Color.rgb(r, g, b));
        mStrokePaint.setAlpha(a);
        mStrokePaint.setStrokeWidth(stroke);
        cvs.drawCircle(posX, posY, radius, mStrokePaint);
    }

    public void DrawFilledCircle(Canvas cvs, int a, int r, int g, int b, float posX, float posY, float radius)
    {
        mFilledPaint.setColor(Color.rgb(r, g, b));
        mFilledPaint.setAlpha(a);
        cvs.drawCircle(posX, posY, radius, mFilledPaint);
    }

    public void DrawRoundRect(Canvas cvs, int a, int r, int g, int b, float stroke, int rx, int ry, float x, float y, float width, float height) {
        mStrokePaint.setStrokeWidth(stroke);
        mStrokePaint.setColor(Color.rgb(r, g, b));
        mStrokePaint.setAlpha(a);
        cvs.drawRoundRect(x, y, x + width, y + height, rx,ry, mStrokePaint);
    }

    public void DrawRect(Canvas cvs, int a, int r, int g, int b, int stroke, float x, float y, float width, float height)
    {
        mStrokePaint.setStrokeWidth(stroke);
        mStrokePaint.setColor(Color.rgb(r, g, b));
        mStrokePaint.setAlpha(a);
        cvs.drawRect(x, y, x + width, y + height, mStrokePaint);
    }

    public void DrawTextRect(Canvas cvs, int a, int r, int g, int b, int a1, int a2, int a3, int a4, int a5, int a6, int a7, int a8, float x, float y)
    {
        mRectPaint1.setColor(Color.rgb(r, g, b));
        mRectPaint1.setStrokeWidth(1.1f);
        mRectPaint1.setAlpha(a);

        mRectPaint2.setColor(Color.rgb(0, 0, 255));
        mRectPaint2.setStrokeWidth(1.1f);
        mRectPaint2.setAlpha(150);

        cvs.drawRect(x - a1, y - a2, x + a3, y + a4, mRectPaint1);
        cvs.drawRect(x - a5, y - a6, x - a7, y + a8, mRectPaint2);
    }

    public void DrawFilledRect(Canvas cvs, int a, int r, int g, int b, float x, float y, float width, float height)
    {
        mFilledPaint.setColor(Color.rgb(r, g, b));

        mFilledPaint.setAlpha(70);
        cvs.drawRect(x, y, x + width, y + height, mFilledPaint);
    }

    public void DrawFilledRectInfo(Canvas cvs, int a, int r, int g, int b, float x, float y, float width, float height)
    {
        mFilledPaint.setColor(Color.rgb(r, g, b));

        mFilledPaint.setAlpha(a);
        cvs.drawRect(x, y, x + width, y + height, mFilledPaint);
    }
}
