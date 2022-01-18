package BattleTowers.powers.abstracts;

import BattleTowers.BattleTowers;
import BattleTowers.util.TextureLoader;
import com.evacipated.cardcrawl.mod.stslib.powers.abstracts.TwoAmountPower;
import com.megacrit.cardcrawl.helpers.ImageMaster;

public abstract class AbstractBTPower extends TwoAmountPower {
    /**
     * @param bigImageName - is the name of the 84x84 image for your power.
     * @param smallImageName - is the name of the 32x32 image for your power.
     */
    public void setImage(String bigImageName, String smallImageName){
        String path128 = BattleTowers.makePowerPath(bigImageName);
        String path48 = BattleTowers.makePowerPath(smallImageName);

        this.region128 = TextureLoader.getTextureAsAtlasRegion(path128);
        this.region48 = TextureLoader.getTextureAsAtlasRegion(path48);
    }

    /**
     * @param imgName - is the name of a 16x16 image. Example: setTinyImage("power.png");
     */
    public void setTinyImage(String imgName){
        this.img = ImageMaster.loadImage(BattleTowers.makePowerPath(imgName));
    }
}