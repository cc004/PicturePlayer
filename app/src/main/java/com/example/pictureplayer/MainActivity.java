package com.example.pictureplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.VideoView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private static byte[] read(InputStream inStream) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while((len = inStream.read(buffer)) != -1)
        {
            outStream.write(buffer,0,len);
        }
        inStream.close();
        return outStream.toByteArray();
    }

    private static byte[] getHttpContent(String uri) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(uri).openConnection();
        conn.setRequestMethod("GET");
        return read(conn.getInputStream());
    }

    private static int search(byte[] main, byte[] sub, int begin) {
        next:
        for (int i = begin; i < main.length; ++i) {
            for (int j = 0; j < sub.length; ++j) {
                if (main[i + j] != sub[j]) continue next;
            }
            return i;
        }
        return -1;
    }

    private final static byte[] spliter = new byte[]{0x40, (byte)0xef, (byte)0xbc, (byte)0x81, 0x40};

    private static Content[] getContents(String addr) throws Exception {
        addr = new JSONObject(new String(getHttpContent(addr), StandardCharsets.UTF_8)).getString("content");
        addr = addr.substring(addr.indexOf("src=\"") + 5);
        addr = addr.substring(0, addr.indexOf("\""));
        byte[] content = getHttpContent(addr);
        Log.d("aaa", "" + content.length);
        int begin = search(content, spliter, 0) + spliter.length;
        int end = search(content, spliter, begin);
        content = Arrays.copyOfRange(content, begin, end);
        String[] texts = new String(content, StandardCharsets.UTF_8).split("\r\n");
        Content[] result = new Content[texts.length];
        for (int i = 0; i < texts.length; ++i) {
            Content c = new Content();
            String[] splits = texts[i].split("@");
            c.cover = splits[0];
            c.name = splits[1];
            c.video = splits[2];
            result[i] = c;
        }
        return result;
    }

    private MutableLiveData<Content[]> contents;
    private MutableLiveData<Content[]> filteredContents;
    private MutableLiveData<Content[]> selectedContents;
    private RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        contents = new MutableLiveData<>();
        filteredContents = new MutableLiveData<>();
        selectedContents = new MutableLiveData<>();

        adapter = new RecyclerViewAdapter(MainActivity.this);
        recyclerView.setAdapter(adapter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Content[] datas = new Content[0];
                try {
                    datas = getContents(getIntent().getExtras().getString("addr"));
                } catch (Exception e) {
                    Log.e(MainActivity.class.getName(), "error fetching addr", e);
                }
                contents.postValue(datas);
            }
        }).start();

        final int ITEMS_PER_PAGE = 20;
        Spinner spinner = findViewById(R.id.spinner);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Content[] val = filteredContents.getValue();
                selectedContents.setValue(Arrays.copyOfRange(val, ITEMS_PER_PAGE * i, Math.min(ITEMS_PER_PAGE * (i + 1), val.length)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        contents.observe(this, new Observer<Content[]>() {
            @Override
            public void onChanged(Content[] contents) {
                filteredContents.setValue(contents);
            }
        });

        EditText filter = findViewById(R.id.textView2);

        filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // nop
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // nop
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Content[] vals = contents.getValue();
                if (vals == null) return;
                String text = filter.getText().toString();
                if (text.isEmpty())
                {
                    filteredContents.setValue(vals);
                    return;
                }
                String[] filters = text.split(" ");
                Stream<Content> stream = Arrays.stream(vals);
                for (String filter : filters)
                    stream = stream.filter(c -> c.name.contains(filter));
                filteredContents.setValue(stream.toArray(Content[]::new));
            }
        });

        filteredContents.observe(this, new Observer<Content[]>() {
            @Override
            public void onChanged(Content[] contents) {
                List<String> pageList = new ArrayList<>();
                for (int i = 0; 20 * i < contents.length; ++i) {
                    pageList.add("第" + (i + 1) + "页");
                }
                spinner.setAdapter(new ArrayAdapter<String>(MainActivity.this, R.layout.my_spinner, pageList));
                spinner.setSelection(0);
            }
        });

        selectedContents.observe(this, new Observer<Content[]>() {
            @Override
            public void onChanged(Content[] contents) {
                adapter.contents = contents;
                adapter.notifyDataSetChanged();
            }
        });
    }
}