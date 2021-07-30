package com.viven.imagezoom;

import ohos.aafwk.ability.Ability;
import ohos.agp.animation.AnimatorValue;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.StackLayout;
import ohos.agp.utils.Point;
import ohos.agp.window.dialog.CommonDialog;
import ohos.agp.window.service.Display;
import ohos.agp.window.service.DisplayManager;
import ohos.app.Context;
import ohos.multimodalinput.event.MmiPoint;
import ohos.multimodalinput.event.TouchEvent;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by viventhraarao on 25/11/2016.
 */

public class ImageZoomHelper {
    private static final int DOUBLE_TAP_DELAY = 600;
    private static final int START_FLAG = 0;
    private static final int SINGLE_TAP_FLAG = 1;
    private static final int DOUBLE_TAP_FLAG = 2;
    private static final float END_FRACTION = 1f;
    private static final long ZOOM_ANIMATION_DURATION = 200;
    private Component zoomableView = null;
    private ComponentContainer parentOfZoomableView;
    private ComponentContainer.LayoutConfig zoomableViewLayoutParam;
    private CommonDialog dialog;
    private int viewIndex;
    private Component darkView;
    private double originalDistance;
    private int[] twoPointCenter;
    private int[] originalMargins;
    private int pivotX = 0;
    private int pivotY = 0;
    private final WeakReference<Ability> abilityWeakReference;
    private int tapFlag = START_FLAG;
    private Instant tapStart;
    private Instant tapEnd;
    private int originalHeight;
    private int originalWidth;
    private int deltaX;
    private int deltaY;
    private boolean isAnimatingDismiss = false;
    private final List<OnZoomListener> zoomListeners = new ArrayList<>();

    /**
     * Constructor for ImageZoomHelper class.
     *
     * @param ability Indicates the Ability object that hosts the current Slice.
     */
    public ImageZoomHelper(Ability ability) {
        this.abilityWeakReference = new WeakReference<>(ability);
    }

    /**
     * Handles touch events, identifies double tap, pinch and drag.
     *
     * @param ev Indicates the touch event.
     * @return boolean
     */
    public boolean onDispatchTouchEvent(TouchEvent ev) {
        Ability ability;
        if ((ability = abilityWeakReference.get()) == null) {
            return false;
        }

        if (ev.getPointerCount() == 1) {
            final int x = (int) ev.getPointerScreenPosition(0).getX();
            final int y = (int) ev.getPointerScreenPosition(0).getY();
            switch (ev.getAction()) {
                case TouchEvent.PRIMARY_POINT_DOWN:
                    pointDownUpdate(x, y, ability);
                    break;
                case TouchEvent.PRIMARY_POINT_UP:
                    pointUpUpdate();
                    break;
                case TouchEvent.POINT_MOVE:
                    pointMoveUpdate(x, y);
                    break;
                default:
                    break;
            }
            return true;
        } else if (ev.getPointerCount() == 2) {
            return twoTouchPoint(ability, ev);
        }

        return false;
    }

    /**
     * Handles the two touch points events.
     *
     * @param ability Indicates the Ability object that hosts the current Slice.
     */
    private void doubleTapped(Ability ability) {
        if (zoomableView == null) {
            Component view = ability.getCurrentFocus();

            if (view != null) {
                originalHeight = view.getHeight();
                originalWidth = view.getWidth();
                final float pctIncrease = 1;

                originalMargins = view.getLocationOnScreen();
                zoomableView = view;

                moveView(view);

                sendZoomEventToListeners(zoomableView, true);

                final int widthStart = originalWidth;
                final int heightStart = originalHeight;
                final int leftMarginStart = originalMargins[0];
                final int topMarginStart = originalMargins[1];
                final float alphaStart = 0f;

                final int widthEnd = (int) ((1 + pctIncrease) * originalWidth);
                final int heightEnd = (int) ((1 + pctIncrease) * originalHeight);
                final int leftMarginEnd = (int) (originalMargins[0] - (pctIncrease / 2) * originalWidth);
                final int topMarginEnd = (int) (originalMargins[1] - (pctIncrease / 2) * originalHeight);
                final float alphaEnd = 1f;

                final AnimatorValue valueAnimatorZoomIn = new AnimatorValue();
                valueAnimatorZoomIn.setDuration(ZOOM_ANIMATION_DURATION);
                valueAnimatorZoomIn.setLoopedCount(0);

                valueAnimatorZoomIn.setValueUpdateListener((animatorValue, v) -> {
                    if (v < END_FRACTION) {
                        updateZoomableView(v, widthStart, heightStart,
                                widthEnd, heightEnd);
                        updateZoomableViewMargins(
                                (int) ((leftMarginEnd - leftMarginStart) * v) + leftMarginStart,
                                (int) ((topMarginEnd - topMarginStart) * v) + topMarginStart
                        );
                        darkView.setAlpha(((alphaEnd - alphaStart) * v) + alphaStart);
                    } else {
                        updateZoomableView(END_FRACTION, widthStart, heightStart,
                                widthEnd, heightEnd);
                        updateZoomableViewMargins(
                                (int) ((leftMarginEnd - leftMarginStart) * END_FRACTION) + leftMarginStart,
                                (int) ((topMarginEnd - topMarginStart) * END_FRACTION) + topMarginStart
                        );
                        valueAnimatorZoomIn.setValueUpdateListener(null);
                    }
                });
                valueAnimatorZoomIn.start();
            }
        } else {
            restoreView();
        }
    }

