package com.vik3.ui.dashboard;

import static android.content.Context.TELEPHONY_SERVICE;
import static com.vik3.ui.activities.SplashScreen.isFirst;
import static com.vik3.utils.CommonMethods.getWebsite;
import static com.vik3.utils.CommonMethods.share;
import static com.vik3.utils.CommonMethods.showAlert;
import static com.vik3.utils.Constants.AUDIO_STREAMING_URL;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jean.jcplayer.JcPlayerManagerListener;
import com.example.jean.jcplayer.general.JcStatus;
import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.vik3.BroadcastReceiverBluetooth;
import com.vik3.BuildConfig;
import com.vik3.R;
import com.vik3.RemoteControlReceiver;
import com.vik3.YourBroadcastReceiver;
import com.vik3.apiClient.RetrofitClient;
import com.vik3.databinding.FragmentRadioBinding;
import com.vik3.prefraceMaager.PrefManagerPlayer;
import com.vik3.prefraceMaager.PreferenceManagerRadioFragment;
import com.vik3.ui.activities.SplashScreen;
import com.vik3.ui.adapters.AdapterHomeArtistDetailList;
import com.vik3.ui.adapters.AdapterSongList;
import com.vik3.ui.fragments.ProfileFragment;
import com.vik3.ui.home.HomeFragment;
import com.vik3.ui.models.Audio;
import com.vik3.ui.models.ModelArtist;
import com.vik3.ui.models.ModelImage;
import com.vik3.ui.models.ModelStations;
import com.vik3.utils.StorageUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RadioFragmentNew extends Fragment implements JcPlayerManagerListener {

    private FragmentRadioBinding binding;
    public static ImageView imageBg, button;
    public static JcPlayerView jcplayer;
    private ComponentName mRemoteControlResponder;
    private static Method mRegisterMediaButtonEventReceiver;
    private static Method mUnregisterMediaButtonEventReceiver;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRadioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        imageBg = binding.imageBg;
        button = binding.button;
        jcplayer = binding.jcplayer;

        root.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if( keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ) {
                    Toast.makeText(getContext(), "Dwn", Toast.LENGTH_SHORT).show();
                    //TODO SOMETHING
                    return true;
                } else if(keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE ) {
                    Toast.makeText(getContext(), "AWS", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return true;
            }
        });
        ArrayList<JcAudio> jcAudios = new ArrayList<>();
        if (new PreferenceManagerRadioFragment(requireContext()).getPrefData() != null) {
            try {
                jcAudios.add(JcAudio.createFromURL(Objects.requireNonNull(new PreferenceManagerRadioFragment(requireContext()).getPrefData()).getCurrentTrack().getTitle(), AUDIO_STREAMING_URL));
            } catch (Exception ignored) { }
        } else {
            jcAudios.add(JcAudio.createFromURL("Audio", AUDIO_STREAMING_URL));
        }

        binding.jcplayer.initPlaylist(jcAudios, null);
        binding.jcplayer.createNotification(R.mipmap.ic_launcher);
        binding.jcplayer.setJcPlayerManagerListener(this);

        if (new PrefManagerPlayer(requireContext()).isPlay()) {
            if (binding.jcplayer.isPlaying()) {
                new PrefManagerPlayer(requireContext()).setPlay(true);
                getAnimation(true, binding.imageBg, binding.button);
            } else {
                new PrefManagerPlayer(requireContext()).setPlay(false);
                getAnimation(false, binding.imageBg, binding.button);
            }
        } else {
            new PrefManagerPlayer(requireContext()).setPlay(false);
            if (!binding.jcplayer.isPlaying() && !isFirst) {
//                Toast.makeText(requireContext(), "Player init", Toast.LENGTH_SHORT).show();
                binding.jcplayer.continueAudio();
            } else {
                binding.jcplayer.pause();
            }
            getAnimation(false, binding.imageBg, binding.button);
        }


        AudioManager mAudioManager =  (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        ComponentName mReceiverComponent = new ComponentName(getActivity(), YourBroadcastReceiver.class);
        mAudioManager.registerMediaButtonEventReceiver(mReceiverComponent);
// somewhere else
        mAudioManager.unregisterMediaButtonEventReceiver(mReceiverComponent);


        IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);//"android.intent.action.MEDIA_BUTTON"
        BroadcastReceiverBluetooth r = new BroadcastReceiverBluetooth();
