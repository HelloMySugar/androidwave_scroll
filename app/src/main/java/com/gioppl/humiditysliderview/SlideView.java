package com.gioppl.humiditysliderview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class SlideView extends View {
  private float width, height; //控件的宽度、高度

  private int maxNum, minNum; // 最大值及最小值
  private int divideNum; //文字的个数
  private int colorLineStart, colorLineEnd; //两端，中间的颜色
  private int colorMark; //刻度的颜色
  private int colorText, colorTextSelect; //文字，文字被选中之后的颜色
  private boolean isRatio; //是否是百分号
  private float normalMarkLength, specialMarkLength; //普通的刻度和为十的刻度的长度
  private float markToLineMargin; //刻度和滑动线之间的距离

  private int colorButton; //按钮的颜色
  private Context context; //上下文

  private Paint mPaintButton; //画按钮的画笔
  private Paint mPaintLine; //画线的画笔
  private Paint mPaintMark; //画刻度尺的画笔
  private Paint mPaintText; //画文本的画笔
  private Paint mPaintTest; //测试的画笔

  private Path mPathLine; //画滑动线的路径

  private float touchX; //本次滑动的坐标的X值
  private float touchY; //本次滑动的坐标的Y值
  private float originalY; //前一次的View滑动的位置Y坐标，判断向上滑动还是向下滑动

  private String result; //返回的结果

  private int touchStatus = 0; //0为禁止，1为上滑动，2为下滑动

  private CircleBean btnCircle = new CircleBean(0, 0); //按钮的中心位置坐标和半径

  public SlideView(Context context) {
    super(context);
  }

  public SlideView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    this.context = context;
    initAttrs(attrs);
  }

  public SlideView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public SlideView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  private void initAttrs(AttributeSet attrs) {
    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlideView);
    maxNum = typedArray.getInt(R.styleable.SlideView_maxNum, 100);
    minNum = typedArray.getInt(R.styleable.SlideView_minNum, 0);
    divideNum = typedArray.getInt(R.styleable.SlideView_divideNum, 8);
    colorLineStart = typedArray.getColor(R.styleable.SlideView_lineStartColor, Color.WHITE);
    colorLineEnd = typedArray.getColor(R.styleable.SlideView_lineEndColor, Color.WHITE);
    colorButton = typedArray.getColor(R.styleable.SlideView_buttonColor, Color.WHITE);
    btnCircle.r = typedArray.getInt(R.styleable.SlideView_circleR, 100);
    colorMark = typedArray.getColor(R.styleable.SlideView_markColor, Color.WHITE);
    colorText = typedArray.getColor(R.styleable.SlideView_textColor, Color.WHITE);
    colorTextSelect = typedArray.getColor(R.styleable.SlideView_colorTextSelect, Color.BLUE);
    isRatio = typedArray.getBoolean(R.styleable.SlideView_isRatio, true);
    normalMarkLength = typedArray.getFloat(R.styleable.SlideView_normalMarkLength, 50);
    specialMarkLength = typedArray.getFloat(R.styleable.SlideView_specialMarkLength, 100);
    markToLineMargin = typedArray.getFloat(R.styleable.SlideView_markToLineMargin, 50);
    typedArray.recycle(); //一定要回收
  }

  //初始化画笔
  private void initPaints() {
    //画按钮的画笔
    mPaintButton = new Paint(); //初始化画笔
    mPaintButton.setColor(colorButton); //设置颜色，这个颜色在构造方法中已经从xml中接收
    mPaintButton.setAntiAlias(true); //设置抗锯齿
    mPaintButton.setDither(true); //设置防止抖动
    mPaintButton.setStyle(Paint.Style.FILL); //设置画笔是空心还是实心，FILL是实心，STROKE是空心
    mPaintButton.setStrokeWidth(5); //画笔的宽度
    mPaintButton.setPathEffect(new CornerPathEffect(10f)); //设置path的样式，比如是实线还是虚线等

    //画滑动线的画笔
    mPaintLine = new Paint();
    mPaintLine.setColor(colorButton);
    mPaintLine.setAntiAlias(true);
    mPaintLine.setDither(true);
    mPaintLine.setStyle(Paint.Style.STROKE);
    mPaintLine.setStrokeWidth(15);
    mPaintLine.setPathEffect(new CornerPathEffect(10f));
    //设置颜色，这里设置的是镜像线性模式，两端颜色一样是colorLineStart，中间是colorLineEnd
    Shader shader = new LinearGradient(0, 0, btnCircle.cx, btnCircle.cy, colorLineStart, colorLineEnd, Shader.TileMode.MIRROR);
    mPaintLine.setShader(shader);

    //画测试点的画笔
    mPaintTest = new Paint();
    mPaintTest.setColor(Color.RED); //锚点我们设置红丝
    mPaintTest.setAntiAlias(true);
    mPaintTest.setDither(true);
    mPaintTest.setStyle(Paint.Style.STROKE);
    mPaintTest.setStrokeWidth(5);
    mPaintTest.setPathEffect(new CornerPathEffect(30f));

    //画刻度的画笔
    mPaintMark = new Paint();
    mPaintMark.setColor(colorMark);
    mPaintMark.setAntiAlias(true);
    mPaintMark.setDither(true);
    mPaintMark.setStyle(Paint.Style.STROKE);
    mPaintMark.setStrokeWidth(2);

    //画文本的画笔
    mPaintText = new Paint();
    mPaintText.setColor(colorText);
    mPaintText.setAntiAlias(true);
    mPaintText.setDither(true);
    mPaintText.setStyle(Paint.Style.FILL);
    mPaintText.setStrokeWidth(5);
    mPaintText.setTextSize(50);
  }

  //这个初始化画笔在onLayout中设置，为什么呢？主要是其中的画线需要前后有颜色变化，所以要得中间的位置的Y信息，只有在这个方法中才可以得到。
  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
