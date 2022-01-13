
package BattleTowers.vfx;

import BattleTowers.BattleTowers;
import BattleTowers.util.TextureLoader;
import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.*;
import com.esotericsoftware.spine.attachments.RegionAttachment;
import com.megacrit.cardcrawl.characters.*;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.*;

import java.util.HashMap;
import java.util.Map;

public class Hats
{
    private static Map<String, AttachPoint> map = new HashMap<>();
    private static float hatHeight;

    public static void removeHat(Skeleton skeleton, String hatID){
        //Write this if its ever needed some day.
    }

    public static void addHat(AbstractPlayer player, Skeleton skeleton, String hatID, String imgPath) {
        if (player instanceof Ironclad){
            addHat(skeleton, hatID, imgPath,1F,1F, 8F, 0F, -85F); return;
        }
        if (player instanceof TheSilent){
            addHat(skeleton, hatID, imgPath,1.1F,1.1F, 5F, 10F, -20F); return;
        }
        if (player instanceof Defect){
            addHat(skeleton, hatID, imgPath,1.1F,1.1F, 27F, 16F, -40F); return;
        }
        if (player instanceof Watcher){
            addHat(skeleton, hatID, imgPath,.9F,.9F, 14F, -5F, -60F); return;
        }

        addHat(skeleton, hatID, imgPath,1F,1F, 8F, 0F, -45F);
    }



    public static boolean addHat(Skeleton skeleton, String hatID, String imgPath, float scaleX, float scaleY, float offsetX, float offsetY, float angle)
    {
        Bone headbone = null;
        Slot headslot = null;
        String bonename;
        String slotname;
        if (skeleton == null) {
            BattleTowers.logger.info("stopping at skeleton == null");
            return false;
        }


        int headslotIndex = 0;


        Array<Bone> possiblebones = skeleton.getBones();
        for (Bone b: possiblebones){
            bonename = b.toString().toLowerCase();
            /*
            BattleTowers.logger.info(bonename + " " + (bonename.equals("head") ||
                    bonename.equals("skull") ||
                    bonename.equals("neck_3")));

             */
            if (bonename.equals("head") ||
                    bonename.equals("skull") ||
                    bonename.equals("neck_3")
            ){
                //BattleTowers.logger.info("FOUND THE HEAD");
                headbone = b;
                break;
            }

        }

        Array<Slot> possibleslots = skeleton.getSlots();
        for (Slot s: possibleslots){
            slotname = s.getBone().toString().toLowerCase();
            /*
            BattleTowers.logger.info(bonename + " " + (bonename.equals("head") ||
                    bonename.equals("skull") ||
                    bonename.equals("neck_3")));

             */
            if (slotname.equals("head") ||
                    slotname.equals("skull") ||
                    slotname.equals("neck_3")
            ){
                //BattleTowers.logger.info("FOUND THE HEAD");
                headslot = s;
                break;
            }
            headslotIndex++;
        }

        if (headbone == null){
           // BattleTowers.logger.info("stopping at headbone == null");
            return false;
        }

        map.put(hatID,
                new AttachPoint(
                        hatID,
                        headbone.toString(),
                        map.size() + 1,
                        imgPath,
                        scaleX, scaleY,
                        offsetX, offsetY,
                        angle
                ));

        AttachPoint attachPoint = map.get(hatID);
        if (attachPoint == null) {
            BattleTowers.logger.info("stopping at AttachPoint == null");
            return false;
        }
        if (headslot == null) {
            BattleTowers.logger.info("stopping at headslot == null");
            return false;
        }

        String attachName = attachPoint.attachName;
        int slotIndex = headslotIndex;

        if (slotIndex < 0) {
            BattleTowers.logger.info("stopping at slotIndex < 0");
            return false;
        }

        if (attachPoint.attachIndex != null) {
            if (skeleton.findSlotIndex(attachName + attachPoint.attachIndex) < 0) {
                // Create a new slot for the attachment
                Slot origSlot = headslot;
                Slot slotClone = new Slot(new SlotData(origSlot.getData().getIndex(), attachName + attachPoint.attachIndex, origSlot.getBone().getData()), origSlot.getBone());
                slotClone.getData().setBlendMode(origSlot.getData().getBlendMode());
                skeleton.getSlots().insert(slotIndex, slotClone);

                Array<Slot> drawOrder = skeleton.getDrawOrder();
                drawOrder.add(slotClone);
                skeleton.setDrawOrder(drawOrder);

                /*
                // Add new slot to draw order
                Array<Slot> drawOrder = skeleton.getDrawOrder();
                int insertIndex = drawOrder.indexOf(origSlot, true);
                boolean found = false;
                for (int i = 0; i < drawOrder.size; ++i) {
                    Slot slot = drawOrder.get(i);
                    if (slot.getData().getName().startsWith(attachName)) {
                        found = true;
                        String tmp = slot.getData().getName().substring(attachName.length());
                        if (tmp.isEmpty()) {
                            tmp = "0";
                        }
                        int curIndex;
                        try {
                            curIndex = Integer.parseInt(tmp);
                        } catch (NumberFormatException ignore) {
                            continue;
                        }
                        insertIndex = i;
                        if (curIndex > attachPoint.attachIndex) {
                            break;
                        }
                    } else if (found) {
                        insertIndex = i;
                        break;
                    }
                }
                drawOrder.insert(insertIndex, slotClone);

                 */
            }
            attachName = attachName + attachPoint.attachIndex;
        }

        Texture tex = TextureLoader.getTexture(map.get(hatID).imgPath);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        TextureRegion region = new TextureRegion(tex);
        RegionAttachment attachment = new RegionAttachment(attachPoint.ID);
        attachment.setRegion(region);
        attachment.setWidth(tex.getWidth());
        attachment.setHeight(tex.getHeight());
        attachment.setX(attachPoint.x * Settings.scale);
        attachment.setY(attachPoint.y * Settings.scale + ((map.size() -1) * hatHeight));
        attachment.setScaleX(attachPoint.scaleX * Settings.scale);
        attachment.setScaleY(attachPoint.scaleY * Settings.scale);
        attachment.setRotation(attachPoint.angle);
        attachment.updateOffset();

        Skin skin = skeleton.getData().getDefaultSkin();
        skin.addAttachment(slotIndex, attachment.getName(), attachment);

        skeleton.setAttachment(attachName, attachment.getName());
        return true;
    }

    public static class AttachPoint
    {
        final String ID;
        final String attachName;
        final Integer attachIndex;
        final String imgPath;
        final float scaleX;
        final float scaleY;
        final float x;
        final float y;
        final float angle;

        public AttachPoint(
                String id, String attachName, String img,
                float scaleX, float scaleY,
                float x, float y,
                float angle
        )
        {
            this(id, attachName, null, img, scaleX, scaleY, x, y, angle);
        }

        public AttachPoint(
                String id, String attachName, Integer attachIndex, String img,
                float scaleX, float scaleY,
                float x, float y,
                float angle
        )
        {
            ID = id;
            this.attachName = attachName;
            this.attachIndex = attachIndex;
            imgPath = img;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.x = x;
            this.y = y;
            this.angle = angle;
        }
    }
}