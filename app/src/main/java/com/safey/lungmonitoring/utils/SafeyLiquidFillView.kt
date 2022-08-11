package com.safey.lungmonitoring.utils

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.utils.WaveViewLoading.ShapeType
import com.safey.lungmonitoring.utils.WaveViewLoading.TriangleDirection
import info.safey.safey_sdk.SafeyInterface
import java.lang.ref.WeakReference
import java.util.*

class SafeyLiquidFillView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {
    /*Type constant*/
    enum class Shape(var value: Int) {
        CIRCLE(1), SQUARE(2), HEART(3), STAR(4), USHAPE(5);

        companion object {
            fun fromValue(value: Int): Shape {
                for (shape in values()) {
                    if (shape.value == value) return shape
                }
                return USHAPE
            }
        }
    }

    /*Displacement Animator*/
    private var shiftX1 = 0f
    private var waveVector = 25f //-0.25f
    private var waveOffset = 10 // 25
    private var speed = 70
    private val thread: HandlerThread? = HandlerThread("SafeyLiquidFillView_" + hashCode())
    private val animHandler: Handler?
    private val uiHandler: Handler

    /*brush*/
    private val mBorderPaint = Paint() //Sideline Paint
    private val mViewPaint = Paint() //Water level Paint
    private var mWavePaint1 = Paint() //After wave coloring
    private var mWavePaint2 = Paint() //Front wave coloring
    private var mPathContent: Path? = null
    private var mPathBorder: Path? = null

    /*Parameter value*/
    private var mShapePadding = DEFAULT_PADDING //Shrink
    private var mProgress = DEFAULT_PROGRESS //Water level
    private var mMax = DEFAULT_MAX //Maximum water level
    private var mFrontWaveColor = DEFAULT_FRONT_WAVE_COLOR //Front wave color
    private var mBehindWaveColor = DEFAULT_BEHIND_WAVE_COLOR //back water color
    private var mBorderColor = DEFAULT_BORDER_COLOR //Edge color
    private var mBorderWidth = DEFAULT_BORDER_WIDTH //Edge width
    private var mTextColor = DEFAULT_TEXT_COLOR //font color
    private var isAnimation = DEFAULT_ENABLE_ANIMATION
    private var isHideText = DEFAULT_HIDE_TEXT
    private var mStrong = DEFAULT_STRONG //crest
    private var mSpikes = DEFAULT_SPIKE_COUNT
    private var mShape = Shape.USHAPE
    private var mListener: SafeyInterface? = null
    private var screenSize = Point(0, 0)

    // Init Top Title
    private var mTopTitle: String = ""
    private var mCenterTitle: String = ""
    var mBottomTitle: String = "Keep Title"
    // Point to draw title.
    private var mTopTitlePaint: Paint
    private var mBottomTitlePaint: TextPaint
    private var mCenterTitlePaint: TextPaint

    private val mTopTitleStrokePaint: Paint
    private var mBottomTitleStrokePaint: Paint

    // Init Top Title

    /**
     * Set the water level
     * 0-MAX
     */
    var progress: Int
        get() = mProgress
        set(progress) {
            if (progress <= mMax) {
//                if (mListener != null) {
//                    mListener!!.onProgressChange(progress)
//                }
                mProgress = progress
                createShader()
                val message = Message.obtain(uiHandler)
                message.sendToTarget()
            }
        }

    fun startAnimation() {
        isAnimation = true
        if (width > 0 && height > 0) {
            animHandler!!.removeCallbacksAndMessages(null)
            animHandler.post(object : Runnable {
                override fun run() {
                    shiftX1 += waveVector //Displacement
                    createShader()
                    val message = Message.obtain(uiHandler)
                    message.sendToTarget()
                    if (isAnimation) {
                        animHandler.postDelayed(this, speed.toLong())
                    }
                }
            })
        }
    }

    fun stopAnimation() {
        isAnimation = false
    }

    var listener: SafeyInterface?
        get() = mListener
        set(mListener) {
            this.mListener = mListener
        }

