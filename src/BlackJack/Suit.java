package BlackJack;

public enum Suit {
    HEARTS("Hearts", "\u2665", true),
    DIAMONDS("Diamonds", "\u2666", true),
    CLUBS("Clubs", "\u2663", false),
    SPADES("Spades", "\u2660", false);

    private final String displayName;
    private final String symbol;
    private final boolean red;

    Suit(String displayName, String symbol, boolean red) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.red = red;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean isRed() {
        return red;
    }
}
