package BattleTowers.relics;

import BattleTowers.room.BattleTowerRoom;
import BattleTowers.util.UC;
import basemod.abstracts.CustomRelic;
import basemod.abstracts.CustomSavable;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardSave;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import static BattleTowers.BattleTowers.makeID;

public class Torch extends CustomRelic implements CustomSavable<CardSave> {
    public static final String ID = makeID(Torch.class.getSimpleName());
    private static RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID);

    public AbstractCard card = null;

    public Torch() {
        super(ID, UC.getTexture("relics", "Torch"), UC.getTexture("relics", "Torch_Outline"), RelicTier.SPECIAL, LandingSound.FLAT);
    }

    public Torch(AbstractCard card) {
        this();
        this.card = card;
        resetDescriptionAndTooltip();
    }

    @Override
    public void onEnterRoom(AbstractRoom room) {
        if(AbstractDungeon.getCurrRoom() instanceof BattleTowerRoom) {
            setTextureOutline(UC.getTexture("relics", "Torch"), UC.getTexture("relics", "Torch_Outline"));
        } else {
            setTextureOutline(UC.getTexture("relics", "UnlitTorch"), UC.getTexture("relics", "UnlitTorch_Outline"));
        }
        resetDescriptionAndTooltip();
    }

    @Override
    public void atBattleStart() {
        if(card != null) {
            flash();
            UC.atb(new RelicAboveCreatureAction(UC.p(), this));
            if (AbstractDungeon.getCurrRoom() instanceof BattleTowerRoom) {
                UC.atb(new MakeTempCardInDrawPileAction(card.makeCopy(), 2, true, true));
            } else {
                UC.atb(new MakeTempCardInDiscardAction(card.makeCopy(), 2));
            }
            resetDescriptionAndTooltip(); //Do this again here in case of save and reload in combat
        } else {
            System.err.print("Err: Torch card is null\n");
        }
    }

    @Override
    public String getUpdatedDescription() {
        if(!CardCrawlGame.isInARun() || card == null)  {
            this.flavorText = relicStrings.FLAVOR;
            return DESCRIPTIONS[4];
        }
        if(AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom() instanceof BattleTowerRoom) {
            this.flavorText = relicStrings.FLAVOR;
            return DESCRIPTIONS[0] + FontHelper.colorString(card.name, "y") + DESCRIPTIONS[1];
        } else {
            this.flavorText = DESCRIPTIONS[5];
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
        if(card != null) {
            return new CardSave(card.cardID, card.timesUpgraded, card.misc);
        }
        return null;
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
