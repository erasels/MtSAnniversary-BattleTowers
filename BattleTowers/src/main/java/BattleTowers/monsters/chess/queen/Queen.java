package BattleTowers.monsters.chess.queen;

import BattleTowers.monsters.chess.AbstractCardChessMonster;
import BattleTowers.monsters.chess.BetterSpriterAnimation;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.mod.stslib.powers.StunMonsterPower;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.FastShakeAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.RemoveAllBlockAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.colorless.Madness;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.EnemyMoveInfo;
import com.megacrit.cardcrawl.monsters.exordium.Sentry;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.vfx.BorderFlashEffect;
import com.megacrit.cardcrawl.vfx.combat.ShockWaveEffect;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeMonsterPath;
import static BattleTowers.util.UC.*;

public class Queen extends AbstractCardChessMonster {
    public static final String ID = makeID(Queen.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;

    // Deal 7 Damage. If this was unblocked, Gain 3 Strength Next turn.
    private static final byte QUEENS_WILL = 0;
    private static final byte STORE_BLACK = 1;
    private static final byte STORE_WHITE = 2;
    private static final byte STORED_WAVE = 3;
    private static final byte INVERSE_STORE = 4;
    private static final byte QUEENS_PROTECTION = 5;
    private static final byte QUEENS_MARCH = 6;
    private static final byte QUEENS_DECREE = 7;

    private int turn = 1;
    // Timeline:

    // T1: Store_Black or Store_White.

    // T2: Stored_Wave + Queen's Will.

    // T3: Store_Black or Store_White.

    // T4: Stored_Wave + Queen's Will.

    // T5: Queen's Protection {BLOCK}.

    // T6: Queen's March

    // T7: Inverse_Store_Black / Inverse_Store_White

    // T8: Stored_Wave + Queen's Will

    // T9: Inverse_Stored_Black/White + Queen's Will.

    // T10: Queen's Will + Stored_Wave

    // T11: Queen's Protection

    // T12: Queen's March

    // T13: Queen's Decree

    // T14: Queen's March

    public final int willDamage = calcAscensionDamage(7);
    public final int willStrength = calcAscensionSpecial(2);

    public final int waveDamage = calcAscensionDamage(12);
    public final int marchDamage = calcAscensionDamage(20);

    public final int protectionBlock = calcAscensionTankiness(20);

    public Queen() {
        this(0.0f, 0.0f);
    }

    public Queen(final float x, final float y) {
        super(NAME, ID, 350, 0.0F, 0, 230.0f, 265.0f, null, x, y);
        this.animation = new BetterSpriterAnimation(makeMonsterPath("Chess/Queen/Spriter/Queen.scml"));
        this.setHp(calcAscensionTankiness(this.maxHealth));
        this.type = EnemyType.BOSS;
        numAdditionalMoves = 1;
        for (int i = 0; i < numAdditionalMoves; i++) {
            additionalMovesHistory.add(new ArrayList<>());
        }

        addMove(QUEENS_WILL, Intent.ATTACK_BUFF, willDamage);
        addMove(STORE_BLACK, Intent.BUFF);
        addMove(STORE_WHITE, Intent.BUFF);
        addMove(STORED_WAVE, Intent.ATTACK, waveDamage);
        addMove(INVERSE_STORE, Intent.BUFF);
        addMove(QUEENS_PROTECTION, Intent.DEFEND, protectionBlock);
        addMove(QUEENS_MARCH, Intent.ATTACK, marchDamage);
        addMove(QUEENS_DECREE, Intent.BUFF);
    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        this.type = EnemyType.BOSS;
    }

    @Override
    public void usePreBattleAction() {

    }


    @Override
    public void takeTurn() {
        DamageInfo info;
        int multiplier = 0;
        if (moves.containsKey(this.nextMove)) {
            EnemyMoveInfo emi = moves.get(this.nextMove);
            info = new DamageInfo(this, emi.baseDamage, DamageInfo.DamageType.NORMAL);
            multiplier = emi.multiplier;
        } else {
            info = new DamageInfo(this, 0, DamageInfo.DamageType.NORMAL);
        }
        AbstractCreature target = p();
        if (info.base > -1) {
            info.applyPowers(this, target);
        }
        switch (this.nextMove) {
            case QUEENS_WILL: {
                dmg(target, info);
                atb(new AbstractGameAction() {
                    @Override
                    public void update() {
                        if (target.lastDamageTaken > 0) {
                            doPow(Queen.this, new StrengthPower(Queen.this, willStrength), true);
                        }
                        isDone = true;
                    }
                });
                break;
            }
            case QUEENS_DECREE: {
                break;
            }
            case QUEENS_MARCH: {
                // make animation later
                dmg(target, info);
                break;
            }
            case INVERSE_STORE: {
                break;
            }
            case STORE_WHITE: {
                atb(new AbstractGameAction() {
                    @Override
                    public void update() {
                        runAnim("IdleW");
                        isDone = true;
                    }
                });
                AbstractDungeon.actionManager.addToBottom(new SFXAction("THUNDERCLAP"));
                if (!Settings.FAST_MODE) {
                    AbstractDungeon.actionManager.addToBottom(new VFXAction(this, new ShockWaveEffect(this.hb.cX, this.hb.cY, Color.WHITE.cpy(), ShockWaveEffect.ShockWaveType.ADDITIVE), 0.5F));
                    AbstractDungeon.actionManager.addToBottom(new FastShakeAction(AbstractDungeon.player, 0.6F, 0.2F));
                } else {
                    AbstractDungeon.actionManager.addToBottom(new VFXAction(this, new ShockWaveEffect(this.hb.cX, this.hb.cY, Color.WHITE.cpy(), ShockWaveEffect.ShockWaveType.ADDITIVE), 0.1F));
                    AbstractDungeon.actionManager.addToBottom(new FastShakeAction(AbstractDungeon.player, 0.6F, 0.15F));
                }
                AbstractDungeon.actionManager.addToBottom(new VFXAction(new BorderFlashEffect(Color.WHITE.cpy(), true)));
                break;
            }
            case STORE_BLACK: {
                break;
            }
            case STORED_WAVE: {
                break;
            }
        }
        atb(new RollMoveAction(this));
        atb(new AbstractGameAction() {
            @Override
            public void update() {
                createIntent();
                this.isDone = true;
            }
        });
    }


    @Override
    protected void waitAnimation(AbstractCreature enemy) {
        waitAnimation(0.25f, enemy);
    }

    private void moveAnimation(float x, AbstractCreature enemy) {
        atb(new AbstractGameAction() {
            @Override
            public void update() {
                if (enemy == null || !enemy.isDeadOrEscaped()) {
                    drawX = x;
                }
                this.isDone = true;
            }
        });
    }

    private void setFlipAnimation(boolean flipHorizontal, AbstractCreature enemy) {
        atb(new AbstractGameAction() {
            @Override
            public void update() {
                if (enemy == null || !enemy.isDeadOrEscaped()) {
                    animation.setFlip(flipHorizontal, false);
                }
                this.isDone = true;
            }
        });
    }

    @Override
    protected void getMove(final int num) {
        if(lastMove(STORE_BLACK) || lastMove(STORE_WHITE) || lastMove(INVERSE_STORE)){setMoveShortcut(INVERSE_STORE, MOVES[INVERSE_STORE], getMoveCardFromByte(INVERSE_STORE));}
        else if(lastMove(QUEENS_DECREE)){setMoveShortcut(QUEENS_MARCH, MOVES[QUEENS_MARCH], getMoveCardFromByte(QUEENS_MARCH));}
        else {
            switch (turn){
                case 6:
                    setMoveShortcut(QUEENS_MARCH, MOVES[QUEENS_MARCH], getMoveCardFromByte(QUEENS_MARCH));
                    break;
                case 9:
                case 7:
                    setMoveShortcut(INVERSE_STORE, MOVES[INVERSE_STORE], getMoveCardFromByte(INVERSE_STORE));
                    break;
                case 13:
                    setMoveShortcut(QUEENS_DECREE, MOVES[QUEENS_DECREE], getMoveCardFromByte(QUEENS_DECREE));
                    break;
                case 5:
                case 11:
                    setMoveShortcut(QUEENS_PROTECTION);
                    break;
                default:
                    if(this.hasPower(StrengthPower.POWER_ID)){
                        if(AbstractDungeon.monsterRng.random(0, 99) <= 45){setMoveShortcut(INVERSE_STORE, MOVES[INVERSE_STORE], getMoveCardFromByte(INVERSE_STORE));}
                        else {
                            if(AbstractDungeon.monsterRng.random(0, 99) <= 45){setMoveShortcut(STORE_WHITE, MOVES[STORE_WHITE], getMoveCardFromByte(STORE_WHITE));}
                            else {setMoveShortcut(STORE_BLACK, MOVES[STORE_BLACK], getMoveCardFromByte(STORE_BLACK)); }
                        }
                    }
                    else {
                        if(AbstractDungeon.monsterRng.random(0, 99) <= 45){
                            setMoveShortcut(STORE_BLACK, MOVES[STORE_BLACK], getMoveCardFromByte(STORE_BLACK));
                        }
                        setMoveShortcut(STORE_WHITE, MOVES[STORE_WHITE], getMoveCardFromByte(STORE_WHITE));
                    }
            }
        }
    }

    @Override
    public void getAdditionalMoves(int num, int whichMove) {
        ArrayList<Byte> moveHistory = additionalMovesHistory.get(whichMove);
        switch (turn) {
            case 2:
            case 4:
            case 5:
            case 8:
            case 10:
            case 11: {
                setAdditionalMoveShortcut(QUEENS_WILL, moveHistory, getMoveCardFromByte(QUEENS_WILL));
                break;
            }
            default: {
                if (turn >= 14) {
                    setAdditionalMoveShortcut(QUEENS_WILL, moveHistory, getMoveCardFromByte(QUEENS_WILL));
                }
                break;
            }
        }
    }

    protected AbstractCard getMoveCardFromByte(Byte move) {
        ArrayList<AbstractCard> list = new ArrayList<>();
        list.add(new Madness());
        list.add(new Madness());
        list.add(new Madness());
        list.add(new Madness());
        list.add(new Madness());
        list.add(new Madness());

        return list.get(move);
    }


}