//    width = right;
//    //初始化控件高度
//    height = bottom;
//    //设置按钮的xy值，默认按钮在中间
//    btnCircle.cx = (left + right) / 2.0f;
//    btnCircle.cy = (top + bottom) / 2.0f;

    // 计算控件高宽度
    width = right - left;
    height = bottom - top;
    //设置按钮的xy值，默认按钮在中间
    btnCircle.cx = width / 2.0f;
    btnCircle.cy = height / 2.0f;

    //初始化画笔
    initPaints();
    touchX = width / 2;
    //初始化按钮的Y位置为0，上一次Y位置为0
    touchY = 0;
    originalY = 0;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
//    canvas.translate(0, 50);
    //绘制按钮
    canvas.drawCircle(btnCircle.cx, btnCircle.cy, btnCircle.r, mPaintButton);
    //绘制滑动的线
    drawLinePaths(canvas);
    //绘制刻度
    drawMark(canvas);
    //绘制文本
    drawText(canvas);
  }

  //绘制滑动的线
  private void drawLinePaths(Canvas canvas) {
    mPathLine = new Path();
    //将起始点移动到按钮的
    mPathLine.moveTo(btnCircle.cx, 0);
    //在按钮前面的的2r处停下
    mPathLine.lineTo(btnCircle.cx, btnCircle.cy - btnCircle.r - btnCircle.r * 2);
    //第一条贝塞尔曲线
    mPathLine.quadTo(btnCircle.cx - btnCircle.r * 0.2f, btnCircle.cy - btnCircle.r * 1.9f, btnCircle.cx - btnCircle.r, btnCircle.cy - btnCircle.r * 1.5f);
    //第二条贝塞尔曲线
    mPathLine.quadTo(btnCircle.cx - 2 * btnCircle.r, btnCircle.cy - btnCircle.r * 0.9f, btnCircle.cx - btnCircle.r * 2, btnCircle.cy);
    //第三条贝塞尔曲线
    mPathLine.quadTo(btnCircle.cx - 2 * btnCircle.r, btnCircle.cy + btnCircle.r * 0.9f, btnCircle.cx - btnCircle.r, btnCircle.cy + btnCircle.r * 1.5f);
    //第四条贝塞尔曲线
    mPathLine.quadTo(btnCircle.cx - btnCircle.r * 0.2f, btnCircle.cy + btnCircle.r * 1.9f, btnCircle.cx, btnCircle.cy + btnCircle.r + btnCircle.r * 2);
    //把剩余的地方画直
    mPathLine.lineTo(btnCircle.cx, height);
    //用画板画线
    canvas.drawPath(mPathLine, mPaintLine);
  }

  //绘制刻度
  private void drawMark(Canvas canvas) {
    int totalMarkNum = divideNum * 10; //刻度总个数
    float everyMarkHeight = height / totalMarkNum; //每个的高度
    int a = 0; //计数器，计数看是否是等于10，等于5的刻度

    //Path的各种方法
    PathMeasure pathMeasure = new PathMeasure(mPathLine, false);
    float[] pos = new float[2];
    float[] tan = new float[2];
    for (int i = -2; i < totalMarkNum; i++, a++) {
      //现在必须要在曲线弯曲的地方刻度同时弯曲，这就要用到一个类
      // PathMeasure.getPosTan(float distance, float pos[], float tan[])
      // 第一个参数是distance，表示曲线的距离，我们直接用每一个小刻度乘以当前的绘制进度i就可以得到了，
      // pos是位置数组，pos[0]为该distance的x值，pos[1]是该distance的y值。
      // tan是正切值，我们暂时用不到，
      // 对了，在PathMeasure 类初始化的时候要传入我们的滑动线的path，然后所有计算的都是该path中的数据。
      pathMeasure.getPosTan(height / totalMarkNum * (i + 5), pos, tan);
      float x = pos[0]; //获取他的X位置
      if (a != 5 && a != 10) { //一般的刻度
        canvas.drawLine(x - markToLineMargin - normalMarkLength, i * everyMarkHeight, x - markToLineMargin, i * everyMarkHeight, mPaintMark);
      } else if (a == 5) { //=5，没设置，和一般刻度一样
        canvas.drawLine(x - markToLineMargin - normalMarkLength, i * everyMarkHeight, x - markToLineMargin, i * everyMarkHeight, mPaintMark);
      } else { //=10，画长一点的线
        canvas.drawLine(x - markToLineMargin - specialMarkLength, i * everyMarkHeight, x - markToLineMargin, i * everyMarkHeight, mPaintMark);
        a = 0;
      }
    }
  }

  //绘制文本
  private void drawText(Canvas canvas) {
    //这个解析率就是我们放大的文字的附近，不允许其他文字，我们在56%的时候旁边的50%，60%全都不绘制，解析率越大，说明不可绘制的范围更大。
    float resolutionRation = 100;
    int totalTextNum = divideNum; //共有多少个文字
    float everyTextHeight = height / totalTextNum;//每个十的整数的高度
    float everyMarkHeight = everyTextHeight / 10;//每个小的刻度的高度
    String normalTail; //正常的文字尾巴，尾巴的作用是是否带百分号
    String enlargeTail; //放大的文字尾巴
    if (isRatio) {//是否带百分号
      normalTail = "0%";
      enlargeTail = "%";
    } else {
      normalTail = "0";
      enlargeTail = "";
    }
    for (int i = 0; i <= totalTextNum; i++) {
      //这个height不是屏幕高度，是我们文字绘制的高度，就是在y的哪里绘制
      //-16是我自己调出来的，别问我为什么，-16好看
      float height = i * everyTextHeight - 16;
      if (touchStatus == 0) {//静止
        canvas.drawText(handleText(i, normalTail), 30, height, mPaintText);
      } else if (touchStatus == 1) {//上滑动
        if ((height) > btnCircle.cy - resolutionRation && (height) < btnCircle.cy + resolutionRation) {//正常绘制
          //放大绘制
          setTextPaintStyle(true);
          int g = (int) (btnCircle.cy / everyMarkHeight) + 2;
          result = g + enlargeTail;
          canvas.drawText(g + enlargeTail, 30, btnCircle.cy + 20, mPaintText);
        } else {
          //正常绘制
          setTextPaintStyle(false);
          canvas.drawText(handleText(i, normalTail), 30, height, mPaintText);
        }
      } else {//下滑动
        if ((height) > btnCircle.cy - resolutionRation && (height) < btnCircle.cy + resolutionRation) {//正常绘制
          //放大绘制
          setTextPaintStyle(true);
          int g = (int) (btnCircle.cy / everyMarkHeight) + 2;
          result = g + enlargeTail;
          canvas.drawText(g + enlargeTail, 30, btnCircle.cy + 20, mPaintText);
        } else {
          //正常绘制
          setTextPaintStyle(false);
          canvas.drawText(handleText(i, normalTail), 30, height, mPaintText);
        }
      }
    }
  }

  //i是我们拖动的进度，tail是尾巴，尾巴就是是否加百分号
  private String handleText(int i, String tail) {
    String s = i + tail;
    if (s.equals("00") || s.equals("00%")) {
      s = s.replaceFirst("00", "0");
    }
    return s;
  }

  //第一个参数是是否放大
  private void setTextPaintStyle(boolean isEnlarge) {
    if (isEnlarge) {
      mPaintText.setColor(colorTextSelect);
      mPaintText.setTextSize(80);
    } else {
      mPaintText.setColor(colorText);
      mPaintText.setTextSize(50);
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    touchX = event.getX();
    //获取滑动的Y值，减去按钮的半径,保存
    touchY = event.getY(); //event.getY() - btnCircle.r;
    //如果这一次的Y值比上一次的Y值大，说明是上滑动，反之下滑动
    if ((touchY - originalY) > 0) {
      touchStatus = 2;//上滑动
    } else {
      touchStatus = 1;//下滑动
    }
    //这次的Y值赋值给上一次的Y值，保存
    originalY = touchY;
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN: //按下

        break;
      case MotionEvent.ACTION_MOVE: //滑动
        btnCircle.cy = touchY;
        invalidate();
        //回调接口，移动回调
        if (result != null) {
          if (result.charAt(result.length() - 1) == '%') {
            result = result.substring(0, result.length() - 1);
            scrollBack.scrollMove(Integer.parseInt(result));
          } else {
            scrollBack.scrollMove(Integer.parseInt(result));
          }
        }
        break;
      case MotionEvent.ACTION_UP: //松开
        //回调接口，松开回调
        if (result != null){
          if (result.charAt(result.length()-1)=='%'){
            result = result.substring(0,result.length()-1);
            scrollBack.scrollUp(Integer.parseInt(result));
          }else {
            scrollBack.scrollUp(Integer.parseInt(result));
          }
        }
        break;
    }
    //返回true，说明这个控件消费了该事件
    return true;
  }

  //本类中的回调
  private ScrollCallBack scrollBack;
  //设置回调
  public void setScrollBack(ScrollCallBack scrollBack) {
    this.scrollBack = scrollBack;
  }

  //回调接口的类
  public interface ScrollCallBack {
    void scrollMove(int num);
    void scrollUp(int num);
  }

  //定义的内部类
  public static class CircleBean {
    float cx, cy, r;

    public CircleBean(int cx, int cy) {
      this.cx = cx;
      this.cy = cy;
    }
  }
}
