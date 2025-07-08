package net.bettercombat.logic;

import net.bettercombat.BetterCombat;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.ComboState;
import net.bettercombat.api.WeaponAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;

import java.util.Arrays;
import java.util.Objects;

import static net.minecraft.entity.EquipmentSlot.MAINHAND;

public class PlayerAttackHelper {
    public static float getDualWieldingAttackDamageMultiplier(PlayerEntity player, AttackHand hand) {
        return isDualWielding(player)
                ? (hand.isOffHand()
                    ? BetterCombat.config.dual_wielding_off_hand_damage_multiplier
                    : BetterCombat.config.dual_wielding_main_hand_damage_multiplier)
                : 1;
    }

    public static boolean shouldAttackWithOffHand(PlayerEntity player, int comboCount) {
        return PlayerAttackHelper.isDualWielding(player) && comboCount % 2 == 1;
    }

    public static boolean isDualWielding(PlayerEntity player) {
        var mainAttributes = WeaponRegistry.getAttributes(player.getMainHandStack());
        var offAttributes = WeaponRegistry.getAttributes(player.getOffHandStack());
        return mainAttributes != null && !mainAttributes.isTwoHanded()
                && offAttributes != null && !offAttributes.isTwoHanded()
                && player.getVehicle() == null;
    }

    public static boolean isTwoHandedWielding(PlayerEntity player) {
        var mainAttributes = WeaponRegistry.getAttributes(player.getMainHandStack());
        if (mainAttributes != null) {
            return mainAttributes.isTwoHanded();
        }
        return false;
    }

    public static float getAttackCooldownTicksCapped(PlayerEntity player) {
        // `getAttackCooldownProgressPerTick` should be called `getAttackCooldownLengthTicks`
        return Math.max(player.getAttackCooldownProgressPerTick(), BetterCombat.config.attack_interval_cap);
    }

    public static AttackHand getCurrentAttack(PlayerEntity player, int comboCount) {
        return getCurrentAttack(player, comboCount, false);
    }

