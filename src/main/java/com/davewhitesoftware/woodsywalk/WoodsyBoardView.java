package com.davewhitesoftware.woodsywalk;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TableLayout;
import android.widget.TableRow;

//
//  A view that displays an entire game board.
//

public class WoodsyBoardView extends TableLayout {
    private WoodsyBoardData board = new WoodsyBoardData();
    private Context ctx;
    private boolean testMode = false;

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
    }
    private void setupTable() {
        //-- sets up the rows and columns in a board view.
        if (this.testMode) this.setupTestBoard();
        this.removeAllViews();
        for (int i = 0; i < this.board.getHeight(); i++) {
            TableRow tr = new TableRow(this.ctx);
            for (int j = 0; j < this.board.getWidth(); j++) {
                PieceView pv = new PieceView(this.ctx);
                TableRow.LayoutParams pvlp = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
                pv.setPiece(this.board.getCell(j,i));
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

        a.recycle();
        setupTable();
    }
}
