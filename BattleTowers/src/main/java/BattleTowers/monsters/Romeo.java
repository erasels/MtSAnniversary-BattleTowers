package BattleTowers.monsters;

import BattleTowers.util.UC;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.animations.TalkAction;
import com.megacrit.cardcrawl.actions.common.ChangeStateAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.EnemyMoveInfo;
import com.megacrit.cardcrawl.powers.StrengthPower;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeMonsterPath;

public class Romeo extends AbstractBTMonster {
    public static final String ID = makeID("Romeo");
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;

    private static final int MHP_S = 55, MHP_X = 65;


    //name of the monster's moves
    private static final byte BUFF_SELF = 0; //buff strength
    private static final byte SIDE_SLASH = 2; // Gain small block and deal damage
    private static final byte STAB = 3; // Deal damage x2 + 1 for every rep
    private static final byte GRIEVOUS_WOUNDS = 1; //Whenever the player takes unblocked attack damage, they lose Max HP now


    private static final int SMALL_DMG = 7;
    private static final int MED_DMG = 12;
    private static final int BLK = 8;

    //TODO: Make transparent and give particle effect if kill bandits was chosen in Masked Bandits event
    public Romeo(final float x, final float y) {
        super(NAME, ID, 50, -10.0F, -7.0F, 180.0F, 285.0F, null, x, y);

        loadAnimation(makeMonsterPath("Romeo/skeleton.atlas"), makeMonsterPath("Romeo/skeleton.json"), 1.0f);
        AnimationState.TrackEntry e = this.state.setAnimation(0, "Idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        this.stateData.setMix("Hit", "Idle", 0.2F);
        this.state.setTimeScale(0.8F);

        this.dialogX = 0.0F * Settings.scale;
        this.dialogY = 50.0F * Settings.scale;

        setHp(calcAscensionTankiness(AbstractDungeon.monsterRng.random(MHP_S, MHP_X)));

        addMove(BUFF_SELF, Intent.BUFF);
        addMove(SIDE_SLASH, Intent.ATTACK_DEFEND, MED_DMG);
        addMove(STAB, Intent.ATTACK, SMALL_DMG, AbstractDungeon.ascensionLevel>=17 ? 3 : 2, true);
        addMove(GRIEVOUS_WOUNDS, Intent.STRONG_DEBUFF);
    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        // we set the enemy type here so the calcAscensionMethods are called after the enemy type is set
        this.type = EnemyType.NORMAL;
    }

    @Override
    public void usePreBattleAction() {
        //TODO: Add power that makes monster attack whenever it's attacked for less than the threshold
    }

    @Override
    public void takeTurn() {
        //Automatically grabs the damage values and number of hits value from the moves hashmap based on the currently set move
        DamageInfo info = new DamageInfo(this, this.moves.get(nextMove).baseDamage, DamageInfo.DamageType.NORMAL);

        if (info.base > -1) {
            info.applyPowers(this, AbstractDungeon.player);
        }

        switch (nextMove) {
            case GRIEVOUS_WOUNDS:
                UC.atb(new TalkAction(this, DIALOG[0]));
                //TODO: Add debuff that makes player lose amount max HP whenever they take unblocked attack damage
                break;
            case SIDE_SLASH:
                UC.atb(new ChangeStateAction(this, "SLASH"));
                UC.atb(new WaitAction(0.5f));
                UC.atb(new DamageAction(UC.p(), info, AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
                UC.atb(new GainBlockAction(this, BLK));
                break;
            case STAB:
                UC.atb(new ChangeStateAction(this, "STAB"));
                UC.atb(new WaitAction(0.5f));
                for (int i = 0; i < moves.get(STAB).multiplier; i++) {
                    UC.atb(new DamageAction(UC.p(), info, MathUtils.randomBoolean()? AbstractGameAction.AttackEffect.SLASH_DIAGONAL : AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
                }
                break;
            case BUFF_SELF:
                UC.atb(new TalkAction(this, DIALOG[1]));
                UC.doPow(this, this, new StrengthPower(this, calcAscensionSpecial(2)), false);
        }

        addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(int i) {
        byte move = (byte) (GameActionManager.turn % 4);
        EnemyMoveInfo info = this.moves.get(move);
        this.setMove(MOVES[move], move, info.intent, info.baseDamage, info.multiplier>0? GameActionManager.turn/3+info.multiplier : 0, info.isMultiDamage);
    }

    public void changeState(String key) {
        if ("SLASH".equals(key)) {
            state.setAnimation(0, "Attack", false);
            state.addAnimation(0, "Idle", true, 0.0F);
        } else if ("STAB".equals(key)) {
            state.setAnimation(0, "Attack", false);
            //TODO: ADjust for stuff
            for (int i = 0; i < GameActionManager.turn/3 + 1; i++) {
                state.addAnimation(0, "Attack", false, 0);
            }
            state.addAnimation(0, "Idle", true, 0.0F);
        }
    }

    public void damage(DamageInfo info) {
        super.damage(info);
        if (info.owner != null && info.type != DamageInfo.DamageType.THORNS && info.output > 0) {
            state.setAnimation(0, "Hit", false);
            state.setTimeScale(0.8F);
            state.addAnimation(0, "Idle", true, 0.0F);
        }
    }
}