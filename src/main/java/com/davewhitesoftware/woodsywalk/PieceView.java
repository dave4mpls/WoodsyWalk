package com.davewhitesoftware.woodsywalk;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * PieceView: a display representation of a WoodsyWalk piece.
 */
public class PieceView extends View implements View.OnClickListener {
    private Context ctx;
    private int mPiece;      // The piece integer with its bitmap is all we need to know to draw a piece.
    private int mRow, mCol;   // used by callers when this piece is part of a table
    private OnClickListener clickListener;

    private TextPaint mTextPaint;
    private Paint mLightGreenPaint, mDarkGreenPaint, mYellowPaint, mRedPaint, mPurplePaint, mBluePaint;
    private Paint mBrownPaint, mGoldPaint, mSilverPaint, mBlackPaint, mEdgeGreenPaint;
    private float mTextWidth;
    private float mTextHeight;
    private Paint[] personHousePaints;

    public PieceView(Context context) {
        super(context);
        ctx = context;
        init(null, 0);
    }

    public PieceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
        init(attrs, 0);
    }

    public PieceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        ctx = context;
        init(attrs, defStyle);
    }

    private void preparePaint(Paint p, int c, double strokeWidth) {
        p.setColor(c);
        p.setStrokeWidth((float)strokeWidth);
        p.setAntiAlias(true);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.MITER);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.PieceView, defStyle, 0);

        mPiece = a.getInteger(R.styleable.PieceView_piece, Pieces.createBlankPiece());

        a.recycle();

        // Default row and col are zero; these are convenience properties for the parent controls,
        // so they are just referenced using setters and getters
        mRow = 0; mCol = 0;

        // clear the click listener on open; set up a link to our onClick listener
        this.clickListener = null;
        super.setOnClickListener(this);

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        // Set up color paint objects for painting parts of pieces.
        mLightGreenPaint = new Paint(); mEdgeGreenPaint = new Paint(); mDarkGreenPaint = new Paint();
        mBlackPaint = new Paint(); mBrownPaint = new Paint(); mGoldPaint = new Paint();
        mSilverPaint = new Paint(); mRedPaint = new Paint();
        mBluePaint = new Paint(); mPurplePaint = new Paint(); mYellowPaint = new Paint();
        this.preparePaint(this.mEdgeGreenPaint, Color.argb(255,164,255,164), 2);
        this.preparePaint(this.mDarkGreenPaint, Color.argb(255, 0,193,0), 2);
        this.preparePaint(this.mLightGreenPaint, Color.argb(255,200,255,200),2);
        this.preparePaint(this.mBlackPaint, Color.BLACK, 2);
        this.preparePaint(this.mBrownPaint, Color.argb(255,184,134,1),this.getWidth() * .1);
        this.preparePaint(this.mGoldPaint, Color.argb(255,255,234,118),2);
        this.preparePaint(this.mSilverPaint, Color.argb(255,189,189,189),2);
        int personLineWidth = 15;    // default -- will be changed by person drawing routine anyway
        this.preparePaint(this.mRedPaint, Color.RED, personLineWidth);
        this.preparePaint(this.mBluePaint, Color.BLUE, personLineWidth);
        this.preparePaint(this.mYellowPaint, Color.YELLOW, personLineWidth);
        this.preparePaint(this.mPurplePaint, Color.argb(255,238,140,228),personLineWidth);
        // Prepare the person house paints color array.
        this.personHousePaints = new Paint[Pieces.numberOfPeople()];
        for (int i = 0; i < Pieces.numberOfPeople(); i++) {
            if (i==0) this.personHousePaints[i] = this.mRedPaint;
            if (i==1) this.personHousePaints[i] = this.mYellowPaint;
            if (i==2) this.personHousePaints[i] = this.mBluePaint;
            if (i==3) this.personHousePaints[i] = this.mPurplePaint;
            if (i==4) this.personHousePaints[i] = this.mGoldPaint;  // right now we only have 4 pairs of houses/people.  If we get more maybe we will add more colors!
            if (i==5) this.personHousePaints[i] = this.mSilverPaint;
        }

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    private void invalidateTextPaintAndMeasurements() {
        mTextWidth = mTextPaint.measureText(this.ctx.getString(R.string.turn_complete));

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    private void drawHouse(Canvas canvas, Paint paint, int centerX, int topY, int width, int height) {
        // Draws a house figure for the piece.
        Path housePath = new Path();
        float lineWidth = (float)(this.getWidth()*0.12);
        topY += lineWidth/2 + 1;
        height -= lineWidth + 2;
        width -= lineWidth;
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(lineWidth);
        housePath.moveTo(centerX,topY);
        housePath.lineTo(centerX+width/2,topY+height/2);
        housePath.lineTo(centerX+width/2,topY+height);
        housePath.lineTo(centerX-width/2,topY+height);
        housePath.lineTo(centerX-width/2,topY+height/2);
        housePath.lineTo(centerX,topY);
        housePath.close();
        canvas.drawPath(housePath, paint);
    }

    private void drawPerson(Canvas canvas, Paint paint, int centerX, int topY, int width, int height) {
        // Draws a person figure for the piece.
        Path personPath = new Path();
        float lineWidth = (float)(width*0.12);
        topY += lineWidth/2 + 1;
        height -= lineWidth + 2;
        width -= lineWidth;
        float headRadius = (float) (height / 6.0);
        float personPartHeight = (float) (height / 3.0);
        float personLimbWidth = (float) (width / 2.0);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth);
        //--- left arm
        personPath.moveTo(centerX, topY + personPartHeight);
        personPath.lineTo(centerX-personLimbWidth, topY + personPartHeight*2);
        //--- right arm
        personPath.moveTo(centerX, topY + personPartHeight);
        personPath.lineTo(centerX+personLimbWidth, topY+personPartHeight*2);
        //--- torso
        personPath.moveTo(centerX, topY + personPartHeight);
        personPath.lineTo(centerX, topY+personPartHeight*2);
        //--- left leg
        personPath.moveTo(centerX,topY+personPartHeight*2);
        personPath.lineTo(centerX-personLimbWidth, topY+personPartHeight*3);
        //--- right leg
        personPath.moveTo(centerX, topY+personPartHeight*2);
        personPath.lineTo(centerX+personLimbWidth, topY+personPartHeight*3);
        //--- now actually fill in the path.
        canvas.drawPath(personPath, paint);
        //--- head uses fill mode
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, topY + headRadius, headRadius, paint);
    }

    public void drawRoadsAndCoins(Canvas canvas, int p, int x1, int y1, int x2, int y2) {
        //-- Draws the roads and gems on the piece.
        // do roads first
        float strokeWidth = Math.abs(x2-x1)*0.1f;
        x1 += strokeWidth/2; y1 += strokeWidth/2; x2 -= strokeWidth/2; y2 -= strokeWidth/2;
        int cx = (x2+x1)/2;
        int cy = (y2+y1)/2;
        Paint brown = this.mBrownPaint;
        brown.setStyle(Paint.Style.STROKE);
        brown.setStrokeWidth(strokeWidth);
        if (Pieces.up(p)) canvas.drawLine(cx,y1,cx,cy,brown);
        if (Pieces.down(p)) canvas.drawLine(cx,y2,cx,cy,brown);
        if (Pieces.left(p)) canvas.drawLine(x1,cy,cx,cy,brown);
        if (Pieces.right(p)) canvas.drawLine(x2,cy,cx,cy,brown);
        //
        // now do coins
        Paint silver = this.mSilverPaint;
        Paint gold = this.mGoldPaint;
        silver.setStyle(Paint.Style.FILL);
        gold.setStyle(Paint.Style.FILL);
        if (Pieces.silver(p)) canvas.drawCircle((x1+cx)/2,(y1+cy)/2,(Math.abs(cy-y1)/2)*0.65f,silver);
        if (Pieces.gold(p)) canvas.drawCircle((x2+cx)/2,(y2+cy)/2,(Math.abs(y2-cy)/2)*0.65f, gold);
    }

    public void setPiece(int p) {
        // Sets the piece to draw.  Of course setting the piece causes a redraw.
        if (this.mPiece==p) return;  // ignore if the piece is unchanged
        this.mPiece = p;
        this.invalidate();
    }

    public int getPiece() {
        return this.mPiece;
    }

    //-- The parent can set rows and columns on a piece to make it easier to know
    //-- which piece in a table was clicked.
    public void setRow(int r) { mRow = r; }
    public void setCol(int c) { mCol = c; }
    public int getRow() { return mRow; }
    public int getCol() { return mCol; }

    @Override
    public void onClick(View v) {
        //-- this passes click events to the listener that the parent/owner set
        if (clickListener != null) clickListener.onClick(v);
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        //-- you call this to set what happens when a piece gets clicked
        this.clickListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        // Draw the piece.  Start with the background fill.
        Paint bgFill;
        boolean pieceIsPerson = Pieces.isPerson(this.mPiece);
        boolean pieceIsHouse = Pieces.isHouse(this.mPiece);
        boolean pieceIsBlank = Pieces.isBlank(this.mPiece);
        boolean pieceIsGreenGrass = Pieces.isGreenGrassPiece(this.mPiece);
        if (pieceIsPerson || pieceIsHouse)
            bgFill = this.mDarkGreenPaint;
        else if (pieceIsBlank)
            bgFill = this.mLightGreenPaint;
        else if (pieceIsGreenGrass)
            bgFill = this.mEdgeGreenPaint;
        else
            bgFill = this.mDarkGreenPaint;
        bgFill.setStyle(Paint.Style.FILL);
        canvas.drawRect(paddingLeft,paddingTop,paddingLeft+contentWidth,paddingTop+contentHeight,bgFill);
        // Now, add the black border.
        this.mBlackPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(paddingLeft,paddingTop,paddingLeft+contentWidth,paddingTop+contentHeight,this.mBlackPaint);
        // Now, draw the roads.  And the coins on the roads.
        this.drawRoadsAndCoins(canvas, this.mPiece, paddingLeft+2,paddingTop+2,paddingLeft+contentWidth-2,paddingTop+contentHeight-2);
        // Now, if there is a person or house on this piece, draw it.  If this is JUST a person or house
        // piece, draw it centered.  Make sure to draw it the right color.
        Paint phPaint;
        if (Pieces.isPerson(this.mPiece)) {
            phPaint = this.personHousePaints[Pieces.personNumber(this.mPiece)-1];
            this.drawPerson(canvas,phPaint,paddingLeft+contentWidth/2,2+paddingTop,((int)(contentWidth*0.4)),contentHeight-4);
        }
        else if (Pieces.isHouse(this.mPiece)) {
            phPaint = this.personHousePaints[Pieces.houseNumber(this.mPiece)-1];
            this.drawHouse(canvas,phPaint,paddingLeft+contentWidth/2,2+paddingTop,((int)(contentWidth*0.4)),contentHeight-4);
        }
        else {
            if (Pieces.personNumber(this.mPiece) > 0) {
                phPaint = this.personHousePaints[Pieces.personNumber(this.mPiece)-1];
                this.drawPerson(canvas,phPaint,paddingLeft+contentWidth/4,2+paddingTop,((int)(contentWidth*0.4)),contentHeight-4);
            }
            if (Pieces.houseNumber(this.mPiece) > 0) {
                phPaint = this.personHousePaints[Pieces.houseNumber(this.mPiece)-1];
                this.drawHouse(canvas,phPaint,paddingLeft+(contentWidth*3)/4,2+paddingTop,((int)(contentWidth*0.4)),contentHeight-4);
            }
        }


/*
        // Draw the text.
        canvas.drawText(mExampleString,
                paddingLeft + (contentWidth - mTextWidth) / 2,
                paddingTop + (contentHeight + mTextHeight) / 2,
                mTextPaint);

        // Draw the example drawable on top of the text.
        if (mExampleDrawable != null) {
            mExampleDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mExampleDrawable.draw(canvas);
        }
*/
    }
}
