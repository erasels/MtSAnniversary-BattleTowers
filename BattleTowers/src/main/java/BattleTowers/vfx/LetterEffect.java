 package BattleTowers.vfx;

 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Graphics;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Interpolation;
 import com.badlogic.gdx.math.Interpolation.ExpIn;
 import com.megacrit.cardcrawl.characters.AbstractPlayer;
 import com.megacrit.cardcrawl.core.Settings;
 import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
 import com.megacrit.cardcrawl.helpers.FontHelper;
 import com.megacrit.cardcrawl.helpers.Hitbox;
 import com.megacrit.cardcrawl.helpers.ImageMaster;
 import com.megacrit.cardcrawl.helpers.input.InputHelper;
 import com.megacrit.cardcrawl.localization.TutorialStrings;
 import com.megacrit.cardcrawl.rooms.AbstractRoom;
 import com.megacrit.cardcrawl.unlock.UnlockTracker;
 import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
 import com.megacrit.cardcrawl.vfx.RefreshEnergyEffect;
 import java.util.ArrayList;

 public class LetterEffect extends AbstractGameEffect
         {
       private static final TutorialStrings tutorialStrings = com.megacrit.cardcrawl.core.CardCrawlGame.languagePack.getTutorialString("Energy Panel Tip");
    
       public static final String[] MSG = tutorialStrings.TEXT;
       public static final String[] LABEL = tutorialStrings.LABEL;
    
       private static final int RAW_W = 256;
    
       private float energyVfxAngle = 0.0F; private float energyVfxScale = Settings.scale;
       private Color energyVfxColor = Color.WHITE.cpy();
       public static float energyVfxTimer = 0.0F;
       public static final float ENERGY_VFX_TIME = 2.0F;
       private static final float VFX_ROTATE_SPEED = -30.0F;

                private static final int ORB_W = 128;
                public static float fontScale = 1.0F;
                private static final float ORB_IMG_SCALE = 1.15F * com.megacrit.cardcrawl.core.Settings.scale;
                private float angle5;
                private float angle4;
             
                public void updateOrb(int orbCount) { if (orbCount == 0) {
                        this.angle5 += Gdx.graphics.getDeltaTime() * -5.0F;
                        this.angle4 += Gdx.graphics.getDeltaTime() * 5.0F;
                        this.angle3 += Gdx.graphics.getDeltaTime() * -8.0F;
                        this.angle2 += Gdx.graphics.getDeltaTime() * 8.0F;
                        this.angle1 += Gdx.graphics.getDeltaTime() * 72.0F;
                      } else {
                        this.angle5 += Gdx.graphics.getDeltaTime() * -20.0F;
                        this.angle4 += Gdx.graphics.getDeltaTime() * 20.0F;
                        this.angle3 += Gdx.graphics.getDeltaTime() * -40.0F;
                        this.angle2 += Gdx.graphics.getDeltaTime() * 40.0F;
                        this.angle1 += Gdx.graphics.getDeltaTime() * 360.0F; } }
             
                private float angle3;
                private float angle2;
                private float angle1;
    
       public LetterEffect() {

           }

       public void update() {
updateOrb();


           }

    
       public void render(SpriteBatch sb)
       {
           sb.setColor(Color.WHITE);
                  sb.draw(ImageMaster.ENERGY_RED_LAYER1, current_x - 64.0F, current_y - 64.0F, 64.0F, 64.0F, 128.0F, 128.0F, ORB_IMG_SCALE, ORB_IMG_SCALE, this.angle1, 0, 0, 128, 128, false, false);

                  sb.draw(ImageMaster.ENERGY_RED_LAYER2, current_x - 64.0F, current_y - 64.0F, 64.0F, 64.0F, 128.0F, 128.0F, ORB_IMG_SCALE, ORB_IMG_SCALE, this.angle2, 0, 0, 128, 128, false, false);

                  sb.draw(ImageMaster.ENERGY_RED_LAYER3, current_x - 64.0F, current_y - 64.0F, 64.0F, 64.0F, 128.0F, 128.0F, ORB_IMG_SCALE, ORB_IMG_SCALE, this.angle3, 0, 0, 128, 128, false, false);

                  sb.draw(ImageMaster.ENERGY_RED_LAYER4, current_x - 64.0F, current_y - 64.0F, 64.0F, 64.0F, 128.0F, 128.0F, ORB_IMG_SCALE, ORB_IMG_SCALE, this.angle4, 0, 0, 128, 128, false, false);
           
                  sb.draw(ImageMaster.ENERGY_RED_LAYER5, current_x - 64.0F, current_y - 64.0F, 64.0F, 64.0F, 128.0F, 128.0F, ORB_IMG_SCALE, ORB_IMG_SCALE, this.angle5, 0, 0, 128, 128, false, false);

                  sb.draw(ImageMaster.ENERGY_RED_LAYER6, current_x - 64.0F, current_y - 64.0F, 64.0F, 64.0F, 128.0F, 128.0F, ORB_IMG_SCALE, ORB_IMG_SCALE, 0.0F, 0, 0, 128, 128, false, false);

       }
    


     }


