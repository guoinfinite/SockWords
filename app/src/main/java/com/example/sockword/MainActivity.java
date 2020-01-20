package com.example.sockword;

import androidx.appcompat.app.AppCompatActivity;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.assetsbasedata.AssetsDatabaseManager;
import com.example.greendao.entity.greendao.CET4Entity;
import com.example.greendao.entity.greendao.CET4EntityDao;
import com.example.greendao.entity.greendao.DaoMaster;
import com.example.greendao.entity.greendao.DaoSession;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "MainActivity";
    /*用于显示单词和音标*/
    private TextView timeText;
    private TextView dateText;
    private TextView wordText;
    private TextView englishText;

    /*播放声音*/
    private ImageView playVioce;

    /*显示时间*/
    private String month;
    private String day;
    private String way;
    private String hours;
    private String minute;

    /*合成图像*/
    //TODO:
    private SpeechSynthesizer speechSynthesizer;

    /*锁屏*/
    private KeyguardManager keyguardManager;
    private KeyguardManager.KeyguardLock keyguardLock;

    /*加载单词的三个选项*/
    private RadioGroup radioGroup;

    /*单词的三个意思*/
    private RadioButton radioOne;
    private RadioButton radioTwo;
    private RadioButton radioThree;

    /*轻量级数据库*/
    private SharedPreferences sharedPreferences;
    /*编辑数据库*/
    private SharedPreferences.Editor editor = null;

    /*记录答了几道题*/
    private int j = 0;
    /*判断题的数目*/
    private List<Integer> list;

    /*读取词库*/
    private List<CET4Entity> datas;
    int k;

    /*手指按下时坐标(x1,y1)
     * 手指离开屏幕市坐标(x2,y2)*/
    private float x1 = 0;
    private float y1 = 0;

    private float x2 = 0;
    private float y2 = 0;

    /*创建数据库*/
    private SQLiteDatabase database;
    /*管理者*/
    private DaoMaster daoMaster, dbMaster;

    /*和数据库进行会话*/
    private DaoSession daoSession, dbSession;

    /*对应的表*/
    private CET4EntityDao questionDao, dbDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_main);
        init();
    }

    /*初始化控件*/
    public void init() {
        sharedPreferences = getSharedPreferences("share", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        list = new ArrayList<Integer>();

        Random random = new Random();
        int i;
        while (list.size() < 10) {
            i = random.nextInt(20);
            if (!list.contains(i)) {
                list.add(i);
            }
        }
        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        keyguardLock = keyguardManager.newKeyguardLock("unLock");

        /*初始化，只需要调用一次*/
        AssetsDatabaseManager.initManager(this);
        AssetsDatabaseManager manager = AssetsDatabaseManager.getManager();

        SQLiteDatabase database1 = manager.getDatabase("word.db");

        daoMaster = new DaoMaster(database1);
        daoSession = daoMaster.newSession();
        questionDao = daoSession.getCET4EntityDao();

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "wrong.db", null);

        database = helper.getWritableDatabase();
        dbMaster = new DaoMaster(database);
        dbSession = dbMaster.newSession();
        dbDao = dbSession.getCET4EntityDao();

        timeText = (TextView) findViewById(R.id.time_text);
        dateText = (TextView) findViewById(R.id.date_text);
        wordText = (TextView) findViewById(R.id.word_text);
        englishText = (TextView) findViewById(R.id.english_text);
        playVioce = (ImageView) findViewById(R.id.play_voice);
        playVioce.setOnClickListener(this);
        radioGroup = (RadioGroup) findViewById(R.id.choose_group);
        radioOne = (RadioButton) findViewById(R.id.choose_btn_one);
        radioTwo = (RadioButton) findViewById(R.id.choose_btn_two);
        radioThree = (RadioButton) findViewById(R.id.choose_btn_three);
        radioGroup.setOnCheckedChangeListener(this);
        //TODO:到讯飞平台申请
        SpeechUtility speechUtility = SpeechUtility.createUtility(MainActivity.this, SpeechConstant.APPID + "=5e23e591");
        speechSynthesizer = SpeechSynthesizer.createSynthesizer(MainActivity.this, new InitListener() {
            @Override
            public void onInit(int i) {
                Log.d(TAG, "onInit: success" + SpeechSynthesizer.getSynthesizer());
            }
        });
        setParam();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Calendar calendar = Calendar.getInstance();
        month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        way = String.valueOf(calendar.get(Calendar.DAY_OF_WEEK));

        if (calendar.get(Calendar.HOUR_OF_DAY) < 10) {
            hours = "0" + calendar.get(Calendar.HOUR_OF_DAY);
        } else {
            hours = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        }

        if (calendar.get(Calendar.MINUTE) < 10) {
            minute = "0" + calendar.get(Calendar.MINUTE);
        } else {
            minute = String.valueOf(calendar.get(Calendar.MINUTE));
        }

        if ("1".equals(way)) {
            way = "天";
        } else if ("2".equals(way)) {
            way = "一";
        } else if ("3".equals(way)) {
            way = "二";
        } else if ("4".equals(way)) {
            way = "三";
        } else if ("5".equals(way)) {
            way = "四";
        } else if ("6".equals(way)) {
            way = "五";
        } else if ("7".equals(way)) {
            way = "六";
        }
        timeText.setText(hours + ":" + minute);
        dateText.setText(month + "月" + day + "日" + "  " + "星期" + way);
        BaseApplication.addDestroyActivity(this, "MainActivity");
        getDBData();
    }

    /*将错题存储到数据库*/
    private void saveWrongData() {
        String word = datas.get(k).getWord();
        String english = datas.get(k).getEnglish();
        String china = datas.get(k).getChina();
        String sign = datas.get(k).getSign();
        CET4Entity data = new CET4Entity(Long.valueOf(dbDao.count()), word, english, china, sign);
        dbDao.insertOrReplace(data);
    }

    /*设置选项的不同颜色*/
    private void btnGetText(String msg, RadioButton btn) {
        /*答对设置绿色，答错红色*/
        if (msg.equals(datas.get(k).getChina())) {
            wordText.setTextColor(Color.GREEN);
            englishText.setTextColor(Color.GREEN);
            btn.setTextColor(Color.GREEN);
        } else {
            wordText.setTextColor(Color.RED);
            englishText.setTextColor(Color.RED);
            btn.setTextColor(Color.RED);
            saveWrongData();
            int wrong = sharedPreferences.getInt("wrong", 0);
            editor.putInt("wrong", wrong + 1);
            editor.putString("wrongId", "," + datas.get(j).getId());
            editor.commit();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_voice:
                String text = wordText.getText().toString();
                speechSynthesizer.startSpeaking(text, listener);
                break;
            default:
                break;
        }
    }

    SynthesizerListener listener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {

        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {

        }

        @Override
        public void onSpeakPaused() {

        }

        @Override
        public void onSpeakResumed() {

        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {

        }

        @Override
        public void onCompleted(SpeechError speechError) {

        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };


    /*初始化语音播报*/
    public void setParam() {
        speechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "50");
        speechSynthesizer.setParameter(SpeechConstant.SPEED, "5");
        speechSynthesizer.setParameter(SpeechConstant.VOLUME, "50");
        speechSynthesizer.setParameter(SpeechConstant.PITCH, "50");
    }

    @Override
    /*选项的点击事件*/
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        radioGroup.setClickable(false);
        switch (checkedId) {
            case R.id.choose_btn_one:
                String msg = radioOne.getText().toString().substring(3);
                btnGetText(msg, radioOne);
                break;
            case R.id.choose_btn_two:
                String msg1 = radioTwo.getText().toString().substring(3);
                btnGetText(msg1, radioTwo);
                break;
            case R.id.choose_btn_three:
                String msg2 = radioThree.getText().toString().substring(3);
                btnGetText(msg2, radioThree);
                break;
        }
    }

    /*还原单词与选项的颜色*/
    private void setTextColor() {
        radioOne.setChecked(false);
        radioTwo.setChecked(false);
        radioThree.setChecked(false);
        radioOne.setTextColor(Color.parseColor("#FFFFFF"));
        radioTwo.setTextColor(Color.parseColor("#FFFFFF"));
        radioThree.setTextColor(Color.parseColor("#FFFFFF"));
        wordText.setTextColor(Color.parseColor("#FFFFFF"));
        englishText.setTextColor(Color.parseColor("#FFFFFF"));
    }

    /*解锁*/
    private void unlocked() {
        Intent intent1 = new Intent(Intent.ACTION_MAIN);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent1.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent1);
        keyguardLock.disableKeyguard();
        finish();
    }

    /*设置选项*/
    private void setChina(List<CET4Entity> datas, int j) {
        Random random = new Random();
        List<Integer> integerList = new ArrayList<>();
        int i;
        while (integerList.size() < 4) {
            i = random.nextInt(20);
            if (!integerList.contains(i)) {
                integerList.add(i);
            }
        }

        if (integerList.get(0) < 7) {
            radioOne.setText("A: " + datas.get(k).getChina());
            if (k - 1 >= 0) {
                radioTwo.setText("B: " + datas.get(k - 1).getChina());
            } else {
                radioTwo.setText("B: " + datas.get(k + 2).getChina());
            }

            if (k + 1 < 20) {
                radioThree.setText("C: " + datas.get(k + 1).getChina());
            } else {
                radioThree.setText("C: " + datas.get(k - 1).getChina());
            }
        } else if (integerList.get(0) < 14) {
            radioTwo.setText("B: " + datas.get(k).getChina());
            if (k - 1 >= 0) {
                radioOne.setText("A: " + datas.get(k - 1).getChina());
            } else {
                radioOne.setText("A: " + datas.get(k + 2).getChina());
            }

            if (k + 1 < 20) {
                radioThree.setText("C: " + datas.get(k + 1).getChina());
            } else {
                radioThree.setText("C: " + datas.get(k - 1).getChina());
            }
        } else {
            radioThree.setText("C: " + datas.get(k).getChina());
            if (k - 1 >= 0) {
                radioTwo.setText("B: " + datas.get(k - 1).getChina());
            } else {
                radioTwo.setText("B: " + datas.get(k + 2).getChina());
            }

            if (k + 1 < 20) {
                radioOne.setText("A: " + datas.get(k + 1).getChina());
            } else {
                radioOne.setText("A: " + datas.get(k - 1).getChina());
            }
        }
    }

    /*获取数据库数据*/
    private void getDBData() {
        datas = questionDao.queryBuilder().list();
        k = list.get(j);
        wordText.setText(datas.get(k).getWord());
        englishText.setText(datas.get(k).getEnglish());
        setChina(datas, k);
    }

    /*复写activity的onTouch方法
     * 监听滑动事件*/
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /*手指按下*/
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            x1 = event.getX();
            y1 = event.getY();
        }

        /*手指离开*/
        if (event.getAction() == MotionEvent.ACTION_UP) {
            x2 = event.getX();
            y2 = event.getY();

            /*上滑*/
            if (y1 - y2 > 200) {
                int num = sharedPreferences.getInt("alreadyMastered", 0) + 1;
                editor.putInt("alreadyMastered", num);
                editor.commit();
                Toast.makeText(this, "已掌握", Toast.LENGTH_SHORT).show();
                getNextData();
                /*向下滑*/
            } else if (y2 - y1 > 200) {
                Toast.makeText(this, "待加功能.....", Toast.LENGTH_SHORT).show();
                /*x向左滑*/
            } else if (x1 - x2 > 200) {
                getNextData();
                /*向右滑*/
            } else if (x2 - x1 > 200) {
                unlocked();
            }
        }
        return super.onTouchEvent(event);
    }

    /*获取下一题*/
    private void getNextData() {
        j++;
        int i = sharedPreferences.getInt("allNum", 2);
        if (i > j) {
            getDBData();
            setTextColor();
            int num = sharedPreferences.getInt("alreadyStudy", 0) + 1;
            editor.putInt("alreadyStudy", num);
            editor.commit();
        } else {
            unlocked();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechSynthesizer.destroy();
    }
}
