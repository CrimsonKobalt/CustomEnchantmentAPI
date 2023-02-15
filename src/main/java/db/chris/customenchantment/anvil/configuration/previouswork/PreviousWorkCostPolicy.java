package db.chris.customenchantment.anvil.configuration.previouswork;

import db.chris.customenchantment.anvil.configuration.previouswork.policy.FreePreviousWorkCostPolicy;
import db.chris.customenchantment.anvil.configuration.previouswork.policy.VanillaPreviousWorkCostPolicy;

@FunctionalInterface
public interface PreviousWorkCostPolicy {

    int cost(int previousAnvilUses);

    PreviousWorkCostPolicy VANILLA = new VanillaPreviousWorkCostPolicy();
    PreviousWorkCostPolicy FREE = new FreePreviousWorkCostPolicy();
}
