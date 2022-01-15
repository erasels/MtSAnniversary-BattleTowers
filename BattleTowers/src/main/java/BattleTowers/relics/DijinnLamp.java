package BattleTowers.relics;

import BattleTowers.cards.Granted;
import BattleTowers.util.TextureLoader;
import basemod.abstracts.CustomRelic;
import basemod.helpers.CardPowerTip;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.cards.AbstractCard;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class DijinnLamp extends CustomRelic {
    public static final String ID = makeID(DijinnLamp.class.getSimpleName());

    private final AbstractCard wish = new Granted();

    public DijinnLamp() {
        super(ID, TextureLoader.getTexture(makeRelicPath("DijinnLamp.png")), RelicTier.SPECIAL, LandingSound.MAGICAL);
        description = getUpdatedDescription();
        tips.add(new CardPowerTip(wish.makeStatEquivalentCopy()));
    }

    @Override
    public void atBattleStart() {
        addToBot(new MakeTempCardInDrawPileAction(wish.makeStatEquivalentCopy(), 1, true, true));
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }
}
