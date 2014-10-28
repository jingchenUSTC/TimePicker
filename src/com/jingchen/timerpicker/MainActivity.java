package com.jingchen.timerpicker;

import java.util.ArrayList;
import java.util.List;

import com.jingchen.timerpicker.PickerView.onSelectListener;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 更多详解见博客http://blog.csdn.net/zhongkejingwang/article/details/38513301
 * 
 * @author chenjing
 * 
 */
public class MainActivity extends Activity
{

	PickerView minute_pv;
	PickerView second_pv;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		minute_pv = (PickerView) findViewById(R.id.minute_pv);
		second_pv = (PickerView) findViewById(R.id.second_pv);
		List<String> data = new ArrayList<String>();
		List<String> seconds = new ArrayList<String>();
		for (int i = 0; i < 10; i++)
		{
			data.add("0" + i);
		}
		for (int i = 0; i < 60; i++)
		{
			seconds.add(i < 10 ? "0" + i : "" + i);
		}
		minute_pv.setData(data);
		minute_pv.setOnSelectListener(new onSelectListener()
		{

			@Override
			public void onSelect(String text)
			{
				Toast.makeText(MainActivity.this, "选择了 " + text + " 分",
						Toast.LENGTH_SHORT).show();
			}
		});
		second_pv.setData(seconds);
		second_pv.setOnSelectListener(new onSelectListener()
		{

			@Override
			public void onSelect(String text)
			{
				Toast.makeText(MainActivity.this, "选择了 " + text + " 秒",
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
