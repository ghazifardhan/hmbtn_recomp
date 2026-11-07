package com.harvestmoon.btn.recomp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import java.util.HashSet;
import java.util.Set;

public class TouchControllerView extends View {
    public static final String PREFS_NAME = "HMRecompPrefs";
    public static final String KEY_ORIENTATION = "orientation";
    public static final String KEY_OPACITY = "opacity";
    public static final String KEY_HAPTIC = "haptic";

    public static final String ORIENTATION_LANDSCAPE = "landscape";
    public static final String ORIENTATION_PORTRAIT = "portrait";
    public static final String ORIENTATION_AUTO = "auto";

    private final SDLActivity mActivity;
    private final Vibrator mVibrator;

    private int mAlpha = 140; // ~55% default opacity
    private boolean mHapticEnabled = true;

    // Paints
    private final Paint mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mBgPressedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mSymbolPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Button definitions
    private static final int BTN_UP = KeyEvent.KEYCODE_DPAD_UP;
    private static final int BTN_DOWN = KeyEvent.KEYCODE_DPAD_DOWN;
    private static final int BTN_LEFT = KeyEvent.KEYCODE_DPAD_LEFT;
    private static final int BTN_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;

    private static final int BTN_CROSS = KeyEvent.KEYCODE_X;
    private static final int BTN_CIRCLE = KeyEvent.KEYCODE_S;
    private static final int BTN_SQUARE = KeyEvent.KEYCODE_Z;
    private static final int BTN_TRIANGLE = KeyEvent.KEYCODE_A;

    private static final int BTN_L1 = KeyEvent.KEYCODE_Q;
    private static final int BTN_R1 = KeyEvent.KEYCODE_W;
    private static final int BTN_L2 = KeyEvent.KEYCODE_E;
    private static final int BTN_R2 = KeyEvent.KEYCODE_R;

    private static final int BTN_SELECT = KeyEvent.KEYCODE_SHIFT_RIGHT;
    private static final int BTN_START = KeyEvent.KEYCODE_ENTER;

    private static final int BTN_CONFIG = -999;

    // Bounds & Positions
    private float mDpadCenterX, mDpadCenterY, mDpadRadius;
    private float mActionCenterX, mActionCenterY, mActionRadius;
    private float mBtnRadius;

    private final RectF mRectL1 = new RectF();
    private final RectF mRectR1 = new RectF();
    private final RectF mRectL2 = new RectF();
    private final RectF mRectR2 = new RectF();
    private final RectF mRectSelect = new RectF();
    private final RectF mRectStart = new RectF();
    private final RectF mRectConfig = new RectF();

    private final Set<Integer> mPressedKeys = new HashSet<>();
    private boolean mConfigPressed = false;

    public TouchControllerView(SDLActivity activity) {
        super(activity);
        this.mActivity = activity;
        this.mVibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);

        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPressedPaint.setStyle(Paint.Style.FILL);
        mBgPressedPaint.setColor(Color.argb(220, 100, 150, 255));

        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(3f);

        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setFakeBoldText(true);

        mSymbolPaint.setStyle(Paint.Style.STROKE);
        mSymbolPaint.setStrokeWidth(5f);

