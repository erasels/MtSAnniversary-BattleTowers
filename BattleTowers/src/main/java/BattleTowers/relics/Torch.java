package BattleTowers.relics;

import BattleTowers.room.BattleTowerRoom;
import BattleTowers.util.TextureLoader;
import basemod.abstracts.CustomRelic;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.powers.DexterityPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class Torch extends CustomRelic {
    public static final String ID = makeID(Torch.class.getSimpleName());
    private static RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID);

    private int powerLevel = 1;

    public Torch() {
        super(ID, TextureLoader.getTexture(makeRelicPath("torch.png")), TextureLoader.getTexture(makeRelicPath("torch_outline.png")), RelicTier.SPECIAL, LandingSound.FLAT);
        description = getUpdatedDescription();
    }

    public Torch(int powerLevel) {
        this();
        this.powerLevel = powerLevel;
        description = getUpdatedDescription();
    }

    @Override
    public String getUpdatedDescription() {
        if(!CardCrawlGame.isInARun())  {
            return DESCRIPTIONS[5];
        }
        if(AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom() instanceof BattleTowerRoom) {
            if(powerLevel > 0) {
                return DESCRIPTIONS[0] + powerLevel + DESCRIPTIONS[1];
            } else {
                return DESCRIPTIONS[2];
            }
        } else if(powerLevel > 0) {
            return DESCRIPTIONS[3];
        } else {
            return DESCRIPTIONS[4];
        }
    }

    @Override
    public void onEnterRoom(AbstractRoom room) {
        if(!(room instanceof BattleTowerRoom)) {
            setTextureOutline(TextureLoader.getTexture(makeRelicPath("unlitTorch.png")), TextureLoader.getTexture(makeRelicPath("unlitTorch_outline.png")));
            description = getUpdatedDescription();
        }
    }

    @Override
    public void atBattleStart() {
        AbstractPlayer p = AbstractDungeon.player;
        if(powerLevel > 0 && !(AbstractDungeon.getCurrRoom() instanceof BattleTowerRoom)) {
            flash();
            addToTop(MathUtils.randomBoolean() ?
                    new ApplyPowerAction(p, p, new DexterityPower(AbstractDungeon.player, 1), 1) :
                    new ApplyPowerAction(p, p, new StrengthPower(AbstractDungeon.player, 1), 1));
            addToTop(new RelicAboveCreatureAction(p, this));
        }
    }
}
