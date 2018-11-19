package io.mattcarroll.hover;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

public class TabMessageView extends FrameLayout {
    private static final String TAG = "TabContentView";

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
        public void onDockChange(@NonNull Point dock) {
            // No-op.
        }
    };

    public TabMessageView(@NonNull Context context, @NonNull View messageView, @NonNull FloatingTab floatingTab) {
        super(context);
        mFloatingTab = floatingTab;
        addView(messageView);
        setVisibility(GONE);
    }

    public void appear(final SideDock dock) {
        setVisibility(VISIBLE);
        mSideDock = dock;
        mFloatingTab.addOnPositionChangeListener(mOnTabPositionChangeListener);
    }

    public void disappear() {
        setVisibility(GONE);
        mSideDock = null;
        mFloatingTab.removeOnPositionChangeListener(mOnTabPositionChangeListener);
    }
}
