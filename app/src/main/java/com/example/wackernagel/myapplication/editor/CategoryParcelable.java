package com.example.wackernagel.myapplication.editor;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.example.wackernagel.myapplication.db.CategoryModel;

import java.util.Date;

class CategoryParcelable implements Parcelable {

    private final CategoryModel categoryModel;

    CategoryParcelable( @NonNull CategoryModel categoryModel) {
        this.categoryModel = categoryModel;
    }

    private CategoryParcelable(Parcel in) {
        categoryModel = new CategoryModel(in.readLong(), in.readLong(), in.readString(), new Date(in.readLong()), new Date(in.readLong()), in.readString());
    }

    public static final Creator<CategoryParcelable> CREATOR = new Creator<CategoryParcelable>() {
        @Override
        public CategoryParcelable createFromParcel(Parcel in) {
            return new CategoryParcelable(in);
        }

        @Override
        public CategoryParcelable[] newArray(int size) {
            return new CategoryParcelable[size];
        }
    };

    @NonNull
    CategoryModel getCategory() {
        return categoryModel;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(categoryModel.getId());
        dest.writeLong(categoryModel.getParentId());
        dest.writeString(categoryModel.getName());
        dest.writeLong(categoryModel.getCreated().getTime());
        dest.writeLong(categoryModel.getChanged().getTime());
    }
}
