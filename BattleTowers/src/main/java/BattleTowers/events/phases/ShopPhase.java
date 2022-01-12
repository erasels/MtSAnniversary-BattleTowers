package BattleTowers.events.phases;

import BattleTowers.BattleTowers;
import BattleTowers.events.PhasedEvent;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.TheEnding;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.ShopRoom;
import com.megacrit.cardcrawl.shop.Merchant;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ShopPhase extends EventPhase {
    public Merchant merchant;
    private ShopRoom dummy;
    private float proceedButtonDelay = 0.6f;

    private PhasedEvent event;
    private Object followupKey;

    public ShopPhase() {
        merchant = null;
    }

    public EventPhase setNextKey(Object key) {
        this.followupKey = key;
        return this;
    }

    @Override
    public void transition(PhasedEvent event) {
        this.event = event;
        AbstractDungeon.rs = AbstractDungeon.RenderScene.NORMAL;
        AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.INCOMPLETE;

        event.setCardRarity(37, 9);
        event.allowRarityAltering = false;

        if (followupKey == null) {
            AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
        }
        if (!AbstractDungeon.id.equals(TheEnding.ID)) {
            AbstractDungeon.getCurrRoom().playBGM("SHOP");
        }

        AbstractDungeon.overlayMenu.proceedButton.setLabel(ShopRoom.TEXT[0]);
        this.merchant = new Merchant();
        dummy = new ShopRoom();
        dummy.setMerchant(merchant);
        proceedButtonDelay = 0.6f;
    }

    @Override
    public void hide(PhasedEvent event) {
        if (dummy != null) {
            dummy.dispose();
            dummy = null;
        }
        merchant = null;
    }

    public boolean finish() { //see ProceedButtonEventPatches
        if (event != null && followupKey != null) {
            event.transitionKey(followupKey);
            return false;
        }
        else {
            //Should already be complete in this case, but just in case.
            AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
            return true;
        }
    }

    private static Method updatePurge;
    static {
        try {
            updatePurge = ShopRoom.class.getDeclaredMethod("updatePurge");
            updatePurge.setAccessible(true);
        } catch (NoSuchMethodException e) {
            BattleTowers.logger.error("Failed to access method \"updatePurge\" of ShopRoom.");
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        if (this.merchant != null) {
            this.merchant.update();
        }

        if (dummy != null) {
            try {
                //This is done so that patches that affect the shop room's removal option should hopefully be more compatible.
                updatePurge.invoke(dummy);
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        //Normally, AbstractRoom constantly calls ProceedButton.show if the room is complete and has no event (ShopRoom)
        if (!AbstractDungeon.isScreenUp) {
            if (proceedButtonDelay > 0)
                proceedButtonDelay -= Gdx.graphics.getDeltaTime();
            if (proceedButtonDelay <= 0) {
                AbstractDungeon.overlayMenu.proceedButton.show();
            }
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        if (this.merchant != null) {
            this.merchant.render(sb);
        }
        if (dummy != null)
            dummy.renderTips(sb); //I'm pretty sure this method is completely unused, but maybe someone patches into it for some reason
    }
}
