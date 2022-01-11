package BattleTowers.powers;

import BattleTowers.BattleTowers;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import static BattleTowers.BattleTowers.makeID;

public class GreedWrathEnvyPower extends AbstractPower {
    public static final String POWER_ID = makeID(GreedWrathEnvyPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public GreedWrathEnvyPower(AbstractCreature owner) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.updateDescription();
        this.priority = 50;
        this.type = PowerType.BUFF;
        this.loadRegion("phantasmal");
    }

    @Override
    public void onInitialApplication() {
        int nonStarterRelics = (int)AbstractDungeon.player.relics.stream().filter(r -> r.tier != AbstractRelic.RelicTier.STARTER).count();
        int bossRelics = (int)AbstractDungeon.player.relics.stream().filter(r -> r.tier == AbstractRelic.RelicTier.BOSS).count();
        int rares = (int)AbstractDungeon.player.masterDeck.group.stream().filter(c -> c.rarity == AbstractCard.CardRarity.RARE).count();
        int uncommons = (int)AbstractDungeon.player.masterDeck.group.stream().filter(c -> c.rarity == AbstractCard.CardRarity.UNCOMMON).count();

        if (nonStarterRelics > 0) {
            this.addToBot(new ApplyPowerAction(this.owner, this.owner, new StrengthPower(this.owner, nonStarterRelics)));
        }
        if (bossRelics > 0 && AbstractDungeon.ascensionLevel >= 17) {
            this.addToBot(new ApplyPowerAction(this.owner, this.owner, new RitualPower(this.owner, bossRelics, false)));
        }
        if (rares > 0) {
            this.addToBot(new ApplyPowerAction(this.owner, this.owner, new ArtifactPower(this.owner, rares)));
        }
        if (uncommons > 0) {
            this.addToBot(new ApplyPowerAction(this.owner, this.owner, new MetallicizePower(this.owner, uncommons)));
        }
    }

    @Override
    public void updateDescription() {
        this.description = AbstractDungeon.ascensionLevel < 17 ? DESCRIPTIONS[0] : DESCRIPTIONS[1];
    }
}
