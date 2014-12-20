package com.gamepari.acryl.data;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by seokceed on 2014-12-20.
 */
public class TSVReader {

    private String filePath;

    public TSVReader(String filePath) {
        this.filePath = filePath;
    }

    public List<Playground> runParse() throws IOException {

        //path : sdcard/we love acryl/list.csv

        FileInputStream fis = new FileInputStream(filePath);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(fis, 1024)));

        StringBuilder stringBuilder = new StringBuilder();

        String line;
        List<String> lines = new ArrayList<>();

        Playground pgObject = null;
        List<Playground> listPlayground = new ArrayList<>();

        while ((line = bufferedReader.readLine()) != null) {

            String columns[] = line.split("\t");
            pgObject = new Playground();
            listPlayground.add(pgObject);
            pgObject.setAddress1(columns[0]);
            pgObject.setTag_num(Integer.valueOf(columns[1]));
            pgObject.setInst_num(Integer.valueOf(columns[2]));
            pgObject.setFullAddress(columns[3]);
            pgObject.setAddress2(columns[4]);
            pgObject.setInstName(columns[5]);
            pgObject.setPay(columns[6]);
        }


        //중랑구,53,2419,서울시 중랑구 면목2동 124-15,면목2동,열매상상어린이공원 놀이터,"₩5,000"

        return listPlayground;
    }
}
