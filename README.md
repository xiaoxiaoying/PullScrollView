PullScrollView
===========

*注：本项目使用Android Studio开发*
*在*https://github.com/MarkMjw/PullScrollView*基础上修改*




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

##Screenshots
![Screenshot 0](https://github.com/xiaoxiaoying/PullScrollView/blob/master/Screenshots/pullscrollView.gif)


[Download apk](https://github.com/xiaoxiaoying/NumberPicker/blob/master/samples/bin/SampleActivity.apk)
License
=======

    Copyright (C) 2014 MarkMjw

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
