package com.zwir.bigpen.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.zwir.bigpen.R;
import com.zwir.bigpen.data.Pen;
import com.zwir.bigpen.fragments.DrawerFragment;

import java.util.List;

public class PenAdapter extends RecyclerView.Adapter<PenAdapter.PenImageRowHolder>{

    private DrawerFragment drawerFragment;
    private List<Pen> itemsListPen;
    private int lastSelectedPosition = 1;
    public PenAdapter(List<Pen> itemsListPen, DrawerFragment drawerFragment ) {
        this.itemsListPen = itemsListPen;
        this.drawerFragment=drawerFragment;
    }

    @Override
    public PenImageRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pen_view,parent,false);
        return new PenImageRowHolder(v);
    }

    @Override
    public void onBindViewHolder(final PenImageRowHolder holder, final int position) {
        if (position==0)
            holder.penImageView.setImageResource(R.drawable.rectangular_shape_primary);
        else holder.penImageView.setImageResource(R.drawable.ic_pen);
        holder.penColor.setBackgroundColor(itemsListPen.get(position).getColorPen());
        holder.selectionState.setChecked(lastSelectedPosition == position);
        holder.selectionState.setVisibility((lastSelectedPosition == position)? View.VISIBLE :View.INVISIBLE );
    }

    @Override
    public int getItemCount() {
        return (null != itemsListPen ? itemsListPen.size() : 0);
    }
    class PenImageRowHolder extends RecyclerView.ViewHolder {
        RelativeLayout penColor;
        ImageView penImageView;
        private RadioButton selectionState;
        PenImageRowHolder(View itemView) {
            super(itemView);
            this.penColor=itemView.findViewById(R.id.pen_color);
            this.penImageView =itemView.findViewById(R.id.pen_image_view);
            this.selectionState=itemView.findViewById(R.id.pen_radio_button);
            penImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawerFragment.getSurfaceDraw().setDrawingColor(itemsListPen.get(getAdapterPosition()).getColorPen());
                    lastSelectedPosition = getAdapterPosition();
                    notifyDataSetChanged();
                }
            });
        }
    }
}
