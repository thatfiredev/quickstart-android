package com.google.firebase.example.fireeats.java.viewmodel;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.Task;
import com.google.firebase.example.fireeats.java.Filters;
import com.google.firebase.example.fireeats.java.model.Rating;
import com.google.firebase.example.fireeats.java.model.Restaurant;
import com.google.firebase.example.fireeats.java.util.RatingUtil;
import com.google.firebase.example.fireeats.java.util.RestaurantUtil;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

/**
 * ViewModel for {@link com.google.firebase.example.fireeats.java.MainFragment}.
 */

public class MainFragmentViewModel extends ViewModel {

    private boolean mIsSigningIn;
    private Filters mFilters;

    private static final int LIMIT = 50;

    // Firestore
    private final FirebaseFirestore mFirestore;

    // Get ${LIMIT} restaurants
    private Query mQuery;

    private final MutableLiveData<QuerySnapshot> querySnapshot;
    private final MutableLiveData<FirebaseFirestoreException> error;
    private ListenerRegistration listenerRegistration;

    public MainFragmentViewModel(FirebaseFirestore firestore) {
        mFirestore = firestore;
        mQuery = mFirestore.collection("restaurants")
                .orderBy("avgRating",Query.Direction.DESCENDING)
                .limit(LIMIT);
        mIsSigningIn = false;
        mFilters = Filters.getDefault();
        querySnapshot = new MutableLiveData<>();
        error = new MutableLiveData<>();
        attachSnapshotListener();
    }

    public MainFragmentViewModel() {
        this(FirebaseFirestore.getInstance());
    }

    private void attachSnapshotListener() {
        listenerRegistration = mQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                querySnapshot.postValue(value);
                error.postValue(e);
            }
        });
    }

    public LiveData<QuerySnapshot> getQuerySnapshot() {
        return querySnapshot;
    }

    public LiveData<FirebaseFirestoreException> getFirestoreError() {
        return error;
    }

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

        attachSnapshotListener();
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

    @Override
    protected void onCleared() {
        super.onCleared();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    public static class Factory implements ViewModelProvider.Factory {
        private FirebaseFirestore mFirestore;

        public Factory(FirebaseFirestore firestore) {
            mFirestore = firestore;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return ((T) new MainFragmentViewModel(mFirestore));
        }
    }
}
