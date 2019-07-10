package com.tjoeun.smartphonemodule;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

import java.util.List;

public class SensorDataActivity extends AppCompatActivity {
    TextView sensorName, sensorAccurracy, sensorValue;

    //센서 목록을 가져오기 위한 변수
    SensorManager manager;
    List<Sensor> list;

    //센서 이름을 저장할 변수
    String sName;

    //상위 Activity에서 넘겨받을 센서 번호를 저장할 변수
    int sensorIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_data);

        sensorName = (TextView)findViewById(R.id.sensorname);
        sensorAccurracy = (TextView)findViewById(R.id.sensoraccurracy);
        sensorValue = (TextView)findViewById(R.id.sensorvalue);

        //센서 목록 기져오기
        //안드로이드 내장 객체들을 만들 때 자주 이용
        manager = (SensorManager)getSystemService(SENSOR_SERVICE);
        list = manager.getSensorList(Sensor.TYPE_ALL);

        //상위 Activity 클래스에서 넘겨준 데이터를 가지고 센서 이름을 찾아와서
        //텍스트 뷰에 출력하기
        Intent intent = getIntent();
        sensorIndex = intent.getIntExtra("sensorIndex", -1);
        Sensor sensor = list.get(sensorIndex);
        sName = sensor.getName();
        sensorName.setText(sName);


    }

    //센서의 값이 변경되거나 정밀도가 변경되는 이벤트를 처리하는 이벤트 핸들러
    //외부에 만든 이유는 등록과 해제를 하기위해서
    SensorEventListener eventHandler =
            new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    String msg = "측정시간:" + sensorEvent.timestamp +
                            "\n";
                    //센서의 값 출력
                    for(int idx = 0; idx<sensorEvent.values.length; idx=idx+1){
                        msg += (idx+1) + ":" + sensorEvent.values[idx] + "\n";
                    }
                    sensorValue.setText(msg);

                }

                //센서의 정밀도가 변경된 경우 호출되는 메소드
               @Override
                public void onAccuracyChanged(Sensor sensor, int i) {
                    //정밀도에 따른 문자열 출력
                   switch (i){
                       case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                           sensorAccurracy.setText("정밀도 높음");
                       break;

                       case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                           sensorAccurracy.setText("정밀도 보통");
                       break;

                       case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                           sensorAccurracy.setText("정밀도 낮음");
                           break;

                       case SensorManager.SENSOR_STATUS_UNRELIABLE:
                           sensorAccurracy.setText("신뢰할 수 없음");
                           break;
                   }

                }
            };

       //Activity가 화면에 출력될 때 호출되는 메소드
       @Override
       public void onResume(){
           super.onResume();
           //센서 리스너 등록
           manager.registerListener(eventHandler,
                   list.get(sensorIndex),
                   SensorManager.SENSOR_DELAY_UI);
       }

       //Activity가 화면에서 제거될 때 호출되는 메소드
       @Override
       public void onPause(){
           super.onPause();
           manager.unregisterListener(eventHandler);
       }

       //화면 터치 처리 메소드
       @Override
       public boolean onTouchEvent(MotionEvent event) {
           finish();
           return super.onTouchEvent(event);
       }
}
