package BattleTowers.powers.abstracts;

import com.megacrit.cardcrawl.powers.AbstractPower;

public interface PowerRemovalNotifier {
    void onPowerRemoved(AbstractPower p);
}
