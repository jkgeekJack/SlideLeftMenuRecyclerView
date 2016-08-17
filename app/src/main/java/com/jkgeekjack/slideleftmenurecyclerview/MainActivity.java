package com.jkgeekjack.slideleftmenurecyclerview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.Toast;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LeftSwipeMenuRecyclerView rv= (LeftSwipeMenuRecyclerView) findViewById(R.id.rv);

        final LinkedList<String> list=new LinkedList<String>();
        list.add("hello");
        list.add("jack");
        list.add("hi");
        list.add("hello");
        list.add("jack");
        list.add("hi");
        list.add("hello");
        list.add("jack");
        list.add("hi");
        list.add("hello");
        list.add("jack");
        list.add("hi");
        list.add("hello");
        list.add("jack");
        list.add("hi");
        rv.setLayoutManager(new LinearLayoutManager(this));
        final RVAdapter adapter=new RVAdapter(this,list);
        rv.setAdapter(adapter);
        rv.setOnItemActionListener(new OnItemActionListener() {
            //点击
            @Override
            public void OnItemClick(int position) {
                Toast.makeText(MainActivity.this,"Click"+position,Toast.LENGTH_SHORT).show();
            }
            //置顶
            @Override
            public void OnItemTop(int position) {
                //获得当前位置的内容
                String temp =list.get(position);
                //移除这个item
                list.remove(position);
                //把它添加到第一个
                list.addFirst(temp);
                //更新数据源
                adapter.notifyDataSetChanged();
            }
            //删除
            @Override
            public void OnItemDelete(int position) {
                list.remove(position);
                //更新数据源
                adapter.notifyDataSetChanged();
            }
        });
    }
}
