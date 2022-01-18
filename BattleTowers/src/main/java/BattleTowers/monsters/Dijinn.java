package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import BattleTowers.cards.DijinnCards.DijinnWrath;
import BattleTowers.cards.DijinnCards.MakeAWish;
import BattleTowers.powers.MakeAWishPower;
import BattleTowers.relics.DijinnLamp;
import com.evacipated.cardcrawl.mod.stslib.actions.tempHp.AddTemporaryHPAction;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.TalkAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.actions.watcher.ChooseOneAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.status.VoidCard;
import com.megacrit.cardcrawl.cards.tempCards.Miracle;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.vfx.combat.GoldenSlashEffect;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class Dijinn extends AbstractBTMonster
{
    public static final String ID = makeID(Dijinn.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;
    private static final String IMG = BattleTowers.makeImagePath("monsters/Dijinn/Dijinn.png");

    private static final byte EMPOWER = 0;
    private static final byte CORRUPTION = 1;
    private static final byte GOLDEN_CRUCIBLE = 2;

    private final int VOIDS = calcAscensionSpecial(2);
    private final int MIRACLES = 2;
    private final int ENEMY_STR = calcAscensionSpecial(4);
    private final int PLAYER_STR = 3;
    private int wishCount = 3;
    public int BLOCK = calcAscensionTankiness(12);

    public Dijinn() {
        this(0.0f, 0.0f);
    }

    public Dijinn(final float x, final float y) {
        super(NAME, ID, 140, 0.0F, 0.0f, 220.0f, 380.0f, IMG, x, y);
        setHp(calcAscensionTankiness(300));
        addMove(EMPOWER, Intent.DEFEND_BUFF);
        addMove(CORRUPTION, Intent.STRONG_DEBUFF);
        addMove(GOLDEN_CRUCIBLE, Intent.ATTACK, calcAscensionDamage(14), 2);
    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        this.type = EnemyType.BOSS;
    }

    @Override
    public void usePreBattleAction() {
        addToBot(new ApplyPowerAction(this, this, new MakeAWishPower(this, wishCount)));
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
            addToBot(new TalkAction(this, DIALOG[0]));
        }

        ArrayList<AbstractCard> options = new ArrayList<>();
        // put dummy cards here just in case somehow none of the cases in the switch statement run
        AbstractCard card1 = new DijinnWrath(this, 0);;
        AbstractCard card2 = new MakeAWish(this, 0);

        switch (this.nextMove) {
            case EMPOWER: {
                if (wishCount <= 0) {
                    EmpowerAction();
                } else {
                    card1 = new DijinnWrath(this, ENEMY_STR);
                    card2 = new MakeAWish(this, PLAYER_STR);
                }
                break;
            }
            case CORRUPTION: {
                if (wishCount <= 0) {
                    CorruptionAction();
                } else {
                    card1 = new DijinnWrath(this, VOIDS);
                    card1.cardsToPreview = new VoidCard();
                    card2 = new MakeAWish(this, MIRACLES);
                    card2.cardsToPreview = new Miracle();
                }
                break;
            }
            case GOLDEN_CRUCIBLE: {
                if (wishCount <= 0) {
                    AttackAction(info, multiplier);
                } else {
                    card1 = new DijinnWrath(this, info.output);
                    card2 = new MakeAWish(this, info.output / 2);
                }
                break;
            }
        }
        if (wishCount > 0) {
            options.add(card1);
            options.add(card2);
            addToBot(new ChooseOneAction(options));
        }
        addToBot(new RollMoveAction(this));
    }

    public void EmpowerAction() {
        addToBot(new GainBlockAction(this, this, BLOCK));
        addToBot(new ApplyPowerAction(this, this, new StrengthPower(this, ENEMY_STR), ENEMY_STR));
    }

    public void CorruptionAction() {
        addToBot(new MakeTempCardInDrawPileAction(new VoidCard(), VOIDS, true, true));
    }

    public void AttackAction(DamageInfo info, int multiplier) {
        useSlowAttackAnimation();
        for (int i = 0; i < multiplier; i++) {
            AbstractDungeon.actionManager.addToBottom(new VFXAction(new GoldenSlashEffect(AbstractDungeon.player.hb.cX - 60.0F * Settings.scale, AbstractDungeon.player.hb.cY, false), 0.0f));
            addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.NONE));
        }
    }

    public void dijinnWrath() {
        DamageInfo info = new DamageInfo(this, this.moves.get(nextMove).baseDamage, DamageInfo.DamageType.NORMAL);
        int multiplier = this.moves.get(nextMove).multiplier;

        if(info.base > -1) {
            info.applyPowers(this, AbstractDungeon.player);
        }

        switch (this.nextMove) {
            case EMPOWER: {
                EmpowerAction();
                break;
            }
            case CORRUPTION: {
                CorruptionAction();
                break;
            }
            case GOLDEN_CRUCIBLE: {
                AttackAction(info, multiplier);
                break;
            }
        }
    }

    public void makeAWish(int magicNumber) {
        switch (this.nextMove) {
            case EMPOWER: {
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new StrengthPower(AbstractDungeon.player, magicNumber), magicNumber));
                break;
            }
            case CORRUPTION: {
                addToBot(new MakeTempCardInHandAction(new Miracle(), magicNumber));
                break;
            }
            case GOLDEN_CRUCIBLE: {
                addToBot(new AddTemporaryHPAction(AbstractDungeon.player, this, magicNumber));
                break;
            }
        }
        wishCount--;
        addToBot(new ReducePowerAction(this, this, MakeAWishPower.POWER_ID, 1));
        if (wishCount <= 0) {
            addToBot(new TalkAction(this, DIALOG[4]));
        } else {
            addToBot(new TalkAction(this, DIALOG[AbstractDungeon.monsterRng.random(1, 3)]));
        }
    }

    @Override
    protected void getMove(final int num) {
        if (!lastMove(GOLDEN_CRUCIBLE)) {
            setMoveShortcut(GOLDEN_CRUCIBLE, MOVES[GOLDEN_CRUCIBLE]);
        } else if (lastMoveBefore(CORRUPTION)) {
            setMoveShortcut(EMPOWER, MOVES[EMPOWER]);
        } else {
            setMoveShortcut(CORRUPTION, MOVES[CORRUPTION]);
        }
    }

}