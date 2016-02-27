package com.jingchen.timerpicker;

import android.app.Activity;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * 更多详解见博客http://blog.csdn.net/zhongkejingwang/article/details/38513301
 *
 * @author chenjing
 */
public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {

    private PickerView minute_pv;
    private PickerView second_pv;

    private PickerView value_pv;//值
    private PickerView unit_pv;//单位

    private String currentValue, currentUnit;

    private RadioGroup vRepeat, hRepeat;
    private RadioButton vpYes, hpYes, vpNo, hpNo;
    private UnitEmun currentUnitEmun;
    private List<String> values, units;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        showVerticalData();
        showHorizontalData();
    }

    private void showHorizontalData() {
        values = new ArrayList<String>();
        units = new ArrayList<String>();
        String [] us=getResources().getStringArray(R.array.units);
        for (int i=0;i<us.length;i++){
            units.add(us[i]);
        }
        unit_pv.setIsRepeat(true);
        unit_pv.setIsHorizontal(true);
        unit_pv.setData(units);
        unit_pv.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(int currentSelected) {
                setValueByUnit();
            }
        });

        setValueByUnit();

        value_pv.setIsRepeat(true);
        value_pv.setIsHorizontal(true);

        value_pv.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(int currentSelected) {
                currentValue=value_pv.getCurrentValue();
                Toast.makeText(MainActivity.this, "值 " + currentValue + " 单位 "+currentUnit, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setValueByUnit() {
        currentUnit = unit_pv.getCurrentValue();
        currentUnitEmun = UnitEmun.getUnitEmun(MainActivity.this, currentUnit);
        values.clear();
        for (int i = 0; i < currentUnitEmun.getLength(); i++) {
            values.add(String.valueOf(currentUnitEmun.getStart() + i * currentUnitEmun.getIncrement()));
        }
        value_pv.setData(values);
    }

    private void showVerticalData() {
        List<String> data = new ArrayList<String>();
        List<String> seconds = new ArrayList<String>();
        for (int i = 0; i < 10; i++) {
            data.add("0" + i);
        }
        for (int i = 0; i < 60; i++) {
            seconds.add(i < 10 ? "0" + i : "" + i);
        }
        minute_pv.setIsRepeat(true);
        minute_pv.setIsHorizontal(false);
        minute_pv.setData(data);
        minute_pv.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(int currentSelected) {
                Toast.makeText(MainActivity.this, "选择了 " + minute_pv.getCurrentValue() + " 分", Toast.LENGTH_SHORT).show();
            }
        });
        second_pv.setIsRepeat(true);
        second_pv.setIsHorizontal(false);
        second_pv.setData(seconds);
        second_pv.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(int currentSelected) {
                Toast.makeText(MainActivity.this, "选择了 " + second_pv.getCurrentValue() + " 秒", Toast.LENGTH_SHORT).show();
            }
        });
        minute_pv.setSelected(0);
    }

    private void initView() {
        minute_pv = (PickerView) findViewById(R.id.minute_pv);
        second_pv = (PickerView) findViewById(R.id.second_pv);
        value_pv = (PickerView) findViewById(R.id.pv_value);
        unit_pv = (PickerView) findViewById(R.id.pv_unit);

        vRepeat = (RadioGroup) findViewById(R.id.rg_v_repeat);
        hRepeat = (RadioGroup) findViewById(R.id.rg_h_repeat);

        vpYes = (RadioButton) findViewById(R.id.rb_v_yes);
        hpYes = (RadioButton) findViewById(R.id.rb_h_yes);
        vpNo = (RadioButton) findViewById(R.id.rb_v_no);
        hpNo = (RadioButton) findViewById(R.id.rb_h_no);

        vRepeat.setOnCheckedChangeListener(this);
        hRepeat.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i) {
            case R.id.rb_v_yes://纵向重复
                minute_pv.setIsRepeat(true);
                second_pv.setIsRepeat(true);
                break;
            case R.id.rb_v_no://纵向不重复
                minute_pv.setIsRepeat(false);
                second_pv.setIsRepeat(false);
                break;
            case R.id.rb_h_yes://横向重复
                value_pv.setIsRepeat(true);
                unit_pv.setIsRepeat(true);
                break;
            case R.id.rb_h_no://横向不重复
                value_pv.setIsRepeat(false);
                unit_pv.setIsRepeat(false);
                break;
        }
    }
}