    public static AttackHand getCurrentAttack(PlayerEntity player, int comboCount, boolean isHeavyAttack) {

        if ( isHeavyAttack ) {
            var itemStack = player.getMainHandStack();
            WeaponAttributes attributes = WeaponRegistry.getAttributes(itemStack);
            if ( attributes != null && attributes.heavyAttacks() != null ) {
                WeaponAttributes.Attack[] heavyAttacks = attributes.heavyAttacks();

                for (WeaponAttributes.Attack attack : heavyAttacks) {
                    if (attack.combo() == null || attack.combo() <= comboCount) { // Enough combos to use this attack
                        WeaponAttributes.Attack[] forcedAttacks = new WeaponAttributes.Attack[]{attack};
                        var attackSelection = selectAttack(0, attributes, player, false, forcedAttacks);
                        if (attackSelection == null) {
                            continue;
                        }
                        if (player.getVehicle() != null && Arrays.stream(attackSelection.attack.conditions()).filter(Objects::nonNull).noneMatch(condition -> condition.equals(WeaponAttributes.Condition.MOUNTED))) {
                            continue; // Attack does not specifically have the mounted tag
                        }
                        var combo = attackSelection.comboState;
                        System.out.println(attackSelection.attack.attackRangeMultiplier());
                        System.out.println(attributes.attackRange());
                        System.out.print("Total: ");
                        System.out.println( attributes.attackRange() * attackSelection.attack.attackRangeMultiplier() );

                        WeaponAttributes tempWeaponAttributes = new WeaponAttributes(attributes); // Create new object to avoid permanent multiplication
                        tempWeaponAttributes.setAttackRange(attributes.attackRange() * attackSelection.attack.attackRangeMultiplier() );
                        return new AttackHand(attack, combo, false, tempWeaponAttributes, itemStack);
                    }

                }
                // Move to other attacks if no valid heavies are found
            }
        }

        if ( player.getVehicle() != null ) {// Mounted to something
            var itemStack = player.getMainHandStack();
            WeaponAttributes attributes = WeaponRegistry.getAttributes(itemStack);
            if ( attributes != null && attributes.mountedAttack() != null ) {
                var attack = attributes.mountedAttack();
                WeaponAttributes.Attack[] attacks = new WeaponAttributes.Attack[] { attack };
                var attackSelection = selectAttack(comboCount, attributes, player, false, attacks);
                var combo = attackSelection.comboState;
                WeaponAttributes tempWeaponAttributes = new WeaponAttributes(attributes);
                tempWeaponAttributes.setAttackRange(attributes.attackRange() * attackSelection.attack.attackRangeMultiplier() );
                tempWeaponAttributes.setAttackRange(attributes.attackRange() * BetterCombat.config.mountedRangeMultiplier);
                return new AttackHand(attack, combo, false, tempWeaponAttributes, itemStack);
            } // If the weapon is unsupported the statement never returns, and moves to other checks
        }

        if (isDualWielding(player)) {
            boolean isOffHand = shouldAttackWithOffHand(player,comboCount);
            var itemStack = isOffHand
                    ? player.getOffHandStack()
                    : player.getMainHandStack();
            var attributes = WeaponRegistry.getAttributes(itemStack);
            if (attributes != null && attributes.attacks() != null) {
                int handSpecificComboCount = ((isOffHand && comboCount > 0) ? (comboCount - 1) : (comboCount)) / 2;
                var attackSelection = selectAttack(handSpecificComboCount, attributes, player, isOffHand);
                var attack = attackSelection.attack;
                var combo = attackSelection.comboState;
                WeaponAttributes tempWeaponAttributes = new WeaponAttributes(attributes);
                tempWeaponAttributes.setAttackRange(attributes.attackRange() * attackSelection.attack.attackRangeMultiplier() );
                return new AttackHand(attack, combo, isOffHand, tempWeaponAttributes, itemStack);
            }
        } else {
            var itemStack = player.getMainHandStack();
            WeaponAttributes attributes = WeaponRegistry.getAttributes(itemStack);

            var offhandStack = player.getOffHandStack();
            if( (offhandStack == null || offhandStack.isEmpty() ) && attributes != null && attributes.twoHandedAttacks() != null) {
                var attackSelection = selectAttack(comboCount, attributes, player, false, attributes.twoHandedAttacks());
                var attack = attackSelection.attack;
                var combo = attackSelection.comboState;
                WeaponAttributes tempWeaponAttributes = new WeaponAttributes(attributes);
                tempWeaponAttributes.setAttackRange(attributes.attackRange() * attackSelection.attack.attackRangeMultiplier() );
                return new AttackHand(attack, combo, false, tempWeaponAttributes, itemStack);
            }


            if (attributes != null && attributes.attacks() != null) {
                var attackSelection = selectAttack(comboCount, attributes, player, false);
                var attack = attackSelection.attack;
                var combo = attackSelection.comboState;
                WeaponAttributes tempWeaponAttributes = new WeaponAttributes(attributes);
                tempWeaponAttributes.setAttackRange(attributes.attackRange() * attackSelection.attack.attackRangeMultiplier() );
                return new AttackHand(attack, combo, false, tempWeaponAttributes, itemStack);
            }
        }
        return null;
    }

    private record AttackSelection(WeaponAttributes.Attack attack, ComboState comboState) { }

    private static AttackSelection selectAttack(int comboCount, WeaponAttributes attributes, PlayerEntity player, boolean isOffHandAttack) {
        var attacks = attributes.attacks();
        return selectAttack(comboCount, attributes, player, isOffHandAttack, attacks);
    }

