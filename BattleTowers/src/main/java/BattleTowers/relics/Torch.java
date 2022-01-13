package BattleTowers.relics;

import BattleTowers.room.BattleTowerRoom;
import BattleTowers.util.TextureLoader;
import BattleTowers.util.UC;
import basemod.abstracts.CustomRelic;
import basemod.abstracts.CustomSavable;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardSave;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.powers.DexterityPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class Torch extends CustomRelic implements CustomSavable<CardSave> {
    public static final String ID = makeID(Torch.class.getSimpleName());
    private static RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID);

    public AbstractCard card = null;

    public Torch() {
        super(ID, UC.getTexture("relics", "torch"), UC.getTexture("relics", "torch_outline"), RelicTier.SPECIAL, LandingSound.FLAT);
    }

    public Torch(AbstractCard card) {
        this();
        this.card = card;
        resetDescriptionAndTooltip();
    }

    @Override
    public void onEnterRoom(AbstractRoom room) {
        if(AbstractDungeon.getCurrRoom() instanceof BattleTowerRoom) {
            setTextureOutline(UC.getTexture("relics", "torch"), UC.getTexture("relics", "torch_outline"));
        } else {
            setTextureOutline(UC.getTexture("relics", "unlitTorch"), UC.getTexture("relics", "unlitTorch_outline"));
        }
        resetDescriptionAndTooltip();
    }

    @Override
    public void atBattleStart() {
        flash();
        UC.atb(new RelicAboveCreatureAction(UC.p(), this));
        if(AbstractDungeon.getCurrRoom() instanceof BattleTowerRoom) {
            UC.atb(new MakeTempCardInDrawPileAction(card.makeCopy(), 2, true, true));
        } else {
            UC.atb(new MakeTempCardInDiscardAction(card.makeCopy(), 2));
        }
        resetDescriptionAndTooltip();
    }

    @Override
    public String getUpdatedDescription() {
        if(!CardCrawlGame.isInARun() || card == null)  {
            return DESCRIPTIONS[4];
        }
        if(AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom() instanceof BattleTowerRoom) {
            return DESCRIPTIONS[0] + FontHelper.colorString(card.name, "y") + DESCRIPTIONS[1];
        } else {
            return DESCRIPTIONS[2] + FontHelper.colorString(card.name, "y") + DESCRIPTIONS[3];
        }
    }

    private void resetDescriptionAndTooltip() {
        description = getUpdatedDescription();
        tips.clear();
        tips.add(new PowerTip(name, description));
        initializeTips();
    }

    @Override
    public CardSave onSave() {
        return new CardSave(card.cardID, card.timesUpgraded, card.misc);
    }

    @Override
    public void onLoad(CardSave cardSave) {
        if(cardSave != null) {
            AbstractCard savedCard = CardLibrary.getCard(cardSave.id);
            savedCard.timesUpgraded = cardSave.upgrades;
            savedCard.misc = cardSave.misc;

            this.card = savedCard;
        }
    }
}
