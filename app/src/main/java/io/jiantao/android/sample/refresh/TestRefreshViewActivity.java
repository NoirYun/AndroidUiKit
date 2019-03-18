package io.jiantao.android.sample.refresh;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;
import java.util.Random;

import android.support.v7.widget.StaggeredGridLayoutManager;
import io.jiantao.android.sample.R;
import io.jiantao.android.uikit.adapter.loadmore.LoadMoreDelegate;
import io.jiantao.android.uikit.adapter.loadmore.MultiTypeLoadMoreAdapter;
import io.jiantao.android.uikit.refresh.ISwipeRefreshLayout;
import io.jiantao.android.uikit.widget.IDividerItemDecoration;
import me.drakeet.multitype.Items;

/**
 * Created by jiantao on 2017/6/15.
 */

public class TestRefreshViewActivity extends Activity {

    ISwipeRefreshLayout refreshLayout;
    MultiTypeLoadMoreAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        refreshLayout = (ISwipeRefreshLayout) findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(new ISwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // do refresh ...
                handler.sendEmptyMessageDelayed(1, 3000);
            }
        });
        refreshLayout.setRefreshHeaderView(new MedlinkerRefreshHeaderView(this));
        handler.sendEmptyMessageDelayed(0, 2000);
        refreshLayout.setEnabled(false);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(getLayoutManager());
        IDividerItemDecoration divierDecoration = new IDividerItemDecoration(this, IDividerItemDecoration.VERTICAL)
                .setVerticalDividerHeight(50)
                .setHorizontalDividerWidth(50)
                .setDividerColor(Color.GRAY)
                .setDividerPadding(30);

        // or setCustomDrawable
        // divierDecoration.setDrawable(getResources().getDrawable(R.drawable.custom_divider))
        recyclerView.addItemDecoration(divierDecoration);

        adapter = new MultiTypeLoadMoreAdapter();
        adapter.register(TextItem.class, new TextItemViewBinder());
        recyclerView.setAdapter(adapter);

        adapter.setLoadMoreItemRetryListener(new MultiTypeLoadMoreAdapter.ILoadMoreRetryListener() {
            @Override
            public void retry() {
                handler.sendEmptyMessage(2);
            }
        });

        adapter.setLoadMoreSubject(new LoadMoreDelegate.LoadMoreSubject() {
            @Override
            public boolean isLoading() {
                return isLoading;
            }

            @Override
            public void onLoadMore() {
                System.out.println(" onloacmore called ");
                handler.sendEmptyMessage(2);
            }
        });

        /* Mock the data */
//        TextItem textItem = new TextItem("world");


        adapter.setItems(createItems());
        adapter.notifyDataSetChanged();

    }

    private RecyclerView.LayoutManager getLayoutManager() {
//        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 3);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        return layoutManager;
    }

    boolean isLoading;
    int index = 1;

    private List<?> createItems() {
        Items items = new Items();
        for (int i = index; i < index + 20; i++) {
            TextItem textItem = new TextItem("world no." + i);
            items.add(textItem);
        }
        index += items.size();
        return items;
    }

    Random random = new Random();

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:// refreshing ture
                    refreshLayout.setRefreshing(true);
                    handler.sendEmptyMessageDelayed(1, 30000);
                    break;

                case 1:// refreshing false
                    refreshLayout.setRefreshing(false);
                    break;

                case 2://加载ing
                    isLoading = true;
                    adapter.setLoadMoreItemState(MultiTypeLoadMoreAdapter.LoadMoreItem.STATE_LOADING);
                    handler.sendEmptyMessageDelayed(3, 2000);
                    break;
                case 3:// 加载成功
                    boolean succeed = random.nextBoolean();
                    if (succeed) {
                        adapter.appendItems(createItems());
                    }else{
                        adapter.setLoadMoreItemState(MultiTypeLoadMoreAdapter.LoadMoreItem.STATE_FAILED);
                    }
                    isLoading = false;
                    if (index < 101) {
                        // do nothing
                    } else {// load completed, no more data
                        adapter.setLoadMoreItemState(MultiTypeLoadMoreAdapter.LoadMoreItem.STATE_NO_MORE_DATA);
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    });

}
