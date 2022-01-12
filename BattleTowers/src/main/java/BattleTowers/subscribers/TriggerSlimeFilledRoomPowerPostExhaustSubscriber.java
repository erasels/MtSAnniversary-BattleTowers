package BattleTowers.subscribers;

import BattleTowers.powers.SlimeFilledRoomPower;
import basemod.interfaces.PostExhaustSubscriber;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class TriggerSlimeFilledRoomPowerPostExhaustSubscriber implements PostExhaustSubscriber {
    @Override
    public void receivePostExhaust(AbstractCard card) {
        for (AbstractMonster m : AbstractDungeon.getCurrRoom().monsters.monsters) {
            if (!m.isDying) {
                SlimeFilledRoomPower power = (SlimeFilledRoomPower)m.getPower(SlimeFilledRoomPower.POWER_ID);
                if (power != null) {
                    power.onPlayerExhaust(card);
                }
            }
        }
    }
}
