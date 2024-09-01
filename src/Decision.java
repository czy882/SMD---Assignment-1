public enum Decision {
    LEFT(0), RIGHT(1), ADJACENT(1), TRUE(1), FALSE(0);

    private final int decision;

    Decision(int decision) {
        this.decision = decision;
    }

    public int getDecision() {
        return decision;
    }
}
