package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import BattleTowers.actions.ChangeChampionImageAndDoDialogueAction;
import BattleTowers.actions.CopyCatAction;
import BattleTowers.powers.ClunkyPower;
import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.TalkAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.powers.MetallicizePower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.vfx.StarBounceEffect;
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
    private int nextMoveToDo;
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
        addToBot(new ApplyPowerAction(this, this, new ClunkyPower(this)));
        if(AbstractDungeon.ascensionLevel >= 8)
        {
            addToBot(new ApplyPowerAction(this, this, new MetallicizePower(this, 6)));
        }
    }
    
    @Override
    public void takeTurn() {
        DamageInfo info = new DamageInfo(this, this.moves.get(nextMove).baseDamage, DamageInfo.DamageType.NORMAL);
        int multiplier = this.moves.get(nextMove).multiplier;
        if(info.base > -1) {
            info.applyPowers(this, AbstractDungeon.player);
        }
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
                addToBot(new TalkAction(this, DIALOG[1]));
                for(int i = 0; i <((AbstractDungeon.ascensionLevel >= 18) ? 4 : 3); i++)
                {
                    addToBot(new VFXAction(new GoldenSlashEffect(AbstractDungeon.player.hb.cX - 60.0F * Settings.scale, AbstractDungeon.player.hb.cY, true), vfxSpeed));
                    addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.NONE));
                }
                break;
            }
            case COPYCAT_B_ON_2_COST: {
                useFastAttackAnimation();
                addToBot(new TalkAction(this, DIALOG[1]));
    
                for(int i = 0; i <((AbstractDungeon.ascensionLevel >= 18) ? 3 : 2); i++)
                {
                    addToBot(new VFXAction(new GoldenSlashEffect(AbstractDungeon.player.hb.cX - 60.0F * Settings.scale, AbstractDungeon.player.hb.cY, true), vfxSpeed));
                    addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.NONE));
                }
                break;
            }
            case COPYCAT_B_DEBUFFED: {
                useFastAttackAnimation();
                addToBot(new TalkAction(this, DIALOG[1]));
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
                break;
            }
            case HEAVY_BLOWS: {
                useFastAttackAnimation();
                for (int i = 0; i < 2; i++) {
                    addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.SLASH_VERTICAL));
                }
                addToBot(new ChangeChampionImageAndDoDialogueAction(this, DIALOG[2]));
                addToBot(new VFXAction(new StarBounceEffect(this.hb.cX, this.hb.cY)));
                addToBot(new VFXAction(new StarBounceEffect(this.hb.cX, this.hb.cY)));
                addToBot(new VFXAction(new StarBounceEffect(this.hb.cX, this.hb.cY)));
                break;
            }
            case HEAVY_BLOWS_DEBUFFED: {
                useFastAttackAnimation();
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.SLASH_VERTICAL));
                addToBot(new ChangeChampionImageAndDoDialogueAction(this, DIALOG[2]));
                addToBot(new VFXAction(new StarBounceEffect(this.hb.cX, this.hb.cY)));
                addToBot(new VFXAction(new StarBounceEffect(this.hb.cX, this.hb.cY)));
                addToBot(new VFXAction(new StarBounceEffect(this.hb.cX, this.hb.cY)));
                break;
            }
            case RECOVER:
            {
                addToBot(new ApplyPowerAction(this, this, new StrengthPower(this, 2)));
                addToBot(new ChangeChampionImageAndDoDialogueAction(this, DIALOG[4]));
            }
        }
        addToBot(new RollMoveAction(this));
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
            if(move == COPYCAT_A)
            {
                nextMoveToDo = HEAVY_BLOWS;
            }
            else
            {
                nextMoveToDo = COPYCAT_A;
            }
        }
        else if(lastMove(COPYCAT_A))
        {
            if(!PLAYED_2_COST) {
                setMoveShortcut(COPYCAT_B, MOVES[COPYCAT_B]);
            }
            if(PLAYED_2_COST) {
                setMoveShortcut(COPYCAT_B_ON_2_COST, MOVES[COPYCAT_B]);
                PLAYED_2_COST = false;
            }
        }
        else if(lastMove(HEAVY_BLOWS) || lastMove(HEAVY_BLOWS_DEBUFFED))
        {
            setMoveShortcut(RECOVER, MOVES[RECOVER]);
        }
        else if(nextMoveToDo == HEAVY_BLOWS)
        {
            setMoveShortcut(HEAVY_BLOWS, MOVES[HEAVY_BLOWS]);
            nextMoveToDo = COPYCAT_A;
        }
        else
        {
            setMoveShortcut(COPYCAT_A, MOVES[COPYCAT_A]);
            nextMoveToDo = HEAVY_BLOWS;
        }
    }
    
    public void debuffAttack()
    {
        if(this.nextMove == COPYCAT_B || this.nextMove == COPYCAT_B_ON_2_COST)
        {
            setMoveShortcut(COPYCAT_B_DEBUFFED, MOVES[COPYCAT_B]);
        }
        if(this.nextMove == HEAVY_BLOWS)
        {
            setMoveShortcut(HEAVY_BLOWS_DEBUFFED, MOVES[HEAVY_BLOWS]);
        }
        this.createIntent();
    }
}
