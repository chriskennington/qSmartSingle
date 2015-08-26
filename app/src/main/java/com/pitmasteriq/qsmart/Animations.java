package com.pitmasteriq.qsmart;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

public class Animations extends Animation
{
	public Animations()
	{
	}
	
	public static Animation getBlinkAnimation()
	{
		Animation anim = new AlphaAnimation(0.0f, 1.0f);
		anim.setDuration(50); //You can manage the time of the blink with this parameter
		anim.setStartOffset(20);
		anim.setRepeatMode(Animation.REVERSE);
		anim.setRepeatCount(Animation.INFINITE);
		return anim;
	}
	
	public static Animation getBlinkAnimation( long time)
	{
		Animation anim = new AlphaAnimation(0.0f, 1.0f);
		anim.setDuration(time); //You can manage the time of the blink with this parameter
		anim.setStartOffset(20);
		anim.setRepeatMode(Animation.REVERSE);
		anim.setRepeatCount(Animation.INFINITE);
		return anim;
	}
	
	public static Animation getPulseAnimation( long time)
	{
		Animation anim = new AlphaAnimation(0.5f, 1.0f);
		anim.setDuration(time); //You can manage the time of the blink with this parameter
		anim.setStartOffset(20);
		anim.setRepeatMode(Animation.REVERSE);
		anim.setRepeatCount(Animation.INFINITE);
		return anim;
	}
}
