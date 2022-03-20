package BattleTowers.actions;

import BattleTowers.util.UC;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

public class WishAction extends AbstractGameAction {
    private UUID uuid;
    private int deckPosition = -1;

    public WishAction(UUID targetUUID)
    {
        uuid = targetUUID;
        duration = Settings.ACTION_DUR_XFAST;
    }

    @Override
    public void update()
    {
        if (duration == Settings.ACTION_DUR_XFAST) {
            for (int i=0; i<AbstractDungeon.player.masterDeck.group.size(); ++i) {
                if (AbstractDungeon.player.masterDeck.group.get(i).uuid == uuid) {
                    deckPosition = i;
                    break;
                }
            }
            AbstractDungeon.player.masterDeck.group.removeIf(card -> card.uuid == uuid);

            CardGroup tmpGroup = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
            tmpGroup.group = (ArrayList<AbstractCard>) CardLibrary.getAllCards().stream()
                    .filter(c -> c.type != AbstractCard.CardType.STATUS
                            && c.type != AbstractCard.CardType.CURSE
                            && c.rarity != AbstractCard.CardRarity.SPECIAL
                            && c.rarity != AbstractCard.CardRarity.CURSE
                            && c.color == UC.p().getCardColor())
                    .collect(Collectors.toList());

            CardGroup group = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
            for (AbstractCard c : tmpGroup.group) {
                AbstractCard tmp = c.makeCopy();
                tmp.upgrade();
                group.addToTop(tmp);
            }
            group.sortAlphabetically(true);
            group.sortByRarity(false);
            group.sortByStatus(true);

            AbstractDungeon.gridSelectScreen.open(group, 1, "Choose a card to replace your Wish for Knowledge.", false, false, false, false);
            tickDuration();
            return;
        }

        if (AbstractDungeon.gridSelectScreen.selectedCards.size() != 0) {
            AbstractCard card = AbstractDungeon.gridSelectScreen.selectedCards.get(0);
            AbstractDungeon.gridSelectScreen.selectedCards.clear();

            AbstractCard realCard = card.makeStatEquivalentCopy();
            if (deckPosition >= 0) {
                AbstractDungeon.player.masterDeck.group.add(deckPosition, realCard);
            }
            AbstractDungeon.actionManager.addToBottom(new MakeTempCardInHandAction(realCard, false));
        }
        tickDuration();
    }
}
