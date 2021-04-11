package com.google.firebase.example.fireeats.java.adapter;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.example.fireeats.R;
import com.google.firebase.example.fireeats.databinding.ItemRestaurantBinding;
import com.google.firebase.example.fireeats.java.model.Restaurant;
import com.google.firebase.example.fireeats.java.util.RestaurantUtil;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * RecyclerView adapter for a list of Restaurants.
 */
public class RestaurantAdapter extends FirestoreAdapter<RestaurantAdapter.ViewHolder> {

    public interface OnRestaurantSelectedListener {

        void onRestaurantSelected(String restaurantId);

    }

    private final OnRestaurantSelectedListener mListener;

    public RestaurantAdapter(OnRestaurantSelectedListener listener) {
        mListener = listener;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemRestaurantBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemRestaurantBinding binding;

        public ViewHolder(ItemRestaurantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnRestaurantSelectedListener listener) {

            Restaurant restaurant = snapshot.toObject(Restaurant.class);
            Resources resources = itemView.getResources();

            // Load image
            Glide.with(binding.restaurantItemImage.getContext())
                    .load(restaurant.getPhoto())
                    .into(binding.restaurantItemImage);

            binding.restaurantItemName.setText(restaurant.getName());
            binding.restaurantItemRating.setRating((float) restaurant.getAvgRating());
            binding.restaurantItemCity.setText(restaurant.getCity());
            binding.restaurantItemCategory.setText(restaurant.getCategory());
            binding.restaurantItemNumRatings.setText(resources.getString(R.string.fmt_num_ratings,
                    restaurant.getNumRatings()));
            binding.restaurantItemPrice.setText(RestaurantUtil.getPriceString(restaurant));

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onRestaurantSelected(snapshot.getId());
                    }
                }
            });
        }

    }
}