        loadPreferences();
    }

    public void loadPreferences() {
        SharedPreferences prefs = mActivity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mAlpha = prefs.getInt(KEY_OPACITY, 140);
        mHapticEnabled = prefs.getBoolean(KEY_HAPTIC, true);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        layoutButtons(w, h);
    }

    private void layoutButtons(int w, int h) {
        boolean isLandscape = w > h;

        if (isLandscape) {
            mBtnRadius = Math.min(w, h) * 0.085f;
            mDpadRadius = mBtnRadius * 1.85f;
            mDpadCenterX = w * 0.14f;
            mDpadCenterY = h * 0.72f;

            mActionRadius = mBtnRadius * 1.85f;
            mActionCenterX = w * 0.86f;
            mActionCenterY = h * 0.72f;

            float shW = mBtnRadius * 2.2f;
            float shH = mBtnRadius * 1.1f;

            mRectL1.set(w * 0.04f, h * 0.10f, w * 0.04f + shW, h * 0.10f + shH);
            mRectL2.set(w * 0.18f, h * 0.10f, w * 0.18f + shW, h * 0.10f + shH);

            mRectR2.set(w * 0.82f - shW, h * 0.10f, w * 0.82f, h * 0.10f + shH);
            mRectR1.set(w * 0.96f - shW, h * 0.10f, w * 0.96f, h * 0.10f + shH);

            float menuW = mBtnRadius * 1.9f;
            float menuH = mBtnRadius * 0.85f;
            mRectSelect.set(w * 0.40f - menuW / 2, h * 0.91f - menuH / 2, w * 0.40f + menuW / 2, h * 0.91f + menuH / 2);
            mRectStart.set(w * 0.60f - menuW / 2, h * 0.91f - menuH / 2, w * 0.60f + menuW / 2, h * 0.91f + menuH / 2);

            float cfgSize = mBtnRadius * 0.9f;
            mRectConfig.set(w * 0.50f - cfgSize, h * 0.06f - cfgSize, w * 0.50f + cfgSize, h * 0.06f + cfgSize);
        } else {
            // Portrait mode: game screen is top 4:3 (~810px high), controls on bottom half
            mBtnRadius = Math.min(w, h) * 0.09f;
            mDpadRadius = mBtnRadius * 2.0f;
            mDpadCenterX = w * 0.28f;
            mDpadCenterY = h * 0.78f;

            mActionRadius = mBtnRadius * 2.0f;
            mActionCenterX = w * 0.72f;
            mActionCenterY = h * 0.78f;

            float shW = mBtnRadius * 2.1f;
            float shH = mBtnRadius * 1.0f;

            mRectL1.set(w * 0.06f, h * 0.52f, w * 0.06f + shW, h * 0.52f + shH);
            mRectL2.set(w * 0.28f, h * 0.52f, w * 0.28f + shW, h * 0.52f + shH);

            mRectR2.set(w * 0.72f - shW, h * 0.52f, w * 0.72f, h * 0.52f + shH);
            mRectR1.set(w * 0.94f - shW, h * 0.52f, w * 0.94f, h * 0.52f + shH);

            float menuW = mBtnRadius * 1.8f;
            float menuH = mBtnRadius * 0.8f;
            mRectSelect.set(w * 0.35f - menuW / 2, h * 0.61f - menuH / 2, w * 0.35f + menuW / 2, h * 0.61f + menuH / 2);
            mRectStart.set(w * 0.65f - menuW / 2, h * 0.61f - menuH / 2, w * 0.65f + menuW / 2, h * 0.61f + menuH / 2);

            float cfgSize = mBtnRadius * 0.9f;
            mRectConfig.set(w * 0.50f - cfgSize, h * 0.44f - cfgSize, w * 0.50f + cfgSize, h * 0.44f + cfgSize);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mAlpha <= 0) return;

        mBgPaint.setColor(Color.argb(mAlpha, 30, 30, 30));
        mBorderPaint.setColor(Color.argb(Math.min(255, mAlpha + 50), 200, 200, 200));
        mTextPaint.setColor(Color.argb(Math.min(255, mAlpha + 80), 255, 255, 255));
        mTextPaint.setTextSize(mBtnRadius * 0.6f);

        // 1. D-Pad
        drawDpad(canvas);

        // 2. Action buttons
        drawActionButtons(canvas);

        // 3. Shoulders
        drawRoundedBtn(canvas, mRectL1, "L1", mPressedKeys.contains(BTN_L1));
        drawRoundedBtn(canvas, mRectL2, "L2", mPressedKeys.contains(BTN_L2));
        drawRoundedBtn(canvas, mRectR1, "R1", mPressedKeys.contains(BTN_R1));
        drawRoundedBtn(canvas, mRectR2, "R2", mPressedKeys.contains(BTN_R2));

        // 4. Select & Start
        drawRoundedBtn(canvas, mRectSelect, "SELECT", mPressedKeys.contains(BTN_SELECT));
        drawRoundedBtn(canvas, mRectStart, "START", mPressedKeys.contains(BTN_START));

        // 5. Config button
        drawRoundedBtn(canvas, mRectConfig, "⚙", mConfigPressed);
    }

    private void drawDpad(Canvas canvas) {
        // Base cross or circle
        canvas.drawCircle(mDpadCenterX, mDpadCenterY, mDpadRadius, mBgPaint);
        canvas.drawCircle(mDpadCenterX, mDpadCenterY, mDpadRadius, mBorderPaint);

        float offset = mDpadRadius * 0.60f;
        float r = mBtnRadius * 0.70f;

        drawDpadDirection(canvas, mDpadCenterX, mDpadCenterY - offset, r, "▲", mPressedKeys.contains(BTN_UP));
        drawDpadDirection(canvas, mDpadCenterX, mDpadCenterY + offset, r, "▼", mPressedKeys.contains(BTN_DOWN));
        drawDpadDirection(canvas, mDpadCenterX - offset, mDpadCenterY, r, "◀", mPressedKeys.contains(BTN_LEFT));
        drawDpadDirection(canvas, mDpadCenterX + offset, mDpadCenterY, r, "▶", mPressedKeys.contains(BTN_RIGHT));
    }

    private void drawDpadDirection(Canvas canvas, float cx, float cy, float r, String label, boolean pressed) {
        if (pressed) {
            canvas.drawCircle(cx, cy, r, mBgPressedPaint);
        }
        mTextPaint.setTextSize(r * 1.1f);
        canvas.drawText(label, cx, cy + (mTextPaint.getTextSize() * 0.35f), mTextPaint);
    }

    private void drawActionButtons(Canvas canvas) {
        float offset = mActionRadius * 0.65f;
        float r = mBtnRadius * 0.75f;

        // Diamond layout: Triangle(top), Cross(bottom), Square(left), Circle(right)
        drawCircleBtn(canvas, mActionCenterX, mActionCenterY - offset, r, "△", Color.rgb(50, 205, 50), mPressedKeys.contains(BTN_TRIANGLE));
        drawCircleBtn(canvas, mActionCenterX, mActionCenterY + offset, r, "✕", Color.rgb(100, 149, 237), mPressedKeys.contains(BTN_CROSS));
        drawCircleBtn(canvas, mActionCenterX - offset, mActionCenterY, r, "□", Color.rgb(255, 105, 180), mPressedKeys.contains(BTN_SQUARE));
        drawCircleBtn(canvas, mActionCenterX + offset, mActionCenterY, r, "○", Color.rgb(255, 69, 0), mPressedKeys.contains(BTN_CIRCLE));
    }

    private void drawCircleBtn(Canvas canvas, float cx, float cy, float r, String sym, int color, boolean pressed) {
        if (pressed) {
            canvas.drawCircle(cx, cy, r, mBgPressedPaint);
        } else {
            canvas.drawCircle(cx, cy, r, mBgPaint);
        }
        mBorderPaint.setColor(pressed ? Color.WHITE : Color.argb(mAlpha, 220, 220, 220));
        canvas.drawCircle(cx, cy, r, mBorderPaint);

        mTextPaint.setColor(color);
        mTextPaint.setTextSize(r * 1.2f);
        canvas.drawText(sym, cx, cy + (mTextPaint.getTextSize() * 0.35f), mTextPaint);
    }

    private void drawRoundedBtn(Canvas canvas, RectF rect, String label, boolean pressed) {
        if (pressed) {
            canvas.drawRoundRect(rect, 15f, 15f, mBgPressedPaint);
        } else {
            canvas.drawRoundRect(rect, 15f, 15f, mBgPaint);
        }
        mBorderPaint.setColor(pressed ? Color.WHITE : Color.argb(mAlpha, 220, 220, 220));
        canvas.drawRoundRect(rect, 15f, 15f, mBorderPaint);

        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(rect.height() * 0.50f);
        canvas.drawText(label, rect.centerX(), rect.centerY() + (mTextPaint.getTextSize() * 0.35f), mTextPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int count = event.getPointerCount();

        Set<Integer> newPressed = new HashSet<>();
        boolean newConfigPressed = false;

        boolean pointerEnding = (action == MotionEvent.ACTION_UP ||
                                 action == MotionEvent.ACTION_POINTER_UP ||
                                 action == MotionEvent.ACTION_CANCEL);
        int endingIndex = (action == MotionEvent.ACTION_POINTER_UP) ? event.getActionIndex() : -1;

        for (int i = 0; i < count; i++) {
            if (pointerEnding && (i == endingIndex || action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)) {
                // If config button was released, trigger dialog
                if (action == MotionEvent.ACTION_UP || (action == MotionEvent.ACTION_POINTER_UP && i == endingIndex)) {
                    float x = event.getX(i);
                    float y = event.getY(i);
                    if (mRectConfig.contains(x, y)) {
                        showConfigDialog();
                    }
                }
                continue;
            }

            float x = event.getX(i);
            float y = event.getY(i);

            // Check Config button
            if (mRectConfig.contains(x, y)) {
                newConfigPressed = true;
                continue;
            }

            // Check D-Pad
            float dpadDx = x - mDpadCenterX;
            float dpadDy = y - mDpadCenterY;
            float dpadDist = (float) Math.hypot(dpadDx, dpadDy);
            if (dpadDist <= mDpadRadius * 1.25f && dpadDist > (mDpadRadius * 0.15f)) {
                double angle = Math.toDegrees(Math.atan2(dpadDy, dpadDx)); // -180 to 180
                // Up: -135 to -45
                // Right: -45 to 45
                // Down: 45 to 135
                // Left: > 135 or < -135
                if (angle >= -157.5 && angle <= -22.5) {
                    newPressed.add(BTN_UP);
                }
                if (angle >= 22.5 && angle <= 157.5) {
                    newPressed.add(BTN_DOWN);
                }
                if (angle >= -67.5 && angle <= 67.5) {
                    newPressed.add(BTN_RIGHT);
                }
                if (angle >= 112.5 || angle <= -112.5) {
                    newPressed.add(BTN_LEFT);
                }
            }

            // Check Action Buttons
            float actDx = x - mActionCenterX;
            float actDy = y - mActionCenterY;
            float actDist = (float) Math.hypot(actDx, actDy);
            if (actDist <= mActionRadius * 1.25f) {
                float r = mBtnRadius * 0.9f;
                float offset = mActionRadius * 0.65f;
                if (Math.hypot(x - mActionCenterX, y - (mActionCenterY - offset)) <= r) newPressed.add(BTN_TRIANGLE);
                if (Math.hypot(x - mActionCenterX, y - (mActionCenterY + offset)) <= r) newPressed.add(BTN_CROSS);
                if (Math.hypot(x - (mActionCenterX - offset), y - mActionCenterY) <= r) newPressed.add(BTN_SQUARE);
                if (Math.hypot(x - (mActionCenterX + offset), y - mActionCenterY) <= r) newPressed.add(BTN_CIRCLE);
            }

            // Check Shoulders & Menu
            if (mRectL1.contains(x, y)) newPressed.add(BTN_L1);
            if (mRectL2.contains(x, y)) newPressed.add(BTN_L2);
            if (mRectR1.contains(x, y)) newPressed.add(BTN_R1);
            if (mRectR2.contains(x, y)) newPressed.add(BTN_R2);
            if (mRectSelect.contains(x, y)) newPressed.add(BTN_SELECT);
            if (mRectStart.contains(x, y)) newPressed.add(BTN_START);
        }

        // Apply changes to SDL
        boolean hasNewlyPressed = false;
        for (int key : newPressed) {
            if (!mPressedKeys.contains(key)) {
                SDLActivity.onNativeKeyDown(key);
                hasNewlyPressed = true;
            }
        }
        for (int key : mPressedKeys) {
            if (!newPressed.contains(key)) {
                SDLActivity.onNativeKeyUp(key);
            }
        }

        if (hasNewlyPressed && mHapticEnabled && mVibrator != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mVibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    mVibrator.vibrate(20);
                }
            } catch (Exception ignored) {}
        }

        mPressedKeys.clear();
        mPressedKeys.addAll(newPressed);
        mConfigPressed = newConfigPressed;
        invalidate();

        // Return true if touched any button or overlay region
        return !newPressed.isEmpty() || newConfigPressed || count > 0;
    }

    public void showConfigDialog() {
        SharedPreferences prefs = mActivity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String currentOrient = prefs.getString(KEY_ORIENTATION, ORIENTATION_LANDSCAPE);

        final String[] orientations = {"Landscape", "Portrait", "Auto / Sensor"};
        int selectedOrient = 0;
        if (ORIENTATION_PORTRAIT.equals(currentOrient)) selectedOrient = 1;
        else if (ORIENTATION_AUTO.equals(currentOrient)) selectedOrient = 2;

        final int[] chosenOrient = {selectedOrient};

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("Game Settings");

        String[] options = {
            "Orientation: " + orientations[selectedOrient],
            "Touch Controls Opacity: " + (mAlpha * 100 / 255) + "%",
            "Haptic Feedback: " + (mHapticEnabled ? "ON" : "OFF")
        };

        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showOrientationPicker();
            } else if (which == 1) {
                showOpacityPicker();
            } else if (which == 2) {
                mHapticEnabled = !mHapticEnabled;
                prefs.edit().putBoolean(KEY_HAPTIC, mHapticEnabled).apply();
                showConfigDialog();
            }
        });

        builder.setPositiveButton("Close", null);
        builder.show();
    }

    private void showOrientationPicker() {
        SharedPreferences prefs = mActivity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String currentOrient = prefs.getString(KEY_ORIENTATION, ORIENTATION_LANDSCAPE);

        final String[] items = {"Landscape (Wide)", "Portrait (Tall)", "Auto-Rotate (Sensor)"};
        int currentIdx = 0;
        if (ORIENTATION_PORTRAIT.equals(currentOrient)) currentIdx = 1;
        else if (ORIENTATION_AUTO.equals(currentOrient)) currentIdx = 2;

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("Choose Screen Orientation");
        builder.setItems(items, (dialog, which) -> {
            String choice = ORIENTATION_LANDSCAPE;
            if (which == 1) choice = ORIENTATION_PORTRAIT;
            else if (which == 2) choice = ORIENTATION_AUTO;

            prefs.edit().putString(KEY_ORIENTATION, choice).apply();
            mActivity.applyOrientation(choice);
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showOpacityPicker() {
        SharedPreferences prefs = mActivity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        final String[] items = {"Hidden (0%)", "Faint (25%)", "Medium (55%)", "Solid (85%)"};
        final int[] alphas = {0, 64, 140, 220};

        int currentIdx = 2;
        for (int i = 0; i < alphas.length; i++) {
            if (Math.abs(mAlpha - alphas[i]) < 30) {
                currentIdx = i;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("Touch Controls Opacity");
        builder.setItems(items, (dialog, which) -> {
            mAlpha = alphas[which];
            prefs.edit().putInt(KEY_OPACITY, mAlpha).apply();
            invalidate();
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
