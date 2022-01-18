package BattleTowers.monsters;

import BattleTowers.powers.VengeancePower;
import BattleTowers.relics.SentryOrb;
import BattleTowers.vfx.ColoredSmallLaserEffect;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.FastShakeAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.status.Dazed;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.ArtifactPower;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.vfx.BorderFlashEffect;
import com.megacrit.cardcrawl.vfx.combat.ShockWaveEffect;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class SentryHuge extends AbstractBTMonster {
    public static final String ID = makeID(SentryHuge.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;

    //name of the monster's moves
    private static final byte BEAM = 0;
    private static final byte ANNOY = 1;
    private static final String ATTACK_STATE = "ATTACK";

    //Monster stats
    private static final int MIN_HP = 82;
    private static final int MAX_HP = 90;
    private static final int BEAM_DAMAGE = 6;
    private static final int HITS = 2;
    private final int STATUS_AMOUNT = calcAscensionSpecial(3);
    private final int ARTIFACT = calcAscensionSpecial(2);
    private final int VENGEANCE = calcAscensionSpecial(2);
    private static final Color COLOR = Color.CYAN.cpy();
    private static final AbstractCard STATUS = new Dazed();
    private static final float SCALE = 0.7F;


    public SentryHuge(final float x, final float y) {
        super(NAME, ID, 140, 0.0F, -5.0f, 180/SCALE, 350/SCALE, null, x, y);
        this.loadAnimation("images/monsters/theBottom/sentry/skeleton.atlas", "images/monsters/theBottom/sentry/skeleton.json", SCALE);
        AnimationState.TrackEntry e = this.state.setAnimation(0, "idle", true);
        e.setTimeScale(2.0F);
        e.setTime(e.getEndTime() * MathUtils.random());
        stateData.setMix("idle", "attack", 0.1F);
        stateData.setMix("idle", "spaz1", 0.1F);
        stateData.setMix("idle", "hit", 0.1F);
        setHp(calcAscensionTankiness(MIN_HP), calcAscensionTankiness(MAX_HP));
        addMove(BEAM, Intent.ATTACK, calcAscensionDamage(BEAM_DAMAGE), HITS, true);
        addMove(ANNOY, Intent.DEBUFF);
    }

    public void usePreBattleAction() {
        addToBot(new ApplyPowerAction(this, this, new ArtifactPower(this, ARTIFACT)));
        for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
            if (m != this && !m.isDeadOrEscaped()) {
                addToBot(new ApplyPowerAction(m, this, new VengeancePower(m, this, VENGEANCE)));
            }
        }
    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        // we set the enemy type here so the calcAscensionMethods are called after the enemy type is set
        this.type = EnemyType.ELITE;
    }

    @Override
    public void takeTurn() {
        //Automatically grabs the damage values and number of hits value from the moves hashmap based on the currently set move
        DamageInfo info = new DamageInfo(this, this.moves.get(nextMove).baseDamage, DamageInfo.DamageType.NORMAL);
        int multiplier = this.moves.get(nextMove).multiplier;

        if (info.base > -1) {
            info.applyPowers(this, AbstractDungeon.player);
        }

        //carries out actions based on the current move
        //useFastAttackAnimation causes the monster to jump forward when it attacks
        switch (this.nextMove) {
            case BEAM:
                addToBot(new ChangeStateAction(this, ATTACK_STATE));
                for (int i = 0; i < multiplier; i++) {
                    addToBot(new VFXAction(new BorderFlashEffect(COLOR.cpy())));
                    addToBot(new SFXAction("ATTACK_MAGIC_BEAM_SHORT", 0.5F));
                    if (Settings.FAST_MODE) {
                        addToBot(new VFXAction(new ColoredSmallLaserEffect(AbstractDungeon.player.hb.cX, AbstractDungeon.player.hb.cY, this.hb.cX, this.hb.cY, COLOR.cpy()), 0.1F));
                    } else {
                        addToBot(new VFXAction(new ColoredSmallLaserEffect(AbstractDungeon.player.hb.cX, AbstractDungeon.player.hb.cY, this.hb.cX, this.hb.cY, COLOR.cpy()), 0.3F));
                    }
                    addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.NONE, Settings.FAST_MODE));
                }
                break;
            case ANNOY:
                addToBot(new SFXAction("THUNDERCLAP"));
                if (!Settings.FAST_MODE) {
                    addToBot(new VFXAction(this, new ShockWaveEffect(this.hb.cX, this.hb.cY, COLOR.cpy(), ShockWaveEffect.ShockWaveType.CHAOTIC), 0.5F));
                    addToBot(new FastShakeAction(AbstractDungeon.player, 0.6F, 0.2F));
                } else {
                    addToBot(new VFXAction(this, new ShockWaveEffect(this.hb.cX, this.hb.cY, COLOR.cpy(), ShockWaveEffect.ShockWaveType.CHAOTIC), 0.1F));
                    addToBot(new FastShakeAction(AbstractDungeon.player, 0.6F, 0.15F));
                }
                addToBot(new MakeTempCardInDiscardAction(STATUS.makeStatEquivalentCopy(), STATUS_AMOUNT));
        }
        addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(int i) {
        ArrayList<Byte> possibilities = new ArrayList<>();
        if (firstMove) {
            if (AbstractDungeon.getMonsters().monsters.lastIndexOf(this) % 2 == 0) {
                possibilities.add(ANNOY);
            } else {
                possibilities.add(BEAM);
            }
            firstMove = false;
        } else {
            if (!this.lastMove(BEAM)) {
                possibilities.add(BEAM);
            }

            if (!this.lastMove(ANNOY)) {
                possibilities.add(ANNOY);
            }
        }
        byte move = possibilities.get(AbstractDungeon.monsterRng.random(possibilities.size() - 1));
        setMoveShortcut(move, MOVES[move]);
    }

    public void damage(DamageInfo info) {
        super.damage(info);
        if (info.owner != null && info.type != DamageInfo.DamageType.THORNS && info.output > 0) {
            this.state.setAnimation(0, "hit", false);
            this.state.addAnimation(0, "idle", true, 0.0F);
        }

    }

    public void changeState(String stateName) {
        if (stateName.equals(ATTACK_STATE)) {
            this.state.setAnimation(0, "attack", false);
            this.state.addAnimation(0, "idle", true, 0.0F);
        }
    }
}