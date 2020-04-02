package com.suvidha.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.suvidha.Activities.CategoriesActivity;
import com.suvidha.Activities.ItemActivity;
import com.suvidha.Models.ShopModel;
import com.suvidha.R;

import static com.suvidha.Utilities.Utils.currentType;
import static com.suvidha.Utilities.Utils.shopItems;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ShopListAdapter extends RecyclerView.Adapter<ShopListAdapter.MyHolder> {

    public Context ctx;
    public List<ShopModel> list;
    public ShopListAdapter(Context ctx, List<ShopModel> list) {
        this.ctx = ctx;
        this.list = list;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_shop,parent,false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        ShopModel data = list.get(position);
        if(currentType==1){
            holder.iv.setImageResource(R.mipmap.ic_groc);
        }
        holder.shop_name.setText(data.name);
        holder.shop_addr.setText(data.address);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    if(currentType==1) {
                        Intent intent = new Intent(ctx, CategoriesActivity.class);
                        intent.putExtra("shopid", data._id);
                        intent.putExtra("shopname", data.name);
                        ctx.startActivity(intent);
                    }else{
                        Intent intent = new Intent(ctx, ItemActivity.class);
                        intent.putExtra("shopid", data._id);
                        intent.putExtra("shopname", data.name);
                        intent.putExtra("flag",1);
                        ctx.startActivity(intent);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(ctx, "No Items Found", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        if(list == null)return  0;
        return list.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView iv;
        TextView shop_name,shop_addr;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.shop_img);
            shop_name = itemView.findViewById(R.id.shop_name);
            shop_addr = itemView.findViewById(R.id.shop_addr);
        }
    }
}
