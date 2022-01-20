package BattleTowers.actions;

import BattleTowers.monsters.AspiringChampion;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.utility.ShowCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.powers.watcher.VigorPower;
import com.megacrit.cardcrawl.vfx.MegaSpeechBubble;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CopyCatAction extends AbstractGameAction {
    AspiringChampion owner;
    String msg;
    String msgNoAttacks;
    private boolean used;
    private float bubbleDuration;
    
    public CopyCatAction(AspiringChampion aspiringChampion, String msg, String msgNoAttacks)
    {
        this.owner = aspiringChampion;
        this.source = aspiringChampion;
        this.msg = msg;
        this.msgNoAttacks = msgNoAttacks;
        if (Settings.FAST_MODE) {
            this.duration = Settings.ACTION_DUR_MED;
        } else {
            this.duration = Settings.ACTION_DUR_LONG;
        }
    }
    
    @Override
    public void update() {
    if(!this.used) {
        this.used = true;
        List<AbstractCard> abstractCardList =
                AbstractDungeon.actionManager.cardsPlayedThisTurn.stream()
                        .filter(abstractCard -> abstractCard.type == AbstractCard.CardType.ATTACK).collect(Collectors.toList());
    
        if (abstractCardList.size() == 0) {
            this.addToTop(new ApplyPowerAction(owner, owner, new StrengthPower(owner, 4)));
            AbstractDungeon.effectList.add(new MegaSpeechBubble(this.source.hb.cX + this.source.dialogX, this.source.hb.cY + this.source.dialogY, 2.0f, this.msgNoAttacks, false));
        } else {
            CardGroup Attacks = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
            for (AbstractCard card : abstractCardList) {
                Attacks.addToTop(card);
            }
            AbstractCard card = Attacks.getRandomCard(AbstractDungeon.cardRandomRng);
            if (card.cost > 1) {
                owner.setPLAYED_2_COST(true);
            }
            AbstractDungeon.effectList.add(new ShowCardBrieflyEffect(card.makeStatEquivalentCopy()));
            this.addToTop(new ApplyPowerAction(owner, owner, new VigorPower(owner, card.baseDamage)));
            AbstractDungeon.effectList.add(new MegaSpeechBubble(this.source.hb.cX + this.source.dialogX, this.source.hb.cY + this.source.dialogY, 2.0f, this.msg, false));
        }
    }
        this.tickDuration();
    }
}
