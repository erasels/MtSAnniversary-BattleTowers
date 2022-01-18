package BattleTowers.monsters.chess.queen;

import BattleTowers.monsters.chess.*;
import BattleTowers.monsters.chess.queen.customintents.IntentEnums;
import BattleTowers.monsters.chess.queen.powers.BlackWave;
import BattleTowers.monsters.chess.queen.powers.FactionChange;
import BattleTowers.monsters.chess.queen.powers.Inverse;
import BattleTowers.monsters.chess.queen.powers.WhiteWave;
import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.FastShakeAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.EnemyMoveInfo;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.scenes.AbstractScene;
import com.megacrit.cardcrawl.vfx.BorderFlashEffect;
import com.megacrit.cardcrawl.vfx.combat.MoveNameEffect;
import com.megacrit.cardcrawl.vfx.combat.ShockWaveEffect;


import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeMonsterPath;
import static BattleTowers.util.UC.*;

public class Queen extends AbstractCardChessMonster {
    public static final String ID = makeID(Queen.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;

    private static final byte QUEENS_WILL = 0;
    private static final byte MIMIC_BLACK = 1;
    private static final byte MIMIC_WHITE = 2;
    private static final byte DRAIN_OF_COLOUR = 3;
    private static final byte MIMIC_INVERT = 4;
    private static final byte QUEENS_PROTECTION = 5;
    private static final byte QUEENS_MARCH = 6;

    private int turn = 1;

    public final int willDamage = calcAscensionDamage(5);
    public final int willStrength = 1;

    public final int waveDamage = calcAscensionDamage(15);
    public final int marchDamage = calcAscensionDamage(20);

    public final int protectionBlock = calcAscensionTankiness(15);


    public Queen() {
        this(0.0f, 0.0f);
    }

    public Queen(final float x, final float y) {
        super(NAME, ID, 300, 0.0F, 0, 230.0f, 400.0f, null, x, y);
        this.animation = new BetterSpriterAnimation(makeMonsterPath("Chess/Queen/Spriter/Queen.scml"));
        this.setHp(calcAscensionTankiness(this.maxHealth));
        this.type = EnemyType.BOSS;
        numAdditionalMoves = 1;
        for (int i = 0; i < numAdditionalMoves; i++) {
            additionalMovesHistory.add(new ArrayList<>());
        }

        addMove(QUEENS_WILL, Intent.ATTACK_BUFF, willDamage);
        addMove(MIMIC_BLACK, Intent.UNKNOWN);
        addMove(MIMIC_WHITE, Intent.UNKNOWN);
        addMove(DRAIN_OF_COLOUR, IntentEnums.QUEEN_DRAIN_ATTACK, waveDamage);
        addMove(MIMIC_INVERT, Intent.UNKNOWN);
        addMove(QUEENS_PROTECTION, Intent.DEFEND, protectionBlock);
        addMove(QUEENS_MARCH, Intent.ATTACK, marchDamage);
    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        this.type = EnemyType.BOSS;
    }

    @Override
    public void usePreBattleAction() {
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(makeMonsterPath("Chess/Scene/atlas.atlas")));
        ReflectionHacks.setPrivate(AbstractDungeon.scene, AbstractScene.class, "bg", atlas.findRegion("mod/Chess"));
        runAnim("Idle");
        doPow(p(), new FactionChange(p()), true);
    }

    @Override
    public void takeTurn() {
        super.takeTurn();
        if (this.firstMove) {
            firstMove = false;
        }
        atb(new RemoveAllBlockAction(this, this));
        takeCustomTurn(this.moves.get(nextMove), p());
        for (int i = 0; i < additionalMoves.size(); i++) {
            EnemyMoveInfo additionalMove = additionalMoves.get(i);
            AdditionalIntent additionalIntent = additionalIntents.get(i);
            atb(new VFXActionButItCanFizzle(this, new MoveNameEffect(hb.cX - animX, hb.cY + hb.height / 2.0F, MOVES[additionalMove.nextMove])));
            atb(new BetterIntentFlashAction(this, additionalIntent.intentImg));
            takeCustomTurn(additionalMove, p());
            atb(new AbstractGameAction() {
                @Override
                public void update() {
                    additionalIntent.usePrimaryIntentsColor = true;
                    this.isDone = true;
                }
            });
        }
        atb(new AbstractGameAction() {
            @Override
            public void update() {
                turn += 1;
                isDone = true;
            }
        });
        atb(new RollMoveAction(this));
    }

