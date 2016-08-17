# SlideLeftMenuRecyclerView

最近学习了如何做一个像QQ的左滑RecyclerView的item显示选项的，主要是用到**Scroller**

##**我们首先新建一个自己的RecyclerView**
定义好一些要用的的变量
重写构造方法，把前两个构造方法改为如下，使无论如何构造都要执行第三个构造方法
在第三个构造方法里初始化Scroller
```
public class LeftSwipeMenuRecyclerView extends RecyclerView {

    //置顶按钮
    private TextView tvTop;
    //删除按钮
    private TextView tvDelete;
    //item相应的布局
    private LinearLayout mItemLayout;
    //菜单的最大宽度
    private int mMaxLength;
    
    //上一次触摸行为的x坐标
    private int mLastX;
    //上一次触摸行为的y坐标
    private int mLastY;
    
    //当前触摸的item的位置
    private int mPosition;

    //是否在垂直滑动列表
    private boolean isDragging;
    //item是在否跟随手指移动
    private boolean isItemMoving;
    //item是否开始自动滑动
    private boolean isStartScroll;
    
    //左滑菜单状态   0：关闭 1：将要关闭 2：将要打开 3：打开
    private int mMenuState;
    private static int MENU_CLOSED = 0;
    private static int MENU_WILL_CLOSED = 1;
    private static int MENU_OPEN = 2;
    private static int MENU_WILL_OPEN = 3;

    //实现弹性滑动，恢复
    private Scroller mScroller;
    
    //item的事件监听
    private OnItemActionListener mListener;

    public LeftSwipeMenuRecyclerView(Context context) {
        this(context, null);
    }

    public LeftSwipeMenuRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LeftSwipeMenuRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScroller = new Scroller(context, new LinearInterpolator());
    }
```
##**重写onTouchEvent方法**
event主要有以下几个Action

 1. ACTION_DOWN  手指接触到屏幕
 2. ACTION_MOVE  手指在屏幕滑动
 3. ACTION_UP  手指离开屏幕

一开始肯定要获取x和y的相对坐标
```
int x= (int) event.getX();
int y= (int) event.getY();
```
然后接下来分别处理3个不同的行为
###**1.ACTION_DOWN**

```
case MotionEvent.ACTION_DOWN:
                if (mMenuState == MENU_CLOSED) {
                    //根据坐标获得view
                    View view = findChildViewUnder(x, y);
                    if (view == null) {
                        return false;
                    }
                    //获得这个view的ViewHolder
                    RVAdapter.Holder holder = (RVAdapter.Holder) getChildViewHolder(view);
                    //获得这个view的position
                    mPosition = holder.getAdapterPosition();
                    //获得这个view的整个布局
                    mItemLayout = holder.llLayout;
                    //获得这个view的删除按钮
                    tvDelete = holder.tvDelete;
                    //这个view的整个置顶按钮
                    tvTop = holder.tvTop;
                    //两个按钮的宽度
                    mMaxLength = tvDelete.getWidth() + tvTop.getWidth();
                    
                    //设置删除按钮点击监听
                    tvDelete.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mItemLayout.scrollTo(0, 0);
                            mMenuState =MENU_CLOSED;
                            mListener.OnItemDelete(mPosition);
                        }
                    });
                    //设置置顶按钮点击监听
                    tvTop.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mItemLayout.scrollTo(0, 0);
                            mMenuState =MENU_CLOSED;
                            mListener.OnItemTop(mPosition);
                        }
                    });
                    //如果是打开状态，点击其他就把当前menu关闭掉
                } else if (mMenuState == MENU_OPEN) {
                    mScroller.startScroll(mItemLayout.getScrollX(), 0, -mMaxLength, 0, 200);
                    invalidate();
                    mMenuState = MENU_CLOSED;
                    //该点击无效
                    return false;
                } else {
                    return false;
                }
                break;
```
###**2.ACTION_MOVE**

