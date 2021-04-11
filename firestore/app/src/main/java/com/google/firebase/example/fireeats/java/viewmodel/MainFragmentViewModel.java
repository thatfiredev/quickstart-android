package com.google.firebase.example.fireeats.java.viewmodel;

import android.content.Context;

import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.example.fireeats.java.Filters;
import com.google.firebase.example.fireeats.java.model.Rating;
import com.google.firebase.example.fireeats.java.model.Restaurant;
import com.google.firebase.example.fireeats.java.util.RatingUtil;
import com.google.firebase.example.fireeats.java.util.RestaurantUtil;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

/**
 * ViewModel for {@link com.google.firebase.example.fireeats.java.MainActivity}.
 */

public class MainFragmentViewModel extends ViewModel {

    private boolean mIsSigningIn;
    private Filters mFilters;

    private static final int LIMIT = 50;

    // Firestore
    // TODO (rosariopfernandes): Use dependency injection here
    private final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

    // Get ${LIMIT} restaurants
    private Query mQuery = mFirestore.collection("restaurants")
            .orderBy("avgRating",Query.Direction.DESCENDING)
            .limit(LIMIT);


    public MainFragmentViewModel() {
        mIsSigningIn = false;
        mFilters = Filters.getDefault();
    }

    // TODO (rosariopfernandes): handle the query change - maybe use livedata?
    public Query getQuery() { return mQuery; }

    public void addFiltersToQuery(Filters filters) {
        // Construct query basic query
        Query query = mFirestore.collection("restaurants");

        // Category (equality filter)
        if (filters.hasCategory()) {
            query = query.whereEqualTo(Restaurant.FIELD_CATEGORY, filters.getCategory());
        }

        // City (equality filter)
        if (filters.hasCity()) {
            query = query.whereEqualTo(Restaurant.FIELD_CITY, filters.getCity());
        }

        // Price (equality filter)
        if (filters.hasPrice()) {
            query = query.whereEqualTo(Restaurant.FIELD_PRICE, filters.getPrice());
        }

        // Sort by (orderBy with direction)
        if (filters.hasSortBy()) {
            query = query.orderBy(filters.getSortBy(), filters.getSortDirection());
        }

        this.mQuery = query.limit(LIMIT);
    }

    public Task<Void> addRandomRestaurants(Context context) {
        // Add a bunch of random restaurants
        WriteBatch batch = mFirestore.batch();
        for (int i = 0; i < 10; i++) {
            DocumentReference restRef = mFirestore.collection("restaurants").document();

            // Create random restaurant / ratings
            Restaurant randomRestaurant = RestaurantUtil.getRandom(context);
            List<Rating> randomRatings = RatingUtil.getRandomList(randomRestaurant.getNumRatings());
            randomRestaurant.setAvgRating(RatingUtil.getAverageRating(randomRatings));

            // Add restaurant
            batch.set(restRef, randomRestaurant);

            // Add ratings to subcollection
            for (Rating rating : randomRatings) {
                batch.set(restRef.collection("ratings").document(), rating);
            }
        }
        return batch.commit();
    }

    public boolean getIsSigningIn() {
        return mIsSigningIn;
    }

    public void setIsSigningIn(boolean mIsSigningIn) {
        this.mIsSigningIn = mIsSigningIn;
    }

    public Filters getFilters() {
        return mFilters;
    }

    public void setFilters(Filters mFilters) {
        this.mFilters = mFilters;
    }
}
