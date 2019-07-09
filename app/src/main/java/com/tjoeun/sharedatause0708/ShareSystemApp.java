package com.tjoeun.sharedatause0708;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class ShareSystemApp extends AppCompatActivity {
    Button contactBtn;
    Button gallaryBtn;
    LinearLayout content;

    //화면 전체 크기의 너비와 높이를 저장할 변수
    int reqWidth;
    int reqHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_system_app);


        //자신의 전화번호
        //TelephonyManager telephony = (TelephonyManager)getSystemService(TELEPHPNY_SERVICE);
        //String phoneNumber = telephony.getLine1Number();



        contactBtn = (Button)findViewById(R.id.contact);
        gallaryBtn = (Button)findViewById(R.id.gallary);
        content = (LinearLayout)findViewById(R.id.content);

        //현재 디바이스의 전체 화면 크기 가져오기
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        //이렇게 쓰는 것은 대부분 스마트폰
        reqWidth = metrics.widthPixels;
        reqHeight = metrics.heightPixels;

        //동적으로 권한 요청하기 - 연락처 접근을 위한 확
        if(ContextCompat.checkSelfPermission(
                ShareSystemApp.this,
                Manifest.permission.READ_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(ShareSystemApp.this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    100);
        }

        //외부 저장 장치 읽기
        if(ContextCompat.checkSelfPermission(
                ShareSystemApp.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(ShareSystemApp.this,
                               Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                               PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    ShareSystemApp.this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    200);

        }


        //연락처 버튼을 눌러서 연락처 화면을 출력하도록 작성
        contactBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                //연락처 Intent 생성
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(Uri.parse("content://com.android.contacts/data/phones"));
                //시스템앱은 여기가 항상 고정되어있다.
                //intent 출력하고 10번으로 구분해서
                //데이터를 넘겨받을 수 있도록 Intent를 출력
                startActivityForResult(intent, 10);
            }
        });

        //갤러리 버튼을 누르면 동작하는 코드
        gallaryBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                //갤러리 앱을 화면에 출력
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 30);
                /*
                Uri uri = Uri.parse("smsto:0000-0000");
                Intent smsIntent = new Intent(Intent.ACTION_SENDTO, uri);
                smsIntent.putExtra("sms_body", "hello world!!");
                startActivity(smsIntent);
                 */

            }
        });
    }

    @Override
    public void onActivityResult(
            int requestCode, int resultCode, Intent data){

        //연락처 Intent가 닫혔을 때
        if(requestCode == 10){
            //선택한 데이터의 id찾아오기
            String id = Uri.parse(data.getDataString()).getLastPathSegment();

            Cursor cursor = getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    new String[]{ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER},
                    ContactsContract.Data._ID+"="+id,
                    null, null);
            cursor.moveToNext();
            String name = cursor.getString(0);
            String phone = cursor.getString(1);

            //TextView를 동적으로 생성해서 content에 추가
            TextView textView = new TextView(ShareSystemApp.this);
            textView.setText(name +":" + phone);
            textView.setTextSize(25);
            //크기 설정
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            content.addView(textView);
        }

        else if(requestCode == 30 && resultCode == RESULT_OK){
            //Log.e("버튼 클릭후 결과", "메시지");
            //선택한 데이터가 있다면
            Log.e("넘어온 데이터", data.toString());
            if(data.getClipData() != null){
                Log.e("데이터", data.getClipData().toString());
                ClipData clipData = data.getClipData();
                for(int i=0; i<clipData.getItemCount(); i=i+1){
                    ClipData.Item item = clipData.getItemAt(i);
                    Uri uri = item.getUri();
                    String filePath = getFilePathFromDocumentUri(
                            ShareSystemApp.this, uri);
                    Log.e("filePath", filePath);
                    if(filePath != null){
                        insertImage(filePath);
                    }
                }
            }

        }

    }

    //이미지 파일의 경로를 주면 이미지를 읽어서 이미지 뷰에 출력해주는 메소드
    private void insertImage(String filePath){
        if(filePath.equals("") == false){
            //파일 경로를 가지고 파일 객체 생성
            File file = new File(filePath);
            //이미지 옵션 객체 생성
            BitmapFactory.Options options =
                    new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try{
                InputStream is = new FileInputStream(file);
                BitmapFactory.decodeStream(is, null, options);
                is.close();
                is = null;
            }catch (Exception e){
                Log.e("이미지 디코딩 실패", e.getMessage());
            }

            //이미지 사이즈를 설정
            final int width = options.outWidth;
            int inSampleSize = 1;
            if(width > reqWidth){
                int widthRatio =
                        Math.round((float)width/(float)reqWidth);
                inSampleSize = widthRatio;
            }

            //가져올 이미지의 옵션을 설정할 옵션
            BitmapFactory.Options imageOptions = new BitmapFactory.Options();
            imageOptions.inSampleSize = inSampleSize;
                    //이미지 읽어오기
                    Bitmap bitmap = BitmapFactory.decodeFile(
                            filePath, imageOptions);
                    //이미지 뷰룰 생성해서 추가하기
            ImageView imageView =
                    new ImageView(ShareSystemApp.this);
            imageView.setImageBitmap(bitmap);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            content.addView(imageView);
        }
    }

    //선택한 이미지 파일의 Uri를 가지고 이미지 파일의 경로를 문자열 리턴해주는 메소드
    //4.4(KitKat) 버전 이상에서 사용
    private String getFilePathFromDocumentUri(
            Context context, Uri uri){
        //선택한 이미지의 id 찾기
        String docId = DocumentsContract.getDocumentId(uri);
        //이미지 파일의 아이디는 image:id로 구성
        //:을 기준으로 분할
        String[] split = docId.split(":");
        String type = split[0];
        Uri contentUri = null;
        //상수와 변수를 비교할 때 상수를 기준으로 비교하는 것이 좋습니다.
        //변수를 가지고 비교하게 되면 NullPointerException이
        //발생할 수 있지만 상수를 가지고 비교하면 NullPointerException이
        //발생하지 않습니다.
        //결과가 Null이란 말이 아님 "image"부분이 Null이라는 말임
        // a == 3 X
        // 3 == a O
        if("image".equals(type)){
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        //실제 이미지 파일의 경로 만들기
        String selection = MediaStore.Images.Media._ID+"=?";
        String [] selectionArg = new String[]{split[1]};
        String column = "_data";
        String [] projection = {column};
        //ContentProvider에서 데이터 가져오기
        Cursor cursor =context.getContentResolver().query(
                contentUri, projection,
                selection, selectionArg, null);
                String filePath = null;
                if(cursor != null && cursor.moveToFirst()){
                    int column_index = cursor.getColumnIndexOrThrow(column);
                    filePath = cursor.getString(column_index);
                }
                cursor.close();
                return filePath;


    }


}