    /**
     * Set maximum
     */
    var max: Int
        get() = mMax
        set(max) {
            if (mMax != max) {
                if (max >= mProgress) {
                    mMax = max
                    createShader()
                    val message = Message.obtain(uiHandler)
                    message.sendToTarget()
                }
            }
        }

    /**
     * Set edge color
     */
    fun setBorderColor(color: Int) {
        mBorderColor = color
        mBorderPaint.color = mBorderColor
        createShader()
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }

    /**
     * Set front wave color
     */
    fun setFrontWaveColor(color: Int) {
        mFrontWaveColor = color
        mWavePaint2.color = mFrontWaveColor
        createShader()
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }

    /**
     * Setting the back wave color
     */
    fun setBehindWaveColor(color: Int) {
        mBehindWaveColor = color
        mWavePaint1.color = mBehindWaveColor
        createShader()
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }

    /**
     * Set text color
     */
    fun setTextColor(color: Int) {
        mTextColor = color
        createShader()
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }

    /**
     * Set edge width
     */
    fun setBorderWidth(width: Float) {
        mBorderWidth = width
        mBorderPaint.strokeWidth = mBorderWidth
        resetShapes()
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }

    /**
     * Setting reduction
     */
    fun setShapePadding(padding: Float) {
        mShapePadding = padding
        resetShapes()
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }

    /**
     * Set animation speed
     * Fast -> Slow
     * 0...∞
     */
    fun setAnimationSpeed(speed: Int) {
        require(speed >= 0) { "The speed must be greater than 0." }
        this.speed = speed
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }

    /**
     * Set how much the water wave shifts before and after each refresh
     * 0-100
     */
    fun setWaveVector(offset: Float) {
        require(!(offset < 0 || offset > 150)) { "The vector of wave must be between 0 and 100." }
        waveVector = (offset - 50f) / 50f
        createShader()
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }

    /**
     * Set whether the font is hidden
     *
     * @param hidden hide
     */
    fun setHideText(hidden: Boolean) {
        isHideText = hidden
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }

    /**
     * Set the number of corners of the star
     * 3...∞
     *
     * @param count Corners
     */
    fun setStarSpikes(count: Int) {
        require(count >= 3) { "The number of spikes must be greater than 3." }
        mSpikes = count
        if (Math.min(screenSize.x, screenSize.y) != 0) {
            /*===Star path===*/
            resetShapes()
        }
    }

    /**
     * Set the water wave phase difference before and after
     * 1-100
     */
    fun setWaveOffset(offset: Int) {
        waveOffset = offset
        createShader()
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }

    /**
     * Set crest
     * 0-100
     */
    fun setWaveStrong(strong: Int) {
        mStrong = strong
        createShader()
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }

    fun setShape(shape: Shape) {
        mShape = shape
        resetShapes()
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenSize = Point(w, h)
        resetShapes()
        if (isAnimation) {
            startAnimation()
        }
    }

    fun setTopTitle(topTitle: String) {
        mTopTitle = topTitle
    }

    fun getTopTitle(): String? {
        return mTopTitle
    }

    fun setCenterTitle(centerTitle: String) {
        mCenterTitle = centerTitle
    }

    fun getCenterTitle(): String? {
        return mCenterTitle
    }

    fun setBottomTitle(bottomTitle: String) {
        mBottomTitle = bottomTitle
    }

    fun getBottomTitle(): String? {
        return mBottomTitle
    }

    fun setTopTitleColor(topTitleColor: Int) {
        mTopTitlePaint.color = topTitleColor
    }

    fun getTopTitleColor(): Int {
        return mTopTitlePaint.color
    }

    fun setCenterTitleColor(centerTitleColor: Int) {
        mCenterTitlePaint.color = centerTitleColor
    }

    fun getCenterTitleColor(): Int {
        return mCenterTitlePaint.color
    }

    fun setBottomTitleColor(bottomTitleColor: Int) {
        mBottomTitlePaint.color = bottomTitleColor
    }

    fun getBottomTitleColor(): Int {
        return mBottomTitlePaint.color
    }

