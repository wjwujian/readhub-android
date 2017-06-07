package com.bihe0832.readhub.module.readhub;

import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bihe0832.readhub.R;
import com.bihe0832.readhub.framework.fragment.base.BaseFragment;
import com.bihe0832.readhub.libware.thread.ShakebaThreadManager;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;


public abstract class ReadhubFragment extends BaseFragment {

    private static final int ACTION_INIT = 0;
    private static final int ACTION_REFRESH = 1;
    private static final int ACTION_LOAD_MORE = 2;

    protected XRecyclerView mRecyclerView;

    protected String mCursor = "";
    protected int pageSize = 10;

    private int mCurrentAction = ACTION_INIT;

    private Button mRefreshBtn = null;
    private LinearLayout mErroePage = null;

    @Override
    protected View setContentView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.com_bihe0832_readhub_fragment, container, false);
    }

    @Override
    protected void initView() {
        LinearLayoutManager LayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView = customFindViewById(R.id.recyclerview);
        setAdapter(mRecyclerView);
        mRecyclerView.setLayoutManager(LayoutManager);
        mRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                switchAction(ACTION_REFRESH);
            }

            @Override
            public void onLoadMore() {
                switchAction(ACTION_LOAD_MORE);
            }
        });
        mRecyclerView.setRefreshProgressStyle(ProgressStyle.BallClipRotatePulse);
        mRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);
        TextView tv_empty = new TextView(getMContext());
        tv_empty.setText("Empty");
        mRecyclerView.setEmptyView(tv_empty);
        mRecyclerView.setRefreshing(true);
        mRecyclerView.setVisibility(View.VISIBLE);

        mErroePage = customFindViewById(R.id.network_error);
        mErroePage.setVisibility(View.GONE);

    }

    @Override
    protected void initData() {
        switchAction(ACTION_INIT);
    }

    protected void showNetWorkError() {

        mRecyclerView.setVisibility(View.GONE);
        mErroePage.setVisibility(View.VISIBLE);
        mRefreshBtn = customFindViewById(R.id.network_fresh_btn);
        mRefreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchAction(ACTION_REFRESH);
            }
        });

    }

    protected void loadComplete() {
        ShakebaThreadManager.getInstance().runOnUIThread(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.setVisibility(View.VISIBLE);
                mErroePage.setVisibility(View.GONE);
                if (mCurrentAction == ACTION_REFRESH)
                    mRecyclerView.refreshComplete();
                if (mCurrentAction == ACTION_LOAD_MORE)
                    mRecyclerView.loadMoreComplete();
            }
        });
    }

    private void switchAction(int action) {
        mCurrentAction = action;
        switch (mCurrentAction) {
            case ACTION_INIT:
                clearAdapter();
                mRecyclerView.refreshComplete();
                mRecyclerView.loadMoreComplete();
                break;
            case ACTION_REFRESH:
                clearAdapter();
                mCursor="";
                getData();
                break;
            case ACTION_LOAD_MORE:
                getData();
                break;
        }
    }

    protected abstract void getData();
    protected abstract void clearAdapter();
    protected abstract void setAdapter(XRecyclerView recyclerView);

}
