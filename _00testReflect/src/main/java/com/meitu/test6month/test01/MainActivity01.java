package com.meitu.test6month.test01;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.meitu.test6month.R;

import java.lang.reflect.Field;

public class MainActivity01 extends AppCompatActivity {

    private static final String TAG = "MainActivity01";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //  通过对象设置private修饰的属性  by shaowenwen 2020-06-12
        Object main = new Main01();

        Class<?> clazz = main.getClass();
        try {
            Log.e(TAG, "onCreate() called with: 修改前，number = [" + ((BaseMain01) main).getNumber() + "]");
            // 异常捕获：NoSuchFieldException；【getField 获取this可访问的变量】
            Field field = clazz.getField("number");
            field.setAccessible(true);
            // 异常捕获：IllegalAccessException
            field.set(main, 2020);
            Log.e(TAG, "onCreate() called with: 修改后，number = [" + main.getClass() + "]");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();

            try {
                //【getField 获取this可访问的所有变量】
                Field field = clazz.getDeclaredField("number");
                field.setAccessible(true);
                // 异常捕获：IllegalAccessException
                field.set(main, 2020);
                Log.e(TAG, "onCreate() called with: 修改后，number = [" + main.getClass() + "]");
            } catch (NoSuchFieldException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
            /*for (Class<?> childClazz = clazz; childClazz != null; childClazz = clazz.getSuperclass()) {
                try {
                    Field field = childClazz.getDeclaredField("number");
                    field.setAccessible(true);
                    // 异常捕获：IllegalAccessException
                    field.set(main, 2020);
                    Log.e(TAG, "onCreate() called with: 修改后，number = [" + ((BaseMain01) main).getNumber() + "]");
                    break;
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                } catch (NoSuchFieldException ex) {
                    ex.printStackTrace();
                }
            }
*/
        }
    }

}