    fun setTopTitleSize(topTitleSize: Float) {
        mTopTitlePaint.textSize = sp2px(topTitleSize).toFloat()
    }

    fun getsetTopTitleSize(): Float {
        return mTopTitlePaint.textSize
    }

    fun setCenterTitleSize(centerTitleSize: Float) {
        mCenterTitlePaint.textSize = sp2px(centerTitleSize).toFloat()
    }

    fun getCenterTitleSize(): Float {
        return mCenterTitlePaint.textSize
    }

    fun setBottomTitleSize(bottomTitleSize: Float) {
        mBottomTitlePaint.textSize = sp2px(bottomTitleSize).toFloat()
    }

    fun getBottomTitleSize(): Float {
        return mBottomTitlePaint.textSize
    }

    fun setTopTitleStrokeWidth(topTitleStrokeWidth: Float) {
        mTopTitleStrokePaint.strokeWidth = dp2px(topTitleStrokeWidth).toFloat()
    }

    fun setTopTitleStrokeColor(topTitleStrokeColor: Int) {
        mTopTitleStrokePaint.color = topTitleStrokeColor
    }

    fun setBottomTitleStrokeWidth(bottomTitleStrokeWidth: Float) {
        mBottomTitleStrokePaint.strokeWidth = dp2px(bottomTitleStrokeWidth).toFloat()
    }

    fun setBottomTitleStrokeColor(bottomTitleStrokeColor: Int) {
        mBottomTitleStrokePaint.color = bottomTitleStrokeColor
    }



    private fun resetShapes() {
        val radius = Math.min(screenSize.x, screenSize.y)
        val cx = (screenSize.x - radius) / 2
        val cy = (screenSize.y - radius) / 2
        when (mShape) {
            Shape.STAR -> {
                /*===Star path===*/mPathBorder = drawStart(
                    radius / 2 + cx,
                    radius / 2 + cy + mBorderWidth.toInt(),
                    mSpikes,
                    radius / 2 - mBorderWidth.toInt(),
                    radius / 4
                )
                mPathContent = drawStart(
                    radius / 2 + cx,
                    radius / 2 + cy + mBorderWidth.toInt(),
                    mSpikes,
                    radius / 2 - mBorderWidth.toInt() - mShapePadding.toInt(),
                    radius / 4 - mShapePadding.toInt()
                )
            }
            Shape.HEART -> {
                /*===Love path===*/mPathBorder = drawHeart(cx, cy, radius)
                mPathContent = drawHeart(
                    cx + mShapePadding.toInt() / 2,
                    cy + mShapePadding.toInt() / 2,
                    radius - mShapePadding.toInt()
                )
            }
            Shape.CIRCLE -> {
                /*===Circular path===*/mPathBorder = drawCircle(cx, cy, radius)
                mPathContent = drawCircle(
                    cx + mShapePadding.toInt() / 2,
                    cy + mShapePadding.toInt() / 2,
                    radius - mShapePadding.toInt()
                )
            }
            Shape.SQUARE -> {
                /*===Square path===*/mPathBorder = drawSquare(cx, cy, radius)
                mPathContent = drawSquare(
                    cx + mShapePadding.toInt() / 2,
                    cy + mShapePadding.toInt() / 2,
                    radius - mShapePadding.toInt()
                )
            }
            Shape.USHAPE -> {
                mPathBorder = drawUShape(cx, cy, radius)
                mPathContent = drawUShape(
                    cx + mShapePadding.toInt() / 2,
                    cy + mShapePadding.toInt() / 2,
                    radius - mShapePadding.toInt()
                )
            }
        }
        createShader()
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }

    private fun drawSquare(cx: Int, cy: Int, radius: Int): Path {
        val path = Path()
        path.moveTo(cx.toFloat(), cy + mBorderWidth / 2)
        path.lineTo(cx.toFloat(), radius + cy - mBorderWidth)
        path.lineTo(radius + cx.toFloat(), radius + cy - mBorderWidth)
        path.lineTo(radius + cx.toFloat(), cy + mBorderWidth)
        path.lineTo(cx.toFloat(), cy + mBorderWidth)
        path.close()
        return path
    }

