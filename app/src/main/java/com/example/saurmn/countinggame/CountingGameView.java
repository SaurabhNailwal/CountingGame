package com.example.saurmn.countinggame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by SaurabhMN on 3/27/2015.
 */
public class CountingGameView extends View{


    // variables for managing the game
    private int spotsTouched; // number of numSpots touched
    private int remainingTime; // current remaining time
    private int level; // current level
    private int viewWidth; // stores the width of this View
    private int viewHeight; // stores the height of this view
    private long animationTime; // how long each spot remains on the screen
    private boolean gameOver; // whether the game has ended
    private boolean gamePaused; // whether the game has ended
    private boolean dialogDisplayed; // whether the game has ended
    private int totalElapsedTime; // total elapsed time
    private int currentIndex;// stores the index of spot array which is to be removed first

    // collections of numSpots (ImageViews) and Animators
    private final Queue<ImageView> numSpots =
            new ConcurrentLinkedQueue<ImageView>();
    private final Queue<Animator> animators =
            new ConcurrentLinkedQueue<Animator>();

    int[] spotArray = new int[10];//storing the spot number values on each level

    private TextView totalElapsedTimeTextView; // displays high remaningTime
    private TextView remainingTimeTextView; // displays current remaningTime
    private TextView levelTextView; // displays current level
    //private LinearLayout livesLinearLayout; // displays lives remaining
    private RelativeLayout relativeLayout; // displays numSpots
    private Resources resources; // used to load resources
    private LayoutInflater layoutInflater; // used to inflate GUIs

    // time in milliseconds for spot and touched spot animations
    private static final int INITIAL_ANIMATION_DURATION = 20000;
    private static final Random random = new Random(); // for random coords
    private static final int SPOT_DIAMETER = 100; // initial spot size
    private static final float SCALE_X = 1; // end animation x scale
    private static final float SCALE_Y = 1; // end animation y scale
    private static final int INITIAL_SPOTS = 5; // initial # of numSpots
    private static final int SPOT_DELAY = 500; // delay in milliseconds
    private static final int TIME_DELAY = 1000; // delay in milliseconds
    private static final int NEW_LEVEL = 10; // numSpots to reach new level
    private Handler spotHandler; // adds new numSpots to the game
    private Handler timeHandler; // to handle the time updates
    private Thread spotThread;

    // sound IDs, constants and variables for the game's sounds
    private static final int ONE_SOUND_ID = 1;
    private static final int TWO_SOUND_ID = 2;
    private static final int THREE_SOUND_ID = 3;
    private static final int FOUR_SOUND_ID = 4;
    private static final int FIVE_SOUND_ID = 5;
    private static final int SIX_SOUND_ID = 6;
    private static final int SEVEN_SOUND_ID = 7;
    private static final int EIGHT_SOUND_ID = 8;
    private static final int NINE_SOUND_ID = 9;
    private static final int TEN_SOUND_ID = 10;
    private static final int ELEVEN_SOUND_ID = 11;
    private static final int TWELVE_SOUND_ID = 12;
    private static final int THIRTEEN_SOUND_ID = 13;
    private static final int FOURTEEN_SOUND_ID = 14;
    private static final int FIFTEEN_SOUND_ID = 15;
    private static final int SIXTEEN_SOUND_ID = 16;
    private static final int SEVENTEEN_SOUND_ID = 17;
    private static final int EIGHTEEN_SOUND_ID = 18;
    private static final int NINETEEN_SOUND_ID = 19;
    private static final int TWENTY_SOUND_ID = 20;
    private static final int TWENTY_ONE_SOUND_ID = 21;
    private static final int TWENTY_TWO_SOUND_ID = 22;
    private static final int TWENTY_THREE_SOUND_ID = 23;
    private static final int TWENTY_FOUR_SOUND_ID = 24;
    private static final int TWENTY_FIVE_SOUND_ID = 25;
    private static final int TWENTY_SIX_SOUND_ID = 26;
    private static final int TWENTY_SEVEN_SOUND_ID = 27;
    private static final int TWENTY_EIGHT_SOUND_ID = 28;
    private static final int TWENTY_NINE_SOUND_ID = 29;
    private static final int THIRTY_SOUND_ID = 30;
    private static final int APPLAUSE_SOUND_ID = 31; // after level 4 completion
    private static final int INCORRECT_GUESS_SOUND_ID = 32;// uhoh sound

    private static final int SOUND_PRIORITY = 1;
    private static final int SOUND_QUALITY = 100;
    private static final int MAX_STREAMS = 4;
    private int spotValue = 0;// stores the value of the spot to be added
    private SoundPool soundPool; // plays sound effects
    private int volume; // sound effect volume
    private Map<Integer,String> imageMap;
    private Map<Integer, Integer> soundMap; // maps ID to soundpool


