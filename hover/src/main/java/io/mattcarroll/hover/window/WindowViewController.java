/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mattcarroll.hover.window;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

/**
 * Controls {@code View}s' positions, visibility, etc within a {@code Window}.
 */
public class WindowViewController {

    private WindowManager mWindowManager;
    private Context mContext;


    public WindowViewController(@NonNull WindowManager windowManager, Context context) {
        mWindowManager = windowManager;
        this.mContext = context;
    }

    public void addView(int width, int height, boolean isTouchable, @NonNull View view) {
        Log.d("TRACK_DEBUG", "WindowViewController - addView");
        addViewToWindow(view, buildLayoutParams(width, height, isTouchable));
    }

    // TODO [WITHOUT_PERMISSION] temporary code, implement real logic
    private int getLayoutParamType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(mContext)) {
            return chooseLayoutParamType(WindowManager.LayoutParams.TYPE_TOAST);
        } else {
            return chooseLayoutParamType(WindowManager.LayoutParams.TYPE_PHONE);
        }
    }

    // TODO [WITHOUT_PERMISSION] temporary code, implement real logic
    private int chooseLayoutParamType(int typeCandidate) {
        if (typeCandidate < WindowManager.LayoutParams.TYPE_STATUS_BAR) {
            return typeCandidate;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (android.provider.Settings.canDrawOverlays(mContext)) {
                return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            }
            return 2037; /* TYPE_PRESENTATION ??? */
        } else if (typeCandidate <= 0) {
            return WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            return typeCandidate;
        }
    }

    // TODO [WITHOUT_PERMISSION] temporary code, implement real logic
    private WindowManager.LayoutParams buildLayoutParams(final int width, final int height, final boolean isTouchable) {
        Log.d("TRACK_DEBUG", "WindowViewController - buildLayoutParams");
        // If this view is untouchable then add the corresponding flag, otherwise set to zero which
        // won't have any effect on the OR'ing of flags.
        int touchableFlag = isTouchable ? 0 : WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

        int windowType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                width,
                height,
                getLayoutParamType(),
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                PixelFormat.TRANSLUCENT
        );
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;


        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 0;

        return params;
    }

    public void removeView(@NonNull View view) {
        if (null != view.getParent()) {
            mWindowManager.removeView(view);
        }
    }

    public Point getViewPosition(@NonNull View view) {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) view.getLayoutParams();
        return new Point(params.x, params.y);
    }

    public void moveViewTo(View view, int x, int y) {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) view.getLayoutParams();
        if (params == null) {
            params = buildLayoutParams(view.getWidth(), view.getHeight(), true);
        }

        params.x = x;
        params.y = y;

        updateViewLayout(view, params);
    }

    public void showView(View view) {
        try {
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) view.getLayoutParams();
            mWindowManager.addView(view, params);
        } catch (IllegalStateException e) {
            // The view is already visible.
        }
    }

    public void hideView(View view) {
        try {
            mWindowManager.removeView(view);
        } catch (IllegalArgumentException e) {
            // The View wasn't visible to begin with.
        }
    }

    public void makeTouchable(View view) {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) view.getLayoutParams();
        if (params == null) {
            params = buildLayoutParams(view.getWidth(), view.getHeight(), true);
        }
        params.flags = params.flags & ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE & ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        updateViewLayout(view, params);
    }

    public void makeUntouchable(View view) {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) view.getLayoutParams();
        if (params == null) {
            params = buildLayoutParams(view.getWidth(), view.getHeight(), true);
        }
        params.flags = params.flags | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        updateViewLayout(view, params);
    }

    private void updateViewLayout(final View view, final WindowManager.LayoutParams params) {
        try {
            mWindowManager.updateViewLayout(view, params);
        } catch (IllegalArgumentException e) {
            // View is not attached to the window manager
            addViewToWindow(view, params);
        }
    }

    private void addViewToWindow(final View view, final WindowManager.LayoutParams params) {
        try {
            mWindowManager.addView(view, params);
        } catch (WindowManager.BadTokenException e) {
            // Permission denied. Cannot add the View to the Window.
        }
    }

    public Point getWindowSize() {
        final Point windowSize = new Point();
        mWindowManager.getDefaultDisplay().getSize(windowSize);
        return new Point(windowSize.x, windowSize.y);
    }

}