    private fun drawCircle(cx: Int, cy: Int, radius: Int): Path {
        val path = Path()
        path.addCircle(
            radius / 2 + cx.toFloat(),
            radius / 2 + cy.toFloat(),
            radius / 2 - mBorderWidth,
            Path.Direction.CCW
        )
        path.close()
        return path
    }

    private fun drawUShape(cx: Int, cy: Int, radius: Int): Path {



        val path = Path()
        val drawingRect = Rect(
            0, 0,
            width, height
        )
        val topLeftArcBound = RectF()
        val topRightArcBound = RectF()
        val bottomLeftArcBound = RectF()
        val bottomRightArcBound = RectF()

        val topRightRadius = 0
        val topLeftRadius = 0
        val bottomLeftRadius = width / 2
        topRightArcBound[0f, 0f, drawingRect.right.toFloat()] =
            (drawingRect.top + topRightRadius * 2).toFloat()
        val bottomRightRadius = width / 2
        bottomRightArcBound[(drawingRect.right - bottomRightRadius * 2).toFloat(), (drawingRect.bottom - bottomRightRadius * 2).toFloat(), drawingRect.right.toFloat()] =
            drawingRect.bottom.toFloat()
        bottomLeftArcBound[drawingRect.left.toFloat(), (drawingRect.bottom - bottomLeftRadius * 2).toFloat(), (drawingRect.left + bottomLeftRadius * 2).toFloat()] =
            drawingRect.bottom.toFloat()
        topLeftArcBound[drawingRect.left.toFloat(), drawingRect.top.toFloat(), (drawingRect.left + topLeftRadius * 2).toFloat()] =
            (drawingRect.top + topLeftRadius * 2).toFloat()

        path.reset()

        path.moveTo((drawingRect.left + topLeftRadius).toFloat(), drawingRect.top.toFloat())

//draw top horizontal line

//draw top horizontal line
        path.lineTo((drawingRect.right - topRightRadius).toFloat(), drawingRect.top.toFloat())

//draw top-right corner

//draw top-right corner
        path.arcTo(topRightArcBound, -90f, 90f)

//draw right vertical line

//draw right vertical line
        path.lineTo(drawingRect.right.toFloat(), (drawingRect.bottom - bottomRightRadius).toFloat())

//draw bottom-right corner

//draw bottom-right corner
        path.arcTo(bottomRightArcBound, 0f, 90f)

//draw bottom horizontal line

//draw bottom horizontal line
       // path.lineTo((drawingRect.left - bottomLeftRadius).toFloat(), drawingRect.bottom.toFloat())

//draw bottom-left corner

//draw bottom-left corner
        path.arcTo(bottomLeftArcBound, 90f, 90f)

//draw left vertical line

//draw left vertical line
        path.lineTo(drawingRect.left.toFloat(), (drawingRect.top + topLeftRadius).toFloat())

//draw top-left corner

//draw top-left corner
        path.arcTo(topLeftArcBound, 180f, 90f)

        path.close()
        return path
    }

    private fun drawHeart(cx: Int, cy: Int, radius: Int): Path {
        val path = Path()
        /*From this point*/path.moveTo(radius / 2 + cx.toFloat(), radius / 5 + cy.toFloat())
        /*Left ascending line*/path.cubicTo(
            5 * radius / 14 + cx.toFloat(),
            cy.toFloat(),
            cx.toFloat(),
            radius / 15 + cy.toFloat(),
            radius / 28 + cx.toFloat(),
            2 * radius / 5 + cy.toFloat()
        )
        /*Left descending line*/path.cubicTo(
            radius / 14 + cx.toFloat(),
            2 * radius / 3 + cy.toFloat(),
            3 * radius / 7 + cx.toFloat(),
            5 * radius / 6 + cy.toFloat(),
            radius / 2 + cx.toFloat(),
            9 * radius / 10 + cy.toFloat()
        )
        /*Right descending line*/path.cubicTo(
            4 * radius / 7 + cx.toFloat(),
            5 * radius / 6 + cy.toFloat(),
            13 * radius / 14 + cx.toFloat(),
            2 * radius / 3 + cy.toFloat(),
            27 * radius / 28 + cx.toFloat(),
            2 * radius / 5 + cy.toFloat()
        )
        /*Right ascent*/path.cubicTo(
            radius + cx.toFloat(),
            radius / 15 + cy.toFloat(),
            9 * radius / 14 + cx.toFloat(),
            cy.toFloat(),
            radius / 2 + cx.toFloat(),
            radius / 5 + cy.toFloat()
        )
        path.close()
        return path
    }

