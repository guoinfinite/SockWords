package com.example.sockword;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;

public class WrongActivity extends AppCompatActivity implements View.OnClickListener {

    //用来显示单词和音标的
    private TextView chinaText;
    private TextView wordText;
    private TextView englishText;

    private SharedPreferences sharedPreferences;            //定义轻量级数据库
    private SharedPreferences.Editor editor;                //数据库编辑器

    //播放声音
    private ImageView playVioce;
    private ImageView backBtn;

    //“我会了”按钮
    private Button iKnowBtn;

    //合成对象
    private SpeechSynthesizer speechSynthesizer;

    //定义数据库
    private SQLiteDatabase db;
    // 数据库管理者
    private DaoMaster mDaoMaster;
    // 与数据库进行会话
    private DaoSession mDaoSession;
    // 对应的表,由java代码生成的,对数据库内相应的表操作使用此对象
    private CET4EntityDao questionDao;

    /**
     * 手指按下的点为（x1,y1）
     * 手指离开屏幕的点为（x2,y2）
     */
    private float x1 = 0;
    private float y1 = 0;
    private float x2 = 0;
    private float y2 = 0;

    //定义一个list泛型为CET4Entity
    private List<CET4Entity> datas;
    private List<CET4Entity> wrongData;
    private int wrongNum = 0;                   //定义一个int型数据
    private int[] wrongArry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_wrong);      //绑定布局文件
        init();
        //设置错题
        setData(wrongNum);
//        setWrongData();
        //初始化语音播报
        SpeechUtility speechUtility = SpeechUtility.createUtility(WrongActivity.this, SpeechConstant.APPID + "=5e23e591");
        speechSynthesizer = SpeechSynthesizer.createSynthesizer(WrongActivity.this, new InitListener() {
            @Override
            public void onInit(int i) {
            }
        });
        setParam();
    }


    private void init() {
        //初始化数据库
        sharedPreferences = getSharedPreferences("share", Context.MODE_PRIVATE);
        //初始化编辑器
        editor = sharedPreferences.edit();

        //汉语绑定id
        chinaText = (TextView) findViewById(R.id.china_text);
        //音标绑定id
        englishText = (TextView) findViewById(R.id.english_text);
        //单词绑定id
        wordText = (TextView) findViewById(R.id.word_text);
        //播放声音按钮绑定id
        playVioce = (ImageView) findViewById(R.id.play_vioce);
        // 播放声音设置监听事件
        playVioce.setOnClickListener(this);
        //“我会了”按钮绑定id
        iKnowBtn = (Button) findViewById(R.id.i_know_btn);
        //“我会了”按钮设置监听事件
        iKnowBtn.setOnClickListener(this);
        //返回按钮绑定id
        backBtn = (ImageView) findViewById(R.id.back_btn);
        //返回按钮设置监听事件
        backBtn.setOnClickListener(this);
        // 通过管理对象获取数据库
        // 对数据库进行操作
        // 此DevOpenHelper类继承自SQLiteOpenHelper,第一个参数Context,第二个参数数据库名字,第三个参数CursorFactory
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "wrong.db", null);
        db = helper.getWritableDatabase();
        mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
        questionDao = mDaoSession.getCET4EntityDao();
    }

//    /**
//     * 从数据库里面取出数据
//     * 并设置到数组里面
//     * */
//    private void setWrongData() {
//        wrongData = new ArrayList<>();
//        String str = sharedPreferences.getString("wrongId", "").substring(1);
//        String[] strArray = str.split(",");
//        wrongArry = new int[strArray.length];
//        for (int i = 0; i < strArray.length; i++) {
//            try {
//                wrongArry[i] = Integer.parseInt(strArray[i]);
//            }catch (Exception e){
//            }
//
//
//        }
//        for (int i = 0; i < wrongArry.length; i++) {
//            wrongData.add(i, datas.get(wrongArry[i]));
//        }
//        setData(wrongNum);
//
//    }


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

    /**
     * 初始化语音播报
     */
    public void setParam() {
        speechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        speechSynthesizer.setParameter(SpeechConstant.SPEED, "50");
        speechSynthesizer.setParameter(SpeechConstant.VOLUME, "50");
        speechSynthesizer.setParameter(SpeechConstant.PITCH, "50");
    }

    /**
     * 设置错题
     */
    private void setData(int j) {
        //“我会了”按钮显示出来
        iKnowBtn.setVisibility(View.VISIBLE);
        //初始化list
        wrongData = new ArrayList<>();
        if (questionDao.queryBuilder().list() != null
                && questionDao.queryBuilder().list().size() > 0
                && j < questionDao.queryBuilder().list().size()
                && j >= 0) {            //判断如果数据库不为空

            for (int i = 0; i < questionDao.queryBuilder().list().size(); i++) {
                wrongData.add(i, questionDao.queryBuilder().list().get(i));         //把数据循环加到list里面
            }

            /**
             * 分别将list里面的数据取出第j条数据设置单词音标以及汉语
             * */
            wordText.setText(wrongData.get(j).getWord());
            englishText.setText(wrongData.get(j).getEnglish());
            chinaText.setText(wrongData.get(j).getChina());

        } else {
            /**
             * 如果数据库为空
             * 隐藏“我会了”按钮
             * */
            wordText.setText("好厉害");
            englishText.setText("[没有]");
            chinaText.setText("没有不会的单词了");
            iKnowBtn.setVisibility(View.GONE);
        }

    }

    /**
     * 复写activity的onTouch方法
     * 监听滑动事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //当手指按下的时候
            x1 = event.getX();
            y1 = event.getY();
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            //当手指离开的时候
            x2 = event.getX();
            y2 = event.getY();
            //向左滑
            if (x1 - x2 > 200) {
                //判断如果数据库里面没有数据了
                if (wrongNum + 2 > questionDao.queryBuilder().list().size()) {
                    Toast.makeText(this, "没有更多的数据了", Toast.LENGTH_SHORT).show();
                    //否则有数据进行数据设置
                } else {
                    //定义的int行数据+1
                    wrongNum++;
                    //设置数据
                    setData(wrongNum);
                }

                //向右滑
            } else if (x2 - x1 > 200) {
                //判断是不是第一条数据
                if (wrongNum - 1 < 0) {
                    Toast.makeText(this, "没有更多的数据了", Toast.LENGTH_SHORT).show();
                    // 如果不是
                } else {
                    //定义的int数据-1
                    wrongNum--;
                    //设置数据
                    setData(wrongNum);
                }
            }
        }
        return super.onTouchEvent(event);
    }


    /**
     * 点击事件
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //播放单词声音
            case R.id.play_vioce:
                //获取文本
                String text = wordText.getText().toString();
                //传给后台
                speechSynthesizer.startSpeaking(text, listener);
                break;
            //我会了  按钮的点击操作
            case R.id.i_know_btn:
                if (wrongNum == 0) {
                    //从数据库删除该条数据
                    questionDao.deleteByKey(questionDao.queryBuilder().list().get(wrongNum).getId());
                    //刷新数据
                    setData(wrongNum++);
                } else if (wrongNum == questionDao.queryBuilder().list().size()) {
                    questionDao.deleteByKey(questionDao.queryBuilder().list().get(wrongNum).getId());
                    setData(wrongNum--);
                } else {
                    questionDao.deleteByKey(questionDao.queryBuilder().list().get(wrongNum).getId());
                    setData(wrongNum--);
                }
                break;
            //返回按钮
            case R.id.back_btn:
                //返回上一个页面
                finish();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
