package BattleTowers.powers;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.DexterityPower;

import java.text.MessageFormat;

import static BattleTowers.BattleTowers.makeID;

public class GainDexterityPower extends AbstractPower {
  public static final String POWER_ID = makeID(GainDexterityPower.class.getSimpleName());
  private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
  public static final String NAME = powerStrings.NAME;
  public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;
  
  public GainDexterityPower(AbstractCreature owner, int newAmount) {
    this.name = NAME;
    this.ID = POWER_ID;
    this.owner = owner;
    this.amount = newAmount;
    this.type = AbstractPower.PowerType.DEBUFF;
    updateDescription();
    loadRegion("shackle");
  }
  
  public void updateDescription() {
    this.description = MessageFormat.format(DESCRIPTIONS[0], this.amount);
  }
  
  public void atEndOfTurn(boolean isPlayer) {
    flash();
    addToBot(new ApplyPowerAction(this.owner, this.owner, new DexterityPower(this.owner, this.amount)));
    addToBot(new RemoveSpecificPowerAction(this.owner, this.owner, this));
  }
}
