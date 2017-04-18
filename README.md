PullScrollView
===========

*注：本项目使用Android Studio开发*
*在*https://github.com/MarkMjw/PullScrollView *基础上修改*




## **本例包含ScrollView的两种实现：** ##
>1.仿照新浪微博Android客户端个人中心的ScrollView，下拉背景伸缩回弹效果。<br>
>2.ScrollView仿IOS回弹效果。<br>

## **使用示例** ##

```java
mScrollView = (PullScrollView) findViewById(R.id.scroll_view);
mHeadImg = (ImageView) findViewById(R.id.background_img);
mScrollView.setOnTurnListener(this);
mScrollView.init(mHeadImg);
```

## Screenshots
![Screenshot 0](https://github.com/xiaoxiaoying/PullScrollView/blob/master/Screenshots/pullscrollView.gif)


[Download apk](https://github.com/xiaoxiaoying/PullScrollView/blob/master/Screenshots/PullScrollView-debug.apk)
