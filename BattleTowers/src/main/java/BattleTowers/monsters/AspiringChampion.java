package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import BattleTowers.actions.ChangeChampImageAction;
import BattleTowers.actions.CopyCatAction;
import BattleTowers.powers.ClunkyPower;
import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.TalkAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.status.Dazed;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.vfx.combat.GoldenSlashEffect;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class AspiringChampion extends AbstractBTMonster{
    
    public static final String ID = makeID(AspiringChampion.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;
    private static final int MAX_HEALTH = 225;
    private static final String IMG = BattleTowers.makeImagePath("monsters/AspiringChampion/AspiringChampion.png");
    private static final String IMG_FALLEN = BattleTowers.makeImagePath("monsters/AspiringChampion/AspiringChampionFallen.png");
    protected Texture imgFallen;
    protected Texture imgUp;
    private static final byte COPYCAT_A = 0;
    private static final byte COPYCAT_B = 1;
    private static final byte HEAVY_BLOWS = 2;
    private static final byte RECOVER = 3;
    private static final byte HEAVY_BLOWS_DEBUFFED = 4;
    private static final byte COPYCAT_B_DEBUFFED = 5;
    private static final byte COPYCAT_B_ON_2_COST = 6;
    private boolean PLAYED_2_COST;
    public void setPLAYED_2_COST(boolean PLAYED_2_COST) {
        this.PLAYED_2_COST = PLAYED_2_COST;
    }
    
    public AspiringChampion() {
        this(0.0f, 0.0f);
    }
    
    public AspiringChampion(float hb_x, float hb_y) {
        super(NAME, ID, MAX_HEALTH, hb_x, hb_y, 400.0F, 410.0F, IMG);
        addMove(COPYCAT_A, Intent.UNKNOWN);
        addMove(COPYCAT_B, Intent.ATTACK, calcAscensionDamage(0), (AbstractDungeon.ascensionLevel >= 18) ? 4 : 3);
        addMove(COPYCAT_B_ON_2_COST, Intent.ATTACK, calcAscensionDamage(0), (AbstractDungeon.ascensionLevel >= 18) ? 3 : 2);
        addMove(COPYCAT_B_DEBUFFED, Intent.ATTACK, calcAscensionDamage(0), 1);
        addMove(HEAVY_BLOWS, Intent.ATTACK, calcAscensionDamage(15), 2);
        addMove(HEAVY_BLOWS_DEBUFFED, Intent.ATTACK, calcAscensionDamage(15), 1);
        addMove(RECOVER, Intent.BUFF);
        imgFallen = ImageMaster.loadImage(IMG_FALLEN);
        imgUp = ImageMaster.loadImage(IMG);
    }
    
    public void usePreBattleAction() {
        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(this, this, new ClunkyPower(this)));
        if(AbstractDungeon.ascensionLevel >= 8)
        {
            AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(this, this, new MetallicizePower(this, 6)));
        }
    }
    
    @Override
    public void takeTurn() {
        float vfxSpeed = 0.1F;
        if (Settings.FAST_MODE) {
            vfxSpeed = 0.0F;
        }
        switch (this.nextMove) {
            case COPYCAT_A: {
                addToBot(new CopyCatAction(this, DIALOG[0], DIALOG[3]));
                break;
            }
            case COPYCAT_B: {
                useFastAttackAnimation();
                for(int i = 0; i <((AbstractDungeon.ascensionLevel >= 18) ? 4 : 3); i++)
                {
                    AbstractDungeon.actionManager.addToBottom(new VFXAction(new GoldenSlashEffect(AbstractDungeon.player.hb.cX - 60.0F * Settings.scale, AbstractDungeon.player.hb.cY, true), vfxSpeed));
                    AbstractDungeon.actionManager.addToBottom(new DamageAction(AbstractDungeon.player, this.damage.get(COPYCAT_B), AbstractGameAction.AttackEffect.NONE));
                }
                break;
            }
            case COPYCAT_B_ON_2_COST: {
                useFastAttackAnimation();
                for(int i = 0; i <((AbstractDungeon.ascensionLevel >= 18) ? 3 : 2); i++)
                {
                    AbstractDungeon.actionManager.addToBottom(new VFXAction(new GoldenSlashEffect(AbstractDungeon.player.hb.cX - 60.0F * Settings.scale, AbstractDungeon.player.hb.cY, true), vfxSpeed));
                    AbstractDungeon.actionManager.addToBottom(new DamageAction(AbstractDungeon.player, this.damage.get(COPYCAT_B), AbstractGameAction.AttackEffect.NONE));
                }
                break;
            }
            case COPYCAT_B_DEBUFFED: {
                useFastAttackAnimation();
                AbstractDungeon.actionManager.addToBottom(new DamageAction(AbstractDungeon.player, this.damage.get(COPYCAT_B), AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
                break;
            }
            case HEAVY_BLOWS: {
                useFastAttackAnimation();
                for (int i = 0; i < 2; i++) {
                    addToBot(new DamageAction(AbstractDungeon.player, this.damage.get(HEAVY_BLOWS), AbstractGameAction.AttackEffect.SLASH_VERTICAL));
                }
                AbstractDungeon.actionManager.addToBottom(new TalkAction(this, DIALOG[2]));
                addToBot(new ChangeChampImageAction(this));
                break;
            }
            case HEAVY_BLOWS_DEBUFFED: {
                useFastAttackAnimation();
                addToBot(new DamageAction(AbstractDungeon.player, this.damage.get(HEAVY_BLOWS), AbstractGameAction.AttackEffect.SLASH_VERTICAL));
                AbstractDungeon.actionManager.addToBottom(new TalkAction(this, DIALOG[2]));
                addToBot(new ChangeChampImageAction(this));
                break;
            }
            case RECOVER:
            {
                addToBot(new ApplyPowerAction(this, this, new StrengthPower(this, 2)));
            }
        }
    }
    public void swapImage()
    {
        if(img == imgFallen)
        {
            img = imgUp;
        }
        else
        {
            img = imgFallen;
        }
    }
    
    @Override
    protected void getMove(int i) {
        if(moveHistory.isEmpty())
        {
            ArrayList<Byte> possibilities = new ArrayList<>();
            possibilities.add(COPYCAT_A);
            possibilities.add(HEAVY_BLOWS);
            byte move = possibilities.get(AbstractDungeon.monsterRng.random(possibilities.size() - 1));
            setMoveShortcut(move, MOVES[move]);
        }
        else if(lastMove(COPYCAT_A))
        {
            if(!PLAYED_2_COST) {
                setMoveShortcut(COPYCAT_B, MOVES[COPYCAT_B]);
            }
            if(PLAYED_2_COST) {
                setMoveShortcut(COPYCAT_B, MOVES[COPYCAT_B_ON_2_COST]);
                PLAYED_2_COST = false;
            }
        }
        else if(lastMove(HEAVY_BLOWS) || lastMove(HEAVY_BLOWS_DEBUFFED))
        {
            setMoveShortcut(RECOVER);
        }
        else if(!(lastTwoMoves(HEAVY_BLOWS) || lastTwoMoves(HEAVY_BLOWS_DEBUFFED)))
        {
            setMoveShortcut(HEAVY_BLOWS, MOVES[HEAVY_BLOWS]);
        }
        else
        {
            setMoveShortcut(COPYCAT_A, MOVES[COPYCAT_A]);
        }
    }
}
