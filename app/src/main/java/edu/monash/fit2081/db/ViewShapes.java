/*

 */
package edu.monash.fit2081.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import edu.monash.fit2081.db.provider.SchemeShapes;
import edu.monash.fit2081.db.provider.ShapeValues;

import static edu.monash.fit2081.db.provider.SchemeShapes.Shape;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViewShapes extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
//    int dX;
//    int dY;
    int mLastTouchX;
    int mLastTouchY;
    ContentResolver resolver;

    public static CustomView customView = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);

        resolver = getActivity().getContentResolver(); //***
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        customView = new CustomView(getContext());

        //***
        customView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Circle is the default shape if can't find the key
                String selectedShapeDrawing = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE).getString("selectedShapeDrawing", "Circle");
                int x = (int) event.getX();
                int y = (int) event.getY();
                int dX, dY;
//
//                event.getPointerId(event.getActionIndex());

                int action = MotionEventCompat.getActionMasked(event);
                switch (action){
                    case MotionEvent.ACTION_POINTER_DOWN:
                        Log.d("tag", "Pointer down");
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        Log.d("tag", "Pointer up");
                        break;
                    case MotionEvent.ACTION_DOWN:
                        Log.d("tag", "down");
                        mLastTouchX = x; mLastTouchY = y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d("tag", "move");
                        if (selectedShapeDrawing.equals("Line")) {
                            dX = 5; dY = 5;
                            storeShape("Circle", x, y, dX, dY);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d("tag", "up");
                        if (!selectedShapeDrawing.equals("Line")) {
                            dX = Math.abs(x - mLastTouchX); dY = Math.abs(y - mLastTouchY);
                            if ("Rectangle Ellipse".indexOf(selectedShapeDrawing) != -1) {
                                if (y < mLastTouchY) {
                                    mLastTouchY = y;
                                }
                                if (mLastTouchX > x) {
                                    mLastTouchX = x;
                                }
                            }
                            if (selectedShapeDrawing.equals("StraightLine")) {
                                dX = x - mLastTouchX; dY =  y - mLastTouchY;
                            }
                            storeShape(selectedShapeDrawing, mLastTouchX, mLastTouchY, dX, dY);
                        }
                        break;
                }
                return true;
            }
        });
        //***

        return (customView);
    }

    //***
    private void storeShape(String shape, int x, int y, int deltaX, int deltaY) {
        int selectedColor = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE).getInt("selectColor", 0);

        ContentValues contentValues = new ContentValues();
        contentValues.put(SchemeShapes.Shape.SHAPE_TYPE, shape);
        contentValues.put(SchemeShapes.Shape.SHAPE_X, x);
        contentValues.put(SchemeShapes.Shape.SHAPE_Y, y);
        contentValues.put(SchemeShapes.Shape.SHAPE_RADIUS, Math.max(deltaX, deltaY));
        contentValues.put(SchemeShapes.Shape.SHAPE_WIDTH, deltaX);
        contentValues.put(SchemeShapes.Shape.SHAPE_HEIGHT, deltaY);
        contentValues.put(SchemeShapes.Shape.SHAPE_BORDER_THICKNESS, 10);
        contentValues.put(SchemeShapes.Shape.SHAPE_COLOR, selectedColor);

        resolver.insert(SchemeShapes.Shape.CONTENT_URI, contentValues);
    }
    //***

    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        CursorLoader cursorLoader = new CursorLoader(getActivity(),
            Shape.CONTENT_URI,
            //VersionContract.Version.buildUri(2),
            Shape.PROJECTION,
            null,
            null,
            null
        );
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        ShapeValues[] shapes = new ShapeValues[cursor.getCount()];
        int i = 0;
        if (cursor.moveToFirst()) {
            do {

                shapes[i] = new ShapeValues(cursor.getString(
                    cursor.getColumnIndex(Shape.SHAPE_TYPE)),
                    cursor.getInt(cursor.getColumnIndex(Shape.SHAPE_X)),
                    cursor.getInt(cursor.getColumnIndex(Shape.SHAPE_Y)),
                    cursor.getInt(cursor.getColumnIndex(Shape.SHAPE_BORDER_THICKNESS)),
                    cursor.getInt(cursor.getColumnIndex(Shape.SHAPE_RADIUS)),
                    cursor.getInt(cursor.getColumnIndex(Shape.SHAPE_WIDTH)),
                    cursor.getInt(cursor.getColumnIndex(Shape.SHAPE_HEIGHT)),
                    cursor.getString(cursor.getColumnIndex(Shape.SHAPE_COLOR))
                );
                i++;
                // do what ever you want here
            } while (cursor.moveToNext());
        }
        // cursor.close();
        customView.numberShapes = cursor.getCount();
        customView.shapes = shapes;
        customView.invalidate();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //do your stuff for your fragment here
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        //    simpleCursorAdapter.swapCursor(null);
    }
}


