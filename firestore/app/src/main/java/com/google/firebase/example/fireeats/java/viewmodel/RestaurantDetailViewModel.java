package com.google.firebase.example.fireeats.java.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.Task;
import com.google.firebase.example.fireeats.java.model.Rating;
import com.google.firebase.example.fireeats.java.model.Restaurant;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

public class RestaurantDetailViewModel extends ViewModel {

    private final FirebaseFirestore mFirestore;
    private final DocumentReference mRestaurantRef;

    private final ListenerRegistration restaurantListener;
    private final ListenerRegistration ratingsListener;
    private final MutableLiveData<DocumentSnapshot> restaurantSnapshot;
    private final MutableLiveData<QuerySnapshot> ratingsSnapshot;
    private final MutableLiveData<FirebaseFirestoreException> error;

    public RestaurantDetailViewModel(FirebaseFirestore firestore, String restaurantId) {
        mFirestore = firestore;
        restaurantSnapshot = new MutableLiveData<>();
        ratingsSnapshot = new MutableLiveData<>();
        error = new MutableLiveData<>();

        // Get reference to the restaurant
        mRestaurantRef = mFirestore.collection("restaurants").document(restaurantId);
        restaurantListener = mRestaurantRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException e) {
                restaurantSnapshot.postValue(value);
                error.postValue(e);
            }
        });

        Query mRatingsQuery = mRestaurantRef
                .collection("ratings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50);
        ratingsListener = mRatingsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                ratingsSnapshot.postValue(value);
                error.postValue(e);
            }
        });
    }

    public LiveData<DocumentSnapshot> getRestaurantSnapshot() {
        return restaurantSnapshot;
    }

    public LiveData<QuerySnapshot> getRatingsSnapshot() {
        return ratingsSnapshot;
    }

    public LiveData<FirebaseFirestoreException> getFirestoreError() {
        return error;
    }

    public Task<Void> addRating(final Rating rating) {
        // In a transaction, add the new rating and update the aggregate totals
        // Create reference for new rating, for use inside the transaction
        final DocumentReference ratingRef = mRestaurantRef.collection("ratings").document();

        // In a transaction, add the new rating and update the aggregate totals
        return mFirestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                Restaurant restaurant = transaction.get(mRestaurantRef).toObject(Restaurant.class);

                // Compute new number of ratings
                int newNumRatings = restaurant.getNumRatings() + 1;

                // Compute new average rating
                double oldRatingTotal = restaurant.getAvgRating() * restaurant.getNumRatings();
                double newAvgRating = (oldRatingTotal + rating.getRating()) / newNumRatings;

                // Set new restaurant info
                restaurant.setNumRatings(newNumRatings);
                restaurant.setAvgRating(newAvgRating);

                // Commit to Firestore
                transaction.set(mRestaurantRef, restaurant);
                transaction.set(ratingRef, rating);

                return null;
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (restaurantListener != null) {
            restaurantListener.remove();
        }
        if (ratingsListener != null) {
            ratingsListener.remove();
        }
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final FirebaseFirestore mFirestore;
        private final String mRestaurantId;

        public Factory(String restaurantId) {
            this(FirebaseFirestore.getInstance(), restaurantId);
        }

        public Factory(FirebaseFirestore firestore, String restaurantId) {
            mFirestore = firestore;
            mRestaurantId = restaurantId;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return ((T) new RestaurantDetailViewModel(mFirestore, mRestaurantId));
        }
    }
}