```
            case MotionEvent.ACTION_MOVE:
                //计算偏移量
                int dx = mLastX - x;
                int dy = mLastY - y;
                //当前滑动的x
                int scrollx = mItemLayout.getScrollX();

                if (Math.abs(dx) > Math.abs(dy)) {

                    isItemMoving = true;
                    //超出左边界则始终保持不动
                    if (scrollx + dx <= 0) {
                        mItemLayout.scrollTo(0, 0);
                        //滑动无效
                        return false;
                    //超出右边界则始终保持不动
                    } else if (scrollx + dx >= mMaxLength) {
                        mItemLayout.scrollTo(mMaxLength, 0);
                        //滑动无效
                        return false;
                    }
                    //菜单随着手指移动
                    mItemLayout.scrollBy(dx, 0);
                //如果水平移动距离大于30像素的话，recyclerView不会上下滑动
                }else  if (Math.abs(dx) > 30){
                    return true;
                }
                //如果菜单正在打开就不能上下滑动
                if (isItemMoving){
                    mLastX = x;
                    mLastY = y;
                    return true;
                }
                break;
```
###**3.ACTION_UP**

```
case MotionEvent.ACTION_UP:
                //手指抬起时判断是否点击,静止且有Listener才能点击
                if (!isItemMoving && !isDragging && mListener != null) {
                    mListener.OnItemClick(mPosition);
                }
                isItemMoving = false;

                //等一下要移动的距离
                int deltaX = 0;
                int upScrollx = mItemLayout.getScrollX();
                //滑动距离大于1/2menu长度就自动展开，否则就关掉
                if (upScrollx >= mMaxLength / 2) {
                    deltaX = mMaxLength - upScrollx;
                    mMenuState = MENU_WILL_OPEN;
                } else if (upScrollx < mMaxLength / 2) {
                    deltaX = -upScrollx;
                    mMenuState = MENU_WILL_CLOSED;
                }
                //知道我们为什么不直接把mMenuState赋值为MENU_OPEN或者MENU_CLOSED吗？因为滑动时有时间的，我们可以在滑动完成时才把状态改为已经完成
                mScroller.startScroll(upScrollx, 0, deltaX, 0, 200);
                isStartScroll = true;
                //刷新界面
                invalidate();
                break;
```

之后还要在onTouchEvent方法里刷新坐标

```
		//只有更新mLastX，mLastY数据才会准确
        mLastX = x;
        mLastY = y;
        return super.onTouchEvent(e);
```

##**因为我们用到了startScroll所以要重写computeScroll方法**

```
    public void computeScroll() {
        //判断scroller是否完成滑动
        if (mScroller.computeScrollOffset()) {
            mItemLayout.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            //这个很重要
            invalidate();
        //如果已经完成就改变状态
        } else if (isStartScroll) {
            isStartScroll = false;
            if (mMenuState == MENU_WILL_CLOSED) {
                mMenuState = MENU_CLOSED;
            }
            if (mMenuState == MENU_WILL_OPEN) {
                mMenuState = MENU_OPEN;
            }
        }
    }
```

##**还要监听RecyclerView是否在上下滑动

```
 @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        //是否在上下滑动
        isDragging = state == SCROLL_STATE_DRAGGING;
    }
```
##**还要设置Listener**

```
//设置Listener
    public void setOnItemActionListener(OnItemActionListener onItemActionListener) {
        this.mListener = onItemActionListener;
    }
```

这个Listener是要自己新建的

```
public interface OnItemActionListener {
    void OnItemClick(int position);
    void OnItemTop(int position);
    void OnItemDelete(int position);
}

```
##**最后是点击，置顶，删除在Activity里的回调**
这里只展示回调实现部分，我这里用的List是LinkedList，可以在第一位添加数据

```
rv.setOnItemActionListener(new OnItemActionListener() {
            //点击
            @Override
            public void OnItemClick(int position) {
                Toast.makeText(MainActivity.this,"Click"+position,Toast.LENGTH_SHORT).show();
            }
            //置顶
            @Override
            public void OnItemTop(int position) {
                //获得当前位置的内容
                String temp =list.get(position);
                //移除这个item
                list.remove(position);
                //把它添加到第一个
                list.addFirst(temp);
                //更新数据源
                adapter.notifyDataSetChanged();
            }
            //删除
            @Override
            public void OnItemDelete(int position) {
                list.remove(position);
                //更新数据源
                adapter.notifyDataSetChanged();
            }
        });
```

Adapter和布局的代码太简单我就不放出来了，大家可以到源码里看看有什么

##**效果图**
![效果图](http://img.blog.csdn.net/20160817171315331)






