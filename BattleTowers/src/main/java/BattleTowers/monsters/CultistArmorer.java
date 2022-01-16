package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import BattleTowers.cards.Chilled;
import BattleTowers.util.UC;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.AnimateShakeAction;
import com.megacrit.cardcrawl.actions.animations.AnimateSlowAttackAction;
import com.megacrit.cardcrawl.actions.animations.TalkAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.unique.CannotLoseAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.TextAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.*;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class CultistArmorer extends AbstractBTMonster {
    public static final String ID = makeID(CultistArmorer.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;

    public CultistArmorer() {
        this(0.0f, 0.0f);
    }

    public CultistArmorer(final float x, final float y) {


        super(NAME, ID, 140, -8.0F, 10.0F, 270.0F, 300.0F, null, x, y);


        loadAnimation(BattleTowers.makeMonsterPath("Armorer/skeleton.atlas"), BattleTowers.makeMonsterPath("Armorer/skeleton.json"), .8F);

        AnimationState.TrackEntry e = this.state.setAnimation(0, "waving", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        e.setTimeScale(0.7F);


        setHp(calcAscensionTankiness(73), calcAscensionTankiness(79));

        this.damage.add(new DamageInfo(this, 6));
    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();

        this.type = EnemyType.NORMAL;
    }


    public void takeTurn() {
        switch (this.nextMove) {
            case 3:
                if (AbstractDungeon.ascensionLevel >= 2) {
                    UC.doPow(new RitualPower(this, 4, false));
                    for (AbstractMonster m:AbstractDungeon.getCurrRoom().monsters.monsters) {
                        UC.doPow(new PlatedArmorPower(this, 4));
                        UC.doPow(new MetallicizePower(this, 4));
                    }
                } else {
                    UC.doPow(new RitualPower(this, 3, false));
                    for (AbstractMonster m:AbstractDungeon.getCurrRoom().monsters.monsters) {
                    UC.doPow(new PlatedArmorPower(this, 3));
                    UC.doPow(new MetallicizePower(this, 3));
                    }
                }
                break;
            case 1:
                AbstractDungeon.actionManager.addToBottom(new AnimateSlowAttackAction(this));
                AbstractDungeon.actionManager.addToBottom(new DamageAction(AbstractDungeon.player,
                        this.damage.get(0), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
                break;
        }


        AbstractDungeon.actionManager.addToBottom(new RollMoveAction(this));
    }

    private void playDeathSfx() {
        int roll = MathUtils.random(2);
        if (roll == 0) {
            CardCrawlGame.sound.playA("VO_CULTIST_2A", -0.3F);
        } else if (roll == 1) {
            CardCrawlGame.sound.playA("VO_CULTIST_2B", -0.3F);
        } else {
            CardCrawlGame.sound.playA("VO_CULTIST_2C", -0.3F);
        }
    }

    @Override
    public void die() {
        playDeathSfx();
        this.state.setTimeScale(0.1F);
        useShakeAnimation(5.0F);
        super.die();
    }

    protected void getMove(int num) {
        if (this.firstMove) {
            this.firstMove = false;
            setMove(CardCrawlGame.languagePack.getMonsterStrings("Cultist").MOVES[2], (byte) 3, AbstractMonster.Intent.BUFF);
            return;
        }
        setMove((byte) 1, AbstractMonster.Intent.ATTACK, (this.damage.get(0)).base);
    }
}



