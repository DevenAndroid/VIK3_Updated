package com.vik3.ui.home;

import static com.vik3.ui.dashboard.RadioFragmentNew.getAnimation;
import static com.vik3.ui.dashboard.RadioFragmentNew.jcplayer;
import static com.vik3.utils.CommonMethods.share;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vik3.R;
import com.vik3.databinding.FragmentHomeBinding;
import com.vik3.prefraceMaager.PrefManagerPlayer;
import com.vik3.prefraceMaager.PreferenceManagerRadioFragment;
import com.vik3.ui.adapters.AdapterHomeAlbumList;
import com.vik3.ui.models.ModelCollections;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    public static ImageView imageBg, button;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        imageBg = binding.imageBg;
        button = binding.button;

        if (new PrefManagerPlayer(requireContext()).isPlay()) {
            if (jcplayer.isPlaying()) {
                getAnimation(true, binding.imageBg, binding.button);
            }
        } else {
            getAnimation(false, binding.imageBg, binding.button);
        }
        binding.button.setOnClickListener(view -> {
            if (jcplayer.isPlaying()) {
                jcplayer.pause();
                new PrefManagerPlayer(requireContext()).setPlay(false);
            } else {
                new PrefManagerPlayer(requireContext()).setPlay(true);
                jcplayer.continueAudio();
//                Glide.with(requireContext()).asGif().load(R.raw.loading_buffering).into(binding.button);
                final Handler handler1 = new Handler();
                int delay1 = 20000;
                handler1.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        handler1.postDelayed(this, delay1);
                        if (!jcplayer.isPlaying() && new PrefManagerPlayer(requireContext()).isPlay()) {
                            jcplayer.continueAudio();
                        }
                    }
                }, delay1);
            }
        });

        getHomeData();



        binding.textViewName.setMovementMethod(new ScrollingMovementMethod());

        binding.scrollview.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                binding.textViewName.getParent().requestDisallowInterceptTouchEvent(false);

                return false;
            }
        });

        binding.textViewName.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                binding.textViewName.getParent().requestDisallowInterceptTouchEvent(true);

                return false;
            }
        });



        if (new PreferenceManagerRadioFragment(requireContext()).getPrefData() != null) {
            try {
                binding.textViewName.setText(Objects.requireNonNull(new PreferenceManagerRadioFragment(requireContext()).getPrefData()).getCurrentTrack().getTitle().split("- ")[1]);
                binding.textViewSinger.setText(new PreferenceManagerRadioFragment(requireContext()).getPrefData().getCurrentTrack().getTitle().split("- ")[0]);
                Glide.with(requireContext())
                        .load(new PreferenceManagerRadioFragment(requireContext()).getPrefData().getCurrentTrack().getArtworkUrlLarge())
                        .into(binding.image);
            } catch (Exception ignored) { }
        }

        return root;
    }

    private void getHomeData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("wpPosts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        try {
                            List<ModelCollections> types = queryDocumentSnapshots.toObjects(ModelCollections.class);
                            Collections.reverse(types);
                            AdapterHomeAlbumList adapter = new AdapterHomeAlbumList(requireContext(), types);
                            binding.recyclerView.setAdapter(adapter);
                            adapter.setClickListener((view, position) -> getShareDialog(types.get(position)));
                        } catch (Exception ignored) {
                        }
                    }
                });
    }

    private void getShareDialog(ModelCollections model) {
        LayoutInflater factory = LayoutInflater.from(requireContext());
        final View view = factory.inflate(R.layout.dialog_share_album, null);
        final AlertDialog deleteDialog = new AlertDialog.Builder(requireContext()).create();
        deleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        deleteDialog.setView(view);
        TextView textView = view.findViewById(R.id.textView);
        textView.setText(model.getPost_title());
        TextView textViewSinger = view.findViewById(R.id.textViewSinger);
        textViewSinger.setText(model.getAuthor().getDisplay_name());
        TextView textViewSingerDate = view.findViewById(R.id.textViewSingerDate);

        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date newDate = null;
        try {
            newDate = spf.parse(model.getPost_date().split(" ")[0]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        spf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        String date = spf.format(Objects.requireNonNull(newDate));
        textViewSingerDate.setText(date);

        ImageView image = view.findViewById(R.id.image);
        Glide.with(requireContext())
                .load(model.getPost_content().split("src=\"")[1].split("\"")[0])
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(image);
        view.findViewById(R.id.button).setOnClickListener(v -> {
            share(requireContext(), model.getPermalink());
            deleteDialog.dismiss();
        });
        view.findViewById(R.id.imageClose).setOnClickListener(v -> deleteDialog.dismiss());

        deleteDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}