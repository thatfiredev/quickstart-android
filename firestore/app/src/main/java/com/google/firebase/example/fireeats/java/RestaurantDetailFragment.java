package com.google.firebase.example.fireeats.java;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.example.fireeats.R;
import com.google.firebase.example.fireeats.databinding.FragmentRestaurantDetailBinding;
import com.google.firebase.example.fireeats.java.adapter.RatingAdapter;
import com.google.firebase.example.fireeats.java.model.Rating;
import com.google.firebase.example.fireeats.java.model.Restaurant;
import com.google.firebase.example.fireeats.java.util.RestaurantUtil;
import com.google.firebase.example.fireeats.java.viewmodel.RestaurantDetailViewModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

public class RestaurantDetailFragment extends Fragment
        implements RatingDialogFragment.RatingListener, View.OnClickListener {

    private static final String TAG = "RestaurantDetail";

    private FragmentRestaurantDetailBinding mBinding;
    
    private RatingDialogFragment mRatingDialog;

    private RatingAdapter mRatingAdapter;

    private RestaurantDetailViewModel mViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentRestaurantDetailBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.restaurantButtonBack.setOnClickListener(this);
        mBinding.fabShowRatingDialog.setOnClickListener(this);

        String restaurantId = RestaurantDetailFragmentArgs.fromBundle(getArguments()).getKeyRestaurantId();

        ViewModelProvider.Factory factory = new RestaurantDetailViewModel.Factory(restaurantId);
        mViewModel = new ViewModelProvider(this, factory).get(RestaurantDetailViewModel.class);

        // RecyclerView
        mRatingAdapter = new RatingAdapter();
        mBinding.recyclerRatings.setLayoutManager(new LinearLayoutManager(requireContext()));
        mBinding.recyclerRatings.setAdapter(mRatingAdapter);

        mRatingDialog = new RatingDialogFragment();

        // Get ratings
        mViewModel.getRatingsSnapshot().observe(getViewLifecycleOwner(), new Observer<QuerySnapshot>() {
            @Override
            public void onChanged(QuerySnapshot querySnapshot) {
                if (querySnapshot != null) {
                    if (querySnapshot.getDocuments().isEmpty()) {
                        mBinding.recyclerRatings.setVisibility(View.GONE);
                        mBinding.viewEmptyRatings.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.recyclerRatings.setVisibility(View.VISIBLE);
                        mBinding.viewEmptyRatings.setVisibility(View.GONE);
                    }
                    mRatingAdapter.submitQuerySnapshot(querySnapshot);
                }
            }
        });

        mViewModel.getRestaurantSnapshot().observe(getViewLifecycleOwner(), new Observer<DocumentSnapshot>() {
            @Override
            public void onChanged(DocumentSnapshot snapshot) {
                if (snapshot != null) {
                    onRestaurantLoaded(snapshot.toObject(Restaurant.class));
                }
            }
        });

        mViewModel.getFirestoreError().observe(getViewLifecycleOwner(), new Observer<FirebaseFirestoreException>() {
            @Override
            public void onChanged(FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "restaurant:onEvent", e);
                }
            }
        });
    }

    private void onRestaurantLoaded(Restaurant restaurant) {
        mBinding.restaurantName.setText(restaurant.getName());
        mBinding.restaurantRating.setRating((float) restaurant.getAvgRating());
        mBinding.restaurantNumRatings.setText(getString(R.string.fmt_num_ratings, restaurant.getNumRatings()));
        mBinding.restaurantCity.setText(restaurant.getCity());
        mBinding.restaurantCategory.setText(restaurant.getCategory());
        mBinding.restaurantPrice.setText(RestaurantUtil.getPriceString(restaurant));

        // Background image
        Glide.with(mBinding.restaurantImage.getContext())
                .load(restaurant.getPhoto())
                .into(mBinding.restaurantImage);
    }

    public void onBackArrowClicked(View view) {
        requireActivity().onBackPressed();
    }

    public void onAddRatingClicked(View view) {
        mRatingDialog.show(getChildFragmentManager(), RatingDialogFragment.TAG);
    }

    @Override
    public void onRating(Rating rating) {
        mViewModel.addRating(rating)
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Rating added");

                        // Hide keyboard and scroll to top
                        hideKeyboard();
                        mBinding.recyclerRatings.smoothScrollToPosition(0);
                    }
                })
                .addOnFailureListener(requireActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Add rating failed", e);

                        // Show failure message and hide keyboard
                        hideKeyboard();
                        Snackbar.make(mBinding.getRoot(), "Failed to add rating",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.restaurantButtonBack:
                onBackArrowClicked(v);
                break;
            case R.id.fabShowRatingDialog:
                onAddRatingClicked(v);
                break;
        }
    }

}