    /**
     * Restores the original view.
     */
    private void restoreView() {
        if (zoomableView != null && !isAnimatingDismiss) {
            isAnimatingDismiss = true;

            final int widthStart = zoomableView.getWidth();
            final int heightStart = zoomableView.getHeight();
            final int leftMarginStart = zoomableView.getMarginLeft();
            final int topMarginStart = zoomableView.getMarginTop();
            final float alphaStart = darkView.getAlpha();

            final int widthEnd = originalWidth;
            final int heightEnd = originalHeight;
            final int leftMarginEnd = originalMargins[0];
            final int topMarginEnd = originalMargins[1];
            final float alphaEnd = 0f;

            final AnimatorValue valueAnimatorZoomOut = new AnimatorValue();
            valueAnimatorZoomOut.setDuration(ZOOM_ANIMATION_DURATION);
            valueAnimatorZoomOut.setLoopedCount(0);

            valueAnimatorZoomOut.setValueUpdateListener((animatorValue, v) -> {
                if (v < END_FRACTION) {
                    updateZoomableView(v, widthStart, heightStart,
                            widthEnd, heightEnd);
                    updateZoomableViewMargins(
                            (int) ((leftMarginEnd - leftMarginStart) * v) + leftMarginStart,
                            (int) ((topMarginEnd - topMarginStart) * v) + topMarginStart
                    );
                    darkView.setAlpha(((alphaEnd - alphaStart) * v) + alphaStart);
                } else {
                    updateZoomableView(END_FRACTION, widthStart, heightStart,
                            widthEnd, heightEnd);
                    updateZoomableViewMargins(
                            (int) ((leftMarginEnd - leftMarginStart) * END_FRACTION) + leftMarginStart,
                            (int) ((topMarginEnd - topMarginStart) * END_FRACTION) + topMarginStart
                    );
                    dismissDialogAndViews();
                    valueAnimatorZoomOut.setValueUpdateListener(null);
                }
            });
            valueAnimatorZoomOut.start();
        }
    }

    /**
     * Handles the two touch points events.
     *
     * @param ability Indicates the Ability object that hosts the current Slice.
     * @param ev Indicates the touch event.
     */
    private boolean twoTouchPoint(Ability ability, TouchEvent ev) {
        if (zoomableView == null) {
            Component view = ability.getCurrentFocus();
            if (view != null) {
                originalHeight = view.getHeight();
                originalWidth = view.getWidth();

                zoomableView = view;

                // get view's original location relative to the window
                originalMargins = view.getLocationOnScreen();

                moveView(view);

                // Pointer variables to store the original touch positions
                MmiPoint pointerCoords1 = new MmiPoint(ev.getPointerPosition(0).getX(),
                        ev.getPointerPosition(0).getY());
                MmiPoint pointerCoords2 = new MmiPoint(ev.getPointerPosition(1).getX(),
                        ev.getPointerPosition(1).getY());

                // storing distance between the two positions to be compared later on for
                // zooming
                originalDistance = (int) getDistance(pointerCoords1.getX(), pointerCoords2.getX(),
                        pointerCoords1.getY(), pointerCoords2.getY());

                // storing center point of the two pointers to move the view according to the
                // touch position
                twoPointCenter = new int[]{ (int) ((pointerCoords2.getX() + pointerCoords1.getX()) / 2), (int)
                        ((pointerCoords2.getY() + pointerCoords1.getY()) / 2)};

                //storing pivot point for zooming image from its touch coordinates
                pivotX = (int) ev.getPointerScreenPosition(0).getX() - originalMargins[0];
                pivotY = (int) ev.getPointerScreenPosition(0).getY() - originalMargins[1];

                sendZoomEventToListeners(zoomableView, true);
                return true;
            }
        } else {
            MmiPoint pointerCoords1 = new MmiPoint(ev.getPointerPosition(0).getX(),
                    ev.getPointerPosition(0).getY());
            MmiPoint pointerCoords2 = new MmiPoint(ev.getPointerPosition(1).getX(),
                    ev.getPointerPosition(1).getY());

            final int[] newCenter = new int[]{ (int) ((pointerCoords2.getX() + pointerCoords1.getX()) / 2), (int)
                    ((pointerCoords2.getY() + pointerCoords1.getY()) / 2)};

            int currentDistance = (int) getDistance(pointerCoords1.getX(), pointerCoords2.getX(),
                    pointerCoords1.getY(), pointerCoords2.getY());
            double pctIncrease = (currentDistance - originalDistance) / originalDistance;

            zoomableView.setPivotX(pivotX);
            zoomableView.setPivotY(pivotY);

            zoomableView.setWidth((int) (1 + pctIncrease) * originalWidth);
            zoomableView.setHeight((int) (1 + pctIncrease) * originalHeight);

            updateZoomableViewMargins(newCenter[0] - twoPointCenter[0] + originalMargins[0],
                    newCenter[1] - twoPointCenter[1] + originalMargins[1]);

            darkView.setAlpha((float) (pctIncrease / 8));
            return true;
        }
        return false;
    }

