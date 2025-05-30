package com.example.sabacc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.FirebaseApp;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private Deck deck;
    private Player player;
    private Card openCard;
    private int selectedCardIndex = -1;
    private boolean isOpenCardSelected = false;
    private boolean isDeckSelected = false;
    private List<Bot> bots = new ArrayList<>();
    private int currentBotIndex = 0;
    private int currentRound = 1;
    private boolean isWaitingForHumanAction = true;
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.main).setOnClickListener(this::onClick);
        findViewById(R.id.openCardView).setOnClickListener(v -> {
            if(openCard != null){
                isOpenCardSelected = true;
                showOpenCardOption();
            }
        });
        findViewById(R.id.deckView).setOnClickListener(v -> {
            if(openCard != null){
                isDeckSelected = true;
                showDeckOption();
            }
        });
        findViewById(R.id.skipTurnBtn).setOnClickListener(view -> {
            isWaitingForHumanAction = false;
            hideActionPanel();
        });

        findViewById(R.id.exitBtn).setOnClickListener(view -> toStartScreen());
        findViewById(R.id.againBtn).setOnClickListener(view -> startNewGame());

        deck = new Deck();
        deck.shuffle();
        player = new Player(deck);
        player.drawCardFromDeck();
        player.drawCardFromDeck();
        updateHandUI();

        openCard = deck.drawCard();
        updateOpenCardUI();

        for(int i = 0; i < 3; i++){
            bots.add(new Bot(this, deck));
        }
        updateBotsUI();
    }
    
    public void onClick(View v){
        if(v.getId() == R.id.main){
            LinearLayout actionPanel = findViewById(R.id.actionPanel);
            Button skipTurnBtn = findViewById(R.id.skipTurnBtn);
            findViewById(R.id.replaceWithOpenBtn).setVisibility(View.GONE);
            findViewById(R.id.replaceFromDeckBtn).setVisibility(View.GONE);
            findViewById(R.id.addCardFromDeckBtn).setVisibility(View.GONE);
            findViewById(R.id.addOpenCardBtn).setVisibility(View.GONE);
            findViewById(R.id.text4btns).setVisibility(View.GONE);
            skipTurnBtn.setVisibility(View.VISIBLE);
            actionPanel.setVisibility(View.VISIBLE);
        }
    }

    private void updateHandUI(){
        LinearLayout handContainer = findViewById(R.id.playerHandContainer);
        handContainer.removeAllViews();
        for(int i = 0; i < player.getHandSize(); i++){
            Card card = player.getCard(i);
            ImageView cardView = createCardView(card, i);
            handContainer.addView(cardView);
        }
        TextView sum = findViewById(R.id.userSum);
        sum.setText("Ваша сумма: " + player.getSum());
    }
    private void updateOpenCardUI(){
        ImageView openCardView = findViewById(R.id.openCardView);
        if(openCard != null){
            openCardView.setImageResource(getCardImageRes(openCard));
            openCardView.setVisibility(View.VISIBLE);
        }
        else{
            openCardView.setVisibility(View.INVISIBLE);
        }
    }
    private void updateBotsUI(){
        LinearLayout botsContainer = findViewById(R.id.botsContainer);
        botsContainer.removeAllViews();
        for(Bot bot : bots){
            View botView = LayoutInflater.from(this).inflate(R.layout.bot_item, botsContainer, false);
            ImageView avatar = botView.findViewById(R.id.botAvatar);
            if(bot.getMyTurn() == true){
                botView.findViewById(R.id.botBound).setVisibility(View.VISIBLE);
            }
            else{
                botView.findViewById(R.id.botBound).setVisibility(View.GONE);
            }
            avatar.setImageResource(bot.getAvatarResId());
            TextView name = botView.findViewById(R.id.botName);
            name.setText(bot.getName());

            LinearLayout cardsContainer = botView.findViewById(R.id.botCardsContainer);
            cardsContainer.removeAllViews();
            int cardCount = bot.getHandSize();

            for(int i = 0; i < cardCount; i++){
                ImageView cardView = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        (int) getResources().getDimension(R.dimen.bot_card_width),
                        (int) getResources().getDimension(R.dimen.bot_card_height));
                if(i != 0) {
                    params.setMargins(-30, 0, 0, 0);
                }
                cardView.setLayoutParams(params);
                if(currentRound < 4) {
                    cardView.setImageResource(R.drawable.card_shirt);
                }
                else{
                    String resourceName = "card_";
                    if(bot.getHand().getCard(i).getRank() == 0){
                        resourceName += "null";
                    }
                    else {
                        resourceName += bot.getHand().getCard(i).getSuit().name().toLowerCase() + "_";
                        int rank = bot.getHand().getCard(i).getRank();
                        if (rank < 0) {
                            resourceName += "m" + rank * -1;
                        } else {
                            resourceName += rank;
                        }
                    }

                    cardView.setImageResource(getResources().getIdentifier(resourceName, "drawable", getPackageName()));
                }
                cardsContainer.addView(cardView);
            }
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            TextView sum = botView.findViewById(R.id.botSum);
            if(currentRound < 4){
                sum.setText(" ");
            }
            else{
                sum.setText("сумма: " + bot.getSum());
            }
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            botsContainer.addView(botView);
        }
    }
    private int getCardImageRes(Card card){
        if(card.getRank() == 0){
            return R.drawable.card_null;
        }
        String resourceName = "card_" + card.getSuit().name().toLowerCase() + "_";
        int rank = card.getRank();
        if(rank < 0){
            resourceName += "m" + rank * -1;
        }
        else{
            resourceName += rank;
        }

        return getResources().getIdentifier(resourceName, "drawable", getPackageName());
    }
    private ImageView createCardView(Card card, int index){
        ImageView cardView = new ImageView(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.card_width),
                (int) getResources().getDimension(R.dimen.card_height));
        if(index != 0) {
            params.setMargins(-60, 0, 0, 0);
        }
        cardView.setLayoutParams(params);
        cardView.setImageResource(getCardImageRes(card));
        cardView.setOnClickListener(v -> showActionDialog(index));
        return cardView;
    }

    private void showActionDialog(int cardIndex){
        selectedCardIndex = cardIndex;
        LinearLayout actionPanel = findViewById(R.id.actionPanel);
        actionPanel.setVisibility(View.VISIBLE);
        Button replaceWithOpenBtn = findViewById(R.id.replaceWithOpenBtn);
        replaceWithOpenBtn.setClickable(true);
        Button replaceFromDeckBtn = findViewById(R.id.replaceFromDeckBtn);
        replaceFromDeckBtn.setClickable(true);

        findViewById(R.id.text4btns).setVisibility(View.VISIBLE);
        replaceWithOpenBtn.setVisibility(View.VISIBLE);
        replaceFromDeckBtn.setVisibility(View.VISIBLE);
        findViewById(R.id.addOpenCardBtn).setVisibility(View.GONE);
        findViewById(R.id.addCardFromDeckBtn).setVisibility(View.GONE);
        findViewById(R.id.skipTurnBtn).setVisibility(View.GONE);

        replaceFromDeckBtn.setOnClickListener(v -> {
            player.replaceCardFromDeck(selectedCardIndex);
            updateHandUI();
            isWaitingForHumanAction = false;
            hideActionPanel();
        });

        replaceWithOpenBtn.setOnClickListener(v -> {
            if(openCard != null){
                player.replaceWithOpenCard(selectedCardIndex, openCard);
                changeOpenCard();
                updateHandUI();
                isWaitingForHumanAction = false;
                hideActionPanel();
            }
        });
    }
    private void showOpenCardOption(){
        LinearLayout actionPanel = findViewById(R.id.actionPanel);
        Button addOpenCardBtn = findViewById(R.id.addOpenCardBtn);
        findViewById(R.id.replaceWithOpenBtn).setVisibility(View.GONE);
        findViewById(R.id.replaceFromDeckBtn).setVisibility(View.GONE);
        findViewById(R.id.addCardFromDeckBtn).setVisibility(View.GONE);
        findViewById(R.id.skipTurnBtn).setVisibility(View.GONE);
        findViewById(R.id.text4btns).setVisibility(View.GONE);
        addOpenCardBtn.setVisibility(View.VISIBLE);
        addOpenCardBtn.setOnClickListener(v -> {
            player.addOpenCard(openCard);
            changeOpenCard();
            updateHandUI();
            isWaitingForHumanAction = false;
            hideActionPanel();
        });
        actionPanel.setVisibility(View.VISIBLE);
    }

    public void changeOpenCard(){
        openCard = null;
        updateOpenCardUI();
        openCard = deck.drawCard();
        updateOpenCardUI();
    }

    private void showDeckOption(){
        LinearLayout actionPanel = findViewById(R.id.actionPanel);
        Button addCardFromDeckBtn = findViewById(R.id.addCardFromDeckBtn);
        findViewById(R.id.replaceWithOpenBtn).setVisibility(View.GONE);
        findViewById(R.id.replaceFromDeckBtn).setVisibility(View.GONE);
        findViewById(R.id.addOpenCardBtn).setVisibility(View.GONE);
        findViewById(R.id.skipTurnBtn).setVisibility(View.GONE);
        findViewById(R.id.text4btns).setVisibility(View.GONE);
        addCardFromDeckBtn.setVisibility(View.VISIBLE);
        addCardFromDeckBtn.setOnClickListener(v -> {
            player.drawCardFromDeck();
            updateHandUI();
            isWaitingForHumanAction = false;
            hideActionPanel();
        });
        actionPanel.setVisibility(View.VISIBLE);
    }

    private void hideActionPanel(){
        LinearLayout actionPanel = findViewById(R.id.actionPanel);
        actionPanel.setVisibility(View.GONE);
        findViewById(R.id.text4btns).setVisibility(View.GONE);
        selectedCardIndex = -1;
        isOpenCardSelected = false;
        isDeckSelected = false;
        isWaitingForHumanAction = false;
        GameManager();
    }

    private void GameManager(){
        LinearLayout actionPanel = findViewById(R.id.actionPanel);

        ImageView dice1 = findViewById(R.id.dice1);
        ImageView dice2 = findViewById(R.id.dice2);

        if(isWaitingForHumanAction == false){
            findViewById(R.id.block).setVisibility(View.VISIBLE);
            OneTimeWorkRequest task01 = createWorkRequest("task01", 1);
            OneTimeWorkRequest task1 = createWorkRequest("task1", 2000);
            OneTimeWorkRequest task02 = createWorkRequest("task02", 1);
            OneTimeWorkRequest task2 = createWorkRequest("task2", 2000);
            OneTimeWorkRequest task03 = createWorkRequest("task03", 1);
            OneTimeWorkRequest task3 = createWorkRequest("task3", 2000);
            OneTimeWorkRequest task4 = createWorkRequest("task4", 5601);
            OneTimeWorkRequest task44 = createWorkRequest("task44", 1);
            OneTimeWorkRequest task5 = createWorkRequest("task5", 1);
            int value1 = random.nextInt(6) + 1;
            int value2 = random.nextInt(6) + 1;
            WorkManager.getInstance(this).beginWith(task01).then(task1)
                    .then(task02).then(task2).then(task03).then(task3)
                    .then(task4).then(task44).then(task5).enqueue();
            observeWork(task01, 0);
            observeWork(task1, bots.get(0));
            observeWork(task02, 1);
            observeWork(task2, bots.get(1));
            observeWork(task03, 2);
            observeWork(task3, bots.get(2));
            observeWork(task4, dice1, dice2, value1, value2);
            observeWork(task44, value1, value2);
            observeWork(task5);
        }
    }

    private OneTimeWorkRequest createWorkRequest(String taskName, long duration){
        Data inputData = new Data.Builder().putString(GenericWorker.KEY_TASK_NAME, taskName)
                .putLong(GenericWorker.KEY_SLEEP_DURATION, duration).build();
        return new OneTimeWorkRequest.Builder(GenericWorker.class).setInputData(inputData).build();
    }

    private void observeWork(OneTimeWorkRequest workRequest, int value){
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null) {
                        WorkInfo.State state = workInfo.getState();
                        if (state == WorkInfo.State.SUCCEEDED) {
                            bots.get(value).setMyTurn(true);
                            updateBotsUI();
                        }
                    }
                });
    }
    private void observeWork(OneTimeWorkRequest workRequest, Bot bot){
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest.getId())
                .observe(this, workInfo -> {
                    if(workInfo != null){
                        WorkInfo.State state = workInfo.getState();
                        if(state == WorkInfo.State.SUCCEEDED){
                            bot.setOpenCard(openCard);
                            bot.setMyTurn(true);
                            if(bot.botMove()){
                                runOnUiThread(() -> changeOpenCard());
                            }
                            bot.setMyTurn(false);
                            runOnUiThread(() -> updateBotsUI());
                        }
                    }
                });
    }
    private void observeWork(OneTimeWorkRequest workRequest, ImageView dice1, ImageView dice2, int value1, int value2){
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest.getId())
                .observe(this, workInfo -> {
                    if(workInfo != null){
                        WorkInfo.State state = workInfo.getState();
                        if(state == WorkInfo.State.RUNNING){
                            String animationName1 = "cube_" + value1 + "_animation";
                            String animationName2 = "cube_" + value2 + "_animation";

                            dice1.setImageResource(getResources().getIdentifier(animationName1, "drawable", getPackageName()));
                            AnimationDrawable animationDrawable1 = (AnimationDrawable) dice1.getDrawable();
                            runOnUiThread(() -> animationDrawable1.start());
                            dice2.setImageResource(getResources().getIdentifier(animationName2, "drawable", getPackageName()));
                            AnimationDrawable animationDrawable2 = (AnimationDrawable) dice2.getDrawable();
                            runOnUiThread(() -> animationDrawable2.start());
                        }
                    }
                });
    }

    private void observeWork(OneTimeWorkRequest workRequest, int value1, int value2){
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null) {
                        WorkInfo.State state = workInfo.getState();
                        if (state == WorkInfo.State.SUCCEEDED) {
                            if(value1 == value2){
                                player.clearHand();
                                updateHandUI();
                                player.drawCardFromDeck();
                                player.drawCardFromDeck();
                                updateHandUI();
                                for(Bot bot : bots){
                                    bot.getHand().clearHand();
                                    bot.getHand().drawCardFromDeck();
                                    bot.getHand().drawCardFromDeck();
                                    updateBotsUI();
                                }
                            }
                        }
                    }
                });
    }

    private void observeWork(OneTimeWorkRequest workRequest){
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null) {
                        WorkInfo.State state = workInfo.getState();
                        if (state == WorkInfo.State.SUCCEEDED) {
                            currentRound += 1;
                            if(currentRound < 4) {
                                isWaitingForHumanAction = true;
                                findViewById(R.id.block).setVisibility(View.GONE);
                                LinearLayout actionPanel = findViewById(R.id.actionPanel);
                                actionPanel.setVisibility(View.VISIBLE);
                                findViewById(R.id.replaceWithOpenBtn).setVisibility(View.GONE);
                                findViewById(R.id.replaceFromDeckBtn).setVisibility(View.GONE);
                                findViewById(R.id.addCardFromDeckBtn).setVisibility(View.GONE);
                                findViewById(R.id.addOpenCardBtn).setVisibility(View.GONE);
                                findViewById(R.id.skipTurnBtn).setVisibility(View.VISIBLE);
                            }
                            else{
                                int id = -1;
                                int best_sum = player.getSum();
                                for(int i = 0; i < bots.size(); i++){
                                    if(Math.abs(best_sum) > Math.abs(bots.get(i).getSum())){
                                        id = i;
                                        best_sum = bots.get(i).getSum();
                                    }
                                }
                                updateBotsUI();
                                TextView not = findViewById(R.id.NOTIFICATION);
                                not.setVisibility(View.VISIBLE);
                                findViewById(R.id.endGameBtns).setVisibility(View.VISIBLE);
                                if(id == -1) {
                                    not.setText("ВЫ ВЫИГРАЛИ!!!");
                                }
                                else{
                                    not.setText(bots.get(id).getName() + " выиграл!");
                                }
                            }
                        }
                    }
                });
    }

    private void startNewGame(){
        currentRound = 1;
        isWaitingForHumanAction = true;
        selectedCardIndex = -1;
        isOpenCardSelected = false;
        isDeckSelected = false;
        currentBotIndex = 0;
        deck.reset();
        openCard = null;
        openCard = deck.drawCard();
        updateOpenCardUI();
        player.clearHand();
        player.drawCardFromDeck();
        player.drawCardFromDeck();
        updateHandUI();
        for (Bot bot: bots){
            bot.setHand(new Player(deck));
            bot.getHand().drawCardFromDeck();
            bot.getHand().drawCardFromDeck();
        }
        updateBotsUI();
        findViewById(R.id.block).setVisibility(View.GONE);
        LinearLayout actionPanel = findViewById(R.id.actionPanel);
        actionPanel.setVisibility(View.VISIBLE);
        findViewById(R.id.NOTIFICATION).setVisibility(View.GONE);
        findViewById(R.id.endGameBtns).setVisibility(View.GONE);
        findViewById(R.id.replaceWithOpenBtn).setVisibility(View.GONE);
        findViewById(R.id.replaceFromDeckBtn).setVisibility(View.GONE);
        findViewById(R.id.addCardFromDeckBtn).setVisibility(View.GONE);
        findViewById(R.id.addOpenCardBtn).setVisibility(View.GONE);
        findViewById(R.id.skipTurnBtn).setVisibility(View.VISIBLE);
    }

    private void toStartScreen(){
        startActivity(new Intent(this, StartActivity.class));
        finish();
    }
}