    /**
     * Right ascent
     *
     * @param cx          X
     * @param cy          Y
     * @param spikes      Number of stars
     * @param outerRadius Outer circle radius
     * @param innerRadius Inner circle radius
     * @return path
     */
    private fun drawStart(cx: Int, cy: Int, spikes: Int, outerRadius: Int, innerRadius: Int): Path {
        val path = Path()
        var rot = Math.PI / 2.0 * 3.0
        val step = Math.PI / spikes
        path.moveTo(cx.toFloat(), cy - outerRadius.toFloat())
        for (i in 0 until spikes) {
            path.lineTo(
                cx + Math.cos(rot).toFloat() * outerRadius, cy + Math.sin(rot)
                    .toFloat() * outerRadius
            )
            rot += step
            path.lineTo(
                cx + Math.cos(rot).toFloat() * innerRadius, cy + Math.sin(rot)
                    .toFloat() * innerRadius
            )
            rot += step
        }
        path.lineTo(cx.toFloat(), cy - outerRadius.toFloat())
        path.close()
        return path
    }

    /* * Create a fill shader
      * y = Asin (ωx + φ) + h wave formula (sine function) y = waveLevel * Math.sin (w * x1 + shiftX) + level
      * φ (initial phase x): Waveform X-axis offset $ shiftX
      * ω (angular frequency): minimum positive period T = 2π / | ω | $ w
      * A (amplitude): the size of the hump $ waveLevel
      * h (initial phase y): Y-axis offset of wave shape $ level
      * <p>
      * Second order Bézier curve
      * B (t) = X (1-t) ^ 2 + 2t (1-t) Y + Zt ^ 2, 0 <= t <= n*/
    private fun createShader() {
        if (screenSize.x <= 0 || screenSize.y <= 0) {
            return
        }
        val viewSize = Math.min(screenSize.x, screenSize.y)
        val w = 2.0f * Math.PI / viewSize

        /*Create canvas
         */
        val bitmap = Bitmap.createBitmap(viewSize, viewSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val level =
            (mMax - mProgress).toFloat() / mMax.toFloat() * viewSize + (screenSize.y / 2 - viewSize / 2) //水位的高度
        val x2 = viewSize + 1 //width
        val y2 = viewSize + 1 //height
        val zzz = viewSize.toFloat() * ((waveOffset - 50) / 150f) / (viewSize.toFloat() / 6.25f)
        val shiftX2 = shiftX1 + zzz //Phase difference
        val waveLevel = mStrong * (viewSize / 20) / 150 // viewSize / 20
        for (x1 in 0 until x2) {
            /*Establishing a post wave (successive coverage)*/
            var y1 = (waveLevel * Math.sin(w * x1 + shiftX1) + level).toFloat()
            canvas.drawLine(x1.toFloat(), y1, x1.toFloat(), y2.toFloat(), mWavePaint1)
            /*Establish a front wave*/y1 =
                (waveLevel * Math.sin(w * x1 + shiftX2) + level).toFloat()
            canvas.drawLine(x1.toFloat(), y1, x1.toFloat(), y2.toFloat(), mWavePaint2)
        }
        mViewPaint.shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP)
    }

