package com.yourserver.core.mobs;

import com.yourserver.core.CorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Random;

/**
 * 야간(또는 설정에 따라 상시)에 일정 확률로 좀비/스켈레톤을
 * 체력/공격력이 강화된 네임드 몹으로 승격시키고, 처치 시 별도 보상을 지급한다.
 */
public class CustomMobListener implements Listener {

    private final CorePlugin plugin;
    private final Random random = new Random();
    private final NamespacedKey KEY_CUSTOM_MOB;

    private static final List<String> ZOMBIE_NAMES = List.of("강화 좀비", "부패한 파수꾼", "묘지기 좀비");
    private static final List<String> SKELETON_NAMES = List.of("변종 스켈레톤", "저주받은 궁수", "뼈 사냥꾼");

    public CustomMobListener(CorePlugin plugin) {
        this.plugin = plugin;
        this.KEY_CUSTOM_MOB = new NamespacedKey(plugin, "custom_mob");
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent e) {
        if (!plugin.getConfigManager().customMobsEnabled()) return;
        if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) return;

        EntityType type = e.getEntityType();
        if (type != EntityType.ZOMBIE && type != EntityType.SKELETON) return;

        if (plugin.getConfigManager().mobNightOnly() && !isNight(e.getEntity())) return;

        int chance = plugin.getConfigManager().mobSpawnChance();
        if (random.nextInt(100) + 1 > chance) return;

        promoteToNamedMob(e.getEntity());
    }

    private boolean isNight(LivingEntity entity) {
        if (entity.getWorld() == null) return false;
        long time = entity.getWorld().getTime();
        // 마인크래프트 밤 시간대: 대략 13000 ~ 23000 틱
        return time >= 13000 && time <= 23000;
    }

    private void promoteToNamedMob(LivingEntity entity) {
        double healthMult = plugin.getConfigManager().mobHealthMult();
        double damageMult = plugin.getConfigManager().mobDamageMult();

        AttributeInstance maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(maxHealth.getBaseValue() * healthMult);
            entity.setHealth(maxHealth.getValue());
        }

        AttributeInstance attackDamage = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.setBaseValue(attackDamage.getBaseValue() * damageMult);
        }

        String name;
        if (entity instanceof Zombie) {
            name = ZOMBIE_NAMES.get(random.nextInt(ZOMBIE_NAMES.size()));
        } else if (entity instanceof Skeleton) {
            name = SKELETON_NAMES.get(random.nextInt(SKELETON_NAMES.size()));
        } else {
            name = "강화된 몬스터";
        }

        entity.setCustomName(ChatColor.RED + "☠ " + name);
        entity.setCustomNameVisible(true);
        entity.getPersistentDataContainer().set(KEY_CUSTOM_MOB, PersistentDataType.BYTE, (byte) 1);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        Byte tag = entity.getPersistentDataContainer().get(KEY_CUSTOM_MOB, PersistentDataType.BYTE);
        if (tag == null || tag != 1) return;

        Player killer = entity.getKiller();
        if (killer == null) return;

        double rewardMoney = plugin.getConfigManager().mobRewardMoney();
        double rewardExp = plugin.getConfigManager().mobRewardExp();

        if (plugin.getVaultHook().isReady()) {
            plugin.getVaultHook().deposit(killer, rewardMoney);
        }
        e.setDroppedExp(e.getDroppedExp() + (int) rewardExp);

        // 특별 전리품 (등급별 확장 가능, 여기서는 기본 예시로 금괴 지급)
        e.getDrops().add(new ItemStack(Material.GOLD_INGOT, 1 + random.nextInt(3)));

        killer.sendMessage(ChatColor.GOLD + "강화 몬스터를 처치했습니다! 보상: "
                + plugin.getVaultHook().format(rewardMoney) + ChatColor.GOLD + " + 경험치 " + (int) rewardExp);
    }
}
