package com.example.wackernagel.myapplication.editor;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.example.wackernagel.myapplication.db.StockItemModel;

import java.util.Date;

class StockItemParcelable implements Parcelable {

    private final StockItemModel stockItemModel;

    StockItemParcelable(@NonNull StockItemModel stockItemModel) {
        this.stockItemModel = stockItemModel;
    }

    private StockItemParcelable(Parcel in) {
        stockItemModel = new StockItemModel( in.readLong(), in.readLong(), in.readString(), in.readInt(), in.readInt(), new Date(in.readLong()), new Date(in.readLong()), in.readString());
    }

    public static final Creator<StockItemParcelable> CREATOR = new Creator<StockItemParcelable>() {
        @Override
        public StockItemParcelable createFromParcel(Parcel in) {
            return new StockItemParcelable(in);
        }

        @Override
        public StockItemParcelable[] newArray(int size) {
            return new StockItemParcelable[size];
        }
    };

    @NonNull
    StockItemModel getStockItem() {
        return stockItemModel;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(stockItemModel.getId());
        dest.writeLong(stockItemModel.getCategoryId());
        dest.writeString(stockItemModel.getName());
        dest.writeInt(stockItemModel.getStock());
        dest.writeInt(stockItemModel.getOrderLimit());
        dest.writeLong(stockItemModel.getCreated().getTime());
        dest.writeLong(stockItemModel.getChanged().getTime());
    }
}
