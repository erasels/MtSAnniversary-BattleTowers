package BattleTowers.blights;

import BattleTowers.BattleTowers;
import BattleTowers.util.UC;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StoreRelic;

import java.util.ArrayList;

public class GreedBlight extends AbstractBTBlight{
    public static final String ID = BattleTowers.makeID(GreedBlight.class.getSimpleName());
    private static final float PRICE_MULTI = 2f;

    public GreedBlight() {
        super(ID, GreedBlight.class.getSimpleName());
    }

    @SpirePatch2(clz = ShopScreen.class, method = "initRelics")
    public static class PriceChangePatch {
        @SpirePostfixPatch
        public static void patch(ArrayList<StoreRelic> ___relics) {
            GreedBlight gb = (GreedBlight) UC.p().getBlight(GreedBlight.ID);
            if(gb != null) {
                for(StoreRelic r : ___relics) {
                    r.price *= PRICE_MULTI;
                }
                gb.flash();
            }
        }
    }

    @SpirePatch2(clz = ShopScreen.class, method = "getNewPrice", paramtypez = {StoreRelic.class})
    public static class CourierFix {
        @SpirePostfixPatch
        public static void patch(StoreRelic r) {
            GreedBlight gb = (GreedBlight) UC.p().getBlight(GreedBlight.ID);
            if(gb != null) {
                r.price *= PRICE_MULTI;
                gb.flash();
            }
        }
    }

    @Override
    public void updateDescription() {
        description = String.format(STRINGS.DESCRIPTIONS[0], (int)PRICE_MULTI);
    }
}