package utool.plugin.singleelimination;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * The ResizeTextView automatically resizes its contents so that it fits within its bounds
 * 
 * @author hoguet
 *
 */
public class ResizeTextView extends TextView {

	/**
	 * ctor
	 * @param context
	 */
    public ResizeTextView(Context context) {
        super(context);
        initialize();
    }

    /**
     * ctor
     * @param context
     * @param attrs
     */
    public ResizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    /**
     * Initialize mTestPaint
     */
    private void initialize() {
        mTestPaint = new Paint();
        mTestPaint.set(this.getPaint());
        //max size defaults to the initially specified text size unless it is too small
    }

    /** Re size the font so the specified text fits in the text box
     * assuming the text box is the specified width.
     * @param text
     * @param textWidth
     */
    private void refitText(String text, int textWidth) 
    { 
        if (textWidth <= 0)
            return;
        int targetWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();
        float hi = 33;
        float lo = 2;
        final float threshold = 0.5f; // How close we have to be

        mTestPaint.set(this.getPaint());

        while((hi - lo) > threshold) {
            float size = (hi+lo)/2;
            mTestPaint.setTextSize(size);
            if(mTestPaint.measureText(text) >= targetWidth) 
                hi = size; // too big
            else
                lo = size; // too small
        }
        // Use lo so that we undershoot rather than overshoot
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, lo);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int height = getMeasuredHeight();
        refitText(this.getText().toString(), parentWidth);
        this.setMeasuredDimension(parentWidth, height);
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        refitText(text.toString(), this.getWidth());
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        if (w != oldw) {
            refitText(this.getText().toString(), w);
        }
    }

    /**
     * Used to measure the dimensions of text for deciding on text size
     */
    private Paint mTestPaint;
}