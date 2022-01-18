package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import BattleTowers.cards.Prick;
import BattleTowers.powers.FeelMyPainPower;
import BattleTowers.relics.CursedDoll;
import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.LoseHPAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAndDeckAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.FrailPower;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.powers.WeakPower;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.vfx.CollectorCurseEffect;

import static BattleTowers.BattleTowers.makeID;

public class VoodooDoll extends AbstractBTMonster
{
    public static final String ID = makeID(VoodooDoll.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;
    private static final String IMG = BattleTowers.makeImagePath("monsters/VoodooDoll/VoodooDoll.png");

    private static final byte CURSE = 0;
    private static final byte PINS = 1;
    private static final byte SMOTHER = 2;

    private static final int CURSE_DEBUFF = 99;
    private static final int CURSE_HP_LOSS = 30;
    private static final int MAX_CURSE = 3;
    private int curseCount = 0;
    private final AbstractCard curse = new Prick();

    public VoodooDoll() {
        this(0.0f, 0.0f);
    }

    public VoodooDoll(final float x, final float y) {
        super(NAME, ID, 140, 0.0F, 0.0f, 180.0f, 170.0f, IMG, x, y);
        setHp(calcAscensionTankiness(200));
        addMove(CURSE, Intent.STRONG_DEBUFF);
        addMove(PINS, Intent.ATTACK_DEBUFF, calcAscensionDamage(12));
        addMove(SMOTHER, Intent.ATTACK, calcAscensionDamage(17));
    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        this.type = EnemyType.ELITE;
    }

    @Override
    public void usePreBattleAction() {
        addToBot(new ApplyPowerAction(this, this, new FeelMyPainPower(this)));
        if (AbstractDungeon.ascensionLevel >= 18) {
            curse.upgrade();
        }
    }

    @Override
    public void takeTurn() {
        DamageInfo info = new DamageInfo(this, this.moves.get(nextMove).baseDamage, DamageInfo.DamageType.NORMAL);
        int multiplier = this.moves.get(nextMove).multiplier;

        if(info.base > -1) {
            info.applyPowers(this, AbstractDungeon.player);
        }

        if (firstMove) {
            firstMove = false;
        }

        switch (this.nextMove) {
            case CURSE: {
                addToBot(new SFXAction("MONSTER_COLLECTOR_DEBUFF"));
                addToBot(new VFXAction(new CollectorCurseEffect(AbstractDungeon.player.hb.cX, AbstractDungeon.player.hb.cY)));
                addToBot(new VFXAction(new CollectorCurseEffect(this.hb.cX, this.hb.cY), 2.0F));

                addToBot(new LoseHPAction(this, this, CURSE_HP_LOSS));
                if (curseCount == 0) {
                    addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new WeakPower(AbstractDungeon.player, CURSE_DEBUFF, true), CURSE_DEBUFF));
                } else if (curseCount == 1) {
                    addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new FrailPower(AbstractDungeon.player, CURSE_DEBUFF, true), CURSE_DEBUFF));
                } else {
                    addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new VulnerablePower(AbstractDungeon.player, CURSE_DEBUFF, true), CURSE_DEBUFF));
                }
                curseCount++;
                break;
            }
            case PINS: {
                useSlowAttackAnimation();
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_LIGHT));
                addToBot(new MakeTempCardInDiscardAndDeckAction(curse.makeStatEquivalentCopy()));
                break;
            }
            case SMOTHER: {
                useSlowAttackAnimation();
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                break;
            }
        }
        addToBot(new RollMoveAction(this));
    }

    @Override
    public void applyPowers() {
        //hijack applyPowers for some budget custom intent stuff lol
        super.applyPowers();
        if (nextMove != CURSE) {
            Color color = new Color(1.0F, 1.0F, 1.0F, 0.5F);
            ReflectionHacks.setPrivate(this, AbstractMonster.class, "intentColor", color);
        } else {
            Color color = new Color(0.5F, 0.0F, 1.0F, 0.5F);
            ReflectionHacks.setPrivate(this, AbstractMonster.class, "intentColor", color);
            PowerTip intentTip = (PowerTip)ReflectionHacks.getPrivate(this, AbstractMonster.class, "intentTip");
            intentTip.body = DIALOG[0] + CURSE_HP_LOSS + DIALOG[1];
        }
    }

    @Override
    public void createIntent() {
        super.createIntent();
        applyPowers();
    }

    @Override
    protected void getMove(final int num) {
        if (curseCount < MAX_CURSE) {
            if (!lastMove(CURSE) && !firstMove) {
                setMoveShortcut(CURSE, MOVES[CURSE]);
            } else if (lastMoveBefore(PINS)) {
                setMoveShortcut(SMOTHER, MOVES[SMOTHER]);
            } else {
                setMoveShortcut(PINS, MOVES[PINS]);
            }
        } else {
            if (!lastTwoMoves(SMOTHER)) {
                setMoveShortcut(SMOTHER, MOVES[SMOTHER]);
            } else {
                setMoveShortcut(PINS, MOVES[PINS]);
            }
        }
    }

}