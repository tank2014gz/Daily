package com.aspsine.zhihu.daily.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aspsine.zhihu.daily.Constants;
import com.aspsine.zhihu.daily.R;
import com.aspsine.zhihu.daily.adapter.ExploreAdapter;
import com.aspsine.zhihu.daily.entity.Story;
import com.aspsine.zhihu.daily.interfaces.OnItemClickListener;
import com.aspsine.zhihu.daily.interfaces.OnItemLongClickListener;
import com.aspsine.zhihu.daily.network.Http;
import com.aspsine.zhihu.daily.ui.activity.StoryActivity;
import com.aspsine.zhihu.daily.util.DateUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aspsine on 2015/2/26.
 */
public class ExploreFragment extends PlaceholderFragment implements OnItemClickListener, OnItemLongClickListener {
    public static final String TAG = ExploreFragment.class.getSimpleName();

    public static final String EXTRA_STORY_ID = "Story_Id";

    private List<Story> mStories;
    private ExploreAdapter mAdapter;

    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStories = new ArrayList<Story>();
        mAdapter = new ExploreAdapter(mStories);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnItemLongClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_dark, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_green_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fresh();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fresh();
    }

    @Override
    public void onItemClick(int position, View view) {
        Intent intent = new Intent(getActivity(), StoryActivity.class);
        intent.putExtra(EXTRA_STORY_ID, String.valueOf(position));
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(int position, View view) {
        Toast.makeText(getActivity(), position + " long click", Toast.LENGTH_SHORT).show();
    }

    private void fresh(){
        swipeRefreshLayout.setRefreshing(true);
        new Thread(new MyRunnable()).start();
    }

    private final class MyRunnable implements Runnable {

        @Override
        public void run() {
            try {
                List<Story> tmpStories = new ArrayList<Story>();
                JSONObject contents = new JSONObject(Http.get(Constants.Url.ZHIHU_DAILY_BEFORE, DateUtils.getCurrentDate()));
                JSONArray jsonStories = contents.getJSONArray("stories");
                for (int i = 0, size = jsonStories.length(); i < size; i++) {
                    JSONObject jsonStory = jsonStories.getJSONObject(i);
                    Story story = new Story();
                    story.setTitle(jsonStory.getString("title"));
                    story.setGaPrefix(jsonStory.getString("ga_prefix"));
                    story.setThumbnail(String.valueOf(jsonStory.getJSONArray("images").get(0)));
                    story.setId(jsonStory.getString("id"));
                    story.setType(jsonStory.getString("type"));
                    tmpStories.add(story);
                }
                handler.obtainMessage(0, tmpStories).sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
                handler.obtainMessage(1).sendToTarget();
            }

        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            swipeRefreshLayout.setRefreshing(false);
            if (msg.what == 0) {
                mStories.clear();
                mStories.addAll((List<Story>)msg.obj);
                mAdapter.notifyDataSetChanged();
            }else {
                Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();
            }
        }
    };


}