    override fun onDetachedFromWindow() {
        animHandler?.removeCallbacksAndMessages(null)
        thread?.quit()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(mPathContent!!, mViewPaint)
        /*Draw border*/if (mBorderWidth > 0) {
            canvas.drawPath(mPathBorder!!, mBorderPaint)
        }
        //if (!isHideText) {
            /*Create percentage text*/
            val percent = mProgress * 150 / mMax.toFloat()
           // var text = ""
        if(percent>0)
            mCenterTitle = String.format(Locale.ENGLISH, "%.1f", percent) + "%"


      /*  mCenterTitlePaint.color = mTextColor
        if (mShape == Shape.STAR) {
            mCenterTitlePaint.textSize = Math.min(screenSize.x, screenSize.y) / 2f / 3
        } else {
            mCenterTitlePaint.textSize = Math.min(screenSize.x, screenSize.y) / 2f / 2
        }
        mCenterTitlePaint.isAntiAlias = true*/
       // val textPaint = TextPaint()


        var textHeight = mCenterTitlePaint.descent() + mCenterTitlePaint.ascent()
        canvas.drawText(
            mCenterTitle,
            (screenSize.x - mCenterTitlePaint.measureText(mCenterTitle)) / 2.0f,
            (screenSize.y - textHeight) / 2.0f,
            mCenterTitlePaint
        )

         textHeight = mBottomTitlePaint.descent() + mCenterTitlePaint.ascent()
            canvas.drawText(
                mBottomTitle,
                screenSize.x  / 2.0f,
                (screenSize.y - mCenterTitlePaint.descent() - mCenterTitlePaint.ascent() - textHeight - textHeight) / 2.0f,
                mBottomTitlePaint
            )

      //  }
        // I know, the code written here is very shit.

        // I know, the code written here is very shit.
       /* if (!TextUtils.isEmpty(mTopTitle)) {
            val top: Float = mTopTitlePaint.measureText(mTopTitle)
            // Draw the stroke of top text
            canvas.drawText(
                mTopTitle, (width - top) / 2,
                height * 2 / 10.0f, mTopTitleStrokePaint
            )
            // Draw the top text
            canvas.drawText(
                mTopTitle, (width - top) / 2,
                height * 2 / 10.0f, mTopTitlePaint
            )
        }

        if (!TextUtils.isEmpty(mCenterTitle)) {
            val middle: Float = mCenterTitlePaint.measureText(mCenterTitle)
            // Draw the stroke of centered text
            //canvas.drawText(mCenterTitle, (getWidth() ) / 2,
            //      getHeight() / 2 - ((mCenterTitleStrokePaint.descent() + mCenterTitleStrokePaint.ascent()) / 2), mCenterTitleStrokePaint);
            // Draw the centered text
            canvas.drawText(
                mCenterTitle,
                (width / 2).toFloat(),
                height / 2 - (mCenterTitlePaint.descent() + mCenterTitlePaint.ascent()) / 2,
                mCenterTitlePaint
            )


            *//* float bottom = mBottomTitlePaint.measureText(mBottomTitle);
                // Draw the stroke of bottom text
                //canvas.drawText(mBottomTitle, (getWidth() - bottom) / 2,
                        //((getHeight()/2) + 100)  - ((mBottomTitleStrokePaint.descent() + mBottomTitleStrokePaint.ascent()) / 2), mBottomTitleStrokePaint);
                // Draw the bottom text
                canvas.drawText(mBottomTitle, (getWidth() ) / 2,
                        ((getHeight()/2)+100)  - ((mBottomTitlePaint.descent() + mBottomTitlePaint.ascent()) / 2), mBottomTitlePaint);
*//*
        }
*/

    }

    private class UIHandler internal constructor(view: WeakReference<View?>) :
        Handler(Looper.getMainLooper()) {
        private val mView: View?
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            mView?.invalidate()
        }