    private void moveView(Component view) {

        // this FrameLayout will be the zoomableView's temporary parent
        StackLayout frameLayout = new StackLayout(view.getContext());
        frameLayout.setLayoutConfig(new StackLayout.LayoutConfig(getScreenWidth(view.getContext()),
                getScreenHeight(view.getContext())));

        // this view is to gradually darken the backdrop as user zooms
        // (here in this case it is a dummy view)
        darkView = new Component(view.getContext());

        // adding darkening backdrop to the frameLayout
        frameLayout.addComponent(darkView, new StackLayout.LayoutConfig(
                ComponentContainer.LayoutConfig.MATCH_PARENT,
                ComponentContainer.LayoutConfig.MATCH_PARENT));

        // the Dialog that will hold the FrameLayout
        dialog = new CommonDialog(view.getContext());
        dialog = dialog.setContentCustomComponent(frameLayout);
        dialog.show();
        dialog.siteKeyboardCallback((interfaceDialog, keyEvent) -> {
            if (keyEvent.isKeyDown()) {
                restoreView();
            }
            return false;
        });

        // get the parent of the zoomable view and get it's index and layout param
        parentOfZoomableView = (ComponentContainer) zoomableView.getComponentParent();
        viewIndex = parentOfZoomableView.getChildIndex(zoomableView);
        zoomableViewLayoutParam = zoomableView.getLayoutConfig();

        StackLayout.LayoutConfig zoomableViewFrameLayoutParam;
        // this is the new layout param for the zoomableView
        zoomableViewFrameLayoutParam = new StackLayout.LayoutConfig(view.getWidth(), view.getHeight());
        zoomableViewFrameLayoutParam.setMarginLeft(originalMargins[0]);
        zoomableViewFrameLayoutParam.setMarginTop(originalMargins[1]);

        // zoomableView has to be removed from parent view before being added to it's
        // new parent
        parentOfZoomableView.removeComponent(zoomableView);
        frameLayout.addComponent(zoomableView, zoomableViewFrameLayoutParam);
    }

    /**
     * Updates the flags when an interaction with the screen has started.
     *
     * @param x x coordinate of the view.
     * @param y y coordinate of the view.
     * @param ability Indicates the Ability object that hosts the current Slice.
     */
    private void pointDownUpdate(int x, int y, Ability ability) {
        tapStart = Instant.now();
        if (tapFlag == SINGLE_TAP_FLAG) {
            if (Duration.between(tapEnd, tapStart).toMillis() <= DOUBLE_TAP_DELAY) {
                tapFlag = DOUBLE_TAP_FLAG;
                doubleTapped(ability);
            } else {
                tapFlag = START_FLAG;
            }
        }
        if (zoomableView != null) {
            StackLayout.LayoutConfig layoutConfig =
                    (StackLayout.LayoutConfig) zoomableView.getLayoutConfig();
            deltaX = x - layoutConfig.getMarginLeft();
            deltaY = y - layoutConfig.getMarginTop();
        }
    }

    /**
     * Updates the flags when an interaction with the screen is done.
     */
    private void pointUpUpdate() {
        tapEnd = Instant.now();
        if (tapFlag == DOUBLE_TAP_FLAG) {
            tapFlag = SINGLE_TAP_FLAG;
        } else {
            if (Duration.between(tapStart, tapEnd).toMillis() <= DOUBLE_TAP_DELAY) {
                tapFlag = SINGLE_TAP_FLAG;
            }
        }
    }

    /**
     * Updates the position of the zoomable view (Drag Functionality).
     *
     * @param x x coordinate of the view.
     * @param y y coordinate of the view.
     */
    private void pointMoveUpdate(int x, int y) {
        if (zoomableView != null) {
            StackLayout.LayoutConfig layoutConfig =
                    (StackLayout.LayoutConfig) zoomableView.getLayoutConfig();
            layoutConfig.setMarginLeft(x - deltaX);
            layoutConfig.setMarginTop(y - deltaY);
            zoomableView.setLayoutConfig(layoutConfig);
        }
    }

