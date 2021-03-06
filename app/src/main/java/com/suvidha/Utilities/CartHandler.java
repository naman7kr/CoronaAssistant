package com.suvidha.Utilities;

import com.suvidha.Models.ItemModel;

import java.util.ArrayList;

public class CartHandler {
    private static CartHandler single_instance = null;
    private static ArrayList<ItemModel> inCart = new ArrayList<>();
    private static ArrayList<ArrayList<ItemModel>> alreadyPlaced = new ArrayList<>();

    public static CartHandler getInstance()
    {
        if (single_instance == null)
            single_instance = new CartHandler();
        return single_instance;
    }
    public void addItem(ItemModel item){
        inCart.add(item);
    }
    public void updateItem(ItemModel item){
        for(int i=0;i<inCart.size();i++){
            if(item.item_id.compareTo(inCart.get(i).item_id)==0){
                inCart.set(i,item);
            }
        }
    }
    public void removeItem(ItemModel item){
        for(int i=0;i<inCart.size();i++){
            if(item.item_id.compareTo(inCart.get(i).item_id)==0){
                inCart.remove(i);
            }
        }
    }
    public ItemModel findItem(ItemModel item){
        for(int i=0;i<inCart.size();i++){
            if(item.item_id.compareTo(inCart.get(i).item_id)==0){
                return inCart.get(i);
            }
        }
        return null;
    }
    public int getItemsCount(){
        return inCart.size();
    }
    public double getTotalWithoutTax(){
        double total=0;
        for(int i=0;i<inCart.size();i++){
            total += inCart.get(i).item_price *inCart.get(i).item_add_qty;
        }
        return total;
    }
    public ArrayList<ItemModel>  getListInCart(){
        return inCart;
    }

    public void clearCart(){
        for(int i=0;i<inCart.size();i++){
            inCart.get(i).item_add_qty=0;
        }
        inCart.clear();
    }
    public ArrayList<ArrayList<ItemModel>> getAlreadyPlaced(){
        return alreadyPlaced;
    }
}
