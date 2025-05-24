package com.example.sabacc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Deck {
    private List<Card> cards;
    private int nullCount;
    public Deck(){
        cards = new ArrayList<>();
        nullCount = 2;
        initializeDeck();
    }
    private void initializeDeck(){
        for(Suit suit : Suit.values()){
            for(int rank = -10; rank <= 10; rank++){
                if(rank != 0){
                    cards.add(new Card(suit, rank));
                }
            }
        }
        for(int i = 0; i < nullCount; i++){
            cards.add(new Card());
        }
    }
    public void shuffle(){
        Collections.shuffle(cards);
    }
    public Card drawCard(){
        if(cards.isEmpty()){
            return null;
        }
        return cards.remove(0);
    }
    public void reset(){
        cards.clear();
        initializeDeck();
        shuffle();
    }
}
