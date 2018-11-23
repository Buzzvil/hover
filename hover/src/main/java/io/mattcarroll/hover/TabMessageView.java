package io.mattcarroll.hover;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

public class TabMessageView extends FrameLayout {
    private static final String TAG = "TabMessageView";

    private final FloatingTab mFloatingTab;
    private SideDock mSideDock;

    private final FloatingTab.OnPositionChangeListener mOnTabPositionChangeListener = new FloatingTab.OnPositionChangeListener() {
        @Override
        public void onPositionChange(@NonNull Point position) {
            Log.d(TAG, mFloatingTab + " tab moved to " + position);
            if (mSideDock != null && mSideDock.sidePosition().getSide() == SideDock.SidePosition.RIGHT) {
                setX(position.x - (mFloatingTab.getTabSize() / 2) - getWidth());
                setY(position.y - (mFloatingTab.getTabSize() / 2));
            } else {
                setX(position.x + (mFloatingTab.getTabSize() / 2));
                setY(position.y - (mFloatingTab.getTabSize() / 2));
            }
        }

        @Override
        public void onDockChange(@NonNull Dock dock) {
            if (dock instanceof SideDock) {
                final SideDock sideDock = (SideDock) dock;
                if (sideDock.sidePosition() != mSideDock.sidePosition()) {
                    appear(sideDock);
                }
            }
        }
    };

    public TabMessageView(@NonNull Context context, @NonNull View messageView, @NonNull FloatingTab floatingTab) {
        super(context);
        mFloatingTab = floatingTab;
        addView(messageView);
        setVisibility(GONE);
    }

    public void appear(final SideDock dock) {
        mSideDock = dock;
        mFloatingTab.addOnPositionChangeListener(mOnTabPositionChangeListener);
        startAnimation(buildAppearAnimation(dock));
        setVisibility(VISIBLE);
    }

    public void disappear() {
        mFloatingTab.removeOnPositionChangeListener(mOnTabPositionChangeListener);
        mSideDock = null;
        startAnimation(buildDisappearAnimation());
        setVisibility(GONE);
    }

    public Animation buildAppearAnimation(final SideDock dock) {
        final AnimationSet animation = new AnimationSet(true);
        final AlphaAnimation alpha = new AlphaAnimation(0, 1);
        final float fromXDelta = getResources().getDimensionPixelSize(R.dimen.hover_message_animate_translation_x)
                * (dock.sidePosition().getSide() == SideDock.SidePosition.LEFT ? -1 : 1);
        final float fromYDelta = getResources().getDimensionPixelSize(R.dimen.hover_message_animate_translation_y);
        TranslateAnimation translate = new TranslateAnimation(fromXDelta, 0, fromYDelta, 0);
        animation.setDuration(300);
        animation.setInterpolator(new LinearOutSlowInInterpolator());
        animation.addAnimation(alpha);
        animation.addAnimation(translate);
        return animation;
    }

    public Animation buildDisappearAnimation() {
        final AnimationSet animation = new AnimationSet(true);
        final AlphaAnimation alpha = new AlphaAnimation(1, 0);
        alpha.setDuration(300);
        animation.addAnimation(alpha);
        return animation;
    }
}
