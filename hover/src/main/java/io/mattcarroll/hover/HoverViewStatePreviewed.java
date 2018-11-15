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
package io.mattcarroll.hover;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.util.Log;

import static android.view.View.GONE;

/**
 * {@link HoverViewState} that operates the {@link HoverView} when it is closed. Closed means that
 * nothing is visible - no tabs, no content.  From the user's perspective, there is no
 * {@code HoverView}.
 */
class HoverViewStatePreviewed extends HoverViewStateCollapsed {

    private static final String TAG = "HoverViewStatePreviewed";

    @Override
    public void takeControl(@NonNull HoverView hoverView) {
        super.takeControl(hoverView);
        mHoverView.mState = this;
        mFloatingTab.showTabContentView(mHoverView.mCollapsedDock);
    }

    @Override
    protected void changeState(@NonNull HoverViewState nextState) {
        mFloatingTab.hideTabContentView();
        super.changeState(nextState);
    }

    @Override
    protected void onDroppedByUser() {
        mHoverView.mScreen.getExitView().setVisibility(GONE);
        if (null != mListener) {
            mListener.onDragEnd();
        }

        boolean droppedOnExit = mHoverView.mScreen.getExitView().isInExitZone(mFloatingTab.getPosition());
        if (droppedOnExit) {
            Log.d(TAG, "User dropped floating tab on exit.");
            closeMenu(new Runnable() {
                @Override
                public void run() {
                    if (null != mHoverView.mOnExitListener) {
                        mHoverView.mOnExitListener.onExit();
                    }
                }
            });
        } else {
            int tabSize = mHoverView.getResources().getDimensionPixelSize(R.dimen.hover_tab_size);
            Point screenSize = new Point(mHoverView.mScreen.getWidth(), mHoverView.mScreen.getHeight());
            float tabVerticalPosition = (float) mFloatingTab.getPosition().y / screenSize.y;
            @SideDock.SidePosition.Side
            final int previousSide = mHoverView.mCollapsedDock.sidePosition().getSide();
            SideDock.SidePosition sidePosition = new SideDock.SidePosition(
                    previousSide,
                    tabVerticalPosition
            );
            mHoverView.mCollapsedDock = new SideDock(
                    mHoverView,
                    tabSize,
                    sidePosition
            );
            mHoverView.saveVisualState();
            Log.d(TAG, "User dropped tab. Sending to new dock: " + mHoverView.mCollapsedDock);

            sendToDock();
        }
    }

    @Override
    public void preview() {
        Log.d(TAG, "Instructed to preview, but already previewed.");
    }

    @Override
    public void collapse() {
        changeState(mHoverView.mCollapsed);
    }
}
