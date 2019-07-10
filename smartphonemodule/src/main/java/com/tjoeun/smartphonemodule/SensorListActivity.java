package com.tjoeun.smartphonemodule;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

public class SensorListActivity extends AppCompatActivity {

    ListView listView;
    List<Sensor> list;
    SensorListAdapter adapter;

    SensorManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_list);

        //센서 목록 가져오기
        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        list = manager.getSensorList(Sensor.TYPE_ALL);
        Log.e("데이터목록", list.toString());
        //가져온 list로 adapter 만들기
        adapter = new SensorListAdapter(
                SensorListActivity.this,
                R.layout.customcell, list);
        //ListView를 만들고 연결하기
        listView = (ListView) findViewById(R.id.sensorlist);

        listView.setAdapter(adapter);

        //리스트 뷰의 항목을 클릭했을 때의 이벤트 처리
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        //하위 액티비티에 해당하는 인텐트를 만들고
                        //sensorIndex라는 이름으로 선택한 항목의 인덱스를 저장하고
                        //화면에 출력

                        Intent intent = new Intent(
                                SensorListActivity.this,
                                SensorDataActivity.class);
                        intent.putExtra("sensorIndex",i);
                        startActivity(intent);
                    }
                }
        );

    }
}
