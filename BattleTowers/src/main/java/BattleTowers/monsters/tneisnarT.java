package BattleTowers.monsters;

import BattleTowers.powers.rewoPgnitfihS;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.vfx.combat.ExplosionSmallEffect;
import com.megacrit.cardcrawl.vfx.combat.InflameEffect;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeMonsterPath;

public class tneisnarT extends AbstractBTMonster
{
    public static final String ID = makeID("tneisnarT");
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;

    //name of the monster's moves
    private static final byte STRIKE = 0; //he smacc

    private static final int OUCHIE = 10; //he ouch

    //default positioning
    public tneisnarT() {
        this(00.0f, 0.0f);
    }

    public tneisnarT(final float x, final float y) {
        super(NAME, ID, 100, 0.0F, -15.0f, 370.0f, 340.0f, null, x, y);

        loadAnimation(makeMonsterPath("tneisnarT/skeleton.atlas"), makeMonsterPath("tneisnarT/skeleton.json"), 1.0f);
        AnimationState.TrackEntry e = this.state.setAnimation(0, "Idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());

        this.flipHorizontal = true;
        //this.flipVertical = true; //?

        this.dialogX = -100.0F * Settings.scale;
        this.dialogY -= 20.0F * Settings.scale;

        setHp(AbstractDungeon.ascensionLevel >= 7 ? 55 : 50);

        addMove(STRIKE, Intent.ATTACK_BUFF, 0);
    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        // we set the enemy type here so the calcAscensionMethods are called after the enemy type is set
        this.type = EnemyType.NORMAL;
    }

    @Override
    public void usePreBattleAction() {
        //Swap this
        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(this, this, new rewoPgnitfihS(this)));
    }

    @Override
    public void takeTurn() {
        //Automatically grabs the damage values and number of hits value from the moves hashmap based on the currently set move
        DamageInfo info = new DamageInfo(this, this.moves.get(nextMove).baseDamage, DamageInfo.DamageType.NORMAL);

        if(info.base > -1) {
            info.applyPowers(this, AbstractDungeon.player);
        }

        if (this.nextMove == STRIKE) {
            AbstractDungeon.actionManager.addToBottom(new ChangeStateAction(this, "ATTACK"));
            AbstractDungeon.actionManager.addToBottom(new WaitAction(0.3F));
            addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_HEAVY));
            this.addToBot(new VFXAction(new InflameEffect(this), 0.1F));
            addToBot(new LoseHPAction(this, this, OUCHIE, AbstractGameAction.AttackEffect.NONE));
        }

        addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(final int num) {
        setMoveShortcut(STRIKE, null);
    }

    public void die() {
        super.die();
        AbstractDungeon.effectsQueue.add(new ExplosionSmallEffect(this.hb.cX, this.hb.cY));
    }

    @Override
    public void render(SpriteBatch sb) {
        super.render(sb);
    }

    @Override
    public void update() {
        super.update();
    }
}