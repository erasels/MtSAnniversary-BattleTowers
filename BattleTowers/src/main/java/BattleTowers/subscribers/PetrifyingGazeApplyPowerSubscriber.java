package BattleTowers.subscribers;

import BattleTowers.powers.PetrifyingGazePower;
import basemod.interfaces.PostPowerApplySubscriber;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;

public class PetrifyingGazeApplyPowerSubscriber implements PostPowerApplySubscriber {
    @Override
    public void receivePostPowerApplySubscriber(AbstractPower power, AbstractCreature target, AbstractCreature source) {
        PetrifyingGazePower p = (PetrifyingGazePower)target.getPower(PetrifyingGazePower.POWER_ID);
        if (p != null) {
            p.onPowerApplied(power, source);
        }
    }
}
