package BattleTowers.monsters.executiveslime;

import BattleTowers.BattleTowers;
import BattleTowers.monsters.AbstractBTMonster;
import BattleTowers.powers.ExpendablePower;
import BattleTowers.powers.OnusPower;
import BattleTowers.vfx.CustomWeightyImpactEffect;
import basemod.animations.SpriterAnimation;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.*;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.status.Slimed;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.powers.WeakPower;
import com.megacrit.cardcrawl.vfx.combat.ShockWaveEffect;

import java.util.ArrayList;
import java.util.List;

import static BattleTowers.BattleTowers.makeID;

public class ExecutiveSlime extends AbstractBTMonster
{
    public static final String ID = makeID("ExecutiveSlime");
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;

    //name of the monster's moves
    private static final byte ONUS = 0; //Onus+Weak+Slimed

    private static final byte OUTRAGE = 1; //Attack + Strength
    private static final byte WEAK_ONUS = 2; //Attack+Onus+1 Slimed
    private static final byte SPLASH = 4; //Slimed
    private static final byte SWIPE = 3; //Attack+Vuln

    private static final byte SWARM = 5; //Summon 2 Slimelings
    private static final byte PREPARE = 6; //All the Slimelings are still alive?????

    private static final byte PUNISHMENT = 7; //Strong Hit

    private static final int INITIAL_SLIMED = 2;
    private final int STRENGTH_GROW = AbstractDungeon.ascensionLevel >= 19 ? 3 : 2;
    private static final int DEBUFF = 2; //Turn 0 Weak, Swipe Vuln
    private static final int SPLASH_SLIME = 2;
    private final int MINION_COUNT = 2; //Three is a No NO. No. Bad. AbstractDungeon.ascensionLevel >= 19 ? 3 : 2;
    private final int WEAK_ONUS_SLIME = AbstractDungeon.ascensionLevel >= 19 ? 2 : 1;

    private static final float[] POS_X = new float[] { -431.0F, -255.0F, -607.0F };
    private static final float[] POS_Y = new float[] { -2.0F, 4.0F, 0.0F };

    //Stuff
    private boolean firstTurn = true;
    private int numTurns = 0;

    //default positioning
    public ExecutiveSlime() {
        this(40.0f, 0.0f);
    }

    public ExecutiveSlime(final float x, final float y) {
        super(NAME, ID, 140, 0.0F, 0.0f, 400.0f, 420.0f, null, x, y);

        this.animation = new SpriterAnimation(BattleTowers.makeMonsterPath("ExecutiveSlime/ExecutiveSlime.scml"));

        setHp(calcAscensionTankiness(180));

        addMove(ONUS, Intent.STRONG_DEBUFF);
        addMove(OUTRAGE, Intent.ATTACK_BUFF, AbstractDungeon.ascensionLevel >= 4 ? 16 : 13);
        addMove(WEAK_ONUS, Intent.ATTACK_DEBUFF, calcAscensionDamage(12));
        addMove(SWIPE, Intent.ATTACK_DEBUFF, calcAscensionDamage(10));
        addMove(SPLASH, Intent.DEBUFF); //, calcAscensionDamage(12));
        addMove(SWARM, Intent.UNKNOWN);
        addMove(PREPARE, Intent.UNKNOWN);
        addMove(PUNISHMENT, Intent.ATTACK, calcAscensionDamage(18)); //+Vuln and Slimelings
    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        // we set the enemy type here so the calcAscensionMethods are called after the enemy type is set
        this.type = EnemyType.BOSS;
    }