    private static AttackSelection selectAttack(int comboCount, WeaponAttributes attributes, PlayerEntity player, boolean isOffHandAttack, WeaponAttributes.Attack[] attacks ) {
        attacks = Arrays.stream(attacks)
                .filter(attack ->
                        attack.conditions() == null
                                || attack.conditions().length == 0
                                || evaluateConditions(attack.conditions(), player, isOffHandAttack)
                )
                .toArray(WeaponAttributes.Attack[]::new);
        if (comboCount < 0) {
            comboCount = 0;
        }
        if ( attacks.length == 0 ) {
            return null;
        }
        int index = comboCount % attacks.length;
        return new AttackSelection(attacks[index], new ComboState(index + 1, attacks.length));
    }

    private static boolean evaluateConditions(WeaponAttributes.Condition[] conditions, PlayerEntity player, boolean isOffHandAttack) {
        return Arrays.stream(conditions).allMatch(condition -> evaluateCondition(condition, player, isOffHandAttack));
    }

    private static boolean evaluateCondition(WeaponAttributes.Condition condition, PlayerEntity player, boolean isOffHandAttack) {
        if (condition == null) {
            return true;
        }
        switch (condition) {
            case NOT_DUAL_WIELDING -> {
                return !isDualWielding(player);
            }
            case DUAL_WIELDING_ANY -> {
                return isDualWielding(player);
            }
            case DUAL_WIELDING_SAME -> {
                return isDualWielding(player) &&
                        (player.getMainHandStack().getItem() == player.getOffHandStack().getItem());
            }
            case DUAL_WIELDING_SAME_CATEGORY -> {
                if (!isDualWielding(player)) {
                    return false;
                }
                var mainHandAttributes = WeaponRegistry.getAttributes(player.getMainHandStack());
                var offHandAttributes = WeaponRegistry.getAttributes(player.getOffHandStack());
                if (mainHandAttributes.category() == null
                        || mainHandAttributes.category().isEmpty()
                        || offHandAttributes.category() == null
                        || offHandAttributes.category().isEmpty()) {
                    return false;
                }
                return mainHandAttributes.category().equals(offHandAttributes.category());
            }
            case NO_OFFHAND_ITEM -> {
                var offhandStack = player.getOffHandStack();
                if(offhandStack == null || offhandStack.isEmpty()) {{
                    return true;
                }}
                return false;
            }
            case OFFHAND_ITEM -> {
                var offhandStack = player.getOffHandStack();
                if(offhandStack == null || offhandStack.isEmpty()) {{
                    return false;
                }}
                return true;
            }
            case OFF_HAND_SHIELD -> {
                var offhandStack = player.getOffHandStack();
                if(offhandStack != null || offhandStack.getItem() instanceof ShieldItem) {{
                    return true;
                }}
                return false;
            }
            case MAIN_HAND_ONLY -> {
                return !isOffHandAttack;
            }
            case OFF_HAND_ONLY -> {
                return isOffHandAttack;
            }
            case MOUNTED -> {
                return player.getVehicle() != null;
            }
            case NOT_MOUNTED -> {
                return player.getVehicle() == null;
            }
        }
        return true;
    }

    private static final Object attributesLock = new Object();

    public static void offhandAttributes(PlayerEntity player, Runnable runnable) {
        synchronized (attributesLock) {
            setAttributesForOffHandAttack(player, true);
            runnable.run();
            setAttributesForOffHandAttack(player, false);
        }
    }

    public static void setAttributesForOffHandAttack(PlayerEntity player, boolean useOffHand) {
        var mainHandStack = player.getMainHandStack();
        var offHandStack = player.getOffHandStack();
        ItemStack add;
        ItemStack remove;
        if (useOffHand) {
            remove = mainHandStack;
            add = offHandStack;
        } else {
            remove = offHandStack;
            add = mainHandStack;
        }
        if (remove != null) {
            player.getAttributes().removeModifiers(remove.getAttributeModifiers(MAINHAND));
        }
        if (add != null) {
            player.getAttributes().addTemporaryModifiers(add.getAttributeModifiers(MAINHAND));
        }
    }
}
