package com.moor.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.moor.im.ui.fragment.intro.FirstIntroFragment;
import com.moor.im.ui.fragment.intro.SecondIntroFragment;
import com.moor.im.ui.fragment.intro.ThirdIntroFragment;
import com.moor.im.ui.view.intro.AppIntro2;

/**
 * Created by long on 2015/7/8.
 * 引导页
 */
public class IntroActivity extends AppIntro2{


    @Override
    public void init(Bundle savedInstanceState) {
        addSlide(new FirstIntroFragment(), getApplicationContext());
        addSlide(new SecondIntroFragment(), getApplicationContext());
        addSlide(new ThirdIntroFragment(), getApplicationContext());
    }

    private void loadLoginActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDonePressed() {
        loadLoginActivity();
    }

}
