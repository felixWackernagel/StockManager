package com.example.wackernagel.myapplication;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.wackernagel.myapplication.db.CategoryModel;
import com.example.wackernagel.myapplication.db.StockItemModel;

import java.util.ArrayList;
import java.util.List;

class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    interface OnItemClickListener {
        void onCategoryClick(CategoryModel categoryModel );
        void onStockItemClick(StockItemModel stockItemModel );
    }

    private List<Object> items;
    private OnItemClickListener listener;
    private FragmentManager fragmentManager;

    CategoryAdapter(final FragmentManager fm) {
        items = new ArrayList<>();
        setHasStableIds( true );
        fragmentManager = fm;
    }

    void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    void addItem(@Nullable final Object item ) {
        if( item != null ) {
            final int start = items.size();
            items.add( item );
            notifyItemInserted( start );
        }
    }

    void addItems(@Nullable final List<?> itemList ) {
        if( itemList != null ) {
            final int start = items.size();
            items.addAll( itemList );
            notifyItemRangeInserted( start, itemList.size() );
        }
    }

    void clearItems() {
        final int size = items.size();
        items.clear();
        notifyItemRangeRemoved( 0, size );
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get( position ) instanceof CategoryModel ? R.layout.item_category :
                items.get( position ) instanceof StockItemModel ? R.layout.item_stock_item : R.layout.item_subheader;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from( parent.getContext() ).inflate(viewType, parent, false);
        RecyclerView.ViewHolder holder;
        switch( viewType ) {
            case R.layout.item_category:
                holder = new CategoryHolder( itemView, listener, fragmentManager );
                break;

            case R.layout.item_stock_item:
                holder = new StockItemHolder( itemView, listener, fragmentManager );
                break;

            case R.layout.item_subheader:
                holder = new HeadlineHolder( itemView );
                break;

            default:
                throw new IllegalStateException( "No mapping between viewType '" + viewType + "' and ViewHolder!" );
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);
        switch( getItemViewType( position ) ) {
            case R.layout.item_category:
                ((CategoryHolder)holder).setCategory( (CategoryModel) item);
                break;

            case R.layout.item_stock_item:
                ((StockItemHolder)holder).setStockItem( (StockItemModel ) item);
                break;

            case R.layout.item_subheader:
                ((HeadlineHolder) holder).setHeadline(String.valueOf( item ));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static class CategoryHolder extends RecyclerView.ViewHolder {

        final TextView text;
        final ImageButton more;

        private final OnItemClickListener clickListener;
        private final FragmentManager fragmentManager;

        private CategoryModel item;

        CategoryHolder(View itemView, OnItemClickListener listener, FragmentManager fm) {
            super(itemView);
            this.clickListener = listener;
            this.fragmentManager = fm;
            text = (TextView) itemView.findViewById(R.id.text);
            more = (ImageButton) itemView.findViewById(R.id.more);
            more.setColorFilter( new PorterDuffColorFilter(ContextCompat.getColor( itemView.getContext(), R.color.sidekick_icon ), PorterDuff.Mode.SRC_IN ) );

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int adapterPos = getAdapterPosition();
                    if( clickListener != null && adapterPos != RecyclerView.NO_POSITION ) {
                        clickListener.onCategoryClick( item );
                    }
                }
            });
            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int adapterPos = getAdapterPosition();
                    if( adapterPos != RecyclerView.NO_POSITION ) {
                        ContextMenuBottomSheet.newInstance( item ).show(fragmentManager, "menu" );
                    }
                }
            });
        }

        void setCategory( final CategoryModel category ) {
            item = category;
            text.setText( category.getName() );
        }
    }

    private static class StockItemHolder extends RecyclerView.ViewHolder {

        final TextView stock;
        final TextView text;
        final ImageButton more;

        private final OnItemClickListener clickListener;
        private final FragmentManager fragmentManager;

        private StockItemModel item;

        StockItemHolder(View itemView, OnItemClickListener listener, FragmentManager fm) {
            super(itemView);
            this.clickListener = listener;
            this.fragmentManager = fm;
            stock = (TextView ) itemView.findViewById(R.id.stock);
            text = (TextView) itemView.findViewById(R.id.text);
            more = (ImageButton) itemView.findViewById(R.id.more);
            more.setColorFilter( new PorterDuffColorFilter(ContextCompat.getColor( itemView.getContext(), R.color.sidekick_icon ), PorterDuff.Mode.SRC_IN ) );

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int adapterPos = getAdapterPosition();
                    if( clickListener != null && adapterPos != RecyclerView.NO_POSITION ) {
                        clickListener.onStockItemClick( item );
                    }
                }
            });
            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int adapterPos = getAdapterPosition();
                    if( adapterPos != RecyclerView.NO_POSITION ) {
                        ContextMenuBottomSheet.newInstance( item ).show(fragmentManager, "menu" );
                    }
                }
            });
        }

        void setStockItem( final StockItemModel stockItem ) {
            item = stockItem;
            stock.setText( String.valueOf( stockItem.getStock() ) );
            text.setText( stockItem.getName() );
        }
    }

    private static class HeadlineHolder extends RecyclerView.ViewHolder {

        final TextView text;

        HeadlineHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text);
        }

        void setHeadline( final String headline ) {
            text.setText( headline );
        }
    }

}
