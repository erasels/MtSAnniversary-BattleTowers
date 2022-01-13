package BattleTowers.monsters;

import BattleTowers.vfx.ColoredSmallLaserEffect;
import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.FastShakeAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.powers.ArtifactPower;
import com.megacrit.cardcrawl.vfx.BorderFlashEffect;
import com.megacrit.cardcrawl.vfx.combat.ShockWaveEffect;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public abstract class AbstractElementalSentry extends AbstractBTMonster {
    public static final String ENCOUNTER = makeID("Elemental_Sentries");
    public final String NAME;
    public final String[] MOVES;
    public final String[] DIALOG;


    //name of the monster's moves
    private static final byte BEAM = 0;
    private static final byte ANNOY = 1;
    private static final String ATTACK_STATE = "ATTACK";

    //Monster stats
    private static final int MIN_HP = 20;
    private static final int MAX_HP = 28;
    private static final int BEAM_DAMAGE = 5;
    private final int STATUS_AMOUNT = calcAscensionSpecial(2)-1;
    private final Color color;
    private final AbstractCard status;
    protected static final float SCALE = 1.4F;


    public AbstractElementalSentry(String ID, Color c, AbstractCard status, final float x, final float y) {
        super(CardCrawlGame.languagePack.getMonsterStrings(ID).NAME, ID, 140, 0.0F, -5.0f, 180/SCALE, 350/SCALE, null, x, y);
        MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
        NAME = monsterStrings.NAME;
        MOVES = monsterStrings.MOVES;
        DIALOG = monsterStrings.DIALOG;
        setHp(calcAscensionTankiness(MIN_HP), calcAscensionTankiness(MAX_HP));
        addMove(BEAM, Intent.ATTACK, calcAscensionDamage(BEAM_DAMAGE));
        addMove(ANNOY, Intent.DEBUFF);
        this.color = c;
        this.status = status;
    }

    public void usePreBattleAction() {
        addToBot(new ApplyPowerAction(this, this, new ArtifactPower(this, 1)));
    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        // we set the enemy type here so the calcAscensionMethods are called after the enemy type is set
        this.type = EnemyType.NORMAL;
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
                addToBot(new SFXAction("ATTACK_MAGIC_BEAM_SHORT", 0.5F));
                addToBot(new VFXAction(new BorderFlashEffect(color.cpy())));
                if (Settings.FAST_MODE) {
                    addToBot(new VFXAction(new ColoredSmallLaserEffect(AbstractDungeon.player.hb.cX, AbstractDungeon.player.hb.cY, this.hb.cX, this.hb.cY, color.cpy()), 0.1F));
                } else {
                    addToBot(new VFXAction(new ColoredSmallLaserEffect(AbstractDungeon.player.hb.cX, AbstractDungeon.player.hb.cY, this.hb.cX, this.hb.cY, color.cpy()), 0.3F));
                }
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.NONE, Settings.FAST_MODE));
                break;
            case ANNOY:
                addToBot(new SFXAction("THUNDERCLAP"));
                if (!Settings.FAST_MODE) {
                    addToBot(new VFXAction(this, new ShockWaveEffect(this.hb.cX, this.hb.cY, color.cpy(), ShockWaveEffect.ShockWaveType.CHAOTIC), 0.5F));
                    addToBot(new FastShakeAction(AbstractDungeon.player, 0.6F, 0.2F));
                } else {
                    addToBot(new VFXAction(this, new ShockWaveEffect(this.hb.cX, this.hb.cY, color.cpy(), ShockWaveEffect.ShockWaveType.CHAOTIC), 0.1F));
                    addToBot(new FastShakeAction(AbstractDungeon.player, 0.6F, 0.15F));
                }
                addToBot(new MakeTempCardInDiscardAction(status.makeStatEquivalentCopy(), STATUS_AMOUNT));
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