package BattleTowers.monsters;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.AnimateShakeAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.unique.ApplyStasisAction;
import com.megacrit.cardcrawl.actions.unique.CannotLoseAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.status.Dazed;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.StasisPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.BorderFlashEffect;
import com.megacrit.cardcrawl.vfx.combat.SmallLaserEffect;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class InvisibleIntentDisplayer extends AbstractBTMonster {
    public static final String ID = makeID(ExampleMonster.class.getSimpleName());

    //name of the monster's moves
    private static final byte STASIS = 0;
    private static final byte DAMAGE = 1;
    private static final byte BLOCK = 2;
    private static final byte STRENGTH = 3;

    Intent lastKnownType;

    public InvisibleIntentDisplayer(final float x, final float y) {
        // maxHealth param doesn't matter, we will override it with setHP
        // hb_x and hb_y shifts the monster's AND its health bar's position around on the screen, usually you don't need to change these values
        // hb_w affects how wide the monster's health bar is. hb_h affects how far up the monster's intent image is. Adjust these values until they look good
        super("", ID, 140, 0.0F, 0.0f, 200.0f, 220.0f, null, x, y);
        // HANDLE YOUR ANIMATION STUFF HERE
        // this.animation = Whatever your animation is

        // calcAscensionTankiness automatically scales HP based on ascension and enemy type
        // passing 2 values makes the game randomly select a value in between the ranges for the HP
        // if you pass only 1 value to set HP it will use that as the HP value
        setHp(calcAscensionTankiness(36), calcAscensionTankiness(42));


        addMove(STASIS, Intent.STRONG_DEBUFF);
        addMove(BLOCK, Intent.DEFEND, calcAscensionDamage(8));
        addMove(DAMAGE, Intent.ATTACK, calcAscensionDamage(8));
        addMove(STRENGTH, Intent.BUFF);
    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        // we set the enemy type here so the calcAscensionMethods are called after the enemy type is set
        this.type = EnemyType.NORMAL;
    }

    public boolean shouldRenderIntent = false;


    @Override
    public void takeTurn() {
    }

    @Override
    protected void getMove(final int num) {
    }

    public boolean returnLastTwoMoves(byte move){
       return lastTwoMoves(move);
    }

    public void recordMove(byte move){
        this.moveHistory.add(move);
        this.nextMove=move;
    }


    @Override
    public void render(SpriteBatch sb) {
        //if (shouldRenderIntent) {
        this.renderIntentVfxBehind(sb);
        this.renderIntent(sb);
        this.renderIntentVfxAfter(sb);
        this.renderDamageRange(sb);
        //}
    }

    public void setIntent(Intent type, int number) {
        shouldRenderIntent = true;
        if (number > -1) {
            this.setMove((byte) 0, type, number);
        } else {
            this.setMove((byte) 0, type);
        }
        lastKnownType = type;
        refreshIntentHbLocation();
        createIntent();
    }

    public void updateIntent(int number){

        shouldRenderIntent = true;
        if (number > -1) {
            this.setMove((byte) 0, lastKnownType, number);
        } else {
            this.setMove((byte) 0, lastKnownType);
        }
        refreshIntentHbLocation();
        createIntent();
    }

    public void hideIntent() {
        flashIntent();

    }
}