package BattleTowers.towers;

import com.megacrit.cardcrawl.random.Random;

public class BattleTower {
    private TowerLayout layout;

    protected String title;

    public BattleTower(Random rng) {
        this.title = String.valueOf(rng.random(0, 1000));
    }

    public String getTitle() {
        return title;
    }

    private static class TowerLayout {

    }
}