//        filter.setPriority(1000); //this line sets receiver priority
        getActivity().registerReceiver(r, filter);

        mRemoteControlResponder = new ComponentName(getActivity().getPackageName(),
                RemoteControlReceiver.class.getName());


        initializeRemoteControlRegistrationMethods();

        binding.button.setOnClickListener(view -> {
            SplashScreen.isFirstClicked = true;
            if (binding.jcplayer.isPlaying()) {
                binding.jcplayer.pause();
                new PrefManagerPlayer(requireContext()).setPlay(false);
            } else {
                new PrefManagerPlayer(requireContext()).setPlay(true);
                binding.jcplayer.continueAudio();
                final Handler handler1 = new Handler();
//                Glide.with(requireContext()).asGif().load(R.raw.loading_buffering).into(binding.button);
                int delay1 = 10000;
                handler1.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        handler1.postDelayed(this, delay1);
                        if (!binding.jcplayer.isPlaying() && new PrefManagerPlayer(requireContext()).isPlay()) {
//                            Toast.makeText(getContext(), "CL", Toast.LENGTH_SHORT).show();
                            binding.jcplayer.continueAudio();
                        }
                    }
                }, delay1);
            }
        });

        binding.buttonSleep.setOnClickListener(v -> getSleepMenus());

        if (new PreferenceManagerRadioFragment(requireContext()).getPrefData() != null) {
            setRadioSongHistory(new PreferenceManagerRadioFragment(requireContext()).getPrefData());
        }


        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    //Incoming call: Pause music
                    binding.jcplayer.pause();
                    new PrefManagerPlayer(requireContext()).setPlay(false);
                    getAnimation(false, binding.imageBg, binding.button);
                } else if(state == TelephonyManager.CALL_STATE_IDLE) {
                    //Not in call: Play music
                    new PrefManagerPlayer(requireContext()).setPlay(true);
                    getAnimation(true, binding.imageBg, binding.button);
                    binding.jcplayer.continueAudio();
                } else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    //A call is dialing, active or on hold
                    binding.jcplayer.pause();
                    getAnimation(false, binding.imageBg, binding.button);
                    new PrefManagerPlayer(requireContext()).setPlay(false);
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        TelephonyManager mgr = (TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        getArtist();
        final Handler handler = new Handler();
        int delay = 5000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getArtist();
                handler.postDelayed(this, delay);
            }
        }, delay);

        return root;
    }

    

