package com.alt.pagination;

import android.support.annotation.NonNull;
import android.text.Layout;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.util.HashMap;

public class PaginationController {

    private static final String TAG = PaginationController.class.getSimpleName();

    private final TextView mTextView;

    private int mPageIndex;
    private String mText;
    private HashMap<Integer, Boundary> mBoundaries;
    private int mLastPageIndex;

    public PaginationController(@NonNull TextView textView) {
        mTextView = textView;
        mBoundaries = new HashMap<>();
        mLastPageIndex = -1;
    }

    public void onTextLoaded(@NonNull String text, @NonNull final OnInitializedListener listener) {
        mPageIndex = 0;
        mText = text;

        if (mTextView.getLayout() == null) {
            mTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ViewTreeObserver obs = mTextView.getViewTreeObserver();
                    obs.removeOnGlobalLayoutListener(this);
                    setTextWithCaching(mPageIndex, 0);
                    listener.onInitialized();
                }
            });
        } else {
            setTextWithCaching(mPageIndex, 0);
            listener.onInitialized();
        }
    }

    /**
     * Assume, that page index can be only next or previous. For other cases
     *
     * @param pageIndex index of selected page
     */
    private void selectPage(int pageIndex) {
        Log.v(TAG, "selectPage=" + pageIndex);

        String displayedText;
        if (mBoundaries.containsKey(pageIndex)) {
            // use existing boundaries
            Boundary boundary = mBoundaries.get(pageIndex);
            displayedText = mText.substring(boundary.start, boundary.end);
            mTextView.setText(displayedText);
            Log.v(TAG, "Existing[" + pageIndex + "]: " + displayedText);
        } else if (mBoundaries.containsKey(pageIndex - 1)) {
            //calculate boundaries for new page (previous exists)
            Boundary previous = mBoundaries.get(pageIndex - 1);
            setTextWithCaching(pageIndex, previous.end);
        } else {
            Log.v(TAG, "selectPage(" + pageIndex + "), values=[" + mBoundaries.keySet());
            // TODO implement selectPage(n), n - random int
        }
    }

    private void setTextWithCaching(int pageIndex, int pageStartSymbol) {
        String restText = mText.substring(pageStartSymbol);

        mTextView.setText(restText);

        int height = mTextView.getHeight();
        int scrollY = mTextView.getScrollY();
        Layout layout = mTextView.getLayout();
        int firstVisibleLineNumber = layout.getLineForVertical(scrollY);
        int lastVisibleLineNumber = layout.getLineForVertical(height + scrollY);

        //check is latest line fully visible
        if (mTextView.getHeight() < layout.getLineBottom(lastVisibleLineNumber)) {
            lastVisibleLineNumber--;
        }

        int start = pageStartSymbol + mTextView.getLayout().getLineStart(firstVisibleLineNumber);
        int end = pageStartSymbol + mTextView.getLayout().getLineEnd(lastVisibleLineNumber);

        if (end == mText.length()) {
            mLastPageIndex = pageIndex;
        }
        String displayedText = mText.substring(start, end);
        Log.v(TAG, "Added to Cache[" + pageIndex + "](symbols={" + start + "," + end + "}): " + displayedText);

        //correct visible text
        mTextView.setText(displayedText);

        mBoundaries.put(pageIndex, new Boundary(start, end));
    }

    public boolean next() {
        throwIfNotInitialized();
        if (isNextEnabled()) {
            selectPage(++mPageIndex);
            return true;
        }
        return false;
    }

    public boolean previous() {
        throwIfNotInitialized();
        if (isPreviousEnabled()) {
            selectPage(--mPageIndex);
            return true;
        }
        return false;
    }

    public boolean isNextEnabled() {
        throwIfNotInitialized();
        return mPageIndex < mLastPageIndex || mLastPageIndex < 0;
    }

    public boolean isPreviousEnabled() {
        throwIfNotInitialized();
        return mPageIndex > 0;
    }

    void throwIfNotInitialized() {
        if (mText == null) {
            throw new IllegalStateException("Call onTextLoaded(String) first");
        }
    }

    private class Boundary {

        final int start;
        final int end;

        private Boundary(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    public interface OnInitializedListener {
        void onInitialized();
    }
}
