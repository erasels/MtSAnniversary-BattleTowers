package BattleTowers.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.AbstractGameAction.ActionType;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.utility.ShowCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.AbstractCard.CardRarity;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.StasisPower;

import java.util.ArrayList;

public class PrismStasisAction extends AbstractGameAction {
    private AbstractCreature owner;
    private float startingDuration;
    private AbstractCard card = null;
    private ArrayList<AbstractCard> rareStasisMatrix = new ArrayList<>();
    private ArrayList<AbstractCard> uncommonStasisMatrix = new ArrayList<>();
    private ArrayList<AbstractCard> commonStasisMatrix = new ArrayList<>();
    private ArrayList<AbstractCard> specialStasisMatrix = new ArrayList<>();
    private ArrayList<AbstractCard> powerMatrix = new ArrayList<>();

    public PrismStasisAction(AbstractCreature owner) {
        this.owner = owner;
        this.duration = Settings.ACTION_DUR_LONG;
        this.startingDuration = Settings.ACTION_DUR_LONG;
        this.actionType = ActionType.WAIT;
    }

    private AbstractCard getRandomCardPrioritizePowers(CardGroup g){

        //seperate pile into rares, uncommons and commons, search for powers to see if any exist
        boolean powersExist = false;
        for (AbstractCard c : g.group) {
            if (c.type.equals(AbstractCard.CardType.POWER)){ powersExist = true; }
            switch (c.rarity){
                case RARE:
                    rareStasisMatrix.add(c);
                    break;
                case UNCOMMON:
                    uncommonStasisMatrix.add(c);
                    break;
                case COMMON:
                    commonStasisMatrix.add(c);
                case SPECIAL:
                    specialStasisMatrix.add(c);
                    break;
            }
        }

        //if no powers, abort
        if (!powersExist){ return null; }

        //add all rare powers
        for (AbstractCard searchCard : rareStasisMatrix){
            if (searchCard.type.equals(AbstractCard.CardType.POWER)) {
                powerMatrix.add(searchCard);
            }
        }

        //gets a random rare power
        if (!powerMatrix.isEmpty()){ return powerMatrix.get((int)(Math.random()*powerMatrix.size())); }

        //add all uncommon powers if no rare powers are found
        for (AbstractCard searchCard : uncommonStasisMatrix){
            if (searchCard.type.equals(AbstractCard.CardType.POWER)) {
                powerMatrix.add(searchCard);
            }
        }

        //gets a random uncommon power
        if (!powerMatrix.isEmpty()){ return powerMatrix.get((int)(Math.random()*powerMatrix.size())); }

        //add all common powers if no rare powers are found
        for (AbstractCard searchCard : commonStasisMatrix){
            if (searchCard.type.equals(AbstractCard.CardType.POWER)) {
                powerMatrix.add(searchCard);
            }
        }

        //gets a random common power
        if (!powerMatrix.isEmpty()){ return powerMatrix.get((int)(Math.random()*powerMatrix.size())); }

        //add all special powers if no rare powers are found
        for (AbstractCard searchCard : specialStasisMatrix){
            if (searchCard.type.equals(AbstractCard.CardType.POWER)) {
                powerMatrix.add(searchCard);
            }
        }

        //gets a random special power
        if (!powerMatrix.isEmpty()){ return powerMatrix.get((int)(Math.random()*powerMatrix.size())); }

        //else, return nothing.
        return null;
    }

    public void update() {
        if (AbstractDungeon.player.drawPile.isEmpty() && AbstractDungeon.player.discardPile.isEmpty()) {
            this.isDone = true;
        } else {
            if (this.duration == this.startingDuration) {
                if (AbstractDungeon.player.drawPile.isEmpty()) {
                    this.card = getRandomCardPrioritizePowers(AbstractDungeon.player.discardPile);
                    if (this.card == null) {
                        this.card = AbstractDungeon.player.discardPile.getRandomCard(AbstractDungeon.cardRandomRng, CardRarity.RARE);
                        if (this.card == null) {
                            this.card = AbstractDungeon.player.discardPile.getRandomCard(AbstractDungeon.cardRandomRng, CardRarity.UNCOMMON);
                            if (this.card == null) {
                                this.card = AbstractDungeon.player.discardPile.getRandomCard(AbstractDungeon.cardRandomRng, CardRarity.COMMON);
                                if (this.card == null) {
                                    this.card = AbstractDungeon.player.discardPile.getRandomCard(AbstractDungeon.cardRandomRng);
                                }
                            }
                        }
                    }

                    AbstractDungeon.player.discardPile.removeCard(this.card);
                } else {
                    this.card = getRandomCardPrioritizePowers(AbstractDungeon.player.drawPile);
                    if (this.card == null) {
                        this.card = AbstractDungeon.player.drawPile.getRandomCard(AbstractDungeon.cardRandomRng, CardRarity.RARE);
                        if (this.card == null) {
                            this.card = AbstractDungeon.player.drawPile.getRandomCard(AbstractDungeon.cardRandomRng, CardRarity.UNCOMMON);
                            if (this.card == null) {
                                this.card = AbstractDungeon.player.drawPile.getRandomCard(AbstractDungeon.cardRandomRng, CardRarity.COMMON);
                                if (this.card == null) {
                                    this.card = AbstractDungeon.player.drawPile.getRandomCard(AbstractDungeon.cardRandomRng);
                                }
                            }
                        }
                    }
                    AbstractDungeon.player.drawPile.removeCard(this.card);
                }

                AbstractDungeon.player.limbo.addToBottom(this.card);
                this.card.setAngle(0.0F);
                this.card.targetDrawScale = 0.75F;
                this.card.target_x = (float)Settings.WIDTH / 2.0F;
                this.card.target_y = (float)Settings.HEIGHT / 2.0F;
                this.card.lighten(false);
                this.card.unfadeOut();
                this.card.unhover();
                this.card.untip();
                this.card.stopGlowing();
            }

            this.tickDuration();
            if (this.isDone && this.card != null) {
                this.addToTop(new ShowCardAction(this.card));
            }

        }
    }
}

