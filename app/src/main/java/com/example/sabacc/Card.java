package com.example.sabacc;

public class Card {
    private Suit suit;
    private int rank;

    public Card(Suit suit, int rank){
        this.suit = suit;
        this.rank = rank;
    }
    public Card(){
        this.suit = null;
        this.rank = 0;
    }
    public Suit getSuit(){ return suit; }
    public int getRank(){ return rank; }
}
