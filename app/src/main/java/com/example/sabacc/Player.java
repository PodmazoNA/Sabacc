package com.example.sabacc;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private List<Card> hand;
    private Deck deck;
    public Player(Deck deck){
        this.hand = new ArrayList<>();
        this.deck = deck;
    }

    public void drawCardFromDeck(){
        Card card = deck.drawCard();
        if(card != null){
            hand.add(card);
        }
    }
    public void replaceCardFromDeck(int handIndex){
        if(handIndex >= 0 && handIndex < hand.size()){
            Card newCard = deck.drawCard();
            if(newCard != null){
                hand.set(handIndex, newCard);
            }
        }
    }
    public void addOpenCard(Card openCard){
        if(openCard != null){
            hand.add(openCard);
        }
    }
    public void replaceWithOpenCard(int handIndex, Card openCard){
        if(handIndex >= 0 && handIndex < hand.size() && openCard != null){
            hand.set(handIndex, openCard);
        }
    }
    public int getSum(){
        int sum = 0;
        for(Card card : hand){
            sum += card.getRank();
        }
        return sum;
    }
    public Card getCard(int index){
        if(index >= 0 && index < hand.size()){
            return hand.get(index);
        }
        return null;
    }
    public int getHandSize(){
        return hand.size();
    }

    public void clearHand(){
        hand.clear();
    }
}