    /**
     * Updates the height and width of zoomable view.
     *
     * @param animatedFraction The fraction by which the view has to be updated.
     * @param widthStart Initial value of width of the view.
     * @param heightStart Initial value of height of the view.
     * @param widthEnd Final value of width of the view.
     * @param heightEnd Final value of height of the view.
     */
    private void updateZoomableView(float animatedFraction, int widthStart, int heightStart,
                                    int widthEnd, int heightEnd) {
        zoomableView.setWidth((int) (((widthEnd - widthStart) * animatedFraction) + widthStart));
        zoomableView.setHeight((int) (((heightEnd - heightStart) * animatedFraction) + heightStart));
    }

    /**
     * Updates the margins of the zoomable view.
     *
     * @param left Left margin of the view.
     * @param top Top margin of the view.
     */
    private void updateZoomableViewMargins(int left, int top) {
        if (zoomableView != null) {
            zoomableView.setMarginLeft(left);
            zoomableView.setMarginTop(top);
        }
    }

    /**
     * Dismiss dialog and set views to null for garbage collection.
     */
    private void dismissDialogAndViews() {
        sendZoomEventToListeners(zoomableView, false);

        if (zoomableView != null) {
            ComponentContainer parent = (ComponentContainer) zoomableView.getComponentParent();
            parent.removeComponent(zoomableView);
            parentOfZoomableView.addComponent(zoomableView, viewIndex, zoomableViewLayoutParam);
            final Component finalZoomView = zoomableView;
            dismissDialog();
            finalZoomView.invalidate();
        } else {
            dismissDialog();
        }

        isAnimatingDismiss = false;
    }

    /**
     * Adds the listener to the list of zoom listeners.
     *
     * @param onZoomListener Indicates the listener for the zoom event.
     */
    public void addOnZoomListener(OnZoomListener onZoomListener) {
        zoomListeners.add(onZoomListener);
    }

    /**
     * Removes the listener from the list of zoom listeners.
     *
     * @param onZoomListener Indicates the listener for the zoom event.
     */
    public void removeOnZoomListener(OnZoomListener onZoomListener) {
        zoomListeners.remove(onZoomListener);
    }

    /**
     * Sends the zoom event to the listeners.
     *
     * @param zoomableView Indicates the view in focus.
     * @param zoom Indicates whether the view is zoomed or not.
     */
    private void sendZoomEventToListeners(Component zoomableView, boolean zoom) {
        for (OnZoomListener onZoomListener : zoomListeners) {
            if (zoom) {
                onZoomListener.onImageZoomStarted(zoomableView);
            } else {
                onZoomListener.onImageZoomEnded(zoomableView);
            }
        }
    }

    /**
     * Dismisses the dialog.
     */
    private void dismissDialog() {
        if (dialog != null) {
            dialog.destroy();
            dialog = null;
        }

        darkView = null;
        resetOriginalViewAfterZoom();
    }

    /**
     * Resets the zoomable view.
     */
    private void resetOriginalViewAfterZoom() {
        if (zoomableView != null) {
            zoomableView.invalidate();
            zoomableView = null;
        }
    }

    /**
     * Get distance between two points.
     *
     * @param x1 x coordinate of 1st point
     * @param x2 x coordinate of 2nd point
     * @param y1 y coordinate of 1st point
     * @param y2 y coordinate of 2nd point
     * @return distance
     */
    private double getDistance(double x1, double x2, double y1, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    /**
     * Registers a listener for zoomed views.
     */
    public interface OnZoomListener {

        void onImageZoomStarted(Component view);

        void onImageZoomEnded(Component view);

    }

    /**
     * Obtains the width of the entire screen.
     *
     * @param context Indicates the context that is being displayed.
     * @return returns the width of the screen as an integer.
     */
    private int getScreenWidth(Context context) {
        DisplayManager displayManager = DisplayManager.getInstance();
        Optional<Display> optDisplay = displayManager.getDefaultDisplay(context);
        Point point = new Point(0, 0);
        if (optDisplay.isPresent()) {
            Display display = optDisplay.get();
            display.getSize(point);
        }
        return (int) point.position[0];
    }

    /**
     * Obtains the height of the entire screen.
     *
     * @param context Indicates the context that is being displayed.
     * @return returns the height of the screen as an integer.
     */
    private int getScreenHeight(Context context) {
        DisplayManager displayManager = DisplayManager.getInstance();
        Optional<Display> optDisplay = displayManager.getDefaultDisplay(context);
        Point point = new Point(0, 0);
        if (optDisplay.isPresent()) {
            Display display = optDisplay.get();
            display.getSize(point);
        }
        return (int) point.position[1];
    }
}
