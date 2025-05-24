package com.example.sabacc;

import android.animation.AnimatorSet;
import android.content.Context;

import java.util.Random;

public class Bot {
    private String name;
    private int avatarResId;
    private Player hand;
    private Card openCard;
    private boolean isMyTurn = false;

    public Bot(Context context, Deck deck){
        this.hand = new Player(deck);
        String[] names = context.getResources().getStringArray(R.array.bot_names);
        this.name = names[new Random().nextInt(names.length)];
        Random random = new Random();
        String resourceName = "avatar_" + (random.nextInt(10) + 1);
        this.avatarResId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
        hand.drawCardFromDeck();
        hand.drawCardFromDeck();
    }

    public String getName(){ return name; }
    public int getAvatarResId(){ return avatarResId; }
    public int getSum(){ return hand.getSum(); }
    public Player getHand(){ return hand; }
    public void setHand(Player hand){ this.hand = hand; }
    public int getHandSize(){ return hand.getHandSize(); }
    public void setOpenCard(Card openCard){ this.openCard = openCard; }

    public boolean getMyTurn(){ return isMyTurn; }
    public void setMyTurn(boolean turn){ this.isMyTurn = turn; }

    public boolean botMove(){
        int current_sum = getSum();
        if(current_sum == 0){
            return false;
        }
        int card_index = -1;
        int new_sum = current_sum + openCard.getRank();
        for(int i = 0; i < getHandSize(); i++){
            if(Math.abs(new_sum) > Math.abs(current_sum - hand.getCard(i).getRank() + openCard.getRank())){
                card_index = i;
                new_sum = current_sum - hand.getCard(i).getRank() + openCard.getRank();
            }
        }
        if(Math.abs(current_sum) > Math.abs(new_sum)) {
            if (card_index == -1) {
                hand.addOpenCard(openCard);
                return true;
            }
            else{
                hand.replaceWithOpenCard(card_index, openCard);
                return true;
            }
        }
        hand.drawCardFromDeck();
        return false;
    }
}
