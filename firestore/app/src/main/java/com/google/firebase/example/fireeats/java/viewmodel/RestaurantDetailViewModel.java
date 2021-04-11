package com.google.firebase.example.fireeats.java.viewmodel;

import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.example.fireeats.java.model.Rating;
import com.google.firebase.example.fireeats.java.model.Restaurant;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

public class RestaurantDetailViewModel extends ViewModel {

    // TODO (rosariopfernandes): Use dependency injection here
    private final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private DocumentReference mRestaurantRef;
    private Query mRatingsQuery;

    public RestaurantDetailViewModel(String restaurantId) {
        // Get reference to the restaurant
        mRestaurantRef = mFirestore.collection("restaurants").document(restaurantId);

        mRatingsQuery = mRestaurantRef
                .collection("ratings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50);
    }

    public Query getRatingsQuery() {
        return mRatingsQuery;
    }

    public DocumentReference getRestaurantRef() {
        return mRestaurantRef;
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
}
