package BattleTowers.monsters;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.status.Slimed;

import static BattleTowers.BattleTowers.makeID;

public class SentryGreen extends AbstractElementalSentry {
    public static final String ID = makeID(SentryGreen.class.getSimpleName());
    private static final Color COLOR = Color.GREEN.cpy();
    private static final AbstractCard STATUS = new Slimed();


    public SentryGreen(final float x, final float y) {
        super(ID, COLOR, STATUS, x, y);
        this.loadAnimation("battleTowersResources/img/monsters/ElementalSentries/Green/skeleton.atlas", "battleTowersResources/img/monsters/ElementalSentries/Green/skeleton.json", SCALE);
        AnimationState.TrackEntry e = this.state.setAnimation(0, "idle", true);
        e.setTimeScale(2.0F);
        e.setTime(e.getEndTime() * MathUtils.random());
        stateData.setMix("idle", "attack", 0.1F);
        stateData.setMix("idle", "spaz1", 0.1F);
        stateData.setMix("idle", "hit", 0.1F);
    }
}