package io.mattcarroll.hover;

import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;

public class HoverViewStateAnchored extends BaseHoverViewState {

    private static final String TAG = "HoverViewStateAnchored";

    private FloatingTab mSelectedTab;
    private HoverMenu.Section mSelectedSection;
    private Point mDock;

    @Override
    public void takeControl(@NonNull HoverView hoverView, final Runnable onStateChanged) {
        super.takeControl(hoverView, onStateChanged);
        Log.d(TAG, "Taking control.");
        mHoverView.makeUntouchableInWindow();
        mHoverView.clearFocus();
        final int pointMargin = hoverView.getContext().getResources().getDimensionPixelSize(R.dimen.hover_tab_anchor_margin);
        mDock = new Point(
                mHoverView.mScreen.getWidth() - pointMargin,
                mHoverView.mScreen.getHeight() - pointMargin
        );

        mSelectedSection = mHoverView.mMenu.getSection(mHoverView.mSelectedSectionId);
        if (mSelectedSection == null) {
            mSelectedSection = mHoverView.mMenu.getSection(0);
        }
        mSelectedTab = mHoverView.mScreen.getChainedTab(mSelectedSection.getId());
        if (mSelectedTab == null) {
            mSelectedTab = mHoverView.mScreen.createChainedTab(mSelectedSection);
        }
        mSelectedTab.setDock(new PositionDock(mDock));
        mSelectedTab.dock(new Runnable() {
            @Override
            public void run() {
                if (!hasControl()) {
                    return;
                }
                onStateChanged.run();
            }
        });
    }

    @Override
    public void giveUpControl(@NonNull HoverViewState nextState) {
        Log.d(TAG, "Giving up control.");
        super.giveUpControl(nextState);
    }

    @Override
    public boolean respondsToBackButton() {
        return false;
    }

    @Override
    public void onBackPressed() {
    }
}
