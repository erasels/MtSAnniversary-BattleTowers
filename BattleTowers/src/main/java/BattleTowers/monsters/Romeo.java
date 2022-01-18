package BattleTowers.monsters;

import BattleTowers.powers.CounterPower;
import BattleTowers.powers.GrievousWoundsPower;
import BattleTowers.util.UC;
import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
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
import com.megacrit.cardcrawl.events.city.MaskedBandits;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.EnemyMoveInfo;
import com.megacrit.cardcrawl.powers.IntangiblePlayerPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.vfx.UncommonPotionParticleEffect;

import java.util.HashMap;

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
    private static final int MHP_DEC = 3;

    private boolean hadTalk1 = false, hadTalk2 = false;
    private int turn = 1;
    private boolean ded;

    public Romeo() {this(0f,0f);}
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
        addMove(STAB, Intent.ATTACK, SMALL_DMG, AbstractDungeon.ascensionLevel>=17 ? 2 : 1, true);
        addMove(GRIEVOUS_WOUNDS, Intent.STRONG_DEBUFF);

        if(CardCrawlGame.isInARun())
            for(HashMap map : CardCrawlGame.metricData.event_choices) {
                if(map.getOrDefault("event_name", "").equals(MaskedBandits.ID) && map.get("player_choice").equals("Fought Bandits")) {
                    ded = true;
                    break;
                }
            }
    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        // we set the enemy type here so the calcAscensionMethods are called after the enemy type is set
        this.type = EnemyType.NORMAL;
    }

    @Override
    public void usePreBattleAction() {
        UC.doPow(this, this, new CounterPower(this, 7, calcAscensionDamage(SMALL_DMG)), false);
        if(ded) {
            UC.doPow(this, this, new IntangiblePlayerPower(this, 1), false);
        }
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
                if(!hadTalk1) {
                    UC.atb(new TalkAction(this, DIALOG[ded?2:0]));
                    hadTalk1 = true;
                }
                UC.doPow(this, UC.p(), new GrievousWoundsPower(UC.p(), calcAscensionSpecial(MHP_DEC)), false);
                break;
            case SIDE_SLASH:
                UC.atb(new ChangeStateAction(this, "SLASH"));
                UC.atb(new WaitAction(0.5f));
                UC.atb(new DamageAction(UC.p(), info, AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
                UC.atb(new GainBlockAction(this, BLK));
                break;
            case STAB:
                UC.atb(new ChangeStateAction(this, "STAB"));
                UC.atb(new WaitAction(0.3f));
                for (int i = 0; i < ((EnemyMoveInfo)ReflectionHacks.getPrivate(this, AbstractMonster.class, "move")).multiplier; i++) {
                    UC.atb(new DamageAction(UC.p(), info, MathUtils.randomBoolean()? AbstractGameAction.AttackEffect.SLASH_DIAGONAL : AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
                }
                break;
            case BUFF_SELF:
                if(!hadTalk2) {
                    UC.atb(new TalkAction(this, DIALOG[1]));
                    hadTalk2 = true;
                }
                UC.doPow(this, this, new StrengthPower(this, calcAscensionSpecial(2)), false);
        }

        addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(int i) {
        byte move = (byte) (turn++ % 4);
        EnemyMoveInfo info = this.moves.get(move);
        this.setMove(MOVES[move], move, info.intent, info.baseDamage, info.multiplier>0? turn/3+info.multiplier : 0, info.isMultiDamage);
    }

    public void changeState(String key) {
        if ("SLASH".equals(key)) {
            state.setAnimation(0, "Attack", false);
            state.addAnimation(0, "Idle", true, 0.0F);
        } else if ("STAB".equals(key)) {
            state.setTimeScale(1.2f);
            state.setAnimation(0, "Attack", false);
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

    private Color backupTintCol;
    private float particleTimer = 0f;
    private static final float PARTICLE_COOLDOWN = 0.15f;
    @Override
    public void render(SpriteBatch sb) {
        if(ded) {
            backupTintCol = tint.color.cpy();
            tint.color = oscillarator(tint.color);
            particleTimer -= UC.gt();
            if(!Settings.DISABLE_EFFECTS && particleTimer <= 0) {
                AbstractDungeon.effectList.add(new UncommonPotionParticleEffect(MathUtils.random(hb.x, hb.x+hb.width), MathUtils.random(hb.y, hb.y+hb.height)));
                particleTimer = PARTICLE_COOLDOWN;
            }
        }
        super.render(sb);
        if(ded) {
            tint.color = backupTintCol;
        }
    }

    private static float oscillatingTimer = 0.0f;
    private static float oscillatingFader = 0.0f;
    public static Color oscillarator(Color c) {
        oscillatingFader += Gdx.graphics.getRawDeltaTime();
        if (oscillatingFader > 0.66F) {
            oscillatingFader = 0.66F;
            oscillatingTimer += Gdx.graphics.getRawDeltaTime() * 1.5f;
        }
        Color col = c.cpy();
        col.a = (0.5F + (MathUtils.cos(oscillatingTimer) + 1.0F) / 3.0F) * oscillatingFader;
        return col;
    }
}