    @Override
    public void takeTurn() {
        //Automatically grabs the damage values and number of hits value from the moves hashmap based on the currently set move
        DamageInfo info = new DamageInfo(this, this.moves.get(nextMove).baseDamage, DamageInfo.DamageType.NORMAL);

        if(info.base > -1) {
            info.applyPowers(this, AbstractDungeon.player);
        }

        //carries out actions based on the current move
        //useFastAttackAnimation causes the monster to jump forward when it attacks
        switch (this.nextMove) {
            case ONUS:
                addToBot(new SFXAction("VO_AWAKENEDONE_1", -0.4f, true));
                addToBot(new VFXAction(new ShockWaveEffect(hb.cX, hb.cY, Color.PURPLE, ShockWaveEffect.ShockWaveType.ADDITIVE)));
                addToBot(new VFXAction(new ShockWaveEffect(hb.cX, hb.cY, Color.PINK, ShockWaveEffect.ShockWaveType.CHAOTIC)));

                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new WeakPower(AbstractDungeon.player, DEBUFF, true), DEBUFF));
                if (!AbstractDungeon.player.hasPower(OnusPower.POWER_ID))
                    addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new OnusPower(AbstractDungeon.player, this), 0));
                addToBot(new MakeTempCardInDrawPileAction(new Slimed(), INITIAL_SLIMED, true, true));
                break;
            case OUTRAGE:
                AbstractDungeon.actionManager.addToBottom(new AnimateSlowAttackAction(this));
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(this, this, new StrengthPower(this, STRENGTH_GROW), STRENGTH_GROW));
                break;
            case WEAK_ONUS:
                AbstractDungeon.actionManager.addToBottom(new AnimateSlowAttackAction(this));
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_LIGHT));
                if (!AbstractDungeon.player.hasPower(OnusPower.POWER_ID))
                    addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new OnusPower(AbstractDungeon.player, this), 0));
                addToBot(new MakeTempCardInDrawPileAction(new Slimed(), WEAK_ONUS_SLIME, true, true));
                break;
            case SWIPE:
                AbstractDungeon.actionManager.addToBottom(new AnimateFastAttackAction(this));
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new VulnerablePower(AbstractDungeon.player, DEBUFF, true), DEBUFF));
                break;
            case SPLASH:
                AbstractDungeon.actionManager.addToBottom(new AnimateFastAttackAction(this));
                //addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.POISON));
                addToBot(new MakeTempCardInDrawPileAction(new Slimed(), SPLASH_SLIME, true, true));
                break;
            case SWARM:
                AbstractDungeon.actionManager.addToBottom(new AnimateShakeAction(this, 1.0f, 0.1f));
                AbstractDungeon.actionManager.addToBottom(new SFXAction("SLIME_SPLIT"));
                spawnSlimes();
                break;
            case PREPARE:
                if (MathUtils.randomBoolean()) {
                    AbstractDungeon.actionManager.addToBottom(new SFXAction("VO_SLIMEBOSS_1A", 0.3f, true));
                } else {
                    AbstractDungeon.actionManager.addToBottom(new SFXAction("VO_SLIMEBOSS_1B", 0.3f, true));
                }
                break;
            case PUNISHMENT:
                AbstractDungeon.actionManager.addToBottom(new AnimateJumpAction(this));
                AbstractDungeon.actionManager.addToBottom(new VFXAction(new CustomWeightyImpactEffect(AbstractDungeon.player.hb.cX, AbstractDungeon.player.hb.cY, new Color(0.7F, 0.55F, 0.8F, 0.0F), Color.WHITE.cpy())));
                AbstractDungeon.actionManager.addToBottom(new WaitAction(0.4F)); //A bit longer on fast mode than just 1 action
                AbstractDungeon.actionManager.addToBottom(new WaitAction(0.4F));
                AbstractDungeon.actionManager.addToBottom(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.POISON));
                break;
        }

        addToBot(new RollMoveAction(this));
    }

    private void spawnSlimes() {
        List<Integer> shouldSpawn = new ArrayList<>();
        for (int i = 0; i < MINION_COUNT; ++i)
            shouldSpawn.add(i);

        for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
            if (!m.isDeadOrEscaped() && m instanceof ExecutiveMinion) {
                shouldSpawn.remove(((ExecutiveMinion) m).getMinionIndex());
            }
        }

        for (int i : shouldSpawn) {
            Slimeling newMinion = new Slimeling(POS_X[i], POS_Y[i]);
            newMinion.setMinionIndex(i);
            AbstractDungeon.actionManager.addToBottom(new SpawnMonsterAction(newMinion, true));
            AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(newMinion, this, new ExpendablePower(newMinion, this, 10, true), 10));
        }
    }

    private int countMinions() {
        int amt = 0;
        for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
            if (!m.isDeadOrEscaped() && m instanceof ExecutiveMinion) {
                ++amt;
            }
        }
        return amt;
    }

    @Override
    protected void getMove(final int num) {
        if (firstTurn) {
            setMoveShortcut(ONUS, MOVES[ONUS]);
            firstTurn = false;
        }
        else {
            switch (numTurns % 4) {
                default:
                    if (!AbstractDungeon.player.hasPower(OnusPower.POWER_ID)) {
                        setMoveShortcut(WEAK_ONUS, MOVES[WEAK_ONUS]);
                    }
                    else {
                        if (lastMove(PUNISHMENT)) {
                            setMoveShortcut(SPLASH, MOVES[SPLASH]);
                        }
                        else {
                            setMoveShortcut(OUTRAGE, MOVES[OUTRAGE]);
                        }
                    }
                    break;
                case 1:
                    if (numTurns % 8 < 4) { //First 4
                        setMoveShortcut(SWIPE, MOVES[SWIPE]);
                    }
                    else { //Second 4
                        setMoveShortcut(OUTRAGE, MOVES[OUTRAGE]);
                    }
                    break;
                case 2:
                    if (countMinions() < MINION_COUNT) {
                        setMoveShortcut(SWARM, MOVES[SWARM]);
                    }
                    else {
                        setMoveShortcut(PREPARE, MOVES[PREPARE]);
                    }
                    break;
                case 3:
                    setMoveShortcut(PUNISHMENT, MOVES[PUNISHMENT]);
                    break;
            }
            ++numTurns;
        }
    }

    public void die() {
        super.die();

        for (AbstractMonster m : AbstractDungeon.getCurrRoom().monsters.monsters) {
            if (!m.isDeadOrEscaped() && m instanceof ExecutiveMinion) {
                AbstractDungeon.actionManager.addToBottom(new EscapeAction(m));
            }
        }
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