First, change your XML animation code to this:

<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:interpolator="@android:anim/accelerate_decelerate_interpolator"
    android:fillAfter="false">

    <translate
        android:fromYDelta="0%p"
        android:toYDelta="100%p"
        android:duration="1000"
        android:repeatCount="infinite"
        android:repeatMode="restart"
        />
</set>
Your layout should be something like this:

<LinearLayout
        android:id="@+id/image"
        android:layout_width="200dp"
        android:layout_height="200dp"
        android:background="@mipmap/ic_launcher">

        <View
            android:id="@+id/bar"
            android:layout_width="200dp"
            android:layout_height="6dp"
            android:visibility="gone"
            android:background="@android:color/black"
            android:src="@mipmap/ic_launcher"/>
    </LinearLayout>
And in your activity, the code can be like this:

LinearLayout imageView = (LinearLayout) findViewById(R.id.image);
        final View bar = findViewById(R.id.bar);
        final Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                bar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                
                bar.setVisibility(View.VISIBLE);
                bar.startAnimation(animation);
                return false;
            }
        });