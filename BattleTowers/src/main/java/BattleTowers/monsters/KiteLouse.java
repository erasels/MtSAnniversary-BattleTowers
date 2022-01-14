package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import BattleTowers.powers.KiteFlightPower;
import BattleTowers.powers.abstracts.PowerRemovalNotifier;
import BattleTowers.util.TextureLoader;
import BattleTowers.util.UC;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.AnimateSlowAttackAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

import static BattleTowers.BattleTowers.makeID;

public class KiteLouse extends AbstractBTMonster implements PowerRemovalNotifier {
    public static final String ID = makeID(KiteLouse.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;

    private boolean isOpen = true;
    private Texture kite = TextureLoader.getTexture(BattleTowers.makeMonsterPath("/KiteLouse/kite.png"));

    //name of the monster's moves
    private static final byte BASIC_ATTACK = 0;
    private static final byte BASIC_BLOCK = 1;
    private static final byte STRONG_ATTACK = 2;
    private static final byte DEATH_RATTLE = 3;
    private static final String CLOSED_STATE = "CLOSED";
    private static final String OPEN_STATE = "OPEN";
    private static final String REAR_IDLE = "REAR_IDLE";
    private static final String REAR = "REAR";

    //Monster stats
    private static final int MIN_HP = 35;
    private static final int MAX_HP = 40;
    private static final int ATK_DMG = 7;
    private static final int DEF_BLK = 6;
    private static final int HEAVYATK_DMG = 16;
    private static final int SELF_DMG = 6;
    private static final int FLIGHT_AMT = 7;
    private final int BLK_AMOUNT = AbstractDungeon.monsterHpRng.random(calcAscensionSpecial(DEF_BLK));


    public KiteLouse(final float x, final float y, boolean offsetAction) {
        super(NAME, ID, 1, 0.0F, 0.0f, 200.0f, 220.0f, null, x, y);
        if(AbstractDungeon.miscRng.randomBoolean()) {
            loadAnimation("images/monsters/theBottom/louseRed/skeleton.atlas", "images/monsters/theBottom/louseRed/skeleton.json", 1.0F);
        } else {
            loadAnimation("images/monsters/theBottom/louseGreen/skeleton.atlas", "images/monsters/theBottom/louseGreen/skeleton.json", 1.0F);
        }
        AnimationState.TrackEntry e = this.state.setAnimation(0, "idle", true);
        state.setTimeScale(2f);
        e.setTime(e.getEndTime() * MathUtils.random());
        setHp(calcAscensionTankiness(MIN_HP), calcAscensionTankiness(MAX_HP));
        addMove(BASIC_ATTACK, Intent.ATTACK, calcAscensionDamage(ATK_DMG));
        addMove(BASIC_BLOCK, Intent.DEFEND);
        addMove(STRONG_ATTACK, Intent.ATTACK, calcAscensionDamage(HEAVYATK_DMG));

        if(offsetAction) {
            moveHistory.add(BASIC_ATTACK);
        }
    }

    public void usePreBattleAction() {
        UC.doPow(this, new KiteFlightPower(this, AbstractDungeon.ascensionLevel >= 17 ? FLIGHT_AMT - 1 : FLIGHT_AMT));
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

        if(info.base > -1) {
            info.applyPowers(this, AbstractDungeon.player);
        }

        //carries out actions based on the current move
        //useFastAttackAnimation causes the monster to jump forward when it attacks
        switch (this.nextMove) {
            case BASIC_ATTACK: {
                if (!this.isOpen) {
                    addToBot(new ChangeStateAction(this, OPEN_STATE));
                    addToBot(new WaitAction(0.5F));
                }
                addToBot(new AnimateSlowAttackAction(this));
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_LIGHT));
                break;
            }
            case BASIC_BLOCK: {
                if (!this.isOpen) {
                    addToBot(new ChangeStateAction(this, REAR));
                    addToBot(new WaitAction(1.2F));
                } else {
                    addToBot(new ChangeStateAction(this, REAR_IDLE));
                    addToBot(new WaitAction(0.9F));
                }
                UC.atb(new GainBlockAction(this, this, BLK_AMOUNT));
                break;
            }
            case STRONG_ATTACK: {
                if (!this.isOpen) {
                    addToBot(new ChangeStateAction(this, OPEN_STATE));
                    addToBot(new WaitAction(0.5F));
                }
                addToBot(new AnimateSlowAttackAction(this));
                UC.atb(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
                UC.atb(new DamageAction(this, new DamageInfo(this, SELF_DMG, DamageInfo.DamageType.THORNS), AbstractGameAction.AttackEffect.BLUNT_LIGHT));
            }
        }
        addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(int i) {
        if(!moveHistory.isEmpty()) {
            switch (moveHistory.get(moveHistory.size()-1)) {
                case BASIC_ATTACK:
                case STRONG_ATTACK:
                    setMoveShortcut(BASIC_BLOCK, MOVES[BASIC_BLOCK]);
                    break;
                case BASIC_BLOCK:
                    if(moveHistory.size() > 2 && moveHistory.get(moveHistory.size() - 2) == BASIC_ATTACK) {
                        setMoveShortcut(STRONG_ATTACK, MOVES[STRONG_ATTACK]);
                    } else {
                        setMoveShortcut(BASIC_ATTACK, MOVES[BASIC_ATTACK]);
                    }
            }
        } else {
            setMoveShortcut(BASIC_ATTACK, MOVES[BASIC_ATTACK]);
        }
    }

    public void changeState(String stateName) {
        switch (stateName) {
            case CLOSED_STATE:
                this.state.setAnimation(0, "transitiontoclosed", false);
                this.state.addAnimation(0, "idle closed", true, 0.0F);
                this.isOpen = false;
                break;
            case OPEN_STATE:
                this.state.setAnimation(0, "transitiontoopened", false);
                this.state.addAnimation(0, "idle", true, 0.0F);
                this.isOpen = true;
                break;
            case REAR_IDLE:
                this.state.setAnimation(0, "rear", false);
                this.state.addAnimation(0, "idle", true, 0.0F);
                this.isOpen = true;
                break;
            default:
                this.state.setAnimation(0, "transitiontoopened", false);
                this.state.addAnimation(0, "rear", false, 0.0F);
                this.state.addAnimation(0, "idle", true, 0.0F);
                this.isOpen = true;
                break;
        }
    }

    @Override
    public void die() {
        super.die();

    }

    @Override
    public void onPowerRemoved(AbstractPower p) {
        UC.atb(new DamageAction(UC.p(), new DamageInfo(this, currentHealth, DamageInfo.DamageType.THORNS), AbstractGameAction.AttackEffect.BLUNT_HEAVY));
        UC.atb(new InstantKillAction(this));
    }

    @Override
    public void render(SpriteBatch sb) {
        super.render(sb);
        sb.draw(kite, skeleton.findBone("seg2").getWorldX(), skeleton.findBone("seg2").getWorldY(), kite.getWidth(), kite.getHeight());
    }
}
