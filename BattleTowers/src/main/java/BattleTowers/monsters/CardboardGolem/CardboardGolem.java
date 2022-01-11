package BattleTowers.monsters.CardboardGolem;

import BattleTowers.monsters.AbstractBTMonster;
import BattleTowers.monsters.CardboardGolem.cards.BlankColorlessCard;
import BattleTowers.monsters.CardboardGolem.powers.CardEaterPower;
import BattleTowers.relics.CardboardHeart;
import BattleTowers.util.TextureLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.unique.VampireDamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.red.BodySlam;
import com.megacrit.cardcrawl.cards.status.Dazed;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.powers.WeakPower;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.vfx.combat.BiteEffect;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeImagePath;

public class CardboardGolem extends AbstractBTMonster
{
    public static final String ID = makeID(CardboardGolem.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;

    //name of the monster's moves
    private static final byte HEAD_BASH = 0;
    private static final byte CHOW_DOWN = 1;
    private static final byte SHRED_SKIN = 2;
    private static final byte SMITH_SELF = 3;

    //calcAscensionSpecial automatically scales the number of status cards based on ascension and enemy type
    private final int STATUS = calcAscensionSpecial(2);
    private final int STRENGTH = calcAscensionSpecial(3);
    private final int BLOCK = calcAscensionSpecial(10);

    private int turnTimer = 0;
    private int upgradeNumber = 0;

    //defaults enemy placement to 0, 0
    public CardboardGolem() {
        this(0.0f, 0.0f);
    }

    public CardboardGolem(final float x, final float y) {
        // maxHealth param doesn't matter, we will override it with setHP
        // hb_x and hb_y shifts the monster's AND its health bar's position around on the screen, usually you don't need to change these values
        // hb_w affects how wide the monster's health bar is. hb_h affects how far up the monster's intent image is. Adjust these values until they look good
        super(NAME, ID, 140, 0.0F, 0.0f, 200.0f, 220.0f, makeImagePath("monsters/CardboardGolem.png"), x, y);
        // HANDLE YOUR ANIMATION STUFF HERE
        // this.animation = Whatever your animation is

        // calcAscensionTankiness automatically scales HP based on ascension and enemy type
        // passing 2 values makes the game randomly select a value in between the ranges for the HP
        // if you pass only 1 value to set HP it will use that as the HP value
        setHp(calcAscensionTankiness(220), calcAscensionTankiness(240));

        // Add these moves to the move hashmap, we will be using them later in getMove
        // calc AscensionDamage automatically scales damage based on ascension and enemy type
        addMove(HEAD_BASH, Intent.ATTACK_DEBUFF, calcAscensionDamage(18));
        addMove(CHOW_DOWN, Intent.ATTACK_BUFF, calcAscensionDamage(14));
        addMove(SHRED_SKIN, Intent.DEFEND_DEBUFF);
        addMove(SMITH_SELF, Intent.BUFF);
    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        // we set the enemy type here so the calcAscensionMethods are called after the enemy type is set
        this.type = EnemyType.BOSS;
    }

    @Override
    public void usePreBattleAction() {
        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(this, this, new CardEaterPower(this, 10), 10));
        AbstractDungeon.getCurrRoom().rewards.add(new RewardItem(new CardboardHeart()));
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
            case HEAD_BASH: {
                useFastAttackAnimation();
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_LIGHT));
                addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new WeakPower(AbstractDungeon.player, 1, true), 1));
                break;
            }
            case CHOW_DOWN: {
                AbstractDungeon.actionManager.addToBottom(new VFXAction(new BiteEffect(AbstractDungeon.player.hb.cX + MathUtils.random(-25.0F, 25.0F) * Settings.scale, AbstractDungeon.player.hb.cY + MathUtils.random(-25.0F, 25.0F) * Settings.scale, Color.GOLD.cpy()), 0.0F));
                addToBot(new VampireDamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.NONE));
                break;
            }
            case SHRED_SKIN: {
                addToBot(new GainBlockAction(this, BLOCK));
                addToBot(new MakeTempCardInDrawPileAction(new BlankColorlessCard(), STATUS, true, false));
                break;
            }
            case SMITH_SELF: {
                addToBot(new ApplyPowerAction(this, this, new VulnerablePower(this, 1, false), 1));
                addToBot(new ApplyPowerAction(this, this, new StrengthPower(this, STRENGTH), STRENGTH));
                upgradeNumber += 1;
                if (!this.name.contains("+")) {
                    this.name = this.name + "+";
                }
                else {
                    this.name = monsterStrings.NAME + "+" + Integer.toString(upgradeNumber);
                }
            }
        }
        addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(final int num) {
        //This is where we determine what move the monster should do next
        //Here, we add the possibilities to a list and randomly choose one with each possibility having equal weight
        ArrayList<Byte> possibilities = new ArrayList<>();

        possibilities.add(HEAD_BASH);
        possibilities.add(CHOW_DOWN);
        possibilities.add(SHRED_SKIN);
        possibilities.add(HEAD_BASH);
        possibilities.add(SMITH_SELF);

        setMoveShortcut(possibilities.get(turnTimer), MOVES[possibilities.get(turnTimer)]);
        turnTimer += 1;
        if (turnTimer > possibilities.size() - 1) {
            turnTimer = 0;
        }
    }
}