//    private void unregisterRemoteControl() {
//        try {
//            if (mUnregisterMediaButtonEventReceiver == null) {
//                return;
//            }
//            mUnregisterMediaButtonEventReceiver.invoke(mAudioManager,
//                    mRemoteControlResponder);
//        } catch (InvocationTargetException ite) {
//            /* unpack original exception when possible */
//            Throwable cause = ite.getCause();
//            if (cause instanceof RuntimeException) {
//                throw (RuntimeException) cause;
//            } else if (cause instanceof Error) {
//                throw (Error) cause;
//            } else {
//                /* unexpected checked exception; wrap and re-throw */
//                throw new RuntimeException(ite);
//            }
//        } catch (IllegalAccessException ie) {
//            System.err.println("unexpected " + ie);
//        }
//    }

    private static void initializeRemoteControlRegistrationMethods() {
        try {
            if (mRegisterMediaButtonEventReceiver == null) {
                mRegisterMediaButtonEventReceiver = AudioManager.class.getMethod(
                        "registerMediaButtonEventReceiver",
                        new Class[] { ComponentName.class } );
            }
            if (mUnregisterMediaButtonEventReceiver == null) {
                mUnregisterMediaButtonEventReceiver = AudioManager.class.getMethod(
                        "unregisterMediaButtonEventReceiver",
                        new Class[] { ComponentName.class } );
            }
            /* success, this device will take advantage of better remote */
            /* control event handling                                    */
        } catch (NoSuchMethodException nsme) {
            /* failure, still using the legacy behavior, but this app    */
            /* is future-proof!                                          */
        }
    }


    @Override
    public void onCompletedAudio() {
        isFirst = true;
        binding.jcplayer.continueAudio();
//        Toast.makeText(getContext(), "CL1", Toast.LENGTH_SHORT).show();
        new PrefManagerPlayer(requireContext()).setPlay(false);
    }

    @Override
    public void onContinueAudio(@NonNull JcStatus jcStatus) {
//        Toast.makeText(getContext(), "CL2"+isFirst, Toast.LENGTH_SHORT).show();
        getAnimation(true, binding.imageBg, binding.button);
        new PrefManagerPlayer(requireContext()).setPlay(true);
        if (HomeFragment.button != null) {
            getAnimation(true, HomeFragment.imageBg, HomeFragment.button);
        }
    }

    @Override
    public void onJcpError(@NonNull Throwable throwable) {
//        Toast.makeText(getContext(), "CL3", Toast.LENGTH_SHORT).show();
        new PrefManagerPlayer(requireContext()).setPlay(false);
        getAnimation(false, binding.imageBg, binding.button);
        if (HomeFragment.button != null) {
            getAnimation(false, HomeFragment.imageBg, HomeFragment.button);
        }
    }

    @Override
    public void onPaused(@NonNull JcStatus jcStatus) {
//        Toast.makeText(getContext(), "CL4", Toast.LENGTH_SHORT).show();

        try {
            new PrefManagerPlayer(requireContext()).setPlay(false);
        } catch (Exception e){}
        getAnimation(false, binding.imageBg, binding.button);
        if (HomeFragment.button != null) {
            getAnimation(false, HomeFragment.imageBg, HomeFragment.button);
        }
    }

    @Override
    public void onPlaying(@NonNull JcStatus jcStatus) {
//        Toast.makeText(getContext(), "CL5", Toast.LENGTH_SHORT).show();
        new PrefManagerPlayer(requireContext()).setPlay(true);
        getAnimation(true, binding.imageBg, binding.button);
        if (HomeFragment.button != null) {
            getAnimation(true, HomeFragment.imageBg, HomeFragment.button);
        }
    }

    @Override
    public void onPreparedAudio(@NonNull JcStatus jcStatus) {
//        Toast.makeText(getContext(), "CL6:: "+jcStatus.getPlayState()+" : "+jcStatus.getPlayState(), Toast.LENGTH_SHORT).show();
            if (!isFirst || !SplashScreen.isFirstClicked) {
            binding.jcplayer.pause();
            getAnimation(false, binding.imageBg, binding.button);
            if (HomeFragment.button != null) {
                getAnimation(false, HomeFragment.imageBg, HomeFragment.button);
            }
        } else {
            getAnimation(true, binding.imageBg, binding.button);
            if (HomeFragment.button != null) {
                getAnimation(true, HomeFragment.imageBg, HomeFragment.button);
            }
        }
    }

    @Override
    public void onStopped(@NonNull JcStatus jcStatus) {
//        Toast.makeText(getContext(), "CL7", Toast.LENGTH_SHORT).show();
        new PrefManagerPlayer(requireContext()).setPlay(false);
    }

    @Override
    public void onTimeChanged(@NonNull JcStatus jcStatus) {
    }

    private void getArtist() {
        Call<ModelStations> call = RetrofitClient
                .getInstance()
                .getApiService()
                .getArtist();
        call.enqueue(new Callback<ModelStations>() {
            @Override
            public void onResponse(@NonNull Call<ModelStations> call, @NonNull Response<ModelStations> response) {
                setRadioSongHistory(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<ModelStations> call, @NonNull Throwable t) {

            }
        });
    }

    private void setRadioSongHistory(ModelStations response) {
        try {
            new PreferenceManagerRadioFragment(requireContext()).setPrefData(response);

            for (int i = 0; i < (response != null ? response.getHistory().size() : 0); i++) {
                response.getHistory().get(i).setUrl(response.getLogoUrl());
            }

            Objects.requireNonNull(response).getHistory().remove(0);

            Objects.requireNonNull(binding.jcplayer.getCurrentAudio()).setTitle(response.getCurrentTrack().getTitle());
            AdapterSongList adapter = new AdapterSongList(requireContext(), response.getHistory().subList(0, 10));
            binding.recyclerView.setAdapter(adapter);
            adapter.setClickListener((view, position) -> {
                if (response.getHistory().get(position).getData() != null) {
                    showBottomSheetDialog(response.getHistory().get(position).getData());
                } else {
                    showAlert(requireContext(), "Details on this song are not available yet.");
                }
            });

            binding.jcplayer.createNotification(R.mipmap.ic_launcher);

            binding.buttonShare.setOnClickListener(v -> share(requireContext(),
                    "Now Playing \"" + response.getCurrentTrack().getTitle().split("- ")[1] + "\" by \"" +
                            response.getCurrentTrack().getTitle().split("- ")[0] + "\" on Radio VIK3. Download Radio VIK3 via https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "  to the best christian songs. Follow us on Twitter - https://twitter.com/RadioVik3"));
            binding.textViewName.setText(Objects.requireNonNull(response).getCurrentTrack().getTitle().split("- ")[1]);
            binding.textViewSinger.setText(response.getCurrentTrack().getTitle().split("- ")[0]);

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


            Glide.with(requireContext())
                    .load(response.getCurrentTrack().getArtworkUrlLarge())
                    .into(binding.image);

            ArrayList<Audio> audioList = new ArrayList<>();
            audioList.add(new Audio(AUDIO_STREAMING_URL, response.getCurrentTrack().getTitle().split("- ")[1], response.getCurrentTrack().getTitle().split("- ")[0], response.getCurrentTrack().getTitle().split("- ")[0]));

            StorageUtil storage = new StorageUtil(requireContext());
//                        storage.clearCachedAudioPlaylist();
            storage.storeAudio(audioList);
            storage.storeAudioIndex(0);

        } catch (Exception ignored) {

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getSleepMenus() {
        PopupMenu menu = new PopupMenu(requireContext(), binding.buttonSleep);
        menu.getMenu().add(getString(R.string.minute_15));
        menu.getMenu().add(getString(R.string.minute_30));
        menu.getMenu().add(getString(R.string.minute_45));
        menu.getMenu().add(getString(R.string.minute_60));
        menu.getMenu().add(getString(R.string.timer_off));
        menu.show();
        menu.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("15 Minute")) {
                getSleep(getString(R.string.min_15), 15 * 60 * 1000);
            } else if (item.getTitle().equals("30 Minute")) {
                getSleep(getString(R.string.min_30), 30 * 60 * 1000);
            } else if (item.getTitle().equals("45 Minute")) {
                getSleep(getString(R.string.min_45), 45 * 60 * 1000);
            } else if (item.getTitle().equals("60 Minute")) {
                getSleep(getString(R.string.min_60), 60 * 60 * 1000);
            } else {
                binding.buttonSleep.setText("Sleep");
            }
            return false;
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getSleep(String time, long sleeperTime) {
        binding.jcplayer.continueAudio();
        /*if (!player.mediaPlayer.isPlaying()) {
            new PrefManagerPlayer(requireContext()).setPlay(true);
            getAnimation(true, binding.imageBg, binding.button);
            player.buildNotification(PlaybackStatus.PLAYING);
            //create the play action
            player.mediaPlayer.start();
        }*/
//        playButtonClick(getContext(), serviceConnection, binding.imageBg, binding.button);
//        if (mediaPlayerService != null && mediaPlayerService.mediaPlayer != null && !mediaPlayerService.mediaPlayer.isPlaying()) {
////            playMusic(requireContext(), binding.button, binding.imageBg);
//        }
        binding.buttonSleep.setText(time);
        new Handler().postDelayed(() -> {
            binding.buttonSleep.setText("Sleep");
            getAnimation(false, binding.imageBg, binding.button);
            binding.jcplayer.pause();
            new PrefManagerPlayer(getContext()).setPlay(true);
            /*if (player.mediaPlayer.isPlaying()) {
                //create the pause action
                player.mediaPlayer.pause();
                player.buildNotification(PlaybackStatus.PAUSED);
                getAnimation(false, binding.imageBg, binding.button);
                new PrefManagerPlayer(requireContext()).setPlay(false);
            }*/
//            stopAudio(getContext(), serviceConnection);
        }, sleeperTime);
    }

    private void showBottomSheetDialog(ModelArtist body) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.SheetDialog);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_singer_detail);

        List<ModelImage> images = new ArrayList<>();
        images.add(new ModelImage(0, getString(R.string.share), R.drawable.img_share));
        images.add(new ModelImage(1, getString(R.string.profile), R.drawable.img_profile));
        if (body.getSocialLinks() != null) {
            if (body.getSocialLinks().getFacebook() != null) {
                images.add(new ModelImage(2, getString(R.string.facebook), R.drawable.img_facebook));
            }
            if (body.getSocialLinks().getInstagram() != null) {
                images.add(new ModelImage(3, getString(R.string.instagram), R.drawable.img_instagram));
            }
            if (body.getSocialLinks().getTwitter() != null) {
                images.add(new ModelImage(4, getString(R.string.twitter), R.drawable.img_twitter));
            }
            if (body.getSocialLinks().getYouTube() != null) {
                images.add(new ModelImage(5, "YouTube", R.drawable.img_you_tube));
            }
        }
        AdapterHomeArtistDetailList adapterHomeArtistDetailList = new AdapterHomeArtistDetailList(requireContext(), images);
        RecyclerView recyclerView = bottomSheetDialog.findViewById(R.id.recyclerView);
        Objects.requireNonNull(recyclerView).setAdapter(adapterHomeArtistDetailList);
        adapterHomeArtistDetailList.setClickListener((view, position) -> {
            if (position == 0) {
                share(requireContext(),
                        "Now Playing \"" + body.getTitle().split("- ")[1] + "\" by \"" +
                                body.getTitle().split("- ")[0] + "\" on Radio VIK3. Download Radio VIK3 via https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "  to the best christian songs. Follow us on Twitter - https://twitter.com/RadioVik3");
            } else if (position == 1) {
                Fragment fragment = new ProfileFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("model", body);
                fragment.setArguments(bundle);
                fragment.setArguments(bundle);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.nav_host_fragment_activity_main, fragment)
                        .addToBackStack(null)
                        .commit();
                bottomSheetDialog.dismiss();
            } else if (position == 2) {
                getWebsite(requireContext(), body.getSocialLinks().getFacebook());
            } else if (position == 3) {
                getWebsite(requireContext(), body.getSocialLinks().getInstagram());
            } else if (position == 4) {
                getWebsite(requireContext(), body.getSocialLinks().getTwitter());
            }
        });
        TextView textView = bottomSheetDialog.findViewById(R.id.textView);
        TextView textViewSinger = bottomSheetDialog.findViewById(R.id.textViewSinger);
        Objects.requireNonNull(textView).setText(body.getTitle());
        Objects.requireNonNull(textViewSinger).setText(body.getBio());
        Button button = bottomSheetDialog.findViewById(R.id.button);
        ImageView imageView = bottomSheetDialog.findViewById(R.id.image);

        Glide.with(requireContext())
                .load(body.getImage())
                .into(Objects.requireNonNull(imageView));

        Objects.requireNonNull(button).setOnClickListener(v -> bottomSheetDialog.dismiss());
        bottomSheetDialog.show();
    }

    public static void getAnimation(boolean isAnimate, ImageView imageViewBg, ImageView button) {
        RotateAnimation rotate = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        if (isAnimate) {
            rotate.setDuration(5000);
            rotate.setInterpolator(new LinearInterpolator());
            rotate.setRepeatCount(Animation.INFINITE);
            imageViewBg.startAnimation(rotate);
            button.setImageResource(R.drawable.img_pause);
        } else {
            rotate.setDuration(0);
            rotate.setInterpolator(new LinearInterpolator());
            imageViewBg.startAnimation(rotate);
            button.setImageResource(R.drawable.img_play);
        }
    }

}