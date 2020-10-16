package com.zwir.bigpen.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zwir.bigpen.R;
import com.zwir.bigpen.data.Pen;
import com.zwir.bigpen.data.PenViewModel;
import com.zwir.bigpen.fragments.ColorFragment;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class HistoricPenAdapter extends RecyclerView.Adapter<HistoricPenAdapter.HistoricPenImageRowHolder>{

    private List<Pen> penList;
    private ColorFragment colorFragment;
    private Context mContext;
    public final String insert_pen="insert_pen",delete_pen="delete_pen";
    private String action=insert_pen;
    private PenViewModel penViewModel;
    public HistoricPenAdapter(ColorFragment colorFragment, Context mContext,PenViewModel penViewModel) {
        this.penViewModel = penViewModel;
        this.colorFragment=colorFragment;
        this.mContext=mContext;
    }

    @Override
    public HistoricPenImageRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.historic_pen_view,parent,false);
        return new HistoricPenImageRowHolder(v);
    }

    @Override
    public void onBindViewHolder(final HistoricPenImageRowHolder holder, final int position) {
        holder.historicPenColor.setBackgroundColor(penList.get(position).getColorPen());
        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return (null != penList ? penList.size() : 0);
    }
    private void setAnimation(View viewToAnimate, int position) {
        if (position ==0 && action.equals(insert_pen)) {
            Animation slideFromTop = AnimationUtils.loadAnimation(mContext,R.anim.slide_from_top);
            viewToAnimate.startAnimation(slideFromTop);
        }
    }
    public void setPenList(List<Pen> penList) {
        this.penList = penList;
        notifyDataSetChanged();
    }
    public void setAction(String action) {
        this.action = action;
    }
    class HistoricPenImageRowHolder extends RecyclerView.ViewHolder {
        RelativeLayout historicPenColor;
        ImageView historicPenImageView;
        ImageView deleteHistoricPen;
        HistoricPenImageRowHolder(final View itemView) {
            super(itemView);
            this.historicPenColor =itemView.findViewById(R.id.historic_pen_color);
            this.historicPenImageView =itemView.findViewById(R.id.historic_pen_imageView);
            this.deleteHistoricPen=itemView.findViewById(R.id.delete_historic_pen);
            historicPenImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    colorFragment.fillColors(penList.get(getAdapterPosition()).getColorPen());
                }
            });
            deleteHistoricPen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    action=delete_pen;
                    penViewModel.deletePen(penList.get(getAdapterPosition()));
                }
            });
        }
    }
}