    @Override
    public void takeCustomTurn(EnemyMoveInfo move, AbstractCreature target) {
        DamageInfo info = new DamageInfo(this, move.baseDamage, DamageInfo.DamageType.NORMAL);
        int multiplier = move.multiplier;

        if(info.base > -1) {
            info.applyPowers(this, target);
        }
        switch (move.nextMove) {
            case QUEENS_WILL: {
                dmg(target, info);
                doPow(Queen.this, new StrengthPower(Queen.this, willStrength), false);
                break;
            }
            case QUEENS_PROTECTION: {
                atb(new GainBlockAction(this, protectionBlock));
                break;
            }
            case QUEENS_MARCH: {
                // make animation later
                dmg(target, info);
                break;
            }
            case MIMIC_INVERT: {
                if(AbstractDungeon.monsterRng.random(0, 99) <= 45){
                    atb(new SFXAction("THUNDERCLAP"));
                    atb(new VFXAction(new BorderFlashEffect(Color.BLACK.cpy(), true)));
                    if (!Settings.FAST_MODE) {
                        atb(new FastShakeAction(Queen.this, 0.6F, 0.2F));
                    } else {
                        atb(new FastShakeAction(Queen.this, 0.6F, 0.15F));
                    }
                    atb(new AbstractGameAction() {
                        @Override
                        public void update() {
                            runAnim("IdleB");
                            isDone = true;
                        }
                    });
                    doPow(this, new BlackWave(this));
                }
                else {
                    atb(new SFXAction("THUNDERCLAP"));
                    atb(new VFXAction(new BorderFlashEffect(Color.WHITE.cpy(), true)));
                    if (!Settings.FAST_MODE) {
                        atb(new FastShakeAction(Queen.this, 0.6F, 0.2F));
                    } else {
                        atb(new FastShakeAction(Queen.this, 0.6F, 0.15F));
                    }
                    atb(new AbstractGameAction() {
                        @Override
                        public void update() {
                            runAnim("IdleW");
                            isDone = true;
                        }
                    });
                    doPow(this, new WhiteWave(this));
                }
                doPow(this, new Inverse(this));

                break;
            }
            case MIMIC_WHITE: {

                atb(new SFXAction("THUNDERCLAP"));
                atb(new VFXAction(new BorderFlashEffect(Color.WHITE.cpy(), true)));
                if (!Settings.FAST_MODE) {
                    atb(new FastShakeAction(Queen.this, 0.6F, 0.2F));
                } else {
                    atb(new FastShakeAction(Queen.this, 0.6F, 0.15F));
                }
                atb(new AbstractGameAction() {
                    @Override
                    public void update() {
                        runAnim("IdleW");
                        isDone = true;
                    }
                });
                doPow(this, new WhiteWave(this));
                break;
            }
            case MIMIC_BLACK: {
                atb(new SFXAction("THUNDERCLAP"));
                atb(new VFXAction(new BorderFlashEffect(Color.BLACK.cpy(), true)));
                if (!Settings.FAST_MODE) {
                    atb(new FastShakeAction(Queen.this, 0.6F, 0.2F));
                } else {
                    atb(new FastShakeAction(Queen.this, 0.6F, 0.15F));
                }
                atb(new AbstractGameAction() {
                    @Override
                    public void update() {
                        runAnim("IdleB");
                        isDone = true;
                    }
                });
                doPow(this, new BlackWave(this));
                break;
            }
            case DRAIN_OF_COLOUR: {
                atb(new AbstractGameAction() {
                    @Override
                    public void update() {
                        // TODO: Find a better way to do this.
                        AbstractPower amIInverted = Queen.this.getPower(Inverse.POWER_ID);
                        AbstractPower amIMimickingWhite = Queen.this.getPower(WhiteWave.POWER_ID);
                        AbstractPower amIMimickingBlack = Queen.this.getPower(BlackWave.POWER_ID);
                        AbstractPower playerFactionPower = p().getPower(FactionChange.POWER_ID);

                        System.out.println(((FactionChange)playerFactionPower).currentStance);
                        // factionStance 0 = BLACK
                        // factionstance 1 = WHITE.
                        if(playerFactionPower != null){

                            if(amIMimickingWhite != null){
                                System.out.println("mimicking white");
                                if(amIInverted != null){
                                    System.out.println("inverted");

                                    // Mimicking black, AKA if player is mimicking WHITE, deal damage.
                                    if(((FactionChange)playerFactionPower).currentStance == FactionChange.STANCE.WHITE){
                                        System.out.println("player mimicking white");
                                        dmg(p(), info, AttackEffect.NONE, true);
                                    }
                                    if (!Settings.FAST_MODE) {
                                        att(new FastShakeAction(p(), 0.6F, 0.2F));
                                        att(new VFXAction(Queen.this, new ShockWaveEffect(Queen.this.hb.cX, Queen.this.hb.cY, Color.BLACK.cpy(), ShockWaveEffect.ShockWaveType.NORMAL), 0.5F));
                                    } else {
                                        att(new FastShakeAction(p(), 0.6F, 0.15F));
                                        att(new VFXAction(Queen.this, new ShockWaveEffect(Queen.this.hb.cX, Queen.this.hb.cY, Color.BLACK.cpy(), ShockWaveEffect.ShockWaveType.NORMAL), 0.1F));
                                    }
                                    att(new VFXAction(new BorderFlashEffect(Color.BLACK.cpy(), true)));
                                    att(new SFXAction("THUNDERCLAP"));
                                }
                                else {
                                    System.out.println("mimicking WHITE still");
                                    if(((FactionChange)playerFactionPower).currentStance == FactionChange.STANCE.BLACK){
                                        System.out.println("player mimicking black");
                                        dmg(p(), info, AttackEffect.NONE, true);
                                    }
                                    if (!Settings.FAST_MODE) {
                                        att(new FastShakeAction(p(), 0.6F, 0.2F));
                                        att(new VFXAction(Queen.this, new ShockWaveEffect(Queen.this.hb.cX, Queen.this.hb.cY, Color.WHITE.cpy(), ShockWaveEffect.ShockWaveType.NORMAL), 0.5F));
                                    } else {
                                        att(new FastShakeAction(p(), 0.6F, 0.15F));
                                        att(new VFXAction(Queen.this, new ShockWaveEffect(Queen.this.hb.cX, Queen.this.hb.cY, Color.WHITE.cpy(), ShockWaveEffect.ShockWaveType.NORMAL), 0.1F));
                                    }
                                    att(new VFXAction(new BorderFlashEffect(Color.WHITE.cpy(), true)));
                                    att(new SFXAction("THUNDERCLAP"));
                                }
                            }
                            else if(amIMimickingBlack != null){
                                System.out.println("mimicking black");
                                if(amIInverted != null){
                                    System.out.println("mimicking white");
                                    // Mimicking White, AKA if player is mimicking black, deal damage.
                                    if(((FactionChange)playerFactionPower).currentStance == FactionChange.STANCE.BLACK){
                                        System.out.println("player mimicking black");
                                        dmg(p(), info, AttackEffect.NONE, true);
                                    }
                                    if (!Settings.FAST_MODE) {
                                        att(new FastShakeAction(p(), 0.6F, 0.2F));
                                        att(new VFXAction(Queen.this, new ShockWaveEffect(Queen.this.hb.cX, Queen.this.hb.cY, Color.WHITE.cpy(), ShockWaveEffect.ShockWaveType.NORMAL), 0.5F));
                                    } else {
                                        att(new FastShakeAction(p(), 0.6F, 0.15F));
                                        att(new VFXAction(Queen.this, new ShockWaveEffect(Queen.this.hb.cX, Queen.this.hb.cY, Color.WHITE.cpy(), ShockWaveEffect.ShockWaveType.NORMAL), 0.1F));
                                    }
                                    att(new VFXAction(new BorderFlashEffect(Color.WHITE.cpy(), true)));
                                    att(new SFXAction("THUNDERCLAP"));
                                }
                                else {
                                    System.out.println("mimicking white");
                                    if(((FactionChange)playerFactionPower).currentStance == FactionChange.STANCE.WHITE){
                                        System.out.println("player mimicking white");
                                        dmg(p(), info, AttackEffect.NONE, true);
                                    }
                                    if (!Settings.FAST_MODE) {
                                        att(new FastShakeAction(p(), 0.6F, 0.2F));
                                        att(new VFXAction(Queen.this, new ShockWaveEffect(Queen.this.hb.cX, Queen.this.hb.cY, Color.BLACK.cpy(), ShockWaveEffect.ShockWaveType.NORMAL), 0.5F));
                                    } else {
                                        att(new FastShakeAction(p(), 0.6F, 0.15F));
                                        att(new VFXAction(Queen.this, new ShockWaveEffect(Queen.this.hb.cX, Queen.this.hb.cY, Color.BLACK.cpy(), ShockWaveEffect.ShockWaveType.NORMAL), 0.1F));
                                    }
                                    att(new VFXAction(new BorderFlashEffect(Color.BLACK.cpy(), true)));
                                    att(new SFXAction("THUNDERCLAP"));
                                }
                            }
                        }
                        isDone = true;
                    }
                });
                atb(new AbstractGameAction() {
                    @Override
                    public void update() {
                        runAnim("Idle");
                        isDone = true;
                    }
                });
                atb(new RemoveSpecificPowerAction(this, this, BlackWave.POWER_ID));
                atb(new RemoveSpecificPowerAction(this, this, WhiteWave.POWER_ID));
                atb(new RemoveSpecificPowerAction(this, this, Inverse.POWER_ID));
                break;
            }
        }
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
    public void applyPowers() {
        super.applyPowers();
        for (int i = 0; i < additionalIntents.size(); i++) {
            AdditionalIntent additionalIntent = additionalIntents.get(i);
            EnemyMoveInfo additionalMove = null;
            if (i < additionalMoves.size()) {
                additionalMove = additionalMoves.get(i);
            }
            if (additionalMove != null) {
                applyPowersToAdditionalIntent(additionalMove, additionalIntent, p(), null);
            }
        }
    }

    @Override
    protected void getMove(final int num) {
        if(lastMove(MIMIC_BLACK) || lastMove(MIMIC_WHITE) || lastMove(MIMIC_INVERT)){setMoveShortcut(DRAIN_OF_COLOUR, MOVES[DRAIN_OF_COLOUR]);}
        else {
            switch (turn){
                case 6:
                    setMoveShortcut(QUEENS_MARCH, MOVES[QUEENS_MARCH]);
                    break;
                case 9:
                case 7:
                    setMoveShortcut(MIMIC_INVERT, MOVES[MIMIC_INVERT]);
                    break;
                case 13:
                case 5:
                case 11:
                    setMoveShortcut(QUEENS_PROTECTION);
                    break;
                default:
                    if(this.hasPower(StrengthPower.POWER_ID)){
                        if(AbstractDungeon.monsterRng.random(0, 99) <= 45){setMoveShortcut(MIMIC_INVERT, MOVES[MIMIC_INVERT]);}
                        else {
                            if(AbstractDungeon.monsterRng.random(0, 99) <= 45){setMoveShortcut(MIMIC_WHITE, MOVES[MIMIC_WHITE]);}
                            else {setMoveShortcut(MIMIC_BLACK, MOVES[MIMIC_BLACK]); }
                        }
                    }
                    else {
                        if(AbstractDungeon.monsterRng.random(0, 99) <= 45){
                            setMoveShortcut(MIMIC_BLACK, MOVES[MIMIC_BLACK]);
                        }
                        else {
                            setMoveShortcut(MIMIC_WHITE, MOVES[MIMIC_WHITE]);
                        }
                    }
            }
        }
    }

    @Override
    public void getAdditionalMoves(int num, int whichMove) {
        ArrayList<Byte> moveHistory = additionalMovesHistory.get(whichMove);
        switch (turn) {
            default: {
                if (turn % 2 != 0) {
                    setAdditionalMoveShortcut(QUEENS_WILL, moveHistory);
                }
                break;
            }
        }
    }


}