    // constructs a new CountingGameView
    public CountingGameView(Context context,
                            RelativeLayout parentLayout)
    {
        super(context);

        // save Resources for loading external values
        resources = context.getResources();

        // save LayoutInflater
        layoutInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        // get references to various GUI components
        relativeLayout = parentLayout;

        totalElapsedTimeTextView = (TextView) relativeLayout.findViewById(
                R.id.totalElapsedTimeTextView);
        remainingTimeTextView = (TextView) relativeLayout.findViewById(
                R.id.remainingTimeTextView);
        levelTextView = (TextView) relativeLayout.findViewById(
                R.id.levelTextView);

        spotHandler = new Handler(); // used to add numSpots when game starts
        timeHandler = new Handler(); // used to update time
    } // end CountingGameView constructor

    // store CountingGameView's width/height
    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh)
    {
        viewWidth = width; // save the new width
        viewHeight = height; // save the new height
    } // end method onSizeChanged

    // called by the CountingGameView Activity when it receives a call to onPause
    public void pause()
    {
        gamePaused = true;
        soundPool.release(); // release audio resources
        soundPool = null;
        cancelAnimations(); // cancel all outstanding animations
    } // end method pause

    // cancel animations and remove ImageViews representing numSpots
    private void cancelAnimations()
    {
        // cancel remaining animations
        for (Animator animator : animators)
            animator.cancel();

        // remove remaining numSpots from the screen
        for (ImageView view : numSpots)
            relativeLayout.removeView(view);

        spotHandler.removeCallbacks(addSpotRunnable);
        timeHandler.removeCallbacks(updateTimeRunnable);
        animators.clear();
        numSpots.clear();
    } // end method cancelAnimations

    // called by the CountingGameView Activity when it receives a call to onResume
    public void resume(Context context)
    {
        gamePaused = false;
        initializeSoundEffects(context); // initialize app's SoundPool

        if (!dialogDisplayed)
            resetGame(); // start the game
    } // end method resume

    // start a new game
    public void resetGame()
    {
        numSpots.clear(); // empty the List of numSpots
        animators.clear(); // empty the List of Animators

        animationTime = INITIAL_ANIMATION_DURATION; // init animation length
        //spotsTouched = 0; // reset the number of numSpots touched
        totalElapsedTime = 0; // reset the total elapsed time
        remainingTime = 60; //reset time remaining for level 1

        level = 1; // reset the level
        gameOver = false; // the game is not over
        currentIndex = 0; // reset the index value to 0
        spotsTouched =0; // reset the spotsTouched variable

        // add new numSpots for level
        addLevelSpots();

        // display scores and level
        timeHandler.postDelayed(updateTimeRunnable, TIME_DELAY);

    } // end method resetGame


    // create the app's SoundPool for playing game audio
    private void initializeSoundEffects(Context context)
    {
        // initialize SoundPool to play the app's three sound effects
        soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC,
                SOUND_QUALITY);

        // set sound effect volume
        AudioManager manager =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        volume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);

        //create image map---- ask
        //imageMap

        // create sound map
        soundMap = new HashMap<Integer, Integer>(); // create new HashMap

        // add each sound effect to the SoundPool
        soundMap.put(ONE_SOUND_ID, soundPool.load(context,R.raw.onevoice,SOUND_PRIORITY));
        soundMap.put(TWO_SOUND_ID, soundPool.load(context,R.raw.twovoice,SOUND_PRIORITY));
        soundMap.put(THREE_SOUND_ID, soundPool.load(context,R.raw.threevoice,SOUND_PRIORITY));
        soundMap.put(FOUR_SOUND_ID,soundPool.load(context,R.raw.fourvoice,SOUND_PRIORITY));
        soundMap.put(FIVE_SOUND_ID,soundPool.load(context,R.raw.fivevoice,SOUND_PRIORITY));
        soundMap.put(SIX_SOUND_ID,soundPool.load(context,R.raw.sixvoice,SOUND_PRIORITY));
        soundMap.put(SEVEN_SOUND_ID,soundPool.load(context,R.raw.sevenvoice,SOUND_PRIORITY));
        soundMap.put(EIGHT_SOUND_ID,soundPool.load(context,R.raw.eightvoice,SOUND_PRIORITY));
        soundMap.put(NINE_SOUND_ID,soundPool.load(context,R.raw.ninevoice,SOUND_PRIORITY));
        soundMap.put(TEN_SOUND_ID,soundPool.load(context,R.raw.tenvoice,SOUND_PRIORITY));
        soundMap.put(ELEVEN_SOUND_ID,soundPool.load(context,R.raw.elevenvoice,SOUND_PRIORITY));
        soundMap.put(TWELVE_SOUND_ID,soundPool.load(context,R.raw.twelvevoice,SOUND_PRIORITY));
        soundMap.put(THIRTEEN_SOUND_ID,soundPool.load(context,R.raw.thirteenvoice,SOUND_PRIORITY));
        soundMap.put(FOURTEEN_SOUND_ID,soundPool.load(context,R.raw.fourteenvoice,SOUND_PRIORITY));
        soundMap.put(FIFTEEN_SOUND_ID,soundPool.load(context,R.raw.fifteenvoice,SOUND_PRIORITY));
        soundMap.put(SIXTEEN_SOUND_ID,soundPool.load(context,R.raw.sixteenvoice,SOUND_PRIORITY));
        soundMap.put(SEVENTEEN_SOUND_ID,soundPool.load(context,R.raw.seventeenvoice,SOUND_PRIORITY));
        soundMap.put(EIGHTEEN_SOUND_ID,soundPool.load(context,R.raw.eighteenvoice,SOUND_PRIORITY));
        soundMap.put(NINETEEN_SOUND_ID,soundPool.load(context,R.raw.nineteenvoice,SOUND_PRIORITY));
        soundMap.put(TWENTY_SOUND_ID,soundPool.load(context,R.raw.twentyvoice,SOUND_PRIORITY));
        soundMap.put(TWENTY_ONE_SOUND_ID,soundPool.load(context,R.raw.twentyonevoice,SOUND_PRIORITY));
        soundMap.put(TWENTY_TWO_SOUND_ID,soundPool.load(context,R.raw.twentytwovoice,SOUND_PRIORITY));
        soundMap.put(TWENTY_THREE_SOUND_ID,soundPool.load(context,R.raw.twentythreevoice,SOUND_PRIORITY));
        soundMap.put(TWENTY_FOUR_SOUND_ID,soundPool.load(context,R.raw.twentyfourvoice,SOUND_PRIORITY));
        soundMap.put(TWENTY_FIVE_SOUND_ID,soundPool.load(context,R.raw.twentyfivevoice,SOUND_PRIORITY));
        soundMap.put(TWENTY_SIX_SOUND_ID,soundPool.load(context,R.raw.twentysixvoice,SOUND_PRIORITY));
        soundMap.put(TWENTY_SEVEN_SOUND_ID,soundPool.load(context,R.raw.twentysevenvoice,SOUND_PRIORITY));
        soundMap.put(TWENTY_EIGHT_SOUND_ID,soundPool.load(context,R.raw.twentyeightvoice,SOUND_PRIORITY));
        soundMap.put(TWENTY_NINE_SOUND_ID,soundPool.load(context,R.raw.twentyninevoice,SOUND_PRIORITY));
        soundMap.put(THIRTY_SOUND_ID,soundPool.load(context,R.raw.thirtyvoice,SOUND_PRIORITY));
        soundMap.put(APPLAUSE_SOUND_ID,
                soundPool.load(context, R.raw.applause, SOUND_PRIORITY));
        soundMap.put(INCORRECT_GUESS_SOUND_ID, soundPool.load(context,R.raw.uhoh, SOUND_PRIORITY));
        //soundMap.put(DISAPPEAR_SOUND_ID,soundPool.load(context, R.raw.disappear, SOUND_PRIORITY));
        //soundMap.put(MISS_SOUND_ID,soundPool.load(context, R.raw.miss, SOUND_PRIORITY));

    } // end method initializeSoundEffect

    //call the function to update time
    private Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            displayTime(); // update time after each second
        }// end method run
    }; //end Runnable


    // display total elapsed time and remaining time and level
    private void displayTime()
    {
        // display the high remainingTime, current remainingTime and level
        totalElapsedTimeTextView.setText(
                resources.getString(R.string.total_elapsed_time) + " " + totalElapsedTime);
        remainingTimeTextView.setText(
                resources.getString(R.string.remaining_time) + " " + remainingTime);
        levelTextView.setText(resources.getString(R.string.level) + " " + level);

        if(remainingTime > 0) {
            totalElapsedTime += 1;
            remainingTime -= 1;
            timeHandler.postDelayed(updateTimeRunnable, TIME_DELAY);
        }else{
            //gameOver
            gameOver = true;
            cancelAnimations();

            //dialog to show GameOver and level reached
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
            dialogBuilder.setTitle(R.string.game_over);
            dialogBuilder.setMessage("Level "+level+" failed");
            dialogBuilder.setPositiveButton(R.string.reset_game,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialogDisplayed = false;
                            resetGame();
                        } // end of onClick
                    }// end of dialogInterface
            );

            dialogDisplayed = true;
            dialogBuilder.show(); //display the reset dame dialog
        }
    } // end function displayTime


    // adds a new spot at a random location and starts its animation
    public void addLevelSpots()
    {
        spotThread = new Thread(levelStart);
        spotThread.start();

    } // end addLevelSpots method

    private Runnable levelStart = new Runnable() {
        @Override
        public void run() {
            int counter = 0;
            //create a spot as per level
            if(level == 1){
                //create level 1 spots from 1 to 10
                for(spotValue=1; spotValue<= 10;spotValue++,counter++){
                    spotArray[counter] = spotValue;
                    spotHandler.postDelayed(addSpotRunnable, SPOT_DELAY);

                    try {
                        Thread.sleep(SPOT_DELAY*2);//delay to add spot before incrementing loop
                    }catch (Exception e){
                        e.getMessage();
                    }

                }
            }else if (level == 2){
                //create level 2 spots from 11 to 20
                for(counter=0,spotValue=11; spotValue<= 20;spotValue++, counter++){
                    spotArray[counter] = spotValue;
                    spotHandler.postDelayed(addSpotRunnable, SPOT_DELAY);

                    try {
                        Thread.sleep(SPOT_DELAY*2);//delay to add spot before incrementing loop
                    }catch (Exception e){
                        e.getMessage();
                    }

                }
            }else if(level == 3){
                //create level 3 spots from 21 to 30
                for(counter=0,spotValue=21; spotValue<= 30;spotValue++, counter++){
                    spotArray[counter] = spotValue;
                    spotHandler.postDelayed(addSpotRunnable,SPOT_DELAY);
                    try {
                        Thread.sleep(SPOT_DELAY*2);//delay to add spot before incrementing loop
                    }catch (Exception e){
                        e.getMessage();
                    }

                }
            }else{
                //create level 4 spots between 1 to 30
                ArrayList<Integer> numList = new ArrayList<Integer>();
                List<Integer> sortedList;

                for(int i=1; i<=30;i++){
                    numList.add(i);
                }
                Collections.shuffle(numList);
                //getting top 10
                sortedList = numList.subList(0,10);

                //sorting the top 10
                Collections.sort(sortedList);

                for(int i=0; i< 10;i++) {
                    spotArray[i] = sortedList.get(i);
                }


                for(int j=0; j< 10;j++) {
                    spotValue = spotArray[j];
                    spotHandler.postDelayed(addSpotRunnable, SPOT_DELAY);
                    try {
                        Thread.sleep(SPOT_DELAY * 2);//delay to add spot before incrementing loop
                    } catch (Exception e) {
                        e.getMessage();
                    }

                }
            }
        }
    };

    // Runnable used to add new numSpots to the game at the start
    private Runnable addSpotRunnable = new Runnable()
    {
        public void run()
        {
            createNewSpot(spotValue); // add spots to the game as per level
        } // end method run
    }; // end Runnable

    // adjust the width position
    private int widthCloseness(int dist){
        if(dist < 100){
            return dist+100;
        }else if(dist > (viewWidth -100)){
            return dist-100;
        }

        return dist;
    }

    // adjust the height position
    private int heightCloseness(int dist){
        if(dist < 100){
            return dist+100;
        }else if(dist > (viewHeight -100)){
            return dist-100;
        }

        return dist;
    }

    // function to create the spot as per number provided
    private void createNewSpot(int input){

        // choose two random coordinates for the starting points
        int x = random.nextInt(viewWidth - SPOT_DIAMETER);
        int y = random.nextInt(viewHeight - SPOT_DIAMETER);

        //to make sure the position is away from the borders
        x = widthCloseness(x);
        y = heightCloseness(y);

        // choose two random coordinates for the ending points
        int x2 = random.nextInt(viewWidth - SPOT_DIAMETER);
        int y2 = random.nextInt(viewHeight - SPOT_DIAMETER);

        //to make sure the position is away from the borders
        x2 = widthCloseness(x2);
        y2 = heightCloseness(y2);

        final ImageView spot = (ImageView) layoutInflater.inflate(R.layout.untouched, null);

        // decide case as per the level and create new spot
        switch(input) {

            case 1: //create spot for 1
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.one);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 2: //create spot for 2
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.two);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 3: //create spot for 3
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.three);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 4: //create spot for 4
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.four);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 5: //create spot for 5
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.five);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 6: //create spot for 6
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.six);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 7: //create spot for 7
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.seven);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 8: //create spot for 8
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.eight);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 9: //create spot for 9
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.nine);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 10: //create spot for 10
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.ten);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 11: //create spot for 11
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.eleven);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 12: //create spot for 12
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.twelve);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 13: //create spot for 13
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.thirteen);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 14: //create spot for 14
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.fourteen);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 15: //create spot for 15
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.fifteen);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 16: //create spot for 16
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.sixteen);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 17: //create spot for 17
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.seventeen);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 18: //create spot for 18
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.eighteen);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 19: //create spot for 19
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.nineteen);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 20: //create spot for 20
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.twenty);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 21: //create spot for 21
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.twentyone);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 22: //create spot for 22
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.twentytwo);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 23: //create spot for 23
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.twentythree);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 24: //create spot for 24
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.twentyfour);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 25: //create spot for 25
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.twentyfive);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 26: //create spot for 26
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.twentysix);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 27: //create spot for 27
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.twentyseven);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 28: //create spot for 28
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.twentyeight);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 29: //create spot for 29
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.twentynine);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            case 30: //create spot for 30
                numSpots.add(spot);
                spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
                spot.setImageResource(R.drawable.thirty);
                spot.setX(x);//starting X location
                spot.setY(y);//starting Y location
                spot.setId(input);
                spot.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                touchedSpot(spot);
                            }
                        }
                );
                relativeLayout.addView(spot);//adding spot to screen
                break;

            default: //throw exception
                try {
                    throw new IllegalArgumentException();

                }catch (Exception e){
                    System.out.println(e.getMessage()+ " -> "+ input);
                }

        }

        // configure and start spot's animation
        spot.animate().x(x2).y(y2).scaleX(SCALE_X).scaleY(SCALE_Y)
                .setDuration(animationTime).setListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        animators.add(animation); // save for possible cancel
                    } // end method onAnimationStart
                }

        ); // end call to setListener


    }


    // called when a spot is touched
    private void touchedSpot(ImageView spot)
    {
        if(remainingTime > 0) {
            if (spotArray[currentIndex] == spot.getId()) {
                //Correctly spotted the number, play the number

                relativeLayout.removeView(spot); // remove touched spot from screen
                numSpots.remove(spot); // remove old spot from list

                // play the hit sounds
                if (soundPool != null)
                    soundPool.play(spotArray[currentIndex], volume, volume,
                            SOUND_PRIORITY, 0, 1f);

                ++spotsTouched; // increment the number of numSpots touched
                ++currentIndex; // increase current index

                // increment level if player touched 10 numSpots in the current level
                if (spotsTouched % NEW_LEVEL == 0) {

                    if(level == 4){

                        //gameOver
                        gameOver = true;
                        cancelAnimations();

                        //if level 4 completed then play applause and show game completed dialog
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                        dialogBuilder.setTitle(R.string.game_completed);
                        dialogBuilder.setMessage(R.string.congrats_message);
                        dialogBuilder.setPositiveButton(R.string.reset_game,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        dialogDisplayed = false;
                                        resetGame();
                                    }
                                });

                        dialogDisplayed = true;
                        dialogBuilder.show();

                        //Game Completed, play the applause
                        soundPool.play(APPLAUSE_SOUND_ID, volume, volume, SOUND_PRIORITY, 1, 1f);
                    }


                    currentIndex = 0; // reset the index value to 0
                    ++level; // increment the level
                    animationTime -= 500; // make game faster than prior level
                    if(level == 2) {
                        remainingTime = 55;// level 2 time
                    }else if(level ==3){
                        remainingTime = 50;// level 3 time
                    }else{
                        remainingTime = 45;// level 4 time
                    }

                    totalElapsedTime = 0;
                    spotsTouched = 0;

                    //Load spots for next level
                    if (!gameOver)
                        addLevelSpots(); // add spots for next level

                } // end if

            } else {
                //Incorrect spot, play sound "uh oh"
                soundPool.play(INCORRECT_GUESS_SOUND_ID, volume, volume, SOUND_PRIORITY, 0, 1f);

            }
        }else{

            //gameOver
            gameOver = true;
            cancelAnimations();

            //dialog to show GameOver and level reached
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
            dialogBuilder.setTitle(R.string.game_over);
            dialogBuilder.setMessage("Level "+level+" failed");
            dialogBuilder.setPositiveButton(R.string.reset_game,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialogDisplayed = false;
                            resetGame();

                        } // end of onClick
                    }// end of dialogInterface
            );

            dialogDisplayed = true;
            dialogBuilder.show(); //display the reset dame dialog
        }
    } // end method touchedSpot
}
