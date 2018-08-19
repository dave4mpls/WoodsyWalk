package com.davewhitesoftware.woodsywalk;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;

//
//  A view that displays an entire game board.
//

public class WoodsyBoardView extends TableLayout {
    private WoodsyBoardData board = new WoodsyBoardData();
    private Context ctx;
    private boolean testMode = false;
    private boolean readOnlyMode = false;

    //--- Interfaces and properties for adding OnBoardCellClicked event with table coordinaes
    //
    public interface OnBoardCellClickedListener {
        public void onBoardCellClicked(WoodsyBoardView w, int col, int row);
    }
    private OnBoardCellClickedListener cellClickedListener = null;

    public WoodsyBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.ctx = context;
        init(attrs,0);
    }
    public WoodsyBoardView(Context context) {
        super(context);
        this.ctx = context;
        init(null,0);
    }
    private void setupTestBoard() {
        //-- for testing, draws a variety of pieces around the board so we can see
        //-- how well the pieces draw themselves.
        int thisrow = 1;
        int thiscol = 1;
        int[] pieceList = Pieces.pieces();
        for (int thisPiece: pieceList) {
            this.board.setCell(thiscol, thisrow, thisPiece);
            thiscol++;
            if (thiscol >= this.board.getWidth() - 1) { thiscol = 1; thisrow++; }
        }
        for (int i = 0; i < 4; i++) {
            int currentPiece = this.board.getCell(i+1,0);
            currentPiece = Pieces.setPersonNumber(currentPiece,i+1);
            this.board.setCell(i+1,0,currentPiece);
            currentPiece = this.board.getCell(i+1,this.board.getHeight()-1);
            currentPiece = Pieces.setHouseNumber(currentPiece,i+1);
            this.board.setCell(i+1,this.board.getHeight()-1,currentPiece);
        }
        int currentPiece = this.board.getCell(1,0);
        currentPiece = Pieces.setHouseNumber(currentPiece, 1);
        this.board.setCell(1,0, currentPiece);
        this.board.setCell(0,0, Pieces.createPersonPiece(1));
        this.board.setCell(0,1,Pieces.createHousePiece(2));
    }
    private void setupTable() {
        //-- sets up the rows and columns in a board view.
        final WoodsyBoardView myBoard = this;

        if (this.testMode) this.setupTestBoard();
        this.removeAllViews();
        for (int i = 0; i < this.board.getHeight(); i++) {
            TableRow tr = new TableRow(this.ctx);
            for (int j = 0; j < this.board.getWidth(); j++) {
                PieceView pv = new PieceView(this.ctx);
                TableRow.LayoutParams pvlp = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
                pv.setPiece(this.board.getCell(j,i));
                pv.setCol(j); pv.setRow(i);
                pv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //-- when an individual cell is clicked, we bubble it up through the OnBoardCellClicked mechanism.
                        PieceView p = (PieceView) v;
                        myBoard.fireOnBoardCellClicked(p.getCol(), p.getRow());
                    }
                });
                tr.addView(pv,pvlp);
            }
            TableLayout.LayoutParams trlp = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
            this.addView(tr,trlp);
        }
        this.invalidate();
    }

    public void redrawBoard() {
        this.setupTable();
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.WoodsyBoardView, defStyle, 0);

        this.testMode = a.getBoolean(R.styleable.WoodsyBoardView_testMode, false);
        this.readOnlyMode = a.getBoolean(R.styleable.WoodsyBoardView_readOnlyMode, false);

        a.recycle();

        cellClickedListener = null;
        setupTable();

    }

    @Override
    //-- We override onMeasure to make the board always square, at its minimum dimension.
    //-- Thanks to: https://blog.jayway.com/2012/12/12/creating-custom-android-views-part-4-measuring-and-how-to-force-a-view-to-be-square/
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = 0;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        if (width > height) {
            size = height;
        } else {
            size = width;
        }
        setMeasuredDimension(size, size);
        int mw = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        int mh = MeasureSpec.makeMeasureSpec(size/this.board.getHeight(), MeasureSpec.EXACTLY);
        this.measureChildren(mw,mh);
    }

    public void setReadOnlyMode(boolean m) {
        this.readOnlyMode = m;
    }

    public void setTestMode(boolean m) {
        this.testMode = m;
        this.redrawBoard();
    }

    private void fireOnBoardCellClicked(int col, int row) {
        //--- Each cell has a click event that calls this when it is clicked.
        //--- Then we pass it up to whoever registered an OnBoardCellClicked event.
        if (this.cellClickedListener != null && !readOnlyMode)
            cellClickedListener.onBoardCellClicked(this, col, row);
    }

    public void setOnBoardCellClickedListener(OnBoardCellClickedListener x) {
        cellClickedListener = x;
    }

}
