package com.example.sockword;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.assetsbasedata.AssetsDatabaseManager;
import com.example.greendao.entity.greendao.DaoMaster;
import com.example.greendao.entity.greendao.DaoSession;
import com.example.greendao.entity.greendao.WisdomEntity;
import com.example.greendao.entity.greendao.WisdomEntityDao;

import java.util.List;
import java.util.Random;

public class StudyFragment extends Fragment {
    private TextView difficultyTv;
    private TextView wisdomEnglish;
    private TextView wisdomChina;
    private TextView alreadyStudyText;
    private TextView alreadyMasteredText;
    private TextView wrongText;
    private SharedPreferences sharedPreferences;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private WisdomEntityDao qusetionDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.study_fragment_layout, null);
        sharedPreferences = getActivity().getSharedPreferences("share", Context.MODE_PRIVATE);
        difficultyTv = (TextView) view.findViewById(R.id.difficulty_text);
        wisdomEnglish = (TextView) view.findViewById(R.id.wisdom_english);
        wisdomChina = (TextView) view.findViewById(R.id.wisdom_china);
        alreadyMasteredText = (TextView) view.findViewById(R.id.already_mastered);
        alreadyStudyText = (TextView) view.findViewById(R.id.already_study);
        wrongText = (TextView) view.findViewById(R.id.wrong_text);

        AssetsDatabaseManager.initManager(getActivity());
        AssetsDatabaseManager manager = AssetsDatabaseManager.getManager();
        SQLiteDatabase database = manager.getDatabase("wisdom.db");
        daoMaster = new DaoMaster(database);
        daoSession = daoMaster.newSession();
        qusetionDao = daoSession.getWisdomEntityDao();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        difficultyTv.setText(sharedPreferences.getString("difficulty","四级") + "英语");
        List<WisdomEntity> datas = qusetionDao.queryBuilder().list();
        Random random = new Random();
        int i = random.nextInt(10);
        wisdomEnglish.setText(datas.get(i).getEnglish());
        wisdomChina.setText(datas.get(i).getChina());
        setText();
    }

    private void setText() {
        alreadyMasteredText.setText(sharedPreferences.getInt("alreadyMastered", 0) + "");
        alreadyStudyText.setText(sharedPreferences.getInt("alreadyStudy",0) + "");
    }
}
