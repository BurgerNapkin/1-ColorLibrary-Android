package com.o7o7orz.colorpickerwheel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.constraint.ConstraintLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Wheels {

    /*
    viewColor is the view you want to change part of it's color

    ColorMethodName must take 1 argument and it must bet int.class (e.g. setTextColor, setBackgroundColor)

    SharedPreferences is the specific sp you want to use and KEY is the key value associated with the color(int) value that is selected
     */


    @SuppressLint({"ClickableViewAccessibility", "ApplySharedPref"})
    public static void colorPicker(final View viewColor, final View viewContainer, final String colorMethodName, final boolean removeAfterSelect, final SharedPreferences sp, final String KEY) {

        final Context context = viewContainer.getContext();
        final String CONSTRAINTLAYOUT = "constraintlayout", RELATIVELAYOUT = "relativelayout", LINEARLAYOUT = "linearlayout";
        final String layoutType = viewContainer instanceof ConstraintLayout? CONSTRAINTLAYOUT: viewContainer instanceof RelativeLayout? RELATIVELAYOUT : LINEARLAYOUT;
        final GradientDrawable anchorDraw, circleRedDraw, circleGreenDraw, circleBlueDraw, circleAlphaDraw;
        final Map<String, Integer> mapARGB = new HashMap<>();
        mapARGB.put("alpha", 255);
        mapARGB.put("red", 0);
        mapARGB.put("green", 0);
        mapARGB.put("blue", 0);

        double percent = 0.9;
        double density = context.getResources().getDisplayMetrics().density;
        double viewHeight = viewContainer.getHeight();
        double viewWidth = viewContainer.getWidth();
        int usedSideLength = (int) (viewHeight > viewWidth ? viewWidth : viewHeight);
        double length = viewHeight > viewWidth ? viewWidth * percent : viewHeight * percent;
        int anchorSize = (int) (length * .3);
        int redSize = (int) (length*.45);
        int greenSize = (int) (length*.6);
        int blueSize = (int) (length*.75);
        int alphaSize = (int) (length*.9);
        int strokeSizeBig = (int) (4*density);
        int strokeSizeSmall = (int) (1.5*density);
        int thumbSize = (int) (length*.06);
        final float centerX, centerY; //TODO
        centerX = (float) (viewContainer.getLeft() + viewWidth/2);
        centerY = (float) (viewContainer.getTop() + viewHeight/2);
        final SharedPreferences saveMapColor = context.getSharedPreferences("o7o7orz_colors", Context.MODE_PRIVATE);

        int noAlpha = Color.TRANSPARENT;
        final int[] colorsRed = {noAlpha, noAlpha ,noAlpha , noAlpha, noAlpha, noAlpha, noAlpha, Color.RED};
        final int[] colorsGreen = {noAlpha, noAlpha ,noAlpha , Color.GREEN};
        final int[] colorsBlue = {noAlpha, noAlpha ,noAlpha , Color.BLUE};
        final int[] colorsAlpha = {noAlpha, noAlpha ,noAlpha , Color.BLACK};

        boolean wasCLReplaced;
        //check too see if new a call has been made not to remove layout (3rd arg is false in both method calls) but the view has changed
        if (viewContainer.findViewWithTag("addedConstraint") != null) {
            wasCLReplaced = true;
            View aView = viewContainer.findViewWithTag("addedConstraint");
            if (layoutType.equals(RELATIVELAYOUT) && aView.getId() != viewColor.getId()) {
                ((RelativeLayout) viewContainer).removeView(aView);
            }
            if (layoutType.equals(LINEARLAYOUT) && aView.getId() != viewColor.getId()) {
                ((LinearLayout) viewContainer).removeView(aView);
            }
            if (layoutType.equals(CONSTRAINTLAYOUT) && aView.getId() != viewColor.getId()) {
                ((ConstraintLayout) viewContainer).removeView(aView);
            }
        } else {
            wasCLReplaced = false;
        }

        final ConstraintLayout CL = (ConstraintLayout) LayoutInflater.from(context).inflate(R.layout.color_selector_layout, null);
        CL.setTag("addedConstraint");

        Bitmap bp;
        if (length > 500) {
            bp = BitmapFactory.decodeResource(context.getResources(), R.drawable.x50);
        } else {
            bp = BitmapFactory.decodeResource(context.getResources(), R.drawable.x25);
        }
        BitmapDrawable bpd = new BitmapDrawable(context.getResources(), bp);
        bpd.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        CL.setBackground(bpd);


        final ImageView anchor = CL.findViewById(R.id.anchor_circle);
        anchorDraw = makeCircle(Color.argb(255, 0, 0, 0), (int) (strokeSizeBig*1.5), anchorSize); //TODO fix color, using for test
        anchor.setBackground(anchorDraw);
        Color.argb(0, 0, 0, 0);
        if (wasCLReplaced) {
            int color = saveMapColor.getInt("anchor", Color.BLACK);
            anchorDraw.setColor(color);
        }

        // <<<<< create TextView for "set" to be inside anchor >>>>>>>>>
        final TextView tvSet = CL.findViewById(R.id.textView_set);
        ConstraintLayout.LayoutParams tvParams = (ConstraintLayout.LayoutParams) tvSet.getLayoutParams();
        tvParams.width = ConstraintLayout.LayoutParams.WRAP_CONTENT;
        tvParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
        tvSet.setLayoutParams(tvParams);
        tvSet.setLines(1);
        tvSet.setText(R.string.set);
        tvSet.setBackgroundColor(Color.argb(255, 55, 55, 55));
        if (wasCLReplaced) {
            tvSet.setTextColor(saveMapColor.getInt("textColor", Color.WHITE));
        } else {
            tvSet.setTextColor(Color.WHITE);
        }

        // <<<<<< inner size is radius so it is HALF OF ANCHOR, stop forgetting radius is half of length >>>>>>>>>>>
        ImageView circleRed = CL.findViewById(R.id.red_circle);
        circleRedDraw= makeDonut(colorsRed, strokeSizeBig, anchorSize/2, redSize);
        circleRed.setBackground(circleRedDraw);
        final ImageView thumbRed = CL.findViewById(R.id.thumb_red);
        thumbRed.setBackground(makeCircle(Color.RED, strokeSizeSmall, thumbSize));
        ConstraintLayout.LayoutParams paramRedThumb = (ConstraintLayout.LayoutParams) thumbRed.getLayoutParams();
        paramRedThumb.circleRadius = (redSize/2);
        int color;
        int invColor;
        if(wasCLReplaced){
            paramRedThumb.circleAngle = saveMapColor.getFloat("red", 0);
            color = (int) (255*(saveMapColor.getFloat("red", 0)/360));
            invColor = 255-color;
            if (color != 0) {
                colorsRed[7] = Color.argb(255, 255, invColor, invColor);
                circleRedDraw.setColors(colorsRed);
                mapARGB.put("red", (color));
            }

        }
        thumbRed.setLayoutParams(paramRedThumb);

        ImageView circleGreen = CL.findViewById(R.id.green_circle);
        circleGreenDraw = makeDonut(colorsGreen, strokeSizeBig, (redSize)/2, greenSize);
        circleGreen.setBackground(circleGreenDraw);
        final ImageView thumbGreen = CL.findViewById(R.id.thumb_green);
        thumbGreen.setBackground(makeCircle(Color.GREEN, strokeSizeSmall, thumbSize));
        ConstraintLayout.LayoutParams paramsGreenThumb = (ConstraintLayout.LayoutParams) thumbGreen.getLayoutParams();
        paramsGreenThumb.circleRadius = (greenSize/2);
        if(wasCLReplaced){
            paramsGreenThumb.circleAngle = saveMapColor.getFloat("green", 0);
            color = (int) (255*(saveMapColor.getFloat("green", 0)/360));
            invColor = 255-color;
            if (color!=0) {
                colorsGreen[3] = Color.argb(255, invColor, 255, invColor);
                circleGreenDraw.setColors(colorsGreen);
                mapARGB.put("green", color);
            }
        }
        thumbGreen.setLayoutParams(paramsGreenThumb);

        ImageView circleBlue = CL.findViewById(R.id.blue_circle);
        circleBlueDraw = makeDonut(colorsBlue, strokeSizeBig, (redSize)/2, blueSize);
        circleBlue.setBackground(circleBlueDraw);
        final ImageView thumbBlue = CL.findViewById(R.id.thumb_blue);
        thumbBlue.setBackground(makeCircle(Color.BLUE, strokeSizeSmall, thumbSize));
        ConstraintLayout.LayoutParams paramsBlueThumb = (ConstraintLayout.LayoutParams) thumbBlue.getLayoutParams();
        paramsBlueThumb.circleRadius = (blueSize/2);
        if(wasCLReplaced){
            paramsBlueThumb.circleAngle = saveMapColor.getFloat("blue", 0);
            color = (int) (255*saveMapColor.getFloat("blue", 0)/360);
            invColor = 255-color;
            if (color != 0) {
                colorsBlue[3] = Color.argb(255, invColor, invColor, 255);
                circleBlueDraw.setColors(colorsBlue);
                mapARGB.put("blue", color);
            }
        }
        thumbBlue.setLayoutParams(paramsBlueThumb);

        ImageView circleAlpha = CL.findViewById(R.id.alpha_circle);
        circleAlphaDraw = makeDonut(colorsAlpha, strokeSizeBig, blueSize/2, alphaSize);
        circleAlpha.setBackground(circleAlphaDraw);
        final ImageView thumbAlpha = CL.findViewById(R.id.thumb_alpha);
        thumbAlpha.setBackground(makeCircle(Color.WHITE, strokeSizeSmall, thumbSize));
        ConstraintLayout.LayoutParams paramsAlphaThumb = (ConstraintLayout.LayoutParams) thumbAlpha.getLayoutParams();
        paramsAlphaThumb.circleRadius = (alphaSize/2);
        if(wasCLReplaced){
            paramsAlphaThumb.circleAngle = saveMapColor.getFloat("alpha", 0);
            color = (int) (255*(saveMapColor.getFloat("alpha", 360)/360));
            invColor = 255-color;
            if (color!=0) {
                colorsAlpha[3] = Color.argb(invColor, 0, 0, 0);
                circleAlphaDraw.setColors(colorsAlpha);
                mapARGB.put("alpha", invColor);
            }
        }
        thumbAlpha.setLayoutParams(paramsAlphaThumb);


        View.OnTouchListener MyTouch = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int ID = v.getId();

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.performClick();
                    SharedPreferences.Editor editor = saveMapColor.edit();
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) thumbAlpha.getLayoutParams();
                    editor.putFloat("alpha", params.circleAngle);
                    params = (ConstraintLayout.LayoutParams) thumbRed.getLayoutParams();
                    editor.putFloat("red", params.circleAngle);
                    params = (ConstraintLayout.LayoutParams) thumbGreen.getLayoutParams();
                    editor.putFloat("green", params.circleAngle);
                    params = (ConstraintLayout.LayoutParams) thumbBlue.getLayoutParams();
                    editor.putFloat("blue", params.circleAngle);
                    int color = Color.argb(mapARGB.get("alpha"), mapARGB.get("red"), mapARGB.get("green"), mapARGB.get("blue"));
                    editor.putInt("anchor", color);
                    color = Color.argb(255, mapARGB.get("red"), mapARGB.get("green"),mapARGB.get("blue") );
                    editor.putInt("textColor", color);
                    editor.commit();
                }

                if (event.getAction()==MotionEvent.ACTION_MOVE && (ID == R.id.thumb_red || ID == R.id.thumb_green || ID == R.id.thumb_blue || ID == R.id.thumb_alpha)) {
                    float rawX = event.getRawX();
                    float rawY = event.getRawY();
                    float dx = rawX-centerX;
                    float dy = centerY - rawY;

                    double deg;

                    ConstraintLayout.LayoutParams innerParam = (ConstraintLayout.LayoutParams) v.getLayoutParams();
                    if (dx == 0) {
                        deg = dy>0? 0 : 180;
                        innerParam.circleAngle = (float) deg;
                        v.setLayoutParams(innerParam);
                    }
                    if (dy == 0) {
                        deg = dx>0? 90 : 270;
                        innerParam.circleAngle = (float) deg;
                        v.setLayoutParams(innerParam);
                    }

                    deg = Math.atan((dy / dx));
                    deg = deg*180/Math.PI;

                    if (dx > 0) {
                        deg = dy > 0 ? 90-deg : Math.abs(deg) + 90;
                    } else {
                        deg = dy > 0 ? Math.abs(deg) + 270 : (90-deg) + 180;
                    }

                    innerParam.circleAngle = (float) deg;
                    v.setLayoutParams(innerParam);
                    int color255 = (int) ((deg/360)*255);
                    int colorInvert = 255-color255;

                    /*
                    The important part of this is that you don't ADD color you take the others away (in order to see it). So you start with White(all 255)
                    if you want RED then you have to subtract both BLUE and Green. Calculation for amount of color gained is angle/360 * 255
                    calculation for color lost is 255-previous calculation
                    To show the correct color in the RGB wheel we need to set the "inverted" calculation, but then just save red to whatever the normal calculation is.
                    SO--- showing and saving are different
                    Also, alpha is opposite of colors, in that you start at 100% alpha and decrease  to 0% so setting and saving are both "inverted" calculation.
                     */
                    if (ID == R.id.thumb_red) {
                        mapARGB.put("red", color255);
                        colorsRed[7] = Color.argb(255, 255, colorInvert, colorInvert);
                        circleRedDraw.setColors(colorsRed);

                    } else if (ID == R.id.thumb_green) {
                        mapARGB.put("green", color255);
                        colorsGreen[3] = Color.argb(255, colorInvert, 255, colorInvert);
                        circleGreenDraw.setColors(colorsGreen);

                    } else if (ID == R.id.thumb_blue) {
                        mapARGB.put("blue", color255);
                        colorsBlue[3] = Color.argb(255, colorInvert, colorInvert, 255);
                        circleBlueDraw.setColors(colorsBlue);

                    } else if (ID == R.id.thumb_alpha) {
                        mapARGB.put("alpha", (colorInvert));
                        colorsAlpha[3] = Color.argb(colorInvert, 0, 0, 0);
                        circleAlphaDraw.setColors(colorsAlpha);

                    } else {
                    }

                    anchorDraw.setColor(Color.argb(mapARGB.get("alpha"), mapARGB.get("red"), mapARGB.get("green"), mapARGB.get("blue")));
                    tvSet.setTextColor(Color.argb(255, mapARGB.get("red"), mapARGB.get("green"),mapARGB.get("blue") ));
                    //tvSet.setBackgroundColor(Color.argb(255, (255-mapARGB.get("alpha")), (255-mapARGB.get("alpha")), (255-mapARGB.get("alpha"))));

                }

                return true;
            }
        };

        thumbRed.setOnTouchListener(MyTouch);
        thumbGreen.setOnTouchListener(MyTouch);
        thumbBlue.setOnTouchListener(MyTouch);
        thumbAlpha.setOnTouchListener(MyTouch);


        // ---- string - method -------------------------------------------
        if (viewColor!=null) {
            anchor.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ApplySharedPref")
                @Override
                public void onClick(View v) {
                    Method method;
                    try {
                        method = viewColor.getClass().getMethod(colorMethodName, int.class);
                        method.invoke(viewColor,Color.argb(mapARGB.get("alpha"), mapARGB.get("red"), mapARGB.get("green"), mapARGB.get("blue")));
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }


                    if (removeAfterSelect) {
                        if (layoutType.equalsIgnoreCase("relativelayout")) {
                            ((RelativeLayout)viewContainer).removeView(CL);
                        }
                        if (layoutType.equalsIgnoreCase("constraintlayout")) {
                            ((ConstraintLayout)viewContainer).removeView(CL);
                        }
                        if (layoutType.equalsIgnoreCase("linearlayout")) {
                            ((LinearLayout)viewContainer).removeView(CL);
                        }
                    }

                    // Moved this to Action_UP so that everything saves on view change EVEN IF ONCLICK IS NOT CALLED, both have pros and cons but I like ACTION_UP more

//                    SharedPreferences.Editor editor = saveMapColor.edit();
//                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) thumbAlpha.getLayoutParams();
//                    editor.putFloat("alpha", params.circleAngle);
//                    params = (ConstraintLayout.LayoutParams) thumbRed.getLayoutParams();
//                    editor.putFloat("red", params.circleAngle);
//                    params = (ConstraintLayout.LayoutParams) thumbGreen.getLayoutParams();
//                    editor.putFloat("green", params.circleAngle);
//                    params = (ConstraintLayout.LayoutParams) thumbBlue.getLayoutParams();
//                    editor.putFloat("blue", params.circleAngle);
//                    int color = Color.argb(mapARGB.get("alpha"), mapARGB.get("red"), mapARGB.get("green"), mapARGB.get("blue"));
//                    editor.putInt("anchor", color);
//                    color = Color.argb(255, mapARGB.get("red"), mapARGB.get("green"),mapARGB.get("blue") );
//                    editor.putInt("textColor", color);
//                    editor.commit();

                }
            });
        }

        if (sp != null) {
            sp.edit().putInt(KEY, Color.argb(mapARGB.get("alpha"), mapARGB.get("red"), mapARGB.get("green"), mapARGB.get("blue"))).commit();
        }


        if (viewContainer.findViewWithTag("addedConstraint")==null) {
            if (layoutType.equalsIgnoreCase(CONSTRAINTLAYOUT)) {
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(usedSideLength, usedSideLength);
                int id = viewContainer.getId();
                params.bottomToBottom = id;
                params.topToTop = id;
                params.startToStart = id;
                params.endToEnd = id;
                params.matchConstraintPercentHeight = 0.5f;
                params.matchConstraintPercentWidth = 0.5f;
                CL.setLayoutParams(params);
                ((ConstraintLayout)viewContainer).addView(CL);
            }

            if (layoutType.equalsIgnoreCase(RELATIVELAYOUT)) {
                RelativeLayout.LayoutParams paramsCL = new RelativeLayout.LayoutParams(usedSideLength, usedSideLength);
                paramsCL.addRule(RelativeLayout.CENTER_IN_PARENT);
                CL.setLayoutParams(paramsCL);
                ((RelativeLayout)viewContainer).addView(CL);
            }

            if (layoutType.equalsIgnoreCase(LINEARLAYOUT)) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(usedSideLength, usedSideLength);
                params.gravity = Gravity.CENTER;
                CL.setLayoutParams(params);
                ((LinearLayout)viewContainer).addView(CL);
            }
        }


    }


    // This is for a solid circle with border (no hole in middle)
    static private GradientDrawable makeCircle(@ColorInt int color, int stroke, int size) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(color);
        gd.setStroke(stroke , Color.BLACK);
        gd.setSize(size, size);

        return gd;
    }

    //  - radius is length/2 STOP FORGETTING TO DIVIDE BY TWO FFS
    static private GradientDrawable makeDonut(int[] colorSet, int stroke, int sizeInner, int sizeOuter) {

        GradientDrawable oval = new GradientDrawable();
        oval.setShape(GradientDrawable.OVAL);
        oval.setStroke(stroke, Color.BLACK);
        oval.setSize(sizeOuter, sizeOuter);

        oval.setGradientRadius(sizeInner);
        oval.setGradientCenter(0.5f,0.5f);
        oval.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        oval.setColors(colorSet);

        return oval;
    }

    public static void colorPicker(View viewColor, View viewContainer,String colorMethodName, boolean removeAfterSelect) {
        colorPicker(viewColor, viewContainer, colorMethodName, removeAfterSelect, null, null);

    }

}
