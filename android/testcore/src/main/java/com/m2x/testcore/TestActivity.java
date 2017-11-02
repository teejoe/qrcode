package com.m2x.testcore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.m2x.testcore.TestWrapper.Binarizer;
import com.m2x.testcore.TestWrapper.DecodeResult;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by mtj on 2017/10/30.
 */

public class TestActivity extends AppCompatActivity {

    private static final String DIR_PATH = "/mnt/sdcard/qrcode/";

    private ArrayList<ViewModel> mListItems = new ArrayList<>();

    private Binarizer mBinarizer = Binarizer.GLOBAL_HISTOGRAM;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);
        loadImageFiles();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.begin:
                beginTest();
                break;
            case R.id.change_binarizer:
                int next = mBinarizer.ordinal() + 1;
                if (next >= Binarizer.values().length) {
                    next = 0;
                }
                mBinarizer = Binarizer.values()[next];
                Toast.makeText(this, "current binarizer:" + mBinarizer.toString(),
                        Toast.LENGTH_LONG).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void beginTest() {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                for (ViewModel model: mListItems) {
                    Bitmap bitmap = BitmapFactory.decodeFile(model.imagePath);
                    if (bitmap != null) {
                        long start = System.currentTimeMillis();
                        DecodeResult result = TestWrapper.decodeBitmap(bitmap, mBinarizer, null);
                        if (!result.success) {
                            result = TestWrapper.decodeBitmap(bitmap, Binarizer.ADJUSTED_HYBRID, null);
                        }
                        model.cost = System.currentTimeMillis() - start;
                        model.finished = true;
                        model.success = result.success;
                        model.result = result.msg;

                        e.onNext(true);
                    }
                }
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean value) {
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        int failedCount = 0;
                        long totalCost = 0;
                        for (ViewModel model: mListItems) {
                            failedCount += model.success? 0: 1;
                            totalCost += model.cost;
                        }
                        Toast.makeText(TestActivity.this, "failed: " + failedCount
                                + "\ntotal cost:" + totalCost + "ms",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadImageFiles() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                File dir = new File(DIR_PATH);
                File[] files = dir.listFiles();
                for(File file: files) {
                    if (file.isFile()) {
                        ViewModel model = new ViewModel();
                        model.imagePath = file.getPath();
                        mListItems.add(model);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                populate();
            }
        }.execute();
    }

    private void populate() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(TestActivity.this));
        mRecyclerView.setAdapter(new MyAdapter());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image)
        public ImageView image;

        @BindView(R.id.path)
        public TextView path;

        @BindView(R.id.checkbox)
        public ImageView checkbox;

        @BindView(R.id.cost)
        public TextView cost;


        @BindView(R.id.result)
        public TextView result;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class ViewModel {
        public ViewModel() {
            imagePath = "";
            result = "";
            cost = -1;
        }

        public String imagePath;
        public long cost;
        public String result;
        public boolean success;
        public boolean finished;
    }

    public class MyAdapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.test_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final ViewModel model = mListItems.get(position);
            Picasso.with(TestActivity.this)
                    .load(new File(model.imagePath))
                    .into(holder.image);
            holder.path.setText(new File(model.imagePath).getName());
            if (model.cost >= 0) {
                holder.cost.setText("cost:" + model.cost + "ms");
            } else {
                holder.cost.setText("");
            }

            holder.result.setText(model.result);
            if (model.finished) {
                holder.checkbox.setVisibility(View.VISIBLE);
            } else {
                holder.checkbox.setVisibility(View.GONE);
            }

            if (model.success) {
                holder.checkbox.setImageResource(R.drawable.ic_success);
            } else {
                holder.checkbox.setImageResource(R.drawable.ic_fail);
            }

            holder.image.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    new ImageDialog(TestActivity.this, model.imagePath).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mListItems.size();
        }
    }
}
