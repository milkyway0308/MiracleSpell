package com.nisovin.magicspells.spells.instant;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.materials.MagicMaterial;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.BoundingBox;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.compat.EventUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Written by ChronoKeeper
 */
public class BlockBeamSpell extends InstantSpell {
    
    MagicMaterial material;
    int removeDelay;
    float rotationX;
    float rotationY;
    float hitRadius;
    float maxDistance;
    float interval;
    double yOffset;
    boolean small;
    String hitSpellName;
    Subspell hitSpell;
    BlockBeamSpell thisSpell;
    Set<List<ArmorStand>> listSet;
    boolean hpFix;

    public BlockBeamSpell(MagicConfig config, String spellName) {
        super(config, spellName);
        thisSpell = this;

        String blockTypeInfo = getConfigString("block-type", "iron_block");
        this.material = MagicSpells.getItemNameResolver().resolveBlock(blockTypeInfo);

        this.maxDistance = getConfigFloat("max-distance", 20.0F);
        this.hitRadius = getConfigFloat("hit-radius", 1F);
        this.removeDelay = getConfigInt("remove-delay", 40);
        this.interval = getConfigFloat("interval", 1F);
        this.yOffset = getConfigFloat("y-offset", 0.5F);
        this.rotationX = getConfigFloat("rotation-x", 0.0F);
        this.rotationY = getConfigFloat("rotation-y", 0.0F);
        this.small = getConfigBoolean("small", false);
        this.hitSpellName = getConfigString("spell", "");
        this.hpFix = getConfigBoolean("use-hp-fix", false);

        if (interval < 0.01) interval = 0.01F;

        listSet = new HashSet<>();

    }

    @Override
    public void initialize() {
        super.initialize();
        hitSpell = new Subspell(hitSpellName);
        if (!hitSpell.process()) MagicSpells.error("BlockBeamSpell " + this.internalName + " has an invalid spell defined!");
    }

    @Override
    public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
        if (state == SpellCastState.NORMAL) {
            List<ArmorStand> entityList = new ArrayList<>();

            World world = player.getWorld();

            Location from = player.getLocation();
            double pitch = from.getPitch() * Math.PI / 180;
            double yaw = from.getYaw() * Math.PI / 180;
            Location eyeLoc = player.getEyeLocation();

            Vector start = eyeLoc.toVector();
            start = start.add(new Vector(0, yOffset, 0));
            Vector direction = eyeLoc.getDirection().multiply(interval);
            Vector position = start.clone();
            Vector armorStandPos;

            float distance = 0;
            while (distance < maxDistance) {
                distance += interval;
                position.add(direction);

                armorStandPos = position.clone();
                armorStandPos = armorStandPos.add(new Vector(0, -2, 0));

                final ArmorStand entity = world.spawn(armorStandPos.toLocation(world), ArmorStand.class);
                entityList.add(entity);
                entity.setHelmet(material.toItemStack());
                entity.setGravity(false);
                entity.setVisible(false);
                entity.setRemoveWhenFarAway(true);
                entity.setCollidable(false);
                entity.setInvulnerable(true);
                entity.setHeadPose(new EulerAngle(pitch + rotationX, yaw + rotationY, 0));
                if (hpFix) {
                    entity.setMaxHealth(2000);
                    entity.setHealth(2000);
                }
                if (small){
                    entity.setSmall(true);
                }

                playSpellEffects(EffectPosition.SPECIAL, position.toLocation(world));

                if (!isTransparent(position.toLocation(world).getBlock())) break;
            }

            BoundingBox box = new BoundingBox(start.toLocation(world), position.toLocation(world));
            box.expand(hitRadius);
            for (LivingEntity e : world.getLivingEntities()) {
                if (e.equals(player)) continue;
                if (e.isDead()) continue;
                if (!box.contains(e)) continue;
                if (validTargetList != null && !validTargetList.canTarget(e)) continue;
                double dist = pointLineDist(start, position, e.getLocation().add(0, 0.8, 0).toVector());
                if (dist < hitRadius) {
                    SpellTargetEvent event = new SpellTargetEvent(this, player, e, power);
                    EventUtil.call(event);
                    if (event.isCancelled()) continue;
                    hitSpell.castAtEntity(player, event.getTarget(), event.getPower());
                    playSpellEffects(EffectPosition.TARGET, event.getTarget());
                }
            }

            listSet.add(entityList);

            MagicSpells.scheduleDelayedTask(() -> { listSet.remove(entityList);  entityList.stream().filter(stand -> !stand.isDead()).forEach(Entity::remove); } , removeDelay);

            playSpellEffects(EffectPosition.CASTER, player);
        }
        return PostCastAction.HANDLE_NORMALLY;

    }

    @Override
    public void turnOff() {
        super.turnOff();
        listSet.forEach(list -> list.forEach(Entity::remove));
    }

    double pointLineDist(Vector p1, Vector p2, Vector p0) {
        Vector v1 = p2.clone().subtract(p1);
        Vector v2 = p1.clone().subtract(p0);
        Vector v3 = v1.clone().crossProduct(v2);
        return v3.length() / v1.length();
    }

}