        init {
            mView = view.get()
        }
    }

    companion object {
        /*Initial constant*/
        private const val DEFAULT_PROGRESS = 0
        private const val DEFAULT_MAX = 100
        private const val DEFAULT_STRONG = 70  //50
        val DEFAULT_BEHIND_WAVE_COLOR = Color.parseColor("#443030d5")
        val DEFAULT_FRONT_WAVE_COLOR = Color.parseColor("#000000")
        val DEFAULT_BORDER_COLOR = Color.parseColor("#000000")
        private const val DEFAULT_BORDER_WIDTH = 5f
        val DEFAULT_TEXT_COLOR = Color.parseColor("#000000")
        private const val DEFAULT_ENABLE_ANIMATION = true
        private const val DEFAULT_HIDE_TEXT = false
        private const val DEFAULT_SPIKE_COUNT = 5
        private const val DEFAULT_PADDING = 0f //-2f
        private val DEFAULT_TITLE_COLOR = Color.parseColor("#000000")
        private const val DEFAULT_STROKE_COLOR = Color.TRANSPARENT

        private const val DEFAULT_TITLE_STROKE_WIDTH = 0f

        // This is incorrect/not recommended by Joshua Bloch in his book Effective Java (2nd ed).
        private val DEFAULT_WAVE_SHAPE = ShapeType.ROUNDRECT.ordinal
        private val DEFAULT_TRIANGLE_DIRECTION = TriangleDirection.NORTH.ordinal
        private const val DEFAULT_ROUND_RECTANGLE_X_AND_Y = 30
        private const val DEFAULT_TITLE_TOP_SIZE = 18.0f
        private const val DEFAULT_TITLE_CENTER_SIZE = 50.0f
        private const val DEFAULT_TITLE_BOTTOM_SIZE = 24.0f


    }

    private fun sp2px(spValue: Float): Int {
        val fontScale: Float = context.getResources().getDisplayMetrics().scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    private fun dp2px(dp: Float): Int {
        val scale: Float = context.getResources().getDisplayMetrics().density
        return (dp * scale + 0.5f).toInt()
    }
    init {
        /*Get xml parameters*/
        val attributes =
            context.theme.obtainStyledAttributes(attrs, R.styleable.SafeyLiquidFillView, defStyleAttr, 0)

        /*Set xml parameters*/mFrontWaveColor =
            attributes.getColor(R.styleable.SafeyLiquidFillView_frontColor, DEFAULT_FRONT_WAVE_COLOR)
        mBehindWaveColor =
            attributes.getColor(R.styleable.SafeyLiquidFillView_behideColor, DEFAULT_BEHIND_WAVE_COLOR)
        mBorderColor =
            attributes.getColor(R.styleable.SafeyLiquidFillView_borderColor, DEFAULT_BORDER_COLOR)
        mTextColor = attributes.getColor(R.styleable.SafeyLiquidFillView_textColor, DEFAULT_TEXT_COLOR)
        mProgress = attributes.getInt(R.styleable.SafeyLiquidFillView_progress, DEFAULT_PROGRESS)
        mMax = attributes.getInt(R.styleable.SafeyLiquidFillView_max, DEFAULT_MAX)
        mBorderWidth = attributes.getDimension(
            R.styleable.SafeyLiquidFillView_borderWidthSize,
            DEFAULT_BORDER_WIDTH
        )
        mStrong = attributes.getInt(R.styleable.SafeyLiquidFillView_strong, DEFAULT_STRONG)
        mShape = Shape.fromValue(attributes.getInt(R.styleable.SafeyLiquidFillView_shapeTypeSafey, 1))
        mShapePadding =
            attributes.getDimension(R.styleable.SafeyLiquidFillView_shapePadding, DEFAULT_PADDING)
        isAnimation = attributes.getBoolean(
            R.styleable.SafeyLiquidFillView_animatorEnable,
            DEFAULT_ENABLE_ANIMATION
        )
        isHideText = attributes.getBoolean(R.styleable.SafeyLiquidFillView_textHidden, DEFAULT_HIDE_TEXT)

        /*Set Anti-Aliasing & Set to "Line"*/mBorderPaint.isAntiAlias = true
        mBorderPaint.style = Paint.Style.STROKE
        mBorderPaint.strokeWidth = mBorderWidth
        mBorderPaint.color = mBorderColor

        /*Building the shader*/mWavePaint1 = Paint()
        mWavePaint1.strokeWidth = 2f
        mWavePaint1.isAntiAlias = true
        mWavePaint1.color = mBehindWaveColor
        mWavePaint2 = Paint()
        mWavePaint2.strokeWidth = 2f
        mWavePaint2.isAntiAlias = true
        mWavePaint2.color = mFrontWaveColor


        mTopTitlePaint = Paint()
        mTopTitlePaint.color =
            attributes.getColor(R.styleable.SafeyLiquidFillView_titleTopColor, DEFAULT_TITLE_COLOR)
        mTopTitlePaint.style = Paint.Style.FILL
        mTopTitlePaint.isAntiAlias = true
        mTopTitlePaint.textSize =
            attributes.getDimension(R.styleable.SafeyLiquidFillView_titleTopSize, sp2px(DEFAULT_TITLE_TOP_SIZE).toFloat())

        mTopTitleStrokePaint = Paint()
        mTopTitleStrokePaint.color =
            attributes.getColor(R.styleable.SafeyLiquidFillView_titleTopStrokeColor, DEFAULT_STROKE_COLOR)
        mTopTitleStrokePaint.strokeWidth =
            attributes.getDimension(R.styleable.SafeyLiquidFillView_titleTopStrokeWidth, dp2px(DEFAULT_TITLE_STROKE_WIDTH).toFloat())
        mTopTitleStrokePaint.style = Paint.Style.STROKE
        mTopTitleStrokePaint.isAntiAlias = true
        mTopTitleStrokePaint.textSize = mTopTitlePaint.getTextSize()

        mTopTitle = attributes.getString(R.styleable.SafeyLiquidFillView_titleTop).toString()

        // Init Center Title
        val tf = Typeface.createFromAsset(
            context.assets,
            "fonts/circularstd_medium.otf")
        // Init Center Title
        mCenterTitlePaint = TextPaint()
        mCenterTitlePaint.color =
            attributes.getColor(R.styleable.SafeyLiquidFillView_titleCenterColor, DEFAULT_TITLE_COLOR)


       mCenterTitlePaint.typeface = tf
        mCenterTitlePaint.textSize =
            attributes.getDimension(R.styleable.SafeyLiquidFillView_titleCenterSize, sp2px(DEFAULT_TITLE_CENTER_SIZE).toFloat())

        mCenterTitle = attributes.getString(R.styleable.SafeyLiquidFillView_titleCenter).toString()

        // Init Bottom Title

        // Init Bottom Title
        mBottomTitlePaint = TextPaint()
        mBottomTitlePaint.color = attributes.getColor(R.styleable.SafeyLiquidFillView_titleBottomColor, DEFAULT_TITLE_COLOR)
        mBottomTitlePaint.textAlign = Paint.Align.CENTER

        mBottomTitlePaint.typeface = tf

        mBottomTitlePaint.textSize =
            attributes.getDimension(R.styleable.SafeyLiquidFillView_titleBottomSize, sp2px(DEFAULT_TITLE_BOTTOM_SIZE).toFloat())

        mBottomTitleStrokePaint = Paint()
        mBottomTitleStrokePaint.color = attributes.getColor(R.styleable.SafeyLiquidFillView_titleBottomStrokeColor, DEFAULT_STROKE_COLOR)
        mBottomTitleStrokePaint.strokeWidth =
            attributes.getDimension(R.styleable.SafeyLiquidFillView_titleBottomStrokeWidth, dp2px(DEFAULT_TITLE_STROKE_WIDTH).toFloat())
        mBottomTitleStrokePaint.style = Paint.Style.STROKE
        mBottomTitleStrokePaint.isAntiAlias = true

        mBottomTitleStrokePaint.textSize = mBottomTitlePaint.getTextSize()

        mBottomTitle =  attributes.getString(R.styleable.SafeyLiquidFillView_titleBottom).toString()


        attributes.recycle()



        /*Open Animation Thread*/thread!!.start()
        animHandler = Handler(thread.looper)
        uiHandler = UIHandler(WeakReference(this))
        screenSize = Point(width, height)
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }
}