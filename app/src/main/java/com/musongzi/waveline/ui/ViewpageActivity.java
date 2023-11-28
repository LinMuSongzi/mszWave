package com.musongzi.waveline.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.musongzi.waveline.R;
import com.musongzi.waveline.databinding.ActivityViewpageBinding;

public class ViewpageActivity extends AppCompatActivity {

    @NonNull
    ActivityViewpageBinding activityViewpageBinding;


    int[] colors = {Color.BLACK, Color.DKGRAY, Color.GRAY, Color.LTGRAY, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA};


    private int indexColor = 0;

    int getColorByIndex() {
        int c = colors[indexColor++];
        if (indexColor == colors.length) {
            indexColor = 0;
        }
        return c;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_viewpage);
        activityViewpageBinding = DataBindingUtil.setContentView(this, R.layout.activity_viewpage);
        activityViewpageBinding.idViewpage.setOffscreenPageLimit(1);
        activityViewpageBinding.idViewpage.setAdapter(new RecyclerView.Adapter<SimpleHodler>() {
            @NonNull
            @Override
            public SimpleHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new SimpleHodler(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bg, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull SimpleHodler holder, int position) {
                holder.contentView.setBackgroundColor(getColorByIndex());
            }

            @Override
            public int getItemCount() {
                return Integer.MAX_VALUE;
            }
        });
        activityViewpageBinding.idViewpage.setCurrentItem(Integer.MAX_VALUE / 2);


        activityViewpageBinding.idViewpage.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.right = 100;
                outRect.left = 100;
            }
        });
        activityViewpageBinding.idViewpage.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                page.setTranslationX(-350 * position);
                page.setScaleX(1 - Math.abs(position) * 0.4f);
                page.setScaleY(1 - Math.abs(position) * 0.4f);

                page.setAlpha(1 - Math.abs(position) * 0.7f);
                page.setRotation(10 * position);


            }
        });


    }


    static class SimpleHodler extends RecyclerView.ViewHolder {
        View contentView;

        public SimpleHodler(@NonNull View itemView) {
            super(itemView);
            contentView = itemView.findViewById(R.id.id_view);
        